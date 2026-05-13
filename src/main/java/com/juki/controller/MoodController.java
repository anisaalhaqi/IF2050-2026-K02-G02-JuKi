package com.juki.controller;

import com.juki.db.DatabaseHelper;
import com.juki.model.DailyMood;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoodController {

    public void saveOrUpdateMood(int userId, String moodName, LocalDate date) {
        String checkSql = "SELECT id FROM DailyMood WHERE user_id = ? AND date = ?";
        String insertSql = "INSERT INTO DailyMood (user_id, mood_name, date) VALUES (?, ?, ?)";
        String updateSql = "UPDATE DailyMood SET mood_name = ? WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            int existingId = -1;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setString(2, date.toString());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getInt("id");
                    }
                }
            }

            if (existingId != -1) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, moodName);
                    updateStmt.setInt(2, existingId);
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, moodName);
                    insertStmt.setString(3, date.toString());
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving/updating mood: " + e.getMessage());
        }
    }

    public DailyMood getMoodByDate(int userId, LocalDate date) {
        String sql = "SELECT * FROM DailyMood WHERE user_id = ? AND date = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new DailyMood(
                            rs.getInt("id"),
                            rs.getString("mood_name"),
                            LocalDate.parse(rs.getString("date")),
                            rs.getInt("user_id")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching mood: " + e.getMessage());
        }
        return null;
    }

    public List<DailyMood> getMoodsByMonth(int userId, LocalDate monthStart) {
        List<DailyMood> moods = new ArrayList<>();
        String sql = "SELECT * FROM DailyMood WHERE user_id = ? AND date LIKE ?";
        String monthPattern = monthStart.toString().substring(0, 7) + "%"; // YYYY-MM%

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, monthPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    moods.add(new DailyMood(
                            rs.getInt("id"),
                            rs.getString("mood_name"),
                            LocalDate.parse(rs.getString("date")),
                            rs.getInt("user_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching moods by month: " + e.getMessage());
        }
        return moods;
    }
}
