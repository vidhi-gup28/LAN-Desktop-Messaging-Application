package com.lanmessenger.client.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise modal dialog for creating a new group conversation.
 */
public class CreateGroupDialog extends JDialog {

    private final JTextField groupNameField;
    private final JPanel membersPanel;
    private final List<JCheckBox> memberCheckboxes = new ArrayList<>();
    private boolean confirmed = false;

    public CreateGroupDialog(Frame parent, String currentUsername, List<String> availableUsers) {
        super(parent, "Create New Group", true);
        setSize(380, 420);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(UiUtils.COLOR_BG_WHITE);
        content.setBorder(new EmptyBorder(14, 16, 14, 16));

        // 1. Group Name Section
        JPanel namePanel = new JPanel(new BorderLayout(0, 4));
        namePanel.setBackground(UiUtils.COLOR_BG_WHITE);

        JLabel lblName = new JLabel("Group Name:");
        lblName.setFont(UiUtils.FONT_TITLE);
        namePanel.add(lblName, BorderLayout.NORTH);

        groupNameField = new JTextField();
        groupNameField.setFont(UiUtils.FONT_BODY);
        groupNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));
        namePanel.add(groupNameField, BorderLayout.CENTER);
        content.add(namePanel, BorderLayout.NORTH);

        // 2. Member Selection Checklist
        JPanel selectionContainer = new JPanel(new BorderLayout(0, 4));
        selectionContainer.setBackground(UiUtils.COLOR_BG_WHITE);

        JLabel lblMembers = new JLabel("Select Members:");
        lblMembers.setFont(UiUtils.FONT_TITLE);
        selectionContainer.add(lblMembers, BorderLayout.NORTH);

        membersPanel = new JPanel();
        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));
        membersPanel.setBackground(UiUtils.COLOR_BG_WHITE);

        for (String user : availableUsers) {
            if (user.equalsIgnoreCase(currentUsername)) {
                continue;
            }
            JCheckBox cb = new JCheckBox(user);
            cb.setFont(UiUtils.FONT_BODY);
            cb.setBackground(UiUtils.COLOR_BG_WHITE);
            cb.setFocusPainted(false);
            memberCheckboxes.add(cb);
            membersPanel.add(cb);
        }

        if (memberCheckboxes.isEmpty()) {
            JLabel emptyLabel = new JLabel("No other contacts detected.");
            emptyLabel.setFont(UiUtils.FONT_SMALL);
            emptyLabel.setForeground(UiUtils.COLOR_TEXT_MUTED);
            emptyLabel.setBorder(new EmptyBorder(8, 4, 8, 4));
            membersPanel.add(emptyLabel);
        }

        JScrollPane scrollPane = new JScrollPane(membersPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
        scrollPane.getViewport().setBackground(UiUtils.COLOR_BG_WHITE);
        selectionContainer.add(scrollPane, BorderLayout.CENTER);

        // Quick Select buttons
        JPanel quickSelect = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        quickSelect.setBackground(UiUtils.COLOR_BG_WHITE);

        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.setFont(UiUtils.FONT_SMALL);
        selectAllBtn.setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        selectAllBtn.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
        selectAllBtn.setFocusPainted(false);
        selectAllBtn.addActionListener(e -> memberCheckboxes.forEach(cb -> cb.setSelected(true)));

        JButton clearAllBtn = new JButton("Clear All");
        clearAllBtn.setFont(UiUtils.FONT_SMALL);
        clearAllBtn.setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        clearAllBtn.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
        clearAllBtn.setFocusPainted(false);
        clearAllBtn.addActionListener(e -> memberCheckboxes.forEach(cb -> cb.setSelected(false)));

        quickSelect.add(selectAllBtn);
        quickSelect.add(clearAllBtn);
        selectionContainer.add(quickSelect, BorderLayout.SOUTH);

        content.add(selectionContainer, BorderLayout.CENTER);

        // 3. Action Buttons (Rectangular enterprise buttons)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UiUtils.COLOR_BG_WHITE);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(UiUtils.FONT_BODY);
        cancelBtn.setBackground(UiUtils.COLOR_BG_LIGHT_GREY);
        cancelBtn.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setPreferredSize(new Dimension(75, 28));
        cancelBtn.addActionListener(e -> dispose());
        actions.add(cancelBtn);

        JButton createBtn = new JButton("Create");
        createBtn.setFont(UiUtils.FONT_TITLE);
        createBtn.setBackground(UiUtils.COLOR_NAVY_PRIMARY);
        createBtn.setForeground(Color.WHITE);
        createBtn.setBorder(BorderFactory.createLineBorder(new Color(23, 37, 84), 1));
        createBtn.setFocusPainted(false);
        createBtn.setPreferredSize(new Dimension(80, 28));
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createBtn.addActionListener(e -> {
            String name = groupNameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Group name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });
        actions.add(createBtn);

        content.add(actions, BorderLayout.SOUTH);
        setContentPane(content);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getGroupName() {
        return groupNameField.getText().trim();
    }

    public List<String> getSelectedMembers() {
        List<String> selected = new ArrayList<>();
        for (JCheckBox cb : memberCheckboxes) {
            if (cb.isSelected()) {
                selected.add(cb.getText());
            }
        }
        return selected;
    }
}
