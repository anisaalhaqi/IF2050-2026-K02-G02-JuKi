package com.juki.view;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * DeleteConfirmationModal refactored to use modern JavaFX best practices
 * inspired by HTML/CSS flexbox structures.
 */
public class DeleteConfirmationModal {
    private final Stage stage;
    private final Runnable onConfirm;

    public DeleteConfirmationModal(Runnable onConfirm) {
        this.onConfirm = onConfirm;
        this.stage = new Stage();
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.initStyle(StageStyle.TRANSPARENT);
        
        initialize();
    }

    private void initialize() {
        // 1. BACKDROP (Fullscreen Overlay)
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.40);");
        backdrop.setPrefSize(1920, 1080); // Ensure it covers typical desktop screens
        backdrop.setOnMouseClicked(e -> {
            // Close if clicking outside the modal card
            if (e.getTarget() == backdrop) {
                stage.close();
            }
        });

        // 2. MODAL CARD (Container - Flex Column)
        VBox card = new VBox(27.83); // Spacing between children
        card.setAlignment(Pos.CENTER);
        
        // Use auto-sizing based on content (Hug Content)
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        // CSS Style using Multiline String
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 34.78px;
            -fx-padding: 55.65px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 24, 0, 0, 4);
        """);

        // 3. ICON SECTION
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(111.30, 111.30);
        iconContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        Rectangle redSquare = new Rectangle(85.80, 85.80);
        redSquare.setFill(Color.web("#DC2626"));
        iconContainer.getChildren().add(redSquare);

        // 4. BODY TEXT
        Label bodyText = new Label("Apakah anda yakin untuk hapus target ini?\nAksi ini tidak dapat dipulihkan");
        bodyText.setWrapText(true);
        bodyText.setTextAlignment(TextAlignment.CENTER);
        bodyText.setTextFill(Color.web("#292929"));
        bodyText.setFont(Font.font("Inter", FontWeight.NORMAL, 29.57));
        bodyText.setMaxWidth(600); // Limit width to ensure wrapping looks good

        // 5. BUTTONS ROW (Flex Row)
        HBox buttonRow = new HBox(40);
        buttonRow.setAlignment(Pos.CENTER);

        // Kembali Button (Outline style)
        Button btnCancel = new Button("Kembali");
        btnCancel.setPrefSize(292.17, 83.48);
        btnCancel.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #DC2626;
            -fx-border-width: 1.74px;
            -fx-border-radius: 13.91px;
            -fx-text-fill: #DC2626;
            -fx-font-family: 'Plus Jakarta Sans';
            -fx-font-size: 27.83px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """);
        btnCancel.setOnAction(e -> stage.close());

        // Hapus Button (Filled style)
        Button btnDelete = new Button("Hapus");
        btnDelete.setPrefSize(292.17, 83.48);
        btnDelete.setStyle("""
            -fx-background-color: #DC2626;
            -fx-background-radius: 13.91px;
            -fx-text-fill: #FAFAFA;
            -fx-font-family: 'Plus Jakarta Sans';
            -fx-font-size: 27.83px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """);
        btnDelete.setOnAction(e -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
            stage.close();
        });

        buttonRow.getChildren().addAll(btnCancel, btnDelete);

        // 6. ASSEMBLE COMPONENTS
        card.getChildren().addAll(iconContainer, bodyText, buttonRow);
        backdrop.getChildren().add(card);

        // 7. SCENE & STAGE SETTINGS
        Scene scene = new Scene(backdrop);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public void show() {
        stage.show();
    }
}