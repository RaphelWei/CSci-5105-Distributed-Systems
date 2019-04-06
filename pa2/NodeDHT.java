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
    private static int numDHT;
    private static int ID;
    public static WorkWithNodeHandler handler;
    public static WorkWithNode.Processor processor;

    public NodeDHT(int i) {
        // this.connection = s;
        this.ID = i;
    }

    public static void main(String args[]) throws Exception
    {
        System.out.println(" ***************************************************************************************************");
        // Check for hostname argument
        if (args.length != 4)
        {
            System.out.println("NodeDHT MyPort NumNodes SuperNode-HostName SuperNode-Port");
            System.exit(1);
        }

        int maxNumNodes = Integer.parseInt(args[1]);
        m = (int) Math.ceil(Math.log(maxNumNodes) / Math.log(2));
        FingerTable[] finger = new FingerTable[m+1];
        numDHT = (int)Math.pow(2,m);

        System.out.println("The Node starts by connecting at the SuperNode.");
        System.out.println("Establishing connection to the SuperNode...");
        // Assign security manager
        // if (System.getSecurityManager() == null)
        // {
        //     System.setSecurityManager(new RMISecurityManager());
        // }

        InetAddress myIP = InetAddress.getLocalHost();
        System.out.println("My IP: " + myIP.getHostAddress() + "\n");

        // Call registry for PowerService
        // service = (SuperNodeDef) Naming.lookup("rmi://" + args[1] + "/SuperNodeDef");

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
        me = new Node(Integer.parseInt(tokens[0]),myIP.getHostAddress(),args[0]);
        pred = new Node(Integer.parseInt(tokens[1]),tokens[2],tokens[3]);

        System.out.println("My given Node ID is: "+me.getID() + ". Predecessor ID: " +pred.getID());

        handler = new WorkWithNodeHandler(m, finger, numDHT, pred, me);
        processor = new WorkWithNode.Processor(handler);

        // Socket temp = null;
        Runnable runnable = new NodeDHT(0);
        Thread thread = new Thread(runnable);
        thread.start();
////////////////////////////////////////////////////////////////////////////
        client.PostJoin(me.getID());
        transport.close();

        // int count = 1;
        System.out.println("Listening for connection from Client or other Nodes...");
        int port = Integer.parseInt(args[0]);

        // // server setup
        // Runnable simple = new Runnable() {
        //     public void run() {
        //       // System.out.println(args[0]);
        //         simple(processor, Integer.parseInt(myPort));
        //     }
        // };
        // new Thread(simple).start();

        // try {
        //     serverSocket = new ServerSocket( port );
        // } catch (IOException e) {
        //     System.out.println("Could not listen on port " + port);
        //     System.exit(-1);
        // }
        //
        // while (true) {
        //     //System.out.println( "*** Listening socket at:"+ port + " ***" );
        //     Socket newCon = serverSocket.accept();
        Runnable runnable2 = new NodeDHT(1);
        Thread t = new Thread(runnable2);
            t.start();
        // }
        //Start the Client for NodeDHT
    }

    // public static String makeConnection(String ip, String port, String message) throws Exception {
    //     //System.out.println("Making connection to " + ip + " at " +port + " to " + message);
    //     if (me.getIP().equals(ip) && me.getPort().equals(port)){
    //         String response = considerInput(message);
    //         //System.out.println("local result " + message + " answer: "  + response);
    //         return response;
    //     } else {
    //
    //         Socket sendingSocket = new Socket(ip,Integer.parseInt(port));
    //         DataOutputStream out = new DataOutputStream(sendingSocket.getOutputStream());
    //         BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sendingSocket.getInputStream()));
    //
    //         //System.out.println("Sending request: " + message + " to " + ip + " at " + port);
    //         out.writeBytes(message + "\n");
    //
    //         String result = inFromServer.readLine();
    //         //System.out.println("From Server: " + result);
    //         out.close();
    //         inFromServer.close();
    //         sendingSocket.close();
    //         return result;
    //     }
    // }


    public void run() {

        if (this.ID == 0) {
           FingerTable[] finger = handler.getfingerTable();
            System.out.println("Building Finger table ... ");
            for (int i = 1; i <= m; i++) {
                finger[i] = new FingerTable();
                finger[i].setStart((handler.getme().getID() + (int)Math.pow(2,i-1)) % numDHT);
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
                // service.PostJoin(me.getID());
            } catch (Exception e) {}
        }
        else {
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


    // public static String considerInput(String received) throws Exception {
    //     String[] tokens = received.split("/");
    //     String outResponse = "";
    //
    //     if (tokens[0].equals("setPred")) {
    //         Node newNode = new Node(Integer.parseInt(tokens[1]),tokens[2],tokens[3]);
    //         setPredecessor(newNode);
    //         outResponse = "set it successfully";
    //     }
    //     else if (tokens[0].equals("getPred")) {
    //         Node newNode = getPredecessor();
    //         outResponse = newNode.getID() + "/" + newNode.getIP() + "/" + newNode.getPort() ;
    //     }
    //     else if (tokens[0].equals("findSuc")) {
    //         Node newNode = find_successor(Integer.parseInt(tokens[1]));
    //         outResponse = newNode.getID() + "/" + newNode.getIP() + "/" + newNode.getPort() ;
    //     }
    //     else if (tokens[0].equals("getSuc")) {
    //         Node newNode = getSuccessor();
    //         outResponse = newNode.getID() + "/" + newNode.getIP() + "/" + newNode.getPort() ;
    //     }
    //     else if (tokens[0].equals("closetPred")) {
    //         Node newNode = closet_preceding_finger(Integer.parseInt(tokens[1]));
    //         outResponse = newNode.getID() + "/" + newNode.getIP() + "/" + newNode.getPort() ;
    //     }
    //     else if (tokens[0].equals("updateFing")) {
    //         Node newNode = new Node(Integer.parseInt(tokens[1]),tokens[2],tokens[3]);
    //         update_finger_table(newNode,Integer.parseInt(tokens[4]));
    //         outResponse = "update finger " + Integer.parseInt(tokens[4]) + " successfully";
    //     }
    //     else if (tokens[0].equals("print")) {
    //         outResponse = returnAllFingers();
    //     }
    //     else if (tokens[0].equals("tryInsert")){
    //         tryInsert(Integer.parseInt(tokens[1]),tokens[2],tokens[3]);
    //         outResponse = "Inserted pair " + tokens[2] + ":" + tokens[3] + " into DHT";
    //     }
    //     else if (tokens[0].equals("insertKey")) {
    //         insertKey(Integer.parseInt(tokens[1]),tokens[2],tokens[3]);
    //     }
    //     else if (tokens[0].equals("lookupKey")){
    //         outResponse = lookupKey(Integer.parseInt(tokens[1]),tokens[2]);
    //     }
    //     else if (tokens[0].equals("getWord")) {
    //         outResponse = getWord(tokens[1]);
    //     }
    //     //System.out.println("outResponse for " + tokens[0] + ": " + outResponse);
    //     return outResponse;
    // }
    //
    // public static String getWord(String word){
    //     Iterator<Word> iterator = wordList.iterator();
    //     while (iterator.hasNext()) {
    //         Word wordScan = iterator.next();
    //         String wordMatch = wordScan.getWord();
    //         if (word.equals(wordMatch)) {
    //             System.out.println("*** Found at this Node [" + me.getID() + "] the meaning ("
    //                     + wordScan.getMeaning() + ") of word (" + word + ")");
    //             return me.getID() + "/" + wordScan.getMeaning();
    //         }
    //     }
    //     System.out.println("*** Found its Node [" + me.getID() + "] but No Word ("+word+") Found here!");
    //     return "No Word Found!";
    // }
    //
    // public static String lookupKey(int key, String word) throws Exception {
    //     System.out.println("*** Looking Up starting here at Node [" + me.getID() +
    //             "] for word (" + word + ") with key (" + key + ")");
    //     Node destNode = find_successor(key);
    //     String request = "getWord/" +  word ;
    //     String response = "";
    //     response = makeConnection(destNode.getIP(),destNode.getPort(),request);
    //     return response;
    // }
    //
    // public static void tryInsert(int key, String word, String meaning) throws Exception {
    //     System.out.println("*** Starting here at this Node ["+me.getID()+"] to insert word ("+word+
    //             ") with key ("+key+"), routing to destination Node...");
    //     Node destNode = find_successor(key);
    //     String request = "insertKey/" + key + "/" +  word + "/" + meaning;
    //     makeConnection(destNode.getIP(),destNode.getPort(),request);
    // }
    //
    // public static void insertKey(int key, String word, String meaning) throws Exception {
    //     System.out.println("*** Found the dest Node ["+me.getID()+"] here for Insertion of word ("
    //             + word + ") with key ("+key+")");
    //     wordList.add(new Word(key,word,meaning));
    // }
    //
    // public static String returnAllFingers(){
    //     String response = "";
    //     response = response + pred.getID() + "/" + pred.getIP() + ":" + pred.getPort() + "/";
    //     response = response + wordList.size() + "/";
    //     for (int i = 1; i <= m; i++) {
    //         response = response + finger[i].getStart() + "/" + finger[i].getSuccessor().getID() + "/"
    //             + finger[i].getSuccessor().getIP() + ":" + finger[i].getSuccessor().getPort() + "/";
    //     }
    //     return response;
    // }

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

        //printAllFingers();

        Node pred =new Node();
        // String result2 = makeConnection(finger[1].getSuccessor().getIP(),finger[1].getSuccessor().getPort(),request2);

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
        // String result2 = makeConnection(finger[1].getSuccessor().getIP(),finger[1].getSuccessor().getPort(),request2);

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
        int normalInterval = 1;
        for (int i = 1; i <= m-1; i++) {

            myID = handler.getme().getID();
            nextID = finger[i].getSuccessor().getID();

            if (myID >= nextID)
                normalInterval = 0;
            else normalInterval = 1;
            // note finger[] is local
            if ( (normalInterval==1 && (finger[i+1].getStart() >= myID && finger[i+1].getStart() <= nextID))
                    || (normalInterval==0 && (finger[i+1].getStart() >= myID || finger[i+1].getStart() <= nextID))) {

                finger[i+1].setSuccessor(finger[i].getSuccessor());
            } else {
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // String request4 = "findSuc/" + finger[i+1].getStart();
                // String result4 = makeConnection(n.getIP(),n.getPort(),request4);
                // String[] tokens4 = result4.split("/");
                Node result4 = new Node();
                try{
                  TTransport  transport = new TSocket(n.getIP(),Integer.parseInt(n.getPort()));
                  TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
                  WorkWithNode.Client client = new WorkWithNode.Client(protocol);
                  //Try to connect
                  transport.open();
                  ///////////////////////////////////////////////////////////////////////////////
                  result4 = client.find_successor(finger[i+1].getStart());
                  transport.close();

                } catch(TException e) {
                }
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                int fiStart = finger[i+1].getStart();
                int succ = result4.getID();
                int fiSucc = finger[i+1].getSuccessor().getID();
                if (fiStart > succ)
                    succ = succ + numDHT;
                if (fiStart > fiSucc)
                    fiSucc = fiSucc + numDHT;

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
                id = id + numDHT;

            p = handler.find_predecessor(id);



            // String request = "updateFing/" + me.getID() + "/" + me.getIP() + "/" + me.getPort() + "/" + i;
            // makeConnection(p.getIP(),p.getPort(),request);
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

    // public static void update_finger_table(Node s, int i) throws Exception // RemoteException,
    //        {
    //
    //            Node p;
    //            int normalInterval = 1;
    //            int myID = me.getID();
    //            int nextID = finger[i].getSuccessor().getID();
    //            if (myID >= nextID)
    //                normalInterval = 0;
    //            else normalInterval = 1;
    //
    //            //System.out.println("here!" + s.getID() + " between " + myID + " and " + nextID);
    //
    //            if ( ((normalInterval==1 && (s.getID() >= myID && s.getID() < nextID)) ||
    //                        (normalInterval==0 && (s.getID() >= myID || s.getID() < nextID)))
    //                    && (me.getID() != s.getID() ) ) {
    //
    //                //	System.out.println("there!");
    //
    //                finger[i].setSuccessor(s);
    //                p = pred;
    //
    //               //  String request = "updateFing/" + s.getID() + "/" + s.getIP() + "/" + s.getPort() + "/" + i;
    //               //  makeConnection(p.getIP(),p.getPort(),request);
    //               try{
    //                 TTransport  transport = new TSocket(p.getIP(), Integer.parseInt(p.getPort()));
    //                 TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
    //                 WorkWithNode.Client client = new WorkWithNode.Client(protocol);
    //                 //Try to connect
    //                 transport.open();
    //                 client.update_finger_table(s, i);
    //                 transport.close();
    //
    //                 } catch(TException e) {
    //                 }
    //
    //               }
    //            //printAllFingers();
    // }
    //
    // public static void setPredecessor(Node n) // throws RemoteException
    // {
    //     pred = n;
    // }
    //
    // public static Node getPredecessor() //throws RemoteException
    // {
    //     return pred;
    // }





    // public static Node getSuccessor() //throws RemoteException
    // {
    //     return finger[1].getSuccessor();
    // }

    // public static Node closet_preceding_finger(int id) //throws RemoteException
    // {
    //     int normalInterval = 1;
    //     int myID = me.getID();
    //     if (myID >= id) {
    //         normalInterval = 0;
    //     }
    //
    //     for (int i = m; i >= 1; i--) {
    //         int nodeID = finger[i].getSuccessor().getID();
    //         if (normalInterval == 1) {
    //             if (nodeID > myID && nodeID < id)
    //                 return finger[i].getSuccessor();
    //         } else {
    //             if (nodeID > myID || nodeID < id)
    //                 return finger[i].getSuccessor();
    //         }
    //     }
    //     return me;
    // }

}
