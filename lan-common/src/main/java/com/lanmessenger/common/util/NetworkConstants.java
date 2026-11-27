package com.lanmessenger.common.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Common network constants for LAN Messaging.
 */
public final class NetworkConstants {

    public static final int DEFAULT_PORT = 5000;
    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final String LOCALHOST = "127.0.0.1";
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final String BROADCAST_TARGET = "__BROADCAST__";

    public static final int SO_TIMEOUT_MS = 0; // Infinite timeout for read, handled via heartbeat/close

    private NetworkConstants() {
    }
}
