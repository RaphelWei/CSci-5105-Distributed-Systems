import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import org.json.simple.JSONObject;
// import java.io.*;
// import java.util.*;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import java.io.File;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.concurrent.*;

public class WorkWithNodeHandler implements WorkWithNode.Iface
{
  // each "server", node, has a handler in its object.
  // range update!!!!!!!!!!!!!!!
  private String KeyRange = "0:"+2^128-1;// init to 0
  private NavigableMap<Stirng, String> records = new TreeMap<>();
  private String NodeID;

  public Void setKeyRange(String Range){
    KeyRange = Range;
  }

  public Void setNodeID(String MyID){
    NodeID = MyID;
  }

  // IP:port:nodeID
  private String predecessorInfo;
  public Void setpredecessorInfo(String Info){
    predecessorInfo=Info;
  }

  // IP:port:nodeID
  private String myInfo;
  public Void setmyInfo(String Info){
    myInfo=Info;
  }

  // IP:Port:NodeID:KeyRange|IP:Port:NodeID:KeyRange
  private String fingerTable;

  public Void setfingerTable(String content){
    fingerTable=content;
  }
  public String getfingerTable(){
    return fingerTable;
  }



  // the new node just split the range of a current node.

  // special case to handle the second node. at this point, the finger table of the first node all point to itself.
  // each new node would updata its predecessor's fingerTable first.
  // just check if the finger's corresponding key is before the new node.
  // then update fingertable with find_successor_ByKey(?)

  // MD5 comparison with BigInteger class. it has a compareTo method.

  // sourceIP:sourcePort:sourceID
  @Override
  public String UpdateDHT(String SourceInfo){
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Using String to represent MD5 code.
    // need BigInteger(md5, 16) to get numeric operations.
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // find the second half

    String[] keyBoundaries =  KeyRange.split(":");
    // !!!!!!!!!!!!!!!!!!!!!
    // I won't need to get a half of key range; instead, I want to divide my key range by the key of the new node.
    // BigInteger hBI= new BigInteger(keyBoundaries[0], 16);
    // BigInteger tBI= new BigInteger(keyBoundaries[1], 16);
    // String middleKey = hBI.add(tBI).divide(new BigInteger(2)).toString();
    // String premiddleKey = hBI.add(tBI).divide(new BigInteger(2)).subtract(new BigInteger(1)).toString(16);
    String[] SourceInfoList = SourceInfo.split(":");
    String middleKey = SourceInfoList[2]; // new node's NodeMD5
    BigInteger BImiddleKey = new BigInteger(middleKey, 16);
    String premiddleKey = BImiddleKey.subtract(new BigInteger(1)).toString(16);// new node's NodeMD5 - 1
    String pairset;

    KeyRange=middleKey+":"+keyBoundaries[1];
    // predecessor&fingerTable(the first one is successor)&newNode's keyRange
    String[] ret = predecessorInfo+"&"+fingerTable+"&"+keyBoundaries[0]+":"+premiddleKey;

    // SourceInfo: sourceIP:sourcePort:sourceID
    // predecessorInfo: IP:port:nodeID
    // updating my predecessor's successor information
    HandleSuccessorOfPredecessor(SourceInfo, predecessorInfo);

    // updating my predecessorInfo to be the new node
    predecessorInfo = SourceInfo;
    return ret;
  }
  
  public String getRange(){
    return KeyRange;
  }

  // //wantedNodeMD5:tarAddr:tarPort:tarNodeMD5:wantedIP:wantedPort
  // public String CorrectContactInfo(String InitContactInfo){
  //   String[] InitContactInfoList = InitContactInfo.split(":");
  //   String CorrectContactInfo = find_successor_ByKey(InitContactInfoList[0], NodeID, false);
  // }




  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // when updating finger table, try just update the successor pointer of the immediate predecessor.
  // then we can update other nodes's finger table with existing finger table.
  // this only works for find predecessor; and I am using find predecessor right now.
  @Override
  public Void UpdateFingerTable(String SourceInfo, String affectKeyIndex){
    String[] SourceInfoList = SourceInfo.split(":");
    BigInteger s = new BigInteger(SourceInfoList[2]);

    BigInteger BINodeID = new BigInteger(NodeID, 16);

    int i = Integer.parseInt(affectKeyIndex);
    String[] fingerTableList = fingerTable.split("|");
    String nodeFingered = fingerTableList[i].split(":");
    BigInteger nodeFingeredID = new BigInteger(nodeFingered[2]);

    boolean passingZero = false;
    if (BINodeID.compareTo(nodeFingeredID)>=0) {
      passingZero = true;
    }

    if(!passingZero&&(s.compareTo(BINodeID)>=0&&s.compareTo(nodeFingeredID)<=0)||
    passingZero&&(s.compareTo(BINodeID)>=0||s.compareTo(nodeFingeredID)<=0)){
      fingerTableList[i]=SourceInfo;
      fingerTable=fingerTableList[0];
      for(int t = 1; i<fingerTableList.length; i++){
        fingerTable=fingerTable+"|"+fingerTableList[t];
      }


      String predecessorInfoList = predecessorInfo.split(":");
      try{
        TTransport  transport = new TSocket(predecessorInfoList[0], Integer.parseInt(predecessorInfoList[1]));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithNodehandler.Client client = new WorkWithNodehandler.Client(protocol);
        //Try to connect
        transport.open();
        clent.UpdateFingerTable(SourceInfo, affectKeyIndex)

      } catch(TException e) {
      }

    }
  }

  // SourceInfo: sourceIP:sourcePort:sourceID
  // predecessorInfo: IP:port:nodeID
  // let my predecessor's successor be the new node
  public Void HandleSuccessorOfPredecessor(String SourceInfo, String predecessorInfo){
    String predecessorInfoList = predecessorInfo.split(":");
    try{
      TTransport  transport = new TSocket(predecessorInfoList[0], Integer.parseInt(predecessorInfoList[1]));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      WorkWithNodehandler.Client client = new WorkWithNodehandler.Client(protocol);
      //Try to connect
      transport.open();

      String fT = client.getfingerTable();
      String[] FTList = fT.split("|",2); // split out the first finger from others
      fT = SourceInfo+"|"+FTList[1]; // replace the first finger.node with the new node
      client.setfingerTable(fT);



    } catch(TException e) {
    }
  }


  // randomly given an key, which may not be in the interval provided by the supernode
  @Override
  public String find_successor_ByKey(String key, String initID, boolean passedZero){

      // // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! do I really need this??????
      // // I won't overshoot, as I am always looking for the predecessor node of the intended Key.
      // // base condition 1: check if current NodeID passed the initID.
      // // here the flag check if it is second time to check NodeID.
      // BigInteger BIinitID = new BigInteger(initID, 16);
      // BigInteger BINodeID = new BigInteger(NodeID, 16);
      // if(BIinitID.compareTo(BINodeID)<=0&&passedZero){
      //   return "NACK2|the key is not in the DHT range!";
      // }


      String[] myKeyBoundaries = KeyRange.split(":");
      String[] Fingers = FingerTable.split("|");
      // get ranges between each 2 finger nodeID (including the myNodeID)
      String[] PointedKeyBoundaries = new String[Fingers.length+1];
      PointedKeyBoundaries[0] = NodeID;
      for(int i = 0; i < Fingers.length; i++){
        String[] ContactPoint = Fingers[i].split(":");
        PointedKeyBoundaries[i+1] = ContactPoint[2];
      }
      // find the finger pointing(including the nodeID of current node) to the node before the node wanted.
      String GuessContactPoint;// this is the cloest_prceding_finger
      BigInteger BIkey = new BigInteger(key, 16);
      // check if key in the range between fingers(including me)
      // PointedKeyBoundaries: [me, ..., figure[figure.length-1]]
      for(int i = 0; i < Fingers.length-1; i++){

        // !!!!!!!!!!!!!!!!!!
        // problem may arise if higher boundary is smaller than lower boundary.
        // it matters. it is always smaller than upper and greater than lower, but when upper boundary is smaller than lower boundary, the relationship becomes or not and.
        BigInteger BIUB = new BigInteger(PointedKeyBoundaries[i+1], 16);
        BigInteger BILB = new BigInteger(PointedKeyBoundaries[i], 16);
        boolean passingZero=false;
        if(BIUB.compareTo(BILB)<0){
          passingZero=true;
        }

        // !!!!!!!!!!!!!!!!!!!!!!!
        // somewhat to handle error if key is already in a node.
        // !!!!!!!!!!!!!!!!!!!!!!!

        if((!passingZero&&BIkey.compareTo(BIUB)<0&&BIkey.compareTo(BILB)>0)||
        (passingZero&&BIkey.compareTo(BIUB)<0||passingZero&&BIkey.compareTo(BILB)>0)){
          //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          // loop direction; id in ranges or max boundaries in n to range
          // this may not be an issue. I am comparing between each 2 PointedKeyBoundaries not from myNodeID to each fingers (which is done in the paper)
          if(i==0){ // if node wanted is in the range between me and my successor
            // base condition 2: my successor is the correct contact point.
            return Fingers[i]; //IP:Port:NodeID:KeyRange
          }else{
            // for i>0, found the key is between finger[i-1] and finger[i]
            // contact that node to again call find_successor_ByKey
            GuessContactPoint=Fingers[i-1]; //IP:Port:NodeID:KeyRange
            break;
          }
        }
      }
      // check if key in the range between farthest finger and me: [figure[figure.length-1], me]
      if(BIkey.compareTo(new BigInteger(PointedKeyBoundaries[0], 16))<0&&
      BIkey.compareTo(new BigInteger(PointedKeyBoundaries[Fingers.length], 16)>0){
          GuessContactPoint=Fingers[i]; //IP:Port:NodeID:KeyRange
      }

      // contact the GuessContactPoint (cloest_prceding_finger.node) to see its finger table.
      String GuessContactPointList = GuessContactPoint.split(":")
      try{
        TTransport  transport = new TSocket(GuessContactPointList[0], Integer.parseInt(GuessContactPointList[1]));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithNodehandler.Client client = new WorkWithNodehandler.Client(protocol);
        //Try to connect
        transport.open();


        // // as client reach to other nodes to get information back.
        // // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // // help base condition to make sure only check when second time current nodeID is the initID.
        // // I won't overshoot, as I am always looking for the predecessor node of the intended Key.
        // String[] myInfoList = myInfo.split(":");
        //
        // if(Integer.parseInt(ContactPoint[2])<Integer.parseInt(myInfoList[2])){
        //   passedZero = true;
        // }
        // // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        return client.find_successor_ByKey(ID, initID, passedZero);
      } catch(TException e) {
      }
  }

  // randomly given an ID, which is not in the interval provided by the supernode
  @Override
  public String find_predeccessor_ByKey(String key, String initID, boolean passedZero){

          // // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! do I really need this??????
          // // I won't overshoot, as I am always looking for the predecessor node of the intended Key.
          // // base condition 1: check if current NodeID passed the initID.
          // // here the flag check if it is second time to check NodeID.
          // BigInteger BIinitID = new BigInteger(initID, 16);
          // BigInteger BINodeID = new BigInteger(NodeID, 16);
          // if(BIinitID.compareTo(BINodeID)<=0&&passedZero){
          //   return "NACK2|the key is not in the DHT range!";
          // }


          String[] myKeyBoundaries = KeyRange.split(":");
          String[] Fingers = FingerTable.split("|");
          // get ranges between each 2 finger nodeID (including the myNodeID)
          String[] PointedKeyBoundaries = new String[Fingers.length+1];
          PointedKeyBoundaries[0] = NodeID;
          for(int i = 0; i < Fingers.length; i++){
            String[] ContactPoint = Fingers[i].split(":");
            PointedKeyBoundaries[i+1] = ContactPoint[2];
          }
          // find the finger pointing(including the nodeID of current node) to the node before the node wanted.
          String GuessContactPoint;// this is the cloest_prceding_finger
          BigInteger BIkey = new BigInteger(key, 16);
          // check if key in the range between fingers(including me)
          // PointedKeyBoundaries: [me, ..., figure[figure.length-1]]
          for(int i = 0; i < Fingers.length-1; i++){

            // !!!!!!!!!!!!!!!!!!
            // problem may arise if higher boundary is smaller than lower boundary.
            // it matters. it is always smaller than upper and greater than lower, but when upper boundary is smaller than lower boundary, the relationship becomes or not and.
            BigInteger BIUB = new BigInteger(PointedKeyBoundaries[i+1], 16);
            BigInteger BILB = new BigInteger(PointedKeyBoundaries[i], 16);
            boolean passingZero=false;
            if(BIUB.compareTo(BILB)<0){
              passingZero=true;
            }

            // !!!!!!!!!!!!!!!!!!!!!!!
            // somewhat to handle error if key is already in a node.
            // !!!!!!!!!!!!!!!!!!!!!!!

            if((!passingZero&&BIkey.compareTo(BIUB)<0&&BIkey.compareTo(BILB)>0)||
            (passingZero&&BIkey.compareTo(BIUB)<0||passingZero&&BIkey.compareTo(BILB)>0)){
              //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
              // loop direction; id in ranges or max boundaries in n to range
              // this may not be an issue. I am comparing between each 2 PointedKeyBoundaries not from myNodeID to each fingers (which is done in the paper)
              if(i==0){ // if node wanted is in the range between me and my successor
                // base condition 2: my successor is the correct contact point.
                return myInfo+"|"+myKeyRange; //IP:Port:NodeID:KeyRange
              }else{
                // for i>0, found the key is between finger[i-1] and finger[i]
                // contact that node to again call find_successor_ByKey
                GuessContactPoint=Fingers[i-1]; //IP:Port:NodeID:KeyRange
                break;
              }
            }
          }
          // check if key in the range between farthest finger and me: [figure[figure.length-1], me]
          if(BIkey.compareTo(new BigInteger(PointedKeyBoundaries[0], 16))<0&&
          BIkey.compareTo(new BigInteger(PointedKeyBoundaries[Fingers.length], 16)>0){
              GuessContactPoint=Fingers[i]; //IP:Port:NodeID:KeyRange
          }

          // contact the GuessContactPoint (cloest_prceding_finger.node) to see its finger table.
          String GuessContactPointList = GuessContactPoint.split(":")
          try{
            TTransport  transport = new TSocket(GuessContactPointList[0], Integer.parseInt(GuessContactPointList[1]));
            TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
            WorkWithNodehandler.Client client = new WorkWithNodehandler.Client(protocol);
            //Try to connect
            transport.open();


            // // as client reach to other nodes to get information back.
            // // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // // help base condition to make sure only check when second time current nodeID is the initID.
            // // I won't overshoot, as I am always looking for the predecessor node of the intended Key.
            // String[] myInfoList = myInfo.split(":");
            //
            // if(Integer.parseInt(ContactPoint[2])<Integer.parseInt(myInfoList[2])){
            //   passedZero = true;
            // }
            // // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            return client.find_predeccessor_ByKey(ID, initID, passedZero);
          } catch(TException e) {
          }
  }
}
