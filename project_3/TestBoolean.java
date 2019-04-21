package com.example.Java;

import java.util.*;
import java.io.*;

public class TestBoolean {

	public static void main(String[] args) {

		 	//TODO Auto-generated method stub
		String path1 = "./read-heavy.txt";
		try { 

			File file1 = new File(path1);
			FileOutputStream fos1 = null;
			if(!file1.exists()){
				file1.createNewFile();
				fos1 = new FileOutputStream(file1);
			}else{

				file1.delete();
				BufferedWriter out1 = null;
				out1 = new BufferedWriter(new OutputStreamWriter(   
					new FileOutputStream(file1, true)));
				for (int i = 0; i<1000;i++) {
					System.out.println(i);
					Random rand1 = new Random();
					int n1 = rand1.nextInt(10);
					if (n1 == 0) {
						Random randomw1 = new Random();
						int num1 = randomw1.nextInt(9);
						out1.write("W/Helloworld[" + num1 + "]/Helloworld[" + num1 + "]");
						out1.newLine();
					} else {
						Random randomr1 = new Random();
						int num2 = randomr1.nextInt(9);

						out1.write("R/Helloworld[" + num2 + "]/" + "");
						out1.newLine();
					}

				}
				out1.close(); 
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		String path2 = "./write-heavy.txt";
		try { 

			File file2 = new File(path2);
			FileOutputStream fos2 = null;
			if(!file2.exists()){
				file2.createNewFile();
				fos2 = new FileOutputStream(file2);
			}else{

				file2.delete();
				BufferedWriter out2 = null;
				out2 = new BufferedWriter(new OutputStreamWriter(   
					new FileOutputStream(file2, true)));
				for (int i = 0; i<1000;i++) {
					System.out.println(i);
					Random rand2 = new Random();
					int n2 = rand2.nextInt(10);
					if (n2 != 0) {
						Random randomw2 = new Random();
						int num3 = randomw2.nextInt(9);
						out2.write("W/Helloworld[" + num3 + "]/Helloworld[" + num3 + "]");
						out2.newLine();
					} else {
						Random randomr2 = new Random();
						int num4 = randomr2.nextInt(9);

						out2.write("R/Helloworld[" + num4 + "]/" + "");
						out2.newLine();
					}

				}
				out2.close(); 
			}

		} catch (Exception e) {
			e.printStackTrace();
		}



		String path3 = "./read-only.txt";
		try { 
			
			File file3 = new File(path3);
			FileOutputStream fos3 = null;
			if(!file3.exists()){
				file3.createNewFile();
				fos3 = new FileOutputStream(file3);
			}else{

				file3.delete();
				BufferedWriter out3 = null;
				out3 = new BufferedWriter(new OutputStreamWriter(   
					new FileOutputStream(file3, true)));
				for (int i = 0; i<1000;i++) {
					System.out.println(i);

					

					Random rand3 = new Random();
					int num5 = rand3.nextInt(9);

					out3.write("R/Helloworld[" + num5 + "]/" + "");
					out3.newLine();
				}
				

				out3.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		String path4 = "./write-only.txt";
		try { 
			
			File file4 = new File(path4);
			FileOutputStream fos4 = null;
			if(!file4.exists()){
				file4.createNewFile();
				fos4 = new FileOutputStream(file4);
			}else{

				file4.delete();
				BufferedWriter out4 = null;
				out4 = new BufferedWriter(new OutputStreamWriter(   
					new FileOutputStream(file4, true)));
				for (int i = 0; i<1000;i++) {
					System.out.println(i);
					Random rand4 = new Random();
					int num6 = rand4.nextInt(9);
					
					out4.write("W/Helloworld[" + num6 + "]/Helloworld[" + num6 + "]");
					out4.newLine();



				}
				

				out4.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}