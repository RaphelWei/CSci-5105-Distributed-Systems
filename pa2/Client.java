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
  private static int numDHT;

    public static void readGivenFile(String path, String ip, String port) throws IOException{
        // HashMap<String, String> map = new HashMap<>();
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String thisline = null;
            while ((thisline = br.readLine()) != null) {
                String[] str = thisline.split(":");// assume title:genre
                insertRecord(ip, port, str[0], str[1]);
                // map.put(str[0], str[1]);maxNumNodes_
            }

        } catch (IOException e) {
            System.err.println("File does not exist. Please input the correct file path.");
            e.printStackTrace();
        }
    }

    public static void lookupBook(String ip, String port, String title) throws NoSuchAlgorithmException {
        int key = getHashedKey(title);
        String ret = "";
        try{
              TTransport  transport = new TSocket(ip, Integer.parseInt(port));
              TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
              WorkWithNode.Client client = new WorkWithNode.Client(protocol);
              //Try to connect
              transport.open();
              ret = client.lookupBook_ByKey(key, title); // NACK or genre
              transport.close();

            } catch(TException e) {
                e.printStackTrace();
            }
        if(ret.equals("NACK")){
          System.out.println("Sorry the book queried is not in the DHT");
        } else {
          System.out.println("The result of your search is: "+ ret);
        }
}

    public static void insertRecord(String ip, String port, String title, String genre) {

        int key = getHashedKey(title);
        String ret = "";
        try{
              TTransport  transport = new TSocket(ip, Integer.parseInt(port));
              TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
              WorkWithNode.Client client = new WorkWithNode.Client(protocol);
              //Try to connect
              transport.open();
              ret = client.insertRecord_ByKey(key, title, genre);
              transport.close();

        } catch(TException e) {
            e.printStackTrace();
        }
        if(ret.equals("NACK")){
          System.out.println("insert not success");
        } else {
          System.out.println("insert success");
        }


    }


    public static int getHashedKey(String key) {
        int hashedKey = 0;
        try {

          MessageDigest md = MessageDigest.getInstance("SHA1");
          md.reset();
          md.update(key.getBytes());
          byte[] digest = md.digest();
          BigInteger hashNum = new BigInteger(digest);

          hashedKey = Math.abs(hashNum.intValue()) % numDHT;



            // MessageDigest md = MessageDigest.getInstance("SHA1");
            // md.update(key.getBytes());
            // byte[] digest = md.digest();
            // BigInteger bi = new BigInteger(digest);
            // BigInteger m = new BigInteger("128");
            // bi = bi.mod(m);
            // hashedKey = Integer.parseInt(bi.toString(10));
            // System.out.println(hashedKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedKey;
    }

    public static void printAllNodeInfo() {

    }



    // args format [SNIP] [SNPort]
    public static void main (String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length !=3) {
            System.out.println("Please check your input format: [SuperNode IP] [SuperNode Port] [MaxNumNodes]");
            System.exit(1);
        }
        maxNumNodes = Integer.parseInt(args[2]);
        m = (int) Math.ceil(Math.log(maxNumNodes) / Math.log(2));
        numDHT = (int)Math.pow(2,m);

        for (;;) {
            System.out.print("\n\n\n");
            System.out.println("******************************** System starts ************************************************");
            System.out.println("Please specify the operation that you want this system to execute: ");
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
            String operation = scan.nextLine();

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

            if (operation.equals("Set")) {
                System.out.println("Please input the path of the given file: ");
                String path = scan.nextLine();

                readGivenFile(path, p.getIP(), p.getPort());
            }
            else if (operation.equals("Get")) {
                System.out.println("Please input the book title you want to look up: ");
                String title = scan.nextLine();
                lookupBook(p.getIP(), p.getPort(), title);
                System.out.println("\n");
            }
            else if (operation.equals("Insert")) {
                System.out.println("Please input the book title and genre pair: ");
                String pair = scan.nextLine();
                String[] str = pair.split(":");
                if (str.length != 2) {
                	System.out.println("Improper format. Please check your input pair.");
                } else {
                    insertRecord(p.getIP(), p.getPort(), str[0], str[1]);
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
