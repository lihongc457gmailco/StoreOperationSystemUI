package com.mycompany.storeoperationsystemgui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TransactionHistoryPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Components
    private JTextField startDateField, endDateField;
    private JTextField txtSearch;
    private JLabel totalSalesLabel;
    private JPanel adminPanel;
    private JButton btnEditMode;
    private JComboBox<String> sortBox;
    
    private boolean isEditMode = false;
    private static final String SALES_FILE = "sales_data.csv";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d/MM/yyyy");
    
    private final String[] COLUMN_HEADERS = {
        "Date", "Time", "Outlet", "SalesPerson", "Customer", 
        "Payment", "Model", "Qty", "Price", "Total"
    };

    public TransactionHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 1. TOP PANEL ---
        JPanel mainTopPanel = new JPanel(new BorderLayout());
        mainTopPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainTopPanel.setBackground(new Color(245, 245, 245));
        
        // Row 1: Filter Bar (GridBagLayout for nice alignment)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterBar.setOpaque(false);
        
        // Search Input
        JPanel pSearch = createLabeledInput("Search:", txtSearch = new JTextField(12));
        // Date Inputs
        JPanel pDate1 = createLabeledInput("From (d/mm/yyyy):", startDateField = new JTextField(8));
        JPanel pDate2 = createLabeledInput("To (d/mm/yyyy):", endDateField = new JTextField(8));
        
        JButton btnFilter = new JButton("Apply");
        btnFilter.setBackground(new Color(52, 152, 219));
        btnFilter.setForeground(Color.WHITE);
        
        JButton btnReset = new JButton("Reset");
        
        filterBar.add(pSearch);
        filterBar.add(pDate1);
        filterBar.add(pDate2);
        filterBar.add(btnFilter);
        filterBar.add(btnReset);
        
        mainTopPanel.add(filterBar, BorderLayout.NORTH);

        // Row 2: Sorting & Analytics
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        toolBar.setOpaque(false);

        String[] sortOptions = { "Date (Newest First)", "Date (Oldest First)", "Amount (High-Low)", "Amount (Low-High)" };
        sortBox = new JComboBox<>(sortOptions);
        JButton btnSort = new JButton("Sort");

        JButton btnAnalytics = new JButton("View Analytics Report");
        btnAnalytics.setBackground(new Color(155, 89, 182)); // Purple
        btnAnalytics.setForeground(Color.WHITE);

        toolBar.add(Box.createHorizontalStrut(20));

        btnEditMode = new JButton("Edit");
        btnEditMode.setBackground(new Color(46, 204, 113));
        btnEditMode.setForeground(Color.WHITE);
        
        toolBar.add(new JLabel("Sort By:"));
        toolBar.add(sortBox);
        toolBar.add(btnSort);
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));
        toolBar.add(btnAnalytics);
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));
        toolBar.add(btnEditMode);
        
        mainTopPanel.add(toolBar, BorderLayout.SOUTH);
        add(mainTopPanel, BorderLayout.NORTH);

        // --- 2. CENTER PANEL: Table ---
        tableModel = new DefaultTableModel(COLUMN_HEADERS, 0) {
            @Override public boolean isCellEditable(int row, int column) { return isEditMode; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 9 || columnIndex == 8) return Double.class;
                if (columnIndex == 7) return Integer.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setComparator(0, (String d1, String d2) -> {
            try { return LocalDate.parse(d1, DATE_FMT).compareTo(LocalDate.parse(d2, DATE_FMT)); } 
            catch (Exception e) { return d1.compareTo(d2); }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 3. BOTTOM PANEL ---
        JPanel bottomContainer = new JPanel(new BorderLayout());
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        totalPanel.setBackground(new Color(240, 248, 255));
        totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        
        totalSalesLabel = new JLabel("RM 0.00");
        totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalSalesLabel.setForeground(new Color(46, 204, 113));
        
        totalPanel.add(new JLabel("Total (Visible):"));
        totalPanel.add(totalSalesLabel);
        bottomContainer.add(totalPanel, BorderLayout.NORTH);

        adminPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        adminPanel.setBackground(new Color(255, 230, 230)); 
        adminPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.RED));

        JButton btnSave = new JButton("Save Changes");
        JButton btnDelete = new JButton("Delete Row");
        JButton btnExit = new JButton("Exit");
        adminPanel.add(btnSave);
        adminPanel.add(btnDelete);
        adminPanel.add(btnExit);
        
        adminPanel.setVisible(false);
        bottomContainer.add(adminPanel, BorderLayout.SOUTH);
        add(bottomContainer, BorderLayout.SOUTH);

        // --- LOAD DATA ---
        loadSalesData();
        calculateTotal();

        // --- ACTIONS ---
        btnFilter.addActionListener(e -> applyFilters());
        btnReset.addActionListener(e -> {
            startDateField.setText(""); endDateField.setText("");txtSearch.setText(""); sorter.setRowFilter(null); calculateTotal();
            startDateField.setText(""); endDateField.setText(""); txtSearch.setText(""); sorter.setRowFilter(null); calculateTotal();
        });
        btnSort.addActionListener(e -> applySorting());
        btnAnalytics.addActionListener(e -> generateAnalyticsReport());
        btnEditMode.addActionListener(e -> setEditMode(true));
        btnDelete.addActionListener(e -> {
            if (table.getSelectedRow() >= 0) {
                if (JOptionPane.showConfirmDialog(this, "Delete record?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(table.convertRowIndexToModel(table.getSelectedRow()));
                    calculateTotal();
                }
            }
        });
        btnSave.addActionListener(e -> saveSalesData());
        btnExit.addActionListener(e -> setEditMode(false));
    }

    
    // Helper for clean UI
    private JPanel createLabeledInput(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void applyFilters() {
        String startStr = startDateField.getText().trim();
        String endStr = endDateField.getText().trim();
        String searchStr = txtSearch.getText().trim(); 

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        
        if (!startStr.isEmpty() || !endStr.isEmpty()) {
            filters.add(new RowFilter<Object, Object>() {
                @Override public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    try {
                        LocalDate rowDate = LocalDate.parse((String) entry.getValue(0), DATE_FMT);
                        if (!startStr.isEmpty() && rowDate.isBefore(LocalDate.parse(startStr, DATE_FMT))) return false;
                        if (!endStr.isEmpty() && rowDate.isAfter(LocalDate.parse(endStr, DATE_FMT))) return false;
                        return true;
                    } catch (Exception e) { return false; }
                }
            });
        }
        
        if (!searchStr.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(searchStr)));
        }

        if (filters.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.andFilter(filters));
        
        calculateTotal();
    }
    
    // --- ANALYTICS LOGIC ---
    private void generateAnalyticsReport() {
        String[] options = {"Daily", "Monthly", "Yearly"};
        int choice = JOptionPane.showOptionDialog(this, "Select Analysis Period:", "Analytics Report",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == -1) return;
        String period = options[choice];

        Map<Comparable, Double> trendData = new TreeMap<>(); 
        Map<String, Integer> productSales = new HashMap<>();
        double totalRevenue = 0;
        
        File file = new File(SALES_FILE);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "Database file (sales_data.csv) not found!");
            return;
        }
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                   String[] parts = line.split(",", -1);
                   if(parts.length < 10) continue;
                   try{
                       String dateStr = parts[0]; 
                       String model = parts[6];
                       int qty = Integer.parseInt(parts[7]);
                       double amount = Double.parseDouble(parts[9]);
                
                       LocalDate date = LocalDate.parse(dateStr, DATE_FMT);
                       Comparable key = null;
                       if (period.equals("Daily")) key = date;
                       else if (period.equals("Monthly")) key = YearMonth.from(date);
                       else if (period.equals("Yearly")) key = Year.from(date);
                
                       trendData.put(key, trendData.getOrDefault(key, 0.0) + amount);
                       productSales.put(model, productSales.getOrDefault(model, 0) + qty);
                       totalRevenue += amount;
            } catch (Exception e) { 
                continue;
            }}
        }catch(IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading database: " + e.getMessage());
            return;
        }

        if (trendData.isEmpty()) { JOptionPane.showMessageDialog(this, "No data available."); return; }

        double avgRevenue = totalRevenue / trendData.size();
        String topProduct = productSales.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Data Analytics: " + period + " View", true);
        dialog.setSize(900, 700);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        // Metrics Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statsPanel.add(createMetricCard("Total Revenue", "RM " + String.format("%.2f", totalRevenue)));
        statsPanel.add(createMetricCard("Avg " + period + " Sales", "RM " + String.format("%.2f", avgRevenue)));
        statsPanel.add(createMetricCard("Best Selling Model", topProduct));
        
        dialog.add(statsPanel, BorderLayout.NORTH);

        // --- SCROLLABLE CHARTS PANEL ---
        JPanel chartsContainer = new JPanel();
        chartsContainer.setLayout(new BoxLayout(chartsContainer, BoxLayout.Y_AXIS));
        chartsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Chart 1: Trend
        SimpleChartPanel chart1 = new SimpleChartPanel(period + " Revenue Trend", (Map) trendData);
        chartsContainer.add(chart1);
        
        chartsContainer.add(Box.createVerticalStrut(20)); // Spacer
        
        // Chart 2: Top Products
        Map<String, Double> top5Products = productSales.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (double)e.getValue(), (e1, e2) -> e1, LinkedHashMap::new));
        
        // *** KEY FIX: Use horizontal layout mode correctly ***
        SimpleChartPanel chart2 = new SimpleChartPanel("Top 5 Products (Qty Sold)", (Map) top5Products, true);
        chartsContainer.add(chart2);

        JScrollPane scrollPane = new JScrollPane(chartsContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane, BorderLayout.CENTER);

        dialog.setVisible(true);  
    }

    private JPanel createMetricCard(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245, 245, 245));
        p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 20));
        lblValue.setForeground(new Color(52, 152, 219));
        p.add(lblTitle, BorderLayout.NORTH);
        p.add(lblValue, BorderLayout.CENTER);
        return p;
    }

    // --- FIX: IMPROVED CHART PANEL THAT CALCULATES HEIGHT DYNAMICALLY ---
    class SimpleChartPanel extends JPanel {
        private String title;
        private Map<?, Double> data;
        private boolean isHorizontal;
        private final int BAR_HEIGHT = 40; // Fixed bar height
        private final int GAP = 15;        // Fixed gap between bars
        private final int PADDING = 40;
        private final int BAR_WIDTH = 50;

        public SimpleChartPanel(String title, Map<?, Double> data) { this(title, data, false); }
        
        public SimpleChartPanel(String title, Map<?, Double> data, boolean isHorizontal) {
            this.title = title; 
            this.data = data; 
            this.isHorizontal = isHorizontal;
            setBorder(BorderFactory.createTitledBorder(title)); 
            setBackground(Color.WHITE);
        }

        // *** CRITICAL: Override getPreferredSize to force scrollpane to expand ***
        @Override
        public Dimension getPreferredSize() {
            int width = 800; // Default width
            int height = 350;
            
            if (isHorizontal) {
                // Height grows based on number of items
                height = PADDING * 2 + (data.size() * (BAR_HEIGHT + GAP));
            } else {
                // Fixed height for vertical charts
                int requiredWidth = PADDING * 2 + (data.size() * (BAR_WIDTH + GAP));
                width = Math.max(800, requiredWidth);
            }
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            
            double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
            
            int i = 0;
            for (Map.Entry<?, Double> entry : data.entrySet()) {
                double val = entry.getValue();
                g2.setColor(new Color(100, 149, 237));
                
                if (isHorizontal) {
                    // *** Horizontal Logic using fixed BAR_HEIGHT ***
                    int barLen = (int) ((val / max) * (w - 2 * PADDING - 150)); // -150 for labels
                    int y = PADDING + i * (BAR_HEIGHT + GAP);
                    
                    g2.fillRect(PADDING + 120, y, barLen, BAR_HEIGHT);
                    g2.setColor(Color.BLACK);
                    
                    // Draw Label Left
                    g2.drawString(entry.getKey().toString(), PADDING, y + BAR_HEIGHT / 2 + 5);
                    // Draw Value Right
                    g2.drawString(String.format("%.0f", val), PADDING + 125 + barLen, y + BAR_HEIGHT / 2 + 5);
                } else {
                    // Vertical Logic (Revenue Trend)
                    int barLen = (int) ((val / max) * (h - 2 * PADDING - 20));
                    int x = PADDING + i * (BAR_WIDTH + GAP);
                    int y = h - PADDING - barLen;
                    
                    g2.fillRect(x, y, BAR_WIDTH, barLen);
                    g2.setColor(Color.BLACK);
                    
                    String lbl = entry.getKey().toString();
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(lbl);
                    int textX = x + (BAR_WIDTH - textWidth) / 2;
                    
                    g2.drawString(lbl, textX, h - 15);
                    
                    String valStr = String.format("%.0f", val);
                    int valWidth = fm.stringWidth(valStr);
                    int valX = x + (BAR_WIDTH - valWidth) / 2;
                    g2.drawString(valStr, valX, y - 5);
                }
                i++;
            }
        }
    }

    // --- UTILS ---
    private void setEditMode(boolean active) {
        isEditMode = active;
        adminPanel.setVisible(active);
        btnEditMode.setEnabled(!active);
        tableModel.fireTableDataChanged(); 
        revalidate(); repaint();
    }
    
    private void loadSalesData() {
        File file = new File(SALES_FILE);
        if (!file.exists()) return;
        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextLine()) scanner.nextLine(); 
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue; 
                String[] parts = line.split(",", -1);
                if (parts.length >= COLUMN_HEADERS.length) {
                    Object[] rowData = new Object[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        if (i == 9 || i == 8) { try { rowData[i] = Double.parseDouble(parts[i]); } catch(Exception e) { rowData[i] = 0.0; } }
                        else if (i == 7) { try { rowData[i] = Integer.parseInt(parts[i]); } catch(Exception e) { rowData[i] = 0; } }
                        else { rowData[i] = parts[i]; }
                    }
                    tableModel.addRow(rowData);
                }
            }
        } catch (FileNotFoundException e) { e.printStackTrace(); }
    }

    private void applySorting() {
        String selection = (String) sortBox.getSelectedItem();
        List<RowSorter.SortKey> keys = new ArrayList<>();
        int col = 0; SortOrder order = SortOrder.ASCENDING;
        if (selection.contains("Date")) { col = 0; if (selection.contains("Newest")) order = SortOrder.DESCENDING; }
        else if (selection.contains("Amount")) { col = 9; if (selection.contains("High")) order = SortOrder.DESCENDING; }
        keys.add(new RowSorter.SortKey(col, order));
        sorter.setSortKeys(keys); sorter.sort();
    }

    private void calculateTotal() {
        double total = 0.0;
        for (int i = 0; i < table.getRowCount(); i++) {
            total += (Double) tableModel.getValueAt(table.convertRowIndexToModel(i), 9);
        }
        totalSalesLabel.setText("RM " + String.format("%.2f", total));
    }

    private void saveSalesData() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        try (PrintWriter pw = new PrintWriter(new File(SALES_FILE))) {
            pw.println(String.join(",", COLUMN_HEADERS));
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                List<String> rowData = new ArrayList<>();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Object val = tableModel.getValueAt(i, j);
                    rowData.add(val == null ? "" : val.toString());
                }
                pw.println(String.join(",", rowData));
            }
            JOptionPane.showMessageDialog(this, "Saved!");
        } catch (IOException e) { JOptionPane.showMessageDialog(this, "Error saving."); }
    }
}