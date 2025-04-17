import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Medicine {
    int id;
    String name;
    int quantity;
    double price;

    public Medicine(int id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%-5d %-20s %-10d %-10.2f", id, name, quantity, price);
    }
}

public class PharmacyManagementUI {
    private static final String CSV_FILE = "medicine_data.csv";
    private static java.util.List<Medicine> medicines = new ArrayList<>();
    private static int nextId = 1;
    private static final Random random = new Random();

    private JFrame frame;
    private JTextArea displayArea;
    private JTextField nameField, quantityField, priceField;
    private JTextField sellNameField, sellQuantityField;

    public PharmacyManagementUI() {
        loadMedicines();
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Pharmacy Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());

        displayArea = new JTextArea(15, 50);
        displayArea.setEditable(false);
        displayArea.setBackground(Color.DARK_GRAY);
        displayArea.setForeground(Color.WHITE);
        displayArea.setFont(new Font("Monospaced", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(displayArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5)); // Increased row count
        buttonPanel.setBackground(Color.BLACK);

        JButton addButton = createStyledButton("Add Medicine");
        JButton sellButton = createStyledButton("Sell Medicine");
        JButton displayButton = createStyledButton("Display Stock");
        JButton exitButton = createStyledButton("Exit");

        addButton.addActionListener(e -> addMedicine());
        sellButton.addActionListener(e -> sellMedicine());
        displayButton.addActionListener(e -> displayStock());
        exitButton.addActionListener(e -> {
            saveMedicines();
            frame.dispose();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(displayButton);
        buttonPanel.add(exitButton);

        // Credit label
        JLabel creditLabel = new JLabel("<html><center>By<br>~ Yukta Aggarwal<br>~ Riya Acharya<br>~ Kshitij Yagnik</center></html>");
        creditLabel.setFont(new Font("Arial", Font.BOLD, 14));  // Adjusted font size
        creditLabel.setForeground(Color.WHITE);
        creditLabel.setHorizontalAlignment(SwingConstants.CENTER);
        creditLabel.setVerticalAlignment(SwingConstants.CENTER);
        creditLabel.setOpaque(true);
        creditLabel.setBackground(Color.BLACK);
        creditLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Added padding around the text
        creditLabel.setPreferredSize(new Dimension(200, 10));  // Set a preferred size to make it compact

        buttonPanel.add(creditLabel); // Add credit label below exit button

        frame.add(buttonPanel, BorderLayout.WEST);
        frame.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        return button;
    }

    private void addMedicine() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Medicine Name:"));
        nameField = new JTextField();
        panel.add(nameField);
    
        panel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        panel.add(quantityField);
    
        panel.add(new JLabel("Price:"));
        priceField = new JTextField();
        panel.add(priceField);
    
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
    
        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Medicine", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
    
                // Check for duplicate medicine name
                for (Medicine med : medicines) {
                    if (med.name.equalsIgnoreCase(name)) {
                        JOptionPane.showMessageDialog(frame, "Error: Medicine name already exists!", "Duplicate Entry", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
    
                // Ensure unique ID is assigned
                int id = getNextUniqueId(); 
    
                // Add new medicine
                medicines.add(new Medicine(id, name, quantity, price));
                saveMedicines();
                displayArea.append("Medicine added successfully!\n");
    
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input! Please enter valid numbers.");
            }
        }
    }

    private void sellMedicine() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Medicine Name:"));
        sellNameField = new JTextField();
        panel.add(sellNameField);

        panel.add(new JLabel("Quantity:"));
        sellQuantityField = new JTextField();
        panel.add(sellQuantityField);

        SwingUtilities.invokeLater(() -> sellNameField.requestFocusInWindow());

        int result = JOptionPane.showConfirmDialog(frame, panel, "Sell Medicine", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = sellNameField.getText().trim();
                int quantity = Integer.parseInt(sellQuantityField.getText().trim());

                for (Medicine med : medicines) {
                    if (med.name.equalsIgnoreCase(name)) {
                        if (med.quantity >= quantity) {
                            med.quantity -= quantity;
                            generateBill(med, quantity);
                            saveMedicines();
                            displayArea.append("Sold " + quantity + " of " + med.name + "\nClick on Display stock to refresh the stock\n");
                            return;
                        } else {
                            displayArea.append("Not enough stock! Ordering...\n");
                            orderMedicine(name);
                            return;
                        }
                    }
                }

                displayArea.append("Medicine not found! Ordering...\n");
                orderMedicine(name);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input! Please enter valid numbers.");
            }
        }
    }
    private void orderMedicine(String medicineName) {
        int quantity = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter Quantity to Order:"));
    
        JDialog loadingDialog = new JDialog(frame, "Ordering Medicine...", false);
        loadingDialog.setSize(300, 150);
        loadingDialog.setLayout(new BorderLayout());
    
        JLabel loadingLabel = new JLabel("Ordering, please wait...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loadingDialog.add(loadingLabel, BorderLayout.CENTER);
    
        loadingDialog.setLocationRelativeTo(frame);
        loadingDialog.setVisible(true);
    
        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> {
            loadingDialog.dispose();
    
            // Check if medicine exists in stock
            Medicine existingMedicine = null;
            for (Medicine med : medicines) {
                if (med.name.equalsIgnoreCase(medicineName)) {
                    existingMedicine = med;
                    break;
                }
            }
    
            if (existingMedicine != null) {
                // Update the quantity of the existing medicine
                existingMedicine.quantity += quantity;
                displayStock();
                JOptionPane.showMessageDialog(frame, "Medicine quantity updated in stock!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Add the medicine as new if not found in stock
                double price = 50 + (random.nextDouble() * 450);
                int id = getNextUniqueId();  // Ensure unique ID
                medicines.add(new Medicine(id, medicineName, quantity, Math.round(price * 100.0) / 100.0));
                displayStock();
                JOptionPane.showMessageDialog(frame, "Medicine is now in stock!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
    
            saveMedicines();
        });
    
        timer.setRepeats(false);
        timer.start();
    }
    private void displayStock() {
        medicines.sort(Comparator.comparing(m -> ((Medicine) m).name.toLowerCase()));
        displayArea.setText("Current Stock:\n");
        displayArea.append(String.format("%-5s %-20s %-10s %-10s\n", "ID", "Name", "Quantity", "Price"));
        displayArea.append("-------------------------------------------------\n");
        for (Medicine med : medicines) {
            displayArea.append(med.toString() + "\n");
        }
    }

    private void generateBill(Medicine medicine, int quantity) {
        double totalPrice = medicine.price * quantity;
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String billContent = "************ PHARMACY BILL ************\n" +
                             "Date: " + new java.util.Date() + "\n" +
                             "----------------------------------------\n" +
                             "Medicine:        " + medicine.name + "\n" +
                             "Quantity:        " + quantity + "\n" +
                             "Price per unit:  " + String.format("%.2f", medicine.price) + "\n" +
                             "----------------------------------------\n" +
                             "Total Amount:    ₹" + String.format("%.2f", totalPrice) + "\n" +
                             "***************************************\n" +
                             "     Thank you for your purchase!      \n" +
                             "***************************************\n";
    
        // Show the bill in a message dialog
        JOptionPane.showMessageDialog(frame, billContent, "Generated Bill", JOptionPane.INFORMATION_MESSAGE);
    
        // Save the bill to a file
        File billFile = new File("bill_" + timeStamp + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(billFile))) {
            writer.write(billContent);
            displayArea.append("Bill generated and saved as " + billFile.getName() + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving bill to file.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }    

    private void loadMedicines() {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                medicines.add(new Medicine(Integer.parseInt(data[0]), data[1], Integer.parseInt(data[2]), Double.parseDouble(data[3])));
            }
        } catch (IOException ignored) {}
    }

    private void saveMedicines() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE))) {
            for (Medicine med : medicines) {
                bw.write(med.id + "," + med.name + "," + med.quantity + "," + med.price);
                bw.newLine();
            }
        } catch (IOException ignored) {}
    }

    private int getNextUniqueId() {
        int maxId = 0;
        for (Medicine med : medicines) {
            if (med.id > maxId) {
                maxId = med.id;
            }
        }
        return maxId + 1;  // Ensure next ID is unique
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PharmacyManagementUI::new);
    }
}