import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class FinanceEntry implements Serializable {
    String type; // "Income" or "Expense"
    String description;
    double amount;

    FinanceEntry(String type, String description, double amount) {
        this.type = type;
        this.description = description;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("%s: %s - %.2f", type, description, amount);
    }
}

public class PersonalFinanceTracker extends JFrame {
    private DefaultListModel<FinanceEntry> model;
    private JList<FinanceEntry> entryList;
    private JLabel balanceLabel;

    private File dataFile = new File("finance_data.ser");

    public PersonalFinanceTracker() {
        setTitle("Personal Finance Tracker");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultListModel<>();
        entryList = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(entryList);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField();
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();

        JButton addBtn = new JButton("Add Entry");
        JButton saveBtn = new JButton("Save Data");
        JButton loadBtn = new JButton("Load Data");

        balanceLabel = new JLabel("Balance: 0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));

        inputPanel.add(typeLabel);
        inputPanel.add(typeCombo);
        inputPanel.add(descLabel);
        inputPanel.add(descField);
        inputPanel.add(amountLabel);
        inputPanel.add(amountField);
        inputPanel.add(addBtn);
        inputPanel.add(saveBtn);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(loadBtn, BorderLayout.SOUTH);
        mainPanel.add(balanceLabel, BorderLayout.PAGE_END);

        add(mainPanel);

        addBtn.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String description = descField.getText().trim();
            String amountText = amountField.getText().trim();
            if (description.isEmpty() || amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                model.addElement(new FinanceEntry(type, description, amount));
                descField.setText("");
                amountField.setText("");
                updateBalance();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        saveBtn.addActionListener(e -> {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
                java.util.List<FinanceEntry> entries = Collections.list(model.elements());
                oos.writeObject(entries);
                JOptionPane.showMessageDialog(this, "Data saved successfully.", "Save", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadBtn.addActionListener(e -> {
            if (!dataFile.exists()) {
                JOptionPane.showMessageDialog(this, "No saved data found.", "Load", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
                java.util.List<FinanceEntry> entries = (java.util.List<FinanceEntry>) ois.readObject();
                model.clear();
                for (FinanceEntry fe : entries) {
                    model.addElement(fe);
                }
                updateBalance();
                JOptionPane.showMessageDialog(this, "Data loaded successfully.", "Load", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Error loading data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateBalance();
    }

    private void updateBalance() {
        double balance = 0.0;
        for (int i = 0; i < model.getSize(); i++) {
            FinanceEntry fe = model.get(i);
            if (fe.type.equals("Income")) {
                balance += fe.amount;
            } else {
                balance -= fe.amount;
            }
        }
        balanceLabel.setText(String.format("Balance: %.2f", balance));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PersonalFinanceTracker tracker = new PersonalFinanceTracker();
            tracker.setVisible(true);
        });
    }
}
