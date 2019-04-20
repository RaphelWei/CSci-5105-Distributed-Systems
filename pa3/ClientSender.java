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
  private String ContactServerIP;
  private String ContactServerPort;
  private String ReceiverIP;
  private String ReceiverPort;

  public static void main(String [] args) {

    if(args.size()<4){
      System.out.println("Want 4 arguments!: ContactServerIP ContactServerPort ReceiverIP ReceiverPort");
      System.exit(-1);
    }

    ContactServerIP = args[0];
    ContactServerPort = args[1];
    ReceiverIP = args[2];
    ReceiverPort = args[3];

    Scanner myObj = new Scanner(System.in);  // Create a Scanner object
    while(true){
      System.out.println("please type the pathname of your request script");
      String scriptPathname = myObj.nextLine();
      handleRequest(scriptPathname);
    }


  }
  public static void handleRequest(String path) {
    File file = new File(path);
    BufferedReader reader = null;
    try {
        reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "utf-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            // str: [op] / [filename] / [content]
            String[] str = line.split("/");
            if(str[0].equals("W")){
              connectServer(str[0], str[1], str[2], ReceiverIP, ReceiverPort);
            } else if (str[0].equals("R")) {
              connectServer(str[0], str[1], null, ReceiverIP, ReceiverPort);
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
  }

  public static void connectServer(String op, String fileName, String content, String clientIP, String clientPort) {
      String request = "";
      REQ request = new REQ(op, fileName, content, clientIP, clientPort);
      try {
          TTransoprt transport = new TSocket(ContactServerIP, Integer.parseInt(ContactServerPort));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
          // Try to connect
          transport.open();
          client.request(request);
          transport.close();
      } catch (TException e) {
          e.printStackTrace();
      }
  }
}
