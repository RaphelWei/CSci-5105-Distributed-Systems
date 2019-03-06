import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.*;

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

            for(int i =0; i<directoryListing.length; i++){
              paths.add(directoryListing[i].getPath());
            }

            //contact server with pathnames
            String outputdir = client.CheckDirectory(paths);
            System.out.printf("result list is in %s\n", outputdir);



        } catch(TException e) {

        }

    }
}
