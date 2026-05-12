package com.juki.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseHelper {
    // Database URL
    private static final String DB_DIR = "data";
    private static final String DB_NAME = "juki.db";
    private static final String URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_NAME;

    public static void initializeDatabase() {
        // Create directory if it doesn't exist
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String sqlUserTable = "CREATE TABLE IF NOT EXISTS User (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT NOT NULL," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "profile_photo_path TEXT" +
                ");";

        String sqlPhotoTable = "CREATE TABLE IF NOT EXISTS Photo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "filePath TEXT NOT NULL" +
                ");";

        String sqlJournalTable = "CREATE TABLE IF NOT EXISTS JournalEntry (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category TEXT," +
                "title TEXT," +
                "description TEXT," +
                "trigger TEXT," +
                "target TEXT," +
                "date TEXT," +
                "time TEXT," +
                "photo_id INTEGER," +
                "user_id INTEGER," +
                "FOREIGN KEY (photo_id) REFERENCES Photo(id)," +
                "FOREIGN KEY (user_id) REFERENCES User(id)" +
                ");";

        String sqlSelfCareTable = "CREATE TABLE IF NOT EXISTS SelfCareGoal (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "is_completed INTEGER DEFAULT 0," +
                "date TEXT NOT NULL" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUserTable);
            
            // Check if profile_photo_path column exists, if not, add it
            try {
                stmt.execute("ALTER TABLE User ADD COLUMN profile_photo_path TEXT;");
            } catch (SQLException e) {
                // Column likely already exists, ignore
            }

            stmt.execute(sqlPhotoTable);
            stmt.execute(sqlJournalTable);
            stmt.execute(sqlSelfCareTable);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void clearAllData() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Menghapus data dari child table terlebih dahulu agar tidak konflik dengan Foreign Key
            stmt.execute("DELETE FROM JournalEntry;");
            stmt.execute("DELETE FROM Photo;");
            stmt.execute("DELETE FROM SelfCareGoal;");
            stmt.execute("DELETE FROM User;");
            System.out.println("Semua data dalam tabel berhasil dikosongkan (Tabel tetap utuh).");
        } catch (SQLException e) {
            System.err.println("Gagal mengosongkan data: " + e.getMessage());
        }
    }

    public static void dropAllTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS JournalEntry;");
            stmt.execute("DROP TABLE IF EXISTS Photo;");
            stmt.execute("DROP TABLE IF EXISTS SelfCareGoal;");
            stmt.execute("DROP TABLE IF EXISTS User;");
            System.out.println("Semua tabel berhasil dihapus dari database.");
        } catch (SQLException e) {
            System.err.println("Gagal menghapus tabel: " + e.getMessage());
        }
    }
}
