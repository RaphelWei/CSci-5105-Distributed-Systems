import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

// Generated code
public class Server {
    public static MultiplyHandler handler;
    public static Multiply.Processor processor;

    public static void main(String [] args) {
        try {
            handler = new MultiplyHandler();
            processor = new Multiply.Processor(handler);

            Runnable simple = new Runnable() {
                public void run() {
                    simple(processor);
                }
            };

            new Thread(simple).start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static void simple(Multiply.Processor processor) {
        try {
            //Create Thrift server socket
            TServerTransport serverTransport = new TServerSocket(9090);
            TTransportFactory factory = new TFramedTransport.Factory();

            //Create service request handler
            MultiplyHandler handler = new MultiplyHandler();
            processor = new Multiply.Processor(handler);

            //Set server arguments
            TServer.Args args = new TServer.Args(serverTransport);
            args.processor(processor);  //Set handler
            args.transportFactory(factory);  //Set FramedTransport (for performance)

            //Run server as a single thread
            TServer server = new TSimpleServer(args);
            server.serve();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

