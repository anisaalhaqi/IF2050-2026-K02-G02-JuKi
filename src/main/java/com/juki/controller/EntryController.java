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
        System.out.println("[EntryController] getEntryDetail called for ID: " + id);
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    JournalEntry entry = mapResultSetToEntry(rs);
                    String photoId = rs.getString("photo_id");
                    System.out.println("[EntryController] Retrieved entry. photo_id column: " + photoId);
                    entry.setPhotos(loadPhotosFromIds(photoId));
                    System.out.println("[EntryController] Entry has " + entry.getPhotos().size() + " photos");
                    return entry;
                } else {
                    System.out.println("[EntryController] No entry found for ID: " + id);
                }
            }
        } catch (SQLException e) {
            System.err.println("[EntryController] Error fetching entry detail: " + e.getMessage());
            e.printStackTrace();
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
        String selectPhotoPathsSql = "SELECT filePath FROM Photo WHERE id IN (%s)";
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

                // First, get file paths to delete files
                List<String> filePaths = new ArrayList<>();
                try (PreparedStatement pstmtPaths = conn.prepareStatement(String.format(selectPhotoPathsSql, placeholders))) {
                    for (int i = 0; i < ids.length; i++) {
                        pstmtPaths.setInt(i + 1, Integer.parseInt(ids[i].trim()));
                    }
                    try (ResultSet rs = pstmtPaths.executeQuery()) {
                        while (rs.next()) {
                            filePaths.add(rs.getString("filePath"));
                        }
                    }
                }

                // Delete files
                for (String path : filePaths) {
                    try {
                        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(path));
                    } catch (java.io.IOException e) {
                        System.err.println("Error deleting file: " + path + " - " + e.getMessage());
                    }
                }

                // Delete from database
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

    public void updateEntryWithPhotos(JournalEntry entry, List<Photo> photosToKeep) {
        String insertPhotoSql = "INSERT INTO Photo(filePath) VALUES(?)";
        String updateJournalSql = "UPDATE JournalEntry SET category=?, title=?, description=?, trigger=?, target=?, date=?, time=?, photo_id=? WHERE id=?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            List<Integer> photoIds = new ArrayList<>();

            // 1. Insert new photos and keep track of existing ones
            if (photosToKeep != null && !photosToKeep.isEmpty()) {
                try (PreparedStatement pstmtPhoto = conn.prepareStatement(insertPhotoSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (Photo photo : photosToKeep) {
                        if (photo != null && photo.getFilePath() != null) {
                            if (photo.getId() != null) {
                                photoIds.add(photo.getId());
                            } else {
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
            }

            String photoIdsStr = photoIds.isEmpty() ? null : photoIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");

            // 2. Identify old photos that need to be deleted
            String oldPhotoIdsSql = "SELECT photo_id FROM JournalEntry WHERE id = ?";
            String oldPhotoIdsStr = null;
            try (PreparedStatement pstmtSelect = conn.prepareStatement(oldPhotoIdsSql)) {
                pstmtSelect.setInt(1, entry.getId());
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        oldPhotoIdsStr = rs.getString("photo_id");
                    }
                }
            }

            List<Integer> idsToDelete = new ArrayList<>();
            if (oldPhotoIdsStr != null && !oldPhotoIdsStr.trim().isEmpty()) {
                String[] oldIds = oldPhotoIdsStr.split(",");
                for (String oldIdStr : oldIds) {
                    try {
                        int oldId = Integer.parseInt(oldIdStr.trim());
                        if (!photoIds.contains(oldId)) {
                            idsToDelete.add(oldId);
                        }
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }

            // 3. Update the JournalEntry record
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

            // 4. Clean up deleted photo files and records
            if (!idsToDelete.isEmpty()) {
                String placeholders = String.join(",", java.util.Collections.nCopies(idsToDelete.size(), "?"));
                String selectPhotoPathsSql = "SELECT filePath FROM Photo WHERE id IN (" + placeholders + ")";
                List<String> filePaths = new ArrayList<>();

                try (PreparedStatement pstmtPaths = conn.prepareStatement(selectPhotoPathsSql)) {
                    for (int i = 0; i < idsToDelete.size(); i++) {
                        pstmtPaths.setInt(i + 1, idsToDelete.get(i));
                    }
                    try (ResultSet rs = pstmtPaths.executeQuery()) {
                        while (rs.next()) {
                            filePaths.add(rs.getString("filePath"));
                        }
                    }
                }

                for (String path : filePaths) {
                    try {
                        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(path));
                    } catch (java.io.IOException e) {
                        System.err.println("Error deleting file: " + path + " - " + e.getMessage());
                    }
                }

                String deletePhotosSql = "DELETE FROM Photo WHERE id IN (" + placeholders + ")";
                try (PreparedStatement pstmtPhotos = conn.prepareStatement(deletePhotosSql)) {
                    for (int i = 0; i < idsToDelete.size(); i++) {
                        pstmtPhotos.setInt(i + 1, idsToDelete.get(i));
                    }
                    pstmtPhotos.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error updating journal: " + e.getMessage());
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
        System.out.println("[EntryController] loadPhotosFromIds called with: " + photoIdsStr);
        if (photoIdsStr == null || photoIdsStr.trim().isEmpty()) {
            System.out.println("[EntryController] photoIdsStr is null or empty");
            return photos;
        }
        String[] ids = photoIdsStr.split(",");
        System.out.println("[EntryController] Found " + ids.length + " photo IDs");
        if (ids.length == 0) {
            return photos;
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.length, "?"));
        String sql = "SELECT id, filePath FROM Photo WHERE id IN (" + placeholders + ")";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.length; i++) {
                int photoId = Integer.parseInt(ids[i].trim());
                pstmt.setInt(i + 1, photoId);
                System.out.println("[EntryController] Query param " + (i + 1) + ": " + photoId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int photoId = rs.getInt("id");
                    String filePath = rs.getString("filePath");
                    System.out.println("[EntryController] Loaded photo ID=" + photoId + ", path=" + filePath);
                    photos.add(new Photo(photoId, filePath));
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("[EntryController] Error loading photos from IDs: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[EntryController] Total photos loaded: " + photos.size());
        return photos;
    }
}
