# 🎓 Java Core Concepts — Complete Viva Preparation & Project Mapping Guide
> **Project Name:** LAN Messaging Application (Java 17, Swing, TCP Sockets, SQLite, Maven)  
> **Purpose:** 100% Comprehensive Viva Question & Answer Guide covering all 13 core Java topics with exact definitions, code mappings, and teacher presentation scripts in Hinglish & English.

---

## 📌 Quick Index of Topics
1. [Data Types (Primitive & Reference)](#1-data-types)
2. [Inheritance (`extends`, Polymorphism)](#2-inheritance)
3. [Abstract Classes & Interfaces (`implements`, Decoupling)](#3-abstract-classes-and-interfaces)
4. [Exception Handling (`try-catch`, `try-with-resources`)](#4-exception-handling)
5. [Java Collections Framework (`List`, `Map`, `Set`, Concurrency)](#5-java-collections-framework)
6. [Access Modifiers (`private`, `public`, `protected`, default)](#6-access-modifiers)
7. [`static` and `final` Keywords](#7-static-and-final-keywords)
8. [AWT and SWING UI Classes](#8-awt-and-swing-classes)
9. [Event Handling (Listeners, Event Dispatch Thread)](#9-event-handling)
10. [Multithreading (Sockets, Threads, ThreadPool, Concurrency)](#10-multithreading)
11. [Servlet and JSP (Comparison & Why TCP Sockets were chosen)](#11-servlet-and-jsp)
12. [Looping in Java (`while`, `for`, `for-each`, ResultSet iterator)](#12-looping-in-java)
13. [JVM, JRE, and JDK Architecture](#13-jvm-jre-jdk)

---

## 1. Data Types

### 📖 Theory & Definition
Java is a **strongly typed language**. Every variable must have a declared type. Types are divided into:
1. **Primitive Data Types (8 types):** Store raw binary values directly in memory (Stack).  
   `byte` (8-bit), `short` (16-bit), `int` (32-bit), `long` (64-bit), `float` (32-bit), `double` (64-bit), `char` (16-bit Unicode), `boolean` (true/false).
2. **Non-Primitive / Reference Data Types:** Store references/memory addresses pointing to objects in the Heap.  
   Classes (`String`, `Socket`, `ChatMessage`), Interfaces (`List`, `Map`), Arrays (`byte[]`).

### 💻 Where & How Used in Our Project
* **`int`**: Used for network port numbers (e.g. `int port = 5000` in [ServerMain.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/ServerMain.java#L22)), database primary keys (`id INTEGER`), and buffer sizes (`65536`).
* **`long`**: Used for millisecond timestamps (`long timestamp = System.currentTimeMillis()`) and attachment file sizes (`Long fileSize` in [ChatMessage.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-common/src/main/java/com/lanmessenger/common/model/ChatMessage.java)).
* **`boolean`**: Used for connection state flags (`boolean running`, `boolean authenticated` in [ClientHandler.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/net/ClientHandler.java#L29)).
* **`String`**: Used for usernames, chat messages, group names, Base64 file payloads, and IP addresses.
* **`byte[]`**: Used when reading file data from disk before Base64 encoding.

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, hamare project me primitive data types jaise `int` port numbers aur buffer size ke liye, `long` high-precision timestamps aur file size ke liye, aur `boolean` connection states track karne ke liye use hue hain. Non-primitive reference types me `String` chat text aur Base64 attachment data store karta hai, aur complex objects jaise `NetworkMessage` aur `Socket` network communication handle karte hain."*

---

## 2. Inheritance

### 📖 Theory & Definition
Inheritance is a core OOP pillar where a child/subclass inherits properties and methods from a parent/superclass using the **`extends`** keyword. It promotes **code reusability** and enables **runtime polymorphism (Method Overriding)**.

### 💻 Where & How Used in Our Project
1. **Desktop GUI Window Inheritance (`extends JFrame`):**
   * [MainChatFrame.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/ui/MainChatFrame.java#L39): `public class MainChatFrame extends JFrame implements ClientNetworkListener`
   * [LoginFrame.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/ui/LoginFrame.java#L24): `public class LoginFrame extends JFrame implements ClientNetworkListener`
   * [ServerConsoleFrame.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/ui/ServerConsoleFrame.java#L27): `public class ServerConsoleFrame extends JFrame implements ServerEventListener`
   * *Why?* Subclasses inherit windowing behaviors like `setVisible()`, `setTitle()`, `setSize()`, and `setContentPane()`.
2. **Custom Component Inheritance (`extends JPanel`):**
   * [MessageBubblePanel.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/ui/MessageBubblePanel.java#L20): `public class MessageBubblePanel extends JPanel`  
     Overrides `paintComponent(Graphics g)` to custom-render rounded chat bubbles, thumbnails, and borders.
   * [ChatConversationPanel.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/ui/ChatConversationPanel.java#L26): `public class ChatConversationPanel extends JPanel`
   * [SidebarPanel.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/ui/SidebarPanel.java#L22): `public class SidebarPanel extends JPanel`

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, humne UI development me hierarchical inheritance use ki hai. Hamari main screens jaise `MainChatFrame`, `LoginFrame`, aur `ServerConsoleFrame` Java Swing ki `JFrame` class ko extend karti hain. Aur chat message cards ke liye `MessageBubblePanel` ne `JPanel` ko extend karke `paintComponent()` method ko override kiya hai custom bubble styling ke liye."*

---

## 3. Abstract Classes and Interfaces

### 📖 Theory & Definition
* **Interface (`interface`):** A 100% abstract contract specifying *what* a class must do, but not *how*. Achieves full abstraction and multiple inheritance. Implemented using **`implements`**.
* **Abstract Class (`abstract class`):** A class that cannot be instantiated and can contain both abstract methods (without body) and concrete methods (with body).

### 💻 Where & How Used in Our Project
1. **Core Interfaces for Event-Driven Network Architecture:**
   * **`ClientNetworkListener`** ([ClientNetworkListener.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/net/ClientNetworkListener.java)):  
     Defines callbacks like `onPrivateMessageReceived()`, `onUserJoined()`, `onDisconnected()`.  
     *Why?* It **decouples** network TCP logic from Swing UI. `NetworkClient` doesn't know anything about Swing; it just notifies the interface!
   * **`ServerEventListener`** ([ServerEventListener.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/net/ServerEventListener.java)):  
     Implemented by `ServerConsoleFrame` to update the GUI client table and server logs.
   * **`Runnable` (Standard Java Interface):**  
     Implemented by [ClientHandler.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/net/ClientHandler.java#L16) to execute per-client socket listening in a separate thread.
2. **Abstract Classes in Swing:**
   * **`WindowAdapter`** in `MainChatFrame.java`: Anonymous subclass of abstract `WindowAdapter` to cleanly handle window close events (`windowClosing()`) without implementing all 7 methods of `WindowListener`.

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, humne Architecture me Loose Coupling achieve karne ke liye Interfaces use kiye hain. Jaise `ClientNetworkListener` interface socket networking aur Swing UI ke beech bridge ka kaam karta hai. Isse network thread UI ko directly touch nahi karta, balki interface callbacks trigger karta hai. Aur multithreading ke liye `ClientHandler` class ne `Runnable` interface ko implement kiya hai."*

---

## 4. Exception Handling

### 📖 Theory & Definition
Mechanism to handle runtime errors gracefully without crashing the program.
* **Checked Exceptions:** Checked at compile time (e.g. `IOException`, `SQLException`).
* **Unchecked Exceptions (Runtime):** Occur during execution (e.g. `NullPointerException`, `NumberFormatException`).
* **Keywords:** `try`, `catch`, `finally`, `throw`, `throws`.
* **Try-with-resources (`AutoCloseable`):** Automatically closes streams and database connections.

### 💻 Where & How Used in Our Project
1. **Network I/O & Socket Errors:**
   * In [NetworkClient.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/net/NetworkClient.java#L54) and [LanServer.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/net/LanServer.java#L59), `throws IOException` handles socket binding and connections.
   * In `ClientHandler.java`, `try-catch(IOException e)` catches sudden client disconnections (cable unplugged or client closed) and safely executes `close()` in the `finally` block to prevent socket leaks.
2. **Try-With-Resources for Safe Database Management:**
   * In [MessageDao.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/db/MessageDao.java#L69) and [DatabaseManager.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/db/DatabaseManager.java#L43):
     ```java
     try (Connection conn = dbManager.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
          ...
     } catch (SQLException e) { ... }
     ```
     Connections, PreparedStatements, and ResultSets automatically close, preventing memory and file locks.
3. **NumberFormatException & Malformed JSON Handling:**
   * In `LoginFrame.java` to validate that port is a valid integer between 1 and 65535.
   * In `ClientHandler.java` to catch corrupt packets without crashing the server.

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, network aur database dono me Exception Handling critical hoti hai. Humne checked exceptions jaise `IOException` aur `SQLException` ko handle kiya hai. Sockets me sudden disconnect par server crash hone se bachane ke liye `try-catch-finally` use kiya hai. Aur database operations me Java 7+ ka **Try-with-resources** use kiya hai taaki `Connection` aur `PreparedStatement` automatically close ho sakein aur database lock ya memory leak na ho."*

---

## 5. Java Collections Framework

### 📖 Theory & Definition
A unified architecture of interfaces and classes for storing and manipulating groups of objects in memory. Rooted in `java.util.Collection` and `java.util.Map`.

### 💻 Where & How Used in Our Project
1. **`ConcurrentHashMap<String, ClientHandler>`** in [ClientManager.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/net/ClientManager.java#L23):
   * *Why?* Multiple clients connect and disconnect on different threads simultaneously. `ConcurrentHashMap` provides **lock-striping and thread-safety** for O(1) user lookups and registrations without blocking the whole table.
2. **`CopyOnWriteArrayList<ClientNetworkListener>`** in [NetworkClient.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/net/NetworkClient.java#L28):
   * *Why?* Prevents `ConcurrentModificationException` when event listeners are added or removed while iterating through callbacks.
3. **`ArrayList<ChatMessage>`** in [MessageDao.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/db/MessageDao.java#L60):
   * Dynamically sized list to fetch and sort chat history from SQLite.
4. **`Set<String>` / `LinkedHashSet<String>`** in [LanServer.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/net/LanServer.java#L356):
   * Ensures that group members contain **no duplicates** while preserving insertion order.

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, humne project me high-concurrency collection classes use kiye hain. Server par active users aur unke socket handlers store karne ke liye `ConcurrentHashMap` use kiya hai jo multi-threading me safe hota hai. Network listeners ke liye `CopyOnWriteArrayList` use kiya hai taaki iteration ke dauran `ConcurrentModificationException` na aaye. Aur chat history ke liye `ArrayList` aur group members ke unique IDs ke liye `LinkedHashSet` use kiya hai."*

---

## 6. Access Modifiers

### 📖 Theory & Definition
Keywords that control the visibility/scope of classes, constructors, methods, and variables:
* **`private`**: Accessible only within the same class. (Core of **Encapsulation**).
* **Default (package-private)**: Accessible only within classes in the same package.
* **`protected`**: Accessible within the same package and subclasses in other packages.
* **`public`**: Accessible from any other class in the application.

### 💻 Where & How Used in Our Project
* **`private` fields with public Getters/Setters (Encapsulation):**  
  In [ChatMessage.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-common/src/main/java/com/lanmessenger/common/model/ChatMessage.java) and [NetworkMessage.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-common/src/main/java/com/lanmessenger/common/model/NetworkMessage.java), all fields (`sender`, `content`, `timestamp`, `fileData`) are `private` to protect data integrity and accessed via public methods.
* **`public` Service APIs:**  
  Methods like `public void connect()`, `public void start()`, `public boolean sendMessage()` are public so client and UI can interact with networking services.
* **Package-private helper methods:**  
  Internal mapping methods inside `com.lanmessenger.server.db` (like `mapRow()`) are restricted to their package.

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, humne OOPs ka **Encapsulation** principle implement karne ke liye access modifiers ka strict use kiya hai. Sabhi model classes jaise `ChatMessage` me data fields ko `private` rakha gaya hai taaki bahar se koi direct modify na kar sake. Unhe access karne ke liye `public` getters aur setters diye gaye hain. Network engine me internal helper methods ko `private` aur external operations ko `public` banaya gaya hai."*

---

## 7. `static` and `final` Keywords

### 📖 Theory & Definition
* **`static`**: Belongs to the class itself rather than instances/objects. Stored in Metaspace/Class area. Accessible without creating an object.
* **`final`**:
  * Final variable: Value cannot be changed (constant).
  * Final method: Cannot be overridden by subclasses.
  * Final class: Cannot be extended/inherited (e.g. `String`, `JsonUtils`).

### 💻 Where & How Used in Our Project
1. **`public static final` Constants:**
   * In [NetworkConstants.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-common/src/main/java/com/lanmessenger/common/util/NetworkConstants.java):
     ```java
     public static final int DEFAULT_PORT = 5000;
     public static final String BROADCAST_TARGET = "ALL";
     public static final Charset CHARSET = StandardCharsets.UTF_8;
     ```
2. **`static` Utility Methods:**
   * In [JsonUtils.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-common/src/main/java/com/lanmessenger/common/util/JsonUtils.java#L39): `public static String toDelimitedJson(Object o)`
   * In [ServerConsoleFrame.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/ui/ServerConsoleFrame.java#L436): `public static String detectLanIpv4Addresses()` (detects local network IP without instantiating the UI).
3. **`final` Class & Immutable References:**
   * `public final class JsonUtils` prevents sub-classing.
   * `private final Socket socket;` in `ClientHandler` ensures the socket reference cannot be accidentally reassigned.

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, `public static final` combination ko humne global application constants ke liye use kiya hai jaise port number `5000`, UTF-8 charset, aur `ALL` broadcast channel. `static` methods humne utility classes me banaye hain jaise `JsonUtils.toDelimitedJson()` aur IP address detection. `final` keyword ko humne unchangeable references ke liye use kiya hai taaki runtime par socket reference corrupt na ho."*

---

## 8. AWT and SWING Classes

### 📖 Theory & Definition
* **AWT (Abstract Window Toolkit):** Heavyweight, platform-dependent GUI components (`java.awt`). Interacts directly with native OS peers.
* **SWING:** Lightweight, platform-independent GUI components written purely in Java (`javax.swing`). Pluggable Look and Feel (PLAF).

### 💻 Where & How Used in Our Project
1. **Swing Components Used:**
   * `JFrame`: Main container windows ([MainChatFrame.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/ui/MainChatFrame.java), [ServerConsoleFrame.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/ui/ServerConsoleFrame.java)).
   * `JPanel`: Layout containers for chat cards, sidebar, and control toolbars.
   * `JSplitPane`: Divides online user list on the left and conversation window on the right.
   * `JScrollPane`: Enables smooth scrolling through chat history and server logs.
   * `JTable` & `DefaultTableModel`: Displays active connected clients in real-time on the server dashboard.
   * `JFileChooser`: Native file dialog allowing users to pick images, videos, and PDFs up to 25MB.
   * `JOptionPane`: Alert popups for login validation and connection errors.
2. **AWT Classes Used:**
   * **Layout Managers:** `BorderLayout`, `FlowLayout`, `GridBagLayout`, `BoxLayout`.
   * **Graphics & Styling:** `Color`, `Font`, `Dimension`, `Graphics2D` (for rendering anti-aliased rounded message bubbles and avatar circles).
   * **System Integration:** `Toolkit.getDefaultToolkit().getSystemClipboard()` (Copy IP feature) and `Desktop.getDesktop().open()` (opening downloaded video/files).

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, hamara UI pure Java Swing framework par bana hai jo platform-independent lightweight GUI provide karta hai. Humne FlatLaf look-and-feel integrate karke modern desktop experience diya hai. Layout structuring ke liye AWT ke `BorderLayout` aur `GridBagLayout` use kiye hain, aur components me `JFrame`, `JSplitPane`, `JScrollPane`, `JTable`, aur `JFileChooser` ka use kiya hai. Custom graphics ke liye AWT ki `Graphics2D` se rounded chat bubbles render kiye hain."*

---

## 9. Event Handling

### 📖 Theory & Definition
The **Delegation Event Model** where an **Event Source** (e.g. Button) generates an **Event Object** (e.g. `ActionEvent`) and sends it to registered **Event Listeners**.
Swing is **single-threaded**; all UI updates must run on the **Event Dispatch Thread (EDT)**.

### 💻 Where & How Used in Our Project
1. **Action Listeners & Modern Java Lambdas:**
   * Send button click: `sendButton.addActionListener(e -> handleSend());`
   * Attachment button click: `attachButton.addActionListener(e -> handleAttachFile());`
   * Enter key press on text field: `inputArea.addActionListener(e -> handleSend());`
2. **Window Events (`WindowAdapter`):**
   * Handled graceful disconnection on clicking window close `[X]`:
     ```java
     addWindowListener(new WindowAdapter() {
         @Override public void windowClosing(WindowEvent e) { handleDisconnect(); }
     });
     ```
3. **Thread Safety via `SwingUtilities.invokeLater()`:**
   * When TCP socket thread receives a message from another machine, it **must not** touch the UI directly.
   * It schedules the update on the EDT using `SwingUtilities.invokeLater(() -> chatConversationPanel.appendMessage(msg));`.

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, Swing me Delegation Event Model use hota hai. Humne button clicks aur key events handle karne ke liye modern Java Lambda expressions ke sath `ActionListener` use kiya hai. Aur sabse important concept: Swing thread-safe nahi hota, isliye background TCP network thread se jab bhi naya message ya user join hota hai, hum use `SwingUtilities.invokeLater()` ke through **Event Dispatch Thread (EDT)** par safely render karte hain."*

---

## 10. Multithreading

### 📖 Theory & Definition
Executing multiple threads concurrently to maximize CPU utilization. In network applications, multithreading is essential so one slow client or network wait does not freeze the entire application.

### 💻 Where & How Used in Our Project
1. **Server Accept Thread:**  
   [LanServer.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/net/LanServer.java#L83) runs `acceptThread` continuously on `serverSocket.accept()` to wait for new incoming connections without blocking the server UI.
2. **Thread Pool (`ExecutorService`):**  
   Server creates `Executors.newCachedThreadPool()` to allocate or reuse worker threads for each client connection (`clientPool.submit(handler)`).
3. **Dedicated Inbound Reader Thread (`Client-Receiver`):**  
   [NetworkClient.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-client/src/main/java/com/lanmessenger/client/net/NetworkClient.java#L73) spawns a daemon thread `listenForServerMessages` that continuously blocks on `reader.readLine()`.
4. **Thread Synchronization & Atomic Variables:**  
   * `synchronized boolean sendMessage(...)` prevents two threads from interleaving bytes on the same TCP output stream.
   * `AtomicBoolean running`, `AtomicBoolean connected` prevent race conditions without heavy locking.

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, Multithreading hamare project ki reedh ki haddi (backbone) hai. Agar multithreading na hoti toh ek socket par `readLine()` block hote hi poora UI freeze ho jata. Server par humne `CachedThreadPool` use kiya hai jisse har client ko ek alag worker thread milta hai. Socket streams par race conditions se bachne ke liye `synchronized` write methods aur `AtomicBoolean` flags use kiye hain."*

---

## 11. Servlet and JSP

### 📖 Theory & Definition
* **Servlet:** A Java class running inside a Web Container (like Apache Tomcat) that extends the capabilities of a server by generating dynamic responses to **HTTP requests** (`doGet`, `doPost`).
* **JSP (JavaServer Pages):** A server-side technology allowing developers to insert Java code into HTML pages using scriptlets `<% ... %>` or JSTL tags. JSP files are internally compiled into Servlets.

### 💡 Why Our Project Uses Raw TCP Sockets Instead of Servlet/JSP (Crucial Viva Question!)
Teacher will definitely ask: *"Aapne Servlet aur JSP kyu nahi use kiya?"*

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir/Ma'am, Servlet aur JSP **Web-tier (HTTP Protocol - Layer 7)** technologies hain, jo mainly browser-based stateless web pages ke liye design ki gayi hain.  
> Lekin hamara project ek **High-Performance Desktop LAN Messenger** hai jise sub-millisecond real-time communication chahiye.  
> HTTP stateless hota hai jisme server client ko directly push nahi kar sakta (polling karni padti hai).  
> Isliye humne **Transport Layer (Layer 4) Raw TCP Sockets** aur Java Swing use kiya hai, jisse:  
> 1. Persistent full-duplex bi-directional connection milta hai.  
> 2. Web server (Tomcat/JBoss) ka koi extra overhead nahi hota.  
> 3. Zero-delay direct media and file streaming possible hoti hai."*

---

## 12. Looping in Java

### 📖 Theory & Definition
Control flow structures that repeat a block of code until a condition is satisfied:
* `while` loop: Condition evaluated before executing body (best when iteration count is unknown).
* `for` loop: Traditional counter-based loop (`for(init; condition; update)`).
* Enhanced `for-each` loop: Clean iteration over Arrays and Collections (`Iterable`).

### 💻 Where & How Used in Our Project
1. **Network Stream Listening Loop (`while`):**
   * In [ClientHandler.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/net/ClientHandler.java#L48):
     ```java
     while (!closed.get() && (line = reader.readLine()) != null) { ... }
     ```
     Keeps reading packets line-by-line until the connection closes.
2. **Server Accept Loop (`while`):**
   * In `LanServer.java`: `while (running.get()) { Socket client = serverSocket.accept(); ... }`
3. **Database ResultSet Iteration (`while`):**
   * In [MessageDao.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/db/MessageDao.java#L78):
     ```java
     while (rs.next()) { list.add(mapRow(rs)); }
     ```
4. **Enhanced `for-each` Loop:**
   * In `LanServer.java`: Broadcasting messages to all connected clients:
     ```java
     for (String onlineUser : clientManager.getOnlineUsernames()) {
         clientManager.sendTo(onlineUser, forwardMsg);
     }
     ```
5. **Traditional `for` Loop:**
   * In [ServerMain.java](file:///d:/Desktop/z%20Java%20Bv%20project/lan-server/src/main/java/com/lanmessenger/server/ServerMain.java#L26) for parsing command line arguments (`--port`, `--db`, `--headless`).

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, humne conditions ke hisaab se loops use kiye hain. Network streaming aur JDBC ResultSet fetch karne ke liye `while` loop use kiya hai kyunki data kab tak aayega pehle se fixed nahi hota. Broadcast messages aur group members ko dispatch karne ke liye enhanced `for-each` loop use kiya hai. Aur CLI arguments parse karne ke liye indexed `for` loop use kiya hai."*

---

## 13. JVM, JRE, JDK

### 📖 Theory & Definition
The 3 fundamental building blocks of Java Execution:
1. **JDK (Java Development Kit):** The full developer suite containing `javac` (compiler), `jar` (archiver), `javadoc`, debugger, and the JRE.
2. **JRE (Java Runtime Environment):** Contains the JVM + core Java class libraries (`rt.jar`, modules) needed to run compiled Java applications.
3. **JVM (Java Virtual Machine):** The abstract computing machine that executes compiled **Java Bytecode (`.class`)**. Responsible for platform independence (*"Write Once, Run Anywhere"*), Just-In-Time (JIT) compilation, and automatic Garbage Collection (GC).

```text
+--------------------------------------------------------------+
| JDK (Development Kit: javac, jar, jdb, tools)                |
|  +--------------------------------------------------------+  |
|  | JRE (Runtime Environment: Core Libraries, java.base)   |  |
|  |  +--------------------------------------------------+  |  |
|  |  | JVM (Execution Engine: JIT, Interpreter, GC)     |  |  |
|  |  +--------------------------------------------------+  |  |
|  +--------------------------------------------------------+  |
+--------------------------------------------------------------+
```

### 💻 Where & How Used in Our Project
* **JDK 17**: We used the `javac` compiler in JDK 17 to compile our 3 Maven modules (`lan-common`, `lan-server`, `lan-client`) with modern Java features (records, text blocks `"""`, pattern matching `switch`).
* **JVM**: When running `start-server.bat` or `start-client.bat`, the JVM allocates the **Heap Memory** for storing UI panels, image buffers, and cached messages, while the **Garbage Collector** automatically frees closed socket resources and detached UI components.
* **Uber JAR Packaging**: Using `maven-shade-plugin`, we bundled compiled bytecode and third-party dependencies (SQLite JDBC, FlatLaf, Jackson) into executable JARs runnable on any OS having a compatible JRE/JVM.

### 🗣️ Teacher ke samne kya bolna hai (Viva Script):
> *"Sir, JDK developer ke liye hota hai jisme humne `javac` se project compile kiya aur Maven se build banayi. JRE client machine par program run karne ke liye environment aur libraries provide karta hai. Aur JVM actual bytecode ko machine code me convert karke execute karta hai aur Garbage Collection ke zariye unused chat buffers aur network objects ko automatically clean karta hai."*

---

## 🎯 Quick Cheat-Sheet Summary Table for Viva

| Topic | Definition in 1 Line | Project Class / File Reference |
| :--- | :--- | :--- |
| **1. Data Types** | Value types (primitive: stack) vs reference types (heap) | `int port`, `long timestamp`, `String content` in `ChatMessage.java` |
| **2. Inheritance** | Child class acquiring parent properties via `extends` | `MainChatFrame extends JFrame`, `MessageBubblePanel extends JPanel` |
| **3. Abstract/Interface** | Pure contract via `interface` and template via `abstract` | `implements ClientNetworkListener`, `implements Runnable` in `ClientHandler` |
| **4. Exceptions** | Handling runtime errors safely via `try-catch-finally` | `try-with-resources` in `MessageDao`, `IOException` in socket reader |
| **5. Collections** | Framework for dynamic groups of objects in memory | `ConcurrentHashMap` in `ClientManager`, `ArrayList` in `MessageDao` |
| **6. Access Modifiers** | Visibility scope: `private`, default, `protected`, `public` | `private` encapsulated fields in `ChatMessage`, `public` socket APIs |
| **7. static & final** | `static` = class level; `final` = constant/immutable | `static final int DEFAULT_PORT = 5000` in `NetworkConstants.java` |
| **8. AWT & Swing** | AWT is heavyweight; Swing is pure Java lightweight GUI | `JFrame`, `JSplitPane`, `JTable`, `JFileChooser`, `Graphics2D` |
| **9. Event Handling** | Delegation Event Model + EDT thread safety | `ActionListener` lambdas, `SwingUtilities.invokeLater()` |
| **10. Multithreading** | Concurrent execution of multiple threads | `acceptThread`, `ClientHandler Runnable`, `CachedThreadPool` |
| **11. Servlet & JSP** | Web HTTP tier (vs our Low-Latency Layer-4 TCP Sockets) | Explain why TCP Sockets are faster & bi-directional vs HTTP polling |
| **12. Looping** | Repeated execution control structures | `while (reader.readLine() != null)`, `for-each` broadcast user loops |
| **13. JVM, JRE, JDK** | JDK (Tools) -> JRE (Libraries) -> JVM (Bytecode Execution) | Compiled on JDK 17, packaged with Maven Shade, run on JVM |
