package com.juki.controller;

import com.juki.db.DatabaseHelper;
import com.juki.model.SelfCareGoal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoalController {

    public List<SelfCareGoal> getGoalsByDate(LocalDate date, int userId) {
        List<SelfCareGoal> goals = new ArrayList<>();
        String sql = (date == null) ? 
            "SELECT * FROM SelfCareGoal WHERE user_id = ? ORDER BY id DESC" : 
            "SELECT * FROM SelfCareGoal WHERE date = ? AND user_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (date != null) {
                pstmt.setString(1, date.toString());
                pstmt.setInt(2, userId);
            } else {
                pstmt.setInt(1, userId);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                SelfCareGoal goal = new SelfCareGoal();
                goal.setId(rs.getInt("id"));
                goal.setTitle(rs.getString("title"));
                goal.setCompleted(rs.getInt("is_completed") == 1);
                goal.setDate(LocalDate.parse(rs.getString("date")));
                goal.setUserId(rs.getInt("user_id"));
                goals.add(goal);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching goals: " + e.getMessage());
        }
        return goals;
    }

    public void updateGoalStatus(int id, boolean isCompleted, int userId) {
        String sql = "UPDATE SelfCareGoal SET is_completed = ? WHERE id = ? AND user_id = ?"; 
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isCompleted ? 1 : 0);
            pstmt.setInt(2, id);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating goal status: " + e.getMessage());
        }
    }

    public void saveGoalsForDate(LocalDate date, List<SelfCareGoal> goals, int userId) {      
        deleteAllGoalsForDate(date, userId);
        for (SelfCareGoal goal : goals) {
            goal.setUserId(userId);
            addGoal(goal);
        }
    }

    public void addGoal(SelfCareGoal goal) {
        String sql = "INSERT INTO SelfCareGoal(title, is_completed, date, user_id) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goal.getTitle());
            pstmt.setInt(2, goal.isCompleted() ? 1 : 0);
            pstmt.setString(3, goal.getDate().toString());
            pstmt.setInt(4, goal.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding goal: " + e.getMessage());
        }
    }

    public void deleteAllGoalsForDate(LocalDate date, int userId) {
        String sql = "DELETE FROM SelfCareGoal WHERE date = ? AND user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting goals for date: " + e.getMessage());
        }
    }

    public int calculateStreak(int userId) {
        // Query efisien untuk mendapatkan semua tanggal yang 'all-completed'
        String sql = "SELECT date FROM SelfCareGoal WHERE user_id = ? GROUP BY date HAVING MIN(is_completed) = 1";
        Set<LocalDate> completedDates = new HashSet<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                completedDates.add(LocalDate.parse(rs.getString("date")));
            }
        } catch (SQLException e) { return 0; }

        if (completedDates.isEmpty()) return 0;

        int streak = 0;
        LocalDate current = LocalDate.now();
        
        // Cek apakah hari ini harus dihitung atau skip
        List<SelfCareGoal> todayGoals = getGoalsByDate(current, userId);
        if (todayGoals.isEmpty() || completedDates.contains(current)) {
            // Jika hari ini kosong atau sudah selesai, mulai cek dari hari ini/kemarin
            if (todayGoals.isEmpty()) current = current.minusDays(1);
        } else {
            // Jika hari ini ada target tapi BELUM selesai, streak dihitung dari kemarin
            current = current.minusDays(1);
        }

        while (completedDates.contains(current)) {
            streak++;
            current = current.minusDays(1);
        }
        return streak;
    }
}