/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rentcar;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        Connection connection = DatabaseConnection.connect();
        if (connection != null) {
            System.out.println("Database connection successful!");
        } else {
            System.out.println("Failed to connect to the database.");
        }
    }
}

