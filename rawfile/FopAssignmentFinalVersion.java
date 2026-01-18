package fop.assignment.pkgfinal.version;

import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FopAssignmentFinalVersion {
    // --- DATA LOAD STATE (Global Lists) ---

    public static List<Employee> employeeList;
    public static List<WatchModel> stockList;
    public static List<SaleRecord> salesList;
    public static List<AttendanceLog> attendanceList;
    public static Employee currentUser = null; // Tracks logged-in user
    
    public static void main(String[] args) {
        // 1. Load Data on Startup
        loadAllData();
        Scanner sc = new Scanner(System.in);
        boolean running = true;
        System.out.println("=== GOLDEN HOUR STORE OPERATIONS MANAGEMENT SYSTEM ===");
        while (running) {
            if (currentUser == null) {
                // Not logged in
                login(sc);
            } else {
                // Logged in - Show Menu
                displayMainMenu(sc);
            }
        }
    }
    
    // --- INITIALIZATION ---
    public static void loadAllData() {
        employeeList = data.loadEmployee();
        stockList = data.loadModels();
        salesList = data.loadSales();
        attendanceList = data.loadAttendance();
    }
    
    // --- LOGIN SYSTEM ---
    public static void login(Scanner sc) {
        System.out.println("\n=== Employee Login ===");
        System.out.print("Enter User ID: ");
        String id = sc.nextLine();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        for (Employee e : employeeList) {
            if (e.getEmployeeID().equals(id) && e.getPassword().equals(pass)) {
                currentUser = e;
                System.out.println("Login Successful!");
                System.out.println("Welcome, " + e.getName() + " (" + e.getRole() + ")");
                return;
            }
        }
        System.out.println("Login Failed: Invalid User ID or Password.");
    }
    
    public static void logout() {
        System.out.println("Logging out...");
        currentUser = null;
    }
    
    // --- MAIN MENU ---
    public static void displayMainMenu(Scanner sc) {
        System.out.println("\n--- MAIN MENU [" + currentUser.getName() + "] ---");
        System.out.println("1. Attendance");
        System.out.println("2. Stock Management");
        System.out.println("3. Sales System");
        System.out.println("4. Search Information");
        System.out.println("5. Edit Information");
        System.out.println("6. Analytics & Reports");
        System.out.println("7. Send Daily Report (Email)");
        
        if (currentUser.getRole().equalsIgnoreCase("Manager")) {
            System.out.println("8. Register New Employee (Manager Only)");
        }
        
        System.out.println("0. Logout");
        System.out.print("Select option: ");

        String choice = sc.nextLine();
        switch (choice) {
            case "1": 
                handleAttendance(sc);
                break;
            case "2": 
                handleStockManagement(sc); 
                break;
            case "3": 
                handleSales(sc); 
                break;
            case "4": 
                handleSearch(sc); 
                break;
            case "5": 
                edit_information.showEditMenu(sc, stockList, salesList); 
                break; 
            case "6": 
                Analytics.showAnalyticsMenu(sc, salesList, currentUser); 
                break; 
            case "7": 
                if (currentUser.getRole().equalsIgnoreCase("Manager")) 
                    registerEmployee(sc);
                else 
                    System.out.println("Access Denied.");
                break;
            case "0": 
                logout(); 
                break;
            default: System.out.println("Invalid option.");
        }
    }
    
    // --- FEATURE: EMPLOYEE REGISTRATION (Manager Only) ---
    public static void registerEmployee(Scanner sc) {
        System.out.println("\n=== Register New Employee ===");
        System.out.print("Enter Employee Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Employee ID: ");
        String id = sc.nextLine();
        
        // Check duplicate
        for(Employee e : employeeList) {
            if(e.getEmployeeID().equals(id)) {
                System.out.println("Error: Employee ID already exists.");
                return;
            }
        }
        System.out.print("Set Password: ");
        String pass = sc.nextLine();
        System.out.print("Set Role: ");
        String role = sc.nextLine();

        employeeList.add(new Employee(id, name, pass, role));
        data.saveEmployee(employeeList);
        System.out.println();
        System.out.println("Employee successfully registered!");
    }
    
    // --- FEATURE: ATTENDANCE ---
    public static void handleAttendance(Scanner sc) {
        System.out.println("\n=== Attendance Log ===");
        System.out.println("1. Clock In");
        System.out.println("2. Clock Out");
        System.out.print("Select: ");
        String type = sc.nextLine().equals("1") ? "Clock In" : "Clock Out";

        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm"));

        attendanceList.add(new AttendanceLog(date, time, currentUser.getEmployeeID(), type));
        data.saveAttendance(attendanceList);
        
        System.out.println(type + " Successful!");
        System.out.println("Date: " + date);
        System.out.println("Time: " + time);
        
        if (type.equals("Clock Out")) {
            calculateTotalHours(date,time);
        }
    }
    
    // --- FEATURE: STOCK MANAGEMENT ---
    public static void handleStockManagement(Scanner sc) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        System.out.println("\n=== Stock Management ===");
        System.out.println("1. Stock Count");
        System.out.println("2. Stock Movement");
        System.out.print("Select: ");
        String opt = sc.nextLine();

        if (opt.equals("1")) {
            LocalTime qp = LocalTime.now();
            String session;
            if(qp.getHour()<12)
                session = "Morning";
            else
                session = "Night";
            // Stock Count Logic
            int mismatches = 0,correct = 0, total =0;
            System.out.println("===" + session +  "Stock Count ===");
            System.out.println("Date: " + date);
            System.out.println("Time: " + time);
            for (WatchModel wm : stockList) {
                System.out.println("Model: " + wm.getModelName() + " - Counted: ");
                int physical = sc.nextInt();
                System.out.print("Store Record: " + wm.getQuantity());
                total++;
                try {
                    int diff;
                    if (physical != wm.getQuantity()) {
                        if(physical > wm.getQuantity())
                            diff = physical - wm.getQuantity();
                        else
                            diff = wm.getQuantity()- physical;
                        System.out.println("! Mismatch detected (" + diff + " unit difference)");
                        mismatches++;
                    } else {
                        System.out.println("Stock tally correct.");
                        correct++;
                    }
                } catch (NumberFormatException e) { 
                    System.out.println("Invalid number."); 
                }
            }
            System.out.println("Total Models Checked: " + total);
            System.out.println("Tally Correct: " + correct);
            System.out.println("Mismatches: " + mismatches);
            System.out.println(session + "stock count completed.");
            if(mismatches!=0)
                System.out.println("Warning: Please verify stock");
        } else if (opt.equals("2")) {
            handleStockMovement(sc);
        }else{
            System.out.println("Invalid option.");
        }
    }
    
    public static void handleStockMovement(Scanner sc) {
        // 1. Transaction Type
        System.out.println("\n--- Stock Movement ---");
        System.out.println("1. Stock In (Receive)");
        System.out.println("2. Stock Out (Transfer)");
        System.out.print("Select Transaction Type: ");
        String typeChoice = sc.nextLine();
        
        String transType = "";
        String actionHeader = "";
        switch (typeChoice) {
            case "1":
                transType = "Stock In ";
                actionHeader = "Models received.";
                break;
            case "2":
                transType = "Stock Out ";
                actionHeader = "Models transfeered.";
                break;
            default:
                System.out.println("Invalid selection.");
                return;
        }
        
        LocalDateTime ab = LocalDateTime.now();
        String date = ab.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timebase = ab.format(DateTimeFormatter.ofPattern("hh:mm a")); // e.g., 02:50 p.m.
        String ampm = (ab.getHour()<12) ? "a.m." : "p.m.";
        String time = timebase + " " + ampm;
        
        System.out.print("From: ");
        String fromLoc = sc.nextLine();
        System.out.print("To: ");
        String toLoc = sc.nextLine();
        
        List<String> receiptItems = new ArrayList<>(); // To store line items for receipt
        int totalQty = 0;
        boolean addingItems = true;

        while (addingItems) {
            System.out.print("-");
            
            System.out.println("--- Enter Models (Type 'done' to finish) ---");
        while (true) {
            System.out.print("Enter Model Name: ");
            String modelName = sc.nextLine();
            if (modelName.equalsIgnoreCase("done")) 
                break;

            WatchModel model = findModel(modelName);
            if (model == null) {
                System.out.println("Error: Model not found.");
                continue;
            }

            System.out.print("Enter Quantity: ");
            try {
                int qty = Integer.parseInt(sc.nextLine());
                
                // Validation for Stock Out
                if (transType.equals("Stock Out") && model.getQuantity() < qty) {
                    System.out.println("Error: Insufficient stock.");
                    continue;
                }

                // Update Memory
                if (transType.equals("Stock In")) model.setQuantity(model.getQuantity() + qty);
                else model.setQuantity(model.getQuantity() - qty);

                // Add to list for printing
                receiptItems.add(model.getModelName() + " (Quantity: " + qty + ")");
                totalQty += qty;

            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }

        if (totalQty == 0) return; // Cancel if empty

        // 4. Save Updates
        data.saveModels(stockList);

        // 5. Print output
        System.out.println();
        System.out.println("=== " + transType + " ===");
        System.out.println("Date: " + date);
        System.out.println("Time: " + time);
        System.out.println("From: " + fromLoc);
        System.out.println("To: " + toLoc);
        System.out.println(actionHeader);
        for (String item : receiptItems) {
            System.out.println(item); // e.g., "DW2400-2 (Quantity: 2)"
        }
        System.out.println("Total Quantity: " + totalQty);
        System.out.println("Model quantities updated successfully.");
        System.out.println(transType + " recorded.");
        System.out.println("Receipt generated: receipts_" + date + ".txt");

        // 6. GENERATE TEXT RECEIPT FILE (Includes Employee Name as per reqs) [cite: 97, 104]
        try (PrintWriter pw = new PrintWriter(new FileOutputStream("receipts_" + date + ".txt", true))) {
            pw.println("=== " + transType + " ===");
            pw.println("Date: " + date);
            pw.println("Time: " + time);
            pw.println("From: " + fromLoc);
            pw.println("To: " + toLoc);
            pw.println(actionHeader);
            for (String item : receiptItems) {
                pw.println(item);
            }
            pw.println("Total Quantity: " + totalQty);
            pw.println("Employee in Charge: " + currentUser.getName()); // Requirement 
            pw.println("--------------------------------------");
        } catch (IOException e) {
            System.out.println("Error saving receipt: " + e.getMessage());
        }
    }
    }
          
    // --- FEATURE: SALES SYSTEM ---
    public static void handleSales(Scanner sc) {
        System.out.println("\n=== Record New Sale ===");
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timebase = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String ampm = (now.getHour()<12) ? "a.m." : "p.m.";
        String time = timebase + " " + ampm;

        System.out.println("Date: " + date);
        System.out.println("Time: " + time);
        System.out.print("Customer Name: ");
        String custName = sc.nextLine();
        System.out.println("Item(s) Purchased: ");
        List<SaleRecord> currentTransaction = new ArrayList<>(); // To store temp records before saving
        List<WatchModel> modelsToUpdate = new ArrayList<>();     // To store model refs for stock deduction
        List<Integer> quantitiesToDeduct = new ArrayList<>();    // To store qty to deduct
        boolean moreitem = true;
        double total=0;
        while (moreitem){
            System.out.print("Enter Model: ");
            String modelName = sc.nextLine();
        
            WatchModel model = findModel(modelName);
            if (model == null) {
                 System.out.println("Error: Model not found.");
                 return;
            }

            System.out.print("Enter Quantity: ");
            int qty = Integer.parseInt(sc.nextLine());
        
            if (model.getQuantity() < qty) {
                System.out.println("Error: Insufficient stock. Available: " + model.getQuantity());
                return;
            }
        
            System.out.printf("Unit Price: RM ", model.getPrice());
            double subtotal = model.getPrice() * qty;
            total += subtotal;
            
            currentTransaction.add(new SaleRecord(date, time, custName, model.getModelName(), qty, subtotal, "", currentUser.getEmployeeID()));
            modelsToUpdate.add(model);
            quantitiesToDeduct.add(qty);
            
            System.out.print("Are there more items purchased? (Y/N): ");
            String confirm = sc.next();
            if (confirm.equalsIgnoreCase("N")) {
                moreitem = false;
            }
    }
        System.out.print("Payment Method (Cash/Card/E-wallet): ");
        String method = sc.nextLine();
        System.out.printf("Subtotal: RM ", total);
        System.out.println();
        System.out.println("Transaction successful.");
        System.out.println("Sale recorded successfully.");
        System.out.println("Model quantities updated successfully.");
        String receiptFileName = "sales_" + date + ".txt";
        System.out.println("Receipt generated: " + receiptFileName);
        
        //Update payment method and add to sales list
        for (SaleRecord rec : currentTransaction) {
            rec.setPaymentMethod(method);
            salesList.add(rec);
        }
        data.saveSales(salesList); // Save to CSV
        
        //deduct stock
        for (int i = 0; i < modelsToUpdate.size(); i++) {
            WatchModel m = modelsToUpdate.get(i);
            int q = quantitiesToDeduct.get(i);
            m.setQuantity(m.getQuantity() - q);
        }
        data.saveModels(stockList); // Save to CSV
        
        //Generate receipt
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(receiptFileName, true))) {
            pw.println("=== SALES RECEIPT ===");
            pw.println("Date: " + date);
            pw.println("Time: " + time);
            pw.println("Customer: " + custName);
            pw.println("Employee: " + currentUser.getName());
            pw.println("--------------------------------------");
            pw.printf("%-20s | %-5s | %-10s\n", "Model", "Qty", "Price");
            for (SaleRecord rec : currentTransaction) {
                pw.printf("%-20s | %-5d | RM%.2f\n", rec.getModelName(), rec.quantity, rec.totalPrice);
            }
            pw.println("--------------------------------------");
            pw.printf("Total: RM%.2f\n", total);
            pw.println("Method: " + method);
            pw.println("======================================\n");
        } catch (IOException e) {
            System.out.println("Error saving receipt: " + e.getMessage());
        }
    }
    
    // --- FEATURE: SEARCH INFORMATION ---
    public static void handleSearch(Scanner sc) {
        System.out.println("\n=== Search Information ===");
        System.out.println("1. Search Stock");
        System.out.println("2. Search Sales");
        System.out.print("Select: ");
        String opt = sc.nextLine();

        if (opt.equals("1")) 
            searchStock(sc);
        else if (opt.equals("2"))
            searchSales(sc);
        else
            System.out.println("Invalid option.");
    }
    
    private static void searchStock(Scanner sc) {
        System.out.println("\n=== Search Stock Information ===");
        System.out.print("Search Model Name: ");
        String searchName = sc.nextLine().trim();
        System.out.println("Searching...");

        // A. Load Outlet Names (C60 -> Kuala Lumpur City Centre)
        Map<String, String> outletMap = loadOutletMap();

        // B. Search in model.csv
        File file = new File("model.csv");
        if (!file.exists()) {
            System.out.println("Error: model.csv database not found.");
            return;
        }

        try (Scanner fsc = new Scanner(new FileInputStream(file))) {
            // Read Header to map columns to Outlet Codes
            if (!fsc.hasNextLine()) 
                return;
            String headerLine = fsc.nextLine();
            String[] headers = headerLine.split(",");
            // headers[0]=Model, headers[1]=Price, headers[2...]=OutletCodes(C60,C61...)

            boolean found = false;
            while (fsc.hasNextLine()) {
                String line = fsc.nextLine();
                String[] cols = line.split(",");

                // Check if Model Name matches
                if (cols[0].equalsIgnoreCase(searchName)) {
                    found = true;
                    System.out.println("Model: " + cols[0]);
                    
                    // Format Price (Remove decimals if .00)
                    double price = Double.parseDouble(cols[1]);
                    System.out.printf("Unit Price: RM", price);

                    System.out.println("Stock by Outlet:");
                    
                    // Iterate through outlet columns (Index 2 onwards)
                    int printCount = 0;
                    for (int i = 2; i < cols.length; i++) {
                        int qty = Integer.parseInt(cols[i]);
                        if (qty > 0) {
                            String outletCode = headers[i].trim();
                            // Use map to get name, fallback to code if not found
                            // Note: Sample output uses short names like "KLCC", but csv has full names.
                            // Use the full name from outlet.csv as it is the provided data source.
                            String outletName = outletMap.getOrDefault(outletCode, outletCode);
                            
                            // Formatting to mimic the "wrapped" style in sample
                            System.out.print(outletName + ": " + qty + "   ");
                            printCount++;
                            
                            // Break line every 4 items for readability 
                            if (printCount % 4 == 0) 
                                System.out.println();
                        }
                    }
                    if (printCount % 4 != 0) 
                        System.out.println(); // Final newline
                    break;
                }
            }
            if (!found) 
                System.out.println("Model not found.");

        } catch (Exception e) {
            System.out.println("Error reading model data: " + e.getMessage());
        }
    }
    
    private static void searchSales(Scanner sc) {
        System.out.println("\n=== Search Sales Information ===");
        System.out.print("Search keyword: ");
        String keyword = sc.nextLine().toLowerCase();
        System.out.println("Searching...");

        boolean found = false;
        for (SaleRecord s : salesList) {
            // Search criteria: Date, Customer Name, or Model Name
            if (s.getDate().toLowerCase().contains(keyword) ||  s.getCustomerName().toLowerCase().contains(keyword) || s.getModelName().toLowerCase().contains(keyword)) {
                System.out.println("Sales Record Found:");
                System.out.println("Date: " + s.getDate());
                System.out.println("Time: " + s.time); // Ensure SaleRecord has 'time' field visible
                System.out.println("Quantity: " + s.quantity); // Ensure 'quantity' is visible
                System.out.println("Customer: " + s.getCustomerName());
                System.out.println("Item(s): " + s.getModelName());
                System.out.printf("Total: RM", s.getTotalPrice());
                System.out.println("Transaction Method: " + s.paymentMethod);
                System.out.println("Employee: " + s.getEmployeeID()); // Ideally map ID to Name if possible
                System.out.println("Status: Transaction verified.");
                System.out.println("-----------------------------------");
                found = true;
            }
        }
        if (!found) 
            System.out.println("No records found matching '" + keyword + "'.");
    }
    
    // Helper
    private static WatchModel findModel(String name) {
        for (WatchModel m : stockList) {
            if (m.getModelName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }
    
    private static Map<String, String> loadOutletMap() {
        Map<String, String> map = new HashMap<>();
        File file = new File("outlet.csv");
        if (file.exists()) {
            try (Scanner sc = new Scanner(new FileInputStream(file))) {
                if (sc.hasNextLine()) sc.nextLine(); // Skip header
                while (sc.hasNextLine()) {
                    String[] parts = sc.nextLine().split(",");
                    if (parts.length >= 2) {
                        map.put(parts[0].trim(), parts[1].trim());
                    }
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not load outlet details.");
            }
        }
        // Manual override for short names if you strictly want "KLCC" instead of "Kuala Lumpur City Centre"
        // map.put("C60", "KLCC"); 
        return map;
    }
    
    private static void calculateTotalHours(String today,String clockOutTime){
        String clockInTime = null;
        
        for(int i = attendanceList.size()-1; i>=0;i--){
            AttendanceLog log = attendanceList.get(i);
            if(log.employeeID.equals(currentUser.getEmployeeID())&&log.date.equals(today)&&log.action.equalsIgnoreCase("Clock In")){
                clockInTime = log.time;
                break;
            }
        }
        
        if(clockInTime != null){
            try{
                DateTimeFormatter parser = DateTimeFormatter.ofPattern("hh:mm a");
                LocalTime tIn = LocalTime.parse(clockInTime,parser);
                LocalTime tOut = LocalTime.parse(clockOutTime,parser);
                long minutes = java.time.Duration.between(tIn,tOut).toMinutes();
                
                if(minutes>0){
                    double hours = minutes/60.0;
                    System.out.printf("Total Hours Worked: %.1f hours\n" , hours);
                }else{
                    System.out.println("Total Hours Worked: 0.0 hours");
                }
            }catch(Exception e){
                System.out.println("Error: can't calculate total working hours (" + e.getMessage() + ")");
            }
        }else
            System.out.println("Warning: No matching 'Clock In' record found for today.");    
            }
}
