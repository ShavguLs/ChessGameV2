package com.ShavguLs.chess.client.view;

import com.ShavguLs.chess.server.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginDialog extends JDialog {

    private JTextField nicknameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton cancelButton;

    private boolean loginSuccessful = false;
    private String nickname = null;

    public LoginDialog(JFrame parent) {
        super(parent, "Login to Chess Game", true);

        // Set up the dialog
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Create the main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Nickname label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Nickname:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        nicknameField = new JTextField(15);
        mainPanel.add(nicknameField, gbc);

        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistrationDialog();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Allow Enter key to login
        getRootPane().setDefaultButton(loginButton);
    }

    private void performLogin() {
        String nick = nicknameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (nick.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both nickname and password!",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Here we would normally check with the database
        // For now, we'll simulate a successful login
        String response = DatabaseManager.checkLogin(nick, pass);

        if (response.equals("SUCCESS")) {
            loginSuccessful = true;
            nickname = nick;
            JOptionPane.showMessageDialog(this,
                    "Login successful! Welcome " + nick + "!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid nickname or password!",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    private void openRegistrationDialog() {
        RegisterDialog registerDialog = new RegisterDialog(this);
        registerDialog.setVisible(true);

        // If registration was successful, auto-fill the login fields
        if (registerDialog.wasRegistrationSuccessful()) {
            nicknameField.setText(registerDialog.getRegisteredNickname());
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    public boolean wasLoginSuccessful() {
        return loginSuccessful;
    }

    public String getLoggedInNickname() {
        return nickname;
    }
}