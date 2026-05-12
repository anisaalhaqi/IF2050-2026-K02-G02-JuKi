package com.juki.controller;

import com.juki.db.DatabaseHelper;
import com.juki.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrationFormController {
    public User signIn(String username, String password) {
        String sqlSelect = "SELECT id, full_name, profile_photo_path FROM User WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("full_name"), username, rs.getString("profile_photo_path")); // Berhasil sign in
            }
        } catch (SQLException e) {
            System.err.println("Error saat sign in: " + e.getMessage());
        }
        return null; // Gagal sign in
    }

    public User signUp(String fullName, String username, String password) {
        String sqlInsert = "INSERT INTO User (full_name, username, password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlInsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                return new User(generatedId, fullName, username, null);
            }
        } catch (SQLException e) {
            System.err.println("Error saat sign up (username mungkin sudah ada): " + e.getMessage());
        }
        return null; // Gagal mendaftar
    }
}