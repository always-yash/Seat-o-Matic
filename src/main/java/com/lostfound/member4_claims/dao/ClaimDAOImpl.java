package com.lostfound.member4_claims.dao;

import com.lostfound.config.DatabaseConnection;
import com.lostfound.model.Claim;
import com.lostfound.model.HandoverRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Implementation of ClaimDAO with ACID transaction support.
 */
public class ClaimDAOImpl implements ClaimDAO {

    private Claim mapResultSetToClaim(ResultSet rs) throws SQLException {
        Claim claim = new Claim();
        claim.setClaimId(rs.getLong("claim_id"));
        claim.setItemId(rs.getLong("item_id"));
        claim.setClaimantId(rs.getLong("claimant_id"));
        claim.setReason(rs.getString("reason"));
        claim.setProofDescription(rs.getString("proof_description"));
        claim.setStatus(rs.getString("status"));
        claim.setOtp(rs.getString("otp"));
        claim.setOtpExpiry(rs.getTimestamp("otp_expiry"));
        claim.setQrToken(rs.getString("qr_token"));
        claim.setCreatedAt(rs.getTimestamp("created_at"));

        try { claim.setItemTitle(rs.getString("item_title")); } catch (SQLException ignored) {}
        try { claim.setItemType(rs.getString("item_type")); } catch (SQLException ignored) {}
        try { claim.setItemLocation(rs.getString("item_location")); } catch (SQLException ignored) {}
        try { claim.setItemImage(rs.getString("item_image")); } catch (SQLException ignored) {}
        try { claim.setClaimantName(rs.getString("claimant_name")); } catch (SQLException ignored) {}
        try { claim.setClaimantEmail(rs.getString("claimant_email")); } catch (SQLException ignored) {}
        try { claim.setClaimantPhone(rs.getString("claimant_phone")); } catch (SQLException ignored) {}
        try { claim.setOwnerId(rs.getLong("owner_id")); } catch (SQLException ignored) {}
        try { claim.setOwnerName(rs.getString("owner_name")); } catch (SQLException ignored) {}
        try { claim.setOwnerEmail(rs.getString("owner_email")); } catch (SQLException ignored) {}

        return claim;
    }

    private static final String BASE_SELECT =
            "SELECT c.claim_id, c.item_id, c.claimant_id, c.reason, c.proof_description, " +
            "c.status, c.otp, c.otp_expiry, c.qr_token, c.created_at, " +
            "i.title AS item_title, i.item_type, i.location AS item_location, i.image AS item_image, " +
            "u.name AS claimant_name, u.email AS claimant_email, u.phone AS claimant_phone, " +
            "owner.user_id AS owner_id, owner.name AS owner_name, owner.email AS owner_email " +
            "FROM claims c " +
            "JOIN items i ON c.item_id = i.item_id " +
            "JOIN users u ON c.claimant_id = u.user_id " +
            "JOIN users owner ON i.user_id = owner.user_id ";

    @Override
    public Claim create(Claim claim) throws SQLException {
        String sql = "INSERT INTO claims (item_id, claimant_id, reason, proof_description, status) " +
                     "VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, claim.getItemId());
            ps.setLong(2, claim.getClaimantId());
            ps.setString(3, claim.getReason());
            ps.setString(4, claim.getProofDescription());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    claim.setClaimId(rs.getLong(1));
                    claim.setStatus("PENDING");
                }
            }
            return claim;
        }
    }

    @Override
    public Claim findById(Long claimId) throws SQLException {
        if (claimId == null) return null;
        String sql = BASE_SELECT + "WHERE c.claim_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClaim(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Claim> findByUser(Long claimantId) throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.claimant_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, claimantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToClaim(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Claim> findByItem(Long itemId) throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.item_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToClaim(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Claim> findPendingClaims() throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.status = 'PENDING' ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToClaim(rs));
            }
        }
        return list;
    }

    @Override
    public List<Claim> findClaimsForUserItems(Long ownerId) throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE i.user_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToClaim(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean hasActiveClaim(Long itemId, Long claimantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM claims WHERE item_id = ? AND claimant_id = ? " +
                     "AND status IN ('PENDING', 'APPROVED', 'VERIFIED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itemId);
            ps.setLong(2, claimantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean approve(Long claimId, String otp, Timestamp otpExpiry, String qrToken) throws SQLException {
        String sql = "UPDATE claims SET status = 'APPROVED', otp = ?, otp_expiry = ?, qr_token = ? WHERE claim_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, otp);
            ps.setTimestamp(2, otpExpiry);
            ps.setString(3, qrToken);
            ps.setLong(4, claimId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean reject(Long claimId) throws SQLException {
        String sql = "UPDATE claims SET status = 'REJECTED' WHERE claim_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, claimId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(Long claimId, String status) throws SQLException {
        String sql = "UPDATE claims SET status = ? WHERE claim_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, claimId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean executeHandoverTransaction(Long claimId, Long itemId, Long ownerId, Long receiverId,
                                             String verificationMethod, String remarks) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // 1. Insert Handover Record
            String insertHandoverSql = "INSERT INTO handover_records (claim_id, item_id, owner_id, receiver_id, " +
                                       "verification_method, remarks) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertHandoverSql)) {
                ps.setLong(1, claimId);
                ps.setLong(2, itemId);
                ps.setLong(3, ownerId);
                ps.setLong(4, receiverId);
                ps.setString(5, verificationMethod);
                ps.setString(6, remarks);
                ps.executeUpdate();
            }

            // 2. Update Claim status -> COMPLETED
            String updateClaimSql = "UPDATE claims SET status = 'COMPLETED' WHERE claim_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateClaimSql)) {
                ps.setLong(1, claimId);
                ps.executeUpdate();
            }

            // 3. Update Item status -> RETURNED
            String updateItemSql = "UPDATE items SET status = 'RETURNED' WHERE item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateItemSql)) {
                ps.setLong(1, itemId);
                ps.executeUpdate();
            }

            // 4. Create Notification for receiver / claimant
            String notifSql = "INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(notifSql)) {
                ps.setLong(1, receiverId);
                ps.setString(2, "Handover Completed!");
                ps.setString(3, "Item #" + itemId + " has been successfully handed over to you.");
                ps.executeUpdate();

                // Notification for owner
                ps.setLong(1, ownerId);
                ps.setString(2, "Item Handover Finalized");
                ps.setString(3, "Handover for item #" + itemId + " has been finalized via " + verificationMethod + ".");
                ps.executeUpdate();
            }

            // 5. Audit log entry
            String auditSql = "INSERT INTO audit_logs (user_id, action, ip_address) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(auditSql)) {
                ps.setLong(1, ownerId);
                ps.setString(2, "HANDOVER_COMPLETED: Item #" + itemId + " via " + verificationMethod);
                ps.setString(3, "127.0.0.1");
                ps.executeUpdate();
            }

            conn.commit(); // COMMIT TRANSACTION
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // ROLLBACK ON ERROR
                } catch (SQLException rbEx) {
                    rbEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public List<HandoverRecord> getAllHandoverRecords() throws SQLException {
        List<HandoverRecord> list = new ArrayList<>();
        String sql = "SELECT hr.*, i.title AS item_title, u1.name AS owner_name, u2.name AS receiver_name " +
                     "FROM handover_records hr " +
                     "JOIN items i ON hr.item_id = i.item_id " +
                     "LEFT JOIN users u1 ON hr.owner_id = u1.user_id " +
                     "LEFT JOIN users u2 ON hr.receiver_id = u2.user_id " +
                     "ORDER BY hr.handover_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                HandoverRecord record = new HandoverRecord();
                record.setHandoverId(rs.getLong("handover_id"));
                record.setClaimId(rs.getLong("claim_id"));
                record.setItemId(rs.getLong("item_id"));
                record.setOwnerId(rs.getLong("owner_id"));
                record.setReceiverId(rs.getLong("receiver_id"));
                record.setHandoverDate(rs.getTimestamp("handover_date"));
                record.setVerificationMethod(rs.getString("verification_method"));
                record.setRemarks(rs.getString("remarks"));
                record.setItemTitle(rs.getString("item_title"));
                record.setOwnerName(rs.getString("owner_name"));
                record.setReceiverName(rs.getString("receiver_name"));
                list.add(record);
            }
        }
        return list;
    }

    @Override
    public HandoverRecord getHandoverRecordByClaim(Long claimId) throws SQLException {
        String sql = "SELECT hr.*, i.title AS item_title, u1.name AS owner_name, u2.name AS receiver_name " +
                     "FROM handover_records hr " +
                     "JOIN items i ON hr.item_id = i.item_id " +
                     "LEFT JOIN users u1 ON hr.owner_id = u1.user_id " +
                     "LEFT JOIN users u2 ON hr.receiver_id = u2.user_id " +
                     "WHERE hr.claim_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HandoverRecord record = new HandoverRecord();
                    record.setHandoverId(rs.getLong("handover_id"));
                    record.setClaimId(rs.getLong("claim_id"));
                    record.setItemId(rs.getLong("item_id"));
                    record.setOwnerId(rs.getLong("owner_id"));
                    record.setReceiverId(rs.getLong("receiver_id"));
                    record.setHandoverDate(rs.getTimestamp("handover_date"));
                    record.setVerificationMethod(rs.getString("verification_method"));
                    record.setRemarks(rs.getString("remarks"));
                    record.setItemTitle(rs.getString("item_title"));
                    record.setOwnerName(rs.getString("owner_name"));
                    record.setReceiverName(rs.getString("receiver_name"));
                    return record;
                }
            }
        }
        return null;
    }
}
