package com.violation.sqllite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
*
* @author sqlitetutorial.net
*/
public class SQLiteJDBCDriverConnection {
	
	Connection conn = null;
	
	public SQLiteJDBCDriverConnection(){}

    /**
    * Create connection to sonar database
    */
   public Connection openConnection() {
       try {
           // db parameters
           String url = "jdbc:sqlite:./SonarQube.db";
           // create a connection to the database
           conn = DriverManager.getConnection(url);
           System.out.println("Connection to SQLite has been established.");
           return conn;
           
       } catch (SQLException e) {
           System.out.println(e.getMessage());
           return null;
       } 
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