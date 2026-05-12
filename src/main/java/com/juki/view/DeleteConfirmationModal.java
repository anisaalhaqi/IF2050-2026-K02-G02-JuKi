package com.juki.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        VBox root = new VBox(27.83);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 34.78px; -fx-padding: 55.65px;");
        root.setPrefWidth(700); // Approximate based on padding and buttons

        // Icon Section
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(111.30, 111.30);
        iconContainer.setMaxSize(111.30, 111.30);
        
        Rectangle redSquare = new Rectangle(85.80, 85.80);
        redSquare.setFill(Color.web("#DC2626"));
        // Positioned at 12.75px from top/left within 111.30px container
        StackPane.setAlignment(redSquare, Pos.TOP_LEFT);
        redSquare.setTranslateX(12.75);
        redSquare.setTranslateY(12.75);
        
        iconContainer.getChildren().add(redSquare);

        // Body Text
        Label bodyText = new Label("Apakah anda yakin untuk hapus target ini? Aksi ini tidak dapat dipulihkan");
        bodyText.setWrapText(true);
        bodyText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        bodyText.setTextFill(Color.web("#292929"));
        bodyText.setFont(Font.font("Inter", FontWeight.NORMAL, 29.57));
        bodyText.setLineSpacing(38.26 - 29.57); // Adjusting line height
        bodyText.setStyle("-fx-letter-spacing: 0.30px;");
        bodyText.setMaxWidth(600);

        // Buttons Section
        HBox buttonBox = new HBox(40);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnCancel = new Button("Kembali");
        btnCancel.setPrefSize(292.17, 83.48);
        btnCancel.setStyle("-fx-background-color: white; -fx-border-color: #DC2626; -fx-border-width: 1.74px; -fx-border-radius: 13.91px; -fx-text-fill: #DC2626; -fx-font-family: 'Plus Jakarta Sans'; -fx-font-size: 27.83px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> stage.close());

        Button btnDelete = new Button("Hapus");
        btnDelete.setPrefSize(292.17, 83.48);
        btnDelete.setStyle("-fx-background-color: #DC2626; -fx-background-radius: 13.91px; -fx-text-fill: #FAFAFA; -fx-font-family: 'Plus Jakarta Sans'; -fx-font-size: 27.83px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            if (onConfirm != null) onConfirm.run();
            stage.close();
        });

        buttonBox.getChildren().addAll(btnCancel, btnDelete);

        root.getChildren().addAll(iconContainer, bodyText, buttonBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }
}