package com.lanmessenger.client.model;

import com.lanmessenger.common.model.ChatMessage;
import com.lanmessenger.common.model.GroupInfo;
import com.lanmessenger.common.util.NetworkConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing an active or open chat conversation.
 */
public class Conversation {

    private final String id;
    private String title;
    private final ConversationType type;
    private boolean online;
    private int unreadCount = 0;
    private final List<ChatMessage> messages = new ArrayList<>();
    private GroupInfo groupInfo;
    private boolean historyLoaded = false;

    public Conversation(String id, String title, ConversationType type) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.online = (type == ConversationType.BROADCAST);
    }

    public static Conversation createBroadcast() {
        return new Conversation(NetworkConstants.BROADCAST_TARGET, "📢 Broadcast Channel", ConversationType.BROADCAST);
    }

    public static Conversation createPrivate(String username, boolean isOnline) {
        Conversation c = new Conversation(username, username, ConversationType.PRIVATE);
        c.setOnline(isOnline);
        return c;
    }

    public static Conversation createGroup(GroupInfo group) {
        Conversation c = new Conversation(group.getId(), group.getName(), ConversationType.GROUP);
        c.setGroupInfo(group);
        c.setOnline(true);
        return c;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ConversationType getType() {
        return type;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = Math.max(0, unreadCount);
    }

    public void incrementUnread() {
        this.unreadCount++;
    }

    public void clearUnread() {
        this.unreadCount = 0;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void addMessage(ChatMessage msg) {
        this.messages.add(msg);
    }

    public void setMessages(List<ChatMessage> list) {
        this.messages.clear();
        if (list != null) {
            this.messages.addAll(list);
        }
    }

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
        if (groupInfo != null && groupInfo.getName() != null) {
            this.title = groupInfo.getName();
        }
    }

    public boolean isHistoryLoaded() {
        return historyLoaded;
    }

    public void setHistoryLoaded(boolean historyLoaded) {
        this.historyLoaded = historyLoaded;
    }
}
