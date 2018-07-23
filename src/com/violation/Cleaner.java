package com.violation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.violation.helpers.CommitObject;
import com.violation.helpers.HashMapGenerator;

public class Cleaner {
	private static String projectName = "TOTAL";
	private  static String basePath = "projects/"+projectName;
	private static LinkedHashMap <String, Integer> indexes;
	private static int indexSha = 0;
	private static List<String> projects;
	
	
	public Cleaner(){}
	
	public void getCleanedTotalFile(List<String> projects){
		this.projects = projects;
		correlate();
		projectName = "TOTAL";
	    basePath = "projects/"+projectName;
		stripDuplicatesFromFile(basePath+ "/"+projectName+"_cleaned.csv");
	}
	
	
	

	private static void fillMapIndexes(String projectName){
		String tempPath = basePath + "/"+projectName+"_measures-and-issues-cleaned.csv";
		BufferedReader br=null;

			try {
				br = new BufferedReader(new FileReader((tempPath)));
				String sCurrentLine = br.readLine();
				String[] array=sCurrentLine.split(",");
				for(int i =0; i< array.length; i++){
					if (array[i].equals("git-hash")){
						indexSha = i;
						break;
					}
				}
				HashMapGenerator hmg = new HashMapGenerator(sCurrentLine);
				indexes = hmg.getHashMapFields();
			
				
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}
	
	private  void correlate() {
		PrintWriter pw = null;
		try {
			 pw = new PrintWriter(basePath + "/"+projectName+"_cleaned.csv");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		basePath = "projects/"+projects.get(0);
		fillMapIndexes(projects.get(0));
		writeHeader(pw);
		for (String project : projects){
			if (project.equals("TOTAL"))
				continue;
	    basePath = "projects/"+project;
	    this.projectName = project;
		fillMapIndexes(project);
		
		String tempPath = basePath + "/"+project+"_BugInducingCommits.csv";
		BufferedReader br;
		String sCurrentLine="";
		int fail = 0;
		int count = 1;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			while ((sCurrentLine = br.readLine()) != null ) {
				if (!sCurrentLine.startsWith("bug") && !sCurrentLine.isEmpty()){
				String shaInducing = sCurrentLine.split(";")[3];
				String shaFixing = sCurrentLine.split(";")[0];
				CommitObject inducingCommit = null;
				CommitObject fixingCommit = null;
				CommitObject indMinusOneCommit = null;
				if (!getCommiString(shaInducing).isEmpty())
					inducingCommit  = new CommitObject (getCommiString(shaInducing),indexes,shaInducing);
				if (!getCommiString(shaFixing).isEmpty())
					fixingCommit = new CommitObject( getCommiString(shaFixing),indexes,shaFixing);				
				if (!getCommitPredString(shaInducing).isEmpty() && !getCommitPredString(shaInducing).contains("measure"))
					indMinusOneCommit = new CommitObject (getCommitPredString(shaInducing),indexes, getSha(getCommitPredString(shaInducing)));
				if ((inducingCommit!=null &&fixingCommit!= null) && indMinusOneCommit!=null ){
					writeFinalRow( indMinusOneCommit,  inducingCommit,  fixingCommit,pw, project);
					fail = 0;}
				else{
					fail++;
					if (fail == 100)
						break;
				}}
				}
				
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sCurrentLine);
		} }
		pw.close();
		
	}
	
	/**
	 * 
	 * @param sha
	 * @return
	 */
	private static String getCommiString(String sha){
		String tempPath = basePath + "/"+projectName+"_measures-and-issues-cleaned.csv";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.contains(sha)){
					br.close();
					return sCurrentLine;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  return "";
	}
	
	/**
	 * 
	 * @param sha
	 * @return
	 */
	private static String getCommitPredString(String sha){
		String tempPath = basePath + "/"+projectName+"_measures-and-issues-cleaned.csv";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			String sCurrentLine;
			String sCurrentLineTemp = "";
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.contains(sha)){
					br.close();
					return sCurrentLineTemp;
				}
				sCurrentLineTemp = sCurrentLine;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  return "";
	}
	
	private   String getSha(String row){
		String[] array = row.split(",");
		return array[indexSha];
		
	}
	
	
	private static void writeHeader(PrintWriter pw){
		String finalRow = "project" + ",";
        finalRow += "ind-1 sha" + ",";                     
		finalRow += "ind-1 ts" + ",";                            
		finalRow += "ind sha" + ",";                             
		finalRow += "ind ts" + ",";                              
		finalRow += "fixing sha" + ",";                          
		finalRow += "fixing ts" + ","; 
		finalRow += "jira_key " + ",";
		finalRow += "priority " + ",";
		for (String key : indexes.keySet()){
			finalRow += "∆(ind-1 ind)"+key + ",";;
			finalRow += "∆(ind fixing)"+key+ ",";;
			finalRow += "∆1 + ∆2 "+key+ ",";
			finalRow += "∆1 + ∆2 "+key+ " cleaned,";
		}

		pw.println(finalRow);                                    
	}
	
	private static void writeFinalRow(CommitObject indMinusOne, CommitObject ind, CommitObject fixing, PrintWriter pw, String project){
		String finalRow = project+",";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH:mm:SS");
	    finalRow += indMinusOne.getSha() + ",";
		finalRow += sdf.format(new Date(indMinusOne.getTimeEpoch()*1000)) + ",";
		finalRow += ind.getSha() + ",";
		finalRow       += sdf.format(new Date(ind.getTimeEpoch()*1000)) + ",";
		finalRow += fixing.getSha() + ",";
		finalRow       += sdf.format(new Date(fixing.getTimeEpoch()*1000)) + ",";
		finalRow += getFaultKey(fixing.getSha()) + ",";
		finalRow += getPriority(fixing.getSha()) + ",";
		
		LinkedHashMap <String,Double> indMinusOneMap = indMinusOne.getFieldsValues();
		LinkedHashMap <String,Double> indMap = ind.getFieldsValues();
		LinkedHashMap <String,Double> fixMap = fixing.getFieldsValues();
		NumberFormat numberFormatter = new DecimalFormat("0.00");
		for (String key : indexes.keySet()){
			if (!key.contains("git-changed-files")){
				finalRow += numberFormatter.format(indMap.get(key) - indMinusOneMap.get(key)) +",";
				finalRow += numberFormatter.format(fixMap.get(key) - indMap.get(key)) +",";
				finalRow += numberFormatter.format((fixMap.get(key) - indMap.get(key))+(indMap.get(key) - indMinusOneMap.get(key))) +",";
				double std = getStd(key);
				double sum = (fixMap.get(key) - indMap.get(key))+(indMap.get(key) - indMinusOneMap.get(key));
				if ((indMap.get(key) - indMinusOneMap.get(key)) <= 0 )
					finalRow += "-999" + ",";
				else if (sum < std)
					finalRow += "0.0" + ",";
				else
					finalRow += numberFormatter.format(sum)+ ",";
			}
				
		}
		pw.println(finalRow);
	}
	
	private static String getFaultKey(String fixingSha){
		String result = "";
		String tempPath = basePath + "/"+projectName+"_BugFixingCommits.csv";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.startsWith(fixingSha)){
					result= sCurrentLine.split(";")[3];
					break;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	  return result;
	}
	
	private static String getPriority(String fixingSha){
		String result = "";

	  return result;
	}
	
	public static void stripDuplicatesFromFile(String filename)  {
	    BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			Set<String> lines = new LinkedHashSet<String>(10000); // maybe should be bigger
		    String line;
		    while ((line = reader.readLine()) != null) {
		        lines.add(line);
		    }
		    reader.close();
		    PrintWriter writer = new PrintWriter((filename));
		    String header = "";
		    for (String unique : lines) {
		      if (unique.contains("ind"))
		    	  header = unique;
		    }
		    writer.println(header);
		    lines.remove(header);
		    
		    for (String unique : lines) {
		        writer.println(unique);

		    }
		    writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	private static double getStd(String violation){
		try {
			List<String> lines = Files.readAllLines(new File("projects/TOTAL/TOTAL_std.csv").toPath());
			for (String line : lines){
				String[] array = line.split(",");
				if (array[0].contains(violation))
					return Double.parseDouble(array[4]) * 3;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0.0;
	}
	
	
	

}

	
	
	


