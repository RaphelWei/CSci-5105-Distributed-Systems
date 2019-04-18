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


public class CoordinatorHandler implements Coordinator.Iface
{
  // sth to init!!!!!!!!!!!!!!!!!!!!!
  String CoordinatorIP;
  String CoordinatorPort;
  // Server[] ServerList;
  // Server: IP;Port
  ArrayList<Server> ServerList = new ArrayList<Server>();
  int NR;
  int NW;
  // REQ:OP;Filename;ClientIP;ClientPort
  ArrayList<REQ> reqs = new ArrayList<REQ>();
  // <Filename,OP>
  // note: for R, the number of char R means how many R op working.
  ConcurrentHashMap<String, String> FileOP = new ConcurrentHashMap<String, String>();
  ConcurrentHashMap<String, Integer> FileR = new ConcurrentHashMap<String, Integer>();
  Boolean SYNC = false;

  // @Override
  // public void request(String filename, String clientInfo, String request)
  // {
  //   try{
  //     TTransport  transport = new TSocket(CoordinatorIP, Integer.parseInt(CoordinatorPort));
  //     TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
  //     WorkWithNode.Client client = new WorkWithNode.Client(protocol);
  //     //Try to connect
  //     transport.open();
  //     result2 = client.forwardReq(filename, clientInfo, request);
  //     transport.close();
  //
  //   } catch(TException e) {
  //   }
  // }

  public synchronized void forwardReq(REQ r){
    reqs.add(r);
  }

  @Override
  public void join(Server S){
    ServerList.add(S);
  }

  // size is either NR or NW
  public ArrayList<Server> getQuo(int size){
    // Server[] Quo = new Server[size];
    ArrayList<Server> Quo = new ArrayList<Server>();
    Boolean[] inds = new Boolean[ServerList.size()];
    for(int i=0;i<size;i++){
      int index=0;
      while(true){
        index = getRandomServerID();
        if(!inds[index]){
          inds[index]=true;
          break;
        }
      }
      Quo.add(ServerList.get(index));
    }
    return Quo;
  }

  public int getRandomServerID() {
    Random rand = new Random();
    return rand.nextInt(ServerList.size());
  }

  // ServerCheckList: formed by getQuo; with size NR or NW
  public Server find_newest(ArrayList<Server> ServerCheckList, String Filename){
    Server tarSer = ServerCheckList.get(0);
    int tarVer = 0;
    for (int i=0;i<ServerCheckList.size();i++){
      TTransport  transport = new TSocket(ServerCheckList.get(i).getIP(), Integer.parseInt(ServerCheckList.get(i).getPort()));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      ServerHandler.Client client = new ServerHandler.Client(protocol);
      //Try to connect
      transport.open();
      int NextVerNum = client.getVersion(Filename);
      transport.close();
      if(tarVer<NextVerNum){
        tarVer = NextVerNum;
        tarSer = ServerCheckList.get(i);
      }
    }
    return tarSer;
  }

  public synchronized void ExecReqs(){
    for (int i = 0; i < reqs.size(); i++)
    REQ r = reqs.get(i);
    if(FileOP.get(r.getFilename())==null){
      ExecReq(r);
    }
    else if(FileOP.get(r.getFilename()).equals("W")){
      continue;
    } else if (FileOP.get(r.getFilename())[0].equals("R")){
      if(r.getOP().equals("R")){// R after R; go for it
        FileOP.put(r.getFilename(),FileOP.get(r.getFilename())+"R")
        ExecReq(r);
      } else if(r.getOP().equals("W")){// W after R; go for it
        continue;
      }
    }
  }
  public void ExecR(REQ r){
    Server s = find_newest(getQuo(NR),r.getFilename());

    TTransport  transport = new TSocket(s.getIP(), Integer.parseInt(s.getPort()));
    TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
    ServerHandler.Client client = new ServerHandler.Client(protocol);
    //Try to connect
    transport.open();
    String ret = client.readback(r);
    if(ret.equals("ACK")){
      String flag = FileOP.get(r.getFilename()).substring(1);
      if(flag.equals("")){
        FileOP.remove(r.getFilename());
      } else{
        FileOP.put(r.getFilename(), flag);
      }
    }else{
      System.out.println(ret);
      System.out.println("ExecR: This should not happen!");
    }
    transport.close();
  }

  public void ExecW(REQ r){
    ArrayList<Server> QW = getQuo(NW);
    Server s = find_newest(QW,r.getFilename());

    TTransport  transport = new TSocket(s.get(i).IP, Integer.parseInt(s.get(i).Port));
    TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
    ServerHandler.Client client = new ServerHandler.Client(protocol);
    //Try to connect
    transport.open();
    String ret = client.readback(r);
    if(ret.equals("ACK")){
      String flag = FileOP.get(r.getFilename()).substring(1);
      if(flag.equals("")){
        FileOP.remove(r.getFilename());
      } else{
        FileOP.put(r.getFilename(), flag);
      }
    }else{
      System.out.println(ret);
      System.out.println("ExecR: This should not happen!");
    }
    transport.close();

    for(int i=0; i<QW.size();i++){
      Server s_ = QW.get(i);
      if(!s_.getIP()+s_.getPort().equals(s.getIP()+s.getPort())){
        TTransport  transport = new TSocket(s_.get(i).IP, Integer.parseInt(s_.get(i).Port));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        ServerHandler.Client client = new ServerHandler.Client(protocol);
        //Try to connect
        transport.open();
        client.updateVer(r);
        transport.close();
      }
    }


  }
}
