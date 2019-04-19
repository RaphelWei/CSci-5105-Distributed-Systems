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


public class CoordinatorWorkHandler implements CoordinatorWork.Iface
{
  // sth to init!!!!!!!!!!!!!!!!!!!!!
  String CoordinatorIP;
  String CoordinatorPort;
  // Node: IP;Port
  ArrayList<Node> ServerList = new ArrayList<Node>();
  int NR;
  int NW;
  // REQ:OP;Filename;Content;ClientIP;ClientPort
  ArrayList<REQ> reqs = new ArrayList<REQ>();
  // <Filename,OP>
  // note: for R, the number of char R means how many R op working.
  ConcurrentHashMap<String, String> FileOP = new ConcurrentHashMap<String, String>();
  ArrayList<REQ> FilestoSYNC = new ArrayList<REQ>();
  Boolean SYNC = false;

  CoordinatorWorkHandler(String CoordinatorIP, String CoordinatorPort, int NR, int NW) {
    this.CoordinatorIP = CoordinatorIP;
    this.CoordinatorPort = CoordinatorPort;
    this.NR = NR;
    this.NW = NW;
  }
  // !!!!!!!!!!!!!!!!!!!! synchronized adding request; lock on requests?
  public synchronized void forwardReq(REQ r){
    reqs.add(r);
  }

  @Override
  public void join(Node S){
    ServerList.add(S);
  }

  // size is either NR or NW
  public ArrayList<Node> getQuo(int size){
    // Node[] Quo = new Node[size];
    ArrayList<Node> Quo = new ArrayList<Node>();
    Boolean[] inds = new Boolean[ServerList.size()];
    for(int i=0;i<size;i++){
      int index=0;
      while(true) {
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
  public Node find_newest(ArrayList<Node> ServerCheckList, String Filename){
    Node tarSer = null;
    int tarVer = 0;
    for (int i=0;i<ServerCheckList.size();i++){
      try{
        TTransport  transport = new TSocket(ServerCheckList.get(i).getIP(), Integer.parseInt(ServerCheckList.get(i).getPort()));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
        //Try to connect
        transport.open();
        int NextVerNum = client.getVersion(Filename);
        transport.close();
        if(tarVer<=NextVerNum){
          tarVer = NextVerNum;
          tarSer = ServerCheckList.get(i);
        }
      } catch(Exception e){
        e.printStackTrace();
      }
    }
    return tarSer;
  }

  public void SyncOlder(ArrayList<Node> ServerCheckList, String Filename, int NewestVerNum){
    // Node tarSer = ServerCheckList.get(0);
    // int tarVer = 0;
    for (int i=0;i<ServerCheckList.size();i++){
      try{
        TTransport  transport = new TSocket(ServerCheckList.get(i).getIP(), Integer.parseInt(ServerCheckList.get(i).getPort()));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
        //Try to connect
        transport.open();
        int NextVerNum = client.getVersion(Filename);
        if(NewestVerNum<NextVerNum){
          String ret = client.overWriteFile(r, NewestVerNum);
        }
        transport.close();
      } catch(Exception e){
        e.printStackTrace();
      }
    }
    // return tarSer;
  }

  public void SYNC() {
    if(FileOP.isEmpty()){return;}
    int NewestVerNum;
    for(int i=0; i<FilestoSYNC.size();i++){
      Node newest = find_newest(ServerList, FilestoSYNC.get(i).getFilename()); // newest == null
      if(newest==null{
        System.out.println("SYNC: This file does not exist!" + FilestoSYNC.get(i).getFilename());
        String str = ", does not exist.";
        NACKClient(FilestoSYNC.get(i), str);
        continue;
      }
      try{
        TTransport  transport = new TSocket(newest.getIP(), Integer.parseInt(newest.getPort()));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
        //Try to connect
        transport.open();
        NewestVerNum = client.getVersion(r.getFilename());
        SyncOlder(ServerList,r.getFilename(),NewestVerNum);
        transport.close();
      } catch(Exception e){
        e.printStackTrace();
      }
    }
    SYNC=false;
    FilestoSYNC = new ArrayList<REQ>();
  }

  // do not need threading; this will only loop in coordinator's main.
  public void ExecReqs(){
    // if(SYNC){return;}
    for (int i = 0; i < reqs.size(); i++) {
      REQ r = reqs.get(i);
      if(FileOP.get(r.getFilename())==null){// there is no op running on this file
        if(r.getOP().equals("R")){ // the req want to Read

          Runnable Reading = new Runnable() {
              public void run() {
                  ExecR(r);
              }
          };
          new Thread(Reading).start();

        } else if(r.getOP().equals("W")){ // the req want to Write
          FilestoSYNC.add(r);// adding record for SYNC
          Runnable Writing = new Runnable() {
              public void run() {
                  ExecW(r);
              }
          };
          new Thread(Writing).start();
        }
      } else if (FileOP.get(r.getFilename()).equals("W")){// there is a W op running on this file
        continue; // cannot do anything
      } else if (FileOP.get(r.getFilename())[0].equals("R")){// there is a R op running on this file
        if(r.getOP().equals("R")){ // R after R; go for it
          FileOP.put(r.getFilename(),FileOP.get(r.getFilename())+"R"); // adding count for R op on the file
                                                                      // note: # of 'R' = # of R op on the file
          Runnable Reading = new Runnable() {
              public void run() {
                  ExecR(r);
              }
          };
          new Thread(Reading).start();

        } else if(r.getOP().equals("W")){// W after R; go for it
          continue;
        }
      }
    }
  }

  public void NACKClient(REQ r, String s){
    try{
      TTransport  transport = new TSocket(r.getClientIP(), Integer.parseInt(r.getClientPort()));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
      //Try to connect
      transport.open();
      client.printRet("NACK/filename: "+r.getFilename+s);
      transport.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  // for threading.
  public void ExecR(REQ r){
    Node s = find_newest(getQuo(NR),r.getFilename());
    if(s==null{
      String flag = FileOP.get(r.getFilename()).substring(1); // take out a string like "RRRR" except the first 'R'
      if(flag.equals("")){  // the original string is "R", the one got is ""
        FileOP.remove(r.getFilename()); // no other R op working
      } else{
        FileOP.put(r.getFilename(), flag); // remove the R flag for this request
      }
      System.out.println("ExecR: This file does not exist!");
      String str = ", does not exist.";
      NACKClient(r, str);
      return;
    }

    try{
      TTransport  transport = new TSocket(s.getIP(), Integer.parseInt(s.getPort()));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
      //Try to connect
      transport.open();
      String ret = client.readback(r);
      if(ret.equals("ACK")){
        String flag = FileOP.get(r.getFilename()).substring(1); // take out a string like "RRRR" except the first 'R'
        if(flag.equals("")){  // the original string is "R", the one got is ""
          FileOP.remove(r.getFilename()); // no other R op working
        } else{
          FileOP.put(r.getFilename(), flag); // remove the R flag for this request
        }
      }else{
        System.out.println(ret);
        System.out.println("ExecR: This should not happen!");
      }
      transport.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  public void ExecW(REQ r){
    ArrayList<Node> QW = getQuo(NW);
    Node s = find_newest(QW, r.getFilename());

    if(s==null{
      FileOP.remove(r.getFilename()); //!!!!!!!!!!!!!! here can only use ConcurrentHashMap. Is this fulfilling the requirement?
      System.out.println("ExecR: This file does not exist!");
      String str = ", does not exist.";
      NACKClient(r, str);
      return;
    }

    int NewestVerNum;

    TTransport  transport = new TSocket(s.getIP(), Integer.parseInt(s.getPort()));
    TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
    ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
    //Try to connect
    transport.open();
    String ret = client.writeback(r);
    NewestVerNum = client.getVersion(Filename);
    if(ret.equals("ACK")){
      FileOP.remove(r.getFilename()); //!!!!!!!!!!!!!! here can only use ConcurrentHashMap. Is this fulfilling the requirement?
    }else{
      System.out.println(ret);
      System.out.println("ExecW: This should not happen!");
    }
    transport.close();

    for(int i=0; i<QW.size();i++){
      Node s_ = QW.get(i);
      if(!s_.getIP()+s_.getPort().equals(s.getIP()+s.getPort())){
        try{
          TTransport  transport = new TSocket(s_.getIP(), Integer.parseInt(s_.getPort()));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
          //Try to connect
          transport.open();
          client.overWriteFile(r, NewestVerNum);
          transport.close();
        }
      }
    }
  }
}
