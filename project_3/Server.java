import java.io.*;

public class Server {
	private static Node n = new Node();
	// private static int id = s.getID();
	// private static String ip = s.getIP();
	// private static String port = s.getPort();
	private static String pathOutputDir = "";

	final private static int numOfFiles = 5;

    // Format of the input args: [ServerIP] [ServerPort] [ClientPort]
    // [CordIP] - The IP address of the Coordinator
    // [CordPort] - The port number of the Coordinator
    // [ServerPort] - The port number of the Server
	public static void main(String[] args) {

		InetAddress myIP = InetAddress.getLocalHost();
        n.setIP(myIP.getHostAddress());
        n.setPort(args[2]);
        pathOutputDir = "./" + n.getIP() + ":_" + n.getPort();
		File outputDir = new File(pathOutputDir);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        } else {
            File[] files = outputDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }

    	for (int i = 1; i <= numOfFiles; i++) {
    		String fileName = "HelloWorld-" + "[" + i +"]" + ".txt";
    		try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(pathOutputDir + fileName, true)));
                out.write(fileName + " " + 0 + "\n");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    	}
	}
}