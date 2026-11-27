package com.lanmessenger.server.net;

import com.lanmessenger.common.model.NetworkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe registry and router for active client connections.
 */
public class ClientManager {

    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);

    // Map: case-insensitive username -> ClientHandler
    private final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    // Original case preserved: case-insensitive username -> original username
    private final ConcurrentHashMap<String, String> originalUsernames = new ConcurrentHashMap<>();

    private final List<ServerEventListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(ServerEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ServerEventListener listener) {
        listeners.remove(listener);
    }

    public void notifyLog(String level, String message) {
        for (ServerEventListener listener : listeners) {
            try {
                listener.onLog(level, message);
            } catch (Exception e) {
                logger.error("Error in server event listener", e);
            }
        }
    }

    /**
     * Registers an authenticated client.
     * Rejects duplicate usernames to enforce single active session per username.
     */
    public synchronized boolean registerClient(String username, ClientHandler handler) {
        String key = username.trim().toLowerCase();
        if (activeClients.containsKey(key)) {
            logger.warn("Rejecting duplicate login attempt for username '{}'", username);
            return false;
        }
        activeClients.put(key, handler);
        originalUsernames.put(key, username.trim());
        logger.info("User '{}' registered. Total active: {}", username, activeClients.size());

        for (ServerEventListener listener : listeners) {
            try {
                listener.onClientLoggedIn(username.trim(), handler.getRemoteAddress());
            } catch (Exception e) {
                logger.error("Error notifying listener", e);
            }
        }
        return true;
    }

    public synchronized void removeClient(String username) {
        if (username == null) {
            return;
        }
        String key = username.trim().toLowerCase();
        ClientHandler removed = activeClients.remove(key);
        String orig = originalUsernames.remove(key);
        String displayName = orig != null ? orig : username;

        if (removed != null) {
            logger.info("User '{}' unregistered. Total active: {}", displayName, activeClients.size());
            for (ServerEventListener listener : listeners) {
                try {
                    listener.onClientDisconnected(displayName, removed.getRemoteAddress());
                } catch (Exception e) {
                    logger.error("Error notifying listener", e);
                }
            }
        }
    }

    public boolean isOnline(String username) {
        if (username == null) return false;
        return activeClients.containsKey(username.trim().toLowerCase());
    }

    public ClientHandler getClient(String username) {
        if (username == null) return null;
        return activeClients.get(username.trim().toLowerCase());
    }

    public List<String> getOnlineUsernames() {
        return new ArrayList<>(originalUsernames.values());
    }

    public int getActiveCount() {
        return activeClients.size();
    }

    public Map<String, ClientHandler> getAllClients() {
        return Collections.unmodifiableMap(activeClients);
    }

    /**
     * Sends a message to a specific user if online.
     * Returns true if delivered, false if recipient offline.
     */
    public boolean sendTo(String recipient, NetworkMessage message) {
        if (recipient == null) return false;
        ClientHandler handler = getClient(recipient);
        if (handler != null && handler.isAuthenticated()) {
            handler.sendMessage(message);
            return true;
        }
        return false;
    }

    /**
     * Broadcasts a message to all authenticated online clients.
     */
    public void broadcast(NetworkMessage message) {
        for (ClientHandler handler : activeClients.values()) {
            if (handler.isAuthenticated()) {
                handler.sendMessage(message);
            }
        }
    }

    /**
     * Sends a message to a specific collection of usernames (e.g. group members).
     */
    public void broadcastTo(Collection<String> usernames, NetworkMessage message) {
        if (usernames == null || usernames.isEmpty()) {
            return;
        }
        Set<String> targetKeys = new HashSet<>();
        for (String u : usernames) {
            targetKeys.add(u.trim().toLowerCase());
        }

        for (Map.Entry<String, ClientHandler> entry : activeClients.entrySet()) {
            if (targetKeys.contains(entry.getKey()) && entry.getValue().isAuthenticated()) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    public void notifyMessageRouted(String type, String sender, String recipientOrGroup) {
        for (ServerEventListener listener : listeners) {
            try {
                listener.onMessageRouted(type, sender, recipientOrGroup);
            } catch (Exception e) {
                logger.error("Error notifying listener", e);
            }
        }
    }

    public void disconnectAll() {
        for (ClientHandler handler : activeClients.values()) {
            handler.close();
        }
        activeClients.clear();
        originalUsernames.clear();
    }
}
