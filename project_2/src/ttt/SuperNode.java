import java.math.*;
import java.rmi.*;
import java.rmi.server.*;
import java.security.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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
//
// SuperNode
//
// A RMI service that return the Client requests for Lookup() and Insert()
//
public class SuperNode
{
  public static WorkWithSuperNodeHandler handler;
  public static WorkWithSuperNode.Processor processor;


    public static void main ( String args[] ) throws Exception
    {
        if (args.length != 2)
        {
            System.out.println
                ("Syntax - SuperNode [numNodes] [myport]");
            System.exit(1);
        }


        // m = (int) Math.ceil(Math.log(maxNumNodes) / Math.log(2));
        // numDHT = (int)Math.pow(2,m);
        //
        // nodeList = new Node[numDHT];
        // Assign a security manager, in the event that dynamic
        // classes are loaded
        // if (System.getSecurityManager() == null)
        //     System.setSecurityManager ( new RMISecurityManager() );
        //
        // busy = 0;

        // Create an instance of our power service server ...
        try {
            // //System.out.println( "*** A Client came; Service it *** " + this.ID );
            //
            // BufferedReader inFromClient =
            //     new BufferedReader(new InputStreamReader(connection.getInputStream()));
            // DataOutputStream outToClient = new DataOutputStream(connection.getOutputStream());
            // String received = inFromClient.readLine();
            // //System.out.println("Received: " + received);
            // String response = considerInput(received);
            // //System.out.println("Sending back to client: "+ response);
            //
            // outToClient.writeBytes(response + "\n");
            int maxNumNodes = Integer.parseInt(args[0]);
            handler = new WorkWithSuperNodeHandler(maxNumNodes);
            processor = new WorkWithSuperNode.Processor(handler);
            // server setup
            Runnable simple = new Runnable() {
                public void run() {
                  // System.out.println(args[0]);
                    simple(processor, Integer.parseInt(args[1]));
                }
            };
              new Thread(simple).start();
            } catch (Exception e) {
                System.out.println("Thread cannot serve connection");
            }




    }
    public static void simple(WorkWithSuperNode.Processor processor, int port) {
      System.out.println(port);
      try {
          // TServerTransport serverTransport = new TServerSocket(port);
          // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
          // server.serve();


          //Create Thrift server socket
          TServerTransport serverTransport = new TServerSocket(port);
          TTransportFactory factory = new TFramedTransport.Factory();

          // //Create service request handler
          // MultiplyHandler handler = new WorkWithNodeHandler();
          // processor = new WorkWithNode.Processor(handler);

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
