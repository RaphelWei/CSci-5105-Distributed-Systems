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
            System.out.println(args[1]);
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

    public static void simple(WorkWithSuperNode.Processor processor, int port) {
        try {
          // TServerTransport serverTransport = new TServerSocket(port);
          // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
          // server.serve();


          //Create Thrift server socket
          TServerTransport serverTransport = new TServerSocket(port);
          TTransportFactory factory = new TFramedTransport.Factory();

          // //Create service request handler
          // MultiplyHandler handler = new WorkWithNodeHandler();
          // processor = new WorkWithNode.Processor(handler);

          //Set server arguments
          TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
          args.processor(processor);  //Set handler
          args.transportFactory(factory);  //Set FramedTransport (for performance)

          //Run server as a single thread
          TServer server = new TThreadPoolServer(args);
          server.serve();






          // //Create Thrift server socket
          // TServerTransport serverTransport = new TServerSocket(port);
          // TTransportFactory factory = new TFramedTransport.Factory();
          //
          // // //Create service request handler
          // // MultiplyHandler handler = new WorkWithNodeHandler();
          // // processor = new WorkWithNode.Processor(handler);
          //
          // //Set server arguments
          // TServer.Args args = new TServer.Args(serverTransport);
          // args.processor(processor);  //Set handler
          // args.transportFactory(factory);  //Set FramedTransport (for performance)
          //
          // //Run server as a single thread
          // TServer server = new TSimpleServer(args);
          // server.serve();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
