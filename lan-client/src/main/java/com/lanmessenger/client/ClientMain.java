package com.lanmessenger.client;

import com.formdev.flatlaf.FlatLightLaf;
import com.lanmessenger.client.net.NetworkClient;
import com.lanmessenger.client.ui.LoginFrame;
import com.lanmessenger.client.ui.MainChatFrame;
import com.lanmessenger.common.model.NetworkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;

/**
 * Client desktop application bootstrap.
 * Configured with Professional Enterprise Light Theme.
 */
public class ClientMain {

    private static final Logger logger = LoggerFactory.getLogger(ClientMain.class);

    public static void main(String[] args) {
        String host = null;
        int port = 5000;
        String username = null;

        for (int i = 0; i < args.length; i++) {
            if ("--host".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                host = args[++i];
            } else if ("--port".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException ignored) {
                }
            } else if ("--user".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                username = args[++i];
            }
        }

        final String finalHost = host;
        final int finalPort = port;
        final String finalUsername = username;

        SwingUtilities.invokeLater(() -> {
            try {
                // Strict Professional Light Theme
                UIManager.setLookAndFeel(new FlatLightLaf());

                // Rectangular desktop appearance (No rounded pill cards or excessive arcs)
                UIManager.put("Button.arc", 0);
                UIManager.put("Component.arc", 0);
                UIManager.put("TextComponent.arc", 0);
                UIManager.put("ScrollBar.thumbArc", 0);
                UIManager.put("TabbedPane.tabArc", 0);
            } catch (Exception e) {
                logger.warn("Could not set FlatLightLaf look and feel", e);
            }

            if (finalHost != null && finalUsername != null) {
                autoConnect(finalHost, finalPort, finalUsername);
            } else {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }

    private static void autoConnect(String host, int port, String username) {
        NetworkClient client = new NetworkClient(host, port);
        try {
            client.connect();
            client.send(NetworkMessage.login(username));
            MainChatFrame frame = new MainChatFrame(client, username);
            frame.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not auto-connect to " + host + ":" + port + ":\n" + e.getMessage(), "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            new LoginFrame().setVisible(true);
        }
    }
}
