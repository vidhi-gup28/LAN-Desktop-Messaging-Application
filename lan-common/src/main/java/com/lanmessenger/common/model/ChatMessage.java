package com.lanmessenger.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Represents a stored or transmitted chat message.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FULL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private Long id;
    private String sender;
    private String receiver;
    private String groupId;
    private String groupName;
    private MessageType messageType;
    private String content;
    private Long timestamp;
    private String fileName;
    private Long fileSize;
    private String fileData;

    public ChatMessage() {
    }

    public ChatMessage(Long id, String sender, String receiver, String groupId,
                       String groupName, MessageType messageType, String content, Long timestamp) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.groupId = groupId;
        this.groupName = groupName;
        this.messageType = messageType;
        this.content = content;
        this.timestamp = (timestamp != null) ? timestamp : System.currentTimeMillis();
    }

    public static ChatMessage ofPrivate(String sender, String receiver, String content) {
        return new ChatMessage(null, sender, receiver, null, null,
                MessageType.PRIVATE_MESSAGE, content, System.currentTimeMillis());
    }

    public static ChatMessage ofGroup(String sender, String groupId, String groupName, String content) {
        return new ChatMessage(null, sender, null, groupId, groupName,
                MessageType.GROUP_MESSAGE, content, System.currentTimeMillis());
    }

    public static ChatMessage ofBroadcast(String sender, String content) {
        return new ChatMessage(null, sender, null, null, null,
                MessageType.BROADCAST_MESSAGE, content, System.currentTimeMillis());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public boolean hasAttachment() {
        return fileData != null && !fileData.trim().isEmpty();
    }

    public boolean isImageAttachment() {
        if (!hasAttachment() || fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".bmp") || lower.endsWith(".webp");
    }

    public boolean isVideoAttachment() {
        if (!hasAttachment() || fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi")
                || lower.endsWith(".mov") || lower.endsWith(".webm") || lower.endsWith(".wmv");
    }

    public String getFormattedTime() {
        if (timestamp == null) {
            return "";
        }
        return TIME_FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    public String getFormattedDateTime() {
        if (timestamp == null) {
            return "";
        }
        return FULL_FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", messageType=" + messageType +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
