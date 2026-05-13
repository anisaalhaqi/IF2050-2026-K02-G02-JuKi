package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import com.juki.service.GoalService;
import javafx.geometry.Insets;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.collections.MapChangeListener;
import javafx.application.Platform;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CalendarView {
    private User currentUser;
    private LocalDate[] currentMonth;
    private GridPane grid;
    private Label monthLabel;
    private VBox targetBox;
    private LocalDate selectedDate;
    
    private final GoalService goalService = GoalService.getInstance();
    private final EntryController entryController = new EntryController();
    private List<JournalEntry> entries;

    private static final String MUTED_STYLE = "-fx-text-fill: #A5A5A5; -fx-opacity: 0.8;";

    public CalendarView(User user) {
        this.currentUser = user;
        this.currentMonth = new LocalDate[]{LocalDate.now().withDayOfMonth(1)};
        this.selectedDate = LocalDate.now();
        this.entries = entryController.getAllEntries(user.getId());
        
        goalService.setCurrentUser(user);

        // UI Update Listener
        goalService.getGoalsCache().addListener((MapChangeListener<LocalDate, List<SelfCareGoal>>) change -> {
            Platform.runLater(() -> {
                if (grid != null) {
                    renderCalendar();
                    updateSidebarTargets();
                }
            });
        });
    }

    public HBox getView() {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: white;");

        // 1. LEFT SIDEBAR
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(294);
        sidebar.setStyle("-fx-background-color: #FAE7FF; -fx-padding: 16px;");
        sidebar.setAlignment(Pos.TOP_CENTER);

        VBox contentSidebar = new VBox(24);
        contentSidebar.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-padding: 24px 16px;");
        contentSidebar.setAlignment(Pos.TOP_CENTER);

        VBox streakBox = new VBox(8); streakBox.setAlignment(Pos.CENTER);
        HBox streakValueBox = new HBox(8); streakValueBox.setAlignment(Pos.CENTER);
        Label streakNum = new Label(); streakNum.textProperty().bind(goalService.streakProperty().asString());
        streakNum.setFont(Font.font("Outfit", FontWeight.MEDIUM, 50));
        ImageView fireImage = new ImageView(new Image("file:img/dashboard/streak_fire.png"));
        fireImage.setFitWidth(42); fireImage.setPreserveRatio(true);
        streakValueBox.getChildren().addAll(streakNum, fireImage);
        streakBox.getChildren().addAll(streakValueBox, new Label("day streak"));

        targetBox = new VBox(16);

        contentSidebar.getChildren().addAll(streakBox, targetBox);
        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        HBox profileBox = new HBox(16); profileBox.setAlignment(Pos.CENTER_LEFT);
        Circle avatar = new Circle(35, Color.web("#D9D9D9"));
        profileBox.getChildren().addAll(avatar, new Label(currentUser.getFullName()));
        sidebar.getChildren().addAll(contentSidebar, spacer, profileBox);

        // 2. MAIN CALENDAR AREA
        VBox mainArea = new VBox(); HBox.setHgrow(mainArea, Priority.ALWAYS);
        HBox header = new HBox(); header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-border-color: #D6D6D6; -fx-border-width: 0 0 1px 1px; -fx-padding: 38px 0;");
        
        ImageView btnPrev = new ImageView(new Image("file:img/icons/arrow-left.png")); btnPrev.setFitWidth(32); btnPrev.setPreserveRatio(true); btnPrev.setStyle("-fx-cursor: hand;");
        monthLabel = new Label(); monthLabel.setFont(Font.font("Outfit", 35)); monthLabel.setMinWidth(250); monthLabel.setAlignment(Pos.CENTER);
        ImageView btnNext = new ImageView(new Image("file:img/icons/arrow-right.png")); btnNext.setFitWidth(32); btnNext.setPreserveRatio(true); btnNext.setStyle("-fx-cursor: hand;");
        header.getChildren().addAll(btnPrev, monthLabel, btnNext);

        grid = new GridPane(); VBox.setVgrow(grid, Priority.ALWAYS);
        for (int i = 0; i < 7; i++) { ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(100.0/7.0); grid.getColumnConstraints().add(cc); }
        for (int i = 0; i < 6; i++) { RowConstraints rc = new RowConstraints(); rc.setPercentHeight(100.0/6.0); grid.getRowConstraints().add(rc); }

        btnPrev.setOnMouseClicked(e -> { currentMonth[0] = currentMonth[0].minusMonths(1); renderCalendar(); });
        btnNext.setOnMouseClicked(e -> { currentMonth[0] = currentMonth[0].plusMonths(1); renderCalendar(); });

        renderCalendar();
        updateSidebarTargets();
        
        mainArea.getChildren().addAll(header, grid);
        root.getChildren().addAll(sidebar, mainArea);
        return root;
    }

    private void updateSidebarTargets() {
        if (targetBox == null) return;
        targetBox.getChildren().clear();
        Label targetTitle = new Label("Target Self-care"); targetTitle.setFont(Font.font("Outfit", 25));
        targetBox.getChildren().add(targetTitle);
        List<SelfCareGoal> goals = goalService.getGoalsForDate(selectedDate);
        if (goals.isEmpty()) { Label n = new Label("Belum ada target."); n.setStyle(MUTED_STYLE); targetBox.getChildren().add(n); }
        else {
            for (SelfCareGoal g : goals) {
                HBox i = new HBox(16); i.setAlignment(Pos.CENTER_LEFT);
                Circle d = new Circle(10, g.isCompleted() ? Color.web("#82DD55") : Color.TRANSPARENT); d.setStroke(Color.web("#82DD55"));
                Label l = new Label(g.getTitle()); l.setFont(Font.font(18));
                if (g.isCompleted()) l.setStyle("-fx-text-decoration: line-through; -fx-text-fill: #767676;");
                i.getChildren().addAll(d, l); targetBox.getChildren().add(i);
            }
        }
    }

    private void renderCalendar() {
        if (grid == null) return;
        grid.getChildren().clear();
        monthLabel.setText(currentMonth[0].getMonth().name() + " " + currentMonth[0].getYear());
        int dayOfWeek = currentMonth[0].getDayOfWeek().getValue() % 7;
        LocalDate currentDay = currentMonth[0].minusDays(dayOfWeek);
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                final LocalDate cellDate = currentDay;
                VBox cell = new VBox(6); cell.setAlignment(Pos.TOP_CENTER);
                cell.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-width: 0 1px 1px " + (col == 0 ? "1px" : "0") + "; -fx-padding: 8px; -fx-cursor: hand;");
                
                StackPane datePane = new StackPane(); Label dateLbl = new Label(String.valueOf(cellDate.getDayOfMonth())); dateLbl.setFont(Font.font(18));
                if (cellDate.equals(LocalDate.now())) { Circle bg = new Circle(15, Color.web("#FFE341")); datePane.getChildren().addAll(bg, dateLbl); }
                else if (cellDate.getMonth() != currentMonth[0].getMonth()) { dateLbl.setStyle(MUTED_STYLE); datePane.getChildren().add(dateLbl); }
                else { datePane.getChildren().add(dateLbl); }
                cell.getChildren().add(datePane);

                List<SelfCareGoal> goals = goalService.getGoalsForDate(cellDate);
                if (!goals.isEmpty()) {
                    HBox h = new HBox(4); h.setAlignment(Pos.CENTER);
                    Circle dot = new Circle(5, goals.stream().allMatch(SelfCareGoal::isCompleted) ? Color.web("#82DD55") : Color.web("#D6D6D6"));
                    h.getChildren().add(dot); cell.getChildren().add(h);
                }

                cell.setOnMouseClicked(e -> {
                    selectedDate = cellDate; updateSidebarTargets();
                    if (!goals.isEmpty()) showDetailModal(cellDate);
                    else new GoalModal(currentUser, cellDate, () -> goalService.refreshDate(cellDate)).show();
                });
                grid.add(cell, col, row); currentDay = currentDay.plusDays(1);
            }
        }
    }

    private void showDetailModal(LocalDate date) {
        Stage stage = new Stage(); stage.initModality(Modality.APPLICATION_MODAL); stage.initStyle(StageStyle.TRANSPARENT);

        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        backdrop.setPrefSize(1920, 1080);
        backdrop.setOnMouseClicked(e -> { if (e.getTarget() == backdrop) stage.close(); });

        VBox card = new VBox(24);
        card.setMinWidth(688); card.setMaxWidth(688); card.setPrefWidth(688);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 34.78px; -fx-padding: 55px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 24, 0, 0, 4);");
        card.setAlignment(Pos.TOP_LEFT);

        // HEADER ROW: title + date + X button
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label("Target Self-Care");
        titleLbl.setFont(Font.font("Outfit", FontWeight.NORMAL, 29.57));
        titleLbl.setTextFill(Color.web("#292929"));

        Region headerSpacer = new Region(); HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        String formattedDate = date.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.US));
        Label dateLbl = new Label(formattedDate);
        dateLbl.setFont(Font.font("Outfit", FontWeight.NORMAL, 22));
        dateLbl.setTextFill(Color.web("#767676"));

        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #767676; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 0 0 0 8px;");
        btnClose.setOnAction(e -> stage.close());

        headerRow.getChildren().addAll(titleLbl, headerSpacer, dateLbl, btnClose);

        // DIVIDER
        javafx.scene.control.Separator divider = new javafx.scene.control.Separator();
        divider.setStyle("-fx-background-color: #D6D6D6;");

        // TARGET LIST
        VBox targetList = new VBox(8);
        for (SelfCareGoal g : goalService.getGoalsForDate(date)) {
            HBox item = new HBox(16); item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(6, 0, 6, 0));
            Label lbl = new Label(g.getTitle());
            lbl.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
            if (g.isCompleted()) {
                lbl.setStyle("-fx-text-decoration: line-through; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-font-weight: 300;");
            }
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Circle c = new Circle(17.5, g.isCompleted() ? Color.web("#82DD55") : Color.TRANSPARENT);
            c.setStroke(Color.web("#82DD55")); c.setStrokeWidth(2);
            c.setStyle("-fx-cursor: hand;");
            c.setOnMouseClicked(e -> { goalService.toggleGoalStatus(g); stage.close(); showDetailModal(date); });
            item.getChildren().addAll(lbl, sp, c);
            targetList.getChildren().add(item);
        }

        card.getChildren().addAll(headerRow, divider, targetList);
        backdrop.getChildren().add(card); StackPane.setAlignment(card, Pos.CENTER);
        Scene scene = new Scene(backdrop); scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene); stage.show(); stage.centerOnScreen();
    }
}