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
  private int nodeID = -1;

  // <nodeID, hostname:Port>
  private Map<Integer, String> NodeRecords = new HashMap<Integer, String>();

  @Override
  private void setNumNode(int n){
    NumNode=n;
  }

  public String Join(String IP, String Port){
    if(Joining){
      return "NACK0|Some other node is joining. Please wait...";
    }

    Joining=true;
    if(nodeID>=NumNode){// We have reach the max number of nodes.
      Joining=false;
      return "NACK1|We have reach the max number of nodes.";
    }

    if(NodeRecords.isEmpty()){ // This is the first node.
      nodeID=nodeID+1;
      NodeRecords.put(nodeID,IP+":"+Port);
      Joining=false;
      return "done|"+nodeID;
    }

    Random r = new Random();
    int ContactNodeID = nodeID-r.nextInt((nodeID) + 1);
    nodeID = nodeID+1;
    NodeRecords.put(nodeID,IP+":"+Port);
    // ACK|nodeID:IP:Port:ContactNodeID
    return "ACK|"+nodeID+":"+NodeRecords.get(ContactNodeID)+":"+ContactNodeID;

  }
	public void PostJoin(String IP, String Port){

  }
	public String GetNode(){

  }
}
