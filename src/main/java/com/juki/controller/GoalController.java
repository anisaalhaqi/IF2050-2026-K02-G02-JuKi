package com.juki.controller;

import com.juki.db.DatabaseHelper;
import com.juki.model.SelfCareGoal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalController {

    public List<SelfCareGoal> getGoalsByDate(LocalDate date, int userId) {
        List<SelfCareGoal> goals = new ArrayList<>();
        String sql = (date == null) ? 
            "SELECT * FROM SelfCareGoal WHERE user_id = ?" : 
            "SELECT * FROM SelfCareGoal WHERE date = ? AND user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (date != null) { pstmt.setString(1, date.toString()); pstmt.setInt(2, userId); }
            else { pstmt.setInt(1, userId); }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { goals.add(mapResultSetToGoal(rs)); }
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return goals;
    }

    /**
     * AMBIL BANYAK DATA SEKALIGUS (Sangat Cepat)
     */
    public Map<LocalDate, List<SelfCareGoal>> getGoalsInRange(LocalDate start, LocalDate end, int userId) {
        Map<LocalDate, List<SelfCareGoal>> map = new HashMap<>();
        String sql = "SELECT * FROM SelfCareGoal WHERE user_id = ? AND date BETWEEN ? AND ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                SelfCareGoal g = mapResultSetToGoal(rs);
                map.computeIfAbsent(g.getDate(), k -> new ArrayList<>()).add(g);
            }
        } catch (SQLException e) { System.err.println("Error bulk load: " + e.getMessage()); }
        return map;
    }

    public void updateGoalStatus(int id, boolean isCompleted, int userId) {
        String sql = "UPDATE SelfCareGoal SET is_completed = ? WHERE id = ? AND user_id = ?"; 
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isCompleted ? 1 : 0);
            pstmt.setInt(2, id);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { System.err.println("Error update status: " + e.getMessage()); }
    }

    public void saveGoalsForDate(LocalDate date, List<SelfCareGoal> goals, int userId) {      
        deleteAllGoalsForDate(date, userId);
        for (SelfCareGoal g : goals) {
            g.setUserId(userId);
            addGoal(g);
        }
    }

    private void addGoal(SelfCareGoal g) {
        String sql = "INSERT INTO SelfCareGoal(title, is_completed, date, user_id) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, g.getTitle());
            pstmt.setInt(2, g.isCompleted() ? 1 : 0);
            pstmt.setString(3, g.getDate().toString());
            pstmt.setInt(4, g.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {}
    }

    public void deleteAllGoalsForDate(LocalDate date, int userId) {
        String sql = "DELETE FROM SelfCareGoal WHERE date = ? AND user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {}
    }

    public int calculateStreak(int userId) {
        String sql = "SELECT date FROM SelfCareGoal WHERE user_id = ? GROUP BY date HAVING MIN(is_completed) = 1";
        List<LocalDate> dates = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { dates.add(LocalDate.parse(rs.getString("date"))); }
        } catch (SQLException e) { return 0; }
        
        int streak = 0;
        LocalDate cur = LocalDate.now();
        // Skip today if no targets or not done
        List<SelfCareGoal> today = getGoalsByDate(cur, userId);
        if (today.isEmpty() || !dates.contains(cur)) cur = cur.minusDays(1);
        
        while (dates.contains(cur)) { streak++; cur = cur.minusDays(1); }
        return streak;
    }

    private SelfCareGoal mapResultSetToGoal(ResultSet rs) throws SQLException {
        SelfCareGoal g = new SelfCareGoal();
        g.setId(rs.getInt("id"));
        g.setTitle(rs.getString("title"));
        g.setCompleted(rs.getInt("is_completed") == 1);
        g.setDate(LocalDate.parse(rs.getString("date")));
        g.setUserId(rs.getInt("user_id"));
        return g;
    }
}