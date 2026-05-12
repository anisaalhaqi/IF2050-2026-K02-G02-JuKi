package com.juki;

import com.juki.controller.RegistrationFormController;
import com.juki.controller.SearchController;
import com.juki.db.DatabaseHelper;
import com.juki.model.JournalEntry;
import com.juki.model.User;
import com.juki.view.DashboardView;
import java.util.List;
import com.juki.view.CalendarView;
import com.juki.view.EntryFormView;
import com.juki.view.EntryListView;
import com.juki.view.RegistrationFormView;
import com.juki.view.ProfileView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.LocalDate;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Inisialisasi Tabel SQLite
        DatabaseHelper.initializeDatabase();

        showLoginScreen(primaryStage);
    }

    private void showLoginScreen(Stage primaryStage) {
        RegistrationFormView loginView = new RegistrationFormView(user -> {
            showMainDashboard(primaryStage, user);
        });

        Scene scene = new Scene(loginView.getView(), 1600, 900);
        primaryStage.setTitle("JuKi - Login");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void showMainDashboard(Stage primaryStage, User user) {
        BorderPane root = new BorderPane();
        SearchController searchController = new SearchController();
        
        // Top Navigation Bar
        HBox navBar = new HBox(20);
        navBar.setStyle("-fx-background-color: #A114AC; -fx-padding: 42px 100px;");
        navBar.setAlignment(Pos.CENTER_LEFT);

        // Logo Section: [Image]
        ImageView logo = new ImageView();
        try {
            logo.setImage(new Image("file:img/dashboard/logo (3).png"));
            logo.setFitHeight(50);
            logo.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }
        
        HBox logoBox = new HBox(32);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.getChildren().addAll(logo);

        TextField searchField = new TextField();
        searchField.setPromptText("Cari Jurnal");
        searchField.setPrefWidth(320);
        searchField.setStyle("-fx-background-radius: 100px; -fx-background-color: white; -fx-padding: 12px 18px; -fx-font-size: 16px;");
        Button searchButton = new Button("Cari");
        searchButton.setStyle("-fx-background-color: #FFE341; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 16px; -fx-background-radius: 100px; -fx-padding: 12px 24px; -fx-cursor: hand;");

        Runnable performSearch = () -> {
            String keyword = searchField.getText() != null ? searchField.getText().trim() : "";
            if (keyword.isEmpty()) {
                return;
            }
            List<JournalEntry> results = searchController.searchEntries(new com.juki.model.SearchFilter(null, keyword, null), user.getId());
            EntryListView searchResultView = new EntryListView(user, results);
            root.setCenter(searchResultView.getView());
        };

        searchField.setOnAction(e -> performSearch.run());
        searchButton.setOnAction(e -> performSearch.run());
        
        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setVisible(false);
        searchBox.setManaged(false);
        
        // Navigation Links Section
        HBox navLinks = new HBox(64);
        navLinks.setAlignment(Pos.CENTER_LEFT);
        
        Label navBeranda = new Label("Beranda");
        navBeranda.setTextFill(Color.web("#F2F6FC"));
        navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        navBeranda.setStyle("-fx-cursor: hand;");

        Label navJurnal = new Label("Jurnal");
        navJurnal.setTextFill(Color.web("#F2F6FC"));
        navJurnal.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        navJurnal.setStyle("-fx-cursor: hand;");

        Label navKalendar = new Label("Kalendar");
        navKalendar.setTextFill(Color.web("#F2F6FC"));
        navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        navKalendar.setStyle("-fx-cursor: hand;");
        
        // Tambah Self-Care Button (Action Button)
        Button btnAction = new Button("Tambah Self-Care");
        try {
            ImageView notesIcon = new ImageView(new Image("file:img/icons/notes.png"));
            notesIcon.setFitWidth(32);
            notesIcon.setFitHeight(32);
            notesIcon.setPreserveRatio(true);
            btnAction.setGraphic(notesIcon);
            btnAction.setGraphicTextGap(10);
        } catch (Exception e) {
            System.err.println("Could not load action icon: " + e.getMessage());
        }
        btnAction.setStyle("-fx-background-color: white; -fx-text-fill: #A114AC; -fx-font-family: 'Outfit'; -fx-font-size: 25px; -fx-background-radius: 10px; -fx-padding: 16px 32px; -fx-cursor: hand;");
        
        navLinks.getChildren().addAll(navBeranda, navJurnal, navKalendar, btnAction);

        // Helper to update button
        java.util.function.BiConsumer<String, String> updateActionButton = (text, iconPath) -> {
            btnAction.setText(text);
            try {
                ImageView icon = new ImageView(new Image("file:" + iconPath));
                icon.setFitWidth(32); icon.setFitHeight(32); icon.setPreserveRatio(true);
                btnAction.setGraphic(icon);
            } catch (Exception e) {}
        };

        // Profile Photo
        Circle profileCircle = new Circle(32, 32, 32);
        profileCircle.setStroke(Color.TRANSPARENT);
        profileCircle.setStyle("-fx-cursor: hand;");
        
        Runnable updateNavbarProfile = () -> {
            try {
                String photoPath = user.getProfilePhotoPath();
                if (photoPath == null || photoPath.isEmpty()) {
                    photoPath = "img/dashboard/default_profile_photo.jpg";
                }
                Image profileImg = new Image("file:" + photoPath);
                profileCircle.setFill(new ImagePattern(profileImg));
            } catch (Exception e) {
                profileCircle.setFill(Color.web("#D9D9D9"));
                System.err.println("Could not load profile photo: " + e.getMessage());
            }
        };
        updateNavbarProfile.run(); // Call initially

        profileCircle.setOnMouseClicked(e -> {
            navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            navJurnal.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            searchBox.setVisible(false);
            searchBox.setManaged(false);
            ProfileView profileView = new ProfileView(() -> showLoginScreen(primaryStage));
            root.setCenter(profileView.getView(user));
        });

        // Right Menu Box (Nav Links + Profile Photo)
        HBox menuBox = new HBox(64);
        menuBox.setAlignment(Pos.CENTER_RIGHT);
        menuBox.getChildren().addAll(navLinks, profileCircle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        navBar.getChildren().addAll(logoBox, searchBox, spacer, menuBox);
        root.setTop(navBar);

        // Event Navigation Routing
        navBeranda.setOnMouseClicked(e -> {
            updateNavbarProfile.run();
            navBeranda.setFont(Font.font("Outfit", FontWeight.BOLD, 25));
            navJurnal.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            searchBox.setVisible(false);
            searchBox.setManaged(false);
            updateActionButton.accept("Tulis Jurnal", "img/icons/notes.png");
            DashboardView dashboardView = new DashboardView();
            root.setCenter(dashboardView.getDashboardView(user, root));
        });

        navJurnal.setOnMouseClicked(e -> {
            updateNavbarProfile.run();
            navJurnal.setFont(Font.font("Outfit", FontWeight.BOLD, 25));
            navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            searchBox.setVisible(true);
            searchBox.setManaged(true);
            updateActionButton.accept("Tulis Jurnal", "img/icons/notes.png");
            EntryListView entryListView = new EntryListView(user);
            root.setCenter(entryListView.getView());
        });
        
        navKalendar.setOnMouseClicked(e -> {
            updateNavbarProfile.run();
            navKalendar.setFont(Font.font("Outfit", FontWeight.BOLD, 25));
            navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            navJurnal.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            searchBox.setVisible(false);
            searchBox.setManaged(false);
            updateActionButton.accept("Tambah Target", "img/icons/calendar.png");
            CalendarView calendarView = new CalendarView(user);
            root.setCenter(calendarView.getView());
        });

        btnAction.setOnAction(e -> {
            updateNavbarProfile.run();
            if (btnAction.getText().equals("Tulis Jurnal")) {
                navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
                navJurnal.setFont(Font.font("Outfit", FontWeight.BOLD, 25));
                navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
                searchBox.setVisible(true);
                searchBox.setManaged(true);

                EntryFormView entryFormView = new EntryFormView(user, () -> {
                    navJurnal.setFont(Font.font("Outfit", FontWeight.BOLD, 25));
                    navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
                    navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
                    searchBox.setVisible(true);
                    searchBox.setManaged(true);
                    EntryListView entryListView = new EntryListView(user);
                    root.setCenter(entryListView.getView());
                });
                root.setCenter(entryFormView.getView().getCenter());
            } else {
                com.juki.view.GoalModal goalModal = new com.juki.view.GoalModal(user, LocalDate.now(), () -> {
                    searchBox.setVisible(false);
                    searchBox.setManaged(false);
                    CalendarView calendarView = new CalendarView(user);
                    root.setCenter(calendarView.getView());
                });
                goalModal.show();
            }
        });

        // Panggil View Beranda (Dashboard)
        navBeranda.setFont(Font.font("Outfit", FontWeight.BOLD, 25)); // Set aktif di Beranda
        updateActionButton.accept("Tulis Jurnal", "img/icons/notes.png");
        DashboardView dashboardView = new DashboardView();
        root.setCenter(dashboardView.getDashboardView(user, root));

        Scene scene = new Scene(root, 1600, 900);
        scene.getStylesheets().add("data:text/css,.chart-series-area-fill { -fx-fill: rgba(255, 105, 180, 0.4); } .chart-series-area-line { -fx-stroke: #FF69B4; -fx-stroke-width: 3px; }");
        primaryStage.setTitle("JuKi - App");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}