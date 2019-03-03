// javac -cp ".:/usr/local/Thrift/*" Server.java -d .
// javac -cp ".:/usr/local/Thrift/*" Client.java -d .
// javac -cp ".:/usr/local/Thrift/*" Mapper.java -d .
// javac -cp ".:/usr/local/Thrift/*" Sorter.java -d .


// java -cp ".:/usr/local/Thrift/*" Server
// java -cp ".:/usr/local/Thrift/*" Mapper 0 9091
// java -cp ".:/usr/local/Thrift/*" Mapper 0 9092
// java -cp ".:/usr/local/Thrift/*" Mapper 0 9093
// java -cp ".:/usr/local/Thrift/*" Mapper 0 9094
// java -cp ".:/usr/local/Thrift/*" Sorter 9095

// java -cp ".:/usr/local/Thrift/*" Client ./example

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
        //
        // // private int allwords = 0;
        // private double poswords = 0;
        // private double negwords = 0;

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
          // List<String> statusRecords = new ArrayList<String>();
          // private Map<Integer, String> records = new ConcurrentHashMap<Integer, String>();
          File outputDirectory = new File(interDir);
          if (! outputDirectory.exists()){
              outputDirectory.mkdir();
          } else{
            File[] files = outputDirectory.listFiles();
            System.out.println("__________________________________________");
            for (int i=0; i<files.length; i++){
              System.out.println(files[i].getName());
                files[i].delete();
            }
            System.out.println("__________________________________________");
          }
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
            try{
              Thread.sleep(500);
            }catch(Exception e){System.out.println(e);}
            int taskLeftCount = 0;
            left = false;
            for (Map.Entry<String, String> entry : statusRecords.entrySet())
            {

              if(entry.getValue()=="!") {
                SubmitTask(entry.getKey(), portRecords.get(entry.getKey()));
                left = true;
                taskLeftCount=taskLeftCount+1;
              } else if(entry.getValue()=="-"){
                left = true;
                taskLeftCount=taskLeftCount+1;}
            }
            System.out.println("running tasks:  " + directoryListing.length);
            System.out.println("finished tasks: " + (directoryListing.length-taskLeftCount));
          }
          SubmitSorting(interDir, 9095);
        }


        @Override
        public String SubmitTask(String file, int port) throws TException {
          String ret = "-";
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
                        statusRecords.put(file, client.CheckFile(file));
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
            public String SubmitSorting(String interDir, int port) throws TException {
              String ret = "-";
              Runnable Check = new Runnable() {
                      public void run() {
                        try{
                            // System.out.println(port);
                            TTransport  transport = new TSocket("localhost", port);
                            TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
                            CheckDir.Client client = new CheckDir.Client(protocol);

                            //Try to connect
                            transport.open();

                            //What you need to do.
                            client.ping();

                            //contact server with pathname
                            // while(ret = client.CheckFile(file)=="!");
                            statusRecords.put(interDir, client.SortIntermediateData(interDir));
                            System.out.printf("%s finished\n", interDir);
                            transport.close();
                            // to write file
                        } catch (TException x){
                          // x.printStackTrace();
                          System.out.println(x);
                        }
                      }
                  };
                  new Thread(Check).start();
                  System.out.printf("%s started\n", interDir);
                  return ret;
                }

        @Override
        public String CheckFile(String path) throws TException {
          double poswords = 0;
          double negwords = 0;
          System.out.printf("CheckFile");
          if(Math.random()<loadProb && loadFlag){
            return "!";
          }
          System.out.printf("loadprob");
          if(Math.random()<loadProb){
            try{
              Thread.sleep(3000);
            }catch(Exception e){System.out.println(e);}
          }
          System.out.println("delay");
          // BufferedReader br;
          try{
            BufferedReader br = new BufferedReader(new FileReader(path));

            String line = null;

            goodWords = getSentimentWords(pathPositiveWords);
            badWords = getSentimentWords(pathNegativeWords);
            Pattern pattern = Pattern.compile("([a-zA-Z]+\\-*)+");

            // System.out.println("pattern");
            while ((line = br.readLine()) != null) {
              // System.out.println(line);
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
            // System.out.println("writing!!!");
            File outputFile = new File(fileName);
            // fileName = "./intermidiateData/"+outputFile.getName();
            outputFile = new File(interDir+outputFile.getName());
            // System.out.println("writing to" + outputFile.getPath());
            File outputDirectory = new File(interDir);
            try{
            if (outputFile.exists())
                outputFile.delete();
            // File directory = new File(outputDirectory);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.getPath()));
            // System.out.println(outputFile.getParentFile().getName()+"\\"+outputFile.getName()+"\n");
            // System.out.println("Postive word: "+poswords+" negative word: "+negwords+"\n");
            // System.out.println("Sentiment score: "+sentiment+"\n");
            // System.out.println("\n");
            // writer.write(str);

            writer.write(fileName+" "+sentiment);
            writer.close();
            // System.out.println("writing done!!!");
            }catch(IOException e){System.out.println(e);}
            return fileName;
        }

        public String SortIntermediateData(String DirPath) throws TException {

              String path = DirPath;
              File file = new File(path);
              List<String> outputFileList = new ArrayList<String>();
              outputFileList = getFileList(file);
              HashMap<String, Double> map = new HashMap<>();
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

              List<Map.Entry<String, Double>> list = new ArrayList<>(map.entrySet());
              Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                  @Override
                  public int compare(Map.Entry<String, Double> m1, Map.Entry<String, Double> m2) {
                      return -Double.compare(m1.getValue(), m2.getValue());
                  }
              });
              File outputDirectory = new File("./data/");
              if (! outputDirectory.exists()){
                  outputDirectory.mkdir();
              }
              File outputFile = new File("./data/output.txt");
              if (outputFile.exists()){
                  outputFile.delete();
                  System.out.println("--------------------------------------------------------------------------------");

                  System.out.println("--------------------------------------------------------------------------------");
                }
              BufferedWriter out = null;
              for (Map.Entry<String, Double> t:list) {
                  try {
                      out = new BufferedWriter(new OutputStreamWriter(
                              new FileOutputStream("./data/output.txt", true)));
                      out.write(t.getKey()+" "+t.getValue()+"\n");

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
              return DirPath;
          }

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
