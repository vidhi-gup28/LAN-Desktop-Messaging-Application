package com.lanmessenger.client.net;

import com.lanmessenger.common.model.ChatMessage;
import com.lanmessenger.common.model.GroupInfo;

import java.util.List;

/**
 * Callback listener for network and messaging events received by the client.
 */
public interface ClientNetworkListener {
    void onConnected();
    void onDisconnected(String reason);
    void onLoginSuccess(String username, String welcomeNote);
    void onLoginFailure(String reason);
    void onOnlineUsersReceived(List<String> users);
    void onUserJoined(String username);
    void onUserLeft(String username);
    void onPrivateMessageReceived(ChatMessage message);
    void onGroupMessageReceived(ChatMessage message);
    void onBroadcastMessageReceived(ChatMessage message);
    void onGroupCreated(GroupInfo group);
    void onGroupListReceived(List<GroupInfo> groups);
    void onChatHistoryReceived(String chatType, String targetId, List<ChatMessage> history);
    void onError(String errorMessage);
}
