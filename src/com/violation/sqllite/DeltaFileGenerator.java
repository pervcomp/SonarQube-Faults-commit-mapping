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
	private final String fileName = "SONAR_ADDED_REMOVED_ISSUES.csv";
	private  String basePath = "extraction/";
	private List<String> commits = null;
	private Statement stmt = null;
	private int countAdded = 0;
	private int countAddedHistory = 0;
	private int countRemoved = 0;
	private int countRemovedHistory = 0;

	
	public DeltaFileGenerator(String project) {
		this.project = project;
		basePath = basePath + project;
		squidsList = new LinkedList<String>();
		connection = new SQLiteJDBCDriverConnection ();
		if (connection.openConnection()){
			c = connection.getConnection();
			try {
				stmt = c.createStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			c = null;
			System.out.print("Connection to SonarQube DB LiteSQL failed");
			System.exit(-99);
		}
		try {
			try {
				commits = Files.readAllLines((new File(basePath+"/git-commits.csv")).toPath(), StandardCharsets.ISO_8859_1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PrintWriter pw = new PrintWriter (basePath + "/" + fileName);
			printHeader(pw);
			List<String> lines = Files.readAllLines((new File(basePath+"/measure-history.csv")).toPath(), StandardCharsets.ISO_8859_1);
			lines.remove(0);
			int count =lines.size();
			for (String line : lines){
				System.out.println(project + " missing "+count +" commits");
				printRow(pw, line );
				count--;
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
				+ "WHERE rule like '%squid%'"
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
		String measureDate = array[0].replaceAll("\"", "");
		String commitSha = getCommitSha(measureDate);
		String finalRow =  project     + "," 
						 + commitSha   + ","
						 + measureDate + ",";
		int count = squidsList.size();
		long startTime = System.currentTimeMillis();
		for (String squid : squidsList){
			getTotal( squid, measureDate);
		    count--;
			finalRow +=  countAdded +",";
		    finalRow +=  countRemoved +",";
			finalRow +=  (countAddedHistory-countRemovedHistory) +",";
	

		
		}
		long end = System.currentTimeMillis();
		System.out.println(end-startTime);
		pw.print(finalRow.replace("\"",""));
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
		String sql = "select count(RowNo) from sonar_issues"
				+ ""
				+ ""
				+ " where rule = '"+rule+ "' "+ "and "
				+ "creationDate='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"'";
		
        ResultSet rs = null;
		try {
			rs    = stmt.executeQuery(sql);
			int count =  rs.getInt(1);  
	        return count;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	private int getCountRemoved(String rule,String measureDate){
		String sql = "select count(RowNo) from sonar_issues where rule = '"+rule+ "' and "
				+ "closeDate='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"'";
        ResultSet rs = null;
		try {
			rs    = stmt.executeQuery(sql);
			int count =  rs.getInt(1);  
	        return count;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	private int getTotal(String rule,String measureDate){
		/*return getCountAddedWithHistory(rule,measureDate) - 
				getCountRemovedWithHistory(rule,measureDate);*/
		
		String sql = "select count(RowNo) from sonar_issues where rule = '"+rule+ "' and "
				+ "creationDate='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"' UNION ALL ";
			   sql += "select count(RowNo) from sonar_issues where rule = '"+rule+ "' and "
				+ "closeDate='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"' UNION ALL ";
		      sql += "select count(RowNo) from sonar_issues where rule = '"+rule+ "' and "
				+ "creationDate<='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"' UNION ALL  ";
		      sql +=   "select count(RowNo) from sonar_issues where rule = '"+rule+ "' and "
				+ "(closeDate<='"+measureDate+"')and "
				+ "PROJECT_ID='"+project+"'";
        ResultSet rs = null;
        
        try {
			stmt = c.createStatement();
			rs    = stmt.executeQuery(sql);
			int count = 1;
			while (rs.next()) {
				switch (count){
				case 1: 
					countAdded = rs.getInt(1);
					break;
				case 2:
					countRemoved = rs.getInt(1);
					break;
				case 3: 
					countAddedHistory = rs.getInt(1);
					break;
				case 4:
					countRemovedHistory = rs.getInt(1);
					break;
					
			
				}
				count++;
	        }
			/*int countAdded =  rs.getInt(1); 
			boolean s = rs.next();
			rs.next();
			int countRemoved =  rs.getInt(1); 
	        return (countAdded - countRemoved);*/
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return 0;
	}
	
	
	
	private int getCountAddedWithHistory(String rule,String measureDate){
		String sql = "select count(RowNo) from sonar_issues where rule = '"+rule+ "' and "
				+ "creationDate<='"+measureDate+"'and "
				+ "PROJECT_ID='"+project+"'";
        ResultSet rs = null;
		try {
			stmt = c.createStatement();
			rs    = stmt.executeQuery(sql);
			int count =  rs.getInt(1);  
	        return count;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	private int getCountRemovedWithHistory(String rule,String measureDate){
		String sql = "select count(RowNo) from sonar_issues where rule = '"+rule+ "' and "
				+ "(closeDate<='"+measureDate+"')and "
				+ "PROJECT_ID='"+project+"'";
        ResultSet rs = null;
		try {
			stmt = c.createStatement();
			rs    = stmt.executeQuery(sql);
			int count =  rs.getInt(1);  
	        return count;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	
}
