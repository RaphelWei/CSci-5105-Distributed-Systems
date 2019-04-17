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


public class CoordinatorHandler implements Coordinator.Iface
{
  // sth to init!!!!!!!!!!!!!!!!!!!!!
  String CoordinatorIP;
  String CoordinatorPort;
  // Server[] ServerList;
  ArrayList<Server> ServerList = new ArrayList<Server>();
  int NR;
  int NW;
  ArrayList<REQ> reqs = new ArrayList<REQ>();
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

  // todo: save request
  public void forwardReq(String filename, String clientInfo, String request){

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

  public Server find_newest(Server[] ServerCheckList, String filename){
    Server tarSer = ServerCheckList[0];
    int tarVer = 0;
    for (int i=0;i<ServerCheckList.size();i++){
      TTransport  transport = new TSocket(ServerCheckList[i].IP, Integer.parseInt(ServerCheckList[i].Port));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      ServerHandler.Client client = new ServerHandler.Client(protocol);
      //Try to connect
      transport.open();
      int NextVerNum = client.getVersion(filename);
      transport.close();
      if(tarVer<NextVerNum){
        tarVer = NextVerNum;
        tarSer = ServerCheckList[i];
      }
    }
    return tarSer;
  }

  public
}
