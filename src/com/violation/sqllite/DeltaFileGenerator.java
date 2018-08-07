package com.violation.sqllite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class DeltaFileGenerator {
	private String project;
	private SQLiteJDBCDriverConnection connection;
	private List<String> squidsList;
	private Connection c = null;
	private final String fileName = "SONAR_ADDED_REMOVED_ISSUES";
	private  String basePath = "extraction/";
	private List<String> commits = null;
	
	public DeltaFileGenerator(String project) {
		this.project = project;
		basePath = basePath + project;
		squidsList = new LinkedList<String>();
		if (connection.openConnection()){
			c = connection.getConnection();}
		else{
			c = null;
			System.out.print("Connection to SonarQube DB LiteSQL failed");
			System.exit(-99);
		}
		try {
			try {
				commits = Files.readAllLines((new File(basePath+"/"+project+"_git-commits.csv")).toPath(), StandardCharsets.ISO_8859_1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PrintWriter pw = new PrintWriter ("SONAR_ADDED_REMOVED_ISSUES");
			printHeader(pw);
			List<String> lines = Files.readAllLines((new File(basePath+"/measure-history.csv")).toPath(), StandardCharsets.ISO_8859_1);
			for (String line : lines){
				printRow(pw, line );
			}

			pw.close();
				
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * It prints the header
	 * @param pw
	 */
	private void printHeader(PrintWriter pw){
		String header = "PROJECT-ID, COMMIT_SHA, MEASURE-DATE,";
		String sql = "SELECT DISTINCT rule "
        		+ "FROM sonar_Issues "
        		+ "ORDER by rule";
        Statement stmt = null;
        ResultSet rs = null;
		try {
			stmt = c.createStatement();
			rs    = stmt.executeQuery(sql);
			while (rs.next()) {
	            String rule =  rs.getString("rule");
	            header += "ADDED_SQUID:"   + rule.replaceAll("[^0-9]","")   + ",";
	            header += "REMOVED_SQUID:" + rule.replaceAll("[^0-9]","") + ",";
	            header += "TOT_SQUID:"     + rule.replaceAll("[^0-9]","")     + ",";
	            squidsList.add(rule);
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.println(header);
	}
	
	private void printRow(PrintWriter pw, String rowMeasure ){
		String[] array = rowMeasure.split(",");
		String measureDate = array[0];
		String commitSha = getCommitSha(measureDate);
		String finalRow =  project     + "," 
						 + commitSha   + ","
						 + measureDate + ","; 		
		for (String squid : squidsList){
			finalRow +=  getCountAdded(squid,measureDate) +",";
			finalRow +=  getCountRemoved(squid,measureDate) +",";
			finalRow +=  getTotal(squid,measureDate) +",";
		}
		pw.print(finalRow);
	}
	
	private String getCommitSha(String measureDate){
		for(String commit : commits){
			 if (commit.contains(measureDate)){
    			 String[] array = commit.split(",");
    			 return  array[2];
    		 }
		}
		return null;
		
	}
	
	private int getCountAdded(String rule,String measureDate){
		String sql = "select count(*) from sonar_issues where rule like '%"+rule+ "%' and "
				+ "creationDate='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"'";
		Statement stmt = null;
        ResultSet rs = null;
		try {
			stmt = c.createStatement();
			rs    = stmt.executeQuery(sql);
			int count =  rs.getInt(0);  
	        return count;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	private int getCountRemoved(String rule,String measureDate){
		String sql = "select count(*) from sonar_issues where rule like '%"+rule+ "%' and "
				+ "closeDate='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"'";
		Statement stmt = null;
        ResultSet rs = null;
		try {
			stmt = c.createStatement();
			rs    = stmt.executeQuery(sql);
			int count =  rs.getInt(0);  
	        return count;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	private int getTotal(String rule,String measureDate){
		return getCountAddedWithHistory(rule,measureDate) - 
				getCountRemovedWithHistory(rule,measureDate);
	}
	
	
	
	private int getCountAddedWithHistory(String rule,String measureDate){
		String sql = "select count(*) from sonar_issues where rule like '%"+rule+ "%' and "
				+ "creationDate<='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"'";
		Statement stmt = null;
        ResultSet rs = null;
		try {
			stmt = c.createStatement();
			rs    = stmt.executeQuery(sql);
			int count =  rs.getInt(0);  
	        return count;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	private int getCountRemovedWithHistory(String rule,String measureDate){
		String sql = "select count(*) from sonar_issues where rule like '%"+rule+ "%' and "
				+ "closeDate<='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"'";
		Statement stmt = null;
        ResultSet rs = null;
		try {
			stmt = c.createStatement();
			rs    = stmt.executeQuery(sql);
			int count =  rs.getInt(0);  
	        return count;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	
}
