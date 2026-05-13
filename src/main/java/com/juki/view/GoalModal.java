package com.juki.view;

import com.juki.controller.GoalController;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import com.juki.service.GoalService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoalModal {
    private Stage stage;
    private User user;
    private LocalDate selectedDate;
    private Runnable onSave;
    private final GoalService goalService = GoalService.getInstance();
    private VBox targetListContainer;
    private List<SelfCareGoal> currentGoals;
    private boolean isEditMode;

    public GoalModal(User user, LocalDate date, Runnable onSave) {
        this(user, date, onSave, false);
    }

    public GoalModal(User user, LocalDate date, Runnable onSave, boolean isEditMode) {
        this.user = user;
        this.selectedDate = date != null ? date : LocalDate.now();
        this.onSave = onSave;
        this.isEditMode = isEditMode;
        this.currentGoals = new ArrayList<>(goalService.getGoalsForDate(this.selectedDate));
        
        initialize();
    }

    private void initialize() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox modalRoot = new VBox(30);
        modalRoot.setPadding(new Insets(40));
        modalRoot.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-width: 1.4; -fx-border-radius: 28; -fx-background-radius: 28;");
        modalRoot.setPrefWidth(600);

        // HEADER WITH CLOSE BUTTON
        HBox headerRow = new HBox(20);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label headerLabel = new Label("Target Self-Care");
        headerLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 24));
        headerLabel.setTextFill(Color.web("#292929"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #292929; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 5px 10px;");
        closeButton.setOnAction(e -> stage.close());

        headerRow.getChildren().addAll(headerLabel, spacer, closeButton);

        // DATE SECTION
        VBox dateContainer = new VBox(10);
        Label dateLabel = new Label("Tanggal");
        dateLabel.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        dateLabel.setTextFill(Color.web("#292929"));

        HBox dateRow = new HBox(10);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        
        Label dateValue = new Label(selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id"))));
        dateValue.setFont(Font.font("Outfit", FontWeight.NORMAL, 14));
        dateValue.setTextFill(Color.web("#434343"));
        
        ImageView calendarIcon = new ImageView(new Image("file:img/icons/calendar.png"));
        calendarIcon.setFitWidth(18);
        calendarIcon.setFitHeight(18);
        calendarIcon.setPreserveRatio(true);
        calendarIcon.setStyle("-fx-cursor: hand;");

        Region dateRowSpacer = new Region();
        HBox.setHgrow(dateRowSpacer, Priority.ALWAYS);

        dateRow.getChildren().addAll(dateValue, dateRowSpacer, calendarIcon);
        dateRow.setStyle("-fx-cursor: hand;");
        
        if (!isEditMode) {
            dateRow.setOnMouseClicked(e -> {
                DatePicker dp = new DatePicker(selectedDate);
                final Callback<DatePicker, DateCell> dayCellFactory = d -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #eeeeee;");
                        }
                    }
                };
                dp.setDayCellFactory(dayCellFactory);
                dp.show();
                dp.setOnAction(ev -> {
                    selectedDate = dp.getValue();
                    dateValue.setText(selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id"))));
                    currentGoals = new ArrayList<>(goalService.getGoalsForDate(selectedDate));
                    renderGoalList();
                });
            });
        } else {
            dateValue.setTextFill(Color.web("#767676"));
        }
        dateContainer.getChildren().addAll(dateLabel, dateRow);

        // TARGET LIST SECTION
        VBox listSection = new VBox(10);
        Label listLabel = new Label("Daftar Target");
        listLabel.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        listLabel.setTextFill(Color.web("#292929"));

        targetListContainer = new VBox(8);
        renderGoalList();

        HBox addRow = new HBox(8);
        addRow.setAlignment(Pos.CENTER_LEFT);
        TextField inputField = new TextField();
        inputField.setPromptText("Tambah target baru...");
        inputField.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8px 12px; -fx-border-color: #D6D6D6; -fx-border-radius: 8; -fx-border-width: 1; -fx-font-size: 12px;");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        
        Button btnAdd = new Button("+");
        btnAdd.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 8; -fx-padding: 8px 12px; -fx-cursor: hand; -fx-text-fill: #74400F; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnAdd.setOnAction(e -> {
            if (!inputField.getText().trim().isEmpty()) {
                currentGoals.add(new SelfCareGoal(null, inputField.getText().trim(), false, selectedDate, user.getId()));
                inputField.clear();
                renderGoalList();
            }
        });
        addRow.getChildren().addAll(inputField, btnAdd);
        listSection.getChildren().addAll(listLabel, targetListContainer, addRow);

        // ACTION BUTTONS ROW
        HBox buttonRow = new HBox(12);
        buttonRow.setAlignment(Pos.CENTER);

        Button btnCancel = new Button("Batal");
        btnCancel.setPrefWidth(130);
        btnCancel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #292929; -fx-font-family: 'Outfit'; -fx-font-size: 14px; -fx-background-radius: 12.5px; -fx-padding: 12px 24px; -fx-cursor: hand; -fx-border-color: #D6D6D6; -fx-border-width: 1px; -fx-border-radius: 12.5px;");
        btnCancel.setOnAction(e -> stage.close());

        Button btnSave = new Button("Simpan");
        btnSave.setPrefWidth(130);
        btnSave.setStyle("-fx-background-color: #FFE341; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 14px; -fx-background-radius: 12.5px; -fx-padding: 12px 24px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            goalService.saveGoalsForDate(selectedDate, currentGoals);
            if (onSave != null) onSave.run();
            stage.close();
        });

        buttonRow.getChildren().addAll(btnCancel, btnSave);
        modalRoot.getChildren().addAll(headerRow, dateContainer, listSection, buttonRow);

        Scene scene = new Scene(modalRoot);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private void renderGoalList() {
        targetListContainer.getChildren().clear();
        if (currentGoals.isEmpty()) {
            Label placeholder = new Label("Tidak ada daftar target");
            placeholder.setFont(Font.font("Outfit", FontWeight.NORMAL, 13));
            placeholder.setTextFill(Color.web("rgba(0,0,0,0.30)"));
            targetListContainer.getChildren().add(placeholder);
        } else {
            for (SelfCareGoal g : currentGoals) {
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setStyle("-fx-padding: 8px; -fx-background-color: #FAE7FF; -fx-background-radius: 8; -fx-border-color: #E8D5F2; -fx-border-width: 1; -fx-border-radius: 8;");
                
                Label lbl = new Label(g.getTitle());
                lbl.setFont(Font.font("Outfit", FontWeight.NORMAL, 13));
                lbl.setTextFill(Color.web("#434343"));
                lbl.setWrapText(true);
                lbl.setMaxWidth(450);
                
                Region s = new Region();
                HBox.setHgrow(s, Priority.ALWAYS);
                
                ImageView deleteIcon = new ImageView(new Image("file:img/icons/warning.png"));
                deleteIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16);
                deleteIcon.setPreserveRatio(true);
                deleteIcon.setStyle("-fx-cursor: hand;");
                deleteIcon.setOnMouseClicked(e -> {
                    currentGoals.remove(g);
                    renderGoalList();
                });
                
                item.getChildren().addAll(lbl, s, deleteIcon);
                targetListContainer.getChildren().add(item);
            }
        }
    }

    public void show() { stage.show(); }
}