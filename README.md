# LAN Desktop Messaging Application

An enterprise-grade, professional desktop messaging application built in Java 17 and Java Swing. The system enables multiple computers connected to the same Local Area Network (Wi-Fi or Ethernet LAN) to communicate seamlessly through a central server with real-time private messaging, group chats, global announcements, SQLite chat history persistence, and dynamic user presence tracking.

---

## 🌟 Key Features

### 🖥️ Central Server (`lan-server`)
- **Multi-Client Architecture**: Non-blocking concurrent connection handling using Java `ServerSocket` and thread pools (`ExecutorService`).
- **LAN Binding**: Binds to `0.0.0.0` to accept connections across all LAN interfaces (Wi-Fi, Ethernet, Loopback).
- **Modern Administrative GUI Dashboard**:
  - Live server status indicator (ONLINE / STOPPED).
  - Auto-detection and display of all local IPv4 LAN addresses for client connections with a "Copy IP" button.
  - Configurable listening port (default: `5000`).
  - Active Connected Clients table showing usernames, IP addresses, and session duration.
  - Administrator kick/disconnect client functionality.
  - Real-time colored event log with auto-scroll and clear options.
  - Global Server Broadcast tool to push urgent announcements to all connected users.
- **Headless / CLI Mode**: Can run as a GUI dashboard or headlessly in terminal environments using `--headless` or `--cli`.
- **Duplicate Username Guard**: Rejects duplicate active logins with clear error feedback.
- **SQLite Database Persistence**: All messages, users, groups, and memberships are permanently stored in SQLite via JDBC.

### 💻 Client Application (`lan-client`)
- **Professional Enterprise Light Theme**: Designed with clean white backgrounds (`#FFFFFF`), light grey structured panels (`#F8F9FA`), subtle 1px grey borders (`#D1D5DB`), and muted navy blue accents (`#1E3A8A`).
- **Native Desktop Application Architecture**:
  - Traditional menu bar (`File`, `View`, `Help`).
  - Compact desktop toolbar and system status bar.
  - Aligned connection dialog with clear two-column layout.
  - Rectangular controls, buttons, and inputs without website landing page styling or excessive rounded cards.
- **Dynamic Directory & Search**:
  - Compact real-time search field for finding network contacts and groups.
  - 📢 **Broadcast Channel**: Top-level channel reaching all connected LAN users.
  - **Groups Section**: Displays user groups with member counters and a `+ New` button.
  - **Contacts Directory**: Displays online users with small green presence dots (`●`) and offline users with outline circles (`○`).
  - Unread message count badges (simple rectangular badges).
- **Conversation Stream**:
  - Clean desktop header with active contact title and session status.
  - Rectangular message cards: Outgoing messages (right-aligned in subtle `#EEF4FE` blue tint with muted navy header) and Incoming messages (left-aligned in crisp white with sender header).
  - Compact message input area with **Enter to Send** and **Shift+Enter for newline**.
  - Auto-scroll to bottom on incoming messages.
- **Group Creation Dialog**: Classic desktop modal dialog with member checklist and quick select/clear buttons.
- **Chat History**: Automatically retrieves conversation history from SQLite upon opening any chat.
- **Audio Chimes**: Subtle, discreet desktop beep for incoming messages via `javax.sound.sampled`.
- **Graceful Lifecycle**: Proper socket shutdown on window close and disconnection alerts.

---

## 🏗️ Project Architecture (Maven Multi-Module)

```
lan-messaging-application/
├── pom.xml                                   # Parent POM (Java 17, dependency versions)
├── build.bat                                 # One-click Windows build script
├── start-server.bat                          # One-click script to launch the Server
├── start-client.bat                          # One-click script to launch the Client
├── allow-firewall-port.bat                   # Admin script to configure Windows Firewall for Port 5000
│
├── lan-common/                               # Shared Protocol & Data Models
│   ├── pom.xml
│   └── src/main/java/com/lanmessenger/common/
│       ├── model/
│       │   ├── MessageType.java              # Network protocol enum (LOGIN, PRIVATE_MESSAGE, etc.)
│       │   ├── ChatMessage.java              # Message model with timestamps & sender info
│       │   ├── GroupInfo.java                # Group metadata and member list
│       │   └── NetworkMessage.java            # Unified JSON protocol payload
│       └── util/
│           ├── JsonUtils.java                # Newline-delimited Jackson JSON serializer
│           └── NetworkConstants.java         # Port 5000, UTF-8 charset, broadcast constants
│
├── lan-server/                               # Central TCP Server & Database
│   ├── pom.xml
│   └── src/main/java/com/lanmessenger/server/
│       ├── db/
│       │   ├── DatabaseManager.java          # SQLite connection manager & schema init
│       │   ├── UserDao.java                  # User persistence & last seen timestamps
│       │   ├── GroupDao.java                 # Group creation & membership operations
│       │   └── MessageDao.java               # Message storage & history queries
│       ├── net/
│       │   ├── ClientHandler.java            # Per-client socket listener & writer
│       │   ├── ClientManager.java            # Thread-safe active client registry
│       │   ├── LanServer.java                # ServerSocket listener & message dispatcher
│       │   └── ServerEventListener.java      # Dashboard event listener interface
│       ├── ui/
│       │   └── ServerConsoleFrame.java       # Modern Swing Server Control Dashboard
│       └── ServerMain.java                   # Server entry point (GUI / CLI)
│
└── lan-client/                               # Desktop Client & GUI Application
    ├── pom.xml
    └── src/main/java/com/lanmessenger/client/
        ├── model/
        │   ├── Conversation.java             # Client conversation model
        │   └── ConversationType.java         # BROADCAST, PRIVATE, GROUP
        ├── net/
        │   ├── NetworkClient.java            # TCP client socket manager & receiver thread
        │   └── ClientNetworkListener.java    # UI event dispatch interface
        ├── ui/
        │   ├── LoginFrame.java               # Modern connection & login window
        │   ├── MainChatFrame.java            # Main window coordinating chat & sidebar
        │   ├── SidebarPanel.java             # Searchable user/group list with badges
        │   ├── ChatConversationPanel.java    # Message bubbles, header & input field
        │   ├── MessageBubblePanel.java       # Rounded bubble component with timestamps
        │   ├── CreateGroupDialog.java        # Group creation modal dialog
        │   └── UiUtils.java                  # Avatars, palette & sound synthesizer
        └── ClientMain.java                   # Client desktop entry point
```

---

## 📡 Network Protocol (JSON-based)

All communication between client and server travels over TCP in **UTF-8 newline-delimited JSON (`\n`)**.

| Message Type | Purpose | Payload Fields |
|---|---|---|
| `LOGIN` | Authenticate session | `sender` (username) |
| `LOGIN_SUCCESS` | Confirm login | `recipient`, `content` |
| `LOGIN_FAILURE` | Reject duplicate username | `recipient`, `content` (error reason) |
| `ONLINE_USERS` | Transmit active user directory | `users` (list of active usernames) |
| `USER_JOINED` | Notify presence online | `sender` (new user's username) |
| `USER_LEFT` | Notify presence offline | `sender` (disconnected username) |
| `PRIVATE_MESSAGE` | Direct 1-to-1 chat message | `sender`, `recipient`, `content`, `timestamp` |
| `GROUP_MESSAGE` | Message to all group members | `sender`, `groupId`, `groupName`, `content`, `timestamp` |
| `BROADCAST_MESSAGE` | Global announcement to all | `sender`, `content`, `timestamp` |
| `CREATE_GROUP` | Create new group | `sender` (creator), `groupName`, `users` (members) |
| `GROUP_CREATED` | Push newly created group info | `groupId`, `groupName`, `sender`, `users`, `timestamp` |
| `GROUP_LIST` | Send user's existing groups | `groups` (list of GroupInfo objects) |
| `CHAT_HISTORY_REQUEST` | Request previous messages | `sender`, `chatType` (PRIVATE/GROUP/BROADCAST), `targetId` |
| `CHAT_HISTORY` | Return retrieved chat history | `chatType`, `targetId`, `messages` (list of ChatMessage objects) |
| `ERROR` | Transmit server error notice | `content` (error details) |

---

## 🗄️ SQLite Database Schema

The database (`lan_messenger.db`) is automatically initialized upon server startup:

```sql
-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL COLLATE NOCASE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen INTEGER NOT NULL
);

-- 2. Groups Table
CREATE TABLE IF NOT EXISTS groups (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    created_by TEXT NOT NULL,
    created_at INTEGER NOT NULL
);

-- 3. Group Memberships
CREATE TABLE IF NOT EXISTS group_members (
    group_id TEXT NOT NULL,
    username TEXT NOT NULL COLLATE NOCASE,
    joined_at INTEGER NOT NULL,
    PRIMARY KEY (group_id, username),
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);

-- 4. Messages Table
CREATE TABLE IF NOT EXISTS messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sender TEXT NOT NULL COLLATE NOCASE,
    receiver TEXT COLLATE NOCASE,
    group_id TEXT,
    message_type TEXT NOT NULL,
    content TEXT NOT NULL,
    timestamp INTEGER NOT NULL
);

-- Fast Indexing
CREATE INDEX IF NOT EXISTS idx_messages_private ON messages(sender, receiver, timestamp);
CREATE INDEX IF NOT EXISTS idx_messages_group ON messages(group_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_messages_type ON messages(message_type, timestamp);
CREATE INDEX IF NOT EXISTS idx_group_members_user ON group_members(username);
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17 JDK or higher** installed and available in your `PATH`. Verify with:
  ```cmd
  java -version
  ```
- **Apache Maven 3.8+** (or use the included `build.bat` which auto-detects Maven).

---

### Step 1: Build the Project
Open a command prompt in the project directory and run:
```cmd
build.bat
```
Alternatively, with Maven directly:
```cmd
mvn clean package
```
This compiles all three modules, executes all tests, and produces standalone executable fat JARs:
- `lan-server\target\lan-server-1.0.0-executable.jar`
- `lan-client\target\lan-client-1.0.0-executable.jar`

---

### Step 2: Start the Central Server
Double-click `start-server.bat` or run:
```cmd
start-server.bat
```
*Direct Java command:*
```cmd
java -jar lan-server\target\lan-server-1.0.0-executable.jar
```
The **Server Control Dashboard** window will open. Notice the green **ONLINE** badge and the detected **LAN IP Address** (e.g. `192.168.1.16`).

---

### Step 3: Start Clients on the Same Machine (Local Testing)
Double-click `start-client.bat` or run:
```cmd
start-client.bat
```
You can run `start-client.bat` multiple times to open multiple client instances simultaneously (e.g., one for Alice, one for Bob, one for Charlie).
- Use `127.0.0.1` as the Server IP.
- Enter distinct usernames (`Alice`, `Bob`, `Charlie`).
- Enjoy real-time private messaging, group creation, and broadcasts!

---

## 🌐 Running Across Multiple Computers on a Local Network (LAN / Wi-Fi)

To connect other laptops or desktop PCs on the same Wi-Fi/LAN:

### 1. Configure the Host Computer (Server Machine)
1. **Find the Host Computer's Local IPv4 Address**:
   - The Server Dashboard automatically displays it in green at the top (e.g. `192.168.1.16`). Click **Copy IP**.
   - Or open Command Prompt and run `ipconfig`. Look for `IPv4 Address` under your Wi-Fi or Ethernet adapter.
2. **Allow Port 5000 in Windows Firewall**:
   - Right-click `allow-firewall-port.bat` and select **Run as administrator**.
   - *Or manually in an elevated command prompt:*
     ```cmd
     netsh advfirewall firewall add rule name="LAN Messenger Port 5000" dir=in action=allow protocol=TCP localport=5000
     ```

### 2. Distribute Client to Other Computers
Only the client JAR and startup script are needed on other computers:
1. Copy `lan-client\target\lan-client-1.0.0-executable.jar` and `start-client.bat` to the other computer (via USB drive or shared folder).
2. On the other computer, ensure Java 17 is installed.
3. Double-click `start-client.bat`.
4. In the **Server IP Address** box, enter the host computer's IPv4 address (e.g. `192.168.1.16`).
5. Enter a username (e.g. `Bob`) and click **Connect to LAN Server**.
6. The client connects instantly to the central server across your local network!

---

## 🧪 Automated Testing Verification

The project includes automated integration tests covering all 13 required scenarios:

| # | Test Scenario | Status | Verified Component |
|---|---|---|---|
| 1 | Start server successfully | ✅ PASS | `LanServer.start()`, ServerSocket bind to `0.0.0.0:5000` |
| 2 | Connect two clients | ✅ PASS | Two concurrent TCP connections, `LOGIN_SUCCESS` |
| 3 | Connect at least 3 clients simultaneously | ✅ PASS | Alice, Bob, Charlie concurrent sessions in `ClientManager` |
| 4 | Send private messages between two clients | ✅ PASS | Private message routing, recipient delivery, DB persistence |
| 5 | Send group messages | ✅ PASS | Group creation, membership verification, multi-cast delivery |
| 6 | Send broadcast messages | ✅ PASS | Global delivery to all active clients |
| 7 | Verify online user updates | ✅ PASS | `USER_JOINED` broadcast when new client registers |
| 8 | Verify timestamps | ✅ PASS | Epoch millis and formatted date/time on all messages |
| 9 | Disconnect a client | ✅ PASS | Clean socket closure, `USER_LEFT` broadcast to peers |
| 10 | Verify chat history after restart | ✅ PASS | Server stopped, new server started with SQLite DB, history restored |
| 11 | Test incorrect server IP | ✅ PASS | Handled gracefully with connection timeout and `IOException` |
| 12 | Test duplicate username | ✅ PASS | Second session with same username rejected with `LOGIN_FAILURE` |
| 13 | Test server shutdown & reconnection | ✅ PASS | Clients notified on server stop; clients successfully reconnect |

To run the complete test suite at any time:
```cmd
mvn test
```

---

## 📦 Packaging & Executables

Standalone executable JARs are built using `maven-shade-plugin`, bundling all dependencies (FlatLaf, Jackson, SQLite JDBC, SLF4J):

```cmd
mvn clean package -DskipTests
```

The resulting executable JARs can be run directly on any system with Java 17:
```cmd
# Server
java -jar lan-server\target\lan-server-1.0.0-executable.jar

# Client
java -jar lan-client\target\lan-client-1.0.0-executable.jar
```

---

## 📄 License
This project is open-source and free to use for personal, academic, and commercial demonstrations.
