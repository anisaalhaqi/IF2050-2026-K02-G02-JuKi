package com.juki.controller;

import com.juki.db.DatabaseHelper;
import com.juki.model.SelfCareGoal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GoalController {
    
    public List<SelfCareGoal> getGoalsByDate(LocalDate date, int userId) {
        List<SelfCareGoal> goals = new ArrayList<>();
        String sql = (date == null) ? "SELECT * FROM SelfCareGoal WHERE user_id = ?" : "SELECT * FROM SelfCareGoal WHERE date = ? AND user_id = ?";
        
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

    public void deleteGoal(int id, int userId) {
        String sql = "DELETE FROM SelfCareGoal WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting goal: " + e.getMessage());
        }
    }

    public void saveGoalsForDate(LocalDate date, List<SelfCareGoal> goals, int userId) {
        deleteAllGoalsForDate(date, userId);
        for (SelfCareGoal goal : goals) {
            goal.setUserId(userId);
            addGoal(goal);
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
}