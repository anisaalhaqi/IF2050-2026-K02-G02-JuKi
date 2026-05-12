package com.juki.view;

import com.juki.controller.GoalController;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GoalModal {
    private Stage stage;
    private User user;
    private LocalDate selectedDate;
    private Runnable onSave;
    private GoalController goalController;
    private VBox targetListContainer;
    private List<SelfCareGoal> currentGoals;

    public GoalModal(User user, LocalDate date, Runnable onSave) {
        this.user = user;
        this.selectedDate = date;
        this.onSave = onSave;
        this.goalController = new GoalController();
        this.currentGoals = new ArrayList<>(goalController.getGoalsByDate(date));
        
        initialize();
    }

    private void initialize() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Tambah Target Self-Care");

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-background-radius: 20px;");
        root.setPrefWidth(500);

        Label titleLabel = new Label("Tambah Target Self-Care");
        titleLabel.setFont(Font.font("Outfit", FontWeight.BOLD, 24));

        // Date Picker Section
        VBox dateSection = new VBox(8);
        Label dateLabel = new Label("Tanggal");
        dateLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 16));
        DatePicker datePicker = new DatePicker(selectedDate);
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setOnAction(e -> {
            selectedDate = datePicker.getValue();
            refreshGoals();
        });
        dateSection.getChildren().addAll(dateLabel, datePicker);

        // Target List Section
        VBox listSection = new VBox(10);
        Label listLabel = new Label("Daftar Target");
        listLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 16));
        targetListContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(targetListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white;");
        
        // Add Input Section
        HBox addSection = new HBox(10);
        TextField targetInput = new TextField();
        targetInput.setPromptText("Tambah target baru...");
        HBox.setHgrow(targetInput, Priority.ALWAYS);
        Button btnAdd = new Button("+");
        btnAdd.setOnAction(e -> {
            if (!targetInput.getText().isEmpty()) {
                SelfCareGoal newGoal = new SelfCareGoal(null, targetInput.getText(), false, selectedDate);
                currentGoals.add(newGoal);
                targetInput.clear();
                renderGoalList();
            }
        });
        addSection.getChildren().addAll(targetInput, btnAdd);
        listSection.getChildren().addAll(listLabel, scrollPane, addSection);

        // Buttons Section
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnCancel = new Button("Batal");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-border-color: #D6D6D6; -fx-border-radius: 10; -fx-padding: 10 30;");
        btnCancel.setOnAction(e -> stage.close());

        Button btnSave = new Button("Simpan");
        btnSave.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 10; -fx-padding: 10 30; -fx-font-weight: bold;");
        btnSave.setOnAction(e -> {
            goalController.saveGoalsForDate(selectedDate, currentGoals);
            if (onSave != null) onSave.run();
            stage.close();
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);

        root.getChildren().addAll(titleLabel, dateSection, listSection, buttonBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        renderGoalList();
    }

    private void refreshGoals() {
        currentGoals = new ArrayList<>(goalController.getGoalsByDate(selectedDate));
        renderGoalList();
    }

    private void renderGoalList() {
        targetListContainer.getChildren().clear();
        for (SelfCareGoal goal : currentGoals) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(goal.getTitle());
            HBox.setHgrow(lbl, Priority.ALWAYS);
            Button btnDelete = new Button("X");
            btnDelete.setOnAction(e -> {
                currentGoals.remove(goal);
                renderGoalList();
            });
            item.getChildren().addAll(lbl, btnDelete);
            targetListContainer.getChildren().add(item);
        }
    }

    public void show() {
        stage.show();
    }
}