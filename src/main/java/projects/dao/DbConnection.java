package projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import projects.exception.DbException;

public class DbConnection {
	
	/**
	 * Creates a class that obtains a JDBC Connection object from the driver manager. 
	 * When you call DriverManager.getConnection(), the driver manager looks up the 
	 * MySQL driver and loads it. It then establishes a TCP connection between the 
	 * application and a MySQL server. If the connection cannot be made 
	 * for some reason, the driver manager throws a checked SQLException. 
	 * This is converted to an unchecked exception in a catch block.
	 */

	private static String HOST= "localhost";
	private static String PASSWORD= "projects";
	private static int PORT= 3306;
	private static String SCHEMA= "projects";
	private static String USER= "projects";
	
	public static java.sql.Connection getConnection(){
		String uri= String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", 
				HOST, PORT, SCHEMA, USER, PASSWORD);
				
		
		try {
			Connection conn= DriverManager.getConnection(uri);
			System.out.println("Connection to schema " + SCHEMA + " is successful.");
			return conn;
		} catch (SQLException e) {
			System.out.println("Unable to get connection at "+ uri);
			throw new DbException("Unable to get connection \" +uri");
		}
		
		
	}
	
}
