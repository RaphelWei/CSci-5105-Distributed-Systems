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
            Multiply.Client client = new Multiply.Client(protocol);

            //Try to connect
            transport.open();

            //What you need to do.
			      client.ping();


            //contact server with pathname
            String ret = client.CheckDir(args[1]);
            System.out.printf("I got %d from the server\n", ret);



        } catch(TException e) {

        }

    }
}
