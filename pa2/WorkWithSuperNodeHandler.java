import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
// import java.io.*;
// import java.util.*;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import java.io.File;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.concurrent.*;

public class WorkWithSuperNodeHandler implements WorkWithSuperNode.Iface
{
  private int NumNode = 0;
  private Boolean Joining = false;
  private int nodeIdx = -1;

  // nodeIDx, hostname:Port:NodeMD5
  private Map<Integer, String> NodeRecords = new HashMap<Integer, String>();

// main!!!!!!!!!!!!!!!!!!!

  @Override
  private void setNumNode(int n){
    NumNode=n;
  }

  public String Join(String IP, String Port){
    synchronized (this) {
        if(Joining){
          return "NACK0|Some other node is joining. Please wait...";
        }

        Joining=true;
    }

    if(nodeIdx>=NumNode){// We have reach the max number of nodes.
      Joining=false;
      return "NACK1|We have reach the max number of nodes.";
    }

    if(NodeRecords.isEmpty()){ // This is the first node.
      nodeIdx=nodeIdx+1;
      String NodeMD5 = getMd5(IP+Port);
      NodeRecords.put(nodeIdx,IP+":"+Port+":"+NodeMD5);
      Joining=false;
      return "done|"+getMd5(IP+Port);
    }

    Random r = new Random();
    // int ContactNodeIdx = nodeIdx-r.nextInt((nodeIdx) + 1);!!!!!!!!!!!!!!!!!!!!!!!!!
    nodeIdx = nodeIdx+1;
    int ContactNodeIdx = r.nextInt((nodeIdx));// random int among [0,nodeIdx)

    String NodeMD5 = getMd5(IP+Port);
    // the record for the new node:  <nodeIdx, IP:Port:NodeMD5>
    NodeRecords.put(nodeIdx,IP+":"+Port+":"+NodeMD5);
    // ACK|newNodeMD5:ContactNodeIP:ContactNodePort:ContactNodeMD5
    return "ACK|"+NodeMD5+":"+NodeRecords.get(ContactNodeIdx);

  }
	public void PostJoin(String IP, String Port){
    synchronized (this) {
        Joining=false;
    }
  }
	public String GetNode(){

  }

  public static String getMd5(String input)
  {
    try {

        // Static getInstance method is called with hashing MD5
        MessageDigest md = MessageDigest.getInstance("MD5");

        // digest() method is called to calculate message digest
        //  of an input digest() return array of byte
        byte[] messageDigest = md.digest(input.getBytes());

        // Convert byte array into signum representation
        BigInteger no = new BigInteger(1, messageDigest);

        // Convert message digest into hex value
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    // For specifying wrong message digest algorithms
    catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
    }
  }
}
