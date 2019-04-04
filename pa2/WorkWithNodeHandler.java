import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

// import org.json.simple.JSONObject;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NavigableMap;
import java.util.TreeMap;


public class WorkWithNodeHandler implements WorkWithNode.Iface
{
  // each "server", node, has a handler in its object.
  // range update!!!!!!!!!!!!!!!
  // private String KeyRange = "0:"+"ffffffffffffffffffffffffffffffff";// init to 0 to maximum of MD5
  private NavigableMap<String, String> records = new TreeMap<>();
  private String NodeID;

  // public void setKeyRange(String Range){
  //   KeyRange = Range;
  // }
  // public String getKeyRange(){
  //   return KeyRange;
  // }

  public void setNodeID(String MyID){
    NodeID = MyID;
  }

  // IP:port:nodeID
  private String predecessorInfo;
  public void setpredecessorInfo(String Info){
    predecessorInfo=Info;
  }
  public String getpredecessorInfo(){
    return predecessorInfo;
  }

  // IP:port:nodeID
  private String myInfo;
  public void setmyInfo(String Info){
    myInfo=Info;
  }
  public String getmyInfo(){
    return myInfo;
  }
  // IP:Port:NodeID:KeyRange|IP:Port:NodeID:KeyRange
  private String fingerTable;
  @Override
  public void setfingerTable(String content){
    fingerTable=content;
  }
  @Override
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
System.out.println("inside UpdateDHT");
    // find the second half

    // String[] keyBoundaries =  KeyRange.split(":");
    // !!!!!!!!!!!!!!!!!!!!!
    // I won't need to get a half of key range; instead, I want to divide my key range by the key of the new node.
    // BigInteger hBI= new BigInteger(keyBoundaries[0], 16);
    // BigInteger tBI= new BigInteger(keyBoundaries[1], 16);
    // String middleKey = hBI.add(tBI).divide(new BigInteger(2)).toString();
    // String premiddleKey = hBI.add(tBI).divide(new BigInteger(2)).subtract(new BigInteger(1)).toString(16);
    String[] SourceInfoList = SourceInfo.split(":");
    String middleKey = SourceInfoList[2]; // new node's NodeMD5
    BigInteger BImiddleKey = new BigInteger(middleKey, 16);
    String premiddleKey = BImiddleKey.subtract(BigInteger.valueOf(1)).toString(16);// new node's NodeMD5 - 1
    String pairset;

    // KeyRange=middleKey+":"+keyBoundaries[1];
    // predecessor&fingerTable(the first one is successor)&newNode's keyRange
    String ret = predecessorInfo+"&"+fingerTable;
System.out.println("jumpping in HandleSuccessorOfPredecessor");
    // SourceInfo: sourceIP:sourcePort:sourceID
    // predecessorInfo: IP:port:nodeID
    // updating my predecessor's successor information
    HandleSuccessorOfPredecessor(SourceInfo, predecessorInfo);
System.out.println("left in HandleSuccessorOfPredecessor");
    // updating my predecessorInfo to be the new node
    predecessorInfo = SourceInfo;
    return ret;
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
  public void UpdateFingerTable(String SourceInfo, String affectKeyIndex, String initAffectedNode){
    System.out.println("inside UpdateFingerTable SourceInfo:     " + SourceInfo);
    System.out.println("inside UpdateFingerTable affectKeyIndex: " + affectKeyIndex);
    String[] SourceInfoList = SourceInfo.split(":");
    BigInteger s = new BigInteger(SourceInfoList[2], 16);

    BigInteger BINodeID = new BigInteger(NodeID, 16);

    int i = Integer.parseInt(affectKeyIndex);
    String[] fingerTableList = fingerTable.split("\\|");
    String[] nodeFingered = fingerTableList[i].split(":");
    BigInteger nodeFingeredID = new BigInteger(nodeFingered[2], 16);

    boolean passingZero = false;
    if (BINodeID.compareTo(nodeFingeredID)>=0) {
      passingZero = true;
    }

    if(!passingZero&&(s.compareTo(BINodeID)>=0&&s.compareTo(nodeFingeredID)<=0)||
    passingZero&&(s.compareTo(BINodeID)>=0||s.compareTo(nodeFingeredID)<=0)){
System.out.println("got one entry to update:              "+SourceInfo);
System.out.println("got one entry to update the index is: "+i);
      fingerTableList[i]=SourceInfo;
      fingerTable=fingerTableList[0];
      for(int t = 1; t<fingerTableList.length; t++){
        fingerTable=fingerTable+"|"+fingerTableList[t];
      }
      // System.out.println();
      // System.out.println();
      // System.out.println();
      // // if(affectKeyIndex.equals("127")){
        // System.out.println(fingerTable);
      // // }
      // System.out.println();
      // System.out.println();

      if(initAffectedNode.equals(predecessorInfo)){
        return;
      }

      String[] predecessorInfoList = predecessorInfo.split(":");
      try{
        TTransport  transport = new TSocket(predecessorInfoList[0], Integer.parseInt(predecessorInfoList[1]));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithNode.Client client = new WorkWithNode.Client(protocol);
        //Try to connect
        transport.open();
System.out.println("get to predecessor to update, predecessor: "+predecessorInfo);
        client.UpdateFingerTable(SourceInfo, affectKeyIndex, initAffectedNode);
System.out.println("return from predecessor");
        transport.close();

      } catch(TException e) {
      }

    }
    else{
      System.out.println("this one entry do not need to be updated");
    }
  }

  // SourceInfo: sourceIP:sourcePort:sourceID
  // predecessorInfo: IP:port:nodeID
  // let my predecessor's successor be the new node
  public void HandleSuccessorOfPredecessor(String SourceInfo, String predecessorInfo){
    System.out.println("inside HandleSuccessorOfPredecessor");
    String[] predecessorInfoList = predecessorInfo.split(":");
    try{
      TTransport  transport = new TSocket(predecessorInfoList[0], Integer.parseInt(predecessorInfoList[1]));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      WorkWithNode.Client client = new WorkWithNode.Client(protocol);
      //Try to connect
      transport.open();
System.out.println("going in getfingerTable");
      String fT = client.getfingerTable();
System.out.println("left getfingerTable");
      String[] FTList = fT.split("\\|",2); // split out the first finger from others
      fT = SourceInfo+"|"+FTList[1]; // replace the first finger.node with the new node
System.out.println("going in  setfingerTable");
      client.setfingerTable(fT);
System.out.println("left setfingerTable");
      transport.close();


    } catch(TException e) {
    }
  }


  // randomly given an key, which may not be in the interval provided by the supernode
  // return: IP:Port:NodeID
  public String find_successor_ByKey(String key){
System.out.println("inside find_successor_ByKey");
    String PredInfo = find_predeccessor_ByKey(key);
System.out.println("inside find_successor_ByKey; PredInfo: "+PredInfo);
    String[] PredInfoList = PredInfo.split(":");
    String SuccInfo = "God, Will I be able to make it? I cannot find pred again!!!!!";

    try{
      TTransport  transport = new TSocket(PredInfoList[0], Integer.parseInt(PredInfoList[1]));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      WorkWithNode.Client client = new WorkWithNode.Client(protocol);
      //Try to connect
      transport.open();
      String PredFingerTable = client.getfingerTable();
      String[] PredFingers = PredFingerTable.split("\\|");
      SuccInfo = PredFingers[0];
      System.out.println("inside find_successor_ByKey; SuccInfo: "+SuccInfo);
      transport.close();

    } catch(TException e) {
      e.printStackTrace();
    }
    return SuccInfo;
  }


  // randomly given an key, which may not be in the interval provided by the supernode
  // return: IP:Port:NodeID
  public String find_predeccessor_ByKey(String key){

    // String[] myKeyBoundaries = KeyRange.split(":");
    String[] Fingers = fingerTable.split("\\|");
    BigInteger BIkey = new BigInteger(key, 16);
    String[] mySucc = Fingers[0].split(":");
    BigInteger BImySuccID = new BigInteger(mySucc[2], 16);
    BigInteger BImyNodeID = new BigInteger(NodeID, 16);

    System.out.println("inside find_predeccessor_ByKey key:             "+key);
    System.out.println("inside find_predeccessor_ByKey nodeID           "+NodeID);
    System.out.println("inside find_predeccessor_ByKey mySucc           "+Fingers[0]);
    System.out.println("inside find_predeccessor_ByKey key VS mySuccID: "+BIkey.compareTo(BImySuccID));


    boolean passingZero=false;
    if(BImySuccID.compareTo(BImyNodeID)<=0){
      passingZero=true;
    }
    System.out.println("inside find_predeccessor_ByKey passingZero      "+passingZero);

    if((!passingZero&&(BIkey.compareTo(BImySuccID)>0||BIkey.compareTo(BImyNodeID)<=0))||
    (passingZero&&BIkey.compareTo(BImySuccID)>0&&passingZero&&BIkey.compareTo(BImyNodeID)<=0)){

      System.out.println("inside find_predeccessor_ByKey: key is not between me and my succ ");
      // System.out.println("I got a guess for contact point. " + GuessContactPoint);
      String successorInfo = cloest_prceding_finger(key);// IP:Port:NodeID
      System.out.println("I got the contact point.         " + successorInfo);
      String[] successorInfoList = successorInfo.split(":");

      try{
        TTransport  transport = new TSocket(successorInfoList[0], Integer.parseInt(successorInfoList[1]));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithNode.Client client = new WorkWithNode.Client(protocol);
        //Try to connect
        transport.open();
        String ret =  client.find_predeccessor_ByKey(key);
        transport.close();
        return ret;

      } catch(TException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("inside find_predeccessor_ByKey: key is between me and my succ going to return");
    }
    return myInfo;
  }

  public String cloest_prceding_finger(String id){
    BigInteger BItarID = new BigInteger(id, 16);
    String[] Fingers = fingerTable.split("\\|");
    for(int i=Fingers.length-1; i>=0;i--){
      String[] fingerPointed = Fingers[i].split(":");
      BigInteger BIfingerID = new BigInteger(fingerPointed[2], 16);
      BigInteger BImyNodeID = new BigInteger(NodeID, 16);

      boolean passingZero=false;
      if(BItarID.compareTo(BImyNodeID)<0){
        passingZero=true;
      }

      if((!passingZero&&BIfingerID.compareTo(BItarID)<0&&BIfingerID.compareTo(BImyNodeID)>0)||
      (passingZero&&BIfingerID.compareTo(BItarID)<0||passingZero&&BIfingerID.compareTo(BImyNodeID)>0)){
        return Fingers[i];
      }

    }
    return myInfo;
  }

}
