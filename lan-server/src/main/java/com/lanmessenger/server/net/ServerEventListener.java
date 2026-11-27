package com.lanmessenger.server.net;

/**
 * Listener interface for server events (connections, messages, disconnections, errors).
 */
public interface ServerEventListener {
    void onServerStarted(String host, int port);
    void onServerStopped();
    void onClientConnected(String address);
    void onClientLoggedIn(String username, String address);
    void onClientDisconnected(String username, String address);
    void onMessageRouted(String type, String sender, String recipientOrGroup);
    void onLog(String level, String message);
}
