package com.juki.view;

import com.juki.controller.GoalController;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import com.juki.service.GoalService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
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
import javafx.util.Duration;

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
    private StackPane backdrop;

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
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX()); stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth()); stage.setHeight(screenBounds.getHeight());

        backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);"); 
        backdrop.setOnMouseClicked(e -> { if (e.getTarget() == backdrop) stage.close(); });

        // INNER CARD - Compact & Dynamic Height
        VBox innerCard = new VBox(20);
        innerCard.getStyleClass().add("modal-card");
        innerCard.setStyle("-fx-background-color: white; -fx-background-radius: 24px; -fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 20, 0, 0, 4);");
        innerCard.setPrefWidth(Region.USE_COMPUTED_SIZE); innerCard.setMinWidth(350); innerCard.setMaxWidth(480);
        innerCard.setAlignment(Pos.TOP_LEFT);
        innerCard.setFillWidth(true);
        innerCard.setMaxHeight(Region.USE_PREF_SIZE); // DYNAMIC HEIGHT

        // HEADER
        Label headerLabel = new Label("Target Self-Care");
        headerLabel.getStyleClass().add("text-dark");
        headerLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 22));

        // DATE ROW
        HBox dateRow = new HBox(12);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        dateRow.setPadding(new Insets(5, 0, 5, 0));
        Label dateLabel = new Label("Tanggal");
        dateLabel.setFont(Font.font("Outfit", FontWeight.LIGHT, 16));
        dateLabel.getStyleClass().add("text-dark");
        Region dateSpacer = new Region(); HBox.setHgrow(dateSpacer, Priority.ALWAYS);
        
        Label dateValue = new Label(selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)));
        dateValue.setFont(Font.font("Outfit", 16));
        dateValue.getStyleClass().add("text-dark");

        ImageView calendarIcon = new ImageView(new Image("file:img/icons/calendar.png"));
        calendarIcon.setFitWidth(20); calendarIcon.setFitHeight(20); calendarIcon.setPreserveRatio(true);
        calendarIcon.setStyle("-fx-cursor: hand;");
        
        if (!isEditMode) {
            calendarIcon.setOnMouseClicked(e -> showCustomDatePicker(calendarIcon, dateValue));
        } else { 
            dateValue.setStyle("-fx-text-fill: #767676;"); 
            calendarIcon.setOpacity(0.5); 
        }

        dateRow.getChildren().addAll(dateLabel, dateSpacer, dateValue, calendarIcon);

        // TARGET LIST SECTION
        VBox listSection = new VBox(8);
        listSection.setPadding(new Insets(10, 0, 10, 0));
        Label listLabel = new Label("Daftar Target");
        listLabel.setFont(Font.font("Outfit", FontWeight.LIGHT, 16));
        listLabel.getStyleClass().add("text-dark");
        
        targetListContainer = new VBox(10);
        targetListContainer.setFillWidth(true);
        renderGoalList();

        ScrollPane scrollPane = new ScrollPane(targetListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(Region.USE_COMPUTED_SIZE);
        scrollPane.setMaxHeight(250);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        
        HBox addRow = new HBox(10); addRow.setAlignment(Pos.CENTER_LEFT);
        TextField inputField = new TextField(); inputField.setPromptText("Tambah target baru...");
        inputField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px; -fx-font-family: 'Outfit'; -fx-font-size: 14px;");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        Button btnAdd = new Button("+"); btnAdd.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 8px; -fx-padding: 6px 12px; -fx-cursor: hand; -fx-font-weight: bold;");
        btnAdd.setOnAction(e -> {
            if (!inputField.getText().trim().isEmpty()) {
                currentGoals.add(new SelfCareGoal(null, inputField.getText().trim(), false, selectedDate, user.getId()));
                inputField.clear(); renderGoalList();
            }
        });
        addRow.getChildren().addAll(inputField, btnAdd);
        listSection.getChildren().addAll(listLabel, scrollPane, addRow);

        // BUTTONS
        HBox buttonRow = new HBox(12); buttonRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancel = new Button("Cancel"); 
        btnCancel.setStyle("-fx-background-color: white; -fx-border-color: #74400F; -fx-border-radius: 8px; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 8 20;");
        btnCancel.setOnAction(e -> stage.close());
        Button btnSave = new Button("Simpan"); 
        btnSave.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 8px; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        btnSave.setOnAction(e -> {
            goalService.saveGoalsForDate(selectedDate, currentGoals);
            if (onSave != null) onSave.run();
            stage.close();
        });
        buttonRow.getChildren().addAll(btnCancel, btnSave);

        innerCard.getChildren().addAll(headerLabel, dateRow, listSection, buttonRow);
        backdrop.getChildren().add(innerCard); StackPane.setAlignment(innerCard, Pos.CENTER);
        
        Scene scene = new Scene(backdrop); scene.setFill(Color.TRANSPARENT);
        try { scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); } catch (Exception e) {}
        stage.setScene(scene); stage.show();
    }

    private void renderGoalList() {
        targetListContainer.getChildren().clear();
        if (currentGoals.isEmpty()) {
            Label placeholder = new Label("Tidak ada daftar target");
            placeholder.setFont(Font.font("Outfit", FontWeight.NORMAL, FontPosture.ITALIC, 14));
            placeholder.setTextFill(Color.web("rgba(0,0,0,0.40)"));
            targetListContainer.getChildren().add(placeholder);
        } else {
            for (SelfCareGoal g : currentGoals) {
                HBox item = new HBox(10); item.setAlignment(Pos.CENTER_LEFT); item.setPadding(new Insets(4, 0, 4, 0));
                Label lbl = new Label(g.getTitle()); 
                lbl.getStyleClass().add("target-label"); lbl.setFont(Font.font("Outfit", 16));
                if (g.isCompleted()) lbl.getStyleClass().add("target-label-completed");
                lbl.setWrapText(true); HBox.setHgrow(lbl, Priority.ALWAYS);
                
                StackPane moreBtn = new StackPane();
                moreBtn.getStyleClass().add("more-menu-button");
                ImageView moreIcon = new ImageView(new Image("file:img/icons/more.png"));
                moreIcon.setFitWidth(18); moreIcon.setPreserveRatio(true);
                moreBtn.getChildren().add(moreIcon);
                moreBtn.setOnMouseClicked(e -> showMoreMenu(moreBtn, g));
                
                item.getChildren().addAll(lbl, moreBtn); targetListContainer.getChildren().add(item);
            }
        }
    }

    private void showMoreMenu(StackPane source, SelfCareGoal goal) {
        VBox menuPopup = new VBox();
        menuPopup.getStyleClass().add("dropdown-menu");
        menuPopup.setMinWidth(150); menuPopup.setMaxWidth(Region.USE_PREF_SIZE);
        menuPopup.setMaxHeight(Region.USE_PREF_SIZE); // DYNAMIC HEIGHT
        
        Label editItem = new Label("Edit Target");
        editItem.getStyleClass().add("dropdown-item"); editItem.setMaxWidth(Double.MAX_VALUE);
        editItem.setOnMouseClicked(e -> { backdrop.getChildren().remove(menuPopup); showEditDialog(goal); });
        
        Label deleteItem = new Label("Hapus Target");
        deleteItem.getStyleClass().addAll("dropdown-item", "more-item-delete"); deleteItem.setMaxWidth(Double.MAX_VALUE);
        deleteItem.setOnMouseClicked(e -> { backdrop.getChildren().remove(menuPopup); showDeleteConfirmation(goal); });
        
        menuPopup.getChildren().addAll(editItem, deleteItem);
        javafx.geometry.Bounds bounds = source.localToScene(source.getBoundsInLocal());
        menuPopup.setTranslateX(bounds.getMinX() - 130); menuPopup.setTranslateY(bounds.getMinY() + 25);
        
        backdrop.getChildren().add(menuPopup);
        StackPane.setAlignment(menuPopup, Pos.TOP_LEFT);
        backdrop.setOnMousePressed(e -> {
            if (!menuPopup.getBoundsInParent().contains(e.getX(), e.getY())) {
                backdrop.getChildren().remove(menuPopup); backdrop.setOnMousePressed(null); 
            }
        });
    }

    private void showDeleteConfirmation(SelfCareGoal goal) {
        Stage confirmStage = new Stage(); confirmStage.initModality(Modality.APPLICATION_MODAL); confirmStage.initStyle(StageStyle.TRANSPARENT);
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        confirmStage.setX(screenBounds.getMinX()); confirmStage.setY(screenBounds.getMinY());
        confirmStage.setWidth(screenBounds.getWidth()); confirmStage.setHeight(screenBounds.getHeight());

        StackPane confirmBackdrop = new StackPane(); confirmBackdrop.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        confirmBackdrop.setOnMouseClicked(e -> { if (e.getTarget() == confirmBackdrop) confirmStage.close(); });

        VBox card = new VBox(20); card.getStyleClass().add("delete-modal");
        card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 20, 0, 0, 4);");
        card.setMinWidth(350); card.setMaxWidth(400); card.setAlignment(Pos.CENTER);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        ImageView warningIcon = new ImageView(new Image("file:img/icons/alert.png"));
        warningIcon.setFitWidth(64); warningIcon.setPreserveRatio(true);
        
        Label title = new Label("Hapus target ini?");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 20)); title.setTextFill(Color.web("#292929"));

        Label msg = new Label("Aksi ini tidak dapat dipulihkan.");
        msg.setFont(Font.font("Outfit", 16)); msg.setTextFill(Color.web("#767676"));
        
        HBox buttons = new HBox(15); buttons.setAlignment(Pos.CENTER);
        Button btnBack = new Button("Batal"); 
        btnBack.setStyle("-fx-background-color: white; -fx-border-color: #767676; -fx-border-radius: 10px; -fx-text-fill: #767676; -fx-font-family: 'Outfit'; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 8 20;");
        btnBack.setOnAction(e -> confirmStage.close());
        
        Button btnDelete = new Button("Hapus"); 
        btnDelete.setStyle("-fx-background-color: #DC2626; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-family: 'Plus Jakarta Sans'; -fx-font-weight: 600; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 8 20;");
        btnDelete.setOnAction(e -> { currentGoals.remove(goal); renderGoalList(); confirmStage.close(); });
        
        buttons.getChildren().addAll(btnBack, btnDelete);
        card.getChildren().addAll(title, msg, buttons); confirmBackdrop.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
        
        Scene scene = new Scene(confirmBackdrop); scene.setFill(Color.TRANSPARENT);
        try { scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); } catch (Exception e) {}
        confirmStage.setScene(scene); confirmStage.show();
    }

    private void showEditDialog(SelfCareGoal goal) {
        Stage editStage = new Stage(); editStage.initModality(Modality.APPLICATION_MODAL); editStage.initStyle(StageStyle.TRANSPARENT);
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        editStage.setX(screenBounds.getMinX()); editStage.setY(screenBounds.getMinY());
        editStage.setWidth(screenBounds.getWidth()); editStage.setHeight(screenBounds.getHeight());

        StackPane editBackdrop = new StackPane(); 
        editBackdrop.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        editBackdrop.setOnMouseClicked(e -> { if (e.getTarget() == editBackdrop) editStage.close(); });

        VBox card = new VBox(20); card.getStyleClass().add("modal-card");
        card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 20, 0, 0, 4);");
        card.setMinWidth(350); card.setMaxWidth(450); card.setAlignment(Pos.CENTER);
        card.setMaxHeight(Region.USE_PREF_SIZE); // DYNAMIC HEIGHT
        
        Label title = new Label("Ubah Target"); title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web("#292929"));

        TextField input = new TextField(goal.getTitle()); input.setPrefWidth(300);
        input.setStyle("-fx-background-radius: 10px; -fx-padding: 10px; -fx-font-family: 'Outfit'; -fx-font-size: 14px; -fx-border-color: #D6D6D6; -fx-border-radius: 10px;");
        
        HBox btns = new HBox(12); btns.setAlignment(Pos.CENTER_RIGHT);
        Button btnB = new Button("Batal"); 
        btnB.setStyle("-fx-background-color: white; -fx-border-color: #767676; -fx-border-radius: 10px; -fx-text-fill: #767676; -fx-font-family: 'Outfit'; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 8 20;");
        btnB.setOnAction(e -> editStage.close());

        Button btnS = new Button("Simpan"); 
        btnS.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 10px; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        btnS.setOnAction(e -> { if (!input.getText().trim().isEmpty()) { goal.setTitle(input.getText().trim()); renderGoalList(); editStage.close(); } });
        
        btns.getChildren().addAll(btnB, btnS);
        card.getChildren().addAll(title, input, btns); editBackdrop.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        Scene scene = new Scene(editBackdrop); scene.setFill(Color.TRANSPARENT);
        try { scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); } catch (Exception e) {}
        editStage.setScene(scene); editStage.show();
    }

    private void showCustomDatePicker(ImageView source, Label dateValue) {
        VBox pickerPopup = new VBox(15);
        pickerPopup.getStyleClass().add("date-picker-popup");
        pickerPopup.setMaxWidth(350);
        pickerPopup.setMaxHeight(Region.USE_PREF_SIZE); // DYNAMIC HEIGHT
        pickerPopup.setAlignment(Pos.CENTER);
        
        // Header (Month/Year)
        HBox header = new HBox(10); header.setAlignment(Pos.CENTER);
        Button btnPrev = new Button("<"); btnPrev.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        Label monthYearLabel = new Label(); monthYearLabel.getStyleClass().add("calendar-header");
        Button btnNext = new Button(">"); btnNext.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        header.getChildren().addAll(btnPrev, monthYearLabel, btnNext);
        
        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar-grid");
        
        final LocalDate[] displayMonth = {selectedDate.withDayOfMonth(1)};
        
        Runnable updateGrid = () -> {
            grid.getChildren().clear();
            monthYearLabel.setText(displayMonth[0].getMonth().name() + " " + displayMonth[0].getYear());
            
            // Days of week
            String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
            for (int i = 0; i < 7; i++) {
                Label d = new Label(days[i]); d.setStyle("-fx-font-weight: bold; -fx-text-fill: #767676;");
                grid.add(d, i, 0);
                GridPane.setHalignment(d, javafx.geometry.HPos.CENTER);
            }
            
            int firstDay = displayMonth[0].getDayOfWeek().getValue() % 7;
            LocalDate day = displayMonth[0].minusDays(firstDay);
            for (int row = 1; row < 7; row++) {
                for (int col = 0; col < 7; col++) {
                    final LocalDate cellDate = day;
                    Label cell = new Label(String.valueOf(day.getDayOfMonth()));
                    cell.getStyleClass().add("calendar-cell");
                    if (day.getMonth() != displayMonth[0].getMonth()) cell.setOpacity(0.3);
                    if (day.equals(selectedDate)) cell.getStyleClass().add("calendar-cell-selected");
                    if (day.equals(LocalDate.now())) cell.getStyleClass().add("calendar-cell-today");
                    if (day.isBefore(LocalDate.now())) { cell.setDisable(true); cell.setOpacity(0.2); }
                    
                    cell.setOnMouseClicked(e -> {
                        selectedDate = cellDate;
                        dateValue.setText(selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)));
                        currentGoals = new ArrayList<>(goalService.getGoalsForDate(selectedDate));
                        renderGoalList();
                        closePicker(pickerPopup);
                    });
                    
                    grid.add(cell, col, row);
                    day = day.plusDays(1);
                }
            }
        };
        
        btnPrev.setOnAction(e -> { displayMonth[0] = displayMonth[0].minusMonths(1); updateGrid.run(); });
        btnNext.setOnAction(e -> { displayMonth[0] = displayMonth[0].plusMonths(1); updateGrid.run(); });
        
        updateGrid.run();
        pickerPopup.getChildren().addAll(header, grid);
        
        // Position popup
        javafx.geometry.Bounds bounds = source.localToScene(source.getBoundsInLocal());
        pickerPopup.setTranslateX(bounds.getMinX() - 300);
        pickerPopup.setTranslateY(bounds.getMinY());
        
        backdrop.getChildren().add(pickerPopup);
        StackPane.setAlignment(pickerPopup, Pos.TOP_LEFT);
        
        // Animation
        FadeTransition ft = new FadeTransition(Duration.millis(200), pickerPopup);
        ft.setFromValue(0); ft.setToValue(1);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), pickerPopup);
        st.setFromX(0.8); st.setFromY(0.8); st.setToX(1); st.setToY(1);
        ft.play(); st.play();
        
        // Click outside to close
        backdrop.setOnMousePressed(e -> {
            if (!pickerPopup.getBoundsInParent().contains(e.getX(), e.getY())) {
                closePicker(pickerPopup);
            }
        });
    }

    private void closePicker(VBox picker) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), picker);
        ft.setToValue(0);
        ft.setOnFinished(ev -> backdrop.getChildren().remove(picker));
        ft.play();
        backdrop.setOnMousePressed(null);
    }

    public void show() { if (!stage.isShowing()) stage.show(); }
}