package com.lanmessenger.client.ui;

import com.lanmessenger.common.model.ChatMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enterprise desktop message row.
 * Displays clean rectangular message panels with subtle borders,
 * clear sender hierarchy, timestamps, rich image/video/file attachments,
 * clickable links, and professional enterprise alignment.
 */
public class MessageBubblePanel extends JPanel {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    public MessageBubblePanel(ChatMessage message, boolean isOutgoing, boolean showSenderName) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(4, 10, 4, 10));

        JPanel messageCard = new JPanel();
        messageCard.setLayout(new BoxLayout(messageCard, BoxLayout.Y_AXIS));

        // Colors for enterprise light theme
        Color bg = isOutgoing ? UiUtils.COLOR_OUTGOING_BG : UiUtils.COLOR_INCOMING_BG;
        Color border = isOutgoing ? UiUtils.COLOR_OUTGOING_BORDER : UiUtils.COLOR_BORDER_GREY;

        messageCard.setBackground(bg);
        messageCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1),
                new EmptyBorder(8, 12, 8, 12)));

        // 1. Top line: Sender name (left) + Timestamp (right)
        JPanel metaHeader = new JPanel(new BorderLayout(16, 0));
        metaHeader.setOpaque(false);

        String senderTitle = isOutgoing ? "You" : (message.getSender() != null ? message.getSender() : "Unknown");
        JLabel senderLabel = new JLabel(senderTitle);
        senderLabel.setFont(UiUtils.FONT_TITLE);
        senderLabel.setForeground(isOutgoing ? UiUtils.COLOR_NAVY_PRIMARY : new Color(30, 41, 59));
        metaHeader.add(senderLabel, BorderLayout.WEST);

        String timeStr = UiUtils.formatTimestamp(message.getTimestamp());
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(UiUtils.FONT_SMALL);
        timeLabel.setForeground(UiUtils.COLOR_TEXT_SECONDARY);
        metaHeader.add(timeLabel, BorderLayout.EAST);

        messageCard.add(metaHeader);
        messageCard.add(Box.createVerticalStrut(6));

        int attachmentHeight = 0;
        int maxCardWidth = 560;

        // 2. Attachment Rendering (Image thumbnail or File/Video card)
        if (message.hasAttachment()) {
            JPanel attachPanel = createAttachmentPanel(message);
            messageCard.add(attachPanel);
            messageCard.add(Box.createVerticalStrut(6));
            attachmentHeight = attachPanel.getPreferredSize().height + 6;
        }

        // 3. Text Content Area
        String text = message.getContent() != null ? message.getContent() : "";
        JTextArea contentArea = new JTextArea(text);
        contentArea.setFont(UiUtils.FONT_BODY);
        contentArea.setForeground(UiUtils.COLOR_TEXT_PRIMARY);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setOpaque(false);
        contentArea.setFocusable(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        messageCard.add(contentArea);

        // 4. Clickable Link Detection (if message contains http:// or https://)
        Matcher urlMatcher = URL_PATTERN.matcher(text);
        if (urlMatcher.find()) {
            String url = urlMatcher.group();
            JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
            linkPanel.setOpaque(false);
            JButton linkBtn = new JButton("🔗 Open Link: " + (url.length() > 40 ? url.substring(0, 37) + "..." : url));
            linkBtn.setFont(UiUtils.FONT_SMALL);
            linkBtn.setForeground(new Color(29, 78, 216));
            linkBtn.setBackground(new Color(239, 246, 255));
            linkBtn.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 1));
            linkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            linkBtn.setFocusPainted(false);
            linkBtn.addActionListener(e -> {
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(url));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Could not open URL: " + ex.getMessage(), "Link Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
            linkPanel.add(linkBtn);
            messageCard.add(linkPanel);
            attachmentHeight += linkPanel.getPreferredSize().height + 4;
        }

        // Dynamically compute width and height so card expands properly
        FontMetrics bodyFm = contentArea.getFontMetrics(UiUtils.FONT_BODY);
        FontMetrics titleFm = senderLabel.getFontMetrics(UiUtils.FONT_TITLE);
        FontMetrics smallFm = timeLabel.getFontMetrics(UiUtils.FONT_SMALL);

        int headerMinWidth = titleFm.stringWidth(senderTitle) + smallFm.stringWidth(timeStr) + 40;
        int minCardWidth = Math.max(headerMinWidth, 180);

        String[] lines = text.split("\r?\n");
        int maxLineWidth = 0;
        for (String line : lines) {
            maxLineWidth = Math.max(maxLineWidth, bodyFm.stringWidth(line));
        }

        int headerHeight = metaHeader.getPreferredSize().height;
        int vPadding = 28; // top/bottom insets + vertical struts + safety

        int cardWidth;
        int cardHeight;

        if (message.hasAttachment()) {
            cardWidth = Math.max(340, Math.min(maxLineWidth + 28, maxCardWidth));
            contentArea.setSize(new Dimension(cardWidth - 28, Short.MAX_VALUE));
            cardHeight = headerHeight + contentArea.getPreferredSize().height + attachmentHeight + vPadding;
        } else if (maxLineWidth <= maxCardWidth - 28 && lines.length <= 1) {
            // Single short line
            cardWidth = Math.min(Math.max(maxLineWidth + 28, minCardWidth), maxCardWidth);
            cardHeight = headerHeight + bodyFm.getHeight() + attachmentHeight + vPadding;
        } else if (maxLineWidth <= maxCardWidth - 28) {
            // Multiple short lines
            cardWidth = Math.min(Math.max(maxLineWidth + 28, minCardWidth), maxCardWidth);
            contentArea.setSize(new Dimension(cardWidth - 28, Short.MAX_VALUE));
            int textHeight = contentArea.getPreferredSize().height;
            cardHeight = headerHeight + textHeight + attachmentHeight + vPadding;
        } else {
            // Long wrapping text
            cardWidth = maxCardWidth;
            contentArea.setSize(new Dimension(maxCardWidth - 28, Short.MAX_VALUE));
            int textHeight = contentArea.getPreferredSize().height;
            cardHeight = headerHeight + textHeight + attachmentHeight + vPadding;
        }

        Dimension cardDim = new Dimension(cardWidth, cardHeight);
        messageCard.setPreferredSize(cardDim);
        messageCard.setMinimumSize(cardDim);

        // Alignment: Left-aligned for incoming, Right-aligned for outgoing
        JPanel alignmentWrapper = new JPanel(new FlowLayout(isOutgoing ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignmentWrapper.setOpaque(false);
        alignmentWrapper.add(messageCard);

        add(alignmentWrapper, BorderLayout.CENTER);

        // Set maximum size so BoxLayout.Y_AXIS aligns rows neatly
        setMaximumSize(new Dimension(Short.MAX_VALUE, cardHeight + 8));
    }

    private JPanel createAttachmentPanel(ChatMessage message) {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.setOpaque(false);

        String fileName = message.getFileName() != null ? message.getFileName() : "attachment";
        String formattedSize = formatFileSize(message.getFileSize());

        if (message.isImageAttachment()) {
            try {
                byte[] imgBytes = Base64.getDecoder().decode(message.getFileData());
                ImageIcon rawIcon = new ImageIcon(imgBytes);
                Image img = rawIcon.getImage();
                int w = img.getWidth(null);
                int h = img.getHeight(null);

                if (w > 0 && h > 0) {
                    double scale = Math.min(280.0 / w, 180.0 / h);
                    if (scale < 1.0) {
                        w = (int) (w * scale);
                        h = (int) (h * scale);
                    }
                    Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    JLabel imgLabel = new JLabel(new ImageIcon(scaled));
                    imgLabel.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));
                    panel.add(imgLabel, BorderLayout.CENTER);
                }

                JButton saveBtn = new JButton("💾 Save Image (" + formattedSize + ")");
                saveBtn.setFont(UiUtils.FONT_SMALL);
                saveBtn.setBackground(new Color(243, 244, 246));
                saveBtn.setForeground(UiUtils.COLOR_NAVY_PRIMARY);
                saveBtn.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
                saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                saveBtn.addActionListener(e -> saveAttachmentToFile(fileName, imgBytes));
                panel.add(saveBtn, BorderLayout.SOUTH);

                return panel;
            } catch (Exception ignored) {
            }
        }

        // Generic File / Video Card
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(new Color(243, 244, 246));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                new EmptyBorder(6, 8, 6, 8)));

        String iconGlyph = message.isVideoAttachment() ? "🎥 Video: " : "📁 File: ";
        JLabel titleLabel = new JLabel(iconGlyph + fileName);
        titleLabel.setFont(UiUtils.FONT_TITLE);
        titleLabel.setForeground(UiUtils.COLOR_NAVY_PRIMARY);
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel sizeLabel = new JLabel("Size: " + formattedSize);
        sizeLabel.setFont(UiUtils.FONT_SMALL);
        sizeLabel.setForeground(UiUtils.COLOR_TEXT_SECONDARY);
        card.add(sizeLabel, BorderLayout.CENTER);

        JButton downloadBtn = new JButton("💾 Save / Open (" + formattedSize + ")");
        downloadBtn.setFont(UiUtils.FONT_SMALL);
        downloadBtn.setBackground(Color.WHITE);
        downloadBtn.setForeground(UiUtils.COLOR_NAVY_PRIMARY);
        downloadBtn.setBorder(BorderFactory.createLineBorder(UiUtils.COLOR_BORDER_GREY, 1));
        downloadBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        downloadBtn.addActionListener(e -> {
            try {
                byte[] fileBytes = Base64.getDecoder().decode(message.getFileData());
                saveAttachmentToFile(fileName, fileBytes);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to decode file: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(downloadBtn, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void saveAttachmentToFile(String defaultName, byte[] data) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        chooser.setDialogTitle("Save Attachment As...");
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File targetFile = chooser.getSelectedFile();
            try {
                Files.write(targetFile.toPath(), data);
                int openOption = JOptionPane.showConfirmDialog(this,
                        "File saved successfully to:\n" + targetFile.getAbsolutePath()
                                + "\n\nDo you want to open it now?",
                        "File Saved", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (openOption == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(targetFile);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to save file: " + e.getMessage(), "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0)
            return "0 B";
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
