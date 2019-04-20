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
  // ConcurrentLinkedQueue<REQ> reqs = new ConcurrentLinkedQueue<REQ>();
  // <Filename,OP>
  // note: for R, the number of char R means how many R op working.
  // ConcurrentHashMap<String, String> FileOP = new ConcurrentHashMap<String, String>();

  ConcurrentHashMap<String, ArrayList<REQ>> reqs = new ConcurrentHashMap<String, ArrayList<REQ>>();
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
    if(reqs.get(r.getFilename())==null){
      reqs.put(r.getFilename(), new ArrayList<REQ>(r));
    } else {
      reqs.put(r.getFilename(), reqs.get(r.getFilename()).add(r));
    }
  }

  @Override
  public synchronized void join(Node S){
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
  }

  public void SYNC() {
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
          System.out.println("SYNC: going to rewrite the missing file with version 0");
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

  public static ConcurrentHashMap<String, , ArrayList<REQ>> copy(
    ConcurrentHashMap<String, ArrayList<REQ>> original)
{
    ConcurrentHashMap<String, ArrayList<REQ>> copy = new ConcurrentHashMap<String, ArrayList<REQ>>();
    for (Map.Entry<String, ArrayList<REQ>> entry : original.entrySet())
    {
        copy.put(entry.getKey(),
           // Or whatever List implementation you'd like here.
           new ArrayList<REQ>(entry.getValue()));
    }
    return copy;
}


  // !!!!!!!!!!!!!!!!!!!!!!! now the ConcurrentHashMap is only to sync between add request and (deep)copy requests
  public void ExecReqs(){
    ConcurrentHashMap<String, ArrayList<REQ>> reqsUpToNow = copy(reqs);
    reqs = new ConcurrentHashMap<String, ArrayList<REQ>>();
    ArrayList<Thread> threads = new ArrayList<Thread>;
    Iterator it = reqsUpToNow.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        Runnable Reading = new Runnable() {
            public void run() {
                ThreadingforEachFile(pair.getValue());
            }
        };
        Thread OPsOnEachFile = new Thread(Reading);
        OPsOnEachFile.start();
        threads.add(OPsOnEachFile);
    }
    for(int i = 0; i < threads.size(); i++){
      threads.get(i).join();
    }
  }

  public void ThreadingforEachFile(ArrayList<REQ> reqsOfAFile){
    ArrayList<Thread> threads = new ArrayList<Thread>; // for join and count number of R
    String PreviousOP = "";
    for (int j = 0; j < reqs.size(); j++) {
      REQ r = reqsOfAFile.get(j);
      if(PreviousOP.euqls("")){// there is no op before
        if(r.getOP().equals("R")){ // the req want to Read
          Runnable Reading = new Runnable() {
              public void run() {
                  ExecR(r);
              }
          };
          Thread ROP = new Thread(Reading);
          ROP.start();
          threads.add(ROP);
          PreviousOP = "R";

        } else if(r.getOP().equals("W")){ // the req want to Write

          Runnable Writing = new Runnable() {
              public void run() {
                  ExecW(r);
              }
          };
          Thread WOP = new Thread(Writing);
          WOP.start();
          threads.add(WOP);
          PreviousOP = "W";

          // adding record for SYNC
          FilestoSYNC.add(r);
        }
      } else if (PreviousOP.euqls("W")){// there is a W op before
        for(int i = 0; i < threads.size(); i++){
          threads.get(i).join();
        }
        threads = new ArrayList<Thread>;

      } else if (PreviousOP.euqls("R")){// there is a R op running on this file
        if(r.getOP().equals("R")){ // R after R; go for it
          Runnable Reading = new Runnable() {
              public void run() {
                  ExecR(r);
              }
          };
          Thread ROP = new Thread(Reading);
          ROP.start();
          threads.add(ROP);
          PreviousOP = "R";

        } else if(r.getOP().equals("W")){// W after R; wait for all R

          // join all R before
          for(int i = 0; i < threads.size(); i++){
            threads.get(i).join();
          }
          threads = new ArrayList<Thread>;

          // do the W
          Runnable Writing = new Runnable() {
              public void run() {
                  ExecW(r);
              }
          };
          Thread WOP = new Thread(Writing);
          WOP.start();
          threads.add(WOP);
          PreviousOP = "W";

          // adding record for SYNC
          FilestoSYNC.add(r);
        }
      }
    }

    // take care the last several R or single W
    for(int i = 0; i < threads.size(); i++){
      threads.get(i).join();
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
    NewestVerNum = client.getVersion(r.getFilename());
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
