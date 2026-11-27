+# LAN Messaging Application — Questions & Answers Guide

Is document mein aapke pooche gaye sabhi sawalon ke topic-wise practical aur detailed answers diye gaye hain, taaki project ko samajhna, demonstrate karna aur kisi dusre PC par chalana bilkul aasan ho.

---

## 📑 Table of Contents

1. [Q1: Application ko kaise run/show karein?](#q1-application-ko-kaise-runshow-karein)
2. [Q2: "Connection refused: no further information" Error kyun aayi thi?](#q2-connection-refused-no-further-information-error-kyun-aayi-thi)
3. [Q3: Message text screen par visible kyun nahi ho raha tha?](#q3-message-text-screen-par-visible-kyun-nahi-ho-raha-tha)
4. [Q4: `start-demo.bat`, `start-server.bat`, aur `start-client.bat` mein kya antar hai?](#q4-start-demobat-start-serverbat-aur-start-clientbat-mein-kya-antar-hai)
5. [Q5: 2 Clients ke beech aapas mein Conversation (Chat) kaise karein?](#q5-2-clients-ke-beech-aapas-mein-conversation-chat-kaise-karein)
6. [Q6: 2 Alag-alag PCs par chalana ho toh dusre PC par kya install karna padega?](#q6-2-alag-alag-pcs-par-chalana-ho-toh-dusre-pc-par-kya-install-karna-padega)
7. [Q7: Project se Unwanted Test Files kaise clean ki gayi?](#q7-project-se-unwanted-test-files-kaise-clean-ki-gayi)
8. [Q8: Project ki ZIP kisi doosre PC par bhejne par direct chalega ya koi issue aayega?](#q8-project-ki-zip-kisi-doosre-pc-par-bhejne-par-direct-chalega-ya-koi-issue-aayega)
9. [Q9: Desktop par Installed Application jaisa Icon / Shortcut kaise banayein?](#q9-desktop-par-installed-application-jaisa-icon--shortcut-kaise-banayein)
10. [Q10: Chat History Clear Kaise Karein aur Users ko Reset/Remove Kaise Karein?](#q10-chat-history-clear-kaise-karein-aur-users-ko-resetremove-kaise-karein)
11. [Q11: Links, Images, Videos aur Files kaise share karte hain?](#q11-links-images-videos-aur-files-kaise-share-karte-hain)

---

## Q1: Application ko kaise run/show karein?

### Answer:

Application ko show karne ke liye **2 sabse aasan tareeqe** hain:

### Tareeqa A: One-Click Demo (Sabse Fast)

Sirf **`start-demo.bat`** par double click karein.

- Yeh apne aap **Server**, **Client 1**, aur **Client 2** teeno windows ko screen par open kar dega.

### Tareeqa B: Manual Real-Life Execution

1. Pehle **`start-server.bat`** run karein (Server Dashboard open hoga aur port 5000 par listen karega).
2. Phir **`start-client.bat`** run karein (Client 1 login karega).
3. Dobara **`start-client.bat`** run karein (Client 2 login karega).

---

## Q2: "Connection refused: no further information" Error kyun aayi thi?

### Answer:

Aapke login screen par jo laal rang ki error aayi thi:

> `Connection failed: Connection refused: no further information`

### Wajah (Root Cause):

Networking architecture mein **Client hamesha kisi chalu Server ke paas jata hai**.

- Us waqt **Server on (chalu) nahi tha**.
- Jab Client ne `192.168.1.16:5000` par connect karne ki koshish ki aur port 5000 par koi Server sunne wala nahi mila, toh Windows Operating System ne connection reject kar diya (`WSAECONNREFUSED`).

### Solution:

Client mein "Connect" dabane se pehle **hamesha `start-server.bat` chalana zaroori hai**. Jab Server Dashboard par status `ONLINE` dikhe, tabhi client connect hoga.

---

## Q3: Message text screen par visible kyun nahi ho raha tha?

### Answer:

Aapke chat screenshot mein sender header (`Alice 21:46`) toh dikh raha tha, lekin neeche ka message text cut-off ho gaya tha.

### Wajah (Root Cause):

`MessageBubblePanel.java` mein Swing layout ki preferred height header create hone ke baad calculate ho rahi thi:

- Card ki height **fixed 28 pixels** par lock ho gayi thi.
- 28 pixels mein se 24 pixels header ne le liye, aur actual text area ko sirf **4 pixels** mile, jisse text cut-off hokar invisible ho gaya tha.

### Solution (Fixed in Code):

1. `MessageBubblePanel.java` ko update kiya gaya jisme **Dynamic Font Metrics aur Text Wrapping** lagaya gaya.
2. Chahe 1 line ka chhota message ho ya multi-line lamba paragraph, bubble card text ki required height automatically calculate karta hai.
3. Ab har message crystal clear display hota hai.

---

## Q4: `start-demo.bat`, `start-server.bat`, aur `start-client.bat` mein kya antar hai?

### Answer:

| File Name              | Kitni Windows Kholta Hai?              | Kaam                                                                      | Kab Use Karein?                                                    |
| :--------------------- | :------------------------------------- | :------------------------------------------------------------------------ | :----------------------------------------------------------------- |
| **`start-demo.bat`**   | **3 Windows** _(1 Server + 2 Clients)_ | **Ek hi click mein** Server + Client 1 + Client 2 sab start kar deta hai. | Ek hi computer par jaldi se poora project test/demo karne ke liye. |
| **`start-server.bat`** | **1 Window** _(Sirf Server)_           | Port 5000 par LAN Server aur SQLite database listener chalu karta hai.    | Jab main computer ko Central Server banana ho.                     |
| **`start-client.bat`** | **1 Window** _(Sirf 1 Client)_         | Ek user ka Chat Application kholta hai.                                   | Naye user ko login karne ya dusre PC par chalane ke liye.          |

---

## Q5: 2 Clients ke beech aapas mein Conversation (Chat) kaise karein?

### Answer:

### Step 1: Dono Clients Login Karein

- **Client 1:** Username `Priyansh` daal kar **Connect** click karein.
- **Client 2:** Username `Rahul` daal kar **Connect** click karein.

### Step 2: Sidebar mein Contact Select Karein (Sabse Zaroori)

1. **Priyansh ki window mein:**
   - Left side mein `CONTACTS` ke neeche `Rahul` green dot (`●`) ke saath dikhega.
   - **Rahul ke naam par CLICK karein** (Center panel mein Rahul ki private chat open hogi).
2. Neeche text box mein message likhein: `"Hello Rahul!"`
3. Keyboard se **Enter** dabayein ya **Send** button click karein.

### Step 3: Reply Dekhein

- Rahul ki window mein Priyansh ke naam ke aage unread badge `[1]` aayega.
- Rahul Priyansh ke naam par click karke reply bhejega.
- Real-time instant messaging chalne lagegi!

---

## Q6: 2 Alag-alag PCs par chalana ho toh dusre PC par kya install karna padega?

### Answer:

Dusre PC par **koi coding software, Git, ya Maven install karne ki bilkul zaroorat nahi hai!**

### Shartein:

1. Dono PC **same Wi-Fi router ya same LAN network** se jude hone chahiye.
2. Dusre PC par sirf **Java 17 (ya higher)** install hona chahiye.

### Setup Step-by-Step:

#### PC 1 (Server PC):

1. Ek baar `allow-firewall-port.bat` ko **Run as administrator** karein (taaki port 5000 unlock ho).
2. `start-server.bat` run karein aur upar dikh raha **Server IP** (jaise `192.168.1.16`) note karein.

#### PC 2 (Dusra Client PC):

1. Pendrive ya Wi-Fi se sirf yeh executable file copy karke le jayein:  
   👉 `lan-client\target\lan-client-1.0.0-executable.jar`
2. Dusre PC par is JAR file par double-click karein.
3. Login Screen mein bharein:
   - **Username**: Apna naam (jaise `Rahul`)
   - **Server IP**: PC 1 ka IP address daalein (jaise `192.168.1.16`)
   - **Port**: `5000`
4. Click karein **Connect** — dono computers aapas mein jud jayenge!

---

## Q7: Project se Unwanted Test Files kaise clean ki gayi?

### Answer:

Project ko production-ready aur light-weight banane ke liye:

1. **Delete ki gayi files:**
   - Sabhi `src/test` folders (`lan-common/src/test`, `lan-server/src/test`, `lan-client/src/test`).
   - Temporary `dependency-reduced-pom.xml` files.
   - Puraane surefire test logs aur dumpstream files.
2. **Surakshit rakhi gayi files:**
   - Saara core Java application code (`src/main/java`).
   - Batch scripts (`start-server.bat`, `start-client.bat`, `start-demo.bat`, `build.bat`, `allow-firewall-port.bat`).
   - `README.md` file aur SQLite database (`lan_messenger.db`).
   - Pre-built executable JAR files.

Clean-up ke baad project ko repackage karke verify kiya gaya: **`BUILD SUCCESS`**.

---

## Q8: Project ki ZIP kisi doosre PC par bhejne par direct chalega ya koi issue aayega?

### Answer:

**Bina kisi problem ke direct chalega!**

Keval yeh **3 baatein** dhyan mein rakhni hain:

1. **ZIP banate waqt `target` folder include karein:**
   - ZIP mein `lan-server\target` aur `lan-client\target` folders hone chahiye, kyunki inke andar pehle se compiled self-contained executable JARs maujood hain. Isse saamne wale ko Maven ki zaroorat nahi padegi.
2. **Java 17 requirement:**
   - Saamne wale PC par Java 17 install hona chahiye.
3. **Execution:**
   - ZIP extract karke seedhe `start-demo.bat` ya `start-server.bat` double-click karke chalaya ja sakta hai.

---

## Q9: Desktop par Installed Application jaisa Icon / Shortcut kaise banayein?

### Answer:
Project ke liye ek custom Navy Blue Messenger Icon (**`lan-messenger.ico`**) design kiya gaya hai aur saath mein ek One-Click Shortcut Installer banaya gaya hai:

### Kaise Install Karein (Desktop Par Icon Lana):
Sirf is file par double-click karein:
👉 **`install-desktop-shortcut.bat`**

Yeh turant aapke Windows Desktop par 3 ready-to-use desktop shortcuts create kar dega jisme official app icon laga hoga:
1. **`LAN Messenger (Demo)`**: Double click karte hi Server aur 2 Clients launch ho jayenge (bilkul normal software ki tarah).
2. **`LAN Messenger Client`**: Sirf Client application ka desktop icon.
3. **`LAN Server Console`**: Central Server Dashboard ka desktop icon.

---

## Q10: Chat History Clear Kaise Karein aur Users ko Reset/Remove Kaise Karein?

### Answer:
Aapke paas **3 aasan tareeqe** hain users ko hatane aur chat history delete karne ke:

### Tareeqa 1: Server Chalte Waqt Kisi Live User ko Kick/Remove Karna
1. **Server Dashboard** khol kar dekhein: Upar **"Connected Client Sessions"** table mein sabhi active users dikhte hain.
2. Jis user ko hatana ho, uski row par mouse se click karein.
3. Neeche **"Disconnect Selected"** button par click karein.
4. Wo user turant server se disconnect hokar bahar nikal diya jayega!

### Tareeqa 2: Server Console se Direct Saari History & Users Clear Karna
1. **Server Dashboard** mein **"Clear History & Reset Users"** button par click karein.
2. Confirmation dialog aayega: **Yes** click karein.
3. SQLite database se saare puraane messages, groups, aur registered users permanently delete ho jayenge aur database 100% fresh ban jayega!

### Tareeqa 3: One-Click Script se Fresh Reset Karna (Bina Server Khole)
1. Project folder mein is file par double-click karein:  
   👉 **`reset-database.bat`**
2. Keyboard se **`Y`** type karke **Enter** dabayein.
3. Yeh saare puraane SQLite database files ko delete kar dega.
4. Jab aap agli baar Server start karenge, toh bilkul **Zero/Fresh Database** banega, jisme koi puraana user ya message nahi hoga!

---

## Q11: Links, Images, Videos aur Files kaise share karte hain?

### Answer:
Application mein **Rich Media & File Attachment Sharing** full support ke saath add kar diya gaya hai:

### 1. 🔗 Clickable Web Links:
- Message box mein koi bhi URL likhein (jaise `https://google.com` ya `http://192.168.1.16:8080`).
- Chat bubble ke andar automatically ek blue color ka **`🔗 Open Link`** button ban jata hai.
- Button click karte hi link default browser mein turant open ho jata hai!

### 2. 🖼️ Images (Photos / Screenshots):
- Input box ke paas **`📎 Attach`** button par click karein.
- Koi bhi Image (`.png`, `.jpg`, `.jpeg`, `.gif`, `.webp`) select karein (25 MB limit).
- Chat stream mein image ka **Direct Thumbnail Preview** dikhai deta hai.
- Recipient neeche bane **`💾 Save Image`** button se image apne computer par save kar sakta hai.

### 3. 🎥 Videos & Documents:
- **`📎 Attach`** button se koi bhi Video (`.mp4`, `.mkv`, `.avi`) ya Document (`.pdf`, `.docx`, `.zip`) select karein.
- Chat card mein clean media card banega with:
  - 🎥 Video / File Name
  - Formatted file size (e.g. `4.2 MB`)
  - **`💾 Save / Open Video`** button jisse file save hokar default video player ya app mein turant open ho jaati hai!


