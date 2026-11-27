package com.lanmessenger.server.ui;

import com.lanmessenger.common.model.NetworkMessage;
import com.lanmessenger.server.net.ClientHandler;
import com.lanmessenger.server.net.LanServer;
import com.lanmessenger.server.net.ServerEventListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * Enterprise desktop Server Control Dashboard.
 * Clean professional light theme, rectangular controls, and standard desktop administrative views.
 */
public class ServerConsoleFrame extends JFrame implements ServerEventListener {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private LanServer server;
    private int port = 5000;
    private String dbPath = "lan_messenger.db";

    // UI components
    private JLabel statusBadge;
    private JTextField portField;
    private JButton startStopBtn;
    private JTable clientsTable;
    private DefaultTableModel clientsModel;
    private JTextArea logArea;
    private JTextField broadcastField;
    private JButton sendBroadcastBtn;

    public ServerConsoleFrame(int port, String dbPath) {
        this.port = port;
        this.dbPath = dbPath;
        initializeUi();
        startServer();
    }

    private void initializeUi() {
        setTitle("LAN Messaging Server — Administration Console");
        setSize(880, 640);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(new Color(248, 249, 250));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        // 1. Top Status & Configuration Banner
        JPanel topPanel = createTopPanel();
        root.add(topPanel, BorderLayout.NORTH);

        // 2. Central Split Pane (Active Clients Table & Server Event Log)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.42);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel clientsPanel = createClientsPanel();
        splitPane.setTopComponent(clientsPanel);

        JPanel bottomPanel = createLogsAndBroadcastPanel();
        splitPane.setBottomComponent(bottomPanel);

        root.add(splitPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Top line: Title & Status
        JPanel headerLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLine.setOpaque(false);

        JLabel title = new JLabel("LAN Messaging Server");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(30, 58, 138)); // Navy
        headerLine.add(title);

        statusBadge = new JLabel(" STOPPED ");
        statusBadge.setOpaque(true);
        statusBadge.setBackground(new Color(220, 38, 38));
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        headerLine.add(statusBadge);

        panel.add(headerLine, BorderLayout.NORTH);

        // Center line: IP addresses & Port controls
        JPanel detailsPanel = new JPanel(new BorderLayout(8, 0));
        detailsPanel.setOpaque(false);

        String lanIps = detectLanIpv4Addresses();
        JLabel ipLabel = new JLabel("<html><b>Host LAN IPv4:</b> <font color='#1E40AF'>" + lanIps + "</font> (Port: " + port + ")</html>");
        ipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsPanel.add(ipLabel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);

        JButton copyIpBtn = new JButton("Copy IP");
        copyIpBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        copyIpBtn.setBackground(new Color(243, 244, 246));
        copyIpBtn.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        copyIpBtn.setFocusPainted(false);
        copyIpBtn.setPreferredSize(new Dimension(75, 24));
        copyIpBtn.addActionListener(e -> {
            String firstIp = getFirstLanIp();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(firstIp), null);
            JOptionPane.showMessageDialog(this, "Copied IP " + firstIp + " to clipboard.", "Clipboard", JOptionPane.INFORMATION_MESSAGE);
        });
        controls.add(copyIpBtn);

        controls.add(new JLabel("Port:"));
        portField = new JTextField(String.valueOf(port), 4);
        portField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        portField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        controls.add(portField);

        startStopBtn = new JButton("Stop Server");
        startStopBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        startStopBtn.setBackground(new Color(220, 38, 38));
        startStopBtn.setForeground(Color.WHITE);
        startStopBtn.setBorder(BorderFactory.createLineBorder(new Color(185, 28, 28), 1));
        startStopBtn.setFocusPainted(false);
        startStopBtn.setPreferredSize(new Dimension(85, 24));
        startStopBtn.addActionListener(e -> toggleServer());
        controls.add(startStopBtn);

        detailsPanel.add(controls, BorderLayout.EAST);
        panel.add(detailsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel title = new JLabel("Connected Client Sessions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(new Color(30, 41, 59));
        panel.add(title, BorderLayout.NORTH);

        clientsModel = new DefaultTableModel(new Object[]{"Username", "Remote IP & Port", "Connected Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        clientsTable = new JTable(clientsModel);
        clientsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clientsTable.setRowHeight(22);
        clientsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        clientsTable.getTableHeader().setReorderingAllowed(false);
        clientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientsTable.setGridColor(new Color(229, 231, 235));

        JScrollPane scrollPane = new JScrollPane(clientsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        btnBar.setOpaque(false);

        JButton disconnectBtn = new JButton("Disconnect Selected");
        disconnectBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        disconnectBtn.setBackground(new Color(243, 244, 246));
        disconnectBtn.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        disconnectBtn.setFocusPainted(false);
        disconnectBtn.addActionListener(e -> {
            int selectedRow = clientsTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) clientsModel.getValueAt(selectedRow, 0);
                if (server != null) {
                    ClientHandler handler = server.getClientManager().getClient(username);
                    if (handler != null) {
                        handler.close();
                        log("INFO", "Administrator terminated session for '" + username + "'.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a client row from the table first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnBar.add(disconnectBtn);

        JButton clearDbBtn = new JButton("Clear History & Reset Users");
        clearDbBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearDbBtn.setBackground(new Color(254, 242, 242));
        clearDbBtn.setForeground(new Color(185, 28, 28));
        clearDbBtn.setBorder(BorderFactory.createLineBorder(new Color(252, 165, 165), 1));
        clearDbBtn.setFocusPainted(false);
        clearDbBtn.setToolTipText("Deletes all stored chat history, groups, and registered user records from the database.");
        clearDbBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to permanently delete all chat history, groups, and registered users?\nThis will give you a fresh, clean database for new users.",
                    "Confirm Reset History & Users", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION && server != null) {
                try {
                    server.getDatabaseManager().clearAllData();
                    log("INFO", "All chat history, groups, and registered users were cleared from SQLite database by Administrator.");
                    JOptionPane.showMessageDialog(this,
                            "All history and user records have been successfully cleared!\nNew users can now connect with a completely clean slate.",
                            "Database Cleared", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error clearing database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnBar.add(clearDbBtn);

        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogsAndBroadcastPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        // Log container
        JPanel logContainer = new JPanel(new BorderLayout(0, 4));
        logContainer.setBackground(Color.WHITE);
        logContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);
        JLabel logTitle = new JLabel("Server Activity Log");
        logTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logHeader.add(logTitle, BorderLayout.WEST);

        JButton clearBtn = new JButton("Clear Log");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        clearBtn.setBackground(new Color(243, 244, 246));
        clearBtn.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> logArea.setText(""));
        logHeader.add(clearBtn, BorderLayout.EAST);
        logContainer.add(logHeader, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 250, 250));
        logArea.setForeground(new Color(30, 41, 59));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        logContainer.add(logScroll, BorderLayout.CENTER);

        panel.add(logContainer, BorderLayout.CENTER);

        // Bottom: Server Admin Broadcast Bar
        JPanel bcastPanel = new JPanel(new BorderLayout(6, 0));
        bcastPanel.setBackground(Color.WHITE);
        bcastPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel bcastLabel = new JLabel("Broadcast:");
        bcastLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bcastPanel.add(bcastLabel, BorderLayout.WEST);

        broadcastField = new JTextField();
        broadcastField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        broadcastField.putClientProperty("JTextField.placeholderText", "Send an administrative announcement to all connected clients...");
        broadcastField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                new EmptyBorder(3, 6, 3, 6)
        ));
        broadcastField.addActionListener(e -> sendAdminBroadcast());
        bcastPanel.add(broadcastField, BorderLayout.CENTER);

        sendBroadcastBtn = new JButton("Send Announcement");
        sendBroadcastBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sendBroadcastBtn.setBackground(new Color(30, 58, 138));
        sendBroadcastBtn.setForeground(Color.WHITE);
        sendBroadcastBtn.setBorder(BorderFactory.createLineBorder(new Color(23, 37, 84), 1));
        sendBroadcastBtn.setFocusPainted(false);
        sendBroadcastBtn.setPreferredSize(new Dimension(140, 26));
        sendBroadcastBtn.addActionListener(e -> sendAdminBroadcast());
        bcastPanel.add(sendBroadcastBtn, BorderLayout.EAST);

        panel.add(bcastPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void toggleServer() {
        if (server != null && server.isRunning()) {
            stopServer();
        } else {
            try {
                this.port = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            startServer();
        }
    }

    public synchronized void startServer() {
        try {
            if (server != null && server.isRunning()) {
                return;
            }
            server = new LanServer(port, dbPath);
            server.getClientManager().addListener(this);
            server.start();

            statusBadge.setText(" ONLINE ");
            statusBadge.setBackground(new Color(22, 163, 74));
            startStopBtn.setText("Stop Server");
            startStopBtn.setBackground(new Color(220, 38, 38));
            startStopBtn.setBorder(BorderFactory.createLineBorder(new Color(185, 28, 28), 1));
            portField.setEnabled(false);
            log("INFO", "Server bound to 0.0.0.0:" + port);
        } catch (IOException e) {
            log("ERROR", "Failed to start server on port " + port + ": " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Could not start server on port " + port + ":\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public synchronized void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
        statusBadge.setText(" STOPPED ");
        statusBadge.setBackground(new Color(156, 163, 175));
        startStopBtn.setText("Start Server");
        startStopBtn.setBackground(new Color(30, 58, 138));
        startStopBtn.setBorder(BorderFactory.createLineBorder(new Color(23, 37, 84), 1));
        portField.setEnabled(true);
        clientsModel.setRowCount(0);
        log("INFO", "Server stopped.");
    }

    private void sendAdminBroadcast() {
        String text = broadcastField.getText().trim();
        if (text.isEmpty() || server == null || !server.isRunning()) {
            return;
        }

        NetworkMessage bcastMsg = NetworkMessage.broadcastMessage("SERVER_ADMIN", text);
        server.getClientManager().broadcast(bcastMsg);
        log("INFO", "Admin announcement broadcast: " + text);
        broadcastField.setText("");
    }

    public void log(String level, String msg) {
        String timestamp = timeFormat.format(new Date());
        String entry = String.format("[%s] [%s] %s%n", timestamp, level, msg);
        SwingUtilities.invokeLater(() -> {
            logArea.append(entry);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void onServerStarted(String host, int port) {
        log("INFO", "Server started on " + host + ":" + port);
    }

    @Override
    public void onServerStopped() {
        log("INFO", "Server stopped.");
    }

    @Override
    public void onClientConnected(String address) {
        log("INFO", "Inbound TCP connection: " + address);
    }

    @Override
    public void onClientLoggedIn(String username, String address) {
        SwingUtilities.invokeLater(() -> {
            clientsModel.addRow(new Object[]{username, address, timeFormat.format(new Date())});
            log("INFO", "Session authenticated: '" + username + "' from " + address);
        });
    }

    @Override
    public void onClientDisconnected(String username, String address) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < clientsModel.getRowCount(); i++) {
                if (username.equalsIgnoreCase((String) clientsModel.getValueAt(i, 0))) {
                    clientsModel.removeRow(i);
                    break;
                }
            }
            log("INFO", "Session closed: '" + username + "'");
        });
    }

    @Override
    public void onMessageRouted(String type, String sender, String recipientOrGroup) {
        log("DEBUG", "Route [" + type + "]: " + sender + " -> " + recipientOrGroup);
    }

    @Override
    public void onLog(String level, String message) {
        log(level, message);
    }

    public static String detectLanIpv4Addresses() {
        StringBuilder sb = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(addr.getHostAddress());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (sb.length() == 0) {
            sb.append("127.0.0.1 (Local Only)");
        }
        return sb.toString();
    }

    public static String getFirstLanIp() {
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

    public LanServer getServer() {
        return server;
    }
}
