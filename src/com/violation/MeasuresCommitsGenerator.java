package com.violation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;

import com.violation.helpers.HashMapGenerator;

public class MeasuresCommitsGenerator {
	private List<String> projects;
	private int indexSha = 0;
	private static LinkedHashMap <String, Integer> indexes;
	
	public MeasuresCommitsGenerator(List<String> projects){
		this.projects = projects;
		PrintWriter pw  = null;

		try {
			pw = new PrintWriter("./extraction/TOTAL/TOTAL_measures_with_commits.csv");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> lines = null;
		List<String> linesCommits = null;
		try {
			lines = Files.readAllLines((new File("extraction/"+projects.get(0)+"/"+projects.get(0)+"_measures-and-issues-cleaned.csv").toPath()),StandardCharsets.ISO_8859_1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	
		fillMapIndexes(lines.get(0));
		writeHeader(pw);
		for (String project : projects){
			
			try {
				lines = Files.readAllLines((new File("extraction/"+project+"/"+project+"_measures-and-issues-cleaned.csv").toPath()),StandardCharsets.ISO_8859_1);
				linesCommits  = Files.readAllLines((new File("extraction/"+project+"/"+project+"_git-commits.csv").toPath()),StandardCharsets.ISO_8859_1);
				System.out.println(project);
				System.out.println(lines.size());
				fillMapIndexes( lines.get(0));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String header = lines.get(0);
			for (int i = 1; i<lines.size();i++){
				
				writeLine( header,  project,  lines.get(i),  pw, linesCommits);
			}
			
		}
		pw.close();
	}
	
	private  void fillMapIndexes(String header){
		HashMapGenerator hmg = new HashMapGenerator(header);
		indexes = hmg.getHashMapFields();
	}
	
	private static void writeHeader(PrintWriter pw){
		String finalRow = "project" + ",";                     
		finalRow += "commitSha" + ",";                            
		finalRow += "commitTs" + ",";                             
		finalRow += "commitType" + ",";                              
		for (String key : indexes.keySet()){
			finalRow += key.replace("\"", "") +  ",";;
		}
		pw.println(finalRow);                                    
	}
	
	

	private String getCommitType(List<String>lines, String sha){
		for (String line : lines){
			if (line.contains(sha)){
				String[] array = line.split(",");
				if (array[0].contains(sha))
					return "-1";
				else if (array[3].contains(sha))
					return "0";
			}
		}
		return "-3";
	}
	
	private void writeLine(String header, String project, String line, PrintWriter pw, List<String> commits){
		List<String> linesInducing = null;
		try {
			linesInducing = Files.readAllLines((new File("./extraction/"+project+"/"+project+"_BugInducingCommits.csv")).toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		line = line.replace("\"\"", "");
		String sha = "";
		int lenght = 0;
		String nummerFileChanged = "";
		String[] arrayLine = line.split("\",");
		String date = arrayLine[0];
		for (String commit : commits){
			if (commit.contains(date)){
				commit = commit.replace("\"\"", "");
				String[] array = commit.split("\",");
			    sha = array[2];
			    lenght = array[2].split(",").length;
				
			}
		}
		
		String finalRow = project + ",";
	    finalRow += sha.replace("\"", "") + ",";
		finalRow += arrayLine[getIndex(header,  "measure-date")].replace("\"", "") +",";
		String type = getCommitType(linesInducing,sha ) ;
		finalRow += type + ",";
		for (String key : indexes.keySet()){
			if (key.contains("git-changed-files")){
				finalRow += lenght;
			}
			else{
			if (getIndex(header,  key) == 999)
				finalRow +=   "0,";
			else
			    finalRow += arrayLine[getIndex(header,  key)].replace("\"", "")+  ",";}
		}
		pw.println(finalRow);
		
	}
	
	private int getIndex(String header, String columnName){
		String[] array = header.split(",");
		for (int i = 0; i<array.length; i++)
			if (array[i].contains(columnName))
				return i;
		return 999;
	}
	
	
}
