package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.controller.GoalController;
import com.juki.controller.ProfileManager;
import com.juki.model.JournalEntry;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ProfileView {
    private Runnable onLogout;
    private VBox profileSection;
    private User currentUser;
    private String tempPhotoPath;

    public ProfileView(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public ScrollPane getView(User user) {
        this.currentUser = user;
        this.tempPhotoPath = user.getProfilePhotoPath();

        EntryController entryController = new EntryController();
        List<JournalEntry> entries = entryController.getAllEntries(user.getId());
        
        VBox content = new VBox(100);
        content.setPadding(new Insets(100));
        content.setStyle("-fx-background-color: white;");
        content.setAlignment(Pos.CENTER);

        // Profile Section + Statistics Section
        HBox profileAndStats = new HBox(146);
        profileAndStats.setAlignment(Pos.CENTER);

        // 1. Profile Section (Left)
        profileSection = new VBox(24);
        profileSection.setAlignment(Pos.CENTER);
        profileSection.setPrefWidth(200);

        renderViewMode();

        // 2. Statistics Section (Right)
        VBox statsSection = new VBox(32);
        statsSection.setAlignment(Pos.TOP_CENTER);
        // statsSection.setPrefWidth(550); // Remove fixed width to allow hug content

        Label statsTitle = new Label("Statistik Ringkasan");
        statsTitle.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        statsTitle.setTextFill(Color.BLACK);

        HBox statsCards = new HBox(24);
        statsCards.setAlignment(Pos.CENTER);

        // Journal Stats Card
        VBox journalCard = createStatCard(String.valueOf(entries.size()), "Jurnal Telah Ditulis");
        
        // Target Stats Card
        GoalController gc = new GoalController();
        List<SelfCareGoal> allGoals = gc.getGoalsByDate(null);
        long totalCompleted = (allGoals != null) ? allGoals.stream().filter(SelfCareGoal::isCompleted).count() : 0;
        
        VBox targetCard = createStatCard(String.valueOf(totalCompleted), "Target Terpenuhi");

        statsCards.getChildren().addAll(journalCard, targetCard);
        statsSection.getChildren().addAll(statsTitle, statsCards);

        profileAndStats.getChildren().addAll(profileSection, statsSection);
        content.getChildren().add(profileAndStats);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: white;");
        
        return scrollPane;
    }

    private void renderViewMode() {
        profileSection.getChildren().clear();

        Circle profileCircle = createProfileCircle(100);

        Label nameLabel = new Label(currentUser.getFullName());
        nameLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 35));
        nameLabel.setTextFill(Color.BLACK);

        Button btnEditProfile = new Button("Edit Profil");
        btnEditProfile.setStyle("-fx-background-color: #FFE341; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-padding: 8px 36px; -fx-background-radius: 12.5px; -fx-cursor: hand;");
        btnEditProfile.setOnAction(e -> renderEditMode());

        Label logoutLabel = new Label("Logout");
        logoutLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 20));
        logoutLabel.setTextFill(Color.RED);
        logoutLabel.setStyle("-fx-cursor: hand; -fx-text-fill: red;");
        logoutLabel.setOnMouseClicked(e -> {
            if (onLogout != null) onLogout.run();
        });

        VBox profileActions = new VBox(8); // 8px gap
        profileActions.setAlignment(Pos.CENTER);
        profileActions.getChildren().addAll(btnEditProfile, logoutLabel);

        profileSection.getChildren().addAll(profileCircle, nameLabel, profileActions);
    }

    private void renderEditMode() {
        profileSection.getChildren().clear();
        tempPhotoPath = currentUser.getProfilePhotoPath();

        Circle profileCircle = createProfileCircle(100);
        profileCircle.setStyle("-fx-cursor: hand;");
        profileCircle.setOnMouseClicked(e -> handlePhotoUpload());
        
        Label hintLabel = new Label("Klik foto untuk ganti");
        hintLabel.setFont(Font.font("Outfit", FontWeight.LIGHT, 14));
        hintLabel.setTextFill(Color.GRAY);

        TextField nameField = new TextField(currentUser.getFullName());
        nameField.setFont(Font.font("Outfit", FontWeight.MEDIUM, 25));
        nameField.setAlignment(Pos.CENTER);
        nameField.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 10px; -fx-padding: 5px;");

        Button btnSave = new Button("Simpan");
        btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-padding: 8px 36px; -fx-background-radius: 10px; -fx-cursor: hand;");
        btnSave.setMinWidth(150);
        btnSave.setOnAction(e -> {
            currentUser.setFullName(nameField.getText());
            currentUser.setProfilePhotoPath(tempPhotoPath);
            ProfileManager pm = new ProfileManager();
            if (pm.updateUser(currentUser)) {
                renderViewMode();
            }
        });

        Button btnCancel = new Button("Batal");
        btnCancel.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D6D6D6; -fx-text-fill: black; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-padding: 8px 36px; -fx-background-radius: 10px; -fx-cursor: hand;");
        btnCancel.setMinWidth(150);
        btnCancel.setOnAction(e -> renderViewMode());

        HBox editActions = new HBox(10, btnSave, btnCancel);
        editActions.setAlignment(Pos.CENTER);

        profileSection.getChildren().addAll(profileCircle, hintLabel, nameField, editActions);
    }

    private Circle createProfileCircle(double radius) {
        Circle circle = new Circle(radius, radius, radius);
        circle.setStroke(Color.TRANSPARENT);
        try {
            String path = tempPhotoPath;
            if (path == null || path.isEmpty()) {
                path = "img/dashboard/default_profile_photo.jpg";
            }
            Image img = new Image("file:" + path);
            circle.setFill(new ImagePattern(img));
        } catch (Exception e) {
            circle.setFill(Color.web("#D9D9D9"));
        }
        return circle;
    }

    private void handlePhotoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(profileSection.getScene().getWindow());
        if (selectedFile != null) {
            try {
                File destDir = new File("img/dashboard");
                if (!destDir.exists()) destDir.mkdirs();
                
                String newFileName = "profile_" + currentUser.getId() + "_" + System.currentTimeMillis() + 
                                     selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                File destFile = new File(destDir, newFileName);
                
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tempPhotoPath = "img/dashboard/" + newFileName;
                
                // Refresh only the circle in edit mode
                ((Circle)profileSection.getChildren().get(0)).setFill(new ImagePattern(new Image("file:" + tempPhotoPath)));
            } catch (Exception e) {
                System.err.println("Error uploading photo: " + e.getMessage());
            }
        }
    }

    private VBox createStatCard(String value, String label) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(32));
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(Region.USE_PREF_SIZE);
        card.setStyle("-fx-border-color: rgba(0, 0, 0, 0.20); -fx-border-radius: 20px; -fx-background-color: white; -fx-background-radius: 20px;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 100));
        valueLabel.setTextFill(Color.web("#A114AC")); // Primary Purple (Same as Navbar)

        Label descLabel = new Label(label);
        descLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 25));
        descLabel.setTextFill(Color.BLACK);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(valueLabel, descLabel);
        return card;
    }
}
