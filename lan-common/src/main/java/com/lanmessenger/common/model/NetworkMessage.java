package com.lanmessenger.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified protocol payload for all client-server communication.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NetworkMessage {

    private MessageType type;
    private String sender;
    private String recipient;
    private String groupId;
    private String groupName;
    private String content;
    private Long timestamp;

    private List<String> users;
    private List<GroupInfo> groups;
    private List<ChatMessage> messages;

    private String chatType;
    private String targetId;
    private String fileName;
    private Long fileSize;
    private String fileData;

    public NetworkMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public NetworkMessage(MessageType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    // Factory methods for clean construction
    public static NetworkMessage login(String username) {
        NetworkMessage msg = new NetworkMessage(MessageType.LOGIN);
        msg.setSender(username);
        return msg;
    }

    public static NetworkMessage loginSuccess(String username, String welcomeNote) {
        NetworkMessage msg = new NetworkMessage(MessageType.LOGIN_SUCCESS);
        msg.setRecipient(username);
        msg.setContent(welcomeNote);
        return msg;
    }

    public static NetworkMessage loginFailure(String username, String reason) {
        NetworkMessage msg = new NetworkMessage(MessageType.LOGIN_FAILURE);
        msg.setRecipient(username);
        msg.setContent(reason);
        return msg;
    }

    public static NetworkMessage privateMessage(String sender, String recipient, String content) {
        NetworkMessage msg = new NetworkMessage(MessageType.PRIVATE_MESSAGE);
        msg.setSender(sender);
        msg.setRecipient(recipient);
        msg.setContent(content);
        return msg;
    }

    public static NetworkMessage groupMessage(String sender, String groupId, String groupName, String content) {
        NetworkMessage msg = new NetworkMessage(MessageType.GROUP_MESSAGE);
        msg.setSender(sender);
        msg.setGroupId(groupId);
        msg.setGroupName(groupName);
        msg.setContent(content);
        return msg;
    }

    public static NetworkMessage broadcastMessage(String sender, String content) {
        NetworkMessage msg = new NetworkMessage(MessageType.BROADCAST_MESSAGE);
        msg.setSender(sender);
        msg.setContent(content);
        return msg;
    }

    public static NetworkMessage onlineUsers(List<String> users) {
        NetworkMessage msg = new NetworkMessage(MessageType.ONLINE_USERS);
        msg.setUsers(new ArrayList<>(users));
        return msg;
    }

    public static NetworkMessage userJoined(String username) {
        NetworkMessage msg = new NetworkMessage(MessageType.USER_JOINED);
        msg.setSender(username);
        return msg;
    }

    public static NetworkMessage userLeft(String username) {
        NetworkMessage msg = new NetworkMessage(MessageType.USER_LEFT);
        msg.setSender(username);
        return msg;
    }

    public static NetworkMessage createGroup(String creator, String groupName, List<String> members) {
        NetworkMessage msg = new NetworkMessage(MessageType.CREATE_GROUP);
        msg.setSender(creator);
        msg.setGroupName(groupName);
        msg.setUsers(new ArrayList<>(members));
        return msg;
    }

    public static NetworkMessage groupCreated(GroupInfo groupInfo) {
        NetworkMessage msg = new NetworkMessage(MessageType.GROUP_CREATED);
        msg.setGroupId(groupInfo.getId());
        msg.setGroupName(groupInfo.getName());
        msg.setSender(groupInfo.getCreatedBy());
        msg.setUsers(new ArrayList<>(groupInfo.getMembers()));
        return msg;
    }

    public static NetworkMessage groupList(List<GroupInfo> groupList) {
        NetworkMessage msg = new NetworkMessage(MessageType.GROUP_LIST);
        msg.setGroups(new ArrayList<>(groupList));
        return msg;
    }

    public static NetworkMessage chatHistoryRequest(String sender, String chatType, String targetId) {
        NetworkMessage msg = new NetworkMessage(MessageType.CHAT_HISTORY_REQUEST);
        msg.setSender(sender);
        msg.setChatType(chatType);
        msg.setTargetId(targetId);
        return msg;
    }

    public static NetworkMessage chatHistory(String chatType, String targetId, List<ChatMessage> history) {
        NetworkMessage msg = new NetworkMessage(MessageType.CHAT_HISTORY);
        msg.setChatType(chatType);
        msg.setTargetId(targetId);
        msg.setMessages(new ArrayList<>(history));
        return msg;
    }

    public static NetworkMessage error(String message) {
        NetworkMessage msg = new NetworkMessage(MessageType.ERROR);
        msg.setContent(message);
        return msg;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
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

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupInfo> groups) {
        this.groups = groups;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
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

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "type=" + type +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
