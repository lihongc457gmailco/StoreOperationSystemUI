package com.mycompany.storeoperationsystemgui;

import java.io.*;
import java.util.Scanner;

public class UserDatabase {
    
    // The name of the file where data is saved
    private static final String FILENAME = "users.csv";

    // 1. Static block: Runs once when the program starts to ensure the file exists
    static {
        File file = new File(FILENAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("Created new database file: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("Error creating database file: " + e.getMessage());
            }
        }
    }

    // 2. Check Login Credentials
public static boolean checkLogin(String username, String password) {
        try (Scanner scanner = new Scanner(new File(FILENAME))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                
                // standard CSV split
                String[] parts = line.split(","); 
                
                // We now check if there are at least 4 columns (ID, Name, Role, Password)
                if (parts.length >= 4) {
                    String storedUser = parts[0].trim(); // Index 0 is EmployeeID (e.g., C6001)
                    String storedPass = parts[3].trim(); // Index 3 is Password (e.g., a2b1c0)
                    
                    // Simple check to skip the header row if your CSV has one
                    if (storedUser.equalsIgnoreCase("EmployeeID")) {
                        continue;
                    }

                    // Check if credentials match
                    if (storedUser.equals(username) && storedPass.equals(password)) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. Register a New User
// 3. Register a New User
// Inside UserDatabase.java

// UPDATED: Now accepts 4 arguments to match the CSV columns
public static boolean registerUser(String id, String name, String role, String password) {
    
    // 1. Check if the Employee ID already exists
    if (isUserExist(id)) {
        return false; 
    }

    // 2. Append the new user to the CSV file
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME, true))) {
        // Format: EmployeeID,EmployeeName,Role,Password
        writer.write(id + "," + name + "," + role + "," + password);
        writer.newLine(); 
        return true; 
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }
}
    // 4. Helper method to check if a username exists
// 4. Helper method to check if a username exists
public static boolean isUserExist(String username) {
    // 1. Safety check: If the username you are trying to check is empty, return false immediately.
    if (username == null || username.trim().isEmpty()) {
        return false;
    }

    try (Scanner scanner = new Scanner(new File(FILENAME))) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim(); // Read and remove outer spaces
            
            // 2. Skip empty lines in the file
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(",");
            
            // 3. Ensure we have data in the first column
            if (parts.length >= 1) {
                String storedUser = parts[0].trim();
                
                // Skip the Header Row
                if (storedUser.equalsIgnoreCase("EmployeeID")) {
                    continue;
                }

                // --- DEBUG PRINT (Remove this later) ---
                // System.out.println("Checking file ID: [" + storedUser + "] vs Input: [" + username + "]");

                // 4. Check for match (Case-Sensitive)
                if (storedUser.equals(username)) {
                    return true; // Match found!
                }
            }
        }
    } catch (FileNotFoundException e) {
        // If file doesn't exist, no users exist yet.
        return false;
    }
    return false; // No match found after checking all lines
}
// 5. Check if the credentials belong to a Manager
    public static boolean isManager(String id, String password) {
        try (Scanner scanner = new Scanner(new File(FILENAME))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                // Ensure we have 4 columns: ID, Name, Role, Password
                if (parts.length >= 4) {
                    String csvId = parts[0].trim();
                    String csvRole = parts[2].trim();     // Column 2 is Role
                    String csvPass = parts[3].trim();     // Column 3 is Password

                    // Check headers
                    if (csvId.equalsIgnoreCase("EmployeeID")) continue;

                    // logic: ID matches + Password matches + Role is Manager
                    if (csvId.equals(id) && csvPass.equals(password)) {
                        if (csvRole.equalsIgnoreCase("Manager")) {
                            return true; // Is a manager
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false; // Not found, wrong password, or not a manager
    }
}