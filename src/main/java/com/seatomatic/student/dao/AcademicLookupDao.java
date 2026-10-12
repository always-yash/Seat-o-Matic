package com.seatomatic.student.dao;

import com.seatomatic.common.db.BaseDAO;
import com.seatomatic.student.model.Branch;
import com.seatomatic.student.model.Section;
import com.seatomatic.student.model.Semester;
import com.seatomatic.student.model.Subject;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object managing lookup lookups for academic metadata:
 * branches, sections, semesters, and subjects.
 */
public class AcademicLookupDao extends BaseDAO {

    // ==========================================
    // Branch operations
    // ==========================================

    public List<Branch> findAllBranches() throws SQLException {
        String sql = "SELECT id, code, name FROM branches ORDER BY code ASC";
        return queryList(sql, rs -> {
            Branch b = new Branch();
            b.setId(rs.getLong("id"));
            b.setCode(rs.getString("code"));
            b.setName(rs.getString("name"));
            return b;
        });
    }

    public Optional<Branch> findBranchById(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, code, name FROM branches WHERE id = ?";
        return queryOptional(sql, rs -> {
            Branch b = new Branch();
            b.setId(rs.getLong("id"));
            b.setCode(rs.getString("code"));
            b.setName(rs.getString("name"));
            return b;
        }, id);
    }

    public Optional<Branch> findBranchByCode(String code) throws SQLException {
        if (code == null || code.isBlank()) return Optional.empty();
        String sql = "SELECT id, code, name FROM branches WHERE UPPER(code) = UPPER(?)";
        return queryOptional(sql, rs -> {
            Branch b = new Branch();
            b.setId(rs.getLong("id"));
            b.setCode(rs.getString("code"));
            b.setName(rs.getString("name"));
            return b;
        }, code.trim());
    }

    public Branch findOrCreateBranch(String code, String name) throws SQLException {
        Optional<Branch> existing = findBranchByCode(code);
        if (existing.isPresent()) {
            return existing.get();
        }
        String sql = "INSERT INTO branches (code, name) VALUES (?, ?)";
        Long generatedId = executeInsert(sql, code.trim().toUpperCase(), (name != null && !name.isBlank()) ? name.trim() : code.trim().toUpperCase());
        Branch b = new Branch();
        b.setId(generatedId);
        b.setCode(code.trim().toUpperCase());
        b.setName((name != null && !name.isBlank()) ? name.trim() : code.trim().toUpperCase());
        return b;
    }

    // ==========================================
    // Section operations
    // ==========================================

    public List<Section> findSectionsByBranchId(Long branchId) throws SQLException {
        if (branchId == null) return List.of();
        String sql = "SELECT id, branch_id, name FROM sections WHERE branch_id = ? ORDER BY name ASC";
        return queryList(sql, rs -> {
            Section s = new Section();
            s.setId(rs.getLong("id"));
            s.setBranchId(rs.getLong("branch_id"));
            s.setName(rs.getString("name"));
            return s;
        }, branchId);
    }

    public List<Section> findAllSections() throws SQLException {
        String sql = "SELECT id, branch_id, name FROM sections ORDER BY branch_id ASC, name ASC";
        return queryList(sql, rs -> {
            Section s = new Section();
            s.setId(rs.getLong("id"));
            s.setBranchId(rs.getLong("branch_id"));
            s.setName(rs.getString("name"));
            return s;
        });
    }

    public Optional<Section> findSectionById(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, branch_id, name FROM sections WHERE id = ?";
        return queryOptional(sql, rs -> {
            Section s = new Section();
            s.setId(rs.getLong("id"));
            s.setBranchId(rs.getLong("branch_id"));
            s.setName(rs.getString("name"));
            return s;
        }, id);
    }

    public Optional<Section> findSectionByBranchAndName(Long branchId, String name) throws SQLException {
        if (branchId == null || name == null || name.isBlank()) return Optional.empty();
        String sql = "SELECT id, branch_id, name FROM sections WHERE branch_id = ? AND UPPER(name) = UPPER(?)";
        return queryOptional(sql, rs -> {
            Section s = new Section();
            s.setId(rs.getLong("id"));
            s.setBranchId(rs.getLong("branch_id"));
            s.setName(rs.getString("name"));
            return s;
        }, branchId, name.trim());
    }

    public Section findOrCreateSection(Long branchId, String name) throws SQLException {
        Optional<Section> existing = findSectionByBranchAndName(branchId, name);
        if (existing.isPresent()) {
            return existing.get();
        }
        String formattedName = name.trim().startsWith("Section ") ? name.trim() : "Section " + name.trim();
        String sql = "INSERT INTO sections (branch_id, name) VALUES (?, ?)";
        Long generatedId = executeInsert(sql, branchId, formattedName);
        Section s = new Section();
        s.setId(generatedId);
        s.setBranchId(branchId);
        s.setName(formattedName);
        return s;
    }

    // ==========================================
    // Semester operations
    // ==========================================

    public List<Semester> findAllSemesters() throws SQLException {
        String sql = "SELECT id, number, name FROM semesters ORDER BY number ASC";
        return queryList(sql, rs -> new Semester(
                rs.getLong("id"),
                rs.getInt("number"),
                rs.getString("name")
        ));
    }

    public Optional<Semester> findSemesterById(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, number, name FROM semesters WHERE id = ?";
        return queryOptional(sql, rs -> new Semester(
                rs.getLong("id"),
                rs.getInt("number"),
                rs.getString("name")
        ), id);
    }

    public Optional<Semester> findSemesterByNumber(int number) throws SQLException {
        String sql = "SELECT id, number, name FROM semesters WHERE number = ?";
        return queryOptional(sql, rs -> new Semester(
                rs.getLong("id"),
                rs.getInt("number"),
                rs.getString("name")
        ), number);
    }

    public Semester findOrCreateSemester(int number, String name) throws SQLException {
        Optional<Semester> existing = findSemesterByNumber(number);
        if (existing.isPresent()) {
            return existing.get();
        }
        String semName = (name != null && !name.isBlank()) ? name.trim() : "Semester " + number;
        String sql = "INSERT INTO semesters (number, name) VALUES (?, ?)";
        Long generatedId = executeInsert(sql, number, semName);
        return new Semester(generatedId, number, semName);
    }

    // ==========================================
    // Subject operations
    // ==========================================

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

    public Optional<Subject> findSubjectById(Long id) throws SQLException {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, code, name FROM subjects WHERE id = ?";
        return queryOptional(sql, rs -> {
            Subject s = new Subject();
            s.setId(rs.getLong("id"));
            s.setCode(rs.getString("code"));
            s.setName(rs.getString("name"));
            return s;
        }, id);
    }

    public Optional<Subject> findSubjectByCode(String code) throws SQLException {
        if (code == null || code.isBlank()) return Optional.empty();
        String sql = "SELECT id, code, name FROM subjects WHERE UPPER(code) = UPPER(?)";
        return queryOptional(sql, rs -> {
            Subject s = new Subject();
            s.setId(rs.getLong("id"));
            s.setCode(rs.getString("code"));
            s.setName(rs.getString("name"));
            return s;
        }, code.trim());
    }
}

