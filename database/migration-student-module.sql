-- =============================================================================
-- Migration: Student Management Module Schema Optimization & Seed
-- Target: seatomatic database (MySQL 8.0+)
-- Author: Lead Student Feature Engineer
-- Description:
--   Adds high-performance lookup indexes for student search (name, roll number,
--   branch/section/semester) and exam eligibility queries, plus baseline seed data
--   for academic branches, sections, and semesters.
-- =============================================================================

USE seatomatic;

-- 1. Optimization indexes for students table
SET @exist_idx_stud_sem := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'students' AND index_name = 'idx_students_semester'
);
SET @sql_idx_stud_sem := IF(@exist_idx_stud_sem = 0, 'CREATE INDEX idx_students_semester ON students(semester_id)', 'SELECT 1');
PREPARE stmt_idx_stud_sem FROM @sql_idx_stud_sem;
EXECUTE stmt_idx_stud_sem;
DEALLOCATE PREPARE stmt_idx_stud_sem;

SET @exist_idx_stud_name := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'students' AND index_name = 'idx_students_name'
);
SET @sql_idx_stud_name := IF(@exist_idx_stud_name = 0, 'CREATE INDEX idx_students_name ON students(name)', 'SELECT 1');
PREPARE stmt_idx_stud_name FROM @sql_idx_stud_name;
EXECUTE stmt_idx_stud_name;
DEALLOCATE PREPARE stmt_idx_stud_name;

SET @exist_idx_stud_sec_sem := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'students' AND index_name = 'idx_students_section_semester'
);
SET @sql_idx_stud_sec_sem := IF(@exist_idx_stud_sec_sem = 0, 'CREATE INDEX idx_students_section_semester ON students(section_id, semester_id)', 'SELECT 1');
PREPARE stmt_idx_stud_sec_sem FROM @sql_idx_stud_sec_sem;
EXECUTE stmt_idx_stud_sec_sem;
DEALLOCATE PREPARE stmt_idx_stud_sec_sem;

-- 2. Optimization indexes for exam_students table (eligibility lookups)
SET @exist_idx_es_student := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'exam_students' AND index_name = 'idx_exam_students_student'
);
SET @sql_idx_es_student := IF(@exist_idx_es_student = 0, 'CREATE INDEX idx_exam_students_student ON exam_students(student_id)', 'SELECT 1');
PREPARE stmt_idx_es_student FROM @sql_idx_es_student;
EXECUTE stmt_idx_es_student;
DEALLOCATE PREPARE stmt_idx_es_student;

SET @exist_idx_es_exam := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'exam_students' AND index_name = 'idx_exam_students_exam'
);
SET @sql_idx_es_exam := IF(@exist_idx_es_exam = 0, 'CREATE INDEX idx_exam_students_exam ON exam_students(exam_id)', 'SELECT 1');
PREPARE stmt_idx_es_exam FROM @sql_idx_es_exam;
EXECUTE stmt_idx_es_exam;
DEALLOCATE PREPARE stmt_idx_es_exam;

-- 3. Academic baseline seeds
INSERT IGNORE INTO branches (id, code, name) VALUES
(1, 'CSE', 'Computer Science and Engineering'),
(2, 'ECE', 'Electronics and Communication Engineering'),
(3, 'MECH', 'Mechanical Engineering'),
(4, 'CIVIL', 'Civil Engineering'),
(5, 'IT', 'Information Technology');

INSERT IGNORE INTO semesters (id, number, name) VALUES
(1, 1, 'Semester 1'),
(2, 2, 'Semester 2'),
(3, 3, 'Semester 3'),
(4, 4, 'Semester 4'),
(5, 5, 'Semester 5'),
(6, 6, 'Semester 6'),
(7, 7, 'Semester 7'),
(8, 8, 'Semester 8');

INSERT IGNORE INTO sections (id, branch_id, name) VALUES
(1, 1, 'Section A'),
(2, 1, 'Section B'),
(3, 2, 'Section A'),
(4, 2, 'Section B'),
(5, 3, 'Section A'),
(6, 4, 'Section A'),
(7, 5, 'Section A');

INSERT IGNORE INTO subjects (id, code, name) VALUES
(1, 'CS101', 'Data Structures & Algorithms'),
(2, 'CS102', 'Database Management Systems'),
(3, 'EC101', 'Digital Signal Processing'),
(4, 'ME101', 'Thermodynamics');

