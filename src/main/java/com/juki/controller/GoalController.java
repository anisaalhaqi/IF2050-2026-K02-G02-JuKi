package com.juki.controller;

import com.juki.db.DatabaseHelper;
import com.juki.model.SelfCareGoal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GoalController {
    
    public List<SelfCareGoal> getGoalsByDate(LocalDate date) {
        List<SelfCareGoal> goals = new ArrayList<>();
        String sql = (date == null) ? "SELECT * FROM SelfCareGoal" : "SELECT * FROM SelfCareGoal WHERE date = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (date != null) {
                pstmt.setString(1, date.toString());
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                SelfCareGoal goal = new SelfCareGoal();
                goal.setId(rs.getInt("id"));
                goal.setTitle(rs.getString("title"));
                goal.setCompleted(rs.getInt("is_completed") == 1);
                goal.setDate(LocalDate.parse(rs.getString("date")));
                goals.add(goal);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching goals: " + e.getMessage());
        }
        return goals;
    }

    public void addGoal(SelfCareGoal goal) {
        String sql = "INSERT INTO SelfCareGoal(title, is_completed, date) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goal.getTitle());
            pstmt.setInt(2, goal.isCompleted() ? 1 : 0);
            pstmt.setString(3, goal.getDate().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding goal: " + e.getMessage());
        }
    }

    public void updateGoalStatus(int id, boolean isCompleted) {
        String sql = "UPDATE SelfCareGoal SET is_completed = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isCompleted ? 1 : 0);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating goal status: " + e.getMessage());
        }
    }

    public void deleteGoal(int id) {
        String sql = "DELETE FROM SelfCareGoal WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting goal: " + e.getMessage());
        }
    }

    public void saveGoalsForDate(LocalDate date, List<SelfCareGoal> goals) {
        deleteAllGoalsForDate(date);
        for (SelfCareGoal goal : goals) {
            addGoal(goal);
        }
    }

    public void deleteAllGoalsForDate(LocalDate date) {
        String sql = "DELETE FROM SelfCareGoal WHERE date = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting goals for date: " + e.getMessage());
        }
    }

    public int calculateStreak(int userId) {
        int streak = 0;
        LocalDate current = LocalDate.now();
        
        List<SelfCareGoal> todayGoals = getGoalsByDate(current);
        if (!todayGoals.isEmpty()) {
            boolean todayAllDone = todayGoals.stream().allMatch(SelfCareGoal::isCompleted);
            if (!todayAllDone) {
                current = current.minusDays(1);
            }
        } else {
            current = current.minusDays(1);
        }

        int daysChecked = 0;
        while (daysChecked < 365) {
            List<SelfCareGoal> dayGoals = getGoalsByDate(current);
            if (dayGoals.isEmpty()) {
                current = current.minusDays(1);
                daysChecked++;
                continue; 
            }
            
            boolean allDone = dayGoals.stream().allMatch(SelfCareGoal::isCompleted);
            if (allDone) {
                streak++;
                current = current.minusDays(1);
            } else {
                break;
            }
            daysChecked++;
        }
        return streak;
    }
}