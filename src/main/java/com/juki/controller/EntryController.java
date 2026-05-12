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
        String sql = "SELECT j.*, p.filePath FROM JournalEntry j " +
                     "LEFT JOIN Photo p ON j.photo_id = p.id " +
                     "WHERE j.user_id = ? AND (" +
                     "LOWER(j.title) LIKE ? OR LOWER(j.description) LIKE ? OR LOWER(j.category) LIKE ? OR " +
                     "LOWER(j.trigger) LIKE ? OR LOWER(j.target) LIKE ?) " +
                     "ORDER BY j.date DESC, j.time DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            for (int i = 2; i <= 6; i++) {
                pstmt.setString(i, searchTerm);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToEntry(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching entries: " + e.getMessage());
        }
        return entries;
    }

    public List<JournalEntry> getEntriesByDate(int userId, LocalDate date) {
        if (date == null) {
            return getAllEntries(userId);
        }

        List<JournalEntry> entries = new ArrayList<>();
        String sql = "SELECT j.*, p.filePath FROM JournalEntry j " +
                     "LEFT JOIN Photo p ON j.photo_id = p.id " +
                     "WHERE j.user_id = ? AND j.date = ? " +
                     "ORDER BY j.date DESC, j.time DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToEntry(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching entries by date: " + e.getMessage());
        }
        return entries;
    }

    public JournalEntry getEntryDetail(int id) {
        String sql = "SELECT * FROM JournalEntry WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    JournalEntry entry = mapResultSetToEntry(rs);
                    entry.setPhotos(loadPhotosFromIds(rs.getString("photo_id")));
                    return entry;
                }
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
                                    photo.setId(photoId); // Set ID ke objek Photo
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

    public void deleteEntry(int id) {
        String selectPhotoIdsSql = "SELECT photo_id FROM JournalEntry WHERE id = ?";
        String deletePhotosSql = "DELETE FROM Photo WHERE id IN (%s)";
        String deleteJournalSql = "DELETE FROM JournalEntry WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            String photoIdsStr = null;
            try (PreparedStatement pstmtSelect = conn.prepareStatement(selectPhotoIdsSql)) {
                pstmtSelect.setInt(1, id);
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        photoIdsStr = rs.getString("photo_id");
                    }
                }
            }
            if (photoIdsStr != null && !photoIdsStr.trim().isEmpty()) {
                String[] ids = photoIdsStr.split(",");
                String placeholders = String.join(",", java.util.Collections.nCopies(ids.length, "?"));
                try (PreparedStatement pstmtPhotos = conn.prepareStatement(String.format(deletePhotosSql, placeholders))) {
                    for (int i = 0; i < ids.length; i++) {
                        pstmtPhotos.setInt(i + 1, Integer.parseInt(ids[i].trim()));
                    }
                    pstmtPhotos.executeUpdate();
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
        
        entry.setPhotos(new ArrayList<>()); // will be loaded separately
        return entry;
    }

    private List<Photo> loadPhotosFromIds(String photoIdsStr) {
        List<Photo> photos = new ArrayList<>();
        if (photoIdsStr == null || photoIdsStr.trim().isEmpty()) {
            return photos;
        }
        String[] ids = photoIdsStr.split(",");
        if (ids.length == 0) {
            return photos;
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.length, "?"));
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
            System.err.println("Error loading photos from IDs: " + e.getMessage());
        }
        return photos;
    }
}
