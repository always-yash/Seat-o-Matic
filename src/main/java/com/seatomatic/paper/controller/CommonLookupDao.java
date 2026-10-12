package com.seatomatic.paper.controller;

import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.exam.model.Exam;
import com.seatomatic.exam.model.Room;
import com.seatomatic.student.model.Subject;

import java.sql.SQLException;
import java.util.List;

/**
 * Shared lookup helper for populating Exam, Room, and Subject options in UI controllers.
 */
public class CommonLookupDao extends BaseDAO {

    public List<Exam> findAllExams() throws SQLException {
        String sql = "SELECT id, title, exam_date, start_time, end_time, status FROM exams ORDER BY exam_date DESC, id DESC";
        return queryList(sql, rs -> {
            Exam exam = new Exam();
            exam.setId(rs.getLong("id"));
            exam.setTitle(rs.getString("title"));
            exam.setExamDate(rs.getDate("exam_date").toLocalDate());
            exam.setStartTime(rs.getTime("start_time").toLocalTime());
            exam.setEndTime(rs.getTime("end_time").toLocalTime());
            exam.setStatus(rs.getString("status"));
            return exam;
        });
    }

    public List<Subject> findAllSubjects() throws SQLException {
        String sql = "SELECT id, code, name FROM subjects ORDER BY code ASC";
        return queryList(sql, rs -> {
            Subject s = new Subject();
            s.setId(rs.getLong("id"));
            s.setCode(rs.getString("code"));
            s.setName(rs.getString("name"));
            return s;
        });
    }

    public List<Room> findAllRooms() throws SQLException {
        String sql = "SELECT id, room_code, total_rows, total_columns, active FROM rooms WHERE active = TRUE ORDER BY room_code ASC";
        return queryList(sql, rs -> {
            Room r = new Room();
            r.setId(rs.getLong("id"));
            r.setRoomCode(rs.getString("room_code"));
            r.setTotalRows(rs.getInt("total_rows"));
            r.setTotalColumns(rs.getInt("total_columns"));
            r.setActive(rs.getBoolean("active"));
            return r;
        });
    }
}
