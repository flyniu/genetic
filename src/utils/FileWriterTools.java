package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileWriterTools {
	private static File file = new File("F:\\GeneLog.txt");
	private static File fileBefore = new File("F:\\test\\BEFORE.TXT");
	private static File fileAfter = new File("F:\\test\\AFTER.TXT");
	public static void WriteToLog(String content){
		try {
			FileWriter fileWriter = new FileWriter(file, true);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.println(content);
			printWriter.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void WriteBefore(String content){
		try {
			FileWriter fileWriter = new FileWriter(fileBefore, true);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.println(content);
			printWriter.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void WriteAfter(String content){
		try {
			FileWriter fileWriter = new FileWriter(fileAfter, true);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.println(content);
			printWriter.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
