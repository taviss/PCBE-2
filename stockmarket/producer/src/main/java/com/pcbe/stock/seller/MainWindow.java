package com.pcbe.stock.seller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.net.URL;

public class MainWindow {
    // public static JTextField userField = null;
    private static String username = "";
    private static boolean isCompany = false;
    public static boolean connect = false;
    private static JFrame mainFrame;

    private static void initGUI() throws IOException {
        mainFrame = new JFrame("Stock Market");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new GridBagLayout());
        URL img = MainWindow.class.getClassLoader().getResource("seller.png");
        if(img != null) {
            ImageIcon icon = new ImageIcon(img);
            mainFrame.setIconImage(icon.getImage());
        }
        mainFrame.setLocationRelativeTo(null);
        JPanel mainPanel = new JPanel(new GridLayout(3, 1));
        JPanel inputUsername = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputUsername.add(new JLabel("Username:"));
        JTextField userField = new JTextField(10);
        userField.setText(username);
        userField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                userField.selectAll();
                username = userField.getText();
            }
        });
        inputUsername.add(userField);
        mainPanel.add(inputUsername);
        
        JPanel connectPanel = new JPanel(new GridLayout(1, 1));
        JButton connectButton = new JButton("Connect");
        connectButton.setActionCommand("connect");
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("connect")) {
                    if (username.equals("")) {
                        JOptionPane.showMessageDialog(null, "Username cannot be empty");
                    } else {
                        connect = true;
                        new StockPublisher(username).run();
                        mainFrame.setVisible(false);
                    }
                }
            }
        });
        connectButton.setEnabled(true);
        connectPanel.add(connectButton);
        mainPanel.add(connectPanel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainFrame.add(mainPanel, gbc);
        mainFrame.setResizable(true);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        initGUI();
    }

}
