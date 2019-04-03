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
            String myIP = args[0];
            String myPort = args[1];
            String m = args[2]; //128
            String SNIP = args[3];
            String SNPort = args[4];

            // SN IP may be hard coded
            TTransport  transport = new TSocket(SNIP, Integer.parseInt(SNPort));
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
                break;
              } else if(retparts[0].equals("ACK")){
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

                // actual contact (update DHT)
                // myNodeMD5:correctTarIP:correctTarPort:correctTarNodeID:correctTarKeyRange:myIP:myPort
                // myPartialDHTdata: tarPredecessor&tarFingerTable(the first one is successor)&myKeyRange
                String myPartialDHTdata = ContactOtherNode(CorrectedContactInfo+":"+myIP+":"+myPort);
                String[] myPartialDHTdataList = myPartialDHTdata.split("&");
                handler.setpredecessorInfo(myPartialDHTdataList[0]);
                handler.setKeyRange(myPartialDHTdataList[2]);

                // in joining process,
                // now new node got fingertable of its succcessor;
                // next I need to update the node's finger table right before the new node. // this has been done by the CorrectedContact node
                // next I may be able to use find successor to update all finger data.

                BigInteger two = BigInteger.valueOf(2);
                BigInteger maxMD5 = two.pow(128);

                String fingertable = myPartialDHTdataList[1];
                String[] Fingers = fingertable.split("|");
                BigInteger myNodeID = new BigInteger(ContactInfo[0], 16);
                String myfingerTable = correctTarInfo;
                for(int i = 1; i<Fingers.length; i++){

                  // String[] FingerInfo1 = Fingers[i].split(":");
                  // BigInteger BIFI1 = new BigInteger(FingerInfo1[2], 16);
                  // String[] FingerInfo2 = Fingers[i-1].split(":");
                  // BigInteger BIFI2 = new BigInteger(FingerInfo2[2], 16);
                  // if(BIFI1.compareTo(BIFI2))

                  // !!!!!!!!!!!!!!!!!
                  // one possible problem may be some of my finger.start, e.g the last finger.start is after my predecessor. Then its successor becomes my successor as no other nodes has my information
                  // this may not happen as we are looking for predecessors and then to find successor at each find_successor_ByKey step. and at this point my predecessor has know my infomation (i.e the first finger).

                  String finger_start = myNodeID.add(two.pow(i)).mod(maxMD5).toString(16);
                  String finger = handler.find_successor_ByKey(finger_start);
                  myfingerTable = myfingerTable+"|"+finger;
                }
                handler.setfingerTable(myfingerTable);
                UpdateOthers(Fingers.length, ContactInfo[0]);
/////////////////////////////////
                break;
              }
            }
            client.PostJoin(myIP, myPort);
            // as a server
            Runnable simple = new Runnable() {
                public void run() {
                  // System.out.println(args[0]);
                    simple(processor, Integer.parseInt(args[2]));
                }
            };

            new Thread(simple).start();


        } catch(TException e) {

        }

    }

    public static void simple(WorkWithNode.Processor processor, int port) {
        try {
            TServerTransport serverTransport = new TServerSocket(port);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
            server.serve();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void UpdateOthers(int Hashm, String myNodeID){
      BigInteger BImyNodeID = new BigInteger(myNodeID, 16);
      BigInteger two = BigInteger.valueOf(2);
      for(int i=0; i<Hashm;i++){
        String affectKey = BImyNodeID.subtract(two.pow(i-1)).toString(16);
        String affectedNodeInfo = handler.find_predeccessor_ByKey(affectKey);
        String[] affectedNodeInfoList = affectedNodeInfo.split(":");
        try {
            TTransport  transport = new TSocket(affectedNodeInfoList[1], Integer.parseInt(affectedNodeInfoList[2]));
            TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
            WorkWithNode.Client client = new WorkWithNode.Client(protocol);

            //Try to connect
            transport.open();

            client.UpdateFingerTable(handler.getmyInfo()+"|"+handler.getRange(), Integer.toString(i));

        } catch(TException e) {

        }

      }
    }


    // Info: myNodeMD5:tarAddr:tarPort:tarNodeMD5
    public static String CorrectContactInfo(String Info){
      String ContactInfo[] = Info.split(":");
      String correctTarInfo = "This is not right! correct contact info failed!!!!";
      try {
          TTransport  transport = new TSocket(ContactInfo[1], Integer.parseInt(ContactInfo[2]));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);

          //Try to connect
          transport.open();

          // as client reach to other nodes to get information back.
          // String[] ContactInfo = Info.split(":");

          // ContactInfo[0]: myNodeMD5; ContactInfo[3]: tarNodeMD5; passedZero: false;
          correctTarInfo = client.find_successor_ByKey(ContactInfo[0]);

      } catch(TException e) {

      }
      return correctTarInfo;
    }

    // Info: myID:tarIP:tarPort:tarNodeMD5:myIP:myPort
    // output: tarPredecessor&tarFingerTable(the first one is successor)&myKeyRange
    public static String ContactOtherNode(String Info){
      String[] ContactInfo = Info.split(":");
      // String myID = ContactInfo[0];
      // String hostname = ContactInfo[1];
      // String port = ContactInfo[2];
      // String ContactNodeID = ContactInfo[3];
      // String myIP = ContactInfo[4];
      // String myPort = ContactInfo[5];
      String myPartialDHTdata = "This is not right! Contact correct node failed!!";

      try {
          TTransport  transport = new TSocket(ContactInfo[1], Integer.parseInt(ContactInfo[2]));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);

          //Try to connect
          transport.open();

          // as client reach to other nodes to get information back.

          // input: myIP:myPort:myID
          // output: tarPredecessor&tarFingerTable(the first one is successor)&myKeyRange
          myPartialDHTdata = client.UpdateDHT(ContactInfo[4]+":"+ContactInfo[5]+":"+ContactInfo[0]);

      } catch(TException e) {

      }
      return myPartialDHTdata;
    }
}
