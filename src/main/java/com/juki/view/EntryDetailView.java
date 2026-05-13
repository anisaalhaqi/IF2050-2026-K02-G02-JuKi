package com.juki.view;

import com.juki.model.JournalEntry;
import com.juki.model.Photo;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class EntryDetailView {

    private final Label titleLabel = new Label();
    
    private final HBox profileBox = new HBox(12);
    private final Circle profilePhoto = new Circle(20); 
    private final Label authorNameLabel = new Label();
    private final Label dateTimeLabel = new Label();
    private final ImageView dotsIcon = new ImageView();
    
    private final Label categoryLabel = new Label();
    
    private final HBox triggerBox = new HBox(8);
    private final Label triggerTitleLabel = new Label("Penyebab:");
    private final Label triggerValueLabel = new Label();
    
    private final Label descriptionLabel = new Label();
    private final FlowPane photoContainer = new FlowPane();
    private final VBox targetCard = new VBox(14);
    
    private final ImageView headerImageView = new ImageView();
    
    private JournalEntry currentEntry;
    private Consumer<JournalEntry> onEditAction;
    private Runnable onDeleteAction;

    public BorderPane getView(JournalEntry entry) {
        return getView(entry, null, null, null, null);
    }

    public BorderPane getView(JournalEntry entry, Runnable backAction) {
        return getView(entry, null, backAction, null, null);
    }

    public BorderPane getView(JournalEntry entry, String userName, Runnable backAction) {
        return getView(entry, userName, backAction, null, null);
    }

    public BorderPane getView(JournalEntry entry, String userName, Runnable backAction, Consumer<JournalEntry> onEditAction, Runnable onDeleteAction) {
        this.currentEntry = entry;
        this.onEditAction = onEditAction;
        this.onDeleteAction = onDeleteAction;
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        // 1. SETUP TOMBOL BACK
        HBox headerRow = new HBox(24);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setStyle("-fx-background-color: transparent;");

        if (backAction != null) {
            Button backButton = new Button("← Back");
            backButton.setFont(Font.font("Outfit", FontWeight.MEDIUM, 18));
            backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
            backButton.setOnAction(e -> backAction.run());
            headerRow.getChildren().add(backButton);
        }

        // 2. SETUP HEADER IMAGE (ImageView dengan Clip Rounded)
        headerImageView.setFitWidth(1000); 
        headerImageView.setFitHeight(400);
        headerImageView.setSmooth(true);
        
        Rectangle headerClip = new Rectangle(1000, 400);
        headerClip.setArcWidth(40); 
        headerClip.setArcHeight(40);
        headerImageView.setClip(headerClip);
        
        headerImageView.setManaged(false); 
        headerImageView.setVisible(false);

        // 3. MAIN CONTAINER
        VBox mainContainer = new VBox(24);
        mainContainer.setMaxWidth(1000); 
        mainContainer.setPadding(new Insets(40, 20, 80, 20));
        mainContainer.setAlignment(Pos.TOP_LEFT);
        mainContainer.setStyle("-fx-background-color: white;");

        titleLabel.setWrapText(true);
        titleLabel.setFont(Font.font("Outfit", FontWeight.BLACK, 46));
        titleLabel.setTextFill(Color.web("#000000"));

        // --- SETUP PROFIL ---
        profileBox.setAlignment(Pos.CENTER_LEFT);
        authorNameLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 22));
        authorNameLabel.setTextFill(Color.web("#292929"));
        dateTimeLabel.setFont(Font.font("Outfit", FontWeight.LIGHT, 18)); 
        dateTimeLabel.setTextFill(Color.web("#757575"));
        
        try {
            dotsIcon.setImage(new Image("file:img/icons/more.png"));
            dotsIcon.setFitWidth(36);
            dotsIcon.setFitHeight(36);
            dotsIcon.setPreserveRatio(true);
            dotsIcon.setStyle("-fx-cursor: hand;");
            dotsIcon.setOnMouseClicked(e -> showContextMenu(dotsIcon, mainContainer));
        } catch (Exception e) {}
        
        Region profileSpacer = new Region();
        profileSpacer.setPrefWidth(3);
        profileBox.getChildren().addAll(profilePhoto, authorNameLabel, dateTimeLabel, profileSpacer, dotsIcon);

        categoryLabel.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 16));
        categoryLabel.setTextFill(Color.web("#333333"));
        categoryLabel.setStyle("-fx-border-color: #F4C430; -fx-border-radius: 999px; -fx-background-radius: 999px; -fx-padding: 8 20; -fx-background-color: #FFFAC1;");

        // --- SETUP PENYEBAB ---
        triggerBox.setAlignment(Pos.CENTER_LEFT);
        triggerBox.setPadding(new Insets(10, 0, 0, 0));
        triggerTitleLabel.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 18));
        triggerTitleLabel.setTextFill(Color.web("#434343"));
        triggerValueLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 18));
        triggerValueLabel.setTextFill(Color.web("#757575"));
        triggerValueLabel.setWrapText(true);
        triggerBox.getChildren().addAll(triggerTitleLabel, triggerValueLabel);

        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #E0E0E0;");

        descriptionLabel.setWrapText(true);
        descriptionLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 18));
        descriptionLabel.setTextFill(Color.web("#333333"));
        descriptionLabel.setStyle("-fx-line-spacing: 6px;"); 

        photoContainer.setAlignment(Pos.TOP_CENTER);
        photoContainer.setHgap(16);
        photoContainer.setVgap(16);

        targetCard.setPadding(new Insets(24));
        targetCard.setStyle("-fx-background-color: white; -fx-border-color: #E5E5E5; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        Label targetHeader = new Label("Target Hari Ini");
        targetHeader.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 20));
        targetHeader.setTextFill(Color.web("#000000"));
        targetCard.getChildren().add(targetHeader);

        // 4. SUSUN URUTAN ELEMEN DARI ATAS KE BAWAH
        mainContainer.getChildren().addAll(
            headerImageView, 
            headerRow, 
            titleLabel, 
            profileBox,      
            categoryLabel, 
            triggerBox,      
            separator, 
            descriptionLabel, 
            photoContainer, 
            targetCard
        );

        // 5. WRAPPER AGAR KONTEN KURUS BERADA DI TENGAH LAYAR
        VBox centerWrapper = new VBox(mainContainer);
        centerWrapper.setAlignment(Pos.TOP_CENTER); 
        centerWrapper.setStyle("-fx-background-color: white;");

        ScrollPane scrollPane = new ScrollPane(centerWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        loadJournalData(entry, userName);

        root.setCenter(scrollPane);
        return root;
    }

    public void loadJournalData(JournalEntry entry, String userName) {
        String title = entry.getTitle() != null && !entry.getTitle().isBlank() ? entry.getTitle() : "Tanpa Judul";
        titleLabel.setText(title);

        String effectiveUserName = userName != null && !userName.isBlank() ? userName : "Arara";
        authorNameLabel.setText(effectiveUserName);
        
        String dateTime = "";
        if (entry.getDate() != null && entry.getTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH.mm");
            dateTime = entry.getDate().atTime(entry.getTime()).format(formatter);
        }
        dateTimeLabel.setText(dateTime);
        
        try {
            Image profImg = new Image("file:img/dashboard/default_profile_photo.jpg");
            profilePhoto.setFill(new ImagePattern(profImg));
        } catch (Exception e) {
            profilePhoto.setFill(Color.web("#D9D9D9"));
        }

        String category = entry.getCategory() != null && !entry.getCategory().isBlank() ? entry.getCategory() : "Umum";
        categoryLabel.setText(category);

        String triggerValue = "Banyak tubes deadline mepet"; 
        try {
             // Jika kamu punya getTrigger() di JournalEntry:
             // triggerValue = entry.getTrigger() != null ? entry.getTrigger() : "-";
        } catch (Exception e) {}
        triggerValueLabel.setText(triggerValue);

        String description = entry.getDescription() != null && !entry.getDescription().isBlank() ? entry.getDescription() : "Tidak ada deskripsi jurnal.";
        descriptionLabel.setText(description);

        photoContainer.getChildren().clear();
        List<Photo> photos = entry.getPhotos();
        
        if (photos != null && !photos.isEmpty()) {
            photoContainer.setAlignment(Pos.TOP_CENTER);
            // --- 1. SET FOTO HEADER ---
            Photo firstPhoto = photos.get(0);
            try {
                if (firstPhoto != null && firstPhoto.getFilePath() != null) {
                    File headerFile = new File(firstPhoto.getFilePath());
                    if (headerFile.exists()) {
                        Image headerImg = new Image(headerFile.toURI().toString());
                        
                        double imgWidth = headerImg.getWidth();
                        double imgHeight = headerImg.getHeight();
                        double targetWidth = 1000.0;
                        double targetHeight = 400.0;
                        
                        double imgRatio = imgWidth / imgHeight;
                        double targetRatio = targetWidth / targetHeight;
                        
                        double cropWidth, cropHeight, cropX, cropY;
                        
                        if (imgRatio > targetRatio) {
                            cropHeight = imgHeight;
                            cropWidth = cropHeight * targetRatio;
                            cropX = (imgWidth - cropWidth) / 2;
                            cropY = 0;
                        } else {
                            cropWidth = imgWidth;
                            cropHeight = cropWidth / targetRatio;
                            cropX = 0;
                            cropY = (imgHeight - cropHeight) / 2;
                        }
                        
                        headerImageView.setImage(headerImg);
                        headerImageView.setViewport(new Rectangle2D(cropX, cropY, cropWidth, cropHeight));
                        
                        headerImageView.setManaged(true);
                        headerImageView.setVisible(true);
                        
                        headerImageView.setOnMouseClicked(e -> showImageLightbox(headerImg));
                    } else {
                        headerImageView.setManaged(false);
                        headerImageView.setVisible(false);
                    }
                }
            } catch (Exception e) {
                System.err.println("Gagal meload header foto: " + e.getMessage());
                headerImageView.setManaged(false);
                headerImageView.setVisible(false);
            }

            // --- 2. SET FOTO GALERI BAWAH ---
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                if (photo != null && photo.getFilePath() != null) {
                    File file = new File(photo.getFilePath());
                    if (file.exists()) {
                        try {
                            Image image = new Image(file.toURI().toString());
                            ImageView imageView = new ImageView(image);
                            
                            double TARGET_HEIGHT = 220.0;
                            imageView.setFitHeight(TARGET_HEIGHT);
                            imageView.setPreserveRatio(true);
                            imageView.setSmooth(true);
                            imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4); -fx-cursor: hand;");
                            
                            double ratio = image.getWidth() / image.getHeight();
                            double calcWidth = TARGET_HEIGHT * ratio;
                            
                            Rectangle clip = new Rectangle();
                            clip.setArcWidth(15); 
                            clip.setArcHeight(15);
                            clip.setWidth(calcWidth);
                            clip.setHeight(TARGET_HEIGHT);
                            
                            imageView.setClip(clip);
                            imageView.setOnMouseClicked(e -> showImageLightbox(image));
                            
                            photoContainer.getChildren().add(imageView);
                        } catch (Exception e) {}
                    }
                }
            }
        } else {
            headerImageView.setManaged(false);
            headerImageView.setVisible(false);
        }

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
    
    // ==========================================
    // METODE UNTUK MENAMPILKAN FOTO LIGHTBOX
    // ==========================================
    private void showImageLightbox(Image image) {
        // Ambil referensi Window utama agar pop-up bisa menempel sempurna
        Window mainWindow = titleLabel.getScene().getWindow();
        
        Stage lightboxStage = new Stage();
        lightboxStage.initOwner(mainWindow); // Mengikat ke window utama
        lightboxStage.initModality(Modality.APPLICATION_MODAL); 
        lightboxStage.initStyle(StageStyle.TRANSPARENT);

        // Setup Gambar Penuh
        ImageView fullImageView = new ImageView(image);
        fullImageView.setPreserveRatio(true);
        // Ukuran foto dinamis, maksimal 80% dari ukuran layar aplikasi
        fullImageView.setFitWidth(mainWindow.getWidth() * 0.8); 
        fullImageView.setFitHeight(mainWindow.getHeight() * 0.8); 
        
        // Bingkai putih ala Polaroid + Shadow dramatis
        StackPane imageFrame = new StackPane(fullImageView);
        imageFrame.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Teks Bantuan
        Label closeHint = new Label("Klik di mana saja untuk menutup");
        closeHint.setTextFill(Color.web("#DDDDDD"));
        closeHint.setFont(Font.font("Outfit", FontWeight.LIGHT, 16));
        StackPane.setAlignment(closeHint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(closeHint, new Insets(0, 0, 25, 0));

        // Root Latar Belakang Hitam Pudar
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-cursor: hand;");
        root.getChildren().addAll(imageFrame, closeHint);

        // Aksi Menutup (dengan efek Fade Out)
        Runnable closeAction = () -> {
            FadeTransition ftOut = new FadeTransition(Duration.millis(200), root);
            ftOut.setFromValue(1.0);
            ftOut.setToValue(0.0);
            ftOut.setOnFinished(e -> lightboxStage.close());
            ftOut.play();
        };

        root.setOnMouseClicked(e -> closeAction.run());

        // Setup Scene untuk menyamakan ukurannya dengan Main Window
        Scene scene = new Scene(root, mainWindow.getWidth(), mainWindow.getHeight());
        scene.setFill(Color.TRANSPARENT);
        lightboxStage.setScene(scene);
        
        // Posisikan Popup tepat di atas window utama
        lightboxStage.setX(mainWindow.getX());
        lightboxStage.setY(mainWindow.getY());
        
        // Binding: Jika window digeser/diresize saat foto terbuka, pop up akan ikut bergeser!
        mainWindow.xProperty().addListener((obs, oldV, newV) -> lightboxStage.setX(newV.doubleValue()));
        mainWindow.yProperty().addListener((obs, oldV, newV) -> lightboxStage.setY(newV.doubleValue()));
        mainWindow.widthProperty().addListener((obs, oldV, newV) -> lightboxStage.setWidth(newV.doubleValue()));
        mainWindow.heightProperty().addListener((obs, oldV, newV) -> lightboxStage.setHeight(newV.doubleValue()));

        // Animasi Fade-In
        FadeTransition ftIn = new FadeTransition(Duration.millis(300), root);
        ftIn.setFromValue(0.0);
        ftIn.setToValue(1.0);
        ftIn.play();

        lightboxStage.show();
    }
    
    // ==========================================
    // CONTEXT MENU - EDIT/HAPUS JURNAL
    // ==========================================
    private void showContextMenu(ImageView iconView, VBox mainContainer) {
        VBox menuBox = new VBox(0);
        menuBox.setStyle("-fx-background-color: white; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);");
        
        Label editOption = new Label("Edit Jurnal");
        editOption.setFont(Font.font("Outfit", FontWeight.MEDIUM, 14));
        editOption.setTextFill(Color.web("#333333"));
        editOption.setStyle("-fx-padding: 12 16; -fx-cursor: hand;");
        editOption.setOnMouseEntered(e -> editOption.setStyle("-fx-padding: 12 16; -fx-background-color: #F5F5F5; -fx-cursor: hand;"));
        editOption.setOnMouseExited(e -> editOption.setStyle("-fx-padding: 12 16; -fx-background-color: white; -fx-cursor: hand;"));
        editOption.setOnMouseClicked(e -> {
            if (onEditAction != null) {
                onEditAction.accept(currentEntry);
            }
        });
        
        Label deleteOption = new Label("Hapus Jurnal");
        deleteOption.setFont(Font.font("Outfit", FontWeight.MEDIUM, 14));
        deleteOption.setTextFill(Color.web("#DC2626"));
        deleteOption.setStyle("-fx-padding: 12 16; -fx-cursor: hand;");
        deleteOption.setOnMouseEntered(e -> deleteOption.setStyle("-fx-padding: 12 16; -fx-background-color: #FEE2E2; -fx-cursor: hand;"));
        deleteOption.setOnMouseExited(e -> deleteOption.setStyle("-fx-padding: 12 16; -fx-background-color: white; -fx-cursor: hand;"));
        deleteOption.setOnMouseClicked(e -> showDeleteConfirmationModal());
        
        menuBox.getChildren().addAll(editOption, deleteOption);
        
        StackPane popup = new StackPane(menuBox);
        popup.setPrefSize(160, 80);
        popup.setStyle("-fx-background-color: transparent;");
        
        Stage menuStage = new Stage();
        menuStage.initStyle(StageStyle.TRANSPARENT);
        menuStage.setScene(new Scene(popup, 160, 80, Color.TRANSPARENT));
        
        // Posisikan menu di sebelah kanan icon
        Window mainWindow = mainContainer.getScene().getWindow();
        double iconX = iconView.localToScene(0, 0).getX() + mainWindow.getX();
        double iconY = iconView.localToScene(0, 0).getY() + mainWindow.getY();
        
        menuStage.setX(iconX + iconView.getFitWidth() + 10); // Di sebelah kanan dengan jarak 10px
        menuStage.setY(iconY);
        
        // Tutup menu jika klik di luar
        popup.setOnMouseExited(e -> {
            if (e.isStillSincePress()) return;
            menuStage.close();
        });
        
        menuStage.show();
    }
    
    // ==========================================
    // MODAL KONFIRMASI HAPUS JURNAL
    // ==========================================
    private void showDeleteConfirmationModal() {
        Window mainWindow = titleLabel.getScene().getWindow();
        
        Stage modalStage = new Stage();
        modalStage.initOwner(mainWindow);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UTILITY); // Ganti ke UTILITY untuk modal kecil
        
        // Dialog Content
        VBox dialogBox = new VBox(15);
        dialogBox.setStyle("-fx-background-color: white; -fx-background-radius: 15px; -fx-padding: 20;");
        dialogBox.setAlignment(Pos.TOP_CENTER);
        dialogBox.setPrefSize(320, 240); // Ukuran lebih kecil
        
        // Alert Icon di bagian atas tengah
        try {
            ImageView alertIcon = new ImageView(new Image("file:img/icons/alert.png"));
            alertIcon.setFitWidth(40);
            alertIcon.setFitHeight(40);
            alertIcon.setPreserveRatio(true);
            dialogBox.getChildren().add(alertIcon);
        } catch (Exception e) {
            // Fallback
            Circle alertCircle = new Circle(20, Color.web("#F59E0B"));
            Label exclamation = new Label("!");
            exclamation.setTextFill(Color.WHITE);
            exclamation.setFont(Font.font("Outfit", FontWeight.BOLD, 24));
            StackPane alertPane = new StackPane(alertCircle, exclamation);
            dialogBox.getChildren().add(alertPane);
        }
        
        // Spacer kecil
        Region spacer = new Region();
        spacer.setPrefHeight(10);
        dialogBox.getChildren().add(spacer);
        
        // Main Text
        Label mainText = new Label("Apakah anda yakin untuk hapus jurnal ini?");
        mainText.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        mainText.setTextFill(Color.web("#000000"));
        mainText.setWrapText(true);
        mainText.setTextAlignment(TextAlignment.CENTER);
        dialogBox.getChildren().add(mainText);
        
        // Secondary Text
        Label secondaryText = new Label("Aksi ini tidak dapat dipulihkan");
        secondaryText.setFont(Font.font("Outfit", FontWeight.NORMAL, 12));
        secondaryText.setTextFill(Color.web("#757575"));
        secondaryText.setWrapText(true);
        secondaryText.setTextAlignment(TextAlignment.CENTER);
        dialogBox.getChildren().add(secondaryText);
        
        // Spacer
        Region spacer2 = new Region();
        spacer2.setPrefHeight(15);
        dialogBox.getChildren().add(spacer2);
        
        // Button Container
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button backButton = new Button("Batalkan");
        backButton.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 12));
        backButton.setStyle("-fx-background-color: white; -fx-text-fill: #DC2626; -fx-border-color: #DC2626; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        backButton.setOnAction(e -> modalStage.close());
        
        Button deleteButton = new Button("Hapus");
        deleteButton.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 12));
        deleteButton.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> {
            if (onDeleteAction != null) {
                modalStage.close();
                onDeleteAction.run();
            }
        });
        
        buttonBox.getChildren().addAll(backButton, deleteButton);
        dialogBox.getChildren().add(buttonBox);
        
        // Scene tanpa overlay
        Scene scene = new Scene(dialogBox);
        modalStage.setScene(scene);
        modalStage.setResizable(false);
        
        // Posisikan di tengah window utama
        modalStage.setX(mainWindow.getX() + (mainWindow.getWidth() - 320) / 2);
        modalStage.setY(mainWindow.getY() + (mainWindow.getHeight() - 240) / 2);
        
        modalStage.show();
    }
}