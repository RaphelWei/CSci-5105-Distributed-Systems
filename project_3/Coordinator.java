import java.util.ArrayList;
import java.io.*;
public class Coordinator {
	final private static int numOfFiles = 5;
	final private static int numW;
	final private static int numR;
	final private static int numTotal;
	final private static String pathOutputDir = "./Coordinator/";
    // private static ArrayList<Server> serverList = new ArrayList<Server>();


    // Format of the input args: [CordPort] [Nr] [Nw] [N]
    // [CordPort] - Port number of coordinator
    // [Nr] - Number of read
    // [Nw] - Number of write
    // [N] - Total number 
    public static void main(String[] args) {
    	numR = Integer.parseInt(args[1]);
    	numW = Integer.parseInt(args[2]);
    	numTotal = Integer.parseInt(args[3]);

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


