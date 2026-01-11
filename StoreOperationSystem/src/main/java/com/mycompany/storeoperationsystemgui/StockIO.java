package com.mycompany.storeoperationsystemgui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StockIO {
    
    private static final String FILE_NAME = "model.csv";

    // 1. Read all data from CSV
    public static List<String[]> getAllStock() {
        List<String[]> data = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) return data;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                // Skip empty lines
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", -1);
                data.add(parts);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    // 2. Save all data to CSV
    public static boolean saveAllStock(String[] headers, List<String[]> dataRows) {
        try (PrintWriter pw = new PrintWriter(new File(FILE_NAME))) {
            // Write Header
            pw.println(String.join(",", headers));
            
            // Write Data
            for (String[] row : dataRows) {
                pw.println(String.join(",", row));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Deduct Stock (Sales / Stock Out)
    public static boolean deductStock(String modelName, String outletColumn, int qtyToDeduct) {
        List<String[]> allData = getAllStock();
        if (allData.isEmpty()) return false;
        
        String[] headers = allData.get(0);
        int outletColIndex = findOutletIndex(headers, outletColumn);
        if (outletColIndex == -1) return false;

        boolean found = false;
        for (int i = 1; i < allData.size(); i++) {
            String[] row = allData.get(i);
            if (row.length > 0 && row[0].equalsIgnoreCase(modelName)) {
                try {
                    // Ensure row has enough columns
                    if (row.length <= outletColIndex) return false;
                    
                    int currentStock = Integer.parseInt(row[outletColIndex]);
                    if (currentStock >= qtyToDeduct) {
                        row[outletColIndex] = String.valueOf(currentStock - qtyToDeduct);
                        found = true;
                    } else {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
                break;
            }
        }

        if (found) {
            return saveAllStock(headers, allData.subList(1, allData.size()));
        }
        return false;
    }

    // 4. Add Stock (Stock In)
    public static boolean addStock(String modelName, String outletColumn, int qtyToAdd) {
        List<String[]> allData = getAllStock();
        if (allData.isEmpty()) return false;

        String[] headers = allData.get(0);
        int outletColIndex = findOutletIndex(headers, outletColumn);
        if (outletColIndex == -1) return false;

        boolean found = false;
        for (int i = 1; i < allData.size(); i++) {
            String[] row = allData.get(i);
            if (row.length > 0 && row[0].equalsIgnoreCase(modelName)) {
                try {
                    if (row.length <= outletColIndex) return false;

                    int currentStock = Integer.parseInt(row[outletColIndex]);
                    row[outletColIndex] = String.valueOf(currentStock + qtyToAdd);
                    found = true;
                } catch (NumberFormatException e) {
                    return false;
                }
                break;
            }
        }

        if (found) {
            return saveAllStock(headers, allData.subList(1, allData.size()));
        }
        return false;
    }

    // Helper to find column index
    private static int findOutletIndex(String[] headers, String outlet) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase(outlet)) return i;
        }
        return -1;
    }
}