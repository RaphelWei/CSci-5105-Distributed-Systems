import java.net.InetAddress;

public class Client {
    

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
    // [mode] - 4 types of 
    //        - Read-heavy
    //        - Write-heavy
    //        - 
    //        - 
    // [ClientPort] - 

    public static void main(String[] args) {

        InetAddress myIP = InetAddress.getLocalHost();
        String clientIP = myIP.getHostAddress();
        int mode = Integer.parseInt(args[2]);
        String clientPort = args[3];
        switch (mode) {
            case 0:
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