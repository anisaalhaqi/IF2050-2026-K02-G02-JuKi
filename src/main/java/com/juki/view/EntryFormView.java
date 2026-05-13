package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.User;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

public class EntryFormView {
    private TextField titleField;
    private ComboBox<String> catCombo;
    private TextField causeField;
    private TextArea writeArea;
    private FlowPane selectedImagePane;
    private List<String> selectedPhotoPaths = new ArrayList<>();
    private User user;
    private Runnable onPostSuccess;
    private JournalEntry editingEntry;
    private List<com.juki.model.Photo> editingPhotos = new ArrayList<>();

    public EntryFormView(User user, Runnable onPostSuccess) {
        this.user = user;
        this.onPostSuccess = onPostSuccess;
        this.editingEntry = null;
    }

    public EntryFormView(User user, Runnable onPostSuccess, JournalEntry entryToEdit) {
        this.user = user;
        this.onPostSuccess = onPostSuccess;
        this.editingEntry = entryToEdit;
        if (entryToEdit != null && entryToEdit.getPhotos() != null) {
            this.editingPhotos = new ArrayList<>(entryToEdit.getPhotos());
        }
    }

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #FFFFFF;");

        // ==========================================
        // 1. TOP NAVIGATION BAR (Header)
        // ==========================================
        HBox navBar = new HBox(20);
        navBar.setStyle("-fx-background-color: #8D1395; -fx-padding: 15px 50px;");
        navBar.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("JuKi");
        logo.setTextFill(Color.WHITE);
        logo.setFont(Font.font("System", FontWeight.BOLD, 36));

        TextField searchBar = new TextField();
        searchBar.setPromptText("Cari Jurnal");
        searchBar.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-padding: 8px 15px;");
        searchBar.setPrefWidth(250);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navLinks = new HBox(30);
        navLinks.setAlignment(Pos.CENTER);
        
        Label navBeranda = new Label("Beranda");
        navBeranda.setTextFill(Color.WHITE);
        navBeranda.setFont(Font.font("System", FontWeight.NORMAL, 16));
        
        Label navJurnal = new Label("Jurnal");
        navJurnal.setTextFill(Color.WHITE);
        navJurnal.setFont(Font.font("System", FontWeight.BOLD, 16)); // Penanda aktif
        
        Label navKalender = new Label("Kalender");
        navKalender.setTextFill(Color.WHITE);
        navKalender.setFont(Font.font("System", FontWeight.NORMAL, 16));

        Button btnTulis = new Button("Tulis Jurnal");
        btnTulis.setStyle("-fx-background-color: white; -fx-text-fill: #8D1395; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 10px 25px; -fx-cursor: hand;");

        navLinks.getChildren().addAll(navBeranda, navJurnal, navKalender, btnTulis);
        navBar.getChildren().addAll(logo, searchBar, spacer, navLinks);
        root.setTop(navBar);

        // ==========================================
        // 2. KONTEN UTAMA (Center)
        // ==========================================
        VBox content = new VBox(20);
        content.setStyle("-fx-background-color: #FFFFFF;");
        // Memberikan padding yang besar di kiri-kanan agar berada di tengah
        content.setPadding(new Insets(40, 180, 60, 180)); 

        // Baris 1: Status & Aksi
        HBox row1 = new HBox();
        row1.setAlignment(Pos.CENTER_LEFT);
        
        Label statusLbl = new Label("Draft");
        statusLbl.setTextFill(Color.web("#757575"));
        statusLbl.setFont(Font.font("System", FontWeight.BOLD, 18));

        Region spacerRow1 = new Region();
        HBox.setHgrow(spacerRow1, Priority.ALWAYS);

        Button btnPost = new Button(editingEntry != null ? "Save Changes" : "Post");
        btnPost.setStyle("-fx-background-color: #FFD54F; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 8px 30px; -fx-cursor: hand;");
        btnPost.setOnAction(e -> handlePost());

        // Dummy Profile Picture menggunakan Circle
        Circle profilePic = new Circle(20, Color.web("#E0E0E0")); 

        HBox actionBox = new HBox(15, btnPost, profilePic);
        actionBox.setAlignment(Pos.CENTER);
        row1.getChildren().addAll(statusLbl, spacerRow1, actionBox);

        // Baris 2: Judul
        titleField = new TextField();
        titleField.setPromptText("Judul");
        titleField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #333333; -fx-padding: 10px 0px;");
        titleField.setFont(Font.font("System", FontWeight.BOLD, 40));

        // Baris 3: Kategori
        VBox row3 = new VBox(8);
        Label catLbl = new Label("Kategori");
        catLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        catCombo = new ComboBox<>();
        catCombo.setPromptText("Pilih Kategori");
        catCombo.getItems().addAll("Pekerjaan", "Keluarga", "Pendidikan", "Kesehatan");
        catCombo.setMaxWidth(Double.MAX_VALUE);
        catCombo.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 5px 10px;");
        row3.getChildren().addAll(catLbl, catCombo);

        // Baris 4: Penyebab
        VBox row4 = new VBox(8);
        Label causeLbl = new Label("Penyebab");
        causeLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        causeField = new TextField();
        causeField.setPromptText("Tulis Penyebab");
        causeField.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 20px; -fx-background-radius: 20px; -fx-padding: 12px 15px;");
        row4.getChildren().addAll(causeLbl, causeField);

        // Baris 5: Area Tulis Jurnal
        VBox row5 = new VBox(15);
        row5.setPadding(new Insets(10));
        row5.setStyle(
            "-fx-background-color: #FFFFFF; " +      
            "-fx-border-color: #E0E0E0; " +          
            "-fx-border-width: 1px; " +              
            "-fx-border-radius: 10px; " +            
            "-fx-background-radius: 10px;"
        );
        Button btnImage = new Button("📷");
        btnImage.setShape(new Circle(25));
        btnImage.setMinSize(50, 50);
        btnImage.setMaxSize(50, 50);
        btnImage.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #e6e5e5; -fx-font-size: 20px; -fx-cursor: hand;");
        btnImage.setOnAction(e -> handleUploadImage());

        selectedImagePane = new FlowPane(10, 10);
        selectedImagePane.setPrefWrapLength(280);
        selectedImagePane.setMaxWidth(280);
        selectedImagePane.setStyle("-fx-background-color: transparent;");

        Label imagePreviewLabel = new Label("Pratinjau gambar akan muncul setelah memilih file.");
        imagePreviewLabel.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 12px;");

        VBox imagePreviewBox = new VBox(10, imagePreviewLabel, selectedImagePane);
        imagePreviewBox.setAlignment(Pos.CENTER_LEFT);
        imagePreviewBox.setPrefWidth(280);

        HBox imageBox = new HBox(15, btnImage, imagePreviewBox);
        imageBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(imagePreviewBox, Priority.ALWAYS);

        writeArea = new TextArea();
        String originalPrompt = "Tulis ceritamu hari ini!";
        writeArea.setPromptText(originalPrompt);

        // Tambahkan logika hilangkan prompt saat fokus
        writeArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Jika kotak diklik (fokus), hilangkan prompt text
                writeArea.setPromptText("");
            } else {
                // Jika klik di luar kotak (hilang fokus) dan kosong, munculkan lagi
                if (writeArea.getText().isEmpty()) {
                    writeArea.setPromptText(originalPrompt);
                }
            }
        });
        writeArea.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-border-color: transparent; -fx-text-fill: black; -fx-prompt-text-fill: #9E9E9E; -fx-font-size: 12px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        writeArea.setPrefHeight(200);
        writeArea.setWrapText(true);
        VBox.setVgrow(writeArea, Priority.ALWAYS);
        
        row5.getChildren().addAll(imageBox, writeArea);

        // Susun semua komponen di dalam container
        content.getChildren().addAll(row1, titleField, row3, row4, row5);

        // Bungkus content dengan ScrollPane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Sembunyikan scroll horizontal
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");

        root.setCenter(scrollPane);
        
        // Pre-fill data jika mode edit
        if (editingEntry != null) {
            preFillFormData();
        }

        return root;
    }

    private void preFillFormData() {
        if (editingEntry == null) return;

        titleField.setText(editingEntry.getTitle() != null ? editingEntry.getTitle() : "");
        catCombo.setValue(editingEntry.getCategory() != null ? editingEntry.getCategory() : "Umum");
        causeField.setText(editingEntry.getTrigger() != null ? editingEntry.getTrigger() : "");
        writeArea.setText(editingEntry.getDescription() != null ? editingEntry.getDescription() : "");

        // Load existing photos
        selectedPhotoPaths.clear();
        selectedImagePane.getChildren().clear();

        if (editingPhotos != null && !editingPhotos.isEmpty()) {
            for (com.juki.model.Photo photo : editingPhotos) {
                if (photo != null && photo.getFilePath() != null) {
                    selectedPhotoPaths.add(photo.getFilePath());
                    displayPhotoWithDeleteButton(photo.getFilePath(), photo.getId());
                }
            }
        }
    }

    private void displayPhotoWithDeleteButton(String filePath, Integer photoId) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try {
            Image image = new Image(file.toURI().toString(), 120, 0, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(120);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            // Delete Button (X)
            Circle deleteBtn = new Circle(12);
            deleteBtn.setFill(Color.web("#DC2626"));
            Label deleteIcon = new Label("✕");
            deleteIcon.setTextFill(Color.WHITE);
            deleteIcon.setFont(Font.font("Outfit", FontWeight.BOLD, 14));

            StackPane deleteButton = new StackPane(deleteBtn, deleteIcon);
            deleteButton.setStyle("-fx-cursor: hand;");
            deleteButton.setOnMouseClicked(e -> {
                selectedPhotoPaths.remove(filePath);
                editingPhotos.removeIf(p -> p.getId() != null && p.getId().equals(photoId));
                selectedImagePane.getChildren().remove((StackPane) imageView.getParent());
            });

            StackPane photoContainer = new StackPane(imageView);
            photoContainer.setPrefSize(120, 120);
            photoContainer.setStyle("-fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4);");

            // Position delete button at top-left
            StackPane.setAlignment(deleteButton, Pos.TOP_LEFT);
            StackPane.setMargin(deleteButton, new Insets(4, 0, 0, 4));
            photoContainer.getChildren().add(deleteButton);

            selectedImagePane.getChildren().add(photoContainer);
        } catch (Exception e) {
            System.err.println("Error loading photo: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. DUMMY METHODS (Aksi Tombol)
    // ==========================================
    private void handlePost() {
        System.out.println("Tombol Post ditekan! Menyimpan jurnal ke database...");

        EntryController controller = new EntryController();
        JournalEntry entry;
        
        if (editingEntry != null) {
            // Mode Update
            entry = editingEntry;
            entry.setTitle(titleField.getText());
            entry.setCategory(catCombo.getValue() != null ? catCombo.getValue() : "Umum");
            entry.setTrigger(causeField.getText());
            entry.setDescription(writeArea.getText());

            // Combine new photos with existing ones
            List<com.juki.model.Photo> allPhotos = new ArrayList<>(editingPhotos);
            for (String path : selectedPhotoPaths) {
                if (!editingPhotos.stream().anyMatch(p -> p.getFilePath().equals(path))) {
                    allPhotos.add(new com.juki.model.Photo(null, path));
                }
            }
            entry.setPhotos(allPhotos);

            controller.updateEntryWithPhotos(entry, allPhotos);
        } else {
            // Mode Create
            entry = new JournalEntry();
            entry.setTitle(titleField.getText());
            entry.setCategory(catCombo.getValue() != null ? catCombo.getValue() : "Umum");
            entry.setTrigger(causeField.getText());
            entry.setDescription(writeArea.getText());
            entry.setDate(LocalDate.now());
            entry.setTime(LocalTime.now());
            entry.setUserId(user.getId());

            if (!selectedPhotoPaths.isEmpty()) {
                List<com.juki.model.Photo> photos = new ArrayList<>();
                for (String path : selectedPhotoPaths) {
                    photos.add(new com.juki.model.Photo(null, path));
                }
                entry.setPhotos(photos);
            }

            controller.saveJournal(entry);
        }

        if (onPostSuccess != null) {
            onPostSuccess.run(); // Alihkan layar via callback
        }
    }

    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar Jurnal");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(writeArea.getScene() != null ? writeArea.getScene().getWindow() : null);
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return;
        }

        for (File selectedFile : selectedFiles) {
            String absolutePath = selectedFile.getAbsolutePath();
            selectedPhotoPaths.add(absolutePath);
            displayNewPhotoWithDeleteButton(absolutePath);
        }
    }

    private void displayNewPhotoWithDeleteButton(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try {
            Image image = new Image(file.toURI().toString(), 120, 0, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(120);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            // Delete Button (X)
            Circle deleteBtn = new Circle(12);
            deleteBtn.setFill(Color.web("#DC2626"));
            Label deleteIcon = new Label("✕");
            deleteIcon.setTextFill(Color.WHITE);
            deleteIcon.setFont(Font.font("Outfit", FontWeight.BOLD, 14));

            StackPane deleteButton = new StackPane(deleteBtn, deleteIcon);
            deleteButton.setStyle("-fx-cursor: hand;");
            deleteButton.setOnMouseClicked(e -> {
                selectedPhotoPaths.remove(filePath);
                selectedImagePane.getChildren().remove((StackPane) imageView.getParent());
            });

            StackPane photoContainer = new StackPane(imageView);
            photoContainer.setPrefSize(120, 120);
            photoContainer.setStyle("-fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4);");

            // Position delete button at top-left
            StackPane.setAlignment(deleteButton, Pos.TOP_LEFT);
            StackPane.setMargin(deleteButton, new Insets(4, 0, 0, 4));
            photoContainer.getChildren().add(deleteButton);

            selectedImagePane.getChildren().add(photoContainer);
        } catch (Exception e) {
            System.err.println("Error displaying photo: " + e.getMessage());
        }
    }

    private void handleAddTarget() {
        System.out.println("Memunculkan modal untuk menambahkan target baru...");
    }
}