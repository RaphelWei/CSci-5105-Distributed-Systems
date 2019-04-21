import java.io.*;

import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.*;
import javax.xml.bind.DatatypeConverter;

import java.net.*;
import java.net.UnknownHostException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;


import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;


public class Coordinator {
  public static ServerWorkHandler SeverHandler;
  public static ServerWork.Processor SeverProcessor;

  public static CoordinatorWorkHandler CoordinatorHandler;
  public static CoordinatorWork.Processor CoordinatorProcessor;

  public static void main(String [] args) {

    if(args.length<5){
      System.out.println("Want 5 arguments!: CoordinatorPort NR NW N ServerPort");
      System.exit(-1);
    }
    int NR = Integer.parseInt(args[1]);
    int NW = Integer.parseInt(args[2]);
    int N = Integer.parseInt(args[3]);

    if(NR + NW <= N || NW <= N/2 ){
      System.out.println("the value of NR, NW, N do not meet requirement");
      System.exit(-1);
    }
    String myIP = "";
    try{
      myIP = InetAddress.getLocalHost().getHostAddress();
      System.out.println("I am at IP:   "+myIP);
      System.out.println("I am at CoordinatorPort: "+args[0]);
      System.out.println("I am at ServerPort: "+args[4]);
    } catch(Exception e) {
      e.printStackTrace();
    }


    // coordinator init
    CoordinatorHandler = new CoordinatorWorkHandler(myIP,args[0],Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    CoordinatorProcessor = new CoordinatorWork.Processor(CoordinatorHandler);

    Runnable PeriodSYNC = new Runnable() {
        public void run() {
            ToSYNC();
        }
    };
    Runnable ProcessRequests = new Runnable() {
        public void run() {
            ProcessingRequests();
        }
    };
    Runnable ThreadingCoordinator = new Runnable() {
        public void run() {
            StartCoordinator(CoordinatorProcessor, Integer.parseInt(CoordinatorHandler.getCoordinatorPort()));
        }
    };
    new Thread(PeriodSYNC).start();
    new Thread(ProcessRequests).start();
    new Thread(ThreadingCoordinator).start();




    // server init

    SeverHandler = new ServerWorkHandler(myIP, args[4], myIP, args[0]);
    SeverProcessor = new ServerWork.Processor(SeverHandler);

    Runnable ThreadingServer = new Runnable() {
        public void run() {
            StartServer(SeverProcessor, Integer.parseInt(SeverHandler.getPort()));
        }
    };
    new Thread(ThreadingServer).start();

    // ToJoin();
    CoordinatorHandler.join(new Node(SeverHandler.getIP(), SeverHandler.getPort()));



  }

  public static void StartCoordinator(CoordinatorWork.Processor processor, int port) {
    System.out.println(port);
      try {
          //Create Thrift server socket
          TServerTransport serverTransport = new TServerSocket(port);
          TTransportFactory factory = new TFramedTransport.Factory();

          //Set server arguments
          TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
          args.processor(processor);  //Set handler
          args.transportFactory(factory);  //Set FramedTransport (for performance)

          //Run server as a single thread
          TServer server = new TThreadPoolServer(args);
          server.serve();

      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  public static void ToSYNC(){
    while(true){
      try {
        Thread.sleep(2000);
        CoordinatorHandler.setSYNC(true);
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  }

  public static void ProcessingRequests(){
    while(true){
      try {
        Thread.sleep(500);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if(CoordinatorHandler.getSYNC()){
        CoordinatorHandler.SYNC();
      } else {
        CoordinatorHandler.ExecReqs();
      }
    }
  }

  public static void StartServer(ServerWork.Processor processor, int port) {
    System.out.println(port);
    try {
        //Create Thrift server socket
        TServerTransport serverTransport = new TServerSocket(port);
        TTransportFactory factory = new TFramedTransport.Factory();

        //Set server arguments
        TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
        args.processor(processor);  //Set handler
        args.transportFactory(factory);  //Set FramedTransport (for performance)

        //Run server as a single thread
        TServer server = new TThreadPoolServer(args);
        server.serve();

    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  // public static void ToJoin(){
  //   try{
  //     TTransport  transport = new TSocket(handler.getCoordinatorIP(), Integer.parseInt(handler.getCoordinatorPort()));
  //     TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
  //     ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
  //     //Try to connect
  //     transport.open();
  //     int NextVerNum = client.join(new Node(handler.getIP(), handler.getPort()));
  //     transport.close();
  //
  //   } catch(Exception e){
  //     e.printStackTrace();
  //   }
  // }
}
