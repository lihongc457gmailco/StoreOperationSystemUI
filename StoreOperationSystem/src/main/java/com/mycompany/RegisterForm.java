package com.mycompany.storeoperationsystemgui;

import javax.swing.*;
import java.awt.*;

public class RegisterForm extends JFrame {

    // Added fields for Name and Role
    JTextField idField, nameField, roleField;
    JPasswordField passField, confirmPassField;

    public RegisterForm() {
        setTitle("Register New User");
        setSize(400, 450); // Increased height to fit new fields
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Header ---
        JLabel header = new JLabel("Create Account", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // --- Form Fields ---
        // Changed GridLayout to (5 rows, 2 columns) to fit the extra fields
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 1. Employee ID
        formPanel.add(new JLabel("User ID:"));
        idField = new JTextField();
        formPanel.add(idField);

        // 2. Employee Name (NEW)
        formPanel.add(new JLabel("Full Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        // 3. Role/Status (NEW)
        formPanel.add(new JLabel("Role (e.g., Manager):"));
        roleField = new JTextField();
        formPanel.add(roleField);

        // 4. Password
        formPanel.add(new JLabel("Password:"));
        passField = new JPasswordField();
        formPanel.add(passField);

        // 5. Confirm Password
        formPanel.add(new JLabel("Confirm Password:"));
        confirmPassField = new JPasswordField();
        formPanel.add(confirmPassField);

        add(formPanel, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel();
        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");

        buttonPanel.add(registerBtn);
        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Logic ---
        
        // 1. Register Button Logic
        registerBtn.addActionListener(e -> {
            // Get text from all 4 fields
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String role = roleField.getText().trim();
            String password = new String(passField.getPassword());
            String confirm = new String(confirmPassField.getPassword());

            // Validation: Check if any field is empty
            if (id.isEmpty() || name.isEmpty() || role.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            // Validation: Check passwords match
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }

            // Call the UPDATED database method with 4 arguments
            boolean success = UserDatabase.registerUser(id, name, role, password);

            if (success) {
                JOptionPane.showMessageDialog(this, "Registration Successful!");
                new LoginForm().setVisible(true);
                this.dispose(); // Close Register window
            } else {
                JOptionPane.showMessageDialog(this, "User ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 2. Back Button Logic
        backBtn.addActionListener(e -> {
            new LoginForm().setVisible(true);
            this.dispose();
        });
    }
}