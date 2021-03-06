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


public class ClientReceiver {
  public static ClientWorkHandler handler;
  public static ClientWork.Processor processor;

  public static void main(String [] args) {

    if(args.length<1){
      System.out.println("Want 1 arguments!\nMyPort");
      System.exit(-1);
    }

    String myIP="";
    try{
      myIP = InetAddress.getLocalHost().getHostAddress();
    } catch(Exception e) {
      e.printStackTrace();
    }
    System.out.println("I am at IP:   "+myIP);
    System.out.println("I am at Port: "+args[0]);

    handler = new ClientWorkHandler(myIP, args[0]);
    processor = new ClientWork.Processor(handler);


    Runnable ThreadingServer = new Runnable() {
        public void run() {
            StartServer(processor, Integer.parseInt(handler.getPort()));
        }
    };
    new Thread(ThreadingServer).start();


  }

  public static void StartServer(ClientWork.Processor processor, int port) {
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
}
