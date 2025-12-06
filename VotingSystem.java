import java.sql.*;
import java.util.*;

public class VotingSystemMySQL {

    private Long blockTime = null; // Timestamp after which voting is blocked

    // Register candidate
    public boolean registerCandidate(String candidateId) {
        try (Connection conn = MySQLDatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO candidates(candidate_id, votes) VALUES(?, 0)")) {
            ps.setString(1, candidateId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // Candidate exists
        }
    }

    // Register voter if not exists
    private void registerVoter(String voterId) {
        try (Connection conn = MySQLDatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT IGNORE INTO voters(voter_id) VALUES(?)")) {
            ps.setString(1, voterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Block voter registration
    public void blockVoterRegistration(long timestamp) {
        this.blockTime = timestamp;
    }

    // Cast a vote
    public boolean vote(long timestamp, String voterId, String candidateId) {
        if (blockTime != null && timestamp >= blockTime) return false;

        try (Connection conn = MySQLDatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            // Check candidate
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT votes FROM candidates WHERE candidate_id = ?")) {
                check.setString(1, candidateId);
                ResultSet rs = check.executeQuery();
                if (!rs.next()) return false;
            }

            // Register voter
            registerVoter(voterId);

            // Insert vote
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO votes(voter_id, candidate_id, timestamp) VALUES(?, ?, ?)")) {
                ps.setString(1, voterId);
                ps.setString(2, candidateId);
                ps.setLong(3, timestamp);
                ps.executeUpdate();
            }

            // Update candidate votes
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE candidates SET votes = votes + 1 WHERE candidate_id = ?")) {
                ps.setString(1, candidateId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get votes for a candidate
    public int getVotes(String candidateId) {
        try (Connection conn = MySQLDatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT votes FROM candidates WHERE candidate_id = ?")) {
            ps.setString(1, candidateId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("votes");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get top N candidates
    public List<String> topNCandidates(int n) {
        List<String> topCandidates = new ArrayList<>();
        try (Connection conn = MySQLDatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT candidate_id FROM candidates ORDER BY votes DESC LIMIT ?")) {
            ps.setInt(1, n);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                topCandidates.add(rs.getString("candidate_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topCandidates;
    }

    // Get voting history for a voter
    public Map<String, Integer> getVotingHistory(String voterId) {
        Map<String, Integer> history = new HashMap<>();
        try (Connection conn = MySQLDatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT candidate_id, COUNT(*) as count FROM votes WHERE voter_id = ? GROUP BY candidate_id")) {
            ps.setString(1, voterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                history.put(rs.getString("candidate_id"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history.isEmpty() ? null : history;
    }

    // Change vote (within 24 hours)
    public boolean changeVote(long timestamp, String voterId, String oldCandidateId, String newCandidateId) {
        try (Connection conn = MySQLDatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            // Check candidates exist
            if (!registerCandidateCheck(oldCandidateId) || !registerCandidateCheck(newCandidateId)) return false;

            // Find last vote for oldCandidateId
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT rowid, timestamp FROM votes WHERE voter_id = ? AND candidate_id = ? ORDER BY timestamp DESC LIMIT 1")) {
                ps.setString(1, voterId);
                ps.setString(2, oldCandidateId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return false;

                long voteTime = rs.getLong("timestamp");
                long rowId = rs.getLong("rowid");
                if (timestamp - voteTime > 86400) return false; // 24 hours

                // Delete old vote
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM votes WHERE rowid = ?")) {
                    del.setLong(1, rowId);
                    del.executeUpdate();
                }

                // Add new vote
                vote(timestamp, voterId, newCandidateId);

                // Update candidates count
                try (PreparedStatement updateOld = conn.prepareStatement(
                        "UPDATE candidates SET votes = votes - 1 WHERE candidate_id = ?")) {
                    updateOld.setString(1, oldCandidateId);
                    updateOld.executeUpdate();
                }

                conn.commit();
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean registerCandidateCheck(String candidateId) {
        try (Connection conn = MySQLDatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT votes FROM candidates WHERE candidate_id = ?")) {
            ps.setString(1, candidateId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
