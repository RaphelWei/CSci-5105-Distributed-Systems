import java.rmi.*;
import java.rmi.Naming;
import java.rmi.server.*;
import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.util.*;

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
//
// This is the Code for the Node that is part of the DHT.
//
//
public class NodeDHT implements Runnable //extends UnicastRemoteObject implements NodeDHTInterface
{
    private static Node me, pred;
    private static int m;
    private static int DHTRange;
    private static int ID;
    public static WorkWithNodeHandler handler;
    public static WorkWithNode.Processor processor;

    public NodeDHT(int i) {
        this.ID = i;
    }

    public static void main(String args[]) throws Exception
    {
        System.out.println(" ****************************************************");
        // Check for hostname argument
        if (args.length != 4)
        {
            System.out.println("NodeDHT MyPort NumNodes SuperNode-HostName SuperNode-Port");
            System.exit(1);
        }

        int maxNumNodes = Integer.parseInt(args[1]);
        m = (int) Math.ceil(Math.log(maxNumNodes) / Math.log(2));
        FingerTable[] finger = new FingerTable[m+1];
        DHTRange = (int)Math.pow(2,m);

        System.out.println("Connecting to SuperNode");

        InetAddress myIP = InetAddress.getLocalHost();
        System.out.println("My IP: " + myIP.getHostAddress() + "\n");

        // String initInfo = service.Join(myIP.getHostAddress(),args[0]);
        // SN IP may be hard coded
        TTransport  transport = new TSocket(args[2], Integer.parseInt(args[3]));
        // TTransport  transport = new TSocket("localhost", 9001);
        TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        WorkWithSuperNode.Client client = new WorkWithSuperNode.Client(protocol);

        //Try to connect
        transport.open();
        String initInfo = "I cannot get an id from Super Node! God there got to be an issue some where.";
        while(true){
          initInfo = client.Join("localhost",args[0]);
          String[] initInfoList = initInfo.split("/");

          if (initInfoList[0].equals("NACK1")) {
              System.out.println(initInfoList[1]);
              try {
                   Thread.sleep(500);
              } catch (InterruptedException e) {
                   e.printStackTrace();
              }
          } else if(initInfoList[0].equals("NACK0")) {
            System.out.println(initInfoList[1]);
            return;
          } else {
              System.out.println("I got my ID and a node to contact!!!!!   "+initInfo);
              break;
          }
        }

        String[] tokens = initInfo.split("/");
        me = new Node(Integer.parseInt(tokens[0]),myIP.getHostAddress(),args[0],tokens[0]);
        pred = new Node(Integer.parseInt(tokens[1]),tokens[2],tokens[3],tokens[1]);

        System.out.println("My given Node ID is: "+me.getID() + ". Predecessor ID: " +pred.getID());

        handler = new WorkWithNodeHandler(m, finger, DHTRange, pred, me);
        processor = new WorkWithNode.Processor(handler);

        Runnable runnable = new NodeDHT(0);
        Thread thread = new Thread(runnable);
        thread.start();
        client.PostJoin(me.getID());
        transport.close();

        System.out.println("Listening for connection from Client or other Nodes...");
        int port = Integer.parseInt(args[0]);

        Runnable runnable2 = new NodeDHT(1);
        Thread t = new Thread(runnable2);
            t.start();
    }

    public void run() {

        if (this.ID == 0) {
           FingerTable[] finger = handler.getfingerTable();
            System.out.println("Building Finger table ... ");
            for (int i = 1; i <= m; i++) {
                finger[i] = new FingerTable();
                finger[i].setStart((handler.getme().getID() + (int)Math.pow(2,i-1)) % DHTRange);
            }
            for (int i = 1; i < m; i++) {
                finger[i].setIntervalBegin(finger[i].getStart());
                finger[i].setIntervalEnd(finger[i+1].getStart());
            }
            finger[m].setIntervalBegin(finger[m].getStart());
            finger[m].setIntervalEnd(finger[1].getStart()-1);


            if (handler.getPredecessor().getID() == handler.getme().getID()) { //if predcessor is same as my ID -> only node in DHT
                for (int i = 1; i <= m; i++) {
                    finger[i].setSuccessor(handler.getme());
                }
                handler.setfingerTable(finger);
                System.out.println(finger.length);
                System.out.println("=============init finger table============================");
                for(int i=1; i<= m; i++){
                  System.out.println("finger["+i+"] succ id  " + finger[i].getSuccessor().getID());
                  System.out.println("finger["+i+"] start id " + finger[i].getStart());
                }
                System.out.println("=============init finger table============================");
                System.out.println("Done, all finger tablet set as me (only node in DHT)");
            }
            else {
                for (int i = 1; i <= m; i++) {
                    finger[i].setSuccessor(handler.getme());
                }
                handler.setfingerTable(finger);
                try{
                    init_finger_table(handler.getPredecessor());
                    ///////////////////////////////////////////////////////////////
                    System.out.println("Initiated Finger Table!");
                    update_others();
                    System.out.println("Updated all other nodes!");
                } catch (Exception e) {}
            }
            try {
            } catch (Exception e) {}
        }
        else {
            try {

                // server setup
                simple(processor, Integer.parseInt(handler.getme().getPort()));
            } catch (Exception e) {
                System.out.println("Thread cannot serve connection");
            }

        }
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

    public static void init_finger_table(Node n) throws Exception {

        // String request = "findSuc/" + finger[1].getStart();
        FingerTable[] finger = handler.getfingerTable();
        Node result = new Node();
        System.out.println("Asking node " + n.getID() + " at " + n.getIP());
        try{
          TTransport  transport = new TSocket(n.getIP(), Integer.parseInt(n.getPort()));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);
          //Try to connect
          transport.open();
          result = client.find_successor(finger[1].getStart());
          transport.close();

        } catch(TException e) {
        }


        finger[1].setSuccessor(result);

        Node pred =new Node();

        try{
          TTransport  transport = new TSocket(finger[1].getSuccessor().getIP(), Integer.parseInt(finger[1].getSuccessor().getPort()));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);
          //Try to connect
          transport.open();
          pred = client.getPredecessor();
          transport.close();

        } catch(TException e) {
        }
        handler.setPredecessor(pred);
        Node result3 =new Node();

        try{
          TTransport  transport = new TSocket(finger[1].getSuccessor().getIP(), Integer.parseInt(finger[1].getSuccessor().getPort()));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);
          //Try to connect
          transport.open();
          client.setPredecessor(me);
          transport.close();

        } catch(TException e) {
        }


        int myID, nextID;
        int crossingZero = 1;
        for (int i = 1; i <= m-1; i++) {
          // init_finger_table
            myID = handler.getme().getID();
            nextID = finger[i].getSuccessor().getID();

            if (myID >= nextID)
                crossingZero = 0;
            else crossingZero = 1;

            if ( (crossingZero==1 && (finger[i+1].getStart() >= myID && finger[i+1].getStart() <= nextID))
                    || (crossingZero==0 && (finger[i+1].getStart() >= myID || finger[i+1].getStart() <= nextID))) {

                finger[i+1].setSuccessor(finger[i].getSuccessor());
            } else {
                Node result4 = new Node();
                try{
                  TTransport  transport = new TSocket(n.getIP(),Integer.parseInt(n.getPort()));
                  TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
                  WorkWithNode.Client client = new WorkWithNode.Client(protocol);
                  //Try to connect
                  transport.open();

                  result4 = client.find_successor(finger[i+1].getStart());
                  transport.close();

                } catch(TException e) {
                }
                int fiStart = finger[i+1].getStart();
                int succ = result4.getID();
                int fiSucc = finger[i+1].getSuccessor().getID();
                if (fiStart > succ)
                    succ = succ + DHTRange;
                if (fiStart > fiSucc)
                    fiSucc = fiSucc + DHTRange;

                if ( fiStart <= succ && succ <= fiSucc ) {
                    finger[i+1].setSuccessor(result4);
                }
            }
        }
        System.out.println("=============init finger table============================");
        for(int i=1; i<= m; i++){
          System.out.println("finger["+i+"] succ id  " + finger[i].getSuccessor().getID());
          System.out.println("finger["+i+"] start id " + finger[i].getStart());
        }
        System.out.println("=============init finger table============================");
        System.out.println();
        handler.setfingerTable(finger);
    }

    public static void update_others() throws Exception{
        Node p;
        for (int i = 1; i <= m; i++) {
            int id = handler.getme().getID() - (int)Math.pow(2,i-1) + 1;
            if (id < 0)
                id = id + DHTRange;

            p = handler.find_predecessor(id);

            Node me = handler.getme();
            try{
              TTransport  transport = new TSocket(p.getIP(), Integer.parseInt(p.getPort()));
              TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
              WorkWithNode.Client client = new WorkWithNode.Client(protocol);
              //Try to connect
              transport.open();
              client.update_finger_table(me, i);
              transport.close();

            } catch(TException e) {
            }

        }
    }

}
