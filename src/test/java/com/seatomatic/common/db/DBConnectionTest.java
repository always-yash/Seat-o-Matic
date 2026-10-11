package com.seatomatic.common.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DBConnectionTest {

    @AfterEach
    void tearDown() {
        DBConnection.resetDataSourceForTesting();
    }

    @Test
    void testOpenWithInjectedDataSource() throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);

        DBConnection.setDataSourceForTesting(mockDataSource);

        try (DBConnection db = DBConnection.open()) {
            assertNotNull(db);
            assertSame(mockConnection, db.connection());
            assertSame(mockConnection, db.getConnection());
        }

        verify(mockConnection).close();
    }

    @Test
    void testGetDataSourceReturnsInjectedDataSource() throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        DBConnection.setDataSourceForTesting(mockDataSource);

        DataSource ds = DBConnection.getDataSource();
        assertSame(mockDataSource, ds);
    }

    @Test
    void testGetDataSourceFailsWhenJndiUnavailable() {
        DBConnection.resetDataSourceForTesting();
        SQLException ex = assertThrows(SQLException.class, DBConnection::getDataSource);
        assertTrue(ex.getMessage().contains("Unable to resolve JNDI DataSource"));
    }
}
