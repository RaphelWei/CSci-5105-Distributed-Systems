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
  @Override
  public String UpdateDHT(String Info){
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // do remember, when adding new keys, convert hex MD5 coding to decimal!!!!!!!!!!!
    // int value = Integer.parseInt(hex, 16);
    // therefore, we only have string of decimal for the key range.
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // find the second half
    String middleKey = Integer.toString((Integer.parseInt(records.firstEntry().getKey())+Integer.parseInt(records.lastEntry().getKey()))/2);
    String pairset;

    // one way
    //================================
    NavigableMap<Stirng, String> tailMap = new TreeMap<String,String>(records.tailMap(middleKey));
    //-----------------------------
    // NavigableMap<Stirng, String> tailMap = new TreeMap<String,String>();
    // tailMap.putAll(records.tailMap(middleKey));
    //-----------------------------
    JSONObject pairsetJSON = new JSONObject(records.tailMap(middleKey));
    pairset = pairsetJSON.toString();

    records.navigableKeySet().removeAll(tailMap.navigableKeySet());
    //================================


    // the other way
    //================================
    NavigableMap<Stirng, String> tailMap = new TreeMap<String,String>();
    while(true){
      if(records.lastEntry().getKey()<=middleKey){
        break;
      }
      Map.Entry<String,String> temp = records.pollLastEntry();
      tailMap.put(temp.getKey(), temp.getValue());

    }
    JSONObject pairsetJSON = new JSONObject(tailMap);
    pairset = pairsetJSON.toString();
    //================================

    // predecessor, successor, pairset
    String[] ret = predecessorInfo+"&"+fingerTable[0]+"&"+pairset;

    // String[] ContactInfo = Info.split(":");
    // String sourceIP = ContactInfo[4];
    // String sourcePort = ContactInfo[5];
    // fingerTable[1]=sourceIP+":"+sourcePort;
    // fingertable left!!!!!!!
    // nodeID:IP:Port

    return ret;
  }
  @Override
  public String getRange(){
    return KeyRange;
  }


  // randomly given an ID, which is not in the interval provided by the supernode
  public String[] find_successor_ByKey(String key, String MyID, boolean passedZero){
      // base condition: check if current NodeID passed the initID.
      // here the flag check if it is second time to check NodeID.
      if(Integer.parseInt(initID)<=Integer.parseInt(NodeID)&&passedZero){
        return "NACK2|the key is not in the DHT range!";
      }


      String[] myKeyBoundaries = KeyRange.split(":");
      String[] Fingers = FingerTable.split("|");
      // get ranges between each 2 finger nodeID
      String[] PointedKeyBoundaries = new String[Fingers.length+1];
      PointedKeyBoundaries[0] = NodeID;
      for(int i = 0; i < Fingers.length; i++){
        String[] ContactPoint = Fingers[i].split(":");
        PointedKeyBoundaries[i+1] = ContactPoint[4];
      }
      // find the finger pointing(including the nodeID of current node) to the node before the node wanted.
      String GuessContactPoint;
      for(int i = 0; i < Fingers.length-1; i++){
        if(key<PointedKeyBoundaries[i+1]&&key>PointedKeyBoundaries[i]){
          if(i==0){ // if node wanted is in the range the successor
            return Fingers[i+1];
          }else{
            GuessContactPoint=Fingers[i];
          }
        }
      }
      if(key<PointedKeyBoundaries[0]&&key>PointedKeyBoundaries[Fingers.length]){
          GuessContactPoint=Fingers[i];
      }

      // contact the GuessContactPoint to see its finger table.
      try{
        TTransport  transport = new TSocket(GuessContactPoint[0], Integer.parseInt(GuessContactPoint[1]));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithNode.Client client = new WorkWithNode.Client(protocol);
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

        return client.find_successor_ByNodeID(ID, initID, passedZero);
      } catch(TException e) {
      }
  }

  public String find_predecessor_ByKey(String key, String MyID){

  }

  // public String find_successor_ByNodeID(String ID, String MyID)

  public String find_successor_ByNodeID(String ID, String initID, boolean passedZero){
        // base condition: check if current NodeID passed the initID.
        // here the flag check if it is second time to check NodeID.
        if(Integer.parseInt(initID)<=Integer.parseInt(NodeID)&&passedZero){
          return "NACK2|the key is not in the DHT range!";
        }

        String[] Fingers = FingerTable.split("|");

        // get ranges between each 2 finger nodeID
        String[] PointedNodesIDs = new String[Fingers.length+1];
        PointedNodesIDs[0] = NodeID;
        for(int i = 0; i < Fingers.length; i++){
          String[] ContactPoint = Fingers[i].split(":");
          PointedNodesIDs[i+1] = ContactPoint[2];
        }
        // find the finger pointing(including the nodeID of current node) to the node before the node wanted.
        String GuessContactPoint;
        for(int i = 0; i < Fingers.length-1; i++){
          if(ID<PointedNodesIDs[i+1]&&ID>PointedNodesIDs[i]){
            if(i==0){ // if node wanted is in the range the successor
              return Fingers[i+1];
            }else{
              GuessContactPoint=Fingers[i];
            }
          }
        }
        if(ID<PointedNodesIDs[0]&&ID>PointedNodesIDs[Fingers.length]){
            GuessContactPoint=Fingers[i];
        }

        // contact the GuessContactPoint to see its finger table.
        try{
          TTransport  transport = new TSocket(GuessContactPoint[0], Integer.parseInt(GuessContactPoint[1]));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);
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

          return client.find_successor_ByNodeID(ID, initID, passedZero);
        } catch(TException e) {
        }
  }
}
  // public String[] find_predecessor_ByNodeID(String ID, String MyID){
  //
  // }
