CREATE TABLE students (
    student_id VARCHAR(32) PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    branch VARCHAR(100) NOT NULL,
    section VARCHAR(30),
    semester INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_student_semester CHECK (semester > 0)
);

CREATE TABLE user_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    account_role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (account_role IN ('ADMIN', 'FACULTY', 'INVIGILATOR'))
);

CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    building VARCHAR(100) NOT NULL,
    seat_rows INT NOT NULL,
    seat_columns INT NOT NULL,
    capacity INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_room_grid CHECK (seat_rows > 0 AND seat_columns > 0 AND capacity > 0 AND capacity <= seat_rows * seat_columns)
);

CREATE TABLE exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(140) NOT NULL,
    subject VARCHAR(120) NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    expected_students INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    room_id BIGINT,
    CONSTRAINT fk_exam_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT chk_exam_students CHECK (expected_students >= 0)
);

CREATE TABLE exam_candidates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_student_id VARCHAR(32) NOT NULL,
    CONSTRAINT fk_candidate_exam FOREIGN KEY (exam_id) REFERENCES exams(id),
    CONSTRAINT fk_candidate_student FOREIGN KEY (student_student_id) REFERENCES students(student_id),
    CONSTRAINT uq_exam_candidate UNIQUE (exam_id, student_student_id)
);

CREATE TABLE seating_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    conflict_score INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_plan_exam FOREIGN KEY (exam_id) REFERENCES exams(id)
);

CREATE TABLE seat_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    student_student_id VARCHAR(32) NOT NULL,
    seat_row INT NOT NULL,
    seat_column INT NOT NULL,
    CONSTRAINT fk_assignment_plan FOREIGN KEY (plan_id) REFERENCES seating_plans(id),
    CONSTRAINT fk_assignment_student FOREIGN KEY (student_student_id) REFERENCES students(student_id),
    CONSTRAINT uq_plan_seat UNIQUE (plan_id, seat_row, seat_column),
    CONSTRAINT uq_plan_student UNIQUE (plan_id, student_student_id),
    CONSTRAINT chk_seat_position CHECK (seat_row > 0 AND seat_column > 0)
);

CREATE TABLE paper_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    set_code VARCHAR(40) NOT NULL,
    required_copies INT NOT NULL,
    buffer_copies INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PREPARING',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_paper_exam FOREIGN KEY (exam_id) REFERENCES exams(id),
    CONSTRAINT fk_paper_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT uq_paper_exam_room_set UNIQUE (exam_id, room_id, set_code),
    CONSTRAINT chk_paper_copies CHECK (required_copies >= 0 AND buffer_copies >= 0)
);

CREATE TABLE audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor VARCHAR(120) NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    details VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exams_starts_at ON exams(starts_at);
CREATE INDEX idx_audit_created_at ON audit_events(created_at);
CREATE INDEX idx_candidates_exam ON exam_candidates(exam_id);
CREATE INDEX idx_assignments_plan ON seat_assignments(plan_id);

INSERT INTO students (student_id, full_name, branch, section, semester) VALUES
('24E1CS001', 'Aarav Sharma', 'Computer Science', 'A', 5),
('24E1CS002', 'Meera Singh', 'Computer Science', 'A', 5),
('24E1EC014', 'Kabir Verma', 'Electronics', 'B', 5),
('24E1ME021', 'Anaya Gupta', 'Mechanical', 'A', 5),
('24E1CS018', 'Reyansh Joshi', 'Computer Science', 'B', 5),
('24E1EC009', 'Saanvi Rathore', 'Electronics', 'A', 5),
('24E1ME011', 'Vivaan Jain', 'Mechanical', 'B', 5),
('24E1CS024', 'Ishita Khan', 'Computer Science', 'A', 5);

INSERT INTO rooms (name, building, seat_rows, seat_columns, capacity) VALUES
('A-204', 'Academic block - 2nd floor', 6, 8, 46),
('A-208', 'Academic block - 2nd floor', 5, 8, 38),
('B-101', 'Science block - ground floor', 6, 6, 34);

INSERT INTO exams (code, name, subject, starts_at, expected_students, status, room_id) VALUES
('CS-501', 'Distributed systems', 'Distributed systems', CURRENT_TIMESTAMP + INTERVAL '2' DAY, 4, 'READY', 1),
('EC-503', 'Digital signal processing', 'Digital signal processing', CURRENT_TIMESTAMP + INTERVAL '2' DAY + INTERVAL '3' HOUR, 2, 'DRAFT', 2),
('ME-505', 'Thermal engineering', 'Thermal engineering', CURRENT_TIMESTAMP + INTERVAL '3' DAY, 2, 'READY', 3);

INSERT INTO exam_candidates (exam_id, student_student_id) VALUES
(1, '24E1CS001'), (1, '24E1EC014'), (1, '24E1ME021'), (1, '24E1CS002'),
(2, '24E1EC009'), (2, '24E1CS018'),
(3, '24E1ME011'), (3, '24E1CS024');

INSERT INTO paper_runs (exam_id, room_id, set_code, required_copies, buffer_copies, status) VALUES
(1, 1, 'SET-A', 4, 2, 'READY'), (2, 2, 'SET-A', 2, 2, 'PREPARING'), (3, 3, 'SET-A', 2, 2, 'DISTRIBUTED');

INSERT INTO audit_events (actor, action, entity_type, details, created_at) VALUES
('Exam cell', 'Plan reviewed', 'Seating plan', 'Room A-204 is ready for review.', CURRENT_TIMESTAMP - INTERVAL '18' MINUTE),
('R. Mehta', 'Room updated', 'Room', 'Blocked seats recorded in A-208.', CURRENT_TIMESTAMP - INTERVAL '1' HOUR),
('Exam cell', 'Paper count confirmed', 'Paper run', 'Set A buffer verified for Thermal engineering.', CURRENT_TIMESTAMP - INTERVAL '3' HOUR);
