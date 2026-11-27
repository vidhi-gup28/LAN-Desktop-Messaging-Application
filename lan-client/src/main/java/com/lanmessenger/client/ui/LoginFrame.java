package com.lanmessenger.client.ui;

import com.lanmessenger.client.net.ClientNetworkListener;
import com.lanmessenger.client.net.NetworkClient;
import com.lanmessenger.common.model.ChatMessage;
import com.lanmessenger.common.model.GroupInfo;
import com.lanmessenger.common.model.NetworkMessage;
import com.lanmessenger.common.util.NetworkConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

/**
 * Enterprise connection dialog.
 * Structured native desktop dialog for server IP, port, and user credentials.
 */
public class LoginFrame extends JFrame implements ClientNetworkListener {

    private final JTextField usernameField;
    private final JTextField hostField;
    private final JTextField portField;
    private final JButton connectButton;
    private final JButton exitButton;
    private final JLabel statusLabel;

    private NetworkClient client;

    public LoginFrame() {
        setTitle("LAN Messenger — Connect to Server");
        setSize(400, 360);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(UiUtils.COLOR_BG_WHITE);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
        headerPanel.setBackground(UiUtils.COLOR_BG_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiUtils.COLOR_BORDER_GREY),
                new EmptyBorder(0, 0, 12, 0)
        ));

        JLabel titleLabel = new JLabel("LAN Messenger Client");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UiUtils.COLOR_NAVY_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel("Enterprise Local Area Network Messaging System");
        descLabel.setFont(UiUtils.FONT_SMALL);
        descLabel.setForeground(UiUtils.COLOR_TEXT_SECONDARY);
        headerPanel.add(descLabel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Form Grid (Standard enterprise two-column aligned layout)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UiUtils.COLOR_BG_WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(UiUtils.FONT_TITLE);
        formPanel.add(lblUser, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        usernameField = new JTextField();
        usernameField.setFont(UiUtils.FONT_BODY);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));
        formPanel.add(usernameField, gbc);

        // Server IP
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblHost = new JLabel("Server IP:");
        lblHost.setFont(UiUtils.FONT_TITLE);
        formPanel.add(lblHost, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        hostField = new JTextField(detectDefaultServerIp());
        hostField.setFont(UiUtils.FONT_BODY);
        hostField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));
        formPanel.add(hostField, gbc);

        // Port
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblPort = new JLabel("Port:");
        lblPort.setFont(UiUtils.FONT_TITLE);
        formPanel.add(lblPort, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        portField = new JTextField(String.valueOf(NetworkConstants.DEFAULT_PORT));
        portField.setFont(UiUtils.FONT_BODY);
        portField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));
        formPanel.add(portField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 3. Bottom Action & Status Panel
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 8));
        bottomContainer.setBackground(UiUtils.COLOR_BG_WHITE);

        statusLabel = new JLabel("Ready to connect");
        statusLabel.setFont(UiUtils.FONT_SMALL);
        statusLabel.setForeground(UiUtils.COLOR_TEXT_SECONDARY);
        bottomContainer.add(statusLabel, BorderLayout.NORTH);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setBackground(UiUtils.COLOR_BG_WHITE);

        exitButton = new JButton("Exit");
        exitButton.setFont(UiUtils.FONT_BODY);
        exitButton.setFocusPainted(false);
        exitButton.setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        exitButton.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
        exitButton.setPreferredSize(new Dimension(80, 28));
        exitButton.addActionListener(e -> System.exit(0));
        buttonBar.add(exitButton);

        connectButton = new JButton("Connect");
        connectButton.setFont(UiUtils.FONT_TITLE);
        connectButton.setBackground(UiUtils.COLOR_NAVY_PRIMARY);
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        connectButton.setBorder(BorderFactory.createLineBorder(new Color(23, 37, 84), 1));
        connectButton.setPreferredSize(new Dimension(90, 28));
        connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectButton.addActionListener(e -> attemptConnect());
        buttonBar.add(connectButton);

        bottomContainer.add(buttonBar, BorderLayout.SOUTH);
        mainPanel.add(bottomContainer, BorderLayout.SOUTH);

        // Enter key submits form
        usernameField.addActionListener(e -> attemptConnect());
        hostField.addActionListener(e -> attemptConnect());
        portField.addActionListener(e -> attemptConnect());

        setContentPane(mainPanel);
    }

    private void attemptConnect() {
        String username = usernameField.getText().trim();
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();

        if (username.isEmpty()) {
            showError("Username is required.");
            usernameField.requestFocus();
            return;
        }

        if (host.isEmpty()) {
            showError("Server IP address is required.");
            hostField.requestFocus();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Port must be a number between 1 and 65535.");
            portField.requestFocus();
            return;
        }

        setLoading(true);
        statusLabel.setText("Connecting to " + host + ":" + port + "...");
        statusLabel.setForeground(UiUtils.COLOR_NAVY_PRIMARY);

        new Thread(() -> {
            try {
                if (client != null) {
                    client.disconnect();
                }
                client = new NetworkClient(host, port);
                client.addListener(this);
                client.connect();

                client.send(NetworkMessage.login(username));
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    setLoading(false);
                    showError("Connection failed: " + e.getMessage());
                });
            }
        }, "Connect-Attempt-Thread").start();
    }

    private void setLoading(boolean loading) {
        connectButton.setEnabled(!loading);
        exitButton.setEnabled(!loading);
        usernameField.setEnabled(!loading);
        hostField.setEnabled(!loading);
        portField.setEnabled(!loading);
        connectButton.setText(loading ? "Connecting..." : "Connect");
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(new Color(185, 28, 28));
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onLoginSuccess(String username, String welcomeNote) {
        SwingUtilities.invokeLater(() -> {
            client.removeListener(this);
            dispose();
            MainChatFrame mainFrame = new MainChatFrame(client, username);
            mainFrame.setVisible(true);
        });
    }

    @Override
    public void onLoginFailure(String reason) {
        SwingUtilities.invokeLater(() -> {
            setLoading(false);
            showError("Login rejected: " + reason);
            if (client != null) {
                client.disconnect();
            }
        });
    }

    @Override
    public void onDisconnected(String reason) {
        SwingUtilities.invokeLater(() -> {
            setLoading(false);
            showError("Disconnected: " + reason);
        });
    }

    @Override public void onOnlineUsersReceived(List<String> users) {}
    @Override public void onUserJoined(String username) {}
    @Override public void onUserLeft(String username) {}
    @Override public void onPrivateMessageReceived(ChatMessage message) {}
    @Override public void onGroupMessageReceived(ChatMessage message) {}
    @Override public void onBroadcastMessageReceived(ChatMessage message) {}
    @Override public void onGroupCreated(GroupInfo group) {}
    @Override public void onGroupListReceived(List<GroupInfo> groups) {}
    @Override public void onChatHistoryReceived(String chatType, String targetId, List<ChatMessage> history) {}
    @Override public void onError(String errorMessage) {
        SwingUtilities.invokeLater(() -> showError(errorMessage));
    }

    private static String detectDefaultServerIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
