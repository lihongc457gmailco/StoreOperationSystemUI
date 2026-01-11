package com.mycompany.storeoperationsystemgui;

import javax.swing.*;
import java.awt.*;

public class MainUI extends JFrame {

    JPanel contentPanel;
    CardLayout cardLayout;

    public MainUI() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Store Operation System - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(200); 
        splitPane.setDividerSize(0); 
        splitPane.setEnabled(false); 
        
        //Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(64, 64, 64)); 
        sidebar.setLayout(new GridLayout(7, 1, 10, 10)); 
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel menuLabel = new JLabel("MENU", SwingConstants.CENTER);
        menuLabel.setForeground(Color.WHITE);
        menuLabel.setFont(new Font("Arial", Font.BOLD, 24));
        sidebar.add(menuLabel);

        // Create Buttons
        JButton btnAttendance = createSidebarButton("Attendance Log");
        JButton btnStock = createSidebarButton("Stock Management");
        JButton btnSales = createSidebarButton("Sales System");
        JButton btnTransaction = createSidebarButton("Transaction History");
        JButton btnLogout = createSidebarButton("Logout");
        
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);

        //ADD BUTTONS TO PANEL
        sidebar.add(btnAttendance);
        sidebar.add(btnStock);
        sidebar.add(btnSales);
        sidebar.add(btnTransaction); 
        sidebar.add(btnLogout);

        //Content Area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        contentPanel.add(new AttendancePanel(), "Attendance");
        contentPanel.add(new StockPanel(), "Stock");
        contentPanel.add(new SalesPanel(), "Sales");
        contentPanel.add(new TransactionHistoryPanel(), "Transactions");

        //ADD ACTION LISTENER 
        btnAttendance.addActionListener(e -> cardLayout.show(contentPanel, "Attendance"));
        btnStock.addActionListener(e -> cardLayout.show(contentPanel, "Stock"));
        btnSales.addActionListener(e -> cardLayout.show(contentPanel, "Sales"));
        btnTransaction.addActionListener(e -> cardLayout.show(contentPanel, "Transactions"));

        // Logout Action
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                new LoginForm().setVisible(true);
                this.dispose(); 
            }
        });

        splitPane.setLeftComponent(sidebar);
        splitPane.setRightComponent(contentPanel);
        add(splitPane);
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        return btn;
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        java.awt.EventQueue.invokeLater(() -> {
            new MainUI().setVisible(true);
        });
    }
}