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
    public WorkWithNodeHandler handler;
    public WorkWithNode.Processor processor;
    // public variable?
    // private String NodeID = 0;
    // private String[] KeyRange = {"0", "0"};
    public static void main(String [] args) {

        try {
            String myIP = args[0];
            String myPort = args[1];
            TTransport  transport = new TSocket(myIP, Integer.parseInt(myPort));
            TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
            WorkWithSuperNode.Client client = new WorkWithSuperNode.Client(protocol);

            //Try to connect
            transport.open();

            // as a client
            while(true){
  			      String ret = client.Join(myIP, myPort);
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
                // String[] ContactInfo = retparts[1].split(":");
                handler.setNodeID(Integer.parseInt(retparts[1]));
                handler.setmyInfo(myIP+":"+myPort+":"+retparts[1]);
                handler.setpredecessorInfo(myIP+":"+myPort+":"+retparts[1]);
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // TODO:set fingertable
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                break;
              } else if(retparts[0].equals("ACK")){
                //myNodeMD5:tarAddr:tarPort:tarNodeMD5
                String[] ContactInfo = retparts[1].split(":");
                handler.setNodeID(Integer.parseInt(ContactInfo[0]));
                handler.setmyInfo(myIP+":"+myPort+":"+ContactInfo[0]);

                // find the correct node to contact (get the contact info of the node with ID right after the one I got.)
                //myNodeMD5:tarAddr:tarPort:tarNodeMD5
                String CorrectedContactInfo = ContactInfo[0]+":"+CorrectContactInfo(retparts[1]);

                // actual contact (update DHT)
                // ACK|myID:IP:Port:ContactNodeID:myIP:myPort

                String myPartialDHTdata = ContactOtherNode(CorrectedContactInfo);
                // predecessor, successor, keyRange
                String[] myPartialDHTdataList = myPartialDHTdata.split("&");
                handler.setpredecessorInfo(myPartialDHTdataList[0]);
                handler.setKeyRange(myPartialDHTdataList[2]);

                // in joinin process,
                // now new node got fingertable of its succcessor;
                // next I need to update the node's finger table right before the new node.
                // next I may be able to use find successor to update all finger data.



                handler.setfingerTable(myPartialDHTdataList[1]);
                break;
              }
            }

            // as a server

        } catch(TException e) {

        }

    }
    public String CorrectContactInfo(String Info){
      try {
          TTransport  transport = new TSocket(ContactInfo[1], Integer.parseInt(ContactInfo[2]));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);

          //Try to connect
          transport.open();

          // as client reach to other nodes to get information back.
          String[] ContactInfo = Info.split(":");
          // if(ContactInfo)

          return client.find_successor_ByKey(ContactInfo[0], ContactInfo[3], false);

      } catch(TException e) {

      }
    }


    public String ContactOtherNode(String Info){
      String[] ContactInfo = Info.split(":");
      // String myID = ContactInfo[0];
      // String hostname = ContactInfo[1];
      // String port = ContactInfo[2];
      // String ContactNodeID = ContactInfo[3];
      // String myIP = ContactInfo[4];
      // String myPort = ContactInfo[5];


      try {
          TTransport  transport = new TSocket(ContactInfo[1], Integer.parseInt(ContactInfo[2]));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);

          //Try to connect
          transport.open();

          // as client reach to other nodes to get information back.

          return client.UpdateDHT(Info);

      } catch(TException e) {

      }
    }
}
