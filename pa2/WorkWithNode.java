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
  public String[] find_successor_ByKey(String key, String MyID){
    try {
      // termintation point.
      if(MyID.equals(NodeID)){
        return "NACK2|the key is not in the DHT range!";
      }
      TTransport  transport = new TSocket(ContactInfo[1], Integer.parseInt(ContactInfo[2]));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      WorkWithNode.Client client = new WorkWithNode.Client(protocol);

      //Try to connect
      transport.open();

      // as client reach to other nodes to get information back.

      String keyrange = client.getRange();
      String[] range = keyrange.split(":");
      if(key>Integer.parseInt(range[0])&&key<=Integer.parseInt(range[1])){
        return NodeID;
      }
    } catch(TException e) {

    }
  }
  // public String find_predecessor_ByKey(String key, String MyID){
  //
  // }

  // public String find_successor_ByNodeID(String ID, String MyID)

  public String find_successor_ByNodeID(String ID, String initID, boolean passedZero){
    try {
        // base condition: check if current NodeID passed the initID.
        // here the flag check if it is second time to check NodeID.
        if(Integer.parseInt(initID)<=Integer.parseInt(NodeID)&&passedZero){
          return "NACK2|the key is not in the DHT range!";
        }
        TTransport  transport = new TSocket(ContactInfo[0], Integer.parseInt(ContactInfo[1]));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithNode.Client client = new WorkWithNode.Client(protocol);

        //Try to connect
        transport.open();
        // // "THIS" is with nodeID after the one want; the correct node is "THIS".
        // if(Integer.parseInt(NodeID)>Integer.parseInt(ID)){
        //   return myInfo;  // IP:port:nodeID
        // }
        // as client reach to other nodes to get information back.
        String FingerTable = client.getFingerTable();
        String[] Fingers = FingerTable.split("|");
        for(int i = 0; i < Fingers.length; i++){
          String[] ContactPoint = Fingers[i].split(":");
          // the successor is with nodeID after the one want; the correct node is "THIS".
          if(Integer.parseInt(ContactPoint[2])>Integer.parseInt(ID)&&i==0){
            return Fingers[i];  // IP:port:nodeID:KeyRange
          }
          // another element in the finger is with nodeID after the one want;
          // contact the one right before and do the same procedure.
          if(Integer.parseInt(ContactPoint[2])>Integer.parseInt(ID)){
            // String[] ContactPoint = Fingers[i-1].split(":");
            try{
              TTransport  transport = new TSocket(ContactInfo[0], Integer.parseInt(ContactInfo[1]));
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
          // all element in this finger table are far before;
          // contact the last one in the finger table and do the same procedure
          if(i==Fingers.length-1){
            try{
              TTransport  transport = new TSocket(ContactPoint[0], Integer.parseInt(ContactPoint[1]));
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

    } catch(TException e) {

    }
  }
}
  // public String[] find_predecessor_ByNodeID(String ID, String MyID){
  //
  // }
