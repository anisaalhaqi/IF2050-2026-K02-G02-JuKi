package com.juki.controller;

import com.juki.db.DatabaseHelper;
import com.juki.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileManager {
    public boolean updateUser(User user) {
        String sql = "UPDATE User SET full_name = ?, profile_photo_path = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getProfilePhotoPath());
            pstmt.setInt(3, user.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            return false;
        }
    }
}
