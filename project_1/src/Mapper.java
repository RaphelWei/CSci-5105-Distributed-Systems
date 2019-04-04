

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Mapper {

    //TODO Set path
<<<<<<< HEAD
    public static String pathPositiveWords = "./data/positive.txt";
    public static String pathNegativeWords = "./data/negative.txt";
    public static String path = "./data/example/";
    public static String pathOutputDir = "./data/output_dir/";
=======
    public static String pathPositiveWords = "data/positive.txt";
    public static String pathNegativeWords = "data/negative.txt";
    public static String path = "data/example/";
    public static String pathOutputDir = "data/output_dir/";
>>>>>>> cbb5ad2831281312fde0fb6cec059f7067d45b52

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
            read = new InputStreamReader(new FileInputStream(file), "utf-8");
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


    public static void main(String[] args) throws IOException {
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

        File outputDir = new File(pathOutputDir);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        } else {
            File[] files = outputDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }


        // TODO Traversal Through all files in the Given Input Directory

        int NUM_OF_THREAD = 5;
        ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
        List<Future<?>> futures = new ArrayList<>();
        int iter = list.size() / NUM_OF_THREAD;


        for (int i = 0; i < iter; i++) {
            String l = list.get(i);
            Callable t1 = new Task(pattern, l, outputDir, positiveWords, negativeWords);
            futures.add(executor.submit(t1));
        }
        for (int i = iter; i < iter * 2; i++) {
            String l = list.get(i);
            Callable t2 = new Task(pattern, l, outputDir, positiveWords, negativeWords);
            futures.add(executor.submit(t2));
        }
        for (int i = 2 * iter; i < iter * 3; i++) {
            String l = list.get(i);
            Callable t3 = new Task(pattern, l, outputDir, positiveWords, negativeWords);
            futures.add(executor.submit(t3));
        }
        for (int i = 3 * iter; i < iter * 4; i++) {
            String l = list.get(i);
            Callable t4 = new Task(pattern, l, outputDir, positiveWords, negativeWords);
            futures.add(executor.submit(t4));
        }
        for (int i = 4 * iter; i < list.size(); i++) {
            String l = list.get(i);
            Callable t5 = new Task(pattern, l, outputDir, positiveWords, negativeWords);
            futures.add(executor.submit(t5));
        }
        executor.shutdown();
<<<<<<< HEAD



    }
}
=======
    }
}




>>>>>>> cbb5ad2831281312fde0fb6cec059f7067d45b52
