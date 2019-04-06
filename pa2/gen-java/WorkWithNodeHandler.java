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
import java.util.*;


import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;


public class WorkWithNodeHandler implements WorkWithNode.Iface
{
public HashMap<String, String> record = new HashMap<>();
private static Node me, pred;
//static int m = 5;
//static FingerTable[] finger = new FingerTable[m+1];
//static int DHTRange = (int)Math.pow(2,m);
private static int m;
private static FingerTable[] finger;
private static int DHTRange;
// private static List<Word> wordList = new ArrayList<Word>();

Node getme(){
  return me;
}

FingerTable[] getfingerTable(){
  return finger;
}

void setfingerTable(FingerTable[] finger_){
  finger = finger_;
}

public void setRecord(String title, String genre) {
  record.put(title, genre);
}

public String getRecord(String title) {
  if(this.record.containsKey(title)){
    return this.record.get(title);
  } else {
    return "NACK";
  }
}


public WorkWithNodeHandler(int m_, FingerTable[] finger_, int DHTRange_, Node pred_, Node me_){
  m = m_;
  finger = finger_;
  DHTRange = DHTRange_;
  me = me_;
  pred = pred_;
}
@Override
public Node getSuccessor() //throws RemoteException
{
    return finger[1].getSuccessor();
}

@Override
public Node getPredecessor() //throws RemoteException
{
    return pred;
}

@Override
public void setPredecessor(Node n) // throws RemoteException
{
    pred = n;
}

@Override
public Node find_predecessor(int id)
{
  System.out.println("in find_pred: I am now at nodeID: "+me.getID() +" to find key: "+id);
  Node n = me;
  int myID = n.getID();
  int succID = finger[1].getSuccessor().getID();
  int crossingZero = 1;
  if (myID >= succID)
      crossingZero = 0;

  if ((crossingZero==1 && (id <= myID || id > succID)) ||
          (crossingZero==0 && (id <= myID && id > succID))) {

      Node result = new Node();
      result = closet_preceding_finger(id);

      Node result2 = new Node();
      try{
        TTransport  transport = new TSocket(result.getIP(), Integer.parseInt(result.getPort()));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithNode.Client client = new WorkWithNode.Client(protocol);
        //Try to connect
        transport.open();
        result2 = client.find_predecessor(id);
        result2.setTrace(myID+" -> "+result2.getTrace());
        System.out.println("in find_predecessor, result2.getTrace(): "+result2.getTrace());
        transport.close();

      } catch(TException e) {
      }
      return result2;
  }
  else{
    return n;
  }
}

@Override
public Node closet_preceding_finger(int id)
{
    int crossingZero = 1;
    int myID = me.getID();
    if (myID >= id) {
        crossingZero = 0;
    }

    for (int i = m; i >= 1; i--) {
        int nodeID = finger[i].getSuccessor().getID();
        if (crossingZero == 1) {
            if (nodeID > myID && nodeID < id)
                return finger[i].getSuccessor();
        } else {
            if (nodeID > myID || nodeID < id)
                return finger[i].getSuccessor();
        }
    }
    return me;
}

@Override
public Node find_successor(int id) {
    System.out.println("in find_succ: I am now at nodeID: "+me.getID() +" to find key: "+id);

    Node n;
    n = find_predecessor(id);

    // go to n for its succ.
    Node result = new Node();
    try{
      TTransport  transport = new TSocket(n.getIP(), Integer.parseInt(n.getPort()));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      WorkWithNode.Client client = new WorkWithNode.Client(protocol);
      //Try to connect
      transport.open();
      result = client.getSuccessor();
      result.setTrace(n.getTrace()+" -> "+result.getTrace());
      transport.close();

    } catch(TException e) {
    }

     return result;
}

@Override
public void update_finger_table(Node s, int i) {
   Node p;
   int crossingZero = 1;
   int myID = me.getID();
   int nextID = finger[i].getSuccessor().getID();
   if (myID >= nextID)
       crossingZero = 0;
   else crossingZero = 1;

   if ( ((crossingZero==1 && (s.getID() >= myID && s.getID() < nextID)) ||
   (crossingZero==0 && (s.getID() >= myID || s.getID() < nextID)))&& (me.getID() != s.getID() ) ) {

    finger[i].setSuccessor(s);

    System.out.println("=============update finger table==========================");
    for(int t=1; t<= m; t++){
     System.out.println("finger["+t+"] succ id  " + finger[t].getSuccessor().getID());
     System.out.println("finger["+t+"] start id " + finger[t].getStart());
    }
    System.out.println("=============update finger table==========================");
    System.out.println();

    p = pred;

    try{
      TTransport  transport = new TSocket(p.getIP(), Integer.parseInt(p.getPort()));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      WorkWithNode.Client client = new WorkWithNode.Client(protocol);
      //Try to connect
      transport.open();
      client.update_finger_table(s, i);
      transport.close();

      } catch(TException e) {
      }
  }
}
@Override
public String lookupBook_ByKey(int key, String title, int showpath) {
		Node p = find_successor(key);
		// HashMap<String, String> record1 = new HashMap<>();
    String record1= "";
		try{
          TTransport  transport = new TSocket(p.getIP(), Integer.parseInt(p.getPort()));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);
          //Try to connect
          transport.open();
          record1 = client.getRecord(title);
          if(showpath==1){
            record1 =  record1+"/"+p.getTrace();
          }
          transport.close();

        } catch(TException e) {
        	e.printStackTrace();
        }
        return record1;
	}


@Override
	public String insertRecord_ByKey(int key, String title, String genre, int showpath) {
		Node p = find_successor(key);
		// HashMap<String, String> record1 = new HashMap<>();
		try{
          TTransport  transport = new TSocket(p.getIP(), Integer.parseInt(p.getPort()));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);

          transport.open();
          // record1 = client.getRecord();
          // record1.put(title, genre);
          client.setRecord(title, genre);
          transport.close();
          if(showpath==1){
            return "ACK/"+p.getTrace();
          }else{
            return "ACK";
          }

        } catch(TException e) {
        	e.printStackTrace();
          return "NACK";
        }
	}
  @Override
  public String AllInFoString(){
    String ret = "I am node with nodeID " + me.getID() +". my predecesor is node with nodeID "+ pred.getID()+".\n";
    ret = ret + "\n\n my partial hash table is listed below \n";
    for (Map.Entry<String, String> entry : record.entrySet()) {
        ret = ret + entry.getKey() + ": " + entry.getValue()+"\n";
    }
    ret = ret + "\n\n";
    ret = ret + "===============================================================\n";
    ret = "===============================================================\n" + ret;
    return ret;
  }
}
