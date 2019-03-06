import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

public class Client {
    public static void main(String [] args) {
        //Create client connect.
        try {
            TTransport  transport = new TSocket("localhost", 9090);
            TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
            CheckDir.Client client = new CheckDir.Client(protocol);

            //Try to connect
            transport.open();

            //What you need to do.
			      client.ping();



            File dir = new File(args[0]);
            File[] directoryListing = dir.listFiles();
            List<String> paths = new ArrayList();
            // List<String> statusRecords = new ArrayList<String>();
            // private Map<Integer, String> records = new ConcurrentHashMap<Integer, String>();
            // File outputDirectory = new File(interDir);
            // if (! outputDirectory.exists()){
            //     outputDirectory.mkdir();
            // } else{
            //   File[] files = outputDirectory.listFiles();
            //   System.out.println("__________________________________________");
            //   for (int i=0; i<files.length; i++){
            //     System.out.println(files[i].getName());
            //       files[i].delete();
            //   }
            //   System.out.println("__________________________________________");
            // }
            for(int i =0; i<directoryListing.length; i++){
              paths.add(directoryListing[i].getPath());
            }

            //contact server with pathnames
            client.CheckDirectory(paths);
            // System.out.printf("I got %d from the server\n", ret);



        } catch(TException e) {

        }

    }
}
