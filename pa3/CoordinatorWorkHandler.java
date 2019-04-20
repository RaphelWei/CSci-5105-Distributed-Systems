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
  int N;
  // REQ:OP;Filename;Content;ClientIP;ClientPort
  // ArrayList<REQ> reqs = new ArrayList<REQ>();
  ConcurrentLinkedQueue<REQ> reqs = new ConcurrentLinkedQueue<REQ>();
  // <Filename,OP>
  // note: for R, the number of char R means how many R op working.
  ConcurrentHashMap<String, String> FileOP = new ConcurrentHashMap<String, String>();
  // <filename, REQ>
  ConcurrentHashMap<String, REQ> FilestoSYNC = new ConcurrentHashMap<String, REQ>();
  // ArrayList<REQ> FilestoSYNC = new ArrayList<REQ>();
  Boolean SYNC = false;

  CoordinatorWorkHandler(String CoordinatorIP, String CoordinatorPort, int NR, int NW, int N) {
    this.CoordinatorIP = CoordinatorIP;
    this.CoordinatorPort = CoordinatorPort;
    this.NR = NR;
    this.NW = NW;
    this.N = N;
  }
  // !!!!!!!!!!!!!!!!!!!! synchronized adding request; lock on requests?
  @Override
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

  public void SyncOlder(ArrayList<Node> ServerCheckList, String Filename, REQ r, int NewestVerNum){
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
        if(NewestVerNum>NextVerNum||NextVerNum==-1){
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
    if(FileOP.isEmpty()){
      SYNC = false;
      return;
    }
    Iterator it = FilestoSYNC.entrySet().iterator();
    int NewestVerNum;
    while(it.hasNext()){
      Map.Entry pair = (Map.Entry)it.next();
      Node newest = find_newest(ServerList, pair.getKey()); // newest == null
      if(newest==null{
        System.out.println("SYNC: This file does not exist!" + pair.getKey());
        String str = ", does not exist.";
        NACKClient(pair.getValue(), str);
        continue; // directly continue; FilestoSYNC always got re-init at the end of this function.
      }
      try{
        TTransport  transport = new TSocket(newest.getIP(), Integer.parseInt(newest.getPort()));
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
        //Try to connect
        transport.open();
        NewestVerNum = client.getVersion(pair.getKey());
        if(NewestVerNum==-1){ // the written file was removed !!!!!!!!!!!!!!!!!
          System.out.println("SYNC: not right! I cannot find a file I wrote before");
          NewestVerNum = 0;
        }
        SyncOlder(ServerList,pair.getKey(),pair.getValue(),NewestVerNum);
        transport.close();
      } catch(Exception e){
        e.printStackTrace();
      }
    }



    // for(int i=0; i<FilestoSYNC.size();i++){
    //   Node newest = find_newest(ServerList, FilestoSYNC.get(i).getFilename()); // newest == null
    //   if(newest==null{
    //     System.out.println("SYNC: This file does not exist!" + FilestoSYNC.get(i).getFilename());
    //     String str = ", does not exist.";
    //     NACKClient(FilestoSYNC.get(i), str);
    //     continue; // directly continue; FilestoSYNC always got re-init at the end of this function.
    //   }
    //   try{
    //     TTransport  transport = new TSocket(newest.getIP(), Integer.parseInt(newest.getPort()));
    //     TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
    //     ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
    //     //Try to connect
    //     transport.open();
    //     NewestVerNum = client.getVersion(r.getFilename());
    //     SyncOlder(ServerList,r.getFilename(),NewestVerNum);
    //     transport.close();
    //   } catch(Exception e){
    //     e.printStackTrace();
    //   }


    }
    SYNC=false;
    FilestoSYNC = new ConcurrentHashMap<String, REQ>();
  }

  // do not need threading; this will only loop in coordinator's main.
  // !!!!!!!!!!!!!!!!!!!!!!! I need to remove item in reqs, which is not thread-safe as I may add request at any time
  // !!!!!!!!!!!!!!!!!!!!!!! I added a synchronized queue, which may violate the lock demand
  public void ExecReqs(){
    // if(SYNC){return;}
// System.out.println("ExecReqs");
    Iterator iterator = reqs.iterator();
    while (iterator.hasNext()) {
// System.out.println("while loop");
      REQ r = iterator.next();
// System.out.println("Request Content: "+r.getOP()+" on "+r.getFilename());
      if(FileOP.get(r.getFilename())==null){// there is no op running on this file
// System.out.println("there is no op running on this file: "+r.getFilename());
        if(r.getOP().equals("R")){ // the req want to Read
// System.out.println("Request: R on "+r.getFilename());
          Runnable Reading = new Runnable() {
              public void run() {
                  ExecR(r);
              }
          };
          new Thread(Reading).start();
          reqs.remove(r);
        } else if(r.getOP().equals("W")){ // the req want to Write
// System.out.println("Request: W on "+r.getFilename());
          FilestoSYNC.put(r.getFilename(), r);// adding record for SYNC
          Runnable Writing = new Runnable() {
              public void run() {
                  ExecW(r);
              }
          };
          new Thread(Writing).start();
          reqs.remove(r);
        }

      } else if (FileOP.get(r.getFilename()).equals("W")){// there is a W op running on this file
// System.out.println("there is a W op running on this file: "+r.getFilename());
        continue; // cannot do anything
      } else if (FileOP.get(r.getFilename())[0].equals("R")){// there is a R op running on this file
// System.out.println("there is a R op running on this file: "+r.getFilename());
        if(r.getOP().equals("R")){ // R after R; go for it
// System.out.println("After R, Request: R on "+r.getFilename());
          FileOP.put(r.getFilename(),FileOP.get(r.getFilename())+"R"); // adding count for R op on the file
                                                                      // note: # of 'R' = # of R op on the file
          Runnable Reading = new Runnable() {
              public void run() {
                  ExecR(r);
              }
          };
          new Thread(Reading).start();
          reqs.remove(r);

        } else if(r.getOP().equals("W")){// W after R; next loop
// System.out.println("After W, Request: R on "+r.getFilename());
          continue;
        }
      }
    }
  }

  public void NACKClient(REQ r, String s){
System.out.println("NACKClient");
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
System.out.println("ExecR");
    Node s = find_newest(getQuo(NR),r.getFilename());
    if(s==null{ // cannot find the file or the file is removed
      String flag = FileOP.get(r.getFilename()).substring(1); // take out a string like "RRRR" except the first 'R'
      if(flag.equals("")){ // no other R op working
        FileOP.remove(r.getFilename());
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
System.out.println("ExecW");
    ArrayList<Node> QW = getQuo(NW);
System.out.println("QW size: "+ QW.size());
    Node s = find_newest(QW, r.getFilename());
    if(s==null{
      FileOP.remove(r.getFilename()); //!!!!!!!!!!!!!! here can only use ConcurrentHashMap. Is this fulfilling the requirement?
      System.out.println("ExecW: This file does not exist!");
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
