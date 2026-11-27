package com.lanmessenger.server.net;

import com.lanmessenger.common.model.NetworkMessage;
import com.lanmessenger.common.util.JsonUtils;
import com.lanmessenger.common.util.NetworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles communication with an individual connected client over TCP socket.
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final LanServer server;
    private final String remoteAddress;
    private final long connectedSince;

    private BufferedReader reader;
    private BufferedWriter writer;

    private String username;
    private final AtomicBoolean authenticated = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public ClientHandler(Socket socket, LanServer server) {
        this.socket = socket;
        this.server = server;
        this.remoteAddress = socket.getRemoteSocketAddress() != null ? socket.getRemoteSocketAddress().toString() : "unknown";
        this.connectedSince = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), NetworkConstants.CHARSET), 65536);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), NetworkConstants.CHARSET), 65536);

            logger.info("Client handler started for {}", remoteAddress);

            String line;
            while (!closed.get() && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    NetworkMessage message = JsonUtils.fromJson(line, NetworkMessage.class);
                    if (message != null) {
                        server.processMessage(this, message);
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse message from {}: '{}'", remoteAddress, line, e);
                    sendMessage(NetworkMessage.error("Malformed JSON message: " + e.getMessage()));
                }
            }
        } catch (IOException e) {
            if (!closed.get()) {
                logger.info("Connection reset or read error for {}: {}", (username != null ? username : remoteAddress), e.getMessage());
            }
        } finally {
            close();
        }
    }

    /**
     * Thread-safe transmission of a protocol message as newline-delimited JSON.
     */
    public synchronized boolean sendMessage(NetworkMessage message) {
        if (closed.get() || writer == null) {
            return false;
        }
        try {
            String jsonDelimited = JsonUtils.toDelimitedJson(message);
            writer.write(jsonDelimited);
            writer.flush();
            return true;
        } catch (IOException e) {
            logger.error("Failed to send message to {}: {}", (username != null ? username : remoteAddress), e.getMessage());
            close();
            return false;
        }
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            logger.info("Closing client handler for {} ({})", (username != null ? username : "unauthenticated"), remoteAddress);
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
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

            // Notify server of client departure
            server.onClientDisconnected(this);
        }
    }

    public boolean isAuthenticated() {
        return authenticated.get();
    }

    public void setAuthenticated(boolean auth) {
        this.authenticated.set(auth);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public long getConnectedSince() {
        return connectedSince;
    }

    public boolean isClosed() {
        return closed.get();
    }
}
