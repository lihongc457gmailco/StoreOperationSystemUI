package fop.assignment.pkgfinal.version;

import java.util.List;
import java.util.Scanner;
public class edit_information {
    
    public static void showEditMenu(Scanner sc, List<WatchModel> stockList, List<SaleRecord> salesList) {
        System.out.println("\n--- EDIT INFORMATION MENU ---");
        System.out.println("1. Edit Stock Quantity/Price");
        System.out.println("2. Edit Sales Transaction");
        System.out.print("Select: ");
        String choice = sc.nextLine();

        if (choice.equals("1")) {
            editStock(sc, stockList);
        } else if (choice.equals("2")) {
            editSalesInformation(sc, salesList);
        } else {
            System.out.println("Invalid option.");
        }
    }
    
    public static void editStock(Scanner sc, List<WatchModel> stockList) {
        System.out.print("Enter Model Name: ");
        String name = sc.nextLine();

        for (WatchModel m : stockList) {
            if (m.getModelName().equalsIgnoreCase(name)) {
                System.out.println("Current Stock: " + m);
                System.out.print("Enter New Stock Value: ");
                int newQty = Integer.parseInt(sc.nextLine());
                m.setQuantity(newQty);
                System.out.println("Stock information updated successfully.");
                data.saveModels(stockList);
                return;
            }
        }
        System.out.println("Model not found.");
    }
    
    public static void editSalesInformation(Scanner sc, List<SaleRecord> salesList) {
        System.out.print("Enter Transcation Date: ");
        String date = sc.nextLine().trim();
        System.out.print("Enter Customer Name: ");
        String name = sc.nextLine().trim();

        SaleRecord foundRecord = null;
        for (SaleRecord sale : salesList) {
            if (sale.getDate().equalsIgnoreCase(date) && sale.getCustomerName().equalsIgnoreCase(name)) {
                foundRecord = sale;
                break;
            }
        }

        if (foundRecord != null) {
            System.out.println("Sales Record Found:");
            
            // Custom display format 
            System.out.println("Model: " + foundRecord.getModelName() + "   Quantity: " + foundRecord.getQuantity());
            System.out.println("Total: RM" + foundRecord.getTotalPrice());
            System.out.println("Transaction Method: " + foundRecord.getPaymentMethod());
            System.out.println();

            System.out.println("Select number to edit:");
            System.out.println("1. Name    2. Model    3. Quantity    4. Total"); 
            System.out.println("5. Transaction Method"); 
            System.out.print("> ");
            int choice = sc.nextInt();
            System.out.println();

            // Variables to hold potential new values
            String newValue = "";
            double newDouble = 0;
            int newInt = 0;

            //HANDLING USER CHOICE
            switch (choice) {
                case 1:
                    System.out.print("Enter New Customer Name: ");
                    newValue = sc.nextLine();
                    break;
                case 2:
                    System.out.print("Enter New Model: ");
                    newValue = sc.nextLine();
                    break;
                case 3:
                    System.out.print("Enter New Quantity: ");
                    newInt = sc.nextInt();
                    sc.nextLine();
                    break;
                case 4:
                    System.out.print("Enter New Total: RM");
                    newDouble = sc.nextDouble();
                    sc.nextLine();
                    break;
                case 5:
                    System.out.print("Enter New Transaction Method: ");
                    newValue = sc.nextLine();
                    break;
                default:
                    System.out.println("Invalid option.");
                    return;
            }

            // 5. CONFIRMATION
            System.out.print("Confirm Update? (Y/N): ");
            String confirm = sc.nextLine();

            if (confirm.equalsIgnoreCase("Y")) {
                switch (choice) {
                    case 1: 
                        foundRecord.setCustomerName(newValue); 
                        break;
                    case 2: 
                        foundRecord.setModelName(newValue); 
                        break;
                    case 3: 
                        foundRecord.setQuantity(newInt); 
                        break;
                    case 4: 
                        foundRecord.setTotalPrice(newDouble); 
                        break;
                    case 5: 
                        foundRecord.setPaymentMethod(newValue); 
                        break;
                }

                // 6. STORAGE SYSTEM SAVE
                data.saveSales(salesList);
                
                System.out.println();
                System.out.println("Sales information updated successfully.");
            } else {
                System.out.println("Update cancelled.");
            }

        } else {
            System.out.println("Sales Record Not Found.");
        }
    }
}
