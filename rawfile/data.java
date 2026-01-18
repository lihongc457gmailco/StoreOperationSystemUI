package fop.assignment.pkgfinal.version;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class data {
    public static String FILE_MODELS = "models.csv";
    public static String FILE_SALES = "sales.csv";
    public static String FILE_EMPLOYEES = "employee.csv";
    public static String FILE_ATTENDANCE = "attendance.csv";
    
    // --- LOADERS ---
    public static List<WatchModel> loadModels() {
        List<WatchModel> list = new ArrayList<>();
        File file = new File(FILE_MODELS);
        if (file.exists()) {
            try (Scanner sc = new Scanner(new FileInputStream(file))) {
                while (sc.hasNextLine()) {
                    String[] data = sc.nextLine().split(",");
                    if (data.length >= 3)
                        list.add(new WatchModel(data[0].trim(), Double.parseDouble(data[1].trim()), Integer.parseInt(data[2].trim())));
                }
            } catch (Exception e) { System.out.println("Error loading models: " + e.getMessage()); }
        }
        return list;
    }
    
    public static List<SaleRecord> loadSales() {
        List<SaleRecord> list = new ArrayList<>();
        File file = new File(FILE_SALES);
        if (file.exists()) {
            try (Scanner sc = new Scanner(new FileInputStream(file))) {
                while (sc.hasNextLine()) {
                    String[] data = sc.nextLine().split(",");
                    if (data.length >= 8)
                        list.add(new SaleRecord(data[0], data[1], data[2], data[3], Integer.parseInt(data[4]), Double.parseDouble(data[5]), data[6], data[7]));
                }
            } catch (Exception e) { System.out.println("Error loading sales: " + e.getMessage()); }
        }
        return list;
    }
    
    public static List<Employee> loadEmployee() {
        List<Employee> list = new ArrayList<>();
        File file = new File(FILE_EMPLOYEES);
        if (file.exists()) {
            try (Scanner sc = new Scanner(new FileInputStream(file))) {
                while (sc.hasNextLine()) {
                    String[] data = sc.nextLine().split(",");
                    if (data.length >= 4)
                        list.add(new Employee(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim()));
                }
            } catch (Exception e) { System.out.println("Error loading employees: " + e.getMessage()); }
        }
        return list;
    }
    
    public static List<AttendanceLog> loadAttendance() {
        List<AttendanceLog> list = new ArrayList<>();
        File file = new File(FILE_ATTENDANCE);
        if (file.exists()) {
            try (Scanner sc = new Scanner(new FileInputStream(file))) {
                while (sc.hasNextLine()) {
                    String[] data = sc.nextLine().split(",");
                    if (data.length >= 4)
                        list.add(new AttendanceLog(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim()));
                }
            } catch (Exception e) { System.out.println("Error loading attendance: " + e.getMessage()); }
        }
        return list;
    }
    
    // --- SAVERS ---
    public static void saveModels(List<WatchModel> list) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(FILE_MODELS))) {
            for (WatchModel m : list) {
                pw.println(m.toCSV()); // Converts object back to String format
            }
            System.out.println("Data successfully saved to " + FILE_MODELS);
        } catch (IOException e) { 
            System.out.println("Error saving models: " + e.getMessage()); 
        }
    }
    public static void saveSales(List<SaleRecord> list) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(FILE_SALES))) {
            for (SaleRecord s : list) {
                pw.println(s.toCSV());
            }
            System.out.println("Data successfully saved to " + FILE_SALES);
        } catch (IOException e) { 
            System.out.println("Error saving sales: " + e.getMessage()); 
        }
    }
    public static void saveEmployee(List<Employee> list) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(FILE_EMPLOYEES))) {
            for (Employee m : list) {
                pw.println(m.toCSV()); // Converts object back to String format
            }
            System.out.println("Data successfully saved to " + FILE_EMPLOYEES);
        } catch (IOException e) { 
            System.out.println("Error saving models: " + e.getMessage()); 
        }
    }
    public static void saveAttendance(List<AttendanceLog> list) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(FILE_ATTENDANCE))) {
            for (AttendanceLog m : list) {
                pw.println(m.toCSV()); // Converts object back to String format
            }
            System.out.println("Data successfully saved to " + FILE_ATTENDANCE);
        } catch (IOException e) { 
            System.out.println("Error saving models: " + e.getMessage()); 
        }
    }
}
