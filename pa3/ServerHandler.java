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

public class ServerHandler implements Server.Iface
{
  String CoordinatorIP;
  String CoordinatorPort;
  // <filename, version>
  ConcurrentHashMap<String, Integer> FileVersion = new ConcurrentHashMap<String, Integer>();
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
    return FileVersion.get(filename);
  }

  @Override
  public String readback(REQ r){

    TTransport  transport = new TSocket(r.getClientIP(), Integer.parseInt(r.getClientPort()));
    TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
    CoordinatorHandler.Client client = new CoordinatorHandler.Client(protocol);
    //Try to connect
    transport.open();
    result2 = client.forwardReq(filename, clientInfo, request);
    transport.close();
    return "ACK";
  }
}
