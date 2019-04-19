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

import java.util.concurrent.*;

public class ServerWorkHandler implements ServerWork.Iface
{
  String IP;
  String Port;
  String CoordinatorIP;
  String CoordinatorPort;
  // <filename, version>
  // HashMap<String, Integer> FileVersion = new HashMap<String, Integer>();
  // HashMap<String, String> FileContent = new HashMap<String, String>();
  @Override
  public void request(String filename, String clientInfo, String request)
  {
    try{
      TTransport  transport = new TSocket(CoordinatorIP, Integer.parseInt(CoordinatorPort));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      CoordinatorHandler.Client client = new CoordinatorHandler.Client(protocol);
      //Try to connect
      transport.open();
      result2 = client.forwardReq(filename, clientInfo, request);
      transport.close();

    } catch(TException e) {
    }
  }

  @Override
  public int getVersion(String filename){
    String ALine = readFile(filename); // content:"filename/version"
    String[] AList = ALine.split("/");
    reader.close();

   return Integer.toString(AList[1]);
  }

  @Override
  public String readback(REQ r){

    TTransport  transport = new TSocket(r.getClientIP(), Integer.parseInt(r.getClientPort()));
    TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
    CoordinatorHandler.Client client = new CoordinatorHandler.Client(protocol);
    //Try to connect
    transport.open();
    result2 = client.printRet("ACKR/filename: "+filename+", "+readFile(r.getFilename()));
    transport.close();
    return "ACK";
  }
  @Override
  public String writeback(REQ r){
    writeFile(r);

    TTransport  transport = new TSocket(r.getClientIP(), Integer.parseInt(r.getClientPort()));
    TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
    CoordinatorHandler.Client client = new CoordinatorHandler.Client(protocol);
    //Try to connect
    transport.open();
    result2 = client.printRet("ACKW/filename: "+filename+", "+FileVersion.get(filename));
    transport.close();
    return "ACK";
  }

  public void writeFile(REQ r){
    String directoryName = "./"+IP+":_"+Port;
    File directory = new File(directoryName);
    if (! directory.exists()){
        directory.mkdir();
    }

    File file = new File(directoryName + "/" + r.getFilename());
    try{
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(r.getContent()+"/"+(getVersion(r.getFilename())+1));
        bw.close();
    }
    catch (IOException e){
        e.printStackTrace();
        System.exit(-1);
    }
  }

  // assuming the content is a line saying "filename/version"
  public String readFile(String filename) {
     // String expected_value = "Hello world";
     String file ="./"+IP+":_"+Port+"/"+filename;

     BufferedReader reader = new BufferedReader(new FileReader(file));
     String ALine = reader.readLine();// content:"filename/version"
     reader.close();

    return ALine;
  }
}
