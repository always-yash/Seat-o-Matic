package com.seatomatic.common.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @FunctionalInterface
    protected interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    protected interface ResultMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }

    protected int executeUpdate(String sql, StatementBinder binder) throws SQLException {
        try (DBConnection database = DBConnection.open();
             PreparedStatement statement = prepare(database, sql, binder)) {
            return statement.executeUpdate();
        }
    }

    protected <T> T queryOne(String sql, StatementBinder binder, ResultMapper<T> mapper) throws SQLException {
        try (DBConnection database = DBConnection.open();
             PreparedStatement statement = prepare(database, sql, binder);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? mapper.map(resultSet) : null;
        }
    }

    protected <T> List<T> queryList(String sql, StatementBinder binder, ResultMapper<T> mapper) throws SQLException {
        List<T> results = new ArrayList<>();
        try (DBConnection database = DBConnection.open();
             PreparedStatement statement = prepare(database, sql, binder);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(mapper.map(resultSet));
            }
        }
        return results;
    }

    private PreparedStatement prepare(DBConnection database, String sql, StatementBinder binder) throws SQLException {
        if (sql == null || sql.isBlank()) {
            throw new SQLException("SQL statement must not be blank");
        }
        PreparedStatement statement = database.connection().prepareStatement(sql);
        try {
            if (binder != null) {
                binder.bind(statement);
            }
            return statement;
        } catch (SQLException ex) {
            statement.close();
            throw ex;
        }
    }
}
