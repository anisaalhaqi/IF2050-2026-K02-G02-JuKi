package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.controller.GoalController;
import com.juki.model.JournalEntry;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class ProfileView {
    private Runnable onLogout;

    public ProfileView(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public ScrollPane getView(User user) {
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
        VBox profileSection = new VBox(24);
        profileSection.setAlignment(Pos.CENTER);
        profileSection.setPrefWidth(200);

        // Profile Photo using Circle + ImagePattern (More robust than clipping)
        Circle profileCircle = new Circle(100, 100, 100);
        profileCircle.setStroke(Color.TRANSPARENT);
        try {
            String photoPath = user.getProfilePhotoPath();
            if (photoPath == null || photoPath.isEmpty()) {
                photoPath = "img/dashboard/default_profile_photo.jpg";
            }
            Image profileImg = new Image("file:" + photoPath);
            profileCircle.setFill(new ImagePattern(profileImg));
        } catch (Exception e) {
            profileCircle.setFill(Color.web("#D9D9D9"));
            System.err.println("Could not load big profile photo: " + e.getMessage());
        }

        Label nameLabel = new Label(user.getFullName());
        nameLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 35));
        nameLabel.setTextFill(Color.BLACK);

        Button btnEditProfile = new Button("Edit Profil");
        btnEditProfile.setStyle("-fx-background-color: #FFE341; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-padding: 8px 36px; -fx-background-radius: 12.5px; -fx-cursor: hand;");

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

    private VBox createStatCard(String value, String label) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(32));
        card.setAlignment(Pos.CENTER);
        // card.setPrefSize(250, 250); // Remove fixed size for hug content
        card.setMinWidth(Region.USE_PREF_SIZE);
        card.setStyle("-fx-border-color: rgba(0, 0, 0, 0.20); -fx-border-radius: 20px; -fx-background-color: white; -fx-background-radius: 20px;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 100));
        valueLabel.setTextFill(Color.web("#A114AC")); // Primary Purple (Same as Navbar)

        Label descLabel = new Label(label);
        descLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 25));
        descLabel.setTextFill(Color.BLACK);
        // descLabel.setWrapText(true); // Disable wrap for hug content
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(valueLabel, descLabel);
        return card;
    }
}
