/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rentcar; // Use your project's package structure

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/rentcar"; // Your database name
    private static final String USER = "root"; // Your MySQL username
    private static final String PASSWORD = ""; // Your MySQL password (empty if not set)

    public static Connection connect() {
        Connection connection = null;
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish Connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database: rentcar");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
        return connection;
    }
}
