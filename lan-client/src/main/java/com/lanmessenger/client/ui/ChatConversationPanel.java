package com.lanmessenger.client.ui;

import com.lanmessenger.client.model.Conversation;
import com.lanmessenger.client.model.ConversationType;
import com.lanmessenger.common.model.ChatMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Enterprise desktop conversation panel.
 * Clean white message stream, structured header, and rectangular input controls.
 */
public class ChatConversationPanel extends JPanel {

    private final String currentUsername;
    private final Consumer<String> onSendMessage;
    private final java.util.function.BiConsumer<String, java.io.File> onSendAttachment;

    private Conversation currentConversation;

    // Header components
    private JLabel titleLabel;
    private JLabel statusLabel;

    // Messages area
    private JPanel messageListPanel;
    private JScrollPane scrollPane;

    // Input area
    private JTextArea inputArea;
    private JButton sendButton;
    private JButton attachButton;

    public ChatConversationPanel(String currentUsername, Consumer<String> onSendMessage) {
        this(currentUsername, onSendMessage, null);
    }

    public ChatConversationPanel(String currentUsername, Consumer<String> onSendMessage,
                                 java.util.function.BiConsumer<String, java.io.File> onSendAttachment) {
        this.currentUsername = currentUsername;
        this.onSendMessage = onSendMessage;
        this.onSendAttachment = onSendAttachment;

        setLayout(new BorderLayout());
        setBackground(UiUtils.COLOR_BG_WHITE);
        setBorder(BorderFactory.createEmptyBorder());

        // 1. Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 2. Message History Area (White background)
        messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setBackground(UiUtils.COLOR_BG_WHITE);
        messageListPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(UiUtils.COLOR_BG_WHITE);
        scrollPane.getViewport().setBackground(UiUtils.COLOR_BG_WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Message Input & Send Panel
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiUtils.COLOR_BORDER_GREY),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        titleLabel = new JLabel("No conversation selected");
        titleLabel.setFont(UiUtils.FONT_HEADER);
        titleLabel.setForeground(UiUtils.COLOR_TEXT_PRIMARY);
        textPanel.add(titleLabel);

        statusLabel = new JLabel("Select a contact or channel from the sidebar");
        statusLabel.setFont(UiUtils.FONT_SMALL);
        statusLabel.setForeground(UiUtils.COLOR_TEXT_SECONDARY);
        textPanel.add(statusLabel);

        header.add(textPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtils.COLOR_BORDER_GREY),
                new EmptyBorder(8, 10, 8, 10)
        ));

        inputArea = new JTextArea(2, 40);
        inputArea.setFont(UiUtils.FONT_BODY);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) {
                        inputArea.append("\n");
                    } else {
                        e.consume();
                        handleSend();
                    }
                }
            }
        });

        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(inputScroll, BorderLayout.CENTER);

        // Action Buttons: Attach File + Send Message
        JPanel actionBtnGroup = new JPanel(new GridLayout(2, 1, 0, 4));
        actionBtnGroup.setOpaque(false);

        attachButton = new JButton("📎 Attach");
        attachButton.setFont(UiUtils.FONT_SMALL);
        attachButton.setBackground(new Color(243, 244, 246));
        attachButton.setForeground(UiUtils.COLOR_NAVY_PRIMARY);
        attachButton.setFocusPainted(false);
        attachButton.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
        attachButton.setPreferredSize(new Dimension(82, 22));
        attachButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        attachButton.setToolTipText("Share an Image, Video, or Document");
        attachButton.addActionListener(e -> handleAttachFile());
        actionBtnGroup.add(attachButton);

        sendButton = new JButton("Send");
        sendButton.setFont(UiUtils.FONT_TITLE);
        sendButton.setBackground(UiUtils.COLOR_NAVY_PRIMARY);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createLineBorder(new Color(23, 37, 84), 1));
        sendButton.setPreferredSize(new Dimension(82, 26));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> handleSend());
        actionBtnGroup.add(sendButton);

        panel.add(actionBtnGroup, BorderLayout.EAST);

        // Subtitle note
        JLabel hintLabel = new JLabel("Press Enter to send, Shift+Enter for new line | Click Attach for images, videos & files");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hintLabel.setForeground(UiUtils.COLOR_TEXT_MUTED);
        panel.add(hintLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void handleAttachFile() {
        if (currentConversation == null) {
            JOptionPane.showMessageDialog(this, "Select a contact or conversation first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Image, Video, or Document to Send");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            if (file != null && file.exists()) {
                long size = file.length();
                if (size > 25 * 1024 * 1024) {
                    JOptionPane.showMessageDialog(this,
                            "File size (" + (size / (1024 * 1024)) + " MB) exceeds the 25 MB direct LAN transfer limit.",
                            "File Too Large", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String caption = inputArea.getText().trim();
                inputArea.setText("");
                if (onSendAttachment != null) {
                    onSendAttachment.accept(caption, file);
                }
            }
        }
    }

    private void handleSend() {
        String text = inputArea.getText().trim();
        if (text.isEmpty() || currentConversation == null) {
            return;
        }
        inputArea.setText("");
        if (onSendMessage != null) {
            onSendMessage.accept(text);
        }
    }

    public synchronized void setConversation(Conversation conv) {
        this.currentConversation = conv;
        if (conv == null) {
            titleLabel.setText("No Conversation Selected");
            statusLabel.setText("");
            messageListPanel.removeAll();
            messageListPanel.revalidate();
            messageListPanel.repaint();
            inputArea.setEnabled(false);
            sendButton.setEnabled(false);
            if (attachButton != null) attachButton.setEnabled(false);
            return;
        }

        inputArea.setEnabled(true);
        sendButton.setEnabled(true);
        if (attachButton != null) attachButton.setEnabled(true);

        titleLabel.setText(conv.getTitle());

        if (conv.getType() == ConversationType.BROADCAST) {
            statusLabel.setText("System Channel • Broadcast to all LAN network computers");
            statusLabel.setForeground(UiUtils.COLOR_NAVY_PRIMARY);
        } else if (conv.getType() == ConversationType.GROUP) {
            int members = conv.getGroupInfo() != null ? conv.getGroupInfo().getMembers().size() : 0;
            statusLabel.setText("Group Conversation • " + members + " members");
            statusLabel.setForeground(UiUtils.COLOR_TEXT_SECONDARY);
        } else {
            statusLabel.setText(conv.isOnline() ? "● Online (Active session)" : "○ Offline (Messages stored in database)");
            statusLabel.setForeground(conv.isOnline() ? UiUtils.COLOR_ONLINE_GREEN : UiUtils.COLOR_OFFLINE_GREY);
        }

        renderMessages(conv.getMessages());
    }

    public synchronized void appendMessage(ChatMessage msg) {
        if (currentConversation == null) return;

        boolean isOutgoing = msg.getSender() != null && msg.getSender().equalsIgnoreCase(currentUsername);
        boolean showSender = currentConversation.getType() != ConversationType.PRIVATE;

        MessageBubblePanel bubble = new MessageBubblePanel(msg, isOutgoing, showSender);
        messageListPanel.add(bubble);
        messageListPanel.revalidate();
        messageListPanel.repaint();

        scrollToBottom();
    }

    public synchronized void renderMessages(List<ChatMessage> messages) {
        messageListPanel.removeAll();

        if (messages == null || messages.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(UiUtils.COLOR_BG_WHITE);
            JLabel emptyLabel = new JLabel("No messages in this conversation. Send a message to start.");
            emptyLabel.setFont(UiUtils.FONT_BODY);
            emptyLabel.setForeground(UiUtils.COLOR_TEXT_MUTED);
            emptyPanel.add(emptyLabel);
            messageListPanel.add(emptyPanel);
        } else {
            for (ChatMessage msg : messages) {
                boolean isOutgoing = msg.getSender() != null && msg.getSender().equalsIgnoreCase(currentUsername);
                boolean showSender = currentConversation.getType() != ConversationType.PRIVATE;
                messageListPanel.add(new MessageBubblePanel(msg, isOutgoing, showSender));
            }
        }

        messageListPanel.revalidate();
        messageListPanel.repaint();
        scrollToBottom();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public Conversation getCurrentConversation() {
        return currentConversation;
    }
}
