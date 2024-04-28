package GestorPorts;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirewallGUI {
    private FirewallManager manager;
    private JTable table;
    private JButton modifyButton;
    private JButton deleteButton;
    private JButton newRuleButton;
    private JButton historyButton;
    private DefaultTableModel tableModel;
    private JList<String> historyList;
    private JFrame frame;

    public FirewallGUI() {
        this.manager = new FirewallManager();
        this.tableModel = new DefaultTableModel(new Object[] { "Nom", "Port", "Protocol", "App", "Usuari", "Grup",
                "IP", "Accio", "Interficie", "Sentit" }, 0);
        this.table = new JTable(tableModel);
        this.modifyButton = new JButton("Modificar");
        this.deleteButton = new JButton("Esborrar");
        this.newRuleButton = new JButton("Nova Regla");
        this.historyButton = new JButton("Historial");
        this.historyList = new JList<>();
        this.frame = new JFrame("Configurar Regles del Firewall");

        modifyButton.setEnabled(false);
        deleteButton.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean isRowSelected = table.getSelectedRow() != -1;
            modifyButton.setEnabled(isRowSelected);
            deleteButton.setEnabled(isRowSelected);
        });

        newRuleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RuleDialog dialog = new RuleDialog(frame);
                dialog.setVisible(true);
                if (dialog.isRuleSaved()) {
                    addRuleToTable(dialog.getRule());
                }
            }
        });

        modifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    List<Object> selectedRule = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        selectedRule.add(tableModel.getValueAt(selectedRow, i));
                    }

                    // Store the original name of the rule
                    String originalName = (String) selectedRule.get(0);
                    System.out.println("Original rule name: " + originalName);

                    RuleModifier ruleModifier = new RuleModifier(selectedRule);
                    JPanel modifyPanel = ruleModifier.getModifyPanel();

                    // Declare the JFrame here
                    JFrame modifyFrame = new JFrame("Modificar Regla");

                    JButton saveButton = new JButton("Guardar");
                    saveButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            // Create a new FirewallRule with the modified values
                            FirewallRule modifiedRule = new FirewallRule(
                                    ruleModifier.getNomFieldText(),
                                    Integer.parseInt(ruleModifier.getPortFieldText()),
                                    ruleModifier.getProtocolFieldText(),
                                    ruleModifier.getAppFieldText(),
                                    ruleModifier.getUsuariFieldText(),
                                    ruleModifier.getGrupFieldText(),
                                    ruleModifier.getIpFieldText(),
                                    ruleModifier.getAccioFieldText(),
                                    ruleModifier.getInterficieFieldText(),
                                    ruleModifier.getSentitFieldText());

                            System.out.println("Modified rule: " + modifiedRule.toString());

                            // Update the rule in the firewall manager
                            try {
                                manager.updateRule(originalName, modifiedRule);
                                System.out.println("Rule updated successfully");
                                // Close the frame after successful update
                                modifyFrame.dispose();
                            } catch (Exception ex) {
                                // Handle the exception appropriately for your application
                                System.out.println("Error updating rule: " + ex.getMessage());
                            }

                            // Update the table model
                            List<Object> modifiedRuleList = Arrays.asList(modifiedRule.getName(),
                                    modifiedRule.getPort(),
                                    modifiedRule.getProtocol(), modifiedRule.getApplication(), modifiedRule.getUser(),
                                    modifiedRule.getGroup(), modifiedRule.getIpAddress(), modifiedRule.getAction(),
                                    modifiedRule.getNetworkInterface(), modifiedRule.getDirection());

                            for (int i = 0; i < 10; i++) {
                                tableModel.setValueAt(modifiedRuleList.get(i), selectedRow, i);
                            }
                        }
                    });
                    modifyPanel.add(saveButton);

                    modifyFrame.setContentPane(modifyPanel);
                    modifyFrame.pack();
                    modifyFrame.setVisible(true);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    // Obtener el nombre de la regla seleccionada
                    String ruleName = (String) tableModel.getValueAt(selectedRow, 0);
        
                    // Eliminar la regla del gestor de firewall
                    try {
                        manager.deleteRule(ruleName);
                        // Eliminar la fila correspondiente de la tabla
                        tableModel.removeRow(selectedRow);
                        System.out.println("Rule deleted successfully");
                        // Agregar una entrada al historial (opcional)
                        // addEntryToHistory("Rule deleted: " + ruleName);
                    } catch (Exception ex) {
                        // Manejar la excepción adecuadamente para tu aplicación
                        System.out.println("Error deleting rule: " + ex.getMessage());
                    }
                }
            }
        });
        
        // Load rules from database
        List<FirewallRule> rules = manager.getAllRules();
        for (FirewallRule rule : rules) {
            addRuleToTable(rule);
        }
    }

    private void addRuleToTable(FirewallRule rule) {
        tableModel.addRow(new Object[] {
                rule.getName(),
                rule.getPort(),
                rule.getProtocol(),
                rule.getApplication(),
                rule.getUser(),
                rule.getGroup(),
                rule.getIpAddress(),
                rule.getAction(),
                rule.getNetworkInterface(),
                rule.getDirection()
        });
    }

    public void start() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel rulesLabel = new JLabel("  Regles: ");
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(rulesLabel, BorderLayout.WEST);
        topPanel.add(historyButton, BorderLayout.EAST);

        JPanel bottomLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomLeftPanel.add(newRuleButton);

        JPanel bottomRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRightPanel.add(modifyButton);
        bottomRightPanel.add(deleteButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(bottomLeftPanel, BorderLayout.WEST);
        bottomPanel.add(bottomRightPanel, BorderLayout.EAST);

        // Add components to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
}