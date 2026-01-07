package com.mycompany.storeoperationsystemgui;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AttendanceManager {

    private static final String FILE_NAME = "attendance.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Ensure file exists with headers
    static {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("UserID,Outlet,ClockInTime,ClockOutTime,TotalHours,Status");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // --- 1. CLOCK IN ---
    public static String clockIn(String userId, String outlet) {
        // First, check if user is already clocked in (has a "PENDING" status)
        if (isCurrentlyClockedIn(userId)) {
            return "Error: You are already clocked in! Please clock out first.";
        }

        String now = LocalDateTime.now().format(FORMATTER);
        
        // Append new line: UserID, Outlet, InTime, "PENDING", "0", "Clocked In"
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(userId + "," + outlet + "," + now + ",PENDING,0,Active");
            writer.newLine();
            return "Success: Clocked in at " + now;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Could not save attendance.";
        }
    }

    // --- 2. CLOCK OUT ---
    public static String clockOut(String userId) {
        List<String> lines = new ArrayList<>();
        boolean foundActive = false;
        String resultMessage = "Error: No active Clock-In found.";

        // Read all lines into memory
        try (Scanner scanner = new Scanner(new File(FILE_NAME))) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            return "Error: Database not found.";
        }

        // Loop to find the user's active session
        // We assume the header is at index 0, so we start loop
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(",");

            // Check if this row belongs to User AND is "Active"
            if (parts.length >= 6 && parts[0].equals(userId) && parts[3].equals("PENDING")) {
                
                // Found the row! Now calculate time.
                String clockInStr = parts[2];
                LocalDateTime inTime = LocalDateTime.parse(clockInStr, FORMATTER);
                LocalDateTime outTime = LocalDateTime.now();
                
                // Calculate duration
                Duration duration = Duration.between(inTime, outTime);
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                String totalTime = hours + "h " + minutes + "m";
                
                String clockOutStr = outTime.format(FORMATTER);

                // Update the line: ID, Outlet, In, OUT, Total, Status
                String updatedLine = parts[0] + "," + parts[1] + "," + parts[2] + "," + 
                                     clockOutStr + "," + totalTime + ",Completed";
                
                lines.set(i, updatedLine); // Replace the old line
                foundActive = true;
                resultMessage = "Success: Clocked out. Worked: " + totalTime;
                break; // Stop searching once updated
            }
        }

        if (foundActive) {
            // Rewrite the whole file with the update
            try (PrintWriter pw = new PrintWriter(new File(FILE_NAME))) {
                for (String line : lines) {
                    pw.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultMessage;
    }

    // Helper: Check if user is already IN
    private static boolean isCurrentlyClockedIn(String userId) {
        try (Scanner scanner = new Scanner(new File(FILE_NAME))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 6 && parts[0].equals(userId) && parts[3].equals("PENDING")) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) { }
        return false;
    }
    
    // Helper: Load data for the JTable
    public static String[][] getAttendanceData() {
        List<String[]> dataList = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(FILE_NAME))) {
            if (scanner.hasNextLine()) scanner.nextLine(); // Skip Header
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    dataList.add(parts);
                }
            }
        } catch (FileNotFoundException e) { }
        
        // Convert List to 2D Array
        String[][] data = new String[dataList.size()][6];
        for (int i = 0; i < dataList.size(); i++) {
            data[i] = dataList.get(i);
        }
        return data;
    }
}