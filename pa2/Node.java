import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

// import java.io.*;
// import java.util.*;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import java.io.File;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.concurrent.*;

public class Node {
    // public variable?
    private int NodeID = 0;
    // private String[] KeyRange = {"0", "0"};
    public static void main(String [] args) {

        try {
            TTransport  transport = new TSocket("localhost", 9090);
            TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
            WorkWithSuperNode.Client client = new WorkWithSuperNode.Client(protocol);

            //Try to connect
            transport.open();

            // as a client
            while(true){
  			      String ret = client.Join();
              String[] retparts = ret.split("|");
              if(retparts[0].equals("NACK0")){
                System.out.println(retparts[1]);
                System.out.println("wait 500 ms and retry");
                try {
                     Thread.sleep(500);
                } catch (InterruptedException e) {
                     e.printStackTrace();
                }
              } else if(retparts[0].equals("NACK1")){
                System.out.println(retparts[1]);
                System.out.println("terminating");
                return;
              } else if(retparts[0].equals("done")){
                break;
              } else if(retparts[0].equals("ACK")){
                String[] ContactInfo = retparts[1].split(":");
                NodeID = Integer.parseInt(ContactInfo[0]);
                ContactOtherNode(ContactInfo);
                break;
              }
            }

            // as a server

        } catch(TException e) {

        }

    }

    public Void ContactOtherNode(String [] ContactInfo){
      String hostname = ContactInfo[1];
      String port = ContactInfo[2];
      String NodeID = ContactInfo[3];


      try {
          TTransport  transport = new TSocket(ContactInfo[1], Integer.parseInt(ContactInfo[2]));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);

          //Try to connect
          transport.open();

          // as client reach to other nodes to get information back.




      } catch(TException e) {

      }
    }
}
