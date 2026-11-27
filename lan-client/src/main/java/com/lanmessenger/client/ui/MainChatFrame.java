package com.lanmessenger.client.ui;

import com.lanmessenger.client.model.Conversation;
import com.lanmessenger.client.model.ConversationType;
import com.lanmessenger.client.net.ClientNetworkListener;
import com.lanmessenger.client.net.NetworkClient;
import com.lanmessenger.common.model.ChatMessage;
import com.lanmessenger.common.model.GroupInfo;
import com.lanmessenger.common.model.MessageType;
import com.lanmessenger.common.model.NetworkMessage;
import com.lanmessenger.common.util.NetworkConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Main enterprise desktop window coordinating menu bar, toolbar, directory
 * sidebar,
 * message conversation panel, and status bar.
 */
public class MainChatFrame extends JFrame implements ClientNetworkListener {

    private final NetworkClient client;
    private final String currentUsername;

    private SidebarPanel sidebarPanel;
    private ChatConversationPanel chatConversationPanel;
    private JLabel statusTextLabel;
    private JLabel connectionTextLabel;

    public MainChatFrame(NetworkClient client, String currentUsername) {
        this.client = client;
        this.currentUsername = currentUsername;

        initializeUi();
        client.addListener(this);

        // Fetch initial online directory and user groups
        client.send(new NetworkMessage(MessageType.ONLINE_USERS));

        // Request default broadcast chat history
        client.send(NetworkMessage.chatHistoryRequest(currentUsername, "BROADCAST", NetworkConstants.BROADCAST_TARGET));
    }

    private void initializeUi() {
        setTitle("LAN Messenger — " + currentUsername + " [" + client.getHost() + ":" + client.getPort() + "]");
        setSize(960, 640);
        setMinimumSize(new Dimension(820, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleDisconnect();
            }
        });

        // 1. Traditional Desktop Menu Bar
        setJMenuBar(createMenuBar());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiUtils.COLOR_BG_WHITE);

        // 2. Compact Desktop Toolbar
        JPanel toolbar = createToolbar();
        root.add(toolbar, BorderLayout.NORTH);

        // 3. Central Split Pane (Sidebar + Chat Area)
        sidebarPanel = new SidebarPanel(currentUsername, this::onConversationSelected, this::promptCreateGroup);
        chatConversationPanel = new ChatConversationPanel(currentUsername, this::onSendMessage, this::onSendAttachment);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, chatConversationPanel);
        splitPane.setDividerLocation(260);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        root.add(splitPane, BorderLayout.CENTER);

        // 4. Desktop Status Bar
        JPanel statusBar = createStatusBar();
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);

        // Select broadcast by default
        Conversation broadcast = sidebarPanel.getSelectedConversation();
        if (broadcast != null) {
            chatConversationPanel.setConversation(broadcast);
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(UiUtils.FONT_BODY);

        JMenuItem newGroupItem = new JMenuItem("New Group...");
        newGroupItem.addActionListener(e -> promptCreateGroup());
        fileMenu.add(newGroupItem);

        fileMenu.addSeparator();

        JMenuItem disconnectItem = new JMenuItem("Disconnect");
        disconnectItem.addActionListener(e -> handleDisconnect());
        fileMenu.add(disconnectItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(UiUtils.FONT_BODY);
        JMenuItem refreshItem = new JMenuItem("Refresh Directory");
        refreshItem.addActionListener(e -> client.send(new NetworkMessage(MessageType.ONLINE_USERS)));
        viewMenu.add(refreshItem);
        menuBar.add(viewMenu);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(UiUtils.FONT_BODY);
        JMenuItem aboutItem = new JMenuItem("About LAN Messenger");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "LAN Desktop Messaging Application v1.0.0\n" +
                        "Architecture: Java 17, Java Swing, TCP Sockets, SQLite, Jackson\n" +
                        "Enterprise Desktop Light Theme Edition",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(UiUtils.COLOR_PANEL_GREY);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiUtils.COLOR_BORDER_GREY),
                new EmptyBorder(6, 10, 6, 10)));

        // Left info
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel userBadge = new JLabel("User: " + currentUsername);
        userBadge.setFont(UiUtils.FONT_TITLE);
        userBadge.setForeground(UiUtils.COLOR_NAVY_PRIMARY);
        left.add(userBadge);

        connectionTextLabel = new JLabel("| Server: " + client.getHost() + ":" + client.getPort() + " [Connected]");
        connectionTextLabel.setFont(UiUtils.FONT_BODY);
        connectionTextLabel.setForeground(UiUtils.COLOR_TEXT_SECONDARY);
        left.add(connectionTextLabel);

        bar.add(left, BorderLayout.WEST);

        // Right actions
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        JButton newGrpBtn = new JButton("+ New Group");
        newGrpBtn.setFont(UiUtils.FONT_SMALL);
        newGrpBtn.setBackground(UiUtils.COLOR_BG_WHITE);
        newGrpBtn.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
        newGrpBtn.setFocusPainted(false);
        newGrpBtn.setPreferredSize(new Dimension(90, 24));
        newGrpBtn.addActionListener(e -> promptCreateGroup());
        right.add(newGrpBtn);

        JButton disconnectBtn = new JButton("Disconnect");
        disconnectBtn.setFont(UiUtils.FONT_SMALL);
        disconnectBtn.setBackground(UiUtils.COLOR_BG_WHITE);
        disconnectBtn.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
        disconnectBtn.setFocusPainted(false);
        disconnectBtn.setPreferredSize(new Dimension(80, 24));
        disconnectBtn.addActionListener(e -> handleDisconnect());
        right.add(disconnectBtn);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout(8, 0));
        statusBar.setBackground(UiUtils.COLOR_PANEL_GREY);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtils.COLOR_BORDER_GREY),
                new EmptyBorder(3, 8, 3, 8)));

        statusTextLabel = new JLabel(
                "Status: Connected to " + client.getHost() + ":" + client.getPort() + " | Session Active");
        statusTextLabel.setFont(UiUtils.FONT_SMALL);
        statusTextLabel.setForeground(UiUtils.COLOR_TEXT_SECONDARY);
        statusBar.add(statusTextLabel, BorderLayout.WEST);

        JLabel envLabel = new JLabel("LAN TCP/IP | SQLite DB");
        envLabel.setFont(UiUtils.FONT_SMALL);
        envLabel.setForeground(UiUtils.COLOR_TEXT_MUTED);
        statusBar.add(envLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void onConversationSelected(Conversation conv) {
        chatConversationPanel.setConversation(conv);

        if (!conv.isHistoryLoaded()) {
            String typeStr = conv.getType().name();
            String targetId = conv.getId();
            client.send(NetworkMessage.chatHistoryRequest(currentUsername, typeStr, targetId));
        }
    }

    private void onSendMessage(String text) {
        Conversation current = chatConversationPanel.getCurrentConversation();
        if (current == null || !client.isConnected()) {
            return;
        }

        long now = System.currentTimeMillis();
        ChatMessage localMsg = null;

        if (current.getType() == ConversationType.PRIVATE) {
            String recipient = current.getId();
            NetworkMessage netMsg = NetworkMessage.privateMessage(currentUsername, recipient, text);
            netMsg.setTimestamp(now);
            client.send(netMsg);

            localMsg = new ChatMessage(null, currentUsername, recipient, null, null,
                    MessageType.PRIVATE_MESSAGE, text, now);
        } else if (current.getType() == ConversationType.GROUP) {
            String groupId = current.getId();
            String groupName = current.getTitle();
            NetworkMessage netMsg = NetworkMessage.groupMessage(currentUsername, groupId, groupName, text);
            netMsg.setTimestamp(now);
            client.send(netMsg);

            localMsg = new ChatMessage(null, currentUsername, null, groupId, groupName,
                    MessageType.GROUP_MESSAGE, text, now);
        } else if (current.getType() == ConversationType.BROADCAST) {
            NetworkMessage netMsg = NetworkMessage.broadcastMessage(currentUsername, text);
            netMsg.setTimestamp(now);
            client.send(netMsg);

            localMsg = new ChatMessage(null, currentUsername, null, null, null,
                    MessageType.BROADCAST_MESSAGE, text, now);
        }

        if (localMsg != null) {
            current.addMessage(localMsg);
            chatConversationPanel.appendMessage(localMsg);
        }
    }

    private void onSendAttachment(String caption, java.io.File file) {
        Conversation current = chatConversationPanel.getCurrentConversation();
        if (current == null || !client.isConnected()) {
            return;
        }

        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            String base64Data = java.util.Base64.getEncoder().encodeToString(bytes);
            String text = (caption != null && !caption.isBlank()) ? caption : ("[Attachment: " + file.getName() + "]");
            long now = System.currentTimeMillis();
            ChatMessage localMsg = null;

            if (current.getType() == ConversationType.PRIVATE) {
                String recipient = current.getId();
                NetworkMessage netMsg = NetworkMessage.privateMessage(currentUsername, recipient, text);
                netMsg.setTimestamp(now);
                netMsg.setFileName(file.getName());
                netMsg.setFileSize(file.length());
                netMsg.setFileData(base64Data);
                client.send(netMsg);

                localMsg = new ChatMessage(null, currentUsername, recipient, null, null,
                        MessageType.PRIVATE_MESSAGE, text, now);
                localMsg.setFileName(file.getName());
                localMsg.setFileSize(file.length());
                localMsg.setFileData(base64Data);
            } else if (current.getType() == ConversationType.GROUP) {
                String groupId = current.getId();
                String groupName = current.getTitle();
                NetworkMessage netMsg = NetworkMessage.groupMessage(currentUsername, groupId, groupName, text);
                netMsg.setTimestamp(now);
                netMsg.setFileName(file.getName());
                netMsg.setFileSize(file.length());
                netMsg.setFileData(base64Data);
                client.send(netMsg);

                localMsg = new ChatMessage(null, currentUsername, null, groupId, groupName,
                        MessageType.GROUP_MESSAGE, text, now);
                localMsg.setFileName(file.getName());
                localMsg.setFileSize(file.length());
                localMsg.setFileData(base64Data);
            } else if (current.getType() == ConversationType.BROADCAST) {
                NetworkMessage netMsg = NetworkMessage.broadcastMessage(currentUsername, text);
                netMsg.setTimestamp(now);
                netMsg.setFileName(file.getName());
                netMsg.setFileSize(file.length());
                netMsg.setFileData(base64Data);
                client.send(netMsg);

                localMsg = new ChatMessage(null, currentUsername, null, null, null,
                        MessageType.BROADCAST_MESSAGE, text, now);
                localMsg.setFileName(file.getName());
                localMsg.setFileSize(file.length());
                localMsg.setFileData(base64Data);
            }

            if (localMsg != null) {
                current.addMessage(localMsg);
                chatConversationPanel.appendMessage(localMsg);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to read attachment file: " + e.getMessage(), "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void promptCreateGroup() {
        List<String> availableUsers = sidebarPanel.getAllKnownUsers();
        CreateGroupDialog dialog = new CreateGroupDialog(this, currentUsername, availableUsers);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String groupName = dialog.getGroupName();
            List<String> selectedMembers = dialog.getSelectedMembers();
            selectedMembers.add(currentUsername);

            NetworkMessage createMsg = NetworkMessage.createGroup(currentUsername, groupName, selectedMembers);
            client.send(createMsg);
        }
    }

    private void handleDisconnect() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to disconnect from the server?",
                "Disconnect Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            client.disconnect("User logged out");
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    // --- ClientNetworkListener Implementation ---

    @Override
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            connectionTextLabel.setText("| Server: " + client.getHost() + ":" + client.getPort() + " [Connected]");
            statusTextLabel
                    .setText("Status: Connected to " + client.getHost() + ":" + client.getPort() + " | Session Active");
        });
    }

    @Override
    public void onDisconnected(String reason) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Disconnected from server:\n" + reason, "Connection Terminated",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    @Override
    public void onLoginSuccess(String username, String welcomeNote) {
    }

    @Override
    public void onLoginFailure(String reason) {
    }

    @Override
    public void onOnlineUsersReceived(List<String> users) {
        SwingUtilities.invokeLater(() -> sidebarPanel.syncOnlineUsers(users));
    }

    @Override
    public void onUserJoined(String username) {
        SwingUtilities.invokeLater(() -> {
            sidebarPanel.updateOnlineStatus(username, true);
            Conversation active = chatConversationPanel.getCurrentConversation();
            if (active != null && active.getId().equalsIgnoreCase(username)) {
                active.setOnline(true);
                chatConversationPanel.setConversation(active);
            }
        });
    }

    @Override
    public void onUserLeft(String username) {
        SwingUtilities.invokeLater(() -> {
            sidebarPanel.updateOnlineStatus(username, false);
            Conversation active = chatConversationPanel.getCurrentConversation();
            if (active != null && active.getId().equalsIgnoreCase(username)) {
                active.setOnline(false);
                chatConversationPanel.setConversation(active);
            }
        });
    }

    @Override
    public void onPrivateMessageReceived(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            String sender = message.getSender();
            Conversation conv = sidebarPanel.getConversation(sender);
            if (conv == null) {
                conv = Conversation.createPrivate(sender, true);
                sidebarPanel.addOrUpdateConversation(conv);
            }

            conv.addMessage(message);

            Conversation active = chatConversationPanel.getCurrentConversation();
            if (active != null && active.getId().equalsIgnoreCase(sender)) {
                chatConversationPanel.appendMessage(message);
            } else {
                conv.incrementUnread();
                sidebarPanel.refreshList();
            }

            UiUtils.playNotificationSound();
        });
    }

    @Override
    public void onGroupMessageReceived(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            String groupId = message.getGroupId();
            Conversation conv = sidebarPanel.getConversation(groupId);
            if (conv != null) {
                conv.addMessage(message);
                Conversation active = chatConversationPanel.getCurrentConversation();
                if (active != null && active.getId().equals(groupId)) {
                    chatConversationPanel.appendMessage(message);
                } else {
                    conv.incrementUnread();
                    sidebarPanel.refreshList();
                }
                UiUtils.playNotificationSound();
            }
        });
    }

    @Override
    public void onBroadcastMessageReceived(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            Conversation broadcast = sidebarPanel.getConversation(NetworkConstants.BROADCAST_TARGET);
            if (broadcast != null) {
                broadcast.addMessage(message);
                Conversation active = chatConversationPanel.getCurrentConversation();
                if (active != null && active.getId().equals(NetworkConstants.BROADCAST_TARGET)) {
                    chatConversationPanel.appendMessage(message);
                } else {
                    broadcast.incrementUnread();
                    sidebarPanel.refreshList();
                }
                UiUtils.playNotificationSound();
            }
        });
    }

    @Override
    public void onGroupCreated(GroupInfo group) {
        SwingUtilities.invokeLater(() -> {
            Conversation conv = Conversation.createGroup(group);
            sidebarPanel.addOrUpdateConversation(conv);
            sidebarPanel.selectConversation(conv);
        });
    }

    @Override
    public void onGroupListReceived(List<GroupInfo> groups) {
        SwingUtilities.invokeLater(() -> {
            if (groups != null) {
                for (GroupInfo g : groups) {
                    Conversation conv = Conversation.createGroup(g);
                    sidebarPanel.addOrUpdateConversation(conv);
                }
            }
        });
    }

    @Override
    public void onChatHistoryReceived(String chatType, String targetId, List<ChatMessage> history) {
        SwingUtilities.invokeLater(() -> {
            Conversation conv = null;
            if ("BROADCAST".equalsIgnoreCase(chatType)) {
                conv = sidebarPanel.getConversation(NetworkConstants.BROADCAST_TARGET);
            } else {
                conv = sidebarPanel.getConversation(targetId);
            }

            if (conv != null) {
                conv.setMessages(history);
                conv.setHistoryLoaded(true);

                Conversation active = chatConversationPanel.getCurrentConversation();
                if (active != null && active.getId().equalsIgnoreCase(conv.getId())) {
                    chatConversationPanel.renderMessages(history);
                }
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(this, errorMessage, "Server Error", JOptionPane.ERROR_MESSAGE));
    }
}
