package com.lanmessenger.client.net;

import com.lanmessenger.common.model.ChatMessage;
import com.lanmessenger.common.model.GroupInfo;
import com.lanmessenger.common.model.MessageType;
import com.lanmessenger.common.model.NetworkMessage;
import com.lanmessenger.common.util.JsonUtils;
import com.lanmessenger.common.util.NetworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages TCP socket communication with the central LAN server.
 */
public class NetworkClient {

    private static final Logger logger = LoggerFactory.getLogger(NetworkClient.class);

    private final String host;
    private final int port;
    private final CopyOnWriteArrayList<ClientNetworkListener> listeners = new CopyOnWriteArrayList<>();

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Thread listenerThread;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void addListener(ClientNetworkListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ClientNetworkListener listener) {
        listeners.remove(listener);
    }

    /**
     * Connects to the LAN server with a 5000ms timeout.
     */
    public synchronized void connect() throws IOException {
        if (connected.get()) {
            return;
        }

        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 5000);
        socket.setTcpNoDelay(true);

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), NetworkConstants.CHARSET), 65536);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), NetworkConstants.CHARSET), 65536);
        connected.set(true);

        dispatch(() -> {
            for (ClientNetworkListener l : listeners) {
                l.onConnected();
            }
        });

        listenerThread = new Thread(this::listenForServerMessages, "Client-Receiver-" + host + ":" + port);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void listenForServerMessages() {
        try {
            String line;
            while (connected.get() && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    NetworkMessage message = JsonUtils.fromJson(line, NetworkMessage.class);
                    if (message != null) {
                        handleInboundMessage(message);
                    }
                } catch (Exception e) {
                    logger.error("Error parsing message from server: {}", line, e);
                }
            }
        } catch (IOException e) {
            if (connected.get()) {
                logger.info("Connection lost to server: {}", e.getMessage());
            }
        } finally {
            disconnect("Connection closed by server or network interruption.");
        }
    }

    private void handleInboundMessage(NetworkMessage msg) {
        dispatch(() -> {
            for (ClientNetworkListener l : listeners) {
                try {
                    switch (msg.getType()) {
                        case LOGIN_SUCCESS -> l.onLoginSuccess(msg.getRecipient(), msg.getContent());
                        case LOGIN_FAILURE -> l.onLoginFailure(msg.getContent());
                        case ONLINE_USERS -> l.onOnlineUsersReceived(msg.getUsers());
                        case USER_JOINED -> l.onUserJoined(msg.getSender());
                        case USER_LEFT -> l.onUserLeft(msg.getSender());
                        case PRIVATE_MESSAGE -> {
                            ChatMessage cm = new ChatMessage(null, msg.getSender(), msg.getRecipient(), null, null,
                                    MessageType.PRIVATE_MESSAGE, msg.getContent(), msg.getTimestamp());
                            cm.setFileName(msg.getFileName());
                            cm.setFileSize(msg.getFileSize());
                            cm.setFileData(msg.getFileData());
                            l.onPrivateMessageReceived(cm);
                        }
                        case GROUP_MESSAGE -> {
                            ChatMessage cm = new ChatMessage(null, msg.getSender(), null, msg.getGroupId(), msg.getGroupName(),
                                    MessageType.GROUP_MESSAGE, msg.getContent(), msg.getTimestamp());
                            cm.setFileName(msg.getFileName());
                            cm.setFileSize(msg.getFileSize());
                            cm.setFileData(msg.getFileData());
                            l.onGroupMessageReceived(cm);
                        }
                        case BROADCAST_MESSAGE -> {
                            ChatMessage cm = new ChatMessage(null, msg.getSender(), null, null, null,
                                    MessageType.BROADCAST_MESSAGE, msg.getContent(), msg.getTimestamp());
                            cm.setFileName(msg.getFileName());
                            cm.setFileSize(msg.getFileSize());
                            cm.setFileData(msg.getFileData());
                            l.onBroadcastMessageReceived(cm);
                        }
                        case GROUP_CREATED -> {
                            GroupInfo group = new GroupInfo(msg.getGroupId(), msg.getGroupName(), msg.getSender(), msg.getUsers(), msg.getTimestamp());
                            l.onGroupCreated(group);
                        }
                        case GROUP_LIST -> l.onGroupListReceived(msg.getGroups());
                        case CHAT_HISTORY -> l.onChatHistoryReceived(msg.getChatType(), msg.getTargetId(), msg.getMessages());
                        case ERROR -> l.onError(msg.getContent());
                        case HEARTBEAT -> {
                            // Heartbeat received
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error invoking listener for message type {}", msg.getType(), e);
                }
            }
        });
    }

    /**
     * Sends a protocol message to the server.
     */
    public synchronized boolean send(NetworkMessage message) {
        if (!connected.get() || writer == null) {
            return false;
        }
        try {
            String jsonDelimited = JsonUtils.toDelimitedJson(message);
            writer.write(jsonDelimited);
            writer.flush();
            return true;
        } catch (IOException e) {
            logger.error("Failed to send message to server", e);
            disconnect("Failed to write to socket: " + e.getMessage());
            return false;
        }
    }

    public synchronized void disconnect() {
        disconnect("User requested disconnect.");
    }

    public synchronized void disconnect(String reason) {
        if (connected.compareAndSet(true, false)) {
            logger.info("Disconnecting client: {}", reason);
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception ignored) {
            }
            try {
                if (reader != null) reader.close();
            } catch (Exception ignored) {
            }
            try {
                if (writer != null) writer.close();
            } catch (Exception ignored) {
            }

            dispatch(() -> {
                for (ClientNetworkListener l : listeners) {
                    l.onDisconnected(reason);
                }
            });
        }
    }

    private void dispatch(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public boolean isConnected() {
        return connected.get();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
