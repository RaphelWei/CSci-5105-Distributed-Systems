import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Map {

    //TODO Set path
    public static String pathPositiveWords = "./data/positive.txt";
    public static String pathNegativeWords = "./data/negative.txt";
    public static String path = "./data/example/";

    /**
     * Returns a list from positive.txt or negative.txt;
     *
     * @param path the path of input files;
     * @return the list of positive or negative words
     */
    public static List<String> getSentimentWords(String path) {
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

    /**
     *
     * @param file path of the directory which contains all the input files
     * @return list of all the pathnames of input files
     */
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





    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO Get The List of Sentiment words
        List positiveWords = getSentimentWords(pathPositiveWords);
        List negativeWords = getSentimentWords(pathNegativeWords);


        // TODO Get A List of All Input File Names
        File file = new File(path);
        List<String> list = new ArrayList<String>();
        list = getFileList(file);

        // TODO Initialization
        int numPositiveWords = 0;
        int numNegativeWords = 0;
        double sentimentScore = 0.0;
        Pattern pattern = Pattern.compile("([a-zA-Z]+\\-*)+");

        File outputFile = new File("./data/log.txt");
        if (outputFile.exists())
            outputFile.delete();

        // TODO Traversal Through all files in the Given Input Directory
        for (String l : list) {

            File filename = new File(l);
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String thisline = null;
            while((thisline = br.readLine()) != null) {
                thisline.replace("--", " ");
                Matcher matcher = pattern.matcher(thisline);
                while(matcher.find()){
                    if (positiveWords.contains(matcher.group().toLowerCase())) {
                        numPositiveWords++;
                    }
                    if (negativeWords.contains(matcher.group().toLowerCase())) {
                        numNegativeWords++;
                    }
                }
                //for (String originalWord : thisline.split("\\s*\\b\\s*")) {
//                for (String originalWord: matcher) {
//                    String word = originalWord.toLowerCase();
//                    if (word.isEmpty()) {
//                        continue;
//                    }
//
//                    // TODO Count "positive" words
//                    if (positiveWords.contains(word)) {
//                        numPositiveWords++;
//
//                    }
//
//                    // TODO Count "bad" words
//                    if (negativeWords.contains(word)) {
//                        numNegativeWords++;
//                    }
//                }

            }
            sentimentScore = (double)(numPositiveWords-numNegativeWords)/(numPositiveWords+numNegativeWords);


            // TODO Write in a file named "log.txt".
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("./data/log.txt", true)));
                out.write(filename.getParentFile().getName()+"\\"+filename.getName()+"\n");
                out.write("Postive word: "+numPositiveWords+" negative word: "+numNegativeWords+"\n");
                out.write("Setiment score: "+sentimentScore+"\n");
                out.write("\n");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // TODO Reset counted numbers of sentiment words
            numNegativeWords = 0;
            numPositiveWords = 0;

        }

    }
}
