# Module 2: Student Management
## Technical Documentation, Architecture Specification, & Operational Manual

---

## 1. Module Overview & Executive Summary

The **Student Management Module** serves as the master candidate directory and academic cohort management engine for **Seat-o-Matic** (Secure Exam Orchestration Cockpit). It provides authoritative candidate record keeping, academic hierarchy alignment (Branches, Sections, and Semesters), high-performance filtering, resilient CSV bulk imports, and exam qualification tracking with live seating status lookups.

Key subsystem capabilities:
1. **Authoritative Student Lifecycle Management:** Complete CRUD operations with strict alphanumeric roll number formatting, duplicate suppression, referential validation against academic metadata, and complete audit logging via `AuditPublisher`.
2. **Resilient Bulk CSV Ingestion:** Fault-tolerant CSV parser powered by Apache Commons CSV that detects in-file duplicates, pre-flight verifies database conflicts, auto-resolves branch/section/semester keys, and provides row-level validation reports.
3. **Dynamic Filtering & Instant Search:** Parameterized SQL search querying student roll numbers, names, departments, sections, semesters, and active status with server-side pagination and debounced JSON autocomplete.
4. **Examination Qualification & Seating Integration:** Cross-module tracking linking active students to examination syllabi (`exam_students`) and resolving desk positions (Room Code, Row, and Column) from active seating arrangements.

---

## 2. Requirement Traceability Matrix

| Requirement Area | Specification Details | Implemented Artifacts |
|---|---|---|
| **Database & Schema** | High-performance search indexes, reuse of `students`, `branches`, `sections`, `semesters`, `exam_students`. | `database/migration-student-module.sql`<br>`database/schema.sql` |
| **Data Models & DTOs** | Entity models, profile projection DTOs, filter criteria, and pagination containers. | `Student.java`, `Semester.java`<br>`StudentDetailDTO.java`<br>`StudentFilter.java`, `Page.java`<br>`BatchImportResult.java`, `BatchImportRow.java`<br>`StudentEligibilityDTO.java`, `BatchGroupStatsDTO.java` |
| **Data Access Layer** | Parameterized `PreparedStatement` DAO operations extending `BaseDAO`. | `AcademicLookupDao.java`<br>`StudentDao.java`<br>`StudentEligibilityDao.java` |
| **Core Service Layer** | Student lifecycle, uniqueness validation, academic verification, audit logging. | `StudentService.java` |
| **CSV Batch Processor** | Bulk student intake, template generation, roster CSV export, row-by-row error auditing. | `StudentImportService.java` |
| **Exam Eligibility** | Inter-module query contract, candidate qualification, desk location lookups. | `StudentEligibilityService.java`<br>`StudentQueryServiceImpl.java`<br>`StudentQueryService.java` |
| **Web Controllers** | Protected MVC servlets with role-based access control, CSRF tokens, and JSON APIs. | `StudentServlet.java` (`/students`)<br>`StudentImportServlet.java` (`/students/import`)<br>`web.xml` |
| **Responsive UI Views** | Cockpit design tokens, glassmorphism cards, debounced live search, dynamic cascading selects. | `student/students.jsp`<br>`student/student-form.jsp`<br>`student/student-detail.jsp` |
| **Automated Testing** | Comprehensive JUnit 5 and Mockito unit/controller suites. | `StudentServiceTest.java`<br>`StudentImportServiceTest.java`<br>`StudentEligibilityServiceTest.java`<br>`StudentServletTest.java` |

---

## 3. Database Schema & Indexing Architecture

### 3.1 Optimized Database Indexes (`database/migration-student-module.sql`)
The migration applies idempotent performance indexes to accelerate high-frequency queries:
```sql
-- 1. Accelerates semester-based batch queries and filtering
CREATE INDEX idx_students_semester ON students(semester_id);

-- 2. Accelerates candidate name lookups and text search
CREATE INDEX idx_students_name ON students(name);

-- 3. Composite index for batch cohort grouping
CREATE INDEX idx_students_section_semester ON students(section_id, semester_id);

-- 4. Fast exam candidate qualification cross-referencing
CREATE INDEX idx_exam_students_student ON exam_students(student_id);
CREATE INDEX idx_exam_students_exam ON exam_students(exam_id);
```

---

## 4. REST & MVC Endpoint Specifications

### 4.1 Web Controller Endpoints (`/students`)
All routes require authentication and are authorized strictly for `ADMIN` and `FACULTY` roles.

| Method | Route / Action | Parameters | Description |
|---|---|---|---|
| `GET` | `/students` | `q`, `branchId`, `sectionId`, `semesterId`, `status`, `page`, `pageSize` | Renders paginated student roster with cohort metrics and filter pills. |
| `GET` | `/students?action=new` | — | Renders student registration form with academic dropdowns. |
| `GET` | `/students?action=edit` | `id` (required) | Renders student edit form with populated candidate data. |
| `GET` | `/students?action=view` | `id` (required) | Renders student profile with examination eligibility and desk allocation. |
| `GET` | `/students?action=search` | `q` (query), `limit` | Returns JSON array of matched students for instant live autocomplete. |
| `GET` | `/students?action=sections` | `branchId` (required) | Returns JSON array of sections for dynamic cascading dropdowns. |
| `GET` | `/students?action=export` | filter parameters | Streams full CSV file of filtered candidates. |
| `GET` | `/students?action=template` | — | Streams sample CSV template for bulk imports. |
| `POST` | `/students` | `action=create`, `_csrf`, fields | Validates and registers a new student record. |
| `POST` | `/students` | `action=update`, `_csrf`, `id`, fields | Validates and updates an existing student profile. |
| `POST` | `/students` | `action=toggle-status`, `_csrf`, `id` | Toggles candidate status between `ACTIVE` and `INACTIVE`. |
| `POST` | `/students` | `action=delete`, `_csrf`, `id` | Deletes student record after foreign key verification. |

### 4.2 Multipart CSV Import Endpoint (`/students/import`)
- **Method:** `POST`
- **Enctype:** `multipart/form-data`
- **Fields:** `file` (.csv), `_csrf`
- **CSV Headers Accepted:** `enrollment_no`, `name`, `branch_code`, `section`, `semester`, `status`
- **Behavior:** Parses records defensively; registers valid candidates; suppresses duplicates; reports detailed validation issues via flash alerts.

---

## 5. Security Architecture & Quality Verification

1. **Role-Based Access Control:** Routes are protected by `AuthenticationFilter` and `AuthorizationFilter` restricting student management to `ADMIN` and `FACULTY`. `INVIGILATOR` access is strictly forbidden with HTTP 403.
2. **CSRF Protection:** Form submissions require session-backed CSRF tokens validated by `CsrfFilter` supporting both `_csrf` form fields and `X-CSRF-TOKEN` headers.
3. **SQL Injection Immunity:** Every database operation in `StudentDao`, `AcademicLookupDao`, and `StudentEligibilityDao` utilizes parameterized `PreparedStatement` execution via `BaseDAO`.
4. **Audit Traceability:** Key student lifecycle events (`STUDENT_REGISTER`, `STUDENT_UPDATE`, `STUDENT_STATUS_TOGGLE`, `STUDENT_DELETE`, `STUDENT_BATCH_IMPORT`) publish structured audit logs.

