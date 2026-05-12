package com.juki.view;

import com.juki.model.JournalEntry;
import com.juki.model.Photo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntryDetailView {

    private final Label titleLabel = new Label();
    private final Label profileLabel = new Label();
    private final Label categoryLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final VBox photoContainer = new VBox(16);
    private final VBox targetCard = new VBox(14);

    public BorderPane getView(JournalEntry entry) {
        return getView(entry, null, null);
    }

    public BorderPane getView(JournalEntry entry, Runnable backAction) {
        return getView(entry, null, backAction);
    }

    public BorderPane getView(JournalEntry entry, String userName, Runnable backAction) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        Label pageHeader = new Label("View Journal");
        pageHeader.setFont(Font.font("Outfit", FontWeight.BOLD, 28));
        pageHeader.setTextFill(Color.web("#000000"));

        HBox headerRow = new HBox(24);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(32, 40, 0, 40));
        headerRow.setStyle("-fx-background-color: white;");

        if (backAction != null) {
            Button backButton = new Button("← Back");
            backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-font-size: 16px; -fx-border-color: transparent; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
            backButton.setOnAction(e -> backAction.run());
            headerRow.getChildren().add(backButton);
        }

        headerRow.getChildren().add(pageHeader);

        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20, 40, 40, 40));
        mainContainer.setAlignment(Pos.TOP_LEFT);
        mainContainer.setStyle("-fx-background-color: white;");

        titleLabel.setWrapText(true);
        titleLabel.setFont(Font.font("Outfit", FontWeight.BLACK, 42));
        titleLabel.setTextFill(Color.web("#000000"));
        titleLabel.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        profileLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 14));
        profileLabel.setTextFill(Color.web("#757575"));
        profileLabel.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        categoryLabel.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        categoryLabel.setTextFill(Color.web("#333333"));
        categoryLabel.setStyle("-fx-border-color: #F4C430; -fx-border-radius: 999px; -fx-background-radius: 999px; -fx-padding: 8 18; -fx-background-color: transparent; -fx-cursor: default; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #E0E0E0;");

        photoContainer.setAlignment(Pos.TOP_LEFT);
        photoContainer.setPadding(new Insets(0, 0, 0, 0));
        photoContainer.setPrefWidth(300);

        descriptionLabel.setWrapText(true);
        descriptionLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 18));
        descriptionLabel.setTextFill(Color.web("#333333"));
        descriptionLabel.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        targetCard.setPadding(new Insets(24));
        targetCard.setPrefWidth(300);
        targetCard.setStyle("-fx-background-color: white; -fx-border-color: #E5E5E5; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label targetHeader = new Label("Target Hari Ini");
        targetHeader.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 20));
        targetHeader.setTextFill(Color.web("#000000"));

        targetCard.getChildren().add(targetHeader);

        mainContainer.getChildren().addAll(titleLabel, profileLabel, categoryLabel, separator, photoContainer, descriptionLabel, targetCard);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        loadJournalData(entry, userName);

        root.setTop(headerRow);
        root.setCenter(scrollPane);
        return root;
    }

    public void loadJournalData(JournalEntry entry, String userName) {
        String title = entry.getTitle() != null && !entry.getTitle().isBlank() ? entry.getTitle() : "Tanpa Judul";
        titleLabel.setText(title);

        String effectiveUserName = userName != null && !userName.isBlank() ? userName : "Arara";
        String dateTime = "-";
        if (entry.getDate() != null && entry.getTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH.mm");
            dateTime = entry.getDate().atTime(entry.getTime()).format(formatter);
        }
        profileLabel.setText(effectiveUserName + " • " + dateTime);

        String category = entry.getCategory() != null && !entry.getCategory().isBlank() ? entry.getCategory() : "Umum";
        categoryLabel.setText(category);

        String description = entry.getDescription() != null && !entry.getDescription().isBlank() ? entry.getDescription() : "Tidak ada deskripsi jurnal.";
        descriptionLabel.setText(description);

        photoContainer.getChildren().clear();
        List<Photo> photos = entry.getPhotos();
        System.out.println("\n=== PHOTO LOADING DIAGNOSTIC ===");
        System.out.println("Entry ID: " + entry.getId());
        System.out.println("Photos list: " + (photos == null ? "NULL" : (photos.isEmpty() ? "EMPTY" : "SIZE: " + photos.size())));
        
        if (photos != null && !photos.isEmpty()) {
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                System.out.println("\nPhoto " + (i + 1) + ":");
                System.out.println("  ID: " + photo.getId());
                System.out.println("  FilePath: " + photo.getFilePath());
                
                if (photo != null && photo.getFilePath() != null) {
                    File file = new File(photo.getFilePath());
                    System.out.println("  File exists: " + file.exists());
                    System.out.println("  Absolute path: " + file.getAbsolutePath());
                    System.out.println("  Can read: " + file.canRead());
                    
                    if (file.exists()) {
                        try {
                            String uri = file.toURI().toString();
                            System.out.println("  URI: " + uri);
                            Image image = new Image(uri);
                            System.out.println("  Image loaded successfully. Width: " + image.getWidth() + ", Height: " + image.getHeight());
                            
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(300);
                            imageView.setPreserveRatio(true);
                            imageView.setSmooth(true);
                            imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4);");
                            Rectangle clip = new Rectangle();
                            clip.setArcWidth(10);
                            clip.setArcHeight(10);
                            clip.widthProperty().bind(imageView.fitWidthProperty());
                            clip.heightProperty().bind(imageView.fitHeightProperty());
                            imageView.setClip(clip);
                            photoContainer.getChildren().add(imageView);
                            System.out.println("  ImageView added to container");
                        } catch (Exception e) {
                            System.err.println("  ERROR loading image: " + e.getClass().getName() + " - " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("  ERROR: Photo file does not exist: " + photo.getFilePath());
                    }
                } else {
                    System.out.println("  ERROR: Photo or FilePath is null");
                }
            }
        } else {
            System.out.println("No photos to display");
        }
        System.out.println("photoContainer children count: " + photoContainer.getChildren().size());
        System.out.println("=== END DIAGNOSTIC ===\n");

        targetCard.getChildren().removeIf(node -> node instanceof Label && node != targetCard.getChildren().get(0));
        List<String> targets = parseTargets(entry.getTarget());
        if (targets.isEmpty()) {
            Label emptyLabel = new Label("Tidak ada target saat ini.");
            emptyLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 16));
            emptyLabel.setTextFill(Color.web("#333333"));
            targetCard.getChildren().add(emptyLabel);
        } else {
            for (String target : targets) {
                Label item = new Label("• " + target);
                item.setFont(Font.font("Outfit", FontWeight.NORMAL, 16));
                item.setTextFill(Color.web("#333333"));
                item.setWrapText(true);
                targetCard.getChildren().add(item);
            }
        }
    }

    private List<String> parseTargets(String targetText) {
        if (targetText == null || targetText.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(targetText.split("[,;\\n]+"))
            .map(String::trim)
            .filter(t -> !t.isEmpty())
            .toList();
    }
}
