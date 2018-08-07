package com.violation.sqllite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
*
* @author sqlitetutorial.net
*/
public class SQLiteJDBCDriverConnection {
	
	private Connection conn = null;
	
	public SQLiteJDBCDriverConnection(){}

    /**
    * Create connection to sonar database
    */
   public boolean openConnection() {
       try {
           // db parameters
           String url = "jdbc:sqlite:./SonarQube.db";
           // create a connection to the database
           conn = DriverManager.getConnection(url);
           System.out.println("Connection to SQLite has been established.");
           return true;
           
       } catch (SQLException e) {
           System.out.println(e.getMessage());
           return false;
       } 
   }
   
   
   public Connection getConnection(){
	   return conn;
   }

   /**
   * Close connection to sonar database
   */
  public void closeConnection() {
      try {
    	  if (conn != null)
    		  conn.close();
      } catch (SQLException e) {
          System.out.println(e.getMessage());
      } 
  }
   
}