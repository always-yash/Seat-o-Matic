CREATE TABLE invigilator_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignment_invig_exam FOREIGN KEY (exam_id) REFERENCES exams(id),
    CONSTRAINT fk_assignment_invig_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_assignment_invig_user FOREIGN KEY (user_id) REFERENCES user_accounts(id),
    CONSTRAINT uq_invig_exam_room UNIQUE (exam_id, room_id),
    CONSTRAINT uq_invig_exam_user UNIQUE (exam_id, user_id)
);

CREATE INDEX idx_invig_user ON invigilator_assignments(user_id);
CREATE INDEX idx_invig_exam ON invigilator_assignments(exam_id);
