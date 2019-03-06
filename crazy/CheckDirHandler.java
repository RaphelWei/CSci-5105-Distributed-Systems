// javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server.java -d .
// javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Client.java -d .
// javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Mapper.java -d .
// javac -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Sorter.java -d .


// java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Server
// java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Mapper 0 true 9091
// java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Mapper 0 true 9092
// java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Mapper 0 true 9093
// java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Mapper 0 true 9094
// java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Sorter 9095

// java -cp ".:/Users/muyun/Downloads/thrift-0.11.0/lib/java/build/*" Client ./example

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

        // status for each task
        private Map<String, String> statusRecords = new ConcurrentHashMap<String, String>();
        // port bounded to each task
        private Map<String, Integer> portRecords = new HashMap<String, Integer>();

        private String pathPositiveWords = "./positive.txt";
        private String pathNegativeWords = "./negative.txt";
        private String interDir = "./intermidiateData/";
        private String outputFilename = "./data/output.txt";
        private String outputDir = "./data/";

        // specify number of compute nodes
        private int numNode = 4;

        // compute node statistc
        private int numTaskRec = 0;
        private long avgTaskTime = 0;
        private final Object lock = new Object();

        // flag for rejecting task
        private boolean loadFlag = false;
        // prob for rejection and injection
        private double loadProb = 0;
        // sort task status: "-" means not fininshed; outputdir means finished.
        private String SortStatus = "-";

        public void setLoadProb(double p){loadProb=p;}
        public void setLoadFlag(){loadFlag=true;}

        // Word boundary defined as whitespace-characters-word boundary-whitespace
        private static final Pattern WORD_BOUNDARY = Pattern.compile("\\s*\\b\\s*");

        @Override
        public boolean ping() throws TException {
  			  System.out.println("I got ping()");
  			  return true;
		    }

        @Override
        // client deliver a list of file pathnames for server to schedule.
        // this method would run on server, evoked by client
        public String CheckDirectory(List<String> Paths) throws TException {
          // initial timestamp
          long initTime = System.nanoTime();
          // make directory or cleaning up the directory
          File interDirectory = new File(interDir);
          if (! interDirectory.exists()){
              interDirectory.mkdir();
          } else{
            File[] files = interDirectory.listFiles();
            for (int i=0; i<files.length; i++){
              System.out.println(files[i].getName());
                files[i].delete();
            }
          }
          // partitioning tasks by files
          Random random = new Random();
          if (Paths != null) {
            for (int i = 0; i < Paths.size(); i++){
              File temp = new File(Paths.get(i));
                if (temp.isFile()){
                  int port = 9090+(random.nextInt(numNode) + 1);
                  // initialize status and threading for each task.
                  statusRecords.put(Paths.get(i), SubmitTask(Paths.get(i), port));
                  portRecords.put(Paths.get(i), i);
                }
            }
          } else {
          }
          // monitor for status of each map task
          boolean left = true;// flag for any task left
          while(left){
            try{
              // periodically checking
              Thread.sleep(1000);
            }catch(Exception e){System.out.println(e);}
            int taskLeftCount = 0;
            left = false;
            for (Map.Entry<String, String> entry : statusRecords.entrySet())
            {
              System.out.println("Monitor: "+"task: "+entry.getKey()+", intermidiatePath:   "+entry.getValue());
              if(entry.getValue().equals("!")) {// rejected and restart
                statusRecords.put(entry.getKey(), "-");
                System.out.println("Monitor: "+entry.getKey()+" restarting");
                int PortInd = random.nextInt(numNode) + 1;
                if(PortInd == portRecords.get(entry.getKey())){
                  PortInd = (PortInd+1)%numNode;
                }
                SubmitTask(entry.getKey(), PortInd+9090);// restart
                portRecords.put(entry.getKey(), PortInd);// update port value
                left = true;
                taskLeftCount=taskLeftCount+1; // count unfinished task
              } else if(entry.getValue().equals("-")){// keep waiting
                System.out.println("Monitor: "+entry.getKey()+" waiting");
                left = true;
                taskLeftCount=taskLeftCount+1; // count unfinished task
              }
            }
            System.out.println("Monitor: running mapper tasks:  " + Paths.size());
            System.out.println("Monitor: finished mapper tasks: " + (Paths.size()-taskLeftCount));
          }
          // all map tasks are marked as done
          // start sorting
          SubmitSorting(statusRecords, 9090+(random.nextInt(numNode) + 1));
          while(SortStatus.equals("-")){ // periodically check status of sorting
            try{
              Thread.sleep(500);
            }catch(Exception e){System.out.println(e);}
            System.out.println("Monitor: Sorting task is running");
          }
          System.out.println("Monitor: Sorting task is done");

          long elapsedTime = System.nanoTime() - initTime;
          return "output file pathname: "+SortStatus+", nanoseconds left: "+elapsedTime;
        }


        @Override
        // submit each map task to compute node by threading
        // this method would run on server
        public String SubmitTask(String file, int port) throws TException {
          String ret = "-";
          Runnable Check = new Runnable() {
                  public void run() {
                    try{
                        System.out.println(port);
                        TTransport  transport = new TSocket("localhost", port);
                        TProtocol protocol = new TBinaryProtocol(transport);
                        CheckDir.Client client = new CheckDir.Client(protocol);

                        transport.open();
                        client.ping();
                        // task submission
                        String Temp = client.CheckFile(file);
                        statusRecords.put(file, Temp);

                        if(!Temp.equals("!")){
                          System.out.println("--------------------------------------------------------------------------------");
                          System.out.printf("%s finished\n", file);
                          System.out.println(Temp);
                          System.out.println("--------------------------------------------------------------------------------");
                        }
                        transport.close();

                    } catch (TException x){
                      x.printStackTrace();
                      // System.out.println(x);
                    }
                  }
              };
              new Thread(Check).start();
              System.out.printf("%s started\n", file);
              return ret;
            }

        @Override
        // submit a sorting task by threading
        // this method would run on server
        public void SubmitSorting(Map<String, String> statusRecords, int port) throws TException {
          // String ret = "-";
          Runnable Check = new Runnable() {
                  public void run() {
                    try{
                        TTransport  transport = new TSocket("localhost", port);
                        TProtocol protocol = new TBinaryProtocol(transport);
                        CheckDir.Client client = new CheckDir.Client(protocol);

                        transport.open();
                        client.ping();

                        // sorting submission
                        SortStatus = client.SortIntermediateData(statusRecords);
                        System.out.println("--------------------------------------------------------------------------------");
                        System.out.printf("Sorting finished\n");
                        System.out.println("--------------------------------------------------------------------------------");
                        transport.close();
                    } catch (TException x){
                      System.out.println(x);
                    }
                  }
              };
              new Thread(Check).start();
              System.out.printf("%s started\n", interDir);
            }

        @Override
        // read a file by its pathname to evaluate sentiment value.
        // this method would run on compute node
        public String CheckFile(String path) throws TException {



          double poswords = 0;
          double negwords = 0;
          // System.out.printf("CheckFile");
          if(Math.random()<loadProb && loadFlag){ // rejection
            System.out.println(path+" rejected with prob "+ loadProb);
            return "!";
          }

          // init timestamp
          long initTime = System.nanoTime();


          if(Math.random()<loadProb){ // injection delay
            try{
              Thread.sleep(3000);
            }catch(Exception e){System.out.println(e);}
            System.out.println("delaying!");
          }


          try{
            BufferedReader br = new BufferedReader(new FileReader(path));

            String line = null;

            // populating sentiment words.
            goodWords = getSentimentWords(pathPositiveWords);
            badWords = getSentimentWords(pathNegativeWords);
            Pattern pattern = Pattern.compile("([a-zA-Z]+\\-*)+");

            // check each word in the file line by line
            while ((line = br.readLine()) != null) {
              // System.out.println(line);
              String thisline = line.toString();
              thisline = thisline.replaceAll("--", " ");
                Matcher matcher = pattern.matcher(thisline);
                while(matcher.find()){
                    // sentiment check for each word
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
          String ret = WriteString(path, poswords, negwords, sentiment);

          long elapsedTime = System.nanoTime() - initTime;
          System.out.println("elapsed time for my job:  "+elapsedTime);
          synchronized (lock) {
            numTaskRec = numTaskRec+1;
            avgTaskTime = (avgTaskTime+elapsedTime)/numTaskRec;
            System.out.println("average time for my jobs: "+avgTaskTime);
          }
          return ret;
        }

        @Override
        // write filename, sentiment value pairs in files. each file would have 1 pair
        public String WriteString(String fileName, double poswords, double negwords, double sentiment)
          throws TException {
            File outputFile = new File(fileName);
            outputFile = new File(interDir+outputFile.getName());
            File interDirectory = new File(interDir);
            try{
            if (outputFile.exists())
                outputFile.delete();

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.getPath()));
            // System.out.println(outputFile.getParentFile().getName()+"\\"+outputFile.getName()+"\n");
            // System.out.println("Postive word: "+poswords+" negative word: "+negwords+"\n");
            // System.out.println("Sentiment score: "+sentiment+"\n");
            // System.out.println("\n");
            // writer.write(str);

            writer.write(outputFile.getParentFile().getName()+"\\"+outputFile.getName()+" "+sentiment);
            writer.close();
            // System.out.println("writing done!!!");
            }catch(IOException e){System.out.println(e);}
            return outputFile.getPath();
        }

        @Override
        // sort input sentiment value and write into a single file.
        public String SortIntermediateData(Map<String, String> statusRecords) throws TException {
              long initTime = System.nanoTime();
              List<String> outputFileList = new ArrayList<String>(statusRecords.values());
              HashMap<String, Double> map = new HashMap<>();

              // parse each line in each file in a map.
              try{
                for (String l : outputFileList){
                    File filename = new File(l);
                    BufferedReader br = new BufferedReader(new FileReader(filename));
                    String thisline = null;
                    while((thisline = br.readLine()) != null) {
                        String[] str = thisline.split(" ");
                        map.put(str[0], Double.parseDouble(str[1]));
                    }
                }
              }catch(Exception e){System.out.println(e);}

              // sort the map
              List<Map.Entry<String, Double>> list = new ArrayList<>(map.entrySet());
              Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                  @Override
                  public int compare(Map.Entry<String, Double> m1, Map.Entry<String, Double> m2) {
                      return -Double.compare(m1.getValue(), m2.getValue());
                  }
              });

              File outputDirectory = new File(outputDir);
              if (! outputDirectory.exists()){
                  outputDirectory.mkdir();
              }
              File outputFile = new File(outputFilename);
              if (outputFile.exists()){
                  outputFile.delete();
                }

              // writing out the sorted map to one file.
              BufferedWriter out = null;
              for (Map.Entry<String, Double> t:list) {
                  try {
                      out = new BufferedWriter(new OutputStreamWriter(
                              new FileOutputStream(outputFile, true)));
                      out.write(t.getKey()+" "+t.getValue()+"\n");
                      System.out.println(t.getKey()+" "+t.getValue()+"\n");

                  } catch (Exception e) {
                      e.printStackTrace();
                      System.out.println(e);
                  } finally {
                      try {
                          out.close();
                      } catch (IOException e) {
                          e.printStackTrace();
                          System.out.println(e);
                      }
                  }
              }

              long elapsedTime = System.nanoTime() - initTime;
              System.out.println("elapsed time for my job:  "+elapsedTime);
              synchronized (lock) {
                numTaskRec = numTaskRec+1;
                avgTaskTime = (avgTaskTime+elapsedTime)/numTaskRec;
                System.out.println("average time for my jobs: "+avgTaskTime);
              }

              return outputFilename;
          }

          // helpper function: resolve a directory to a list of files inside
          public static List<String> getFileList(File file) {
              List<String> result = new ArrayList<String>();

              if (!file.isDirectory()) {
                  System.out.println(file.getAbsolutePath());
                  result.add(file.getAbsolutePath());
              } else {
                  File[] directoryList = file.listFiles(new FileFilter() {
                      public boolean accept(File file) {
                          if (file.isFile()) {
                              return true;
                          } else {
                              return false;
                          }
                      }
                  });
                  for (int i = 0; i < directoryList.length; i++) {
                      result.add(directoryList[i].getPath());
                  }
              }

              return result;
          }

          // helper function: read words with sentiment value into a list and return the list.
          public List<String> getSentimentWords(String path) throws TException {
            List<String> strList = new ArrayList<String>();
            File file = new File(path);
            InputStreamReader read = null;
            BufferedReader reader = null;
            try {
                read = new InputStreamReader(new FileInputStream(file),"utf-8");
                reader = new BufferedReader(read);
                String line;
                // reading and forming the list.
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
                        e.printStackTrace();
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            return strList;
        }
}
