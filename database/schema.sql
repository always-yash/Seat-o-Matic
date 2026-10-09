CREATE DATABASE IF NOT EXISTS seatomatic CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seatomatic;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','FACULTY','INVIGILATOR') NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_role (role)
);

CREATE TABLE branches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch_id BIGINT NOT NULL,
    name VARCHAR(60) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sections_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    UNIQUE KEY uq_section_branch (branch_id, name)
);

CREATE TABLE semesters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    number INT NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subjects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrollment_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    section_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_section FOREIGN KEY (section_id) REFERENCES sections(id),
    CONSTRAINT fk_students_semester FOREIGN KEY (semester_id) REFERENCES semesters(id),
    INDEX idx_students_section (section_id),
    INDEX idx_students_status (status)
);

CREATE TABLE exams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    exam_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('DRAFT','CONFIGURED','GENERATED','REVIEWED','APPROVED','LOCKED') NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exams_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE exam_students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exam_students_exam FOREIGN KEY (exam_id) REFERENCES exams(id),
    CONSTRAINT fk_exam_students_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_exam_students_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    UNIQUE KEY uq_exam_student_subject (exam_id, student_id, subject_id)
);

CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_code VARCHAR(50) NOT NULL UNIQUE,
    total_rows INT NOT NULL,
    total_columns INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    row_no INT NOT NULL,
    col_no INT NOT NULL,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seats_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    UNIQUE KEY uq_room_seat_position (room_id, row_no, col_no)
);

CREATE TABLE seating_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    status ENUM('GENERATED','REVIEWED','APPROVED','LOCKED','SUPERSEDED') NOT NULL DEFAULT 'GENERATED',
    score INT NULL,
    version INT NOT NULL DEFAULT 1,
    generated_by BIGINT NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seating_plan_exam FOREIGN KEY (exam_id) REFERENCES exams(id),
    CONSTRAINT fk_seating_plan_user FOREIGN KEY (generated_by) REFERENCES users(id)
);

CREATE TABLE seat_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    manually_modified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_assignments_plan FOREIGN KEY (plan_id) REFERENCES seating_plans(id),
    CONSTRAINT fk_seat_assignments_seat FOREIGN KEY (seat_id) REFERENCES seats(id),
    CONSTRAINT fk_seat_assignments_student FOREIGN KEY (student_id) REFERENCES students(id),
    UNIQUE KEY uq_plan_seat (plan_id, seat_id),
    UNIQUE KEY uq_plan_student (plan_id, student_id)
);

CREATE TABLE plan_conflicts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    type ENUM('SAME_BRANCH','SAME_SUBJECT') NOT NULL,
    seat_a BIGINT NOT NULL,
    seat_b BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conflict_plan FOREIGN KEY (plan_id) REFERENCES seating_plans(id),
    INDEX idx_conflict_plan (plan_id)
);

CREATE TABLE question_papers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    set_code VARCHAR(50) NOT NULL,
    total_copies INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_question_papers_exam FOREIGN KEY (exam_id) REFERENCES exams(id),
    CONSTRAINT fk_question_papers_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    UNIQUE KEY uq_exam_subject_set (exam_id, subject_id, set_code)
);

CREATE TABLE paper_distribution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    required INT NOT NULL DEFAULT 0,
    buffer INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_paper_distribution_plan FOREIGN KEY (plan_id) REFERENCES seating_plans(id),
    CONSTRAINT fk_paper_distribution_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_paper_distribution_paper FOREIGN KEY (paper_id) REFERENCES question_papers(id),
    UNIQUE KEY uq_plan_room_paper (plan_id, room_id, paper_id)
);

CREATE TABLE invigilator_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    invigilator_id BIGINT NOT NULL,
    status ENUM('ASSIGNED','ACTIVE','COMPLETED') NOT NULL DEFAULT 'ASSIGNED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invigilator_exam FOREIGN KEY (exam_id) REFERENCES exams(id),
    CONSTRAINT fk_invigilator_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_invigilator_user FOREIGN KEY (invigilator_id) REFERENCES users(id),
    UNIQUE KEY uq_exam_room_invigilator (exam_id, room_id, invigilator_id)
);

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_user_id BIGINT NULL,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id BIGINT NULL,
    details JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_actor (actor_user_id),
    INDEX idx_audit_target (target_type, target_id)
);
