package com.seatomatic.common.db;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;

public final class DataSourceProvider {

    private DataSourceProvider() {
    }

    public static DataSource getDataSource() throws NamingException {
        try {
            return DBConnection.getDataSource();
        } catch (SQLException ex) {
            NamingException ne = new NamingException("Failed to locate database DataSource via JNDI: " + ex.getMessage());
            ne.initCause(ex);
            throw ne;
        }
    }
}
