package com.seatomatic.report.audit;

import com.seatomatic.common.audit.AuditPublisher;
import com.seatomatic.common.db.BaseDAO;

import java.sql.SQLException;

public class AuditPublisherImpl extends BaseDAO implements AuditPublisher {
    @Override
    public void publish(String action, String actor, String targetType, Long targetId, String details) {
        String sql = "INSERT INTO audit_logs (actor_user_id, action, target_type, target_id, details) "
                + "VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?)";
        try {
            executeUpdate(sql, statement -> {
                statement.setString(1, actor);
                statement.setString(2, action);
                statement.setString(3, targetType);
                if (targetId == null) {
                    statement.setNull(4, java.sql.Types.BIGINT);
                } else {
                    statement.setLong(4, targetId);
                }
                statement.setString(5, details == null ? "{}" : details);
            });
        } catch (SQLException ex) {
            logger.error("Unable to publish audit event {} for {}", action, actor, ex);
        }
    }
}
