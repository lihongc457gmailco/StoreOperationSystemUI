package com.mycompany.storeoperationsystemgui;

import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {

    JTextField userField;
    JPasswordField passField;

    public LoginForm() {
        setTitle("Login - Store System");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("System Login", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        formPanel.add(new JLabel("User ID:"));
        userField = new JTextField();
        formPanel.add(userField);

        formPanel.add(new JLabel("Password:"));
        passField = new JPasswordField();
        formPanel.add(passField);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Logic ---

        // 1. Login Button
        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            // Check if user exists and password matches
     // NEW CODE inside loginBtn.addActionListener
        if (UserDatabase.checkLogin(username, password)) {
            JOptionPane.showMessageDialog(this, "Login Successful!");
            Session.setCurrentUser(username); // <--- ADD THIS LINE
            new MainUI().setVisible(true); 
            this.dispose(); 
        } else {
            JOptionPane.showMessageDialog(this, "Invalid User ID or Password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        // Inside your Login Button Action:

        });

       
    // 2. Register Button Logic (Protected by Manager Check)
        registerBtn.addActionListener(e -> {
            
            // Create a custom panel for the popup
            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField managerIdField = new JTextField();
            JPasswordField managerPassField = new JPasswordField();

            panel.add(new JLabel("Manager ID:"));
            panel.add(managerIdField);
            panel.add(new JLabel("Password:"));
            panel.add(managerPassField);

            // Show the popup
            int result = JOptionPane.showConfirmDialog(this, panel, 
                    "Manager Authorization Required", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            // If user clicked OK
            if (result == JOptionPane.OK_OPTION) {
                String mgrId = managerIdField.getText().trim();
                String mgrPass = new String(managerPassField.getPassword());

                // Check against CSV using our new method
                if (UserDatabase.isManager(mgrId, mgrPass)) {
                    // Success! Open Register Form
                    new RegisterForm().setVisible(true);
                    this.dispose();
                } else {
                    // Failure
                    JOptionPane.showMessageDialog(this, 
                        "Access Denied.\nInvalid credentials or you are not a Manager.", 
                        "Security Alert", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // MAIN METHOD TO START THE APP
    public static void main(String[] args) {
        // Add a default admin user for testing

        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}