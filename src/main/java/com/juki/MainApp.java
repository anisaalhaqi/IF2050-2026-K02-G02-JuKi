package com.juki;

import com.juki.controller.RegistrationFormController;
import com.juki.db.DatabaseHelper;
import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.User;
import com.juki.view.DashboardView;
import com.juki.view.CalendarView;
import com.juki.view.EntryDetailView;
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

        Scene scene = new Scene(loginView.getView(), 1920, 1080);
        primaryStage.setTitle("JuKi - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void showMainDashboard(Stage primaryStage, User user) {
        BorderPane root = new BorderPane();
        
        // Top Navigation Bar
        HBox navBar = new HBox();
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
        
        // Tambah Self-Care Button
        Button btnTulis = new Button("Tambah Self-Care");
        try {
            ImageView notesIcon = new ImageView(new Image("file:img/icons/notes.png"));
            notesIcon.setFitWidth(32);
            notesIcon.setFitHeight(32);
            notesIcon.setPreserveRatio(true);
            btnTulis.setGraphic(notesIcon);
            btnTulis.setGraphicTextGap(10);
        } catch (Exception e) {
            System.err.println("Could not load notes icon: " + e.getMessage());
        }
        btnTulis.setStyle("-fx-background-color: white; -fx-text-fill: #A114AC; -fx-font-family: 'Outfit'; -fx-font-size: 25px; -fx-background-radius: 10px; -fx-padding: 16px 32px; -fx-cursor: hand;");
        
        navLinks.getChildren().addAll(navBeranda, navJurnal, navKalendar, btnTulis);

        // Profile Photo using Circle + ImagePattern (More robust than clipping)
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
            ProfileView profileView = new ProfileView(() -> showLoginScreen(primaryStage));
            root.setCenter(profileView.getView(user));
        });

        // Right Menu Box (Nav Links + Profile Photo)
        HBox menuBox = new HBox(64);
        menuBox.setAlignment(Pos.CENTER_RIGHT);
        menuBox.getChildren().addAll(navLinks, profileCircle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        navBar.getChildren().addAll(logoBox, spacer, menuBox);
        root.setTop(navBar);

        // Event Navigation Routing
        navBeranda.setOnMouseClicked(e -> {
            updateNavbarProfile.run();
            navBeranda.setFont(Font.font("Outfit", FontWeight.BOLD, 25));
            navJurnal.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            DashboardView dashboardView = new DashboardView();
            root.setCenter(dashboardView.getDashboardView(user, root));
        });

        navJurnal.setOnMouseClicked(e -> {
            updateNavbarProfile.run();
            navJurnal.setFont(Font.font("Outfit", FontWeight.BOLD, 25));
            navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            showEntryList(root, user);
        });
        
        navKalendar.setOnMouseClicked(e -> {
            updateNavbarProfile.run();
            navKalendar.setFont(Font.font("Outfit", FontWeight.BOLD, 25));
            navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            navJurnal.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            CalendarView calendarView = new CalendarView(user);
            root.setCenter(calendarView.getView());
        });

        btnTulis.setOnAction(e -> {
            updateNavbarProfile.run();
            navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            navJurnal.setFont(Font.font("Outfit", FontWeight.BOLD, 25)); // Set aktif di Jurnal
            navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
            
            EntryFormView entryFormView = new EntryFormView(user, () -> {
                // Aksi saat jurnal berhasil diposting (Kembali ke List)
                navJurnal.setFont(Font.font("Outfit", FontWeight.BOLD, 25));
                navBeranda.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
                navKalendar.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
                showEntryList(root, user);
            });
            root.setCenter(entryFormView.getView().getCenter()); // Mengambil kontennya saja tanpa duplikasi navbar
        });

        // Panggil View Beranda (Dashboard)
        navBeranda.setFont(Font.font("Outfit", FontWeight.BOLD, 25)); // Set aktif di Beranda
        DashboardView dashboardView = new DashboardView();
        root.setCenter(dashboardView.getDashboardView(user, root));

        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add("data:text/css,.chart-series-area-fill { -fx-fill: rgba(255, 105, 180, 0.4); } .chart-series-area-line { -fx-stroke: #FF69B4; -fx-stroke-width: 3px; }");
        primaryStage.setTitle("JuKi - App");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void showEntryList(BorderPane root, User user) {
        EntryListView entryListView = new EntryListView(user, id -> showEntryDetail(root, user, id));
        root.setCenter(entryListView.getView());
    }

    private void showEntryDetail(BorderPane root, User user, int entryId) {
        EntryController entryController = new EntryController();
        JournalEntry entry = entryController.getEntryDetail(entryId);
        if (entry == null) {
            System.err.println("Jurnal tidak ditemukan: " + entryId);
            return;
        }
        EntryDetailView detailView = new EntryDetailView();
        root.setCenter(detailView.getView(entry, user.getFullName(), () -> showEntryList(root, user)));
    }

    public static void main(String[] args) {
        launch(args);
    }
}