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
public class Mapper {
    public static CheckDirHandler handler;
    public static CheckDir.Processor processor;

    public static void main(String [] args) {
        try {
            handler = new CheckDirHandler();
            processor = new CheckDir.Processor(handler);
            System.out.println(args[0]);
            System.out.println(args[1]);
            handler.setLoadProb(Double.parseDouble(args[0]));
            if(args[1].equals("true"))
            handler.setLoadFlag();
            Runnable simple = new Runnable() {
                public void run() {
                  // System.out.println(args[0]);
                    simple(processor, Integer.parseInt(args[2]));
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
