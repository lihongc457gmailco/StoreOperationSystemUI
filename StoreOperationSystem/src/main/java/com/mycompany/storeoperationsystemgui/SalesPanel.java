package com.mycompany.storeoperationsystemgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class SalesPanel extends JPanel {

    // Components
    private JComboBox<String> outletBox;
    private JComboBox<String> productBox;
    private JSpinner qtySpinner;
    private JTextField customerField;
    private JComboBox<String> paymentBox;
    private JLabel totalLabel;
    private JLabel salesPersonLabel;
    
    private JTable cartTable;
    private DefaultTableModel cartModel;
    
    private double grandTotal = 0.0;
    private List<String[]> stockData; 
    private static final String MASTER_SALES_FILE = "sales_data.csv";
    
    // Formatter
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Scheduler for 10 PM Email
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SalesPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        //1. TOP PANEL
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Sales Settings"));
        
        topPanel.add(new JLabel("Select Outlet:"));
        String[] outlets = {"C60", "C61", "C62", "C63", "C64", "C65", "C66", "C67", "C68", "C69"};
        outletBox = new JComboBox<>(outlets);
        topPanel.add(outletBox);
        
        // Manual Trigger Button
        JButton btnManualReport = new JButton("Manual Report");
        btnManualReport.setBackground(Color.GRAY);
        btnManualReport.setForeground(Color.WHITE);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(btnManualReport);
        
        add(topPanel, BorderLayout.NORTH);

        //2. LEFT PANEL
        JPanel leftPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        leftPanel.add(new JLabel("Product Model:"));
        productBox = new JComboBox<>();
        loadProducts(); 
        leftPanel.add(productBox);

        leftPanel.add(new JLabel("Quantity:"));
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        leftPanel.add(qtySpinner);

        JButton btnAdd = new JButton("Add to Cart");
        btnAdd.setBackground(new Color(52, 152, 219));
        btnAdd.setForeground(Color.WHITE);
        leftPanel.add(btnAdd);

        JButton btnRemove = new JButton("Remove Item");
        btnRemove.setBackground(new Color(231, 76, 60));
        btnRemove.setForeground(Color.WHITE);
        leftPanel.add(btnRemove);

        add(leftPanel, BorderLayout.WEST);

        //3. CENTER PANEL
        String[] headers = {"Model", "Price", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(headers, 0);
        cartTable = new JTable(cartModel);
        add(new JScrollPane(cartTable), BorderLayout.CENTER);

        //4. BOTTOM PANEL
        JPanel bottomPanel = new JPanel(new GridLayout(3, 2, 10, 10)); 
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Checkout Details"));
        bottomPanel.setPreferredSize(new Dimension(0, 150));

        JPanel custPanel = new JPanel(new BorderLayout());
        custPanel.add(new JLabel("Customer Name: "), BorderLayout.WEST);
        customerField = new JTextField();
        custPanel.add(customerField, BorderLayout.CENTER);
        bottomPanel.add(custPanel);

        JPanel payPanel = new JPanel(new BorderLayout());
        payPanel.add(new JLabel("Payment Method: "), BorderLayout.WEST);
        String[] methods = {"Cash", "Credit Card", "Debit Card", "E-Wallet"};
        paymentBox = new JComboBox<>(methods);
        payPanel.add(paymentBox, BorderLayout.CENTER);
        bottomPanel.add(payPanel);

        salesPersonLabel = new JLabel("Sales Person: " + Session.getCurrentUser());
        salesPersonLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        salesPersonLabel.setForeground(Color.BLUE);
        bottomPanel.add(salesPersonLabel);

        totalLabel = new JLabel("Total: RM 0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(46, 204, 113));
        bottomPanel.add(totalLabel);

        JButton btnPay = new JButton("CONFIRM SALE");
        btnPay.setBackground(new Color(46, 204, 113));
        btnPay.setForeground(Color.WHITE);
        btnPay.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(new JLabel("")); 
        bottomPanel.add(btnPay);

        add(bottomPanel, BorderLayout.SOUTH);

        //ACTIONS
        btnAdd.addActionListener(e -> {
            String selectedModel = (String) productBox.getSelectedItem();
            int qty = (Integer) qtySpinner.getValue();
            double price = getPriceForModel(selectedModel);
            if (price > 0) {
                cartModel.addRow(new Object[]{selectedModel, price, qty, price * qty});
                updateTotal();
            }
        });

        btnRemove.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row != -1) {
                cartModel.removeRow(row);
                updateTotal();
            }
        });

        btnPay.addActionListener(e -> processTransaction());

        btnManualReport.addActionListener(e -> {
            File r = generateReportInternal();
            if(r != null) JOptionPane.showMessageDialog(this, "Report Generated: " + r.getName());
        });
        
        initSalesCSV();
        
        startAutoEmailScheduler();
    }

    private void startAutoEmailScheduler() {
        // 1. Calculate time until 10 PM
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(22).withMinute(0).withSecond(0);
        
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).getSeconds();
        
        System.out.println("Auto-Email Scheduler Started.");
        System.out.println("Next run in: " + initialDelay + " seconds (at 10:00 PM)");

        // 2. Schedule the task
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("10:00 PM Reached. Generating Report...");
                
                // A. Generate the text file
                File reportFile = generateReportInternal();
                
                // B. Calculate total sales for body text
                double todayTotal = getTodayTotalRevenue();
                String dateStr = LocalDate.now().format(DATE_FMT);

                // C. Send Email
                if (reportFile != null && reportFile.exists()) {
                    EmailService.sendDailyReport(dateStr, todayTotal, reportFile);
                } else {
                    System.out.println("Error: Report file generation failed.");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS); // Repeats every 24 hours
    }

    // Calculates Today's Total Revenue for the Email Body
    private double getTodayTotalRevenue() {
        String todayDate = LocalDate.now().format(DATE_FMT);
        double total = 0.0;
        try (BufferedReader br = new BufferedReader(new FileReader(MASTER_SALES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 10 && parts[0].equals(todayDate)) {
                    try { total += Double.parseDouble(parts[9]); } catch (Exception e) {}
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return total;
    }

    // Generates the File and returns the File object
    private File generateReportInternal() {
        String todayDate = LocalDate.now().format(DATE_FMT);
        String filenameDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String reportFileName = "DailyReport_" + filenameDate + ".txt";
        File reportFile = new File(reportFileName);
        
        List<String[]> todayRows = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(MASTER_SALES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 10 && parts[0].trim().equals(todayDate)) {
                    todayRows.add(parts);
                }
            }
        } catch (IOException e) { return null; }

        if (todayRows.isEmpty()) return null; // No sales

        try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
            String currentTxnKey = "";
            List<String[]> currentTxnItems = new ArrayList<>();

            for (String[] row : todayRows) {
                String rowKey = row[1] + row[4] + row[3]; 
                if (!rowKey.equals(currentTxnKey) && !currentTxnItems.isEmpty()) {
                    printReceiptBlock(writer, currentTxnItems);
                    currentTxnItems.clear();
                }
                currentTxnKey = rowKey;
                currentTxnItems.add(row);
            }
            if (!currentTxnItems.isEmpty()) {
                printReceiptBlock(writer, currentTxnItems);
            }
            
            return reportFile; // Return the file so EmailService can attach it

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void initSalesCSV() {
        File file = new File(MASTER_SALES_FILE);
        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(file)) {
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void loadProducts() {
        stockData = StockIO.getAllStock(); 
        if (!stockData.isEmpty()) stockData.remove(0); 
        productBox.removeAllItems();
        for (String[] row : stockData) productBox.addItem(row[0]); 
    }

    private double getPriceForModel(String model) {
        for (String[] row : stockData) {
            if (row[0].equals(model)) {
                try { return Double.parseDouble(row[1]); } catch (Exception e) {}
            }
        }
        return 0.0;
    }

    private void updateTotal() {
        grandTotal = 0.0;
        for (int i = 0; i < cartModel.getRowCount(); i++) grandTotal += (Double) cartModel.getValueAt(i, 3);
        totalLabel.setText("Total: RM " + String.format("%.2f", grandTotal));
    }

    private void processTransaction() {
        String customer = customerField.getText().trim();
        String outlet = (String) outletBox.getSelectedItem();
        
        if (customer.isEmpty() || cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter customer details.");
            return;
        }

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String model = (String) cartModel.getValueAt(i, 0);
            int qty = (Integer) cartModel.getValueAt(i, 2);
            if (!StockIO.deductStock(model, outlet, qty)) {
                JOptionPane.showMessageDialog(this, "Insufficient stock: " + model);
                return; 
            }
        }

        saveTransactionToCSV(customer, outlet);

        String receiptText = generateReceiptString(customer, outlet);
        JTextArea area = new JTextArea(receiptText);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Transaction Receipt", JOptionPane.INFORMATION_MESSAGE);
        
        cartModel.setRowCount(0);
        customerField.setText("");
        updateTotal();
    }

    private void saveTransactionToCSV(String customer, String outlet) {
        String dateStr = LocalDateTime.now().format(DATE_FMT);
        String timeStr = LocalDateTime.now().format(TIME_FMT);
        String salesPerson = Session.getCurrentUser();
        String payment = (String) paymentBox.getSelectedItem();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MASTER_SALES_FILE, true))) {
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                String model = (String) cartModel.getValueAt(i, 0);
                int qty = (Integer) cartModel.getValueAt(i, 2);
                double price = (Double) cartModel.getValueAt(i, 1);
                double total = (Double) cartModel.getValueAt(i, 3);
                
                String line = String.format("%s,%s,%s,%s,%s,%s,%s,%d,%.0f,%.0f",
                        dateStr, timeStr, outlet, salesPerson, customer, payment, model, qty, price, total);
                
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String generateReceiptString(String customer, String outlet) {
        StringBuilder sb = new StringBuilder();
        String dateStr = LocalDateTime.now().format(DATE_FMT);
        String timeStr = LocalDateTime.now().format(TIME_FMT);
        String salesPerson = Session.getCurrentUser();
        
        sb.append("=========================================\n");
        sb.append("           SALE RECEIPT                  \n");
        sb.append("=========================================\n");
        sb.append("Date:       ").append(dateStr).append(" ").append(timeStr).append("\n");
        sb.append("Outlet:     ").append(outlet).append("\n");
        sb.append("SalesPerson:").append(salesPerson).append("\n");
        sb.append("Customer:   ").append(customer).append("\n");
        sb.append("Payment:    ").append(paymentBox.getSelectedItem()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-15s %-5s %-10s %-10s\n", "Item", "Qty", "Price", "Total"));
        sb.append("-----------------------------------------\n");
        
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String model = (String) cartModel.getValueAt(i, 0);
            int qty = (Integer) cartModel.getValueAt(i, 2);
            double price = (Double) cartModel.getValueAt(i, 1);
            double total = (Double) cartModel.getValueAt(i, 3);
            sb.append(String.format("%-15s %-5d %-10.2f %-10.2f\n", model, qty, price, total));
        }
        
        sb.append("-----------------------------------------\n");
        sb.append("GRAND TOTAL: RM ").append(String.format("%.2f", grandTotal)).append("\n");
        sb.append("=========================================\n");
        return sb.toString();
    }

    private void printReceiptBlock(PrintWriter writer, List<String[]> items) {
        if (items.isEmpty()) return;
        
        String[] first = items.get(0);
        String date = first[0];
        String time = first[1];
        String outlet = first[2];
        String emp = first[3];
        String cust = first[4];
        String pay = first[5];

        writer.println("=========================================");
        writer.println("           SALE RECORD                   ");
        writer.println("=========================================");
        writer.println("Date: " + date + " " + time);
        writer.println("Outlet: " + outlet);
        writer.println("Employee: " + emp);
        writer.println("Customer: " + cust);
        writer.println("Payment: " + pay);
        writer.println("-----------------------------------------");
        writer.printf("%-15s %-5s %-10s %-10s\n", "Item", "Qty", "Price", "Subtotal");
        writer.println("-----------------------------------------");

        double total = 0;
        for (String[] item : items) {
            String model = item[6];
            String qty = item[7];
            String price = item[8];
            String sub = item[9];
            
            writer.printf("%-15s %-5s %-10s %-10s\n", model, qty, price, sub);
            try { total += Double.parseDouble(sub); } catch(Exception e){}
        }

        writer.println("-----------------------------------------");
        writer.println("GRAND TOTAL: RM " + String.format("%.2f", total));
        writer.println("=========================================");
        writer.println(); 
    }
}