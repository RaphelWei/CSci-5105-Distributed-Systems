import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



class Task implements Callable{

    public Pattern pattern;
    public String l;
    public List<String> positiveWords;
    public List<String> negativeWords;
    public File outputDir;

    public Task(Pattern p, String l, File file, List<String> pos, List<String> neg) throws IOException{
        this.pattern = p;
        this.l = l;
        this.positiveWords = pos;
        this.negativeWords = neg;
        this.outputDir = file;
    }

    public Object call() throws IOException{
        System.out.println(Thread.currentThread().getName()+"starts.");
        int numPositiveWords = 0;
        int numNegativeWords = 0;
        File filename = new File(l);
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String thisline = null;
        while((thisline = br.readLine()) != null) {
<<<<<<< HEAD
            thisline = thisline.replaceAll("--", " ");
            Matcher matcher = pattern.matcher(thisline);
            while(matcher.find()){
                String temp = matcher.group();
                if (positiveWords.contains(temp.toLowerCase())) {
                    numPositiveWords++;
                }

                if (negativeWords.contains(temp.toLowerCase())) {
                    numNegativeWords++;
                }
            }

//                for (String originalWord: matcher) {
//                    String word = originalWord.toLowerCase();
//                    if (word.isEmpty()) {
//                        continue;
//                    }
//
//                    // TODO Count "positive" words
//
//
//                    // TODO Count "bad" words
//
//                }

=======
            thisline = thisline.replaceAll("--|''|\t|,|\\.|;|:|!|\\?|\\||\\[|\\]|\\(|\\)", " ");
            thisline = thisline.replaceAll(" '|^'|'", " ");
            thisline = thisline.replaceAll("'.*? ", " ");
            thisline = thisline.replaceAll("'.*?\n", "\n");
            for (String originalWord : thisline.split(" ")) {
                String word = originalWord.toLowerCase();
                if (positiveWords.contains(word)) {
                        numPositiveWords++;
                    }
                if (negativeWords.contains(word)) {
                        numNegativeWords++;
                    }
            }
//                for (String originalWord: matcher) {
//                    String word = originalWord.toLowerCase();
//                    if (word.isEmpty()) {
//                        continue;
//                    }
//
//                    // TODO Count "positive" words
//
//
//                    // TODO Count "bad" words
//
//                }

>>>>>>> cbb5ad2831281312fde0fb6cec059f7067d45b52

//            Matcher matcher = pattern.matcher(thisline);
//            while(matcher.find()){
//                String temp = matcher.group();
//                if (positiveWords.contains(temp.toLowerCase())) {
//                    numPositiveWords++;
//
//                }
//
//                if (negativeWords.contains(temp.toLowerCase())) {
//                    numNegativeWords++;
//
//
//                }
//            }
        }
        double sentimentScore = (double)(numPositiveWords-numNegativeWords)/(numPositiveWords+numNegativeWords);

        // TODO Write in a file named "log.txt".
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputDir.getAbsolutePath()+"/"+filename.getName(), true)));
            out.write(filename.getParentFile().getName()+"\\"+filename.getName()+" "+sentimentScore+"\n");
            //out.write("Postive word: "+numPositiveWords+" negative word: "+numNegativeWords+"\n");
            //out.write("Setiment score: "+sentimentScore+"\n");
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
        System.out.println(Thread.currentThread().getName()+"has been finished.");
        return null;
    }
}