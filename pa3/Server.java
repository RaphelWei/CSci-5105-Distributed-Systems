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


public class Node {
  public static ServerWorkHandler handler;
  public static ServerWork.Processor processor;
  public static void main(String [] args) {

    if(args.size()<4){
      System.out.println("Want 4 arguments!\nString CoordinatorIP, String CoordinatorPort, int NR, int NW");
      System.exit(-1);
    }

    handler = new ServerWorkHandler(args[0], args[1],args[2], args[3]);
    processor = new ServerWork.Processor(handler);



    Runnable ThreadingServer = new Runnable() {
        public void run() {
            simple(processor, Integer.parseInt(handler.getIP()));
        }
    };
    new Thread(ThreadingServer).start();
  }

  public static void simple(WorkWithNode.Processor processor, int port) {
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
  public static void ToJoin(){
    try{
      TTransport  transport = new TSocket(handler.getCoordinatorIP(), Integer.parseInt(handler.getCoordinatorPort()));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      ServerHandler.Client client = new ServerHandler.Client(protocol);
      //Try to connect
      transport.open();
      int NextVerNum = client.join(new Server(handler.getIP(), handler.getPort()));
      transport.close();

    } catch(Exception e){
      e.printStackTrace();
    }
  }

}
