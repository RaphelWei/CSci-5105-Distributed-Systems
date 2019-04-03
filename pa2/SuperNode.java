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
public class SuperNode {
    public static WorkWithSuperNodeHandler handler;
    public static WorkWithSuperNode.Processor processor;

    public static void main(String [] args) {
        try {
            handler = new WorkWithSuperNodeHandler();
            processor = new WorkWithSuperNode.Processor(handler);
            System.out.println(args[0]);
            handler.setNumNode(Integer.parseInt(args[1]));
            Runnable simple = new Runnable() {
                public void run() {
                  // System.out.println(args[0]);
                    simple(processor, Integer.parseInt(args[0]));
                }
            };

            new Thread(simple).start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static void simple(CheckDir.Processor processor, int port) {
        try {
            TServerTransport serverTransport = new TServerSocket(port);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
            server.serve();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
