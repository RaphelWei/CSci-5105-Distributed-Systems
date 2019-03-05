import java.io.*;
import java.util.*;
import java.lang.String;
import java.util.Map;
import java.util.concurrent.Executors;

public class Sort {


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


    public static void main(String[] args) throws IOException{

        String path = "data/output_dir/";
        File file = new File(path);
        List<String> outputFileList = new ArrayList<String>();
        outputFileList = getFileList(file);
        HashMap<String, Double> map = new HashMap<>();
        for (String l : outputFileList){
            File filename = new File(l);
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String thisline = null;
            while((thisline = br.readLine()) != null) {
                String[] str = thisline.split(" ");
                map.put(str[0], Double.parseDouble(str[1]));
            }
        }

        List<Map.Entry<String, Double>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> m1, Map.Entry<String, Double> m2) {
                return -Double.compare(m1.getValue(), m2.getValue());
            }
        });
//        for (Map.Entry<String, Double> t: list) {
//            System.out.println(t.getKey()+" "+t.getValue());
//        }
        File outputFile = new File("./data/log.txt");
        if (outputFile.exists())
            outputFile.delete();
        BufferedWriter out = null;
        for (Map.Entry<String, Double> t:list) {
            try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("./data/inter_output.txt", true)));
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



    }
}
