ALTER TABLE exams
    ADD COLUMN registration_closed BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE exams e
SET registration_closed = TRUE
WHERE EXISTS (
    SELECT 1 FROM seating_plans p WHERE p.exam_id = e.id
);