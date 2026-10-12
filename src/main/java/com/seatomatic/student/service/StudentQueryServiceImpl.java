package com.seatomatic.student.service;

import com.google.gson.Gson;
import com.seatomatic.student.contract.StudentQueryService;
import com.seatomatic.student.dao.StudentDao;
import com.seatomatic.student.dao.StudentEligibilityDao;
import com.seatomatic.student.model.Student;
import com.seatomatic.student.model.dto.StudentDetailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of inter-module StudentQueryService contract.
 */
public class StudentQueryServiceImpl implements StudentQueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentQueryServiceImpl.class);
    private static final Gson GSON = new Gson();

    private final StudentEligibilityDao eligibilityDao;
    private final StudentDao studentDao;

    public StudentQueryServiceImpl() {
        this(new StudentEligibilityDao(), new StudentDao());
    }

    public StudentQueryServiceImpl(StudentEligibilityDao eligibilityDao, StudentDao studentDao) {
        this.eligibilityDao = eligibilityDao;
        this.studentDao = studentDao;
    }

    @Override
    public String findEligibleStudents(Long examId) {
        try {
            List<StudentDetailDTO> list = eligibilityDao.findEligibleStudentsForExam(examId);
            return GSON.toJson(list);
        } catch (SQLException e) {
            LOGGER.error("Failed to query eligible students for examId: {}", examId, e);
            return "[]";
        }
    }

    @Override
    public int countByGroup(String branchCode) {
        try {
            return eligibilityDao.countStudentsByBranch(branchCode);
        } catch (SQLException e) {
            LOGGER.error("Failed to count students by branchCode: {}", branchCode, e);
            return 0;
        }
    }

    @Override
    public Long getById(Long studentId) {
        try {
            Optional<Student> studentOpt = studentDao.findById(studentId);
            return studentOpt.map(Student::getId).orElse(null);
        } catch (SQLException e) {
            LOGGER.error("Failed to query student by studentId: {}", studentId, e);
            return null;
        }
    }
}

