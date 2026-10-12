# Campus Lost & Found Management System

> **Enterprise-Grade Java Web Application for University & College Campuses**  
> Built with Pure Java Servlets (Jakarta EE), JSP, JDBC, MySQL 8+, Bootstrap 5, Apache PDFBox, and Google ZXing.  
> Directly importable and runnable in **IntelliJ IDEA**, **JDK 25**, and **Apache Tomcat 11**.

---

## 📌 Project Overview

The **Campus Lost & Found Management System** is a complete, production-grade web application tailored for college campuses. It bridges the gap between students, professors, and campus administration to systematically recover, identify, verify, and return lost possessions.

### 🌟 Key Highlights & Capabilities
- **Strict Jakarta EE 11 / Servlet 6.1 Compatibility**: Optimized for Apache Tomcat 11 and JDK 25 without any legacy `javax.*` dependencies.
- **Pure Java MVC Architecture**: No Spring Boot, Spring MVC, or Hibernate abstractions. Built with clean Controller &rarr; Service &rarr; DAO &rarr; Database separation of concerns.
- **Intelligent Algorithmic Matching Engine**: Combines custom **Levenshtein Text Edit Distance** (40%), **Haversine Geospatial Distance Formula** (30%), **Temporal Proximity** (20%), and **Category Correlation** (10%) to discover high-confidence matches automatically.
- **Two-Factor Claim Handover Protocol**: Employs cryptographically secure 6-digit Time-based OTPs and Google ZXing 2D QR Code tokens to verify claimants before physical handover.
- **ACID Transactional Handover**: Item status transitions (`ACTIVE` &rarr; `CLAIMED` &rarr; `RETURNED`) execute inside atomic database transactions (`setAutoCommit(false)` with rollback protection).
- **Executive Administration & PDF Reporting**: Dynamic analytics dashboard with live Chart.js graphs, user role management, content moderation flags, unclaimed property disposal tracker, and automated downloadable PDF reports via Apache PDFBox.
- **Enterprise Security**: BCrypt password hashing (cost factor 12), parameterized `PreparedStatement` queries everywhere, HTTP session invalidation on logout, CSRF & XSS sanitization, and strict whitelist file-upload controls.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend Core** | Java 17+ (Compatible with JDK 25), Jakarta Servlet 6.1, Jakarta JSP 4.0, Jakarta JSTL 3.0 |
| **Persistence** | JDBC (Java Database Connectivity), MySQL Connector/J 8.4, Raw SQL PreparedStatements |
| **Security & Hashing** | jBCrypt 0.4 (Blowfish salt rounds = 12), Secure HTTP Session Management |
| **Algorithms** | Custom Levenshtein Distance (Text), Custom Haversine Formula (GPS Proximity) |
| **Third-Party Libraries** | Google ZXing 3.5.3 (QR Code Generation), Apache PDFBox 3.0.4 (PDF Engine), Google Gson 2.11.0 |
| **Frontend UI** | JSP, HTML5, CSS3, JavaScript (ES6), Bootstrap 5.3.3, Bootstrap Icons, Chart.js 4.4 |
| **Build Tool & Packaging** | Apache Maven 3.10+, WAR (Web Application Archive) |
| **Runtime Server** | Apache Tomcat 11.0.x (Jakarta EE 10/11) |
| **Database** | MySQL Server 8.0+ |

---

## 📂 Project Architecture & Directory Structure

```
CampusLostFoundPortal/
│
├── pom.xml                                      # Maven configuration (Jakarta EE, Tomcat 11, JDK 25)
├── README.md                                    # Comprehensive system documentation
│
├── database/
│   └── lost_found.sql                           # Complete DDL schema & BCrypt-hashed seed data
│
└── src/
    └── main/
        ├── resources/
        │   └── db.properties                    # Centralized database credentials & app configs
        │
        ├── java/
        │   └── com/
        │       └── lostfound/
        │           ├── config/
        │           │   ├── AppConfig.java       # Properties reader and environment manager
        │           │   └── DatabaseConnection.java # JDBC connection factory & resource cleaner
        │           │
        │           ├── model/
        │           │   ├── User.java            # User entity POJO (Role, status, details)
        │           │   ├── Item.java            # Item entity POJO (Type, location, GPS, photo)
        │           │   ├── Category.java        # Item category POJO
        │           │   ├── Claim.java           # Ownership claim POJO (Status, OTP, QR token)
        │           │   ├── HandoverRecord.java  # Physical handover audit POJO
        │           │   ├── Notification.java    # User notification alert POJO
        │           │   ├── Report.java          # Flagged / moderation report POJO
        │           │   ├── AuditLog.java        # Security audit trail POJO
        │           │   └── MatchResult.java     # Match calculation breakdown & rating POJO
        │           │
        │           ├── member1_auth/            # MEMBER 1: Authentication & User Accounts
        │           │   ├── controller/
        │           │   │   ├── LoginServlet.java
        │           │   │   ├── RegisterServlet.java
        │           │   │   ├── LogoutServlet.java
        │           │   │   ├── ProfileServlet.java
        │           │   │   └── ChangePasswordServlet.java
        │           │   ├── service/
        │           │   │   ├── UserService.java
        │           │   │   └── UserServiceImpl.java
        │           │   ├── dao/
        │           │   │   ├── UserDAO.java
        │           │   │   └── UserDAOImpl.java
        │           │   └── util/
        │           │       ├── BCryptUtil.java
        │           │       └── ValidationUtil.java
        │           │
        │           ├── member2_items/           # MEMBER 2: Item Inventory & Image Uploads
        │           │   ├── controller/
        │           │   │   ├── ItemServlet.java
        │           │   │   └── UploadServlet.java
        │           │   ├── service/
        │           │   │   ├── ItemService.java
        │           │   │   └── ItemServiceImpl.java
        │           │   ├── dao/
        │           │   │   ├── ItemDAO.java
        │           │   │   └── ItemDAOImpl.java
        │           │   └── util/
        │           │       └── ImageUploadUtil.java
        │           │
        │           ├── member3_matching/        # MEMBER 3: Search Engine & Match Algorithms
        │           │   ├── controller/
        │           │   │   ├── SearchServlet.java
        │           │   │   └── MatchServlet.java
        │           │   ├── service/
        │           │   │   ├── SearchService.java
        │           │   │   └── MatchService.java
        │           │   ├── algo/
        │           │   │   ├── LevenshteinAlgorithm.java
        │           │   │   └── HaversineAlgorithm.java
        │           │   └── dao/
        │           │       └── SearchDAOImpl.java
        │           │
        │           ├── member4_claims/          # MEMBER 4: Claims, OTP & Transactional Handover
        │           │   ├── controller/
        │           │   │   ├── ClaimServlet.java
        │           │   │   ├── HandoverServlet.java
        │           │   │   └── VerificationServlet.java
        │           │   ├── service/
        │           │   │   ├── ClaimService.java
        │           │   │   ├── ClaimServiceImpl.java
        │           │   │   └── OtpService.java
        │           │   ├── dao/
        │           │   │   ├── ClaimDAO.java
        │           │   │   └── ClaimDAOImpl.java
        │           │   └── util/
        │           │       └── QRCodeUtil.java
        │           │
        │           └── member5_admin/           # MEMBER 5: Dashboard, Reports & Analytics
        │               ├── controller/
        │               │   ├── AdminServlet.java
        │               │   ├── ReportServlet.java
        │               │   └── NotificationServlet.java
        │               ├── service/
        │               │   ├── AdminService.java
        │               │   ├── ReportService.java
        │               │   └── NotificationService.java
        │               ├── dao/
        │               │   ├── AdminDAO.java
        │               │   └── ReportDAO.java
        │               └── util/
        │                   └── PdfExporter.java
        │
        └── webapp/
            ├── index.jsp                        # Homepage with live metrics & quick actions
            ├── login.jsp                        # User sign-in with 1-click test credentials
            ├── register.jsp                     # Student & Faculty registration
            │
            ├── includes/
            │   ├── header.jsp                   # Meta tags, Bootstrap 5 CDN, Chart.js
            │   ├── navbar.jsp                   # Role-based navigation header
            │   ├── footer.jsp                   # Institutional footer & scripts
            │   └── alerts.jsp                   # Dismissible flash notification alerts
            │
            ├── auth/
            │   ├── profile.jsp                  # User profile viewer & editor
            │   └── change-password.jsp          # Credential update form
            │
            ├── items/
            │   ├── report-lost.jsp              # Lost property submission with GPS auto-fill
            │   ├── report-found.jsp             # Found property submission
            │   ├── item-details.jsp             # Item details with claim & flag triggers
            │   ├── my-items.jsp                 # Current user's reported inventory
            │   ├── gallery.jsp                  # Filterable grid of campus items
            │   └── edit-item.jsp                # Ownership-protected update form
            │
            ├── search/
            │   ├── search.jsp                   # Multi-parameter search entry
            │   ├── filters.jsp                  # Modular criteria filter bar
            │   ├── results.jsp                  # Filtered result cards
            │   └── match-results.jsp            # 4-factor algorithm score breakdown
            │
            ├── claims/
            │   ├── claim-item.jsp               # Ownership claim submission form
            │   ├── my-claims.jsp                # Filed vs. received claims tabs
            │   ├── claim-details.jsp            # QR code display, OTP status & reviews
            │   ├── otp-verification.jsp         # Multi-factor token validator
            │   └── handover.jsp                 # Final atomic handover confirmation
            │
            ├── admin/
            │   ├── dashboard.jsp                # Metric cards, alerts & live audit stream
            │   ├── users.jsp                    # Account search, role updates, blocking
            │   ├── reports.jsp                  # Community moderation review queue
            │   ├── notifications.jsp            # Alert notification center
            │   ├── analytics.jsp                # Dynamic Chart.js visualizations
            │   └── disposal.jsp                 # Unclaimed aging inventory review
            │
            ├── error/
            │   ├── 400.jsp                      # Bad Request
            │   ├── 401.jsp                      # Unauthorized
            │   ├── 403.jsp                      # Forbidden Access
            │   ├── 404.jsp                      # Resource Not Found
            │   └── 500.jsp                      # Internal Server Error (Sanitized)
            │
            ├── css/
            │   └── style.css                    # Professional college theme stylesheet
            ├── js/
            │   └── app.js                       # Geolocation, image previews, client validations
            │
            ├── WEB-INF/
            │   └── web.xml                      # Servlet 6.0 deployment descriptor
            │
            └── uploads/
                └── items/                       # Safe storage folder for item images
```

---

## 🔐 Default Development Credentials

The database script automatically populates pre-configured test users with verified BCrypt password hashes:

| Role | Email Address | Plaintext Password | Access Level |
|---|---|---|---|
| **ADMIN** | `admin@college.com` | `Admin@123` | Full administrative control, user moderation, analytics, PDF export, content review |
| **STUDENT** | `student@college.com` | `Student@123` | Report lost/found items, browse, claim found items, track own inventory |
| **FACULTY** | `faculty@college.com` | `Faculty@123` | Faculty permissions, department logging, property handover verification |

> 💡 **Tip:** On `login.jsp`, click any of the **Quick Test Credentials** buttons to instantly fill the login form!

---

## 🧮 Algorithm Matching Engine Details

When an item is inspected, clicking **"Run Algorithm Match"** triggers `MatchService.java`:

$$\text{Total Score} = (\text{Text Score} \times 0.40) + (\text{Location Score} \times 0.30) + (\text{Date Score} \times 0.20) + (\text{Category Score} \times 0.10)$$

### 1. Text Similarity (40% Weight) — `LevenshteinAlgorithm.java`
- Manually implemented matrix edit distance algorithm with normalization (lowercase, trim, whitespace collapse).
- Augmented with token overlap / Jaccard containment so matching keywords (e.g., *"Wallet"* vs. *"Black Leather Tommy Hilfiger Wallet"*) receive accurate correlation.

### 2. Geospatial Proximity (30% Weight) — `HaversineAlgorithm.java`
- Calculates the spherical great-circle distance $d$ in kilometers using earth radius $R = 6371.0 \text{ km}$:
  $$a = \sin^2\left(\frac{\Delta\text{lat}}{2}\right) + \cos(\text{lat}_1)\cos(\text{lat}_2)\sin^2\left(\frac{\Delta\text{lon}}{2}\right)$$
  $$c = 2 \cdot \arctan2\left(\sqrt{a}, \sqrt{1-a}\right), \quad d = R \cdot c$$
- Threshold Scoring:
  - $0 \le d \le 0.10 \text{ km} \rightarrow 100\%$
  - $0.10 < d \le 0.50 \text{ km} \rightarrow 90\%$
  - $0.50 < d \le 1.00 \text{ km} \rightarrow 75\%$
  - $1.00 < d \le 2.00 \text{ km} \rightarrow 60\%$
  - $2.00 < d \le 5.00 \text{ km} \rightarrow 40\%$
  - $> 5.00 \text{ km} \rightarrow 20\%$
- Automatic text fallback if GPS coordinates are missing.

### 3. Date Proximity (20% Weight)
- Exact same day: $100\%$
- Within 3 days: $85\%$
- Within 7 days: $70\%$
- Within 14 days: $50\%$
- Within 30 days: $30\%$
- Beyond 30 days: $15\%$

### 4. Category Alignment (10% Weight)
- Identical category ID: $100\%$ | Different category: $0\%$

---

## 🗄️ Database Setup (MySQL 8+)

### Step 1: Open MySQL Command Line or MySQL Workbench
Log in as your MySQL root user:
```bash
mysql -u root -p
```

### Step 2: Import the Database Script
Execute the bundled SQL script located at `database/lost_found.sql`:
```sql
source c:/Users/gargv/Desktop/CampusLostFoundPortal/database/lost_found.sql;
```
*(Or in MySQL Workbench: File &rarr; Open SQL Script &rarr; select `database/lost_found.sql` &rarr; Execute).*

### Step 3: Configure Database Password
Open `src/main/resources/db.properties` and ensure `db.password` matches your local MySQL root password:
```properties
db.url=jdbc:mysql://localhost:3306/lost_found_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=UTF-8
db.username=root
db.password=CHANGE_THIS_PASSWORD
```

---

## 🔨 Maven Build Commands

From the project root directory:

```powershell
# 1. Clean build directory
mvn clean

# 2. Compile all source files
mvn compile

# 3. Package WAR application
mvn package
```

The resulting deployable WAR file is generated at:
```
target/LostFoundManagementSystem.war
```

---

## 💻 IntelliJ IDEA & Tomcat 11 Setup Guide

Follow these exact steps to run the application inside IntelliJ IDEA with Apache Tomcat 11:

### 1. Open the Project in IntelliJ IDEA
1. Launch **IntelliJ IDEA**.
2. Select **File &rarr; Open...**
3. Navigate to `c:\Users\gargv\Desktop\CampusLostFoundPortal` and select the directory (or select `pom.xml`).
4. Click **Open as Project**.

### 2. Verify Project SDK (JDK 25 / Java 17+)
1. Go to **File &rarr; Project Structure &rarr; Project**.
2. Set **SDK** to **JDK 25** (or installed JDK 17+ / 21+).
3. Set **Language Level** to **17 - Sealed types, pattern matching** (or SDK default).
4. Click **Apply &rarr; OK**.

### 3. Configure Apache Tomcat 11 Local Server
1. Go to **Run &rarr; Edit Configurations...**
2. Click the **+** (Add New Configuration) icon.
3. Select **Tomcat Server &rarr; Local** (If not visible, click *More items...*).
4. Name the configuration: `Tomcat 11`.
5. Under **Application server**, click **Configure...** and point to your Apache Tomcat 11 installation folder (e.g. `C:\apache-tomcat-11.0.x`).
6. Click the **Deployment** tab:
   - Click **+ &rarr; Artifact...**
   - Choose `LostFoundManagementSystem:war exploded`.
   - In **Application context**, enter: `/LostFoundManagementSystem`
7. Click the **Server** tab:
   - URL: `http://localhost:8080/LostFoundManagementSystem/`
   - HTTP port: `8080`
   - JRE: Select your JDK 25.
8. Click **Apply &rarr; OK**.

### 4. Run the Application
1. Click the green **Run** or **Debug** button in IntelliJ IDEA.
2. Tomcat will start and automatically open your default browser to:
   ```
   http://localhost:8080/LostFoundManagementSystem/
   ```

---

## 📋 Comprehensive Testing Checklist

### 1. Authentication & Security
- [x] **Registration**: Register a new student account (`test@college.com`). Verify success redirect and flash banner.
- [x] **Duplicate Email**: Attempt registering with `admin@college.com`. Verify duplicate rejection.
- [x] **Weak Password**: Attempt registering with password `< 8` chars. Verify validation error.
- [x] **Login**: Sign in with `student@college.com` / `Student@123`. Verify session established and role badge shown.
- [x] **Quick Test Presets**: Use 1-click credential buttons on `login.jsp`.
- [x] **Role Authorization**: Attempt accessing `/admin/dashboard` as a Student. Verify `403 Forbidden` error.
- [x] **Logout**: Click Logout. Verify session invalidation and flash confirmation.

### 2. Item Inventory
- [x] **Report Lost**: Submit lost item with campus location, date, and image. Verify redirect to details page.
- [x] **Report Found**: Submit found item with custody details.
- [x] **GPS Auto-Fill**: Click "GPS" button on reporting forms to populate coordinates via browser API.
- [x] **Image Upload**: Upload `.jpg`, `.png`, or `.webp` file (`< 5MB`). Verify safe UUID renaming.
- [x] **Ownership Protection**: Log in as student B. Verify cannot edit or delete student A's reported item.
- [x] **Gallery Filters**: Filter gallery by "Lost Only", "Found Only", or "All Items".

### 3. Search & Matching
- [x] **Multi-Criteria Search**: Search with keyword "Wallet", category "Wallet", and location "Library".
- [x] **Levenshtein Scoring**: Open item #1 details and click "Run Algorithm Match". Verify Levenshtein text score percentage.
- [x] **Haversine Distance**: Verify GPS distance percentage calculation.
- [x] **Composite Score**: Verify total score calculation $(40\% + 30\% + 20\% + 10\%)$ and Rating Badge (*Excellent Match / Good Match*).

### 4. Claims & Handover
- [x] **File Claim**: As a student, view a found item reported by another user. Click "Claim This Item". Provide reason and proof.
- [x] **Self-Claim Prevention**: Attempt to claim an item you reported yourself. Verify security policy blocks the request.
- [x] **Review Claim**: Log in as the found item reporter or Admin. View claim details and click "Approve Claim".
- [x] **OTP & QR Generation**: Verify 6-digit OTP code and ZXing QR code image generated on claim details page.
- [x] **OTP Verification**: Enter 6-digit OTP on `/claims/verify`. Verify transition to `VERIFIED`.
- [x] **Transactional Handover**: Select verification method, enter remarks, and submit handover. Verify item status updates to `RETURNED` and claim status to `COMPLETED`.

### 5. Administration & Reporting
- [x] **Dashboard Metrics**: Log in as `admin@college.com`. Verify live counts for users, items, claims, and reports.
- [x] **User Management**: Search users, toggle Block/Unblock, and change roles. Verify protection of the last remaining admin.
- [x] **Content Moderation**: Flag an item from the details page. View and resolve the report from `/admin/reports`.
- [x] **Analytics Charts**: Visit `/admin/analytics`. Verify dynamic Chart.js rendering of category counts and status distributions.
- [x] **PDF Audit Export**: Click "Export PDF Audit Report" from the dashboard. Verify immediate download of `LostFound_Campus_Report.pdf` with institutional header, summary counts, and item inventory table.

---

## 🛠️ Common Errors & Troubleshooting

| Issue / Error | Cause | Resolution |
|---|---|---|
| **`Access denied for user 'root'@'localhost'`** | Incorrect MySQL root password. | Update `db.password` in `src/main/resources/db.properties` to match your local MySQL server. |
| **`Unknown database 'lost_found_db'`** | Database script not executed yet. | Execute `database/lost_found.sql` using MySQL CLI or Workbench. |
| **`java.lang.ClassNotFoundException: com.mysql.cj.jdbc.Driver`** | MySQL JDBC Driver missing from classpath. | Run `mvn clean package` or ensure `mysql-connector-j` is included in Tomcat server libraries. |
| **`404 Not Found` when opening application** | Application context path mismatch. | In Tomcat configuration &rarr; Deployment &rarr; set Application context strictly to `/LostFoundManagementSystem`. |
| **`500 - java.lang.NoClassDefFoundError: jakarta/servlet/...`** | Older Tomcat version (Tomcat 9 or 8) used. | Apache Tomcat 11 is required for Jakarta EE 11 / Servlet 6.1 specification. |
| **`File upload size exceeds limit`** | Uploaded image $> 5 \text{ MB}$. | Choose an image file smaller than 5 MB or compress the picture before upload. |
| **`OTP Expired` error during handover** | 5-minute security timeout elapsed. | Re-open claim and re-approve to issue a fresh 5-minute verification OTP code. |

---

## 📄 License & Academic Attribution
Developed for university and college property recovery systems. Free to use and extend for academic projects, institutional operations, and portfolio demonstrations.
