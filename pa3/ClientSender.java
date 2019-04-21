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


public class ClientSender {
  private static String contactServerIP;
  private static String contactServerPort;
  private static String myPort;
  private static String receiverPort;
  private static String myIP;
  private static int numOfOps;
  private static long startTime;

  // Format of the input args: [contactServerIP] [contactServerPort] [myPort] [receiverPort]
  // [contactServerIP] - The IP address of the node the client connect to
  // [contactServerPort] - The port number of the node
  // [myPort] - The port number of the ClientSender
  // [receiverPort] - The port number of the ClientReceiver
  public static void main(String [] args) {

    if(args.length<4){
      System.out.println("Want 4 arguments!: contactServerIP contactServerPort myPort receiverPort");
      System.exit(-1);
    }

    contactServerIP = args[0];
    contactServerPort = args[1];
    myPort = args[2];
    receiverPort = args[3];

    for (;;) {
      numOfOps = 0;
      startTime = 0;
      try{
        myIP = InetAddress.getLocalHost().getHostAddress();
      } catch (Exception e) {
        e.printStackTrace();
      }


      System.out.print("\n\n\n");
      System.out.println("******************************** System starts ************************************************");
      System.out.println("Please select a mode that you want to test: ");
      System.out.println("0 - Read-only, only Reads.");
      System.out.println("1 - Write-only, only Writes.");
      System.out.println("2 - Read-heavy, where 90% are Reads and 10% are Writes.");
      System.out.println("3 - Write-heavy, where 10% are Reads and 90% are Writes.");
      System.out.print("\n\n\n");

      Scanner scan = new Scanner(System.in);
      String mode = scan.nextLine();

      switch(mode) {
        case "0":
          startTime = System.currentTimeMillis();
          numOfOps = handleRequest("./read-only.txt");
          System.out.println("numOfOps: " + numOfOps);
          connectReceiver(myIP, receiverPort, numOfOps, startTime);
          break;
        case "1":
          startTime = System.currentTimeMillis();
          numOfOps = handleRequest("./write-only.txt");
          System.out.println("numOfOps: " + numOfOps);
          connectReceiver(myIP, receiverPort, numOfOps, startTime);
          break;
        case "2":
          startTime = System.currentTimeMillis();
          numOfOps = handleRequest("./read-heavy.txt");
          System.out.println("numOfOps: " + numOfOps);
          connectReceiver(myIP, receiverPort, numOfOps, startTime);
          break;
        case "3":
          startTime = System.currentTimeMillis();
          numOfOps = handleRequest("./write-heavy.txt");
          System.out.println("numOfOps: " + numOfOps);
          connectReceiver(myIP, receiverPort, numOfOps, startTime);
          break;
        default:
          System.out.println("******************************** System ends ************************************************");
          System.exit(0);
      }
    }
  }


  // read a local script to begin read/write - heavy mode
  // then connect to a given server
  public static int handleRequest(String path) {
    File file = new File(path);
    BufferedReader reader = null;
    int count = 0;
    try {
      reader = new BufferedReader(new InputStreamReader(
        new FileInputStream(file), "utf-8"));
      String line;
      while ((line = reader.readLine()) != null) {
        count++;
        // str: [op] / [filename] / [content]
        String[] str = line.split("/");
        if(str[0].equals("W")){
          connectServer(str[0], str[1], str[2], myIP, receiverPort);
        } else if (str[0].equals("R")) {
          connectServer(str[0], str[1], null, myIP, receiverPort);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return count;
  }


  // First, this method would form a request,
  // then it will connect to a given server, this server would later
  // forward the request to the coordinator
  public static void connectServer(String op, String fileName, String content, String clientIP, String clientPort) {
    REQ request = new REQ(op, fileName, content, clientIP, clientPort);
    try {
      TTransport transport = new TSocket(contactServerIP, Integer.parseInt(contactServerPort));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      ServerWork.Client client = new ServerWork.Client(protocol);
          // Try to connect
      transport.open();
      client.ForRequest(request);
      transport.close();
    } catch (TException e) {
      e.printStackTrace();
    }
  }

  // This method would connect to the receiver, to help set the values of numOfOps and
  // startTime in receiver. Then when the receiver has received
  public static void connectReceiver(String myIP, String receiverPort, int numOfOps, long startTime) {
    try {
      TTransport transport = new TSocket(myIP, Integer.parseInt(receiverPort));
      TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
      ClientWork.Client client = new ClientWork.Client(protocol);
      // Try to connect
      transport.open();
      client.setParams(numOfOps, startTime);
      transport.close();
    } catch (TException e) {
      e.printStackTrace();
    }
  }
}
