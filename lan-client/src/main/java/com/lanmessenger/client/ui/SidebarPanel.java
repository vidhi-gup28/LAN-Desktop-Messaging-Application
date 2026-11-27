package com.lanmessenger.client.ui;

import com.lanmessenger.client.model.Conversation;
import com.lanmessenger.client.model.ConversationType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Enterprise desktop navigation sidebar.
 * Features a compact searchable directory of network users, groups, and
 * broadcast channels.
 */
public class SidebarPanel extends JPanel {

    private final String currentUsername;
    private final Consumer<Conversation> onConversationSelected;
    private final Runnable onCreateGroupRequested;

    private final JTextField searchField;
    private final JPanel listContainer;
    private final JScrollPane scrollPane;

    private Conversation selectedConversation;
    private final Map<String, Conversation> allConversations = new LinkedHashMap<>();
    private final Map<String, ConversationRowPanel> itemPanels = new HashMap<>();

    public SidebarPanel(String currentUsername,
            Consumer<Conversation> onConversationSelected,
            Runnable onCreateGroupRequested) {
        this.currentUsername = currentUsername;
        this.onConversationSelected = onConversationSelected;
        this.onCreateGroupRequested = onCreateGroupRequested;

        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(260, 0));
        setMinimumSize(new Dimension(220, 0));
        setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UiUtils.COLOR_BORDER_GREY));

        // 1. Search Bar at Top (Clean rectangular desktop field)
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiUtils.COLOR_BORDER_GREY),
                new EmptyBorder(8, 8, 8, 8)));

        searchField = new JTextField();
        searchField.setFont(UiUtils.FONT_BODY);
        searchField.putClientProperty("JTextField.placeholderText", "Filter users or groups...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1),
                new EmptyBorder(4, 6, 4, 6)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refreshList();
            }

            public void removeUpdate(DocumentEvent e) {
                refreshList();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshList();
            }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        // 2. Scrollable Directory List
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(UiUtils.COLOR_BG_LIGHT_GREY);

        scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        scrollPane.getViewport().setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Initialize Broadcast Channel
        Conversation broadcast = Conversation.createBroadcast();
        allConversations.put(broadcast.getId(), broadcast);
        selectedConversation = broadcast;
        refreshList();
    }

    public synchronized void addOrUpdateConversation(Conversation conversation) {
        allConversations.put(conversation.getId(), conversation);
        refreshList();
    }

    public synchronized void updateOnlineStatus(String username, boolean isOnline) {
        if (username.equalsIgnoreCase(currentUsername))
            return;

        Conversation conv = allConversations.get(username);
        if (conv != null) {
            conv.setOnline(isOnline);
        } else if (isOnline) {
            conv = Conversation.createPrivate(username, true);
            allConversations.put(username, conv);
        }
        refreshList();
    }

    public synchronized void syncOnlineUsers(List<String> onlineUsers) {
        Set<String> onlineSet = new HashSet<>();
        for (String u : onlineUsers) {
            onlineSet.add(u.toLowerCase());
        }

        for (Conversation c : allConversations.values()) {
            if (c.getType() == ConversationType.PRIVATE) {
                c.setOnline(onlineSet.contains(c.getId().toLowerCase()));
            }
        }

        for (String user : onlineUsers) {
            if (!user.equalsIgnoreCase(currentUsername) && !allConversations.containsKey(user)) {
                allConversations.put(user, Conversation.createPrivate(user, true));
            }
        }
        refreshList();
    }

    public synchronized void selectConversation(Conversation conversation) {
        this.selectedConversation = conversation;
        conversation.clearUnread();
        refreshList();
        if (onConversationSelected != null) {
            onConversationSelected.accept(conversation);
        }
    }

    public synchronized Conversation getConversation(String id) {
        return allConversations.get(id);
    }

    public synchronized List<String> getAllKnownUsers() {
        List<String> users = new ArrayList<>();
        for (Conversation c : allConversations.values()) {
            if (c.getType() == ConversationType.PRIVATE) {
                users.add(c.getId());
            }
        }
        return users;
    }

    public synchronized void refreshList() {
        SwingUtilities.invokeLater(() -> {
            listContainer.removeAll();
            itemPanels.clear();

            String query = searchField.getText().trim().toLowerCase();

            // 1. Broadcast Channel
            Conversation broadcast = allConversations.get("__BROADCAST__");
            if (broadcast != null && (query.isEmpty() || broadcast.getTitle().toLowerCase().contains(query))) {
                addSectionHeader("ANNOUNCEMENT", null);
                ConversationRowPanel panel = new ConversationRowPanel(broadcast);
                listContainer.add(panel);
                itemPanels.put(broadcast.getId(), panel);
            }

            // 2. Groups Section
            List<Conversation> groups = allConversations.values().stream()
                    .filter(c -> c.getType() == ConversationType.GROUP)
                    .filter(c -> query.isEmpty() || c.getTitle().toLowerCase().contains(query))
                    .toList();

            addSectionHeader("GROUPS (" + groups.size() + ")", onCreateGroupRequested);
            if (groups.isEmpty()) {
                JLabel emptyGroup = new JLabel("No active groups");
                emptyGroup.setFont(UiUtils.FONT_SMALL);
                emptyGroup.setForeground(UiUtils.COLOR_TEXT_MUTED);
                emptyGroup.setBorder(new EmptyBorder(4, 12, 6, 12));
                listContainer.add(emptyGroup);
            } else {
                for (Conversation g : groups) {
                    ConversationRowPanel panel = new ConversationRowPanel(g);
                    listContainer.add(panel);
                    itemPanels.put(g.getId(), panel);
                }
            }

            // 3. User Directory
            List<Conversation> directUsers = allConversations.values().stream()
                    .filter(c -> c.getType() == ConversationType.PRIVATE)
                    .filter(c -> query.isEmpty() || c.getTitle().toLowerCase().contains(query))
                    .sorted((a, b) -> {
                        if (a.isOnline() != b.isOnline()) {
                            return a.isOnline() ? -1 : 1;
                        }
                        return a.getTitle().compareToIgnoreCase(b.getTitle());
                    })
                    .toList();

            long onlineCount = directUsers.stream().filter(Conversation::isOnline).count();
            addSectionHeader("CONTACTS (" + onlineCount + " Online)", null);

            if (directUsers.isEmpty()) {
                JLabel emptyUsers = new JLabel("No contacts found");
                emptyUsers.setFont(UiUtils.FONT_SMALL);
                emptyUsers.setForeground(UiUtils.COLOR_TEXT_MUTED);
                emptyUsers.setBorder(new EmptyBorder(4, 12, 6, 12));
                listContainer.add(emptyUsers);
            } else {
                for (Conversation u : directUsers) {
                    ConversationRowPanel panel = new ConversationRowPanel(u);
                    listContainer.add(panel);
                    itemPanels.put(u.getId(), panel);
                }
            }

            listContainer.revalidate();
            listContainer.repaint();
        });
    }

    private void addSectionHeader(String title, Runnable action) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtils.COLOR_PANEL_GREY);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, UiUtils.COLOR_BORDER_SUBTLE),
                new EmptyBorder(4, 8, 4, 8)));
        header.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(UiUtils.COLOR_TEXT_SECONDARY);
        header.add(label, BorderLayout.WEST);

        if (action != null) {
            JButton addBtn = new JButton("+ New");
            addBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            addBtn.setMargin(new Insets(1, 4, 1, 4));
            addBtn.setFocusPainted(false);
            addBtn.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
            addBtn.setBackground(UiUtils.COLOR_BG_WHITE);
            addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addBtn.addActionListener(e -> action.run());
            header.add(addBtn, BorderLayout.EAST);
        }

        listContainer.add(header);
    }

    private class ConversationRowPanel extends JPanel {
        private final Conversation conversation;

        ConversationRowPanel(Conversation conv) {
            this.conversation = conv;
            setLayout(new BorderLayout(8, 0));
            setBorder(new EmptyBorder(6, 8, 6, 8));
            setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            boolean isSelected = (selectedConversation != null && selectedConversation.getId().equals(conv.getId()));
            if (isSelected) {
                setBackground(new Color(224, 231, 255)); // Muted Navy selection tint #E0E7FF
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, UiUtils.COLOR_NAVY_PRIMARY),
                        new EmptyBorder(6, 5, 6, 8)));
            } else {
                setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
            }

            // Left: Status Indicator Dot
            JPanel indicatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
            indicatorPanel.setOpaque(false);
            JLabel dotLabel = new JLabel(conv.isOnline() ? "●" : "○");
            dotLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            dotLabel.setForeground(conv.isOnline() ? UiUtils.COLOR_ONLINE_GREEN : UiUtils.COLOR_OFFLINE_GREY);
            indicatorPanel.add(dotLabel);
            add(indicatorPanel, BorderLayout.WEST);

            // Center: Title & Subtitle
            JPanel textPanel = new JPanel(new BorderLayout(4, 0));
            textPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(conv.getTitle());
            titleLabel.setFont(isSelected ? UiUtils.FONT_TITLE : UiUtils.FONT_BODY);
            titleLabel.setForeground(isSelected ? UiUtils.COLOR_NAVY_PRIMARY : UiUtils.COLOR_TEXT_PRIMARY);
            textPanel.add(titleLabel, BorderLayout.CENTER);

            add(textPanel, BorderLayout.CENTER);

            // Right: Unread Counter Badge (Simple rectangular badge)
            if (conv.getUnreadCount() > 0) {
                JLabel badge = new JLabel(" " + conv.getUnreadCount() + " ");
                badge.setOpaque(true);
                badge.setBackground(UiUtils.COLOR_BADGE_BG);
                badge.setForeground(Color.WHITE);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setBorder(BorderFactory.createLineBorder(new Color(185, 28, 28), 1));
                JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
                badgeWrapper.setOpaque(false);
                badgeWrapper.add(badge);
                add(badgeWrapper, BorderLayout.EAST);
            }

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectConversation(conv);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected) {
                        setBackground(new Color(235, 238, 242));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isSelected) {
                        setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
                    }
                }
            });
        }
    }

    public Conversation getSelectedConversation() {
        return selectedConversation;
    }
}
