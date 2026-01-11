package com.mycompany.storeoperationsystemgui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class UserDatabase {
    
    private static final String FILENAME = "users.csv";

    // 1.Runs once when the program starts to ensure the file exists
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
                
                //CSV split
                String[] parts = line.split(","); 
                
                //check if there are at least 4 columns (ID, Name, Role, Password)
                if (parts.length >= 4) {
                    String storedUser = parts[0].trim(); // Index 0 is EmployeeID (e.g., C6001)
                    String storedPass = parts[3].trim(); // Index 3 is Password (e.g., a2b1c0)
                    
                    // Simple check to skip the header row 
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
public static boolean isUserExist(String username) {
    if (username == null || username.trim().isEmpty()) {
        return false;
    }

    try (Scanner scanner = new Scanner(new File(FILENAME))) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(",");
            
            if (parts.length >= 1) {
                String storedUser = parts[0].trim();
                
                if (storedUser.equalsIgnoreCase("EmployeeID")) {
                    continue;
                }
                if (storedUser.equals(username)) {
                    return true; 
                }
            }
        }
    } catch (FileNotFoundException e) {
        return false;
    }
    return false;
}
    public static boolean isManager(String id, String password) {
        try (Scanner scanner = new Scanner(new File(FILENAME))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length >= 4) {
                    String csvId = parts[0].trim();
                    String csvRole = parts[2].trim();  
                    String csvPass = parts[3].trim();

                    if (csvId.equalsIgnoreCase("EmployeeID")) continue;

                    if (csvId.equals(id) && csvPass.equals(password)) {
                        if (csvRole.equalsIgnoreCase("Manager")) {
                            return true;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false; 
    }
}