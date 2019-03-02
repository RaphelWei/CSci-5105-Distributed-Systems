import org.apache.thrift.TException;
import java.io.File;

public class CheckDirHandler implements Multiply.Iface
{
        private Set<String> goodWords = new HashSet<String>();
        private Set<String> badWords = new HashSet<String>();

        private int allwords = 0;
        private int poswords = 0;
        private int negwords = 0;

        // private String intermidiatePath;
        // Word boundary defined as whitespace-characters-word boundary-whitespace
        private static final Pattern WORD_BOUNDARY = Pattern.compile("\\s*\\b\\s*");

        @Override
        public boolean ping() throws TException {
  			  System.out.println("I got ping()");
  			  return true;
		    }

        @Override
        public String CheckDir(String DirPath) throws TException {

          File dir = new File(DirPath);
          File[] directoryListing = dir.listFiles();
          if (directoryListing != null) {
            for (int i = 0; i < directoryListing.length; i++){
                if (directoryListing[i].isFile()){ //this line weeds out other directories/folders
                    SubmitTask(directoryListing[i], 9090+((i+1)%5));
                }
            }
          } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
          }
        }

        @Override
        public void SubmitTask(File file, int port) throws TException {
          TTransport  transport = new TSocket("localhost", port);
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          CheckDir.Client client = new CheckDir.Client(protocol);

          //Try to connect
          transport.open();

          //What you need to do.
          client.ping();


          //contact server with pathname
          client.CheckFile(file);
          // to write file


        }

        @Override
        public void CheckFile(File path) throws TException {

          BufferedReader br = new BufferedReader(new FileReader(path));
          String line = null;

          while ((line = br.readLine()) != null) {
            String line = lineText.toString();

            // If caseSensitive is false, convert everything to lower case.
            // if (!caseSensitive) {
              line = line.toLowerCase();
            // }

            // Store each the current word in the queue for processing.
            Text currentWord = new Text();

            for (String word : WORD_BOUNDARY.split(line))
            {
              // if (word.isEmpty() || patternsToSkip.contains(word)) {
              if (word.isEmpty()) {
                continue;
              }
              // Count instances of each (non-skipped) word.
              currentWord = new Text(word);
              // allwords = allwords+1;

              // Filter and count "good" words.
              if (goodWords.contains(word)) {
                poswords = poswords+1;
              }

              // Filter and count "bad" words.
              if (badWords.contains(word)) {
                negwords = negwords+1;
              }
            }
          }
          // write to file
          float sentiment = ((poswords - negwords) / (poswords + negwords));

          String content = "{"+path.getName()+": "+sentiment+"}";
          String intermidiatePath = "./intermidiateData/"+path.getName();
          WriteString(content, intermidiatePath);

           // (filename, sentiment value).
        }

        public void WriteString(String str, String fileName)
          throws IOException {
            // String str = "Hello";
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(str);

            writer.close();
        }


        // Parse the positive words to match and capture during Map phase.
        // goodWordsUri = new Path(args[i]).toUri();
      	private void parsePositive(URI goodWordsUri) {
      		try {
      			BufferedReader fis = new BufferedReader(new FileReader(
      					new File(goodWordsUri.getPath()).getName()));
      			String goodWord;
      			while ((goodWord = fis.readLine()) != null) {
      				goodWords.add(goodWord);
      			}
      		} catch (IOException ioe) {
      			System.err.println("Caught exception parsing cached file '"
      					+ goodWords + "' : " + StringUtils.stringifyException(ioe));
      		}
      	}




        // Parse the negative words to match and capture during Reduce phase.
      	private void parseNegative(URI badWordsUri) {
      		try {
      			BufferedReader fis = new BufferedReader(new FileReader(
      					new File(badWordsUri.getPath()).getName()));
      			String badWord;
      			while ((badWord = fis.readLine()) != null) {
      				badWords.add(badWord);
      			}
      		} catch (IOException ioe) {
      			System.err.println("Caught exception while parsing cached file '"
      					+ badWords + "' : " + StringUtils.stringifyException(ioe));
      		}
      	}
}
