package com.violation.statistic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class StdFileGenerator {

	private static String projectName = "TOTAL";
	private  static String basePath = "projects/"+projectName;
	private static List<String> lines = null; 
	
	public StdFileGenerator(){
		
	}
	
	public void generateStd(){
		try {
			lines = Files.readAllLines((new File(basePath+"/"+projectName+"_correlation.csv")).toPath(),StandardCharsets.ISO_8859_1);
		} catch (IOException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			PrintWriter pw = new PrintWriter (basePath+"/"+projectName+"_std.csv");
			writeHeader(pw);
			String [] array = lines.get(0).split(",");
			for (int i = 0; i < array.length; i++ ){
				if (array[i].contains("+") && array[i].contains("squid"))
					printLine(i,pw);
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void writeHeader(PrintWriter pw){
		String finalRow = "violation" + ",";                     
		finalRow += "#totalRows" + ",";                            
		finalRow += "#rowsDelta>0" + ",";                                                       
		finalRow += "%#rowsDelta>0" + ",";    
		finalRow += "std" + ",";    
		finalRow += "mean" + ",";    
		finalRow += "min" + ","; 
		finalRow += "max" + ","; 
		pw.println(finalRow);                                    
	}
	
	private static void printLine(int startIndex,PrintWriter pw){
		List <Double> list = new LinkedList<Double>();
		String violation = lines.get(0).split(",")[startIndex].substring(8);
		int totalRows = lines.size() -1 ;
		
		for (int i = 1; i<lines.size();i++){
			String line = lines.get(i);
			line = line.replace("\"\"", "");
			String[] array = line.split(",");	
			System.out.println(array.length);
			System.out.println(line);
			int tempIndex = startIndex-2;
			if (Double.parseDouble(array[tempIndex]) > 0){
				//System.out.println(array[startIndex]);
				list.add(Double.parseDouble(array[startIndex]));
			}		
		}
		Statistics s = new Statistics(list);
		double std = s.getStdDev();
		String finalRow = violation+ ",";                     
		finalRow += totalRows + ",";                            
		finalRow += list.size() + ",";                                                       
		finalRow += ((double)list.size()/(double)totalRows) + ",";    
		finalRow += std + ","; 
		finalRow += s.getMean() + ","; 
		finalRow += s.minmax()[0] + ",";
		finalRow += s.minmax()[1] + ",";

		
		pw.println(finalRow);    
	}
	
	
}
