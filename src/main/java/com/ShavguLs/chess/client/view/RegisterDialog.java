package com.ShavguLs.chess.client.view;

import com.ShavguLs.chess.server.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterDialog extends JDialog {

    private JTextField nicknameField;
    private JPasswordField passwordField;
    private JPasswordField repeatPasswordField;
    private JButton registerButton;
    private JButton cancelButton;

    private boolean registrationSuccessful = false;
    private String registeredNickname = null;

    public RegisterDialog(JDialog parent) {
        super(parent, "Register New Account", true);

        // Set up the dialog
        setSize(400, 250);
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

        // Repeat password label and field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Repeat Password:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        repeatPasswordField = new JPasswordField(15);
        mainPanel.add(repeatPasswordField, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegistration();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Allow Enter key to register
        getRootPane().setDefaultButton(registerButton);
    }

    private void performRegistration() {
        String nick = nicknameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String repeatPass = new String(repeatPasswordField.getPassword());

        // Check if fields are empty
        if (nick.isEmpty() || pass.isEmpty() || repeatPass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields!",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if nickname is too short
        if (nick.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Nickname must be at least 3 characters long!",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if password is too short
        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 4 characters long!",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if passwords match
        if (!pass.equals(repeatPass)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match!",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            repeatPasswordField.setText("");
            return;
        }

        // Try to register with the database
        String response = DatabaseManager.registerUser(nick, pass);

        if (response.equals("SUCCESS")) {
            registrationSuccessful = true;
            registeredNickname = nick;
            JOptionPane.showMessageDialog(this,
                    "Registration successful! You can now login with your nickname.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else if (response.equals("NICKNAME_EXISTS")) {
            JOptionPane.showMessageDialog(this,
                    "This nickname is already taken! Please choose another one.",
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
            nicknameField.setText("");
            nicknameField.requestFocus();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Registration failed! Please try again later.",
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean wasRegistrationSuccessful() {
        return registrationSuccessful;
    }

    public String getRegisteredNickname() {
        return registeredNickname;
    }
}