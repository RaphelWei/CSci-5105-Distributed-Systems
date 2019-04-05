import java.io.*;

import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.*;
import javax.xml.bind.DatatypeConverter;


public class Client {
    public static HashMap<String, String> readGivenFile(String path) throws IOException{
        HashMap<String, String> map = new HashMap<>();
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String thisline = null;
            while ((thisline = br.readLine()) != null) {
                String[] str = thisline.split(":");


                map.put(str[0], str[1]);


            }

        } catch (IOException e) {
            System.err.println("File does not exist. Please input the correct file path.");
        }
        return map;
    }

    public static void lookupBook(String bookTitle) throws NoSuchAlgorithmException{
        

    }

    public static void insertRecord(HashMap<String, String> record) {


    }


    public static String getHashedKey(String key) throws NoSuchAlgorithmException{
        String hashedKey = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(key.getBytes());
            byte[] digest = md.digest();
            hashedKey = DatatypeConverter.printHexBinary(digest).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No such Algorithm Exception!");
        }
        return hashedKey;
    }

    public static void printAllNodeInfo() {
        
    }




    // args format 
    public static void main (String[] args) throws IOException, NoSuchAlgorithmException {

        for (;;) {
            System.out.print("\n\n\n");
            System.out.println("******************************** System starts ************************************************");
            System.out.println("Please specify the operation that you want this system to execute: ");
            System.out.println("Set - Setting a book title and its genre from a given file.");
            System.out.println("Get - Looking up a book title for its genre.");
            System.out.println("Insert - Inserting a new record to the system or updating the genre for a existing book.");
            System.out.println("Print - Printing the information of all nodes.");
            System.out.println("Exit - Exiting from the system.");
            System.out.print("\n\n\n");


//        if (args.length != 1) {
//            System.out.println("Please use the operation listed above!");
//            System.exit(1);
//        }

            Scanner scan = new Scanner(System.in);
            String operation = scan.nextLine();


            if (operation.equals("Set")) {
                System.out.println("Please input the path of the given file: ");
                String path = scan.nextLine();

                HashMap<String, String> map = readGivenFile(path);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    System.out.println(getHashedKey(entry.getKey()) + "*********************" + entry.getValue());
                }
                System.out.print("\n");
            }



            else if (operation.equals("Get")) {
                System.out.println("Please input the book title you want to look up: ");
                String bookTitle = scan.nextLine();
                String bookTitleMD5 = getHashedKey(bookTitle);
                System.out.println(bookTitleMD5);




                System.out.println("\n");
            }

            else if (operation.equals("Insert")) {
                System.out.println("Please input the book title and genre pair: ");
                String pair = scan.nextLine();
                String[] str = pair.split(":")
                if (str.length != 2) {
                	System.out.println("Improper format. Please check your input pair.");
                } else {
                	HashMap<String, String> record = new HashMap<>();
                	record.put(str[0], str[1]);
                	
                }
                  //record.put(bookTitle, genre);

//                String record = scan.nextLine();
//                record = record.split();
//                HashMap<String, String>

            }

            else if (operation.equals("Print")) {
                System.out.println("Please in ");
                String bookTitle = scan.nextLine();
            }

            else if (operation.equals("Exit")) {
                System.out.println("******************************** System ends ************************************************");
                System.exit(0);
            }

            else {
                System.out.println("Invalid command. Please use the operations listed above.");
            }
        }



    }
}
