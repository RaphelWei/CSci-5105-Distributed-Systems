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
  // public static void SortByKey(){
  //   // TreeMap to store values of HashMap
  //   TreeMap<String, String> sorted = new TreeMap<>();
  //
  //   // Copy all data from hashMap into TreeMap
  //   sorted.putAll(records);
  //   records = new HashMap<>();
  //
  //   // Display the TreeMap which is naturally sorted
  //   for (Map.Entry<String, Integer> entry : sorted.entrySet())
  //       System.out.println("Key = " + entry.getKey() +  ", Value = " + entry.getValue());
  //       records.put(entry.getKey(), entry.getValue());
  // }

  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // when updating finger table, try just update the successor pointer of the immediate predecessor.
  // then we can update other nodes's finger table with existing finger table.
  // the new node just split the range of a current node. the former way to find this existing node would lead to the new node now. and finger table for other keys won't change.

  // special case to handle the second node. at this point, the finger table of the first node all point to itself.
  // each new node would updata its predecessor's fingerTable first.
  // just check if the finger's corresponding key is before the new node.
  // then update fingertable with find_successor_ByKey(?)

  // MD5 comparison with BigInteger class. it has a compareTo method.

  // myIP:myPort:myID
  @Override
  public String UpdateDHT(String SourceInfo){
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Using String to represent MD5 code.
    // need BigInteger(md5, 16) to get numeric operations.
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // find the second half

    String[] keyBoundaries =  KeyRange.split(":");
    BigInteger hBI= new BigInteger(keyBoundaries[0], 16);
    BigInteger tBI= new BigInteger(keyBoundaries[1], 16);
    String middleKey = hBI.add(tBI).divide(new BigInteger(2)).toString();
    String premiddleKey = hBI.add(tBI).divide(new BigInteger(2)).subtract(new BigInteger(1)).toString(16);
    String pairset;

    KeyRange=middleKey+":"+keyBoundaries[1];
    // predecessor, fingerTable(the first one is successor), keyRange
    String[] ret = predecessorInfo+"&"+fingerTable+"&"+keyBoundaries[0]+":"+premiddleKey;
    HandleSuccessorOfPredecessor(SourceInfo, predecessorInfo);
    // updating my predecessorInfo
    predecessorInfo = SourceInfo;
    return ret;
  }
  @Override
  public String getRange(){
    return KeyRange;
  }

  // //wantedNodeMD5:tarAddr:tarPort:tarNodeMD5:wantedIP:wantedPort
  // public String CorrectContactInfo(String InitContactInfo){
  //   String[] InitContactInfoList = InitContactInfo.split(":");
  //   String CorrectContactInfo = find_successor_ByKey(InitContactInfoList[0], NodeID, false);
  // }

  public Void UpdateFingerTable(String sourceKey, String affectKeyIndex){

  }


  public Void HandleSuccessorOfPredecessor(String SourceInfo, String predecessorInfo){
    String predecessorInfoList = predecessorInfo.split(":");
    try{
      TTransport  transport = new TSocket(predecessorInfoList[0], Integer.parseInt(predecessorInfoList[1]));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      WorkWithNodehandler.Client client = new WorkWithNodehandler.Client(protocol);
      //Try to connect
      transport.open();

      String fT = client.getfingerTable();
      String FTList = fT.split("|",2);
      fT = SourceInfo+"|"+FTList[1];
      client.setfingerTable(fT);



    } catch(TException e) {
    }
  }


  // randomly given an ID, which is not in the interval provided by the supernode
  public String find_successor_ByKey(String key, String initID, boolean passedZero){
      // base condition 1: check if current NodeID passed the initID.
      // here the flag check if it is second time to check NodeID.
      BigInteger BIinitID = new BigInteger(initID, 16);
      BigInteger BINodeID = new BigInteger(NodeID, 16);
      if(BIinitID.compareTo(BINodeID)<=0&&passedZero){
        return "NACK2|the key is not in the DHT range!";
      }


      String[] myKeyBoundaries = KeyRange.split(":");
      String[] Fingers = FingerTable.split("|");
      // get ranges between each 2 finger nodeID
      String[] PointedKeyBoundaries = new String[Fingers.length+1];
      PointedKeyBoundaries[0] = NodeID;
      for(int i = 0; i < Fingers.length; i++){
        String[] ContactPoint = Fingers[i].split(":");
        PointedKeyBoundaries[i+1] = ContactPoint[2];
      }
      // find the finger pointing(including the nodeID of current node) to the node before the node wanted.
      String GuessContactPoint;
      BigInteger BIkey = new BigInteger(key, 16);
      // check if key in the range between fingers(including me): [me, figure[figure.length-1]]
      for(int i = 0; i < Fingers.length-1; i++){

        if(BIkey.compareTo(new BigInteger(PointedKeyBoundaries[i+1], 16))<0)&&
        BIkey.compareTo(new BigInteger(PointedKeyBoundaries[i], 16)>0){
          //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          // loop direction; id in ranges or max boundaries in n to range
          if(i==0){ // if node wanted is in the range between me and my successor
            // base condition 2: my successor is the correct contact point.
            return Fingers[i];
          }else{
            // for i>0, found the key is between finger[i-1] and finger[i]
            // contact that node to again call find_successor_ByKey
            GuessContactPoint=Fingers[i-1];
            break;
          }
        }
      }
      // check if key in the range between farthest finger and me: [figure[figure.length-1], me]
      if(BIkey.compareTo(new BigInteger(PointedKeyBoundaries[0], 16))<0&&
      BIkey.compareTo(new BigInteger(PointedKeyBoundaries[Fingers.length], 16)>0){
          GuessContactPoint=Fingers[i];
      }

      // contact the GuessContactPoint to see its finger table.
      try{
        TTransport  transport = new TSocket(GuessContactPoint[0], Integer.parseInt(GuessContactPoint[1]));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithNodehandler.Client client = new WorkWithNodehandler.Client(protocol);
        //Try to connect
        transport.open();


        // as client reach to other nodes to get information back.
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // help base condition to make sure only check when second time current nodeID is the initID.
        // String tarInfo = client.getmyInfo();
        // String[] tarInfoList = tarInfo.split(":")
        String[] myInfoList = myInfo.split(":");

        if(Integer.parseInt(ContactPoint[2])<Integer.parseInt(myInfoList[2])){
          passedZero = true;
        }
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        return client.find_successor_ByKey(ID, initID, passedZero);
      } catch(TException e) {
      }
  }

  // randomly given an ID, which is not in the interval provided by the supernode
  public String find_predeccessor_ByKey(String key, String initID, boolean passedZero){
      // base condition 1: check if current NodeID passed the initID.
      // here the flag check if it is second time to check NodeID.
      BigInteger BIinitID = new BigInteger(initID, 16);
      BigInteger BINodeID = new BigInteger(NodeID, 16);
      if(BIinitID.compareTo(BINodeID)<=0&&passedZero){
        return "NACK2|the key is not in the DHT range!";
      }


      String[] myKeyBoundaries = KeyRange.split(":");
      String[] Fingers = FingerTable.split("|");
      // get ranges between each 2 finger nodeID
      String[] PointedKeyBoundaries = new String[Fingers.length+1];
      PointedKeyBoundaries[0] = NodeID;
      for(int i = 0; i < Fingers.length; i++){
        String[] ContactPoint = Fingers[i].split(":");
        PointedKeyBoundaries[i+1] = ContactPoint[2];
      }
      // find the finger pointing(including the nodeID of current node) to the node before the node wanted.
      String GuessContactPoint;
      BigInteger BIkey = new BigInteger(key, 16);
      // check if key in the range between fingers(including me): [me, figure[figure.length-1]]
      for(int i = 0; i < Fingers.length-1; i++){

        if(BIkey.compareTo(new BigInteger(PointedKeyBoundaries[i+1], 16))<0)&&
        BIkey.compareTo(new BigInteger(PointedKeyBoundaries[i], 16)>0){
          if(i==0){ // if node wanted is in the range between me and my successor
            // base condition 2: my successor is the correct contact point.
            return myInfo;
          }else{
            // for i>0, found the key is between finger[i-1] and finger[i]
            // contact that node to again call find_successor_ByKey
            GuessContactPoint=Fingers[i-1];
            break;
          }
        }
      }
      // check if key in the range between farthest finger and me: [figure[figure.length-1], me]
      if(BIkey.compareTo(new BigInteger(PointedKeyBoundaries[0], 16))<0&&
      BIkey.compareTo(new BigInteger(PointedKeyBoundaries[Fingers.length], 16)>0){
          GuessContactPoint=Fingers[i];
      }

      // contact the GuessContactPoint to see its finger table.
      try{
        TTransport  transport = new TSocket(GuessContactPoint[0], Integer.parseInt(GuessContactPoint[1]));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithNodehandler.Client client = new WorkWithNodehandler.Client(protocol);
        //Try to connect
        transport.open();


        // as client reach to other nodes to get information back.
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // help base condition to make sure only check when second time current nodeID is the initID.
        // String tarInfo = client.getmyInfo();
        // String[] tarInfoList = tarInfo.split(":")
        String[] myInfoList = myInfo.split(":");

        if(Integer.parseInt(ContactPoint[2])<Integer.parseInt(myInfoList[2])){
          passedZero = true;
        }
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        return client.find_predeccessor_ByKey(ID, initID, passedZero);
      } catch(TException e) {
      }
  }
}
