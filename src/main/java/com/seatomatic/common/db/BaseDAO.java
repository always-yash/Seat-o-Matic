package com.seatomatic.common.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Base Data Access Object providing robust, injection-safe database execution helpers.
 * All queries and updates utilize parameterized {@link PreparedStatement} instances
 * wrapped with auto-closing try-with-resources blocks and SLF4J logging.
 */
public abstract class BaseDAO {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @FunctionalInterface
    public interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE statement using a custom statement binder.
     */
    protected int executeUpdate(String sql, StatementBinder binder) throws SQLException {
        validateSql(sql);
        try (DBConnection database = DBConnection.open();
             PreparedStatement statement = prepare(database, sql, binder)) {
            return statement.executeUpdate();
        }
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE statement using positional parameters.
     */
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        return executeUpdate(sql, binderFromParams(params));
    }

    /**
     * Executes an INSERT statement and retrieves the generated primary key (if applicable).
     *
     * @return Generated primary key ID, or null if no key was generated.
     */
    protected Long executeInsert(String sql, StatementBinder binder) throws SQLException {
        validateSql(sql);
        try (DBConnection database = DBConnection.open();
             PreparedStatement statement = database.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (binder != null) {
                binder.bind(statement);
            }
            int affected = statement.executeUpdate();
            if (affected == 0) {
                return null;
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys != null && generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            return null;
        }
    }

    /**
     * Executes an INSERT statement with positional parameters and retrieves the generated key.
     */
    protected Long executeInsert(String sql, Object... params) throws SQLException {
        return executeInsert(sql, binderFromParams(params));
    }

    /**
     * Executes a batch update with multiple parameter sets.
     */
    protected int[] executeBatch(String sql, List<StatementBinder> binders) throws SQLException {
        validateSql(sql);
        if (binders == null || binders.isEmpty()) {
            return new int[0];
        }
        try (DBConnection database = DBConnection.open();
             PreparedStatement statement = database.connection().prepareStatement(sql)) {
            for (StatementBinder binder : binders) {
                if (binder != null) {
                    binder.bind(statement);
                    statement.addBatch();
                }
            }
            return statement.executeBatch();
        }
    }

    /**
     * Executes a batch update with an iterable of parameter arrays.
     */
    protected int[] executeBatchParams(String sql, Iterable<Object[]> batchParams) throws SQLException {
        validateSql(sql);
        if (batchParams == null) {
            return new int[0];
        }
        try (DBConnection database = DBConnection.open();
             PreparedStatement statement = database.connection().prepareStatement(sql)) {
            for (Object[] params : batchParams) {
                bindParameters(statement, params);
                statement.addBatch();
            }
            return statement.executeBatch();
        }
    }

    /**
     * Executes a query expected to return at most one result, using a statement binder.
     */
    protected <T> T queryOne(String sql, StatementBinder binder, ResultMapper<T> mapper) throws SQLException {
        validateSql(sql);
        if (mapper == null) {
            throw new IllegalArgumentException("ResultMapper must not be null");
        }
        try (DBConnection database = DBConnection.open();
             PreparedStatement statement = prepare(database, sql, binder);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? mapper.map(resultSet) : null;
        }
    }

    /**
     * Executes a query expected to return at most one result, using positional parameters.
     */
    protected <T> T queryOne(String sql, ResultMapper<T> mapper, Object... params) throws SQLException {
        return queryOne(sql, binderFromParams(params), mapper);
    }

    /**
     * Executes a query returning an Optional wrapped single result.
     */
    protected <T> Optional<T> queryOptional(String sql, StatementBinder binder, ResultMapper<T> mapper) throws SQLException {
        return Optional.ofNullable(queryOne(sql, binder, mapper));
    }

    /**
     * Executes a query returning an Optional wrapped single result, using positional parameters.
     */
    protected <T> Optional<T> queryOptional(String sql, ResultMapper<T> mapper, Object... params) throws SQLException {
        return Optional.ofNullable(queryOne(sql, mapper, params));
    }

    /**
     * Executes a query returning a list of mapped records.
     */
    protected <T> List<T> queryList(String sql, StatementBinder binder, ResultMapper<T> mapper) throws SQLException {
        validateSql(sql);
        if (mapper == null) {
            throw new IllegalArgumentException("ResultMapper must not be null");
        }
        List<T> results = new ArrayList<>();
        try (DBConnection database = DBConnection.open();
             PreparedStatement statement = prepare(database, sql, binder);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(mapper.map(resultSet));
            }
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Executes a query returning a list of mapped records, using positional parameters.
     */
    protected <T> List<T> queryList(String sql, ResultMapper<T> mapper, Object... params) throws SQLException {
        return queryList(sql, binderFromParams(params), mapper);
    }

    /**
     * Executes a count or single numeric scalar query.
     */
    protected long queryCount(String sql, StatementBinder binder) throws SQLException {
        Long result = queryOne(sql, binder, rs -> rs.getLong(1));
        return result != null ? result : 0L;
    }

    /**
     * Executes a count query using positional parameters.
     */
    protected long queryCount(String sql, Object... params) throws SQLException {
        return queryCount(sql, binderFromParams(params));
    }

    /**
     * Helper to prepare a statement and bind parameters with proper error cleanup.
     */
    private PreparedStatement prepare(DBConnection database, String sql, StatementBinder binder) throws SQLException {
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

    /**
     * Binds an array of positional parameters to a PreparedStatement.
     */
    protected static void bindParameters(PreparedStatement statement, Object... params) throws SQLException {
        if (params == null || params.length == 0) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            int parameterIndex = i + 1;
            Object param = params[i];
            if (param == null) {
                statement.setNull(parameterIndex, java.sql.Types.NULL);
            } else if (param instanceof Enum<?> enumVal) {
                statement.setString(parameterIndex, enumVal.name());
            } else if (param instanceof LocalDate localDate) {
                statement.setDate(parameterIndex, java.sql.Date.valueOf(localDate));
            } else if (param instanceof LocalDateTime localDateTime) {
                statement.setTimestamp(parameterIndex, java.sql.Timestamp.valueOf(localDateTime));
            } else if (param instanceof Instant instant) {
                statement.setTimestamp(parameterIndex, java.sql.Timestamp.from(instant));
            } else if (param instanceof Date date) {
                statement.setTimestamp(parameterIndex, new java.sql.Timestamp(date.getTime()));
            } else {
                statement.setObject(parameterIndex, param);
            }
        }
    }

    private StatementBinder binderFromParams(Object... params) {
        if (params == null || params.length == 0) {
            return null;
        }
        return statement -> bindParameters(statement, params);
    }

    private void validateSql(String sql) throws SQLException {
        if (sql == null || sql.isBlank()) {
            throw new SQLException("SQL statement must not be blank");
        }
    }
}
