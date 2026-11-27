package com.lanmessenger.server;

import com.formdev.flatlaf.FlatLightLaf;
import com.lanmessenger.server.net.LanServer;
import com.lanmessenger.server.net.ServerEventListener;
import com.lanmessenger.server.ui.ServerConsoleFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Server application bootstrap. Supports both enterprise Light Theme GUI dashboard and headless CLI mode.
 */
public class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {
        int port = 5000;
        String dbPath = "lan_messenger.db";
        boolean headless = GraphicsEnvironment.isHeadless();

        for (int i = 0; i < args.length; i++) {
            if ("--port".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port number: " + args[i]);
                }
            } else if ("--db".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                dbPath = args[++i];
            } else if ("--headless".equalsIgnoreCase(args[i]) || "--cli".equalsIgnoreCase(args[i])) {
                headless = true;
            }
        }

        if (headless) {
            runCliServer(port, dbPath);
        } else {
            runGuiServer(port, dbPath);
        }
    }

    private static void runGuiServer(int port, String dbPath) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Strict Professional Light Theme
                UIManager.setLookAndFeel(new FlatLightLaf());
                UIManager.put("Button.arc", 0);
                UIManager.put("Component.arc", 0);
                UIManager.put("TextComponent.arc", 0);
                UIManager.put("ScrollBar.thumbArc", 0);
            } catch (Exception e) {
                logger.warn("FlatLightLaf look and feel could not be set", e);
            }
            ServerConsoleFrame frame = new ServerConsoleFrame(port, dbPath);
            frame.setVisible(true);
        });
    }

    private static void runCliServer(int port, String dbPath) {
        System.out.println("Starting LAN Messaging Server in CLI mode on port " + port + "...");
        LanServer server = new LanServer(port, dbPath);
        server.getClientManager().addListener(new ServerEventListener() {
            @Override
            public void onServerStarted(String host, int port) {
                System.out.println("[SERVER] Started on " + host + ":" + port);
            }

            @Override
            public void onServerStopped() {
                System.out.println("[SERVER] Stopped.");
            }

            @Override
            public void onClientConnected(String address) {
                System.out.println("[SERVER] Inbound connection: " + address);
            }

            @Override
            public void onClientLoggedIn(String username, String address) {
                System.out.println("[SERVER] User '" + username + "' logged in from " + address);
            }

            @Override
            public void onClientDisconnected(String username, String address) {
                System.out.println("[SERVER] User '" + username + "' disconnected.");
            }

            @Override
            public void onMessageRouted(String type, String sender, String recipientOrGroup) {
                System.out.println("[ROUTER] " + type + " from " + sender + " to " + recipientOrGroup);
            }

            @Override
            public void onLog(String level, String message) {
                System.out.println("[" + level + "] " + message);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "Server-Shutdown-Hook"));

        try {
            server.start();
            System.out.println("Server running. Local LAN IPs: " + ServerConsoleFrame.detectLanIpv4Addresses());
            System.out.println("Press Ctrl+C to stop.");
            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            System.err.println("Server runtime error: " + e.getMessage());
        }
    }
}
