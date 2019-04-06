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


public class Client {
  private static int maxNumNodes;
  private static int m;
  private static int DHTRange;

    public static void readGivenFile(String path, String ip, String port, int showpath) throws IOException{
        // HashMap<String, String> map = new HashMap<>();
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String thisline = null;
            while ((thisline = br.readLine()) != null) {
                String[] str = thisline.split(":");// assume title:genre
                insertRecord(ip, port, str[0], str[1], showpath);
                // map.put(str[0], str[1]);maxNumNodes_
            }

        } catch (IOException e) {
            System.err.println("File does not exist. Please input the correct file path.");
            // e.printStackTrace();
        }
    }

    public static void lookupBook(String ip, String port, String title, int showpath) throws NoSuchAlgorithmException {
        int key = getHashedKey(title);
        String ret = "";
        try{
              TTransport  transport = new TSocket(ip, Integer.parseInt(port));
              TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
              WorkWithNode.Client client = new WorkWithNode.Client(protocol);
              //Try to connect
              transport.open();
              ret = client.lookupBook_ByKey(key, title, showpath); // NACK or genre
              transport.close();

            } catch(TException e) {
                e.printStackTrace();
            }
        String[] retlist = ret.split("/");
        if(retlist[0].equals("NACK")){
          System.out.println("Sorry the book queried is not in the DHT");
        } else {
          if(showpath == 1) {
            System.out.println("The result of your search is: "+ retlist[0]);
            System.out.println("path: "+ retlist[1]);
          }
          else{
            System.out.println("The result of your search is: "+ retlist[0]);
          }
        }
}

    public static void insertRecord(String ip, String port, String title, String genre, int showpath) {

        int key = getHashedKey(title);
        String ret = "";
        try{
              TTransport  transport = new TSocket(ip, Integer.parseInt(port));
              TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
              WorkWithNode.Client client = new WorkWithNode.Client(protocol);
              //Try to connect
              transport.open();
              ret = client.insertRecord_ByKey(key, title, genre, showpath);
              transport.close();

        } catch(TException e) {
            e.printStackTrace();
        }
        String[] retlist = ret.split("/");
        if(retlist[0].equals("NACK")){
          System.out.println("insert not success");
        } else {
          if(showpath==1) {
            String message = "insert "+title+" success, this insertion has go through the path: \n"+retlist[1];
            System.out.println(message);
            System.out.println("the key to insert: "+key);
          } else {
            String message = "insert "+title+" success";
            System.out.println(message);
          }
        }


    }


    public static int getHashedKey(String key) {
        int hashedKey = 0;
        try {

          MessageDigest md = MessageDigest.getInstance("SHA1");
          md.update(key.getBytes());
          byte[] digest = md.digest();
          BigInteger hashNum = new BigInteger(digest);

          hashedKey = Math.abs(hashNum.intValue()) % DHTRange;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedKey;
    }



    // args format [SNIP] [SNPort]
    public static void main (String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length !=3) {
            System.out.println("Please check your input format: [SuperNode IP] [SuperNode Port] [MaxNumNodes]");
            System.exit(1);
        }
        maxNumNodes = Integer.parseInt(args[2]);
        m = (int) Math.ceil(Math.log(maxNumNodes) / Math.log(2));
        DHTRange = (int)Math.pow(2,m);

        for (;;) {
            System.out.print("\n\n\n");
            System.out.println("******************************** System starts ************************************************");
            System.out.println("Please specify the operation that you want this system to execute: ");
            System.out.println("Please specify (0 or 1) for Set and Get, if you would like to print path. e,g 'Get/1' will print the path, 'Get/0' won't");
            System.out.println("Set - Setting a book title and its genre from a given file.");
            System.out.println("Get - Looking up a book title for its genre.");
            System.out.println("Insert - Inserting a new record to the system or updating the genre for a existing book.");
            System.out.println("Print - Printing the information of all nodes.");
            System.out.println("Exit - Exiting from the system.");
            System.out.print("\n\n\n");


//        if (args.length != 1) {
//            System.out.println("Please use the operation listed above!");
//            System.exit(1);
//        }

            Scanner scan = new Scanner(System.in);
            String op = scan.nextLine();
            String[] oplist = op.split("/");
            String operation = oplist[0];
            int showpath = 0;
            if(oplist.length>1){
              try{
                showpath = Integer.parseInt(oplist[1]);
              }catch(Exception e){
                showpath = 0;
              }
            }
            // get the random init contact point from supernode
            Node p = new Node();
            try{
                TTransport  transport = new TSocket(args[0], Integer.parseInt(args[1]));
                TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
                WorkWithSuperNode.Client client = new WorkWithSuperNode.Client(protocol);
                //Try to connect
                transport.open();
                p = client.getNode();
                transport.close();

                } catch(TException e) {
                    e.printStackTrace();
                }
            System.out.println("the contact point id: "+p.getID());

            if (operation.equals("Set")) {
                System.out.println("Please input the path of the given file: ");
                String path = scan.nextLine();

                readGivenFile(path, p.getIP(), p.getPort(), showpath);
            }
            else if (operation.equals("Get")) {
                System.out.println("Please input the book title you want to look up: ");
                String title = scan.nextLine();
                lookupBook(p.getIP(), p.getPort(), title, showpath);
                System.out.println("\n");
            }
            else if (operation.equals("Insert")) {
                System.out.println("Please input the book title and genre pair: ");
                String pair = scan.nextLine();
                String[] str = pair.split(":");
                if (str.length != 2) {
                	System.out.println("Improper format. Please check your input pair.");
                } else {
                    insertRecord(p.getIP(), p.getPort(), str[0], str[1], showpath);
                }
            }
            else if (operation.equals("Print")) {
                printStructure(p);
            }
            else if (operation.equals("Exit")) {
                System.out.println("******************************** System ends ************************************************");
                System.exit(0);
            }
            else {
                System.out.println("Invalid command. Please use the operations listed above.");
            }
        }
    }

    public static void printStructure(Node p){
      Node init = p;
      do{
        try{
          TTransport  transport = new TSocket(p.getIP(), Integer.parseInt(p.getPort()));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);
          //Try to connect
          transport.open();
          String pInFo = client.AllInFoString();
          System.out.println(pInFo);
          p = client.getPredecessor();
          transport.close();

        } catch(TException e) {
            e.printStackTrace();
        }
      }while(p.getID()!=(init.getID()));
    }

}
