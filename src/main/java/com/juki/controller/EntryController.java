package com.juki.controller;

import com.juki.db.DatabaseHelper;
import com.juki.model.JournalEntry;
import com.juki.model.Photo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EntryController {

    private List<JournalEntry> journal = new ArrayList<>();

    public List<JournalEntry> getAllEntries(int userId) {
        List<JournalEntry> entries = new ArrayList<>();
        String sql = "SELECT j.*, p.filePath FROM JournalEntry j LEFT JOIN Photo p ON j.photo_id = p.id WHERE j.user_id = ? ORDER BY j.date DESC, j.time DESC";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToEntry(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entries: " + e.getMessage());
        }
        return entries;
    }

    public JournalEntry getEntryDetail(int id) {
        String sql = "SELECT j.*, p.filePath FROM JournalEntry j LEFT JOIN Photo p ON j.photo_id = p.id WHERE j.id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntry(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entry detail: " + e.getMessage());
        }
        return null;
    }

    public boolean isDataEmpty(int userId) {
        String sql = "SELECT COUNT(*) FROM JournalEntry WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if data is empty: " + e.getMessage());
        }
        return true;
    }

    public void addEntry(JournalEntry entry) {
        String insertPhotoSql = "INSERT INTO Photo(filePath) VALUES(?)";
        String insertJournalSql = "INSERT INTO JournalEntry(category, title, description, trigger, target, date, time, photo_id, user_id) VALUES(?,?,?,?,?,?,?,?,?)";
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            Integer photoId = null;
            
            if (entry.getPhoto() != null && entry.getPhoto().getFilePath() != null) {
                try (PreparedStatement pstmtPhoto = conn.prepareStatement(insertPhotoSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtPhoto.setString(1, entry.getPhoto().getFilePath());
                    pstmtPhoto.executeUpdate();
                    ResultSet rsPhoto = pstmtPhoto.getGeneratedKeys();
                    if (rsPhoto.next()) {
                        photoId = rsPhoto.getInt(1);
                    }
                }
            }

            try (PreparedStatement pstmtJournal = conn.prepareStatement(insertJournalSql)) {
                pstmtJournal.setString(1, entry.getCategory());
                pstmtJournal.setString(2, entry.getTitle());
                pstmtJournal.setString(3, entry.getDescription());
                pstmtJournal.setString(4, entry.getTrigger());
                pstmtJournal.setString(5, entry.getTarget());
                pstmtJournal.setString(6, entry.getDate() != null ? entry.getDate().toString() : null);
                pstmtJournal.setString(7, entry.getTime() != null ? entry.getTime().toString() : null);
                if (photoId != null) {
                    pstmtJournal.setInt(8, photoId);
                } else {
                    pstmtJournal.setNull(8, Types.INTEGER);
                }
                    pstmtJournal.setInt(9, entry.getUserId());
                pstmtJournal.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error adding entry: " + e.getMessage());
        }
    }

    public void deleteEntry(int id) {
        String deleteJournalSql = "DELETE FROM JournalEntry WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteJournalSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting entry: " + e.getMessage());
        }
    }

    public void updateEntry(JournalEntry entry) {
        String updateJournalSql = "UPDATE JournalEntry SET category=?, title=?, description=?, trigger=?, target=?, date=?, time=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateJournalSql)) {
            pstmt.setString(1, entry.getCategory());
            pstmt.setString(2, entry.getTitle());
            pstmt.setString(3, entry.getDescription());
            pstmt.setString(4, entry.getTrigger());
            pstmt.setString(5, entry.getTarget());
            pstmt.setString(6, entry.getDate() != null ? entry.getDate().toString() : null);
            pstmt.setString(7, entry.getTime() != null ? entry.getTime().toString() : null);
            pstmt.setInt(8, entry.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating entry: " + e.getMessage());
        }
    }

    public void cancelEntry(Integer id) {
        System.out.println("Membatalkan proses entri dengan ID: " + id);
    }

    private JournalEntry mapResultSetToEntry(ResultSet rs) throws SQLException {
        JournalEntry entry = new JournalEntry();
        entry.setId(rs.getInt("id"));
        entry.setCategory(rs.getString("category"));
        entry.setTitle(rs.getString("title"));
        entry.setDescription(rs.getString("description"));
        entry.setTrigger(rs.getString("trigger"));
        entry.setTarget(rs.getString("target"));
        entry.setUserId(rs.getInt("user_id"));
        
        String dateStr = rs.getString("date");
        if (dateStr != null) entry.setDate(LocalDate.parse(dateStr));
        
        String timeStr = rs.getString("time");
        if (timeStr != null) entry.setTime(LocalTime.parse(timeStr));
        
        int photoId = rs.getInt("photo_id");
        if (!rs.wasNull()) {
            Photo p = new Photo(photoId, rs.getString("filePath"));
            entry.setPhoto(p);
        }
        return entry;
    }
}
