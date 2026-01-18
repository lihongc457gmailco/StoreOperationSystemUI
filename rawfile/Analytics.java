
package fop.assignment.pkgfinal.version;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class Analytics {
        public static void runAnalytics(Scanner sc, List<SaleRecord> globalSalesList) {
        System.out.println("\n--- ANALYTICS MODULE ---");
        // 1. INPUTS
        System.out.print("Enter start date(YYYY-MM-DD): ");
        String startdate = sc.nextLine();
        System.out.print("Enter end date(YYYY-MM-DD): ");
        String enddate = sc.nextLine();
        System.out.print("Sort by (date/amount/name): ");
        String sort = sc.nextLine();
        System.out.print("Sort by (ascending/descending): ");
        String arrangement = sc.nextLine();
    
        // 2. FILTERING LOGIC
        ArrayList<SaleRecord> filteredSales = new ArrayList<>();
        double totalCumulative = 0.0;
        
        // Iterate through the main sales list
        for (SaleRecord sale : globalSalesList) {
            String date = sale.getDate();
            // String comparison for dates (YYYY-MM-DD works alphabetically)
            if (date.compareTo(startdate) >= 0 && date.compareTo(enddate) <= 0) {
                filteredSales.add(sale);
                totalCumulative += sale.getTotalPrice();
            }
        }
        
        // 3. SORTING LOGIC (Bubble Sort implementation from uploaded file)
        int n = filteredSales.size();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n - 1; ++j) {
                SaleRecord current = filteredSales.get(j);
                SaleRecord next = filteredSales.get(j + 1);
                
                boolean swap = false;
                int res;

                if (sort.equalsIgnoreCase("date")) {
                    res = current.getDate().compareTo(next.getDate());
                    swap = arrangement.equalsIgnoreCase("ascending") ? res > 0 : res < 0;
                } else if (sort.equalsIgnoreCase("amount")) {
                    double a1 = current.getTotalPrice();
                    double a2 = next.getTotalPrice();
                    swap = arrangement.equalsIgnoreCase("ascending") ? a1 > a2 : a1 < a2;
                } else if (sort.equalsIgnoreCase("name")) {
                    res = current.getCustomerName().compareTo(next.getCustomerName());
                    swap = arrangement.equalsIgnoreCase("ascending") ? res > 0 : res < 0;
                }

                if (swap) {
                    filteredSales.set(j, next);
                    filteredSales.set(j + 1, current);
                }
            }
        }
        
        // 4. DISPLAY FILTERED SALES
        System.out.println("\n--- Filtered Sales Records ---");
        System.out.printf("%-12s | %-15s | %-15s | %-8s\n", "Date", "Customer", "Employee", "Amount");
        for (SaleRecord row : filteredSales) {
            System.out.printf("%-12s | %-15s | %-15s | RM%-8s\n", 
                row.getDate(), row.getCustomerName(), row.getEmployeeID(), String.format("%.2f", row.getTotalPrice()));
        }
        System.out.printf("Total Cumulative Sales: RM%.2f\n", totalCumulative);
        
        // 5. EMPLOYEE PERFORMANCE CHECK
        System.out.print("\nEnter your login role (Manager/Staff): ");
        String role = sc.nextLine();
        
        if (!role.equalsIgnoreCase("Manager")) {
            System.out.println("Access Denied: Manager privilege required for performance data.");
        } else {
            System.out.print("Enter password: ");
            String password = sc.nextLine();
            
            // Hardcoded password check from the uploaded file
            if (password.equals("a2b1c0")) {
                System.out.println("\n--- Confidential Employee Performance Report ---");
                displayPerformanceMetrics(filteredSales);
            } else {
                System.out.println("Access Denied: Password mismatch.");
            }
        }
    }

    // Logic from the uploaded file's displayPerformanceMetrics method
    public static void displayPerformanceMetrics(ArrayList<SaleRecord> allSales) {
        ArrayList<EmployeePerformance> performanceList = new ArrayList<>();
        ArrayList<String> processedIDs = new ArrayList<>();
        
        Iterator<SaleRecord> var3 = allSales.iterator();

        // Aggregate Data
        while (true) {
            String empID;
            do {
                if (!var3.hasNext()) {
                    // Sorting Performance List (Bubble Sort)
                    int n = performanceList.size();
                    for (int i = 0; i < n - 1; ++i) {
                        for (int j = 0; j < n - 1 - i; ++j) {
                            EmployeePerformance current = performanceList.get(j);
                            EmployeePerformance next = performanceList.get(j + 1);
                            // Sort Descending by Total Sales
                            if (current.totalSales < next.totalSales) {
                                performanceList.set(j, next);
                                performanceList.set(j + 1, current);
                            }
                        }
                    }

                    // Display Result
                    System.out.printf("%-15s | %-15s | %-12s\n", "Employee", "Total Sales (RM)", "Transactions");
                    System.out.println("------------------------------------------------");
                    for (EmployeePerformance employee : performanceList) {
                        System.out.printf("%-15s | RM%-13.2f | %-12d\n", 
                            employee.employeeName, employee.totalSales, employee.transactionsCount);
                    }
                    return;
                }

                SaleRecord record = var3.next();
                empID = record.getEmployeeID();
            } while (processedIDs.contains(empID));

            double totalSales = 0.0;
            int transactionsCount = 0;
            
            for (SaleRecord checkRecord : allSales) {
                if (checkRecord.getEmployeeID().equals(empID)) {
                    totalSales += checkRecord.getTotalPrice();
                    ++transactionsCount;
                }
            }

            performanceList.add(new EmployeePerformance(empID, totalSales, transactionsCount));
            processedIDs.add(empID);
        }
    }

    static void showAnalyticsMenu(Scanner sc, List<SaleRecord> salesList,Employee currentUser) {
    System.out.println("\n--- ANALYTICS MENU ---");
    System.out.println("1. Filter and Sort Sales History");
    System.out.println("2. Employee Performance Metrics (Manager Only)");
    System.out.println("3. Back to Main Menu");
    System.out.print("Select: ");
    
    String choice = sc.nextLine();

    switch (choice) {
        case "1":
            // Call the logic to filter and sort (implemented in your Analytics class)
            fop.assignment.pkgfinal.version.Analytics.runAnalytics(sc, salesList);
            break;
            
        case "2":
            // Check for Manager role before showing performance
            if (currentUser.getRole().equalsIgnoreCase("Manager")) {
                // Pass the list to the performance method
                fop.assignment.pkgfinal.version.Analytics.displayPerformanceMetrics(new ArrayList<>());
            } else {
                System.out.println("Access Denied: Manager privilege required.");
            }
            break;
            
        case "3":
            System.out.println("Returning to Main Menu...");
            break;
            
        default:
            System.out.println("Invalid option.");
            break;
    }
}
    // Inner class as defined in the uploaded file
    private static class EmployeePerformance {
        String employeeName;
        double totalSales;
        int transactionsCount;

        public EmployeePerformance(String name, double sales, int count) {
            this.employeeName = name;
            this.totalSales = sales;
            this.transactionsCount = count;
        }
    }
    }

