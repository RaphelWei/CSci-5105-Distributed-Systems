import java.net.InetAddress;
import java.util.

public class Client {
    
    // First, this method would form a request, (read )
    // the it will connect to a given server, this server would later forward the request to the coordinator
    public static void connectServer(String op, String fileName, String content, String clinetIP, String clientPort) {
        String request = "";
        REQ request = new REQ(op, fileName, content, clientIP, clientPort);
        try {
            TTransoprt transport = new TSocket(clientIP, Integer.parseInt(clientPort));
            TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
            ServerWorkHandler.Client client = new ServerWorkHandler.Client(protocol);
            // Try to connect
            transport.open();
            client.request(request);
            transport.close();
        } catch (TException e) {
            e.printStackTrace();
        }    
    } 

    // read a local script to begin read/write - heavy mode
    // then connect to a given server     
    public static void handleRequest(String path) {
        File file = new File(path);
        BufferedReader reader = null;
        try {               
            reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                // str: [op] / [filename] / [content]
                String[] str = line.split("/");
                connectServer(str[0], str[1], str[2], clinetIP, clientPort);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }







    

    // Format of the input args: [ServerIP] [ServerPort] [mode] [ClientPort]
    // [ServerIP] - The IP address of the node the client connect to
    // [ServerPort] - The port number of the node
    // [mode] - 3 types of 
    //      0 - Read-heavy, where 90% are Reads and 10% are Writes.
    //      1 - Write-heavy, where 10% are Reads and 90% are Writes.
    //      2 - Read-write, where the number of Reads and Writes are same, 50% v.s. 50%
    // [ClientPort] - The prot number of the client

    public static void main(String[] args) {

        InetAddress myIP = InetAddress.getLocalHost();
        String clientIP = myIP.getHostAddress();
        int mode = Integer.parseInt(args[2]);
        String clientPort = args[3];
        switch (mode) {
            case 0:
            int StartTime = System.
                handleRequest("./read-heavy.txt");
                break;
            case 1:
                handldRequest("./write-heavy.txt");
                break;
            case 2:
                // statement
                break;
            case 3:
                // statement
                break;
            default:
                System.out.println("Invalid format! Please check your input command.");
         
        }
    }
}