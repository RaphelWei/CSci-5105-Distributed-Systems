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


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Node {
    public static WorkWithNodeHandler handler;
    public static WorkWithNode.Processor processor;
    // public variable?
    // private String NodeID = 0;
    // private String[] KeyRange = {"0", "0"};
    public static void main(String [] args) {
        try {
            handler = new WorkWithNodeHandler();
            processor = new WorkWithNode.Processor(handler);
            String myIP = args[0];
            String myPort = args[1];
            String m = args[2]; //128
            String SNIP = args[3];
            String SNPort = args[4];

            // SN IP may be hard coded
            TTransport  transport = new TSocket(SNIP, Integer.parseInt(SNPort));
            // TTransport  transport = new TSocket("localhost", 9001);
            TProtocol protocol = new TBinaryProtocol(transport);
            WorkWithSuperNode.Client client = new WorkWithSuperNode.Client(protocol);

            //Try to connect
            transport.open();

            // as a client
            String temp ="";
            while(true){
              System.out.println(args[3]);
              System.out.println(args[4]);
  			      String ret = client.getNodeInfo(myIP, myPort);
              System.out.println(ret);
              String[] retparts = ret.split("\\|");
              System.out.println(retparts.length);
              System.out.println(retparts[1]);
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
                System.out.println("I am the first node!!!!!");
                // I am the first node joining the system.
                // String[] ContactInfo = retparts[1].split(":");
                handler.setNodeID(retparts[1]);
                handler.setmyInfo(myIP+":"+myPort+":"+retparts[1]);
                // my predecessor is me.
                handler.setpredecessorInfo(myIP+":"+myPort+":"+retparts[1]);
                // all my fingers point to me.
                String fingerTable = myIP+":"+myPort+":"+retparts[1];
                int Hashm = Integer.parseInt(m);
                for(int i = 1; i<Hashm; i++){
                  fingerTable=fingerTable+"|"+myIP+":"+myPort+":"+retparts[1];
                }
                handler.setfingerTable(fingerTable);
                System.out.println("I am the first node; finished!!!!!!");
                temp = retparts[1];
                break;
              } else if(retparts[0].equals("ACK")){
                System.out.println("I am not the first node!!!!!");
                System.out.println("I got my MD5 and a node to contact!!!!!   "+retparts[0]);
                //myNodeMD5:tarAddr:tarPort:tarNodeMD5
                String[] ContactInfo = retparts[1].split(":");
                handler.setNodeID(ContactInfo[0]);
                handler.setmyInfo(myIP+":"+myPort+":"+ContactInfo[0]);

                // find the correct node to contact (get the contact info of the node with ID right after mine.)
                // this node will be my successor.
                // retparts[1]: myNodeMD5:tarAddr:tarPort:tarNodeMD5
                // CorrectedContactInfo: myNodeMD5:correctTarIP:correctTarPort:correctTarNodeID:correctTarKeyRange
                String correctTarInfo = CorrectContactInfo(retparts[1]);
                String CorrectedContactInfo = ContactInfo[0]+":"+correctTarInfo;
                System.out.println("this is the node I want to contact!!!!!   "+correctTarInfo);

                // actual contact (update DHT)
                // myNodeMD5:correctTarIP:correctTarPort:correctTarNodeID:correctTarKeyRange:myIP:myPort
                ContactOtherNode(CorrectedContactInfo+":"+myIP+":"+myPort, correctTarInfo);
                temp = ContactInfo[0];

                break;
              }
            }
            client.PostJoin(myIP, myPort);
            System.out.println("finished PostJoin; I guess good to go.");
            // as a server
            Runnable simple = new Runnable() {
                public void run() {
                  // System.out.println(args[0]);
                    simple(processor, Integer.parseInt(myPort));
                }
            };

            new Thread(simple).start();
            String[] Fingers = handler.getfingerTable().split("\\|");

            UpdateOthers(Fingers.length, temp);
            System.out.println("finished UpdateOthers");
            System.out.println("finished populating my finger table " + handler.getfingerTable());
            // System.out.println("finished populating my keyRange     " + handler.getKeyRange());

        } catch(TException e) {
          System.out.println(e);
        }
    }
}
