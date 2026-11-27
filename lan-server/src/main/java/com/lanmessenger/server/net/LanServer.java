package com.lanmessenger.server.net;

import com.lanmessenger.common.model.ChatMessage;
import com.lanmessenger.common.model.GroupInfo;
import com.lanmessenger.common.model.MessageType;
import com.lanmessenger.common.model.NetworkMessage;
import com.lanmessenger.common.util.NetworkConstants;
import com.lanmessenger.server.db.DatabaseManager;
import com.lanmessenger.server.db.GroupDao;
import com.lanmessenger.server.db.MessageDao;
import com.lanmessenger.server.db.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central TCP Server handling multi-client connections, routing, and database persistence.
 */
public class LanServer {

    private static final Logger logger = LoggerFactory.getLogger(LanServer.class);

    private final int port;
    private final String bindHost;
    private final DatabaseManager databaseManager;
    private final UserDao userDao;
    private final GroupDao groupDao;
    private final MessageDao messageDao;
    private final ClientManager clientManager;

    private ServerSocket serverSocket;
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread acceptThread;

    public LanServer(int port, String dbPath) {
        this(port, NetworkConstants.DEFAULT_HOST, new DatabaseManager(dbPath));
    }

    public LanServer(int port, String bindHost, DatabaseManager databaseManager) {
        this.port = port;
        this.bindHost = (bindHost != null && !bindHost.isBlank()) ? bindHost : NetworkConstants.DEFAULT_HOST;
        this.databaseManager = databaseManager;
        this.userDao = new UserDao(databaseManager);
        this.groupDao = new GroupDao(databaseManager);
        this.messageDao = new MessageDao(databaseManager);
        this.clientManager = new ClientManager();
    }

    public synchronized void start() throws IOException {
        if (running.get()) {
            logger.warn("Server is already running on port {}", port);
            return;
        }

        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        // Bind to 0.0.0.0 to accept LAN connections from any network interface
        serverSocket.bind(new InetSocketAddress(bindHost, port));
        running.set(true);

        logger.info("==================================================");
        logger.info("LAN Messaging Server started on {}:{}", bindHost, port);
        logger.info("Ready to accept connections from local and LAN devices.");
        logger.info("==================================================");

        clientManager.notifyLog("INFO", "Server started on " + bindHost + ":" + port);

        acceptThread = new Thread(this::listenForClients, "Server-Accept-Thread");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    private void listenForClients() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setTcpNoDelay(true);
                logger.info("New inbound connection from {}", clientSocket.getRemoteSocketAddress());
                clientManager.notifyLog("INFO", "Inbound TCP connection: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                clientPool.submit(handler);
            } catch (IOException e) {
                if (running.get()) {
                    logger.error("Exception in accept loop", e);
                    clientManager.notifyLog("ERROR", "Accept loop error: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Core message dispatcher processing protocol requests from clients.
     */
    public void processMessage(ClientHandler handler, NetworkMessage message) {
        if (message == null || message.getType() == null) {
            handler.sendMessage(NetworkMessage.error("Invalid message format: null type"));
            return;
        }

        logger.debug("Processing {} from {}", message.getType(), handler.getRemoteAddress());

        switch (message.getType()) {
            case LOGIN -> handleLogin(handler, message);
            case PRIVATE_MESSAGE -> handlePrivateMessage(handler, message);
            case GROUP_MESSAGE -> handleGroupMessage(handler, message);
            case BROADCAST_MESSAGE -> handleBroadcastMessage(handler, message);
            case CREATE_GROUP -> handleCreateGroup(handler, message);
            case CHAT_HISTORY_REQUEST -> handleChatHistoryRequest(handler, message);
            case ONLINE_USERS -> {
                if (ensureAuthenticated(handler)) {
                    handler.sendMessage(NetworkMessage.onlineUsers(clientManager.getOnlineUsernames()));
                }
            }
            case HEARTBEAT -> handler.sendMessage(new NetworkMessage(MessageType.HEARTBEAT));
            default -> {
                logger.warn("Unhandled message type: {}", message.getType());
                handler.sendMessage(NetworkMessage.error("Unsupported message type: " + message.getType()));
            }
        }
    }

    private void handleLogin(ClientHandler handler, NetworkMessage msg) {
        String username = msg.getSender();
        if (username == null || username.trim().isEmpty()) {
            handler.sendMessage(NetworkMessage.loginFailure("", "Username cannot be empty."));
            return;
        }

        username = username.trim();
        if (username.length() < 2 || username.length() > 30) {
            handler.sendMessage(NetworkMessage.loginFailure(username, "Username must be between 2 and 30 characters."));
            return;
        }

        if (NetworkConstants.BROADCAST_TARGET.equalsIgnoreCase(username) || "ALL".equalsIgnoreCase(username)) {
            handler.sendMessage(NetworkMessage.loginFailure(username, "Username is reserved."));
            return;
        }

        // Check for duplicate active username
        if (clientManager.isOnline(username)) {
            logger.warn("Login rejected: username '{}' is already online", username);
            handler.sendMessage(NetworkMessage.loginFailure(username, "Username '" + username + "' is already in use by another active session."));
            return;
        }

        // Register client
        boolean registered = clientManager.registerClient(username, handler);
        if (!registered) {
            handler.sendMessage(NetworkMessage.loginFailure(username, "Unable to register session. Username already taken."));
            return;
        }

        handler.setUsername(username);
        handler.setAuthenticated(true);

        // Persist/update user in database
        userDao.upsertUser(username);

        // 1. Confirm login success
        handler.sendMessage(NetworkMessage.loginSuccess(username, "Welcome, " + username + "! Connected successfully."));

        // 2. Send current online users list to this user
        List<String> onlineList = clientManager.getOnlineUsernames();
        handler.sendMessage(NetworkMessage.onlineUsers(onlineList));

        // 3. Send user's groups
        List<GroupInfo> userGroups = groupDao.getUserGroups(username);
        handler.sendMessage(NetworkMessage.groupList(userGroups));

        // 4. Notify all other active clients that this user joined
        NetworkMessage joinedMsg = NetworkMessage.userJoined(username);
        for (String otherUser : onlineList) {
            if (!otherUser.equalsIgnoreCase(username)) {
                clientManager.sendTo(otherUser, joinedMsg);
            }
        }

        clientManager.notifyLog("INFO", "User '" + username + "' logged in from " + handler.getRemoteAddress());
    }

    private void handlePrivateMessage(ClientHandler handler, NetworkMessage msg) {
        if (!ensureAuthenticated(handler)) return;

        String sender = handler.getUsername();
        String recipient = msg.getRecipient();
        String content = msg.getContent();

        if (recipient == null || recipient.trim().isEmpty()) {
            handler.sendMessage(NetworkMessage.error("Recipient cannot be blank."));
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            if (msg.hasAttachment()) {
                content = "[Attachment: " + (msg.getFileName() != null ? msg.getFileName() : "file") + "]";
            } else {
                handler.sendMessage(NetworkMessage.error("Recipient and content cannot be blank."));
                return;
            }
        }

        recipient = recipient.trim();
        long now = System.currentTimeMillis();

        // 1. Save to database
        ChatMessage chatMsg = new ChatMessage(null, sender, recipient, null, null,
                MessageType.PRIVATE_MESSAGE, content, now);
        chatMsg.setFileName(msg.getFileName());
        chatMsg.setFileSize(msg.getFileSize());
        chatMsg.setFileData(msg.getFileData());
        messageDao.saveMessage(chatMsg);

        // 2. Prepare network payload
        NetworkMessage forwardMsg = NetworkMessage.privateMessage(sender, recipient, content);
        forwardMsg.setTimestamp(now);
        forwardMsg.setFileName(msg.getFileName());
        forwardMsg.setFileSize(msg.getFileSize());
        forwardMsg.setFileData(msg.getFileData());

        // 3. Deliver to recipient if online
        boolean delivered = clientManager.sendTo(recipient, forwardMsg);

        clientManager.notifyMessageRouted("PRIVATE", sender, recipient);
        clientManager.notifyLog("INFO", "Private: [" + sender + " -> " + recipient + "] (" + (delivered ? "Online" : "Stored Offline") + ")");
    }

    private void handleGroupMessage(ClientHandler handler, NetworkMessage msg) {
        if (!ensureAuthenticated(handler)) return;

        String sender = handler.getUsername();
        String groupId = msg.getGroupId();
        String content = msg.getContent();

        if (groupId == null || groupId.trim().isEmpty()) {
            handler.sendMessage(NetworkMessage.error("GroupId cannot be blank."));
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            if (msg.hasAttachment()) {
                content = "[Attachment: " + (msg.getFileName() != null ? msg.getFileName() : "file") + "]";
            } else {
                handler.sendMessage(NetworkMessage.error("GroupId and content cannot be blank."));
                return;
            }
        }

        GroupInfo group = groupDao.getGroup(groupId);
        if (group == null) {
            handler.sendMessage(NetworkMessage.error("Group does not exist: " + groupId));
            return;
        }

        // Verify membership
        if (!groupDao.isMember(groupId, sender)) {
            handler.sendMessage(NetworkMessage.error("You are not a member of group: " + group.getName()));
            return;
        }

        long now = System.currentTimeMillis();

        // 1. Save to database
        ChatMessage chatMsg = new ChatMessage(null, sender, null, groupId, group.getName(),
                MessageType.GROUP_MESSAGE, content, now);
        chatMsg.setFileName(msg.getFileName());
        chatMsg.setFileSize(msg.getFileSize());
        chatMsg.setFileData(msg.getFileData());
        messageDao.saveMessage(chatMsg);

        // 2. Prepare payload
        NetworkMessage forwardMsg = NetworkMessage.groupMessage(sender, groupId, group.getName(), content);
        forwardMsg.setTimestamp(now);
        forwardMsg.setFileName(msg.getFileName());
        forwardMsg.setFileSize(msg.getFileSize());
        forwardMsg.setFileData(msg.getFileData());

        // 3. Deliver to all members who are online (excluding sender, client renders own message)
        List<String> members = groupDao.getGroupMembers(groupId);
        for (String member : members) {
            if (!member.equalsIgnoreCase(sender)) {
                clientManager.sendTo(member, forwardMsg);
            }
        }

        clientManager.notifyMessageRouted("GROUP", sender, group.getName());
        clientManager.notifyLog("INFO", "Group: [" + sender + " -> " + group.getName() + "]: " + content);
    }

    private void handleBroadcastMessage(ClientHandler handler, NetworkMessage msg) {
        if (!ensureAuthenticated(handler)) return;

        String sender = handler.getUsername();
        String content = msg.getContent();

        if (content == null || content.trim().isEmpty()) {
            if (msg.hasAttachment()) {
                content = "[Attachment: " + (msg.getFileName() != null ? msg.getFileName() : "file") + "]";
            } else {
                handler.sendMessage(NetworkMessage.error("Broadcast content cannot be blank."));
                return;
            }
        }

        long now = System.currentTimeMillis();

        // 1. Save to database
        ChatMessage chatMsg = new ChatMessage(null, sender, null, null, null,
                MessageType.BROADCAST_MESSAGE, content, now);
        chatMsg.setFileName(msg.getFileName());
        chatMsg.setFileSize(msg.getFileSize());
        chatMsg.setFileData(msg.getFileData());
        messageDao.saveMessage(chatMsg);

        // 2. Prepare broadcast payload
        NetworkMessage forwardMsg = NetworkMessage.broadcastMessage(sender, content);
        forwardMsg.setTimestamp(now);
        forwardMsg.setFileName(msg.getFileName());
        forwardMsg.setFileSize(msg.getFileSize());
        forwardMsg.setFileData(msg.getFileData());

        // 3. Deliver to all online clients except sender
        for (String onlineUser : clientManager.getOnlineUsernames()) {
            if (!onlineUser.equalsIgnoreCase(sender)) {
                clientManager.sendTo(onlineUser, forwardMsg);
            }
        }

        clientManager.notifyMessageRouted("BROADCAST", sender, "ALL");
        clientManager.notifyLog("INFO", "Broadcast: [" + sender + " -> ALL]: " + content);
    }

    private void handleCreateGroup(ClientHandler handler, NetworkMessage msg) {
        if (!ensureAuthenticated(handler)) return;

        String creator = handler.getUsername();
        String groupName = msg.getGroupName();
        List<String> requestedMembers = msg.getUsers();

        if (groupName == null || groupName.trim().isEmpty()) {
            handler.sendMessage(NetworkMessage.error("Group name cannot be blank."));
            return;
        }

        Set<String> memberSet = new LinkedHashSet<>();
        memberSet.add(creator); // Creator is always member
        if (requestedMembers != null) {
            for (String u : requestedMembers) {
                if (u != null && !u.isBlank()) {
                    memberSet.add(u.trim());
                }
            }
        }

        String groupId = "grp_" + UUID.randomUUID().toString().substring(0, 8);
        GroupInfo newGroup = new GroupInfo(groupId, groupName.trim(), creator, new ArrayList<>(memberSet), System.currentTimeMillis());

        boolean created = groupDao.createGroup(newGroup);
        if (!created) {
            handler.sendMessage(NetworkMessage.error("Failed to create group in database."));
            return;
        }

        // Notify all online members about the new group
        NetworkMessage createdMsg = NetworkMessage.groupCreated(newGroup);
        for (String member : memberSet) {
            clientManager.sendTo(member, createdMsg);
        }

        clientManager.notifyLog("INFO", "Group '" + groupName + "' created by " + creator + " with " + memberSet.size() + " members.");
    }

    private void handleChatHistoryRequest(ClientHandler handler, NetworkMessage msg) {
        if (!ensureAuthenticated(handler)) return;

        String sender = handler.getUsername();
        String chatType = msg.getChatType();
        String targetId = msg.getTargetId();

        List<ChatMessage> history;
        if ("PRIVATE".equalsIgnoreCase(chatType)) {
            history = messageDao.getPrivateHistory(sender, targetId, 100);
        } else if ("GROUP".equalsIgnoreCase(chatType)) {
            // Verify membership
            if (!groupDao.isMember(targetId, sender)) {
                handler.sendMessage(NetworkMessage.error("Cannot access history for group you do not belong to."));
                return;
            }
            history = messageDao.getGroupHistory(targetId, 100);
        } else if ("BROADCAST".equalsIgnoreCase(chatType)) {
            history = messageDao.getBroadcastHistory(100);
        } else {
            handler.sendMessage(NetworkMessage.error("Unknown chatType for history: " + chatType));
            return;
        }

        NetworkMessage response = NetworkMessage.chatHistory(chatType, targetId, history);
        handler.sendMessage(response);
        logger.debug("Sent {} history items for {} ({}) to {}", history.size(), chatType, targetId, sender);
    }

    public void onClientDisconnected(ClientHandler handler) {
        String username = handler.getUsername();
        if (username != null && handler.isAuthenticated()) {
            clientManager.removeClient(username);
            userDao.updateLastSeen(username);

            if (running.get()) {
                // Notify remaining online clients
                NetworkMessage leftMsg = NetworkMessage.userLeft(username);
                clientManager.broadcast(leftMsg);

                clientManager.notifyLog("INFO", "User '" + username + "' disconnected.");
            }
        }
    }

    private boolean ensureAuthenticated(ClientHandler handler) {
        if (!handler.isAuthenticated() || handler.getUsername() == null) {
            handler.sendMessage(NetworkMessage.error("Unauthorized: Must login first."));
            return false;
        }
        return true;
    }

    public synchronized void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        logger.info("Stopping LAN Messaging Server...");
        clientManager.notifyLog("INFO", "Server stopping...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }

        clientManager.disconnectAll();
        clientPool.shutdownNow();

        logger.info("LAN Messaging Server stopped.");
        clientManager.notifyLog("INFO", "Server stopped.");
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return port;
    }

    public String getBindHost() {
        return bindHost;
    }

    public ClientManager getClientManager() {
        return clientManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public GroupDao getGroupDao() {
        return groupDao;
    }

    public MessageDao getMessageDao() {
        return messageDao;
    }
}
