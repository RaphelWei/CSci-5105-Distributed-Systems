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
  public static CoordinatorWorkHandler handler;
  public static CoordinatorWork.Processor processor;
  public static void main(String [] args) {

    if(args.size()<4){
      System.out.println("Want 4 arguments!\nString CoordinatorIP, String CoordinatorPort, int NR, int NW");
      System.exit(-1);
    }

    handler = new CoordinatorWorkHandler(args[0],args[1],Integer.toString(args[2]), Integer.toString(args[3]));
    processor = new CoordinatorWork.Processor(handler);

    Runnable PeriodSYNC = new Runnable() {
        public void run() {
            ToSYNC();
        }
    };
    Runnable ProcRequests = new Runnable() {
        public void run() {
          // System.out.println(args[0]);
            ProcessingRequests();
        }
    };
    Runnable CoorSer = new Runnable() {
        public void run() {
          // System.out.println(args[0]);
            simple(processor, handler.getIP());
        }
    };
    new Thread(PeriodSYNC).start();
    new Thread(ProcRequests).start();
    new Thread(CoorSer).start();
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
  public void ToSYNC(){
    while(true){
      try {
        Thread.sleep(2000);
        handler.setSYNC(true);
      } catch (Exception e) {
        System.out.println(e);
      }
    }
  }
  public void ProcessingRequests(){
    while(true){
      try {
        Thread.sleep(500);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if(handler.getSYNC()){
        handler.Sync();
      } else {
        handler.ExecReqs();
      }
    }
  }
}
