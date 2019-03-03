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

public class CheckDirHandler implements CheckDir.Iface
{
        private List<String> goodWords = new ArrayList<String>();
        private List<String> badWords = new ArrayList<String>();
        private Map<String, String> statusRecords = new ConcurrentHashMap<String, String>();
        private Map<String, Integer> portRecords = new HashMap<String, Integer>();
        private String pathPositiveWords = "./positive.txt";
        private String pathNegativeWords = "./negative.txt";
        private String interDir = "./intermidiateData/";
        private boolean loadFlag = false;
        // private Map<Integer, Double> LoadProb = new HashMap<Integer, Double>();
        // private int machinePort;
        private double loadProb = 0;

        // private int allwords = 0;
        private double poswords = 0;
        private double negwords = 0;

        public void setLoadProb(double p){loadProb=p;loadFlag=true;}
        // private String intermidiatePath;
        // Word boundary defined as whitespace-characters-word boundary-whitespace
        private static final Pattern WORD_BOUNDARY = Pattern.compile("\\s*\\b\\s*");

        @Override
        public boolean ping() throws TException {
  			  System.out.println("I got ping()");
          // LoadProb.put(9091, 0.8);
          // LoadProb.put(9092, 0.6);
          // LoadProb.put(9093, 0.5);
          // LoadProb.put(9094, 0.2);
  			  return true;
		    }

        @Override
        public void CheckDirectory(String DirPath) throws TException {

          File dir = new File(DirPath);
          File[] directoryListing = dir.listFiles();
          List<String> statusRecords = new ArrayList<String>();
          // private Map<Integer, String> records = new ConcurrentHashMap<Integer, String>();
          Random random = new Random();
          if (directoryListing != null) {
            for (int i = 0; i < directoryListing.length; i++){
                if (directoryListing[i].isFile()){ //this line weeds out other directories/folders
                  int port = 9090+(random.nextInt(4) + 1);
                  statusRecords.put(directoryListing[i].getPath(), SubmitTask(directoryListing[i].getPath(), port));
                  portRecords.put(directoryListing[i].getPath(), port);
                }
            }
          } else {
          }

          boolean left = true;
          while(left){
            Thread.sleep(500);
            int taskLeftCount = 0;
            left = false;
            for (Map.Entry<Integer, String> entry : statusRecords.entrySet())
            {
              if(entry.getValue()=="!") {
                SubmitTask(entry.getKey().getPath(), portRecords.get(entry.getKey().getPath()));
                left = true;
                taskLeftCount=taskLeftCount+1;
              }
            }
            System.out.println("running tasks:  " + directoryListing.length);
            System.out.println("finished tasks: " + (directoryListing.length-taskLeftCount));
          }
          SortIntermediateData(interDir);

        }


        @Override
        public String SubmitTask(String file, int port) throws TException {
          String ret = "!";
          Runnable Check = new Runnable() {
                  public void run() {
                    try{
                        System.out.println(port);
                        TTransport  transport = new TSocket("localhost", port);
                        TProtocol protocol = new TBinaryProtocol(transport);
                        CheckDir.Client client = new CheckDir.Client(protocol);

                        //Try to connect
                        transport.open();

                        //What you need to do.
                        client.ping();

                        //contact server with pathname
                        // while(ret = client.CheckFile(file)=="!");
                        statusRecords.put(file, client.CheckFile(file, port));
                        System.out.printf("%s finished\n", file);
                        transport.close();
                        // to write file
                    } catch (TException x){
                      x.printStackTrace();
                    }
                  }
              };
              new Thread(Check).start();
              System.out.printf("%s started\n", file);
              return ret;
            }

        @Override
        public String CheckFile(String path) throws TException {
          if(Math.random()<LoadProb && loadFlag){
            return "!";
          }
          if(Math.random()<LoadProb){
            Thread.sleep(3000);
          }
          System.out.println("checkfile!!!");
          // BufferedReader br;
          try{
            BufferedReader br = new BufferedReader(new FileReader(path));

            String line = null;

            goodWords = getSentimentWords(pathPositiveWords);
            badWords = getSentimentWords(pathNegativeWords);
            Pattern pattern = Pattern.compile("([a-zA-Z]+\\-*)+");


            while ((line = br.readLine()) != null) {
              System.out.println(line);
              String thisline = line.toString();
              thisline.replace("--", " ");
                Matcher matcher = pattern.matcher(thisline);
                while(matcher.find()){
                    if (goodWords.contains(matcher.group().toLowerCase())) {
                        poswords++;
                    }
                    if (badWords.contains(matcher.group().toLowerCase())) {
                        negwords++;
                    }
                }
            }
          }catch(IOException e){System.out.println(e);}
          // write to file
          double sentiment = ((poswords - negwords) / (poswords + negwords));
          System.out.println(sentiment);
          return WriteString(path, poswords, negwords, sentiment);
        }

        public String WriteString(String fileName, double poswords, double negwords, double sentiment)
          throws TException {
            // String str = "Hello";
            System.out.println("writing!!!");
            File outputFile = new File(fileName);
            // fileName = "./intermidiateData/"+outputFile.getName();
            System.out.println("writing to" + fileName);
            outputFile = new File(fileName);
            File outputDirectory = new File(interDir);
            try{
            if (outputFile.exists())
                outputFile.delete();
            // File directory = new File(outputDirectory);
            if (! outputDirectory.exists()){
                outputDirectory.mkdir();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.getPath()));
            System.out.println(outputFile.getParentFile().getName()+"\\"+outputFile.getName()+"\n");
            System.out.println("Postive word: "+poswords+" negative word: "+negwords+"\n");
            System.out.println("Sentiment score: "+sentiment+"\n");
            System.out.println("\n");
            // writer.write(str);

            writer.write(fileName+" "+sentiment);
            writer.close();
            System.out.println("writing done!!!");
            }catch(IOException e){System.out.println(e);}
            return fileName;
        }

        public String SortIntermediateData(String DirPath) throws TException {
            // List<Map.Entry<String, Double>> pairs = new ArrayList<>();
            Map<String, Double> map = new HashMap<String, Double>();
            try{
              InputStreamReader read = null;
              BufferedReader reader = null;
              File dir = new File(DirPath);
              File[] directoryListing = dir.listFiles();
              if (directoryListing != null) {
                for (int i = 0; i < directoryListing.length; i++){
                    if (directoryListing[i].isFile()){ //this line weeds out other directories/folders
                        // SubmitTask(directoryListing[i].getPath(), 9090+((i+1)%5));
                        read = new InputStreamReader(new FileInputStream(directoryListing[i]),"utf-8");
                        reader = new BufferedReader(read);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(" ");
                            map.put(parts[0], parts[1]);
                        }

                    }
                }
              }
            }catch(IOException e){System.out.println(e);}
            File outputFile;
            try{
              // map = sortByValue(map);

              System.out.println("writing!!!");
              outputFile = new File("./SortedData/Sortedfile.txt");
              System.out.println("writing to" + outputFile.getPath());
              File outputDirectory = new File("./SortedData/");

              if (outputFile.exists())
                  outputFile.delete();
              // File directory = new File(outputDirectory);
              if (! outputDirectory.exists()){
                  outputDirectory.mkdir();
              }
              BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.getPath()));
              // for (HashMap<String,Double> element : pairs) {
              //     writer.write(element.getKey()+" "+element.getValue());
              // }
              Iterator it = map.entrySet().iterator();
              while (it.hasNext()) {
                  Map.Entry pair = (Map.Entry)it.next();
                  writer.write(pair.getKey() + " " + pair.getValue());
                  // System.out.println(pair.getKey() + " = " + pair.getValue());
                  it.remove(); // avoids a ConcurrentModificationException
              }


              writer.close();
            }catch(IOException e){System.out.println(e);}
            return outputFile.getPath();
          }
          // private class ListComparator implements Comparator<String, double> {
          //     @Override
          //     public int compare(Person a, Person b) {
          //         return a.name.compareToIgnoreCase(b.name);
          //     }
          // }
          // public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
          //     List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
          //     list.sort(Entry.comparingByValue());
          //
          //     Map<K, V> result = new LinkedHashMap<>();
          //     for (Entry<K, V> entry : list) {
          //         result.put(entry.getKey(), entry.getValue());
          //     }
          //
          //     return result;
          // }

          public List<String> getSentimentWords(String path) throws TException {
            List<String> strList = new ArrayList<String>();
            File file = new File(path);
            InputStreamReader read = null;
            BufferedReader reader = null;
            try {
                read = new InputStreamReader(new FileInputStream(file),"utf-8");
                reader = new BufferedReader(read);
                String line;
                while ((line = reader.readLine()) != null) {
                    strList.add(line);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (read != null) {
                    try {
                        read.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
            return strList;
        }
}
