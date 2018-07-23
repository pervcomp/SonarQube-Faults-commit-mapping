package com.violation.helpers;

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

public class HashAdder {

	private static String projectName = "TOTAL";
	private  static String basePath = "extraction/"+projectName;
	private static List<String> lines = null; 
	private static List<String> commits = null; 
	
	public HashAdder(String project){
		System.out.println(project);
			projectName = project;
			basePath = "extraction/"+projectName;
			 PrintWriter pw = null;
			try {
				pw = new PrintWriter(new File(basePath+"/"+projectName+"_measures-and-issues-cleaned.csv"));
				lines = Files.readAllLines((new File(basePath+"/"+projectName+"_measure-and-issue-history.csv")).toPath());
				System.out.println(lines.size());
				commits = Files.readAllLines((new File(basePath+"/"+projectName+"_git-commits.csv")).toPath(), StandardCharsets.ISO_8859_1);
				writeHeader(lines.get(0),pw);
				lines.remove(0);
				for (String line : lines){
					writeLine(line,pw,commits);
				}
				pw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		}
	
     public void writeHeader(String header, PrintWriter pw){
    	 String headerFinal = header + "," + "git-hash";
    	 pw.println(headerFinal);
     }
     
     public void writeLine(String line, PrintWriter pw, List<String> commits){
    	 String [] array = line.split(",");
    	 String ts = array[0];
    	 String sha = "";
    	 for (String commit : commits){
    		 if (commit.contains(ts)){
    			 array = commit.split(",");
    			 sha = array[2];
    			 break;
    		 }
    	 }
    	 String finalS = line+","+sha;
    	 pw.println(finalS);
     }



}