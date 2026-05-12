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

        // INNER CARD
        VBox innerCard = new VBox(24);
        innerCard.setMinWidth(688); innerCard.setMaxWidth(688); innerCard.setPrefWidth(688);
        innerCard.setStyle("-fx-background-color: white; -fx-background-radius: 34.78px; -fx-padding: 55px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 24, 0, 0, 4);");
        innerCard.setAlignment(Pos.TOP_LEFT);

        // HEADER
        Label headerLabel = new Label("Target Self-Care");
        headerLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 29.57));
        headerLabel.setTextFill(Color.web("#292929"));
        headerLabel.setStyle("-fx-line-spacing: 38.26px; -fx-letter-spacing: 0.30px;");

        // DATE ROW
        VBox dateContainer = new VBox();
        dateContainer.setPadding(new Insets(20, 0, 20, 0));
        HBox dateRow = new HBox();
        dateRow.setAlignment(Pos.CENTER_LEFT);
        Label dateLabel = new Label("Tanggal");
        dateLabel.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
        dateLabel.setTextFill(Color.BLACK);
        Region dateSpacer = new Region(); HBox.setHgrow(dateSpacer, Priority.ALWAYS);
        
        Label dateValue = new Label(selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)));
        dateValue.setFont(Font.font("Outfit", 20));
        
        ImageView calendarIcon = new ImageView(new Image("file:img/icons/calendar.png"));
        calendarIcon.setFitWidth(30); calendarIcon.setFitHeight(30); calendarIcon.setPreserveRatio(true);
        
        dateRow.getChildren().addAll(dateLabel, dateSpacer, dateValue, calendarIcon);
        
        if (!isEditMode) {
            dateRow.setStyle("-fx-cursor: hand;");
            dateRow.setOnMouseClicked(e -> {
                DatePicker dp = new DatePicker(selectedDate);
                final Callback<DatePicker, DateCell> dayCellFactory = d -> new DateCell() {
                    @Override public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) { setDisable(true); setStyle("-fx-background-color: #eeeeee;"); }
                    }
                };
                dp.setDayCellFactory(dayCellFactory); dp.show();
                dp.setOnAction(ev -> {
                    selectedDate = dp.getValue();
                    dateValue.setText(selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)));
                    currentGoals = new ArrayList<>(goalService.getGoalsForDate(selectedDate));
                    renderGoalList();
                });
            });
        } else { dateValue.setTextFill(Color.web("#767676")); }
        dateContainer.getChildren().add(dateRow);

        // TARGET LIST ROW
        VBox listSection = new VBox(10);
        listSection.setPadding(new Insets(20, 0, 20, 0));
        Label listLabel = new Label("Daftar Target");
        listLabel.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
        listLabel.setTextFill(Color.BLACK);
        
        targetListContainer = new VBox(10);
        renderGoalList();
        
        HBox addRow = new HBox(10); addRow.setAlignment(Pos.CENTER_LEFT);
        TextField inputField = new TextField(); inputField.setPromptText("Tambah target baru..."); HBox.setHgrow(inputField, Priority.ALWAYS);
        Button btnAdd = new Button("+"); btnAdd.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> {
            if (!inputField.getText().trim().isEmpty()) {
                currentGoals.add(new SelfCareGoal(null, inputField.getText().trim(), false, selectedDate, user.getId()));
                inputField.clear(); renderGoalList();
            }
        });
        addRow.getChildren().addAll(inputField, btnAdd);
        listSection.getChildren().addAll(listLabel, targetListContainer, addRow);

        // BUTTONS ROW
        HBox buttonRow = new HBox(10); buttonRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancel = new Button("Cancel");
        btnCancel.setPrefWidth(120);
        btnCancel.setStyle("-fx-background-color: white; -fx-border-color: #74400F; -fx-border-width: 1px; -fx-border-radius: 12.50px; -fx-padding: 10px; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 24px; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> stage.close());

        Button btnSave = new Button("Simpan");
        btnSave.setPrefWidth(120);
        btnSave.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 12.50px; -fx-padding: 10px; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 24px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            goalService.saveGoalsForDate(selectedDate, currentGoals);
            if (onSave != null) onSave.run();
            stage.close();
        });

        buttonRow.getChildren().addAll(btnCancel, btnSave);
        innerCard.getChildren().addAll(headerLabel, dateContainer, listSection, buttonRow);
        backdrop.getChildren().add(innerCard); StackPane.setAlignment(innerCard, Pos.CENTER);

        Scene scene = new Scene(backdrop); scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene); stage.centerOnScreen();
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
                HBox item = new HBox(10); item.setAlignment(Pos.CENTER_LEFT);
                Label lbl = new Label(g.getTitle()); lbl.setFont(Font.font("Outfit", 18));
                Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
                ImageView deleteIcon = new ImageView(new Image("file:img/icons/warning.png"));
                deleteIcon.setFitWidth(20); deleteIcon.setFitHeight(20); deleteIcon.setPreserveRatio(true); deleteIcon.setStyle("-fx-cursor: hand;");
                deleteIcon.setOnMouseClicked(e -> { currentGoals.remove(g); renderGoalList(); });
                item.getChildren().addAll(lbl, s, deleteIcon); targetListContainer.getChildren().add(item);
            }
        }
    }

    public void show() { stage.show(); }
}