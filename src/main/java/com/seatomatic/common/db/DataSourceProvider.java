package com.seatomatic.common.db;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public final class DataSourceProvider {

    private static final String JNDI_NAME = "java:comp/env/jdbc/seatomatic";

    private DataSourceProvider() {
    }

    public static DataSource getDataSource() throws NamingException {
        try {
            InitialContext context = new InitialContext();
            Object lookup = context.lookup(JNDI_NAME);
            if (lookup instanceof DataSource dataSource) {
                return dataSource;
            }
        } catch (NamingException ex) {
            throw ex;
        }
        throw new NamingException("JNDI resource not found: " + JNDI_NAME);
    }
}
