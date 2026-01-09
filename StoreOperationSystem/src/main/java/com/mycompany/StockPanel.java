package com.mycompany.storeoperationsystemgui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StockPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JScrollPane scrollPane;
    
    // Components
    private JTextField searchField;
    private JButton btnEnableEdit, btnStockCount, btnStockMovement;
    
    // Edit Panel
    private JPanel editPanel;
    private JButton btnSave, btnAdd, btnDelete, btnExitEdit;
    
    private boolean isEditMode = false;
    private String[] columnHeaders;

    public StockPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 1. TOP PANEL ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        topPanel.add(searchField);
        JButton btnSearch = new JButton("Search");
        topPanel.add(btnSearch);

        btnStockCount = new JButton("Stock Count");
        btnStockCount.setBackground(new Color(155, 89, 182));
        btnStockCount.setForeground(Color.WHITE);
        btnStockCount.setFocusPainted(false);
        topPanel.add(Box.createHorizontalStrut(10)); 
        topPanel.add(btnStockCount);
        
        btnStockMovement = new JButton("Stock In/Out");
        btnStockMovement.setBackground(new Color(230, 126, 34));
        btnStockMovement.setForeground(Color.WHITE);
        btnStockMovement.setFocusPainted(false);
        topPanel.add(btnStockMovement);

        btnEnableEdit = new JButton("Edit");
        btnEnableEdit.setBackground(new Color(52, 152, 219));
        btnEnableEdit.setForeground(Color.WHITE);
        btnEnableEdit.setFocusPainted(false);
        topPanel.add(Box.createHorizontalStrut(10)); 
        topPanel.add(btnEnableEdit);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. CENTER PANEL ---
        refreshTableData();

        // --- 3. BOTTOM PANEL ---
        editPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        editPanel.setBackground(new Color(245, 245, 245));
        
        btnAdd = new JButton("Add Row");
        btnAdd.setBackground(new Color(241, 196, 15));
        
        btnDelete = new JButton("Delete Row");
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        
        btnSave = new JButton("Save Changes");
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);

        btnExitEdit = new JButton("Exit");
        btnExitEdit.setBackground(Color.DARK_GRAY);
        btnExitEdit.setForeground(Color.WHITE);

        editPanel.add(btnAdd);
        editPanel.add(btnDelete);
        editPanel.add(btnSave);
        editPanel.add(new JSeparator(SwingConstants.VERTICAL)); 
        editPanel.add(btnExitEdit);

        editPanel.setVisible(false);
        add(editPanel, BorderLayout.SOUTH);

        // --- ACTIONS ---
        btnSearch.addActionListener(e -> {
            String text = searchField.getText();
            if (text.trim().length() == 0) sorter.setRowFilter(null);
            else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        });

        btnStockCount.addActionListener(e -> startStockCountWizard());
        btnStockMovement.addActionListener(e -> openStockMovementDialog());
        btnEnableEdit.addActionListener(e -> setEditMode(true));
        
        btnAdd.addActionListener(e -> {
            String[] emptyRow = new String[columnHeaders.length];
            emptyRow[0] = "NEW ITEM";
            for(int i=1; i<emptyRow.length; i++) emptyRow[i] = "0";
            tableModel.addRow(emptyRow);
        });
        
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) tableModel.removeRow(table.convertRowIndexToModel(selectedRow));
        });
        
        btnSave.addActionListener(e -> saveStockData());
        btnExitEdit.addActionListener(e -> setEditMode(false));
    }

    private void refreshTableData() {
        if (scrollPane != null) remove(scrollPane);
        
        List<String[]> csvData = StockIO.getAllStock();
        if (csvData.isEmpty()) {
            columnHeaders = new String[]{"Model", "Price"};
        } else {
            columnHeaders = csvData.get(0);
            csvData.remove(0); 
        }

        tableModel = new DefaultTableModel(columnHeaders, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return isEditMode;
            }
        };

        for (String[] row : csvData) {
            tableModel.addRow(row);
        }

        table = new JTable(tableModel);
        table.setRowHeight(25);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    private void setEditMode(boolean active) {
        isEditMode = active;
        editPanel.setVisible(active);
        btnEnableEdit.setVisible(!active);
        revalidate();
        repaint();
    }

    private void saveStockData() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        List<String[]> newData = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String[] row = new String[tableModel.getColumnCount()];
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object val = tableModel.getValueAt(i, j);
                row[j] = (val == null) ? "" : val.toString();
            }
            newData.add(row);
        }
        if (StockIO.saveAllStock(columnHeaders, newData)) {
            JOptionPane.showMessageDialog(this, "Saved Successfully!");
        }
    }

    private void openStockMovementDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Stock Movement Record", true);
        dialog.setSize(600, 500);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Movement Details"));

        settingsPanel.add(new JLabel("Transaction Type:"));
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Stock In", "Stock Out"});
        settingsPanel.add(typeBox);

        settingsPanel.add(new JLabel("From:"));
        JComboBox<String> fromBox = new JComboBox<>(getOutletsWithServiceCenter());
        fromBox.setEditable(true);
        settingsPanel.add(fromBox);

        settingsPanel.add(new JLabel("To:"));
        JComboBox<String> toBox = new JComboBox<>(getOutletsWithServiceCenter());
        toBox.setEditable(true);
        settingsPanel.add(toBox);

        typeBox.addActionListener(e -> {
            if (typeBox.getSelectedItem().equals("Stock In")) {
                fromBox.setSelectedItem("Service Center");
                if (toBox.getItemCount() > 0) toBox.setSelectedIndex(0);
            } else {
                if (fromBox.getItemCount() > 0) fromBox.setSelectedIndex(0);
                toBox.setSelectedItem("Service Center");
            }
        });
        dialog.add(settingsPanel, BorderLayout.NORTH);

        DefaultTableModel moveModel = new DefaultTableModel(new String[]{"Model", "Quantity"}, 0);
        JTable moveTable = new JTable(moveModel) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBorder(BorderFactory.createTitledBorder("Items to Move"));
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> modelBox = new JComboBox<>();
        for(int i=0; i<tableModel.getRowCount(); i++) modelBox.addItem((String)tableModel.getValueAt(i, 0));
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JButton btnAddItem = new JButton("Add Item");
        JButton btnRemoveItem = new JButton("Remove Selected");
        btnRemoveItem.setBackground(new Color(231, 76, 60));
        btnRemoveItem.setForeground(Color.WHITE);

        inputPanel.add(new JLabel("Model:"));
        inputPanel.add(modelBox);
        inputPanel.add(new JLabel("Qty:"));
        inputPanel.add(qtySpinner);
        inputPanel.add(btnAddItem);
        inputPanel.add(btnRemoveItem);
        
        itemPanel.add(inputPanel, BorderLayout.NORTH);
        itemPanel.add(new JScrollPane(moveTable), BorderLayout.CENTER);
        dialog.add(itemPanel, BorderLayout.CENTER);

        JButton btnConfirm = new JButton("CONFIRM MOVEMENT & GENERATE RECEIPT");
        btnConfirm.setBackground(new Color(46, 204, 113));
        btnConfirm.setForeground(Color.WHITE);
        dialog.add(btnConfirm, BorderLayout.SOUTH);

        btnAddItem.addActionListener(e -> moveModel.addRow(new Object[]{modelBox.getSelectedItem(), qtySpinner.getValue()}));
        btnRemoveItem.addActionListener(e -> {
            int selectedRow = moveTable.getSelectedRow();
            if (selectedRow != -1) moveModel.removeRow(selectedRow);
        });

        btnConfirm.addActionListener(e -> {
            if (moveModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dialog, "Please add items.");
                return;
            }
            String type = (String) typeBox.getSelectedItem();
            String from = (String) fromBox.getSelectedItem();
            String to = (String) toBox.getSelectedItem();
            boolean success = true;
            for(int i=0; i<moveModel.getRowCount(); i++) {
                String model = (String) moveModel.getValueAt(i, 0);
                int qty = (Integer) moveModel.getValueAt(i, 1);
                if (type.equals("Stock In")) {
                    if (isValidOutlet(to)) if (!StockIO.addStock(model, to, qty)) success = false;
                } else {
                    if (isValidOutlet(from)) if (!StockIO.deductStock(model, from, qty)) success = false;
                }
            }
            if (success) {
                saveStockMovementReceipt(type, from, to, moveModel);
                dialog.dispose();
                refreshTableData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error updating stock.");
            }
        });
        dialog.setVisible(true);
    }

    private String[] getOutletsWithServiceCenter() {
        List<String> list = new ArrayList<>();
        list.add("Service Center");
        for (int i = 2; i < tableModel.getColumnCount(); i++) {
            list.add(tableModel.getColumnName(i));
        }
        return list.toArray(new String[0]);
    }
    
    private boolean isValidOutlet(String name) {
        for(int i=0; i<tableModel.getColumnCount(); i++) {
            if(tableModel.getColumnName(i).equals(name)) return true;
        }
        return false;
    }

    private void saveStockMovementReceipt(String type, String from, String to, DefaultTableModel model) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String filename = "StockMovement_" + dateStr + ".txt";
        String employee = Session.getCurrentUser();

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("        STOCK MOVEMENT RECEIPT           \n");
        sb.append("=========================================\n");
        sb.append("Type:     ").append(type).append("\n");
        sb.append("Date:     ").append(dateStr).append(" ").append(timeStr).append("\n");
        sb.append("From:     ").append(from).append("\n");
        sb.append("To:       ").append(to).append("\n");
        sb.append("Employee: ").append(employee).append("\n");
        sb.append("-----------------------------------------\n");
        
        for (int i = 0; i < model.getRowCount(); i++) {
            String m = (String) model.getValueAt(i, 0);
            int q = (Integer) model.getValueAt(i, 1);
            sb.append(String.format("%-20s %-10d\n", m, q));
        }
        sb.append("=========================================\n\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(sb.toString());
            writer.newLine(); 
        } catch (IOException e) { e.printStackTrace(); }
        
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(sb.toString())), "Movement Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==========================================
    //       FIXED STOCK COUNT LOGIC
    // ==========================================
    class StockRecord {
        String modelName;
        int systemCount;
        int userCount;
        public StockRecord(String modelName, int systemCount) {
            this.modelName = modelName;
            this.systemCount = systemCount;
            this.userCount = -1; 
        }
    }
    
    // Store current state of the wizard
    private JDialog wizardDialog;
    private JLabel lblProgress, lblModel;
    private JTextField txtCount;
    private JButton btnBack, btnNext;
    private List<StockRecord> currentCountList;
    private String currentOutletName;
    private int currentIndex = 0;

    private void startStockCountWizard() {
        // 1. Outlet Selection (Same as before)
        List<String> outlets = new ArrayList<>();
        for (int i = 2; i < tableModel.getColumnCount(); i++) {
            outlets.add(tableModel.getColumnName(i));
        }
        String selectedOutlet = (String) JOptionPane.showInputDialog(
                this, "Select Outlet Area:", "Stock Count", 
                JOptionPane.QUESTION_MESSAGE, null, outlets.toArray(), outlets.get(0));
        if (selectedOutlet == null) return;

        int outletColIndex = -1;
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (tableModel.getColumnName(i).equals(selectedOutlet)) {
                outletColIndex = i;
                break;
            }
        }

        currentCountList = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String model = (String) tableModel.getValueAt(i, 0);
            String sysCountStr = (String) tableModel.getValueAt(i, outletColIndex);
            int sysCount = 0;
            try { sysCount = Integer.parseInt(sysCountStr); } catch (Exception e) {}
            currentCountList.add(new StockRecord(model, sysCount));
        }
        
        currentOutletName = selectedOutlet;
        currentIndex = 0;
        
        // 2. Initialize the Single Dialog
        initWizardDialog();
        
        // 3. Show First Item
        updateWizardUI();
    }

    private void initWizardDialog() {
        wizardDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Stock Count Wizard", true);
        wizardDialog.setSize(400, 300);
        wizardDialog.setLayout(new BorderLayout());
        wizardDialog.setLocationRelativeTo(this);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        lblProgress = new JLabel();
        lblProgress.setFont(new Font("Arial", Font.BOLD, 12));
        lblProgress.setForeground(Color.GRAY);

        lblModel = new JLabel();
        lblModel.setFont(new Font("Arial", Font.BOLD, 20));
        lblModel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblInstruction = new JLabel("Enter physical quantity:");
        txtCount = new JTextField();
        txtCount.setFont(new Font("Arial", Font.PLAIN, 18));
        txtCount.setHorizontalAlignment(SwingConstants.CENTER);

        centerPanel.add(lblProgress);
        centerPanel.add(lblModel);
        centerPanel.add(lblInstruction);
        centerPanel.add(txtCount);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnBack = new JButton("Back");
        btnNext = new JButton("Next");
        
        btnPanel.add(btnBack);
        btnPanel.add(btnNext);

        wizardDialog.add(centerPanel, BorderLayout.CENTER);
        wizardDialog.add(btnPanel, BorderLayout.SOUTH);

        // Events
        btnBack.addActionListener(e -> {
            saveCurrentInput();
            if (currentIndex > 0) {
                currentIndex--;
                updateWizardUI();
            }
        });

        btnNext.addActionListener(e -> {
            if (validateAndSaveInput()) {
                if (currentIndex < currentCountList.size() - 1) {
                    currentIndex++;
                    updateWizardUI();
                } else {
                    // FINISH
                    wizardDialog.dispose();
                    showFinalResultTable();
                }
            }
        });
        
        wizardDialog.getRootPane().setDefaultButton(btnNext);
        updateWizardUI();
        wizardDialog.setVisible(true);
    }

    private void updateWizardUI() {
        StockRecord item = currentCountList.get(currentIndex);
        lblProgress.setText("Item " + (currentIndex + 1) + " of " + currentCountList.size());
        lblModel.setText("Model: " + item.modelName);
        
        if (item.userCount != -1) {
            txtCount.setText(String.valueOf(item.userCount));
            txtCount.selectAll();
        } else {
            txtCount.setText("");
            txtCount.requestFocus();
        }
        
        btnBack.setEnabled(currentIndex > 0);
        btnNext.setText(currentIndex == currentCountList.size() - 1 ? "Finish" : "Next");
    }

    private boolean validateAndSaveInput() {
        String text = txtCount.getText().trim();
        try {
            int val = Integer.parseInt(text);
            currentCountList.get(currentIndex).userCount = val;
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(wizardDialog, "Invalid Number");
            return false;
        }
    }
    
    private void saveCurrentInput() {
        try {
            int val = Integer.parseInt(txtCount.getText().trim());
            currentCountList.get(currentIndex).userCount = val;
        } catch (Exception e) { /* Ignore on back */ }
    }

    private void showFinalResultTable() {
        JDialog resultDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Results: " + currentOutletName, true);
        resultDialog.setLayout(new BorderLayout());

        String[] headers = {"Model", "System Count", "Your Count", "Status"};
        DefaultTableModel resultModel = new DefaultTableModel(headers, 0);
        

        int correctCount = 0, wrongCount = 0;

        for (StockRecord record : currentCountList) {
            boolean isCorrect = (record.systemCount == record.userCount);
            String status = isCorrect ? "TALLY CORRECT" : "MISMATCH DETECTED";
            if(isCorrect) correctCount++; else wrongCount++;
            resultModel.addRow(new Object[]{record.modelName, record.systemCount, record.userCount, status});
        }

        JTable resultTable = new JTable(resultModel);
        resultTable.setRowHeight(30);

        resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getModel().getValueAt(row, 3);
                if ("TALLY CORRECT".equals(status)) {
                    c.setBackground(new Color(198, 239, 206)); 
                    c.setForeground(new Color(0, 97, 0));
                } else {
                    c.setBackground(new Color(255, 199, 206)); 
                    c.setForeground(new Color(156, 0, 6));
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(resultTable);
        scrollPane.setPreferredSize(new Dimension(550, 300));
        resultDialog.add(new JScrollPane(resultTable),BorderLayout.CENTER);
        JPanel bottomcontainer = new JPanel(new BorderLayout());
        
        //Conclusion Panel
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("SUMMARY"));
        
        JLabel lblTotal = new JLabel("Total Items Counted: " + currentCountList.size());
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel lblCorrect = new JLabel("Correct: " + correctCount);
        lblCorrect.setForeground(new Color(46, 204, 113));
        lblCorrect.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel lblWrong = new JLabel("Mismatch: " + wrongCount);
        lblWrong.setForeground(Color.RED);
        lblWrong.setFont(new Font("Arial", Font.BOLD, 14));
        
        statsPanel.add(lblTotal);
        statsPanel.add(lblCorrect);
        statsPanel.add(lblWrong);
        
        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statsWrapper.add(statsPanel, BorderLayout.CENTER);
        bottomcontainer.add(statsWrapper, BorderLayout.CENTER);

        JButton btnClose = new JButton("Close Report");
        btnClose.addActionListener(e -> resultDialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        btnPanel.add(btnClose);
        bottomcontainer.add(btnPanel,BorderLayout.SOUTH);
        resultDialog.add(bottomcontainer, BorderLayout.SOUTH);
        resultDialog.pack(); 
        resultDialog.setLocationRelativeTo(this);
        resultDialog.setVisible(true);
    }
}