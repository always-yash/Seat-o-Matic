package com.seatomatic.common.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BaseDAOTest {

    private DataSource mockDataSource;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    // Concrete test DAO
    private static class TestDAO extends BaseDAO {
    }

    private TestDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockStatement);

        DBConnection.setDataSourceForTesting(mockDataSource);
        dao = new TestDAO();
    }

    @AfterEach
    void tearDown() {
        DBConnection.resetDataSourceForTesting();
    }

    @Test
    void testQueryOneWithResults() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("name")).thenReturn("Test Exam");

        String result = dao.queryOne("SELECT name FROM exams WHERE id = ?",
                statement -> statement.setLong(1, 42L),
                rs -> rs.getString("name"));

        assertEquals("Test Exam", result);
        verify(mockStatement).setLong(1, 42L);
        verify(mockResultSet).close();
        verify(mockStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testQueryOneEmptyReturnsNull() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        String result = dao.queryOne("SELECT name FROM exams WHERE id = ?",
                statement -> statement.setLong(1, 999L),
                rs -> rs.getString("name"));

        assertNull(result);
    }

    @Test
    void testQueryOneWithPositionalParams() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(100L);

        Long val = dao.queryOne("SELECT count(*) FROM users WHERE username = ?",
                rs -> rs.getLong(1),
                "admin");

        assertEquals(100L, val);
        verify(mockStatement).setObject(1, "admin");
    }

    @Test
    void testQueryListMultipleRows() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("code")).thenReturn("CS101", "CS102");

        List<String> list = dao.queryList("SELECT code FROM subjects",
                null,
                rs -> rs.getString("code"));

        assertEquals(List.of("CS101", "CS102"), list);
        verify(mockStatement, never()).setString(anyInt(), anyString());
    }

    @Test
    void testQueryOptional() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(1)).thenReturn("Found");

        Optional<String> opt = dao.queryOptional("SELECT code FROM subjects WHERE id = ?",
                rs -> rs.getString(1), 1L);

        assertTrue(opt.isPresent());
        assertEquals("Found", opt.get());
    }

    @Test
    void testQueryCount() throws SQLException {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(45L);

        long count = dao.queryCount("SELECT COUNT(*) FROM students", (Object[]) null);
        assertEquals(45L, count);
    }

    @Test
    void testExecuteUpdate() throws SQLException {
        when(mockStatement.executeUpdate()).thenReturn(3);

        int updated = dao.executeUpdate("UPDATE users SET active = ? WHERE id = ?", false, 10L);

        assertEquals(3, updated);
        verify(mockStatement).setObject(1, false);
        verify(mockStatement).setObject(2, 10L);
    }

    @Test
    void testExecuteInsertReturnsGeneratedKey() throws SQLException {
        ResultSet mockKeys = mock(ResultSet.class);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockKeys);
        when(mockKeys.next()).thenReturn(true);
        when(mockKeys.getLong(1)).thenReturn(777L);

        Long newId = dao.executeInsert("INSERT INTO users (username) VALUES (?)", "new_user");

        assertEquals(777L, newId);
        verify(mockStatement).setObject(1, "new_user");
    }

    @Test
    void testExecuteBatchWithBinders() throws SQLException {
        int[] expected = new int[]{1, 1};
        when(mockStatement.executeBatch()).thenReturn(expected);

        int[] result = dao.executeBatch("INSERT INTO logs (msg) VALUES (?)", List.of(
                stmt -> stmt.setString(1, "m1"),
                stmt -> stmt.setString(1, "m2")
        ));

        assertArrayEquals(expected, result);
        verify(mockStatement, times(2)).addBatch();
        verify(mockStatement).executeBatch();
    }

    @Test
    void testExecuteBatchWithParamArrays() throws SQLException {
        int[] expected = new int[]{1, 1};
        when(mockStatement.executeBatch()).thenReturn(expected);

        int[] result = dao.executeBatchParams("INSERT INTO tags (name) VALUES (?)", List.of(
                new Object[]{"tag1"},
                new Object[]{"tag2"}
        ));

        assertArrayEquals(expected, result);
        verify(mockStatement, times(2)).addBatch();
    }

    @Test
    void testBlankSqlThrowsException() {
        assertThrows(SQLException.class, () -> dao.executeUpdate("   "));
        assertThrows(SQLException.class, () -> dao.queryOne("", null, rs -> ""));
    }
}
