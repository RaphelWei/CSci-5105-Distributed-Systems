import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import java.math.*;
// import java.rmi.*;
// import java.rmi.server.*;
// import java.security.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class WorkWithSuperNodeHandler implements WorkWithSuperNode.Iface{
  private static int numNodes;
  private static int busy;
  //int m = 5;
  //int numDHT = (int)Math.pow(2,m);
  //Node[] nodeList = new Node[numDHT];
  private static int m;
  private static int numDHT;
  private static Node[] nodeList;
  private List<Integer> nodeIDList;

  public WorkWithSuperNodeHandler(int maxNumNodes)
  {
    numNodes = 0;
    busy = 0;
    m = (int) Math.ceil(Math.log(maxNumNodes) / Math.log(2));
    numDHT = (int)Math.pow(2,m);
    Node[] nodeList = new Node[numDHT];
    nodeIDList = new ArrayList<Integer>();
  }
  @Override
  public String getNodeInfo(String nodeIP, String nodePort) {
      if (busy == 0) {

          synchronized (this) {
              busy = 1;
          }

          int nodeID = 0;
          String initInfo = "";
          numNodes++;
          System.out.println("*** Node Initation Call: Connection from " + nodeIP);
          try{
              MessageDigest md = MessageDigest.getInstance("SHA1");
              md.reset();
              String hashString = nodeIP+ nodePort;
              md.update(hashString.getBytes());
              byte[] hashBytes = md.digest();
              BigInteger hashNum = new BigInteger(1,hashBytes);

              nodeID = Math.abs(hashNum.intValue()) % numDHT;

              System.out.println("Generated ID: " + nodeID + " for requesting node");

              while(nodeList[nodeID] != null) { //ID Collision
                  md.reset();
                  md.update(hashBytes);
                  hashBytes = md.digest();
                  hashNum = new BigInteger(1,hashBytes);
                  nodeID = Math.abs(hashNum.intValue()) % numDHT;
                  System.out.println("ID Collision, new ID: " + nodeID);
              }
              System.out.println("Generated ID: " + nodeID + " Collision Fixed");

              if (nodeList[nodeID] == null) {
                  nodeList[nodeID] = new Node(nodeID,nodeIP,nodePort);
                  nodeIDList.add(nodeID);
                  System.out.println("New node added ... ");
              }
              System.out.println("Generated ID: " + nodeID + " new node added");


              Collections.sort(nodeIDList,Collections.reverseOrder());
              System.out.println("Generated ID: " + nodeID + " sorting");
              int predID = nodeID;
              Iterator<Integer> iterator = nodeIDList.iterator();
              while (iterator.hasNext()) {
                  int next = iterator.next();
                  if (next < predID) {
                      predID = next;
                      break;
                  }
              }
              System.out.println("Generated ID: " + predID + " pred");
              if (predID == nodeID)
                  predID = Collections.max(nodeIDList);

              initInfo = nodeID + "/" + predID + "/" + nodeList[predID].getIP() + "/" + nodeList[predID].getPort();

          } catch (NoSuchAlgorithmException nsae){}

            System.out.println(initInfo);
          return initInfo;

      } else {
          return "NACK";
      }
  }

  public String getRandomNode() {
      Random rand = new Random();
      int randID = rand.nextInt(nodeIDList.size());
      int index = nodeIDList.get(randID);
      String result = nodeList[index].getIP() + ":" + nodeList[index].getPort();
      return result;
  }

  public String getNodeList() {
      String result = "";
      Collections.sort(nodeIDList);
      result = result + nodeIDList.size() + "/";
      Iterator<Integer> iterator = nodeIDList.iterator();
      while (iterator.hasNext()) {
          int next = iterator.next();
          result = result + nodeList[next].getID() + ":" + nodeList[next].getIP() + ":" + nodeList[next].getPort() + "/";
      }

      return result;
  }
  @Override
  public void finishJoining(int id) {
      System.out.println("*** Post Initiation Call: Node " +id + " is in the DHT.");
      System.out.println("Current number of nodes = " + numNodes + "\n");
      synchronized (this) {
          busy = 0;
      }
  }
}
