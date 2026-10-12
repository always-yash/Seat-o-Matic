-- =============================================================================
-- Migration: Module 4 Paper Distribution & Invigilation Schema Alignment
-- Target: seatomatic database (MySQL 8.0+)
-- Author: Sanjay Jangid (Module 4 Lead)
-- Description:
--   Adds workflow state tracking (distributed, collected, status) to
--   paper_distribution, adds auditing timestamps, and optimizes indexing for
--   invigilator assignment lookups to support IDOR-safe queries.
-- =============================================================================

USE seatomatic;

-- 1. Align paper_distribution with state machine columns and timestamps
ALTER TABLE paper_distribution
    ADD COLUMN IF NOT EXISTS distributed INT NOT NULL DEFAULT 0 AFTER buffer,
    ADD COLUMN IF NOT EXISTS collected INT NOT NULL DEFAULT 0 AFTER distributed,
    ADD COLUMN IF NOT EXISTS status ENUM('PENDING','PREPARED','DISTRIBUTED','VERIFIED') NOT NULL DEFAULT 'PENDING' AFTER collected,
    ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0 AFTER status,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Create indexes for room and status filtering on paper_distribution
SET @exist_idx_status := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'paper_distribution' AND index_name = 'idx_paper_distribution_status'
);
SET @sql_idx_status := IF(@exist_idx_status = 0, 'CREATE INDEX idx_paper_distribution_status ON paper_distribution(status)', 'SELECT 1');
PREPARE stmt_idx_status FROM @sql_idx_status;
EXECUTE stmt_idx_status;
DEALLOCATE PREPARE stmt_idx_status;

SET @exist_idx_room := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'paper_distribution' AND index_name = 'idx_paper_distribution_room'
);
SET @sql_idx_room := IF(@exist_idx_room = 0, 'CREATE INDEX idx_paper_distribution_room ON paper_distribution(room_id)', 'SELECT 1');
PREPARE stmt_idx_room FROM @sql_idx_room;
EXECUTE stmt_idx_room;
DEALLOCATE PREPARE stmt_idx_room;

-- 2. Align invigilator_assignments with update timestamp and lookup indexes
ALTER TABLE invigilator_assignments
    ADD COLUMN IF NOT EXISTS check_in_time TIMESTAMP NULL AFTER status,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

SET @exist_idx_invig_user := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'invigilator_assignments' AND index_name = 'idx_invigilator_user'
);
SET @sql_idx_invig_user := IF(@exist_idx_invig_user = 0, 'CREATE INDEX idx_invigilator_user ON invigilator_assignments(invigilator_id)', 'SELECT 1');
PREPARE stmt_idx_invig_user FROM @sql_idx_invig_user;
EXECUTE stmt_idx_invig_user;
DEALLOCATE PREPARE stmt_idx_invig_user;

SET @exist_idx_invig_exam_room := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'invigilator_assignments' AND index_name = 'idx_invigilator_exam_room'
);
SET @sql_idx_invig_exam_room := IF(@exist_idx_invig_exam_room = 0, 'CREATE INDEX idx_invigilator_exam_room ON invigilator_assignments(exam_id, room_id)', 'SELECT 1');
PREPARE stmt_idx_invig_exam_room FROM @sql_idx_invig_exam_room;
EXECUTE stmt_idx_invig_exam_room;
DEALLOCATE PREPARE stmt_idx_invig_exam_room;

-- 3. Optimize question_papers lookup
SET @exist_idx_qp := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'question_papers' AND index_name = 'idx_qp_exam_subject'
);
SET @sql_idx_qp := IF(@exist_idx_qp = 0, 'CREATE INDEX idx_qp_exam_subject ON question_papers(exam_id, subject_id)', 'SELECT 1');
PREPARE stmt_idx_qp FROM @sql_idx_qp;
EXECUTE stmt_idx_qp;
DEALLOCATE PREPARE stmt_idx_qp;

-- 4. Create paper_discrepancies table for room incidents and script reconciliation issues
CREATE TABLE IF NOT EXISTS paper_discrepancies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    distribution_id BIGINT NULL,
    exam_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    incident_type VARCHAR(60) NOT NULL,
    count_affected INT NOT NULL DEFAULT 0,
    description TEXT,
    reported_by BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REPORTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    INDEX idx_discrepancy_exam (exam_id),
    INDEX idx_discrepancy_room (room_id)
);
