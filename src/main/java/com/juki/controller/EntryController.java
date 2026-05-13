package com.juki.controller;

import com.juki.db.DatabaseHelper;
import com.juki.model.JournalEntry;
import com.juki.model.Photo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class EntryController {

    public List<JournalEntry> getAllEntries(int userId) {
        List<JournalEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM JournalEntry WHERE user_id = ? ORDER BY date DESC, time DESC";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    JournalEntry entry = mapResultSetToEntry(rs);
                    entry.setPhotos(loadPhotosFromIds(rs.getString("photo_id")));
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entries: " + e.getMessage());
        }
        return entries;
    }

    public List<JournalEntry> searchEntries(int userId, String keyword) {
        List<JournalEntry> entries = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEntries(userId);
        }

        String searchTerm = "%" + keyword.toLowerCase() + "%";
        String sql = "SELECT * FROM JournalEntry WHERE user_id = ? AND (" +
                     "LOWER(title) LIKE ? OR LOWER(description) LIKE ? OR LOWER(category) LIKE ? OR " +
                     "LOWER(trigger) LIKE ? OR LOWER(target) LIKE ?) " +
                     "ORDER BY date DESC, time DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            for (int i = 2; i <= 6; i++) {
                pstmt.setString(i, searchTerm);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    JournalEntry entry = mapResultSetToEntry(rs);
                    entry.setPhotos(loadPhotosFromIds(rs.getString("photo_id")));
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching entries: " + e.getMessage());
        }
        return entries;
    }

    public List<JournalEntry> getEntriesByDate(int userId, LocalDate date) {
        if (date == null) return getAllEntries(userId);

        List<JournalEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM JournalEntry WHERE user_id = ? AND date = ? ORDER BY time DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    JournalEntry entry = mapResultSetToEntry(rs);
                    entry.setPhotos(loadPhotosFromIds(rs.getString("photo_id")));
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entries by date: " + e.getMessage());
        }
        return entries;
    }

    public JournalEntry getEntryDetail(int id) {
        String sql = "SELECT * FROM JournalEntry WHERE id = ?";
        System.out.println("[EntryController] getEntryDetail called for ID: " + id);
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    JournalEntry entry = mapResultSetToEntry(rs);
                    String photoId = rs.getString("photo_id");
                    entry.setPhotos(loadPhotosFromIds(photoId));
                    return entry;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entry detail: " + e.getMessage());
        }
        return null;
    }

    public void saveJournal(JournalEntry entry) {
        String insertPhotoSql = "INSERT INTO Photo(filePath) VALUES(?)";
        String insertJournalSql = "INSERT INTO JournalEntry(category, title, description, trigger, target, date, time, photo_id, user_id) VALUES(?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            List<Integer> photoIds = new ArrayList<>();

            if (entry.getPhotos() != null && !entry.getPhotos().isEmpty()) {
                try (PreparedStatement pstmtPhoto = conn.prepareStatement(insertPhotoSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (Photo photo : entry.getPhotos()) {
                        if (photo != null && photo.getFilePath() != null) {
                            pstmtPhoto.setString(1, photo.getFilePath());
                            pstmtPhoto.executeUpdate();
                            try (ResultSet rs = pstmtPhoto.getGeneratedKeys()) {
                                if (rs.next()) {
                                    int photoId = rs.getInt(1);
                                    photoIds.add(photoId);
                                    photo.setId(photoId);
                                }
                            }
                        }
                    }
                }
            }

            String photoIdsStr = photoIds.isEmpty() ? null : photoIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");

            try (PreparedStatement pstmtJournal = conn.prepareStatement(insertJournalSql)) {
                pstmtJournal.setString(1, entry.getCategory());
                pstmtJournal.setString(2, entry.getTitle());
                pstmtJournal.setString(3, entry.getDescription());
                pstmtJournal.setString(4, entry.getTrigger());
                pstmtJournal.setString(5, entry.getTarget());
                pstmtJournal.setString(6, entry.getDate() != null ? entry.getDate().toString() : null);
                pstmtJournal.setString(7, entry.getTime() != null ? entry.getTime().toString() : null);
                pstmtJournal.setString(8, photoIdsStr);
                pstmtJournal.setInt(9, entry.getUserId());
                pstmtJournal.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error saving journal: " + e.getMessage());
        }
    }

    public void updateEntryWithPhotos(JournalEntry entry, List<Photo> photosToKeep) {
        String insertPhotoSql = "INSERT INTO Photo(filePath) VALUES(?)";
        String updateJournalSql = "UPDATE JournalEntry SET category=?, title=?, description=?, trigger=?, target=?, date=?, time=?, photo_id=? WHERE id=?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            List<Integer> photoIds = new ArrayList<>();

            // 1. Tangani foto (Keep existing atau Insert new)
            if (photosToKeep != null) {
                try (PreparedStatement pstmtPhoto = conn.prepareStatement(insertPhotoSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (Photo photo : photosToKeep) {
                        if (photo.getId() != null) {
                            photoIds.add(photo.getId());
                        } else {
                            pstmtPhoto.setString(1, photo.getFilePath());
                            pstmtPhoto.executeUpdate();
                            try (ResultSet rs = pstmtPhoto.getGeneratedKeys()) {
                                if (rs.next()) photoIds.add(rs.getInt(1));
                            }
                        }
                    }
                }
            }

            String photoIdsStr = photoIds.isEmpty() ? null : photoIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");

            // 2. Identifikasi foto lama untuk dihapus secara fisik & DB
            String oldPhotoIdsSql = "SELECT photo_id FROM JournalEntry WHERE id = ?";
            String oldPhotoIdsStr = null;
            try (PreparedStatement pstmtSelect = conn.prepareStatement(oldPhotoIdsSql)) {
                pstmtSelect.setInt(1, entry.getId());
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) oldPhotoIdsStr = rs.getString("photo_id");
                }
            }

            List<Integer> idsToDelete = new ArrayList<>();
            if (oldPhotoIdsStr != null) {
                for (String idStr : oldPhotoIdsStr.split(",")) {
                    int id = Integer.parseInt(idStr.trim());
                    if (!photoIds.contains(id)) idsToDelete.add(id);
                }
            }

            // 3. Update record jurnal
            try (PreparedStatement pstmtJournal = conn.prepareStatement(updateJournalSql)) {
                pstmtJournal.setString(1, entry.getCategory());
                pstmtJournal.setString(2, entry.getTitle());
                pstmtJournal.setString(3, entry.getDescription());
                pstmtJournal.setString(4, entry.getTrigger());
                pstmtJournal.setString(5, entry.getTarget());
                pstmtJournal.setString(6, entry.getDate() != null ? entry.getDate().toString() : null);
                pstmtJournal.setString(7, entry.getTime() != null ? entry.getTime().toString() : null);
                pstmtJournal.setString(8, photoIdsStr);
                pstmtJournal.setInt(9, entry.getId());
                pstmtJournal.executeUpdate();
            }

            // 4. Hapus record foto yang dibuang (tanpa hapus file fisik)
            if (!idsToDelete.isEmpty()) {
                String placeholders = String.join(",", Collections.nCopies(idsToDelete.size(), "?"));
                String deleteRows = "DELETE FROM Photo WHERE id IN (" + placeholders + ")";
                try (PreparedStatement pstmtDel = conn.prepareStatement(deleteRows)) {
                    for (int i = 0; i < idsToDelete.size(); i++) pstmtDel.setInt(i + 1, idsToDelete.get(i));
                    pstmtDel.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error updating journal: " + e.getMessage());
        }
    }

    public void deleteEntry(int id) {
        String selectPhotoIdsSql = "SELECT photo_id FROM JournalEntry WHERE id = ?";
        String deleteJournalSql = "DELETE FROM JournalEntry WHERE id = ?";
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            String photoIdsStr = null;
            try (PreparedStatement pstmtSelect = conn.prepareStatement(selectPhotoIdsSql)) {
                pstmtSelect.setInt(1, id);
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) photoIdsStr = rs.getString("photo_id");
                }
            }

            if (photoIdsStr != null && !photoIdsStr.trim().isEmpty()) {
                List<Integer> ids = new ArrayList<>();
                for (String s : photoIdsStr.split(",")) ids.add(Integer.parseInt(s.trim()));
                // Hapus record foto tanpa hapus file fisik
                String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
                String deleteRows = "DELETE FROM Photo WHERE id IN (" + placeholders + ")";
                try (PreparedStatement pstmtDel = conn.prepareStatement(deleteRows)) {
                    for (int i = 0; i < ids.size(); i++) pstmtDel.setInt(i + 1, ids.get(i));
                    pstmtDel.executeUpdate();
                }
            }

            try (PreparedStatement pstmtJournal = conn.prepareStatement(deleteJournalSql)) {
                pstmtJournal.setInt(1, id);
                pstmtJournal.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error deleting entry: " + e.getMessage());
        }
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
        
        String d = rs.getString("date");
        if (d != null) entry.setDate(LocalDate.parse(d));
        String t = rs.getString("time");
        if (t != null) entry.setTime(LocalTime.parse(t));
        
        return entry;
    }

    private List<Photo> loadPhotosFromIds(String photoIdsStr) {
        List<Photo> photos = new ArrayList<>();
        if (photoIdsStr == null || photoIdsStr.trim().isEmpty()) return photos;

        String[] ids = photoIdsStr.split(",");
        String placeholders = String.join(",", Collections.nCopies(ids.length, "?"));
        String sql = "SELECT id, filePath FROM Photo WHERE id IN (" + placeholders + ")";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.length; i++) {
                pstmt.setInt(i + 1, Integer.parseInt(ids[i].trim()));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    photos.add(new Photo(rs.getInt("id"), rs.getString("filePath")));
                }
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
        return photos;
    }
}