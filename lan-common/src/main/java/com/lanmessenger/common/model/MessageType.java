package com.lanmessenger.common.model;

/**
 * Enumeration of all network protocol message types exchanged between client and server.
 */
public enum MessageType {
    LOGIN,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    PRIVATE_MESSAGE,
    GROUP_MESSAGE,
    BROADCAST_MESSAGE,
    ONLINE_USERS,
    USER_JOINED,
    USER_LEFT,
    CHAT_HISTORY_REQUEST,
    CHAT_HISTORY,
    CREATE_GROUP,
    GROUP_CREATED,
    GROUP_LIST,
    ERROR,
    HEARTBEAT
}
