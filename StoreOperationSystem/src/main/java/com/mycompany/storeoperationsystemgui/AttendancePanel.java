package com.mycompany.storeoperationsystemgui;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class AttendancePanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    
    // Input Fields
    private JTextField idField;
    private JPasswordField passField;
    private JComboBox<String> outletBox;

    public AttendancePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        //1. TOP PANEL: User Inputs
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Clock In / Out"));
        inputPanel.setPreferredSize(new Dimension(800, 150));

        inputPanel.add(new JLabel("User ID:"));
        idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Password:"));
        passField = new JPasswordField();
        inputPanel.add(passField);

        inputPanel.add(new JLabel("Outlet Area:"));
        outletBox = new JComboBox<>();
        loadOutletsFromCSV();
        inputPanel.add(outletBox);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnIn = new JButton("CLOCK IN");
        JButton btnOut = new JButton("CLOCK OUT");
        JButton btnRefresh = new JButton("Refresh Log");
        
        // NEW BUTTON: Employee Performance
        JButton btnPerformance = new JButton("Employee Performance (Manager)");
        
        // Style buttons
        btnIn.setBackground(new Color(46, 204, 113)); // Green
        btnOut.setBackground(new Color(231, 76, 60)); // Red
        btnPerformance.setBackground(new Color(52, 152, 219)); // Blue
        btnPerformance.setForeground(Color.WHITE);
        
        btnPanel.add(btnIn);
        btnPanel.add(btnOut);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnPerformance); // Add to panel

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(inputPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.SOUTH);
        
        add(topContainer, BorderLayout.NORTH);

        //2. CENTER PANEL: Table
        String[] columns = {"User ID", "Outlet", "Clock In", "Clock Out", "Total Hours", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        
        add(scrollPane, BorderLayout.CENTER);

        //3. LOGIC
        refreshTable();

        // Clock In
        btnIn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pass = new String(passField.getPassword());
            String outlet = (String) outletBox.getSelectedItem();

            if (validateUser(id, pass)) {
                String msg = AttendanceManager.clockIn(id, outlet);
                JOptionPane.showMessageDialog(this, msg);
                refreshTable();
                clearInputs();
            }
        });

        // Clock Out
        btnOut.addActionListener(e -> {
            String id = idField.getText().trim();
            String pass = new String(passField.getPassword());
            
            if (validateUser(id, pass)) {
                String msg = AttendanceManager.clockOut(id);
                JOptionPane.showMessageDialog(this, msg);
                refreshTable();
                clearInputs();
            }
        });

        btnRefresh.addActionListener(e -> refreshTable());

        //Performance Report Action
        btnPerformance.addActionListener(e -> {
            if (performManagerLogin()) {
                generatePerformanceReport();
            }
        });
    }

    // --- HELPER METHODS ---

    private void loadOutletsFromCSV() {
        File file = new File("outlet.csv");
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                if (scanner.hasNextLine()) scanner.nextLine(); 
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String outletName = parts[1].trim();
                        String area = (parts.length >= 3) ? parts[2].trim() : "";
                        if (!area.isEmpty()) outletBox.addItem(outletName + " (" + area + ")");
                        else outletBox.addItem(outletName);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            outletBox.addItem("Error: outlet.csv not found");
        }
    }

    private boolean validateUser(String id, String pass) {
        if (id.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter User ID and Password.");
            return false;
        }
        if (UserDatabase.checkLogin(id, pass)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Invalid User ID or Password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String[][] data = AttendanceManager.getAttendanceData();
        for (String[] row : data) {
            tableModel.addRow(row);
        }
    }

    private void clearInputs() {
        idField.setText("");
        passField.setText("");
    }

    //NEW FEATURE: MANAGER LOGIN CHECK
    private boolean performManagerLogin() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField idIn = new JTextField();
        JPasswordField passIn = new JPasswordField();
        panel.add(new JLabel("Manager ID:"));
        panel.add(idIn);
        panel.add(new JLabel("Password:"));
        panel.add(passIn);

        int result = JOptionPane.showConfirmDialog(this, panel, "Restricted Access", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            if (UserDatabase.isManager(idIn.getText(), new String(passIn.getPassword()))) {
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Access Denied: Manager Only.", "Security Alert", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    //NEW FEATURE: PERFORMANCE REPORT LOGIC
    private void generatePerformanceReport() {
        File salesFile = new File("sales_data.csv");
        if (!salesFile.exists()) {
            JOptionPane.showMessageDialog(this, "No sales data found.");
            return;
        }

        //1. Data Structure to hold stats
        class EmployeeStats {
            String name;
            double totalSales = 0;
            int transactions = 0;
            
            EmployeeStats(String name) { this.name = name; }
        }

        Map<String, EmployeeStats> statsMap = new HashMap<>();

        //2. Read CSV and Aggregate Data
        try (BufferedReader br = new BufferedReader(new FileReader(salesFile))) {
            String line;
            br.readLine(); // Skip Header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                // CSV Structure: Date(0), Time(1), Outlet(2), SalesPerson(3)... Total(9)
                if (parts.length >= 10) {
                    String empId = parts[3].trim();
                    if(empId.isEmpty()) continue;

                    double amount = 0;
                    try { amount = Double.parseDouble(parts[9]); } catch(Exception e) {}

                    statsMap.putIfAbsent(empId, new EmployeeStats(empId));
                    EmployeeStats stats = statsMap.get(empId);
                    stats.totalSales += amount;
                    stats.transactions++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. Sort by Performance (Highest Sales First)
        List<EmployeeStats> sortedList = new ArrayList<>(statsMap.values());
        sortedList.sort((e1, e2) -> Double.compare(e2.totalSales, e1.totalSales));

        // 4. Display in JDialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Employee Performance Report", true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        String[] headers = {"Rank", "Employee ID", "Transactions", "Total Sales (RM)"};
        DefaultTableModel model = new DefaultTableModel(headers, 0);

        int rank = 1;
        for (EmployeeStats s : sortedList) {
            model.addRow(new Object[]{
                rank++, 
                s.name, 
                s.transactions, 
                String.format("%.2f", s.totalSales)
            });
        }

        JTable reportTable = new JTable(model);
        reportTable.setRowHeight(25);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        reportTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        dialog.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        
        JLabel summaryLabel = new JLabel("  Total Active Employees: " + sortedList.size() + "  ");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        dialog.add(summaryLabel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

}
