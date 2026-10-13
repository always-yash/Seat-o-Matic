# Seat-o-Matic

Seat-o-Matic helps an examination cell prepare and run exam sessions: maintain the student roster, assign rooms, generate and review seating plans, track paper handovers, and retain an audit history.

> Automation proposes an arrangement. Academic staff review and approve the final plan.

## Product shape

This repository is a Java 17 modular monolith. The modules share one deployable application and one database, with ownership boundaries reflected in Java packages and HTTP/service contracts. The web interface uses Spring MVC and Thymeleaf; domain rules, APIs, persistence, allocation and security are Java. The visual layer is plain HTML, CSS and JavaScript.

| Module | Owner | Package | Responsibility | Current foundation |
| --- | --- | --- | --- | --- |
| M1 Core architecture | Yash Choudhary | `com.seatomatic.core` | Authentication, role checks, shared contracts, seating allocation, workflow state, audit integration | Stored Admin/Faculty/Invigilator accounts, app shell, deterministic allocator, plan review/approval/lock |
| M2 Student & academic | Shivesh Surolia | `com.seatomatic.student` | Student directory, academic structure, eligibility and imports | Student records, validation, list/create and CSV import API/UI |
| M3 Exams & rooms | Shivraj Singh | `com.seatomatic.exams` | Exam lifecycle, room grids, capacity, registrations | Exam and room records, candidate registration, capacity validation |
| M4 Papers & invigilation | Sanjay Jangid | `com.seatomatic.papers` | Paper-set counts, room handovers and invigilator tasks | Plan-driven copy counts, configurable buffer, per-room runs and guarded transitions |
| M5 Analysis & audit | Ved Prakash Sharma | `com.seatomatic.analysis` | Reports, exports, operational analytics and audit | Dashboard metrics, append-style activity events, and audited locked-plan CSV export |

Each module should own its entities, repositories and service rules. Cross-module work should go through a service/API contract; avoid importing another module's repository into business logic. `core` owns shared security and integration concerns.

## Current workflow

1. Sign in to the exam-cell workspace.
2. Add students and rooms, then create an examination with a room and expected count.
3. Open the exam's candidate roster and register eligible active students.
4. Generate a draft seating plan. The Java allocator interleaves branches, walks room rows in a snake pattern, and picks seats that reduce adjacent branch/section penalties. It also creates or refreshes the room's question-paper requirement from the candidate count plus the configured buffer. Candidate registration closes when the plan is generated so the roster cannot silently diverge from the saved seating plan.
5. Review the room map and score. A faculty member can move a student to an empty usable seat; the score is recalculated and the change is audited.
6. Advance the plan through `DRAFT → REVIEWED → APPROVED → LOCKED`. Only an Admin can lock; a locked plan cannot be moved or generated again.
7. Track paper runs through `PREPARING → READY → DISTRIBUTED → COLLECTED`.

The allocator treats each registered candidate as eligible for that examination. It rejects an empty roster and any roster larger than the room's usable capacity. The score is an explainable soft-constraint measure: adjacent students from the same branch add 10 points and students from the same section add 2 points. A lower score is preferred; the system does not claim a zero-conflict result unless the score is zero.

## Architecture

```text
Browser (server-rendered workspace + small fetch interactions)
  ├── Spring MVC pages: dashboard, directory, room, exam, paper and plan views
  └── /api/v1 JSON boundary
        ├── M1 Core: authentication, orchestration, allocation and workflow
        ├── M2 Student: directory and candidate eligibility
        ├── M3 Exams: sessions, rooms and candidate registration
        ├── M4 Papers: distribution state
        └── M5 Analysis: audit and reporting
             ↓
       Spring Data JPA → Flyway migrations → H2 (local) / MySQL (deployment)
```

All state-changing web requests use Spring Security CSRF protection. Passwords are encoded with BCrypt. Bean validation and service-level checks protect incoming records; database constraints protect room capacity, unique registrations, seat occupancy, and paper-run identity. Plan changes and their audit events run in the same database transaction. The plan's optimistic version field detects concurrent edits.

## Run locally

Requirements: JDK 17 and Maven 3.6.3 or later.

```powershell
$env:SEAT_ADMIN_USERNAME = "operator"
$env:SEAT_ADMIN_PASSWORD = "choose-a-local-password"
$env:PAPER_BUFFER_COPIES = "2"
mvn spring-boot:run
```

Open <http://localhost:8080>. With no environment overrides, the verified local sign-in is `operator` / `change-this-local-password`. The default local profile uses an in-memory H2 database and applies the initial Flyway migration with demo data at startup. On an empty database the app creates the first Admin from `SEAT_ADMIN_USERNAME` and `SEAT_ADMIN_PASSWORD`. If you used the commands above, sign in with the exact password you assigned there (`choose-a-local-password` in the example). Bootstrap credentials are used only when the account table is empty; changing environment variables does not change an existing account. Stopping and restarting the local app resets its in-memory accounts. The MySQL profile persists accounts and password recovery is not implemented yet.

For MySQL, create an empty database and set the standard Spring datasource environment variables:

```powershell
$env:DATABASE_URL = "jdbc:mysql://localhost:3306/seatomatic?serverTimezone=UTC"
$env:DATABASE_USERNAME = "seatomatic_app"
$env:DATABASE_PASSWORD = "your-secret"
$env:SEAT_ADMIN_USERNAME = "exam-admin"
$env:SEAT_ADMIN_PASSWORD = "a-long-unique-secret"
$env:PAPER_BUFFER_COPIES = "2"
$env:SPRING_PROFILES_ACTIVE = "prod"
mvn spring-boot:run
```

Flyway applies `src/main/resources/db/migration` to either local H2 (in MySQL compatibility mode) or MySQL. `PAPER_BUFFER_COPIES` sets the default per-room spare copy count. The `prod` profile requires datasource and login credentials from environment variables and enables Thymeleaf caching. The fallback login exists only in the default local profile.

## API outline

All API paths require authentication and CSRF on state-changing requests. Student, exam and room operations require Admin or Faculty; account administration and plan locking require Admin; paper handovers require Admin or Faculty until room-to-invigilator assignments are implemented.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET/POST` | `/api/v1/students` | List or add student records |
| `POST` | `/api/v1/students/import` | Import CSV rows with row-level validation results (2 MB and 5,000 rows maximum) |
| `GET/POST` | `/api/v1/users` | Admin-only list or create team accounts |
| `PATCH` | `/api/v1/users/{id}/active` | Admin-only pause or restore an account |
| `GET/POST` | `/api/v1/rooms` | List rooms or add a room grid |
| `GET/POST` | `/api/v1/exams` | List or schedule examinations |
| `GET/POST` | `/api/v1/exams/{id}/candidates` | Read or register eligible students |
| `POST` | `/api/v1/exams/{id}/seating-plan/generate` | Generate or regenerate the draft plan |
| `GET` | `/api/v1/exams/{id}/seating-plan` | Read the plan for an examination |
| `GET` | `/api/v1/seating-plans/{id}` | Read a plan by ID |
| `POST` | `/api/v1/seating-plans/{id}/move` | Move a student to an empty seat |
| `POST` | `/api/v1/seating-plans/{id}/transition` | Review, approve or lock a plan |
| `POST` | `/api/v1/papers/{id}/transition` | Advance a paper run's handover status |
| `GET` | `/api/v1/reports/locked-seating.csv` | Admin-only CSV export of locked plans and assigned seats; export is audited |

## Ownership and collaboration

- Keep package ownership aligned to the five modules above.
- Freeze API/service request and response records before module work is parallelized.
- Put shared schema changes and cross-module contracts through the core owner for review.
- Keep migrations additive and coordinate their version numbers before merging.
- Add module tests for validation, lifecycle rules, capacity, allocation score and audit behavior as those areas mature.

## Current gaps / next increments

This is the new foundation; the legacy lost-and-found code has been removed. Before calling it production-ready, complete password reset and account recovery, academic entities (branches/sections/subjects), blocked-seat editing, multi-room allocation, plan export/print, room-to-invigilator assignment, multiple paper-set configuration, remaining report exports, and automated coverage. CSV import accepts the `studentId,fullName,branch,section,semester` header and reports rejected rows while saving valid records. Admin accounts are stored as BCrypt hashes; the local H2 database and its bootstrap account reset on application restart, while MySQL accounts persist. The demo seed is intended for local evaluation only.
