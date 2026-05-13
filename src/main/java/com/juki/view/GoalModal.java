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
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
        stage.initStyle(StageStyle.TRANSPARENT);

        // OUTER CONTAINER (Full Screen Backdrop)
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.40);");
        backdrop.setPrefSize(1920, 1080);
        backdrop.setOnMouseClicked(e -> { if (e.getTarget() == backdrop) stage.close(); });

        // INNER CARD - Dynamic Height
        VBox innerCard = new VBox(24);
        innerCard.setMinWidth(688); innerCard.setMaxWidth(688); innerCard.setPrefWidth(688);
        innerCard.setMaxHeight(Region.USE_PREF_SIZE);
        innerCard.setStyle("-fx-background-color: white; -fx-background-radius: 34.78px; -fx-padding: 55px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 24, 0, 0, 4);");
        innerCard.setAlignment(Pos.TOP_LEFT);

        // HEADER
        Label headerLabel = new Label("Target Self-Care");
        headerLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 29.57));
        headerLabel.setTextFill(Color.web("#292929"));

        // DATE ROW
        HBox dateRow = new HBox(16);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        dateRow.setPadding(new Insets(20, 0, 20, 0));
        Label dateLabel = new Label("Tanggal");
        dateLabel.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
        dateLabel.setTextFill(Color.BLACK);
        Region dateSpacer = new Region(); HBox.setHgrow(dateSpacer, Priority.ALWAYS);
        
        Label dateValue = new Label(selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)));
        dateValue.setFont(Font.font("Outfit", 20));
        
        DatePicker datePicker = new DatePicker(selectedDate);
        datePicker.setManaged(false); datePicker.setVisible(false);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) { setDisable(true); setStyle("-fx-background-color: #eeeeee;"); }
            }
        });
        
        ImageView calendarIcon = new ImageView(new Image("file:img/icons/calendar.png"));
        calendarIcon.setFitWidth(30); calendarIcon.setFitHeight(30); calendarIcon.setPreserveRatio(true);
        calendarIcon.setStyle("-fx-cursor: hand;");
        
        if (!isEditMode) {
            calendarIcon.setOnMouseClicked(e -> datePicker.show());
            datePicker.setOnAction(ev -> {
                selectedDate = datePicker.getValue();
                dateValue.setText(selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)));
                currentGoals = new ArrayList<>(goalService.getGoalsForDate(selectedDate));
                renderGoalList();
            });
        } else { dateValue.setTextFill(Color.web("#767676")); calendarIcon.setOpacity(0.5); }

        dateRow.getChildren().addAll(dateLabel, dateSpacer, dateValue, calendarIcon, datePicker);

        // TARGET LIST SECTION
        VBox listSection = new VBox(10);
        listSection.setPadding(new Insets(20, 0, 20, 0));
        Label listLabel = new Label("Daftar Target");
        listLabel.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
        
        targetListContainer = new VBox(15);
        renderGoalList();

        ScrollPane scrollPane = new ScrollPane(targetListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        HBox addRow = new HBox(12); addRow.setAlignment(Pos.CENTER_LEFT);
        TextField inputField = new TextField(); inputField.setPromptText("Tambah target baru...");
        inputField.setStyle("-fx-background-radius: 10px; -fx-padding: 10px;");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        Button btnAdd = new Button("+"); btnAdd.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 10px; -fx-padding: 8px 16px; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> {
            if (!inputField.getText().trim().isEmpty()) {
                currentGoals.add(new SelfCareGoal(null, inputField.getText().trim(), false, selectedDate, user.getId()));
                inputField.clear(); renderGoalList();
            }
        });
        addRow.getChildren().addAll(inputField, btnAdd);
        listSection.getChildren().addAll(listLabel, scrollPane, addRow);

        // BUTTONS
        HBox buttonRow = new HBox(15); buttonRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancel = new Button("Cancel"); btnCancel.setPrefSize(120, 40);
        btnCancel.setStyle("-fx-background-color: white; -fx-border-color: #74400F; -fx-border-radius: 12.5px; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> stage.close());
        Button btnSave = new Button("Simpan"); btnSave.setPrefSize(120, 40);
        btnSave.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 12.5px; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            goalService.saveGoalsForDate(selectedDate, currentGoals);
            if (onSave != null) onSave.run();
            stage.close();
        });
        buttonRow.getChildren().addAll(btnCancel, btnSave);

        innerCard.getChildren().addAll(headerLabel, dateRow, listSection, buttonRow);
        backdrop.getChildren().add(innerCard); StackPane.setAlignment(innerCard, Pos.CENTER);
        Scene scene = new Scene(backdrop); scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene); stage.show(); stage.centerOnScreen();
    }

    private void renderGoalList() {
        targetListContainer.getChildren().clear();
        if (currentGoals.isEmpty()) {
            Label placeholder = new Label("Tidak ada daftar target");
            placeholder.setFont(Font.font("Arimo Hebrew Subset", FontWeight.NORMAL, FontPosture.ITALIC, 20));
            placeholder.setTextFill(Color.web("rgba(0,0,0,0.20)"));
            targetListContainer.getChildren().add(placeholder);
        } else {
            for (SelfCareGoal g : currentGoals) {
                HBox item = new HBox(12); item.setAlignment(Pos.CENTER_LEFT); item.setPadding(new Insets(5, 0, 5, 0));
                Label lbl = new Label(g.getTitle()); lbl.setFont(Font.font("Outfit", 18)); lbl.setWrapText(true); HBox.setHgrow(lbl, Priority.ALWAYS);
                ImageView del = new ImageView(new Image("file:img/icons/warning.png")); del.setFitWidth(20); del.setPreserveRatio(true); del.setStyle("-fx-cursor: hand;");
                del.setOnMouseClicked(e -> { currentGoals.remove(g); renderGoalList(); });
                item.getChildren().addAll(lbl, del); targetListContainer.getChildren().add(item);
            }
        }
    }

    public void show() { if (!stage.isShowing()) stage.show(); }
}