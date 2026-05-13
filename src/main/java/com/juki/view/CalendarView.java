package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import com.juki.service.GoalService;
import javafx.animation.FadeTransition;
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
import javafx.util.Duration;

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

    private static final String MUTED_STYLE = "-fx-text-fill: #858585; -fx-opacity: 1;";

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
        grid.getRowConstraints().clear();

        // HEADER HARI
        RowConstraints headerRow = new RowConstraints();
        headerRow.setPercentHeight(8);
        grid.getRowConstraints().add(headerRow);

        // ROW TANGGAL
        for (int i = 0; i < 6; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(15.3);
            grid.getRowConstraints().add(rc);
        }

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
        Label targetTitle = new Label("Target Self-care"); 
        targetTitle.setFont(Font.font("Outfit", 25));
        targetTitle.setTextFill(Color.web("#292929"));
        targetBox.getChildren().add(targetTitle);
        
        List<SelfCareGoal> goals = goalService.getGoalsForDate(selectedDate);
        if (goals.isEmpty()) { 
            Label n = new Label("Belum ada target."); 
            n.setStyle(MUTED_STYLE); 
            targetBox.getChildren().add(n); 
        } else {
            for (SelfCareGoal g : goals) {
                HBox i = new HBox(16); i.setAlignment(Pos.CENTER_LEFT);
                Circle d = new Circle(10, g.isCompleted() ? Color.web("#82DD55") : Color.TRANSPARENT); d.setStroke(Color.web("#82DD55"));
                Label l = new Label(g.getTitle()); 
                l.setFont(Font.font("Outfit", 18));
                l.setTextFill(Color.web("#292929"));
                if (g.isCompleted()) {
                    l.setStyle("-fx-text-decoration: line-through;");
                    l.setTextFill(Color.web("#767676"));
                }
                i.getChildren().addAll(d, l); targetBox.getChildren().add(i);
            }
        }
    }

    private void renderCalendar() {
        if (grid == null) return;
        grid.getChildren().clear();
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            StackPane dayHeader = new StackPane();
            dayHeader.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #D6D6D6;" +
                "-fx-border-width: 0 1px 1px " + (i == 0 ? "1px" : "0") + ";" +
                "-fx-padding: 12px;"
            );

            Label dayLabel = new Label(days[i]);
            dayLabel.setFont(Font.font("Outfit", FontWeight.BOLD, 16));
            dayLabel.setTextFill(Color.web("#767676"));

            dayHeader.getChildren().add(dayLabel);

            grid.add(dayHeader, i, 0);
        }
        monthLabel.setText(currentMonth[0].getMonth().name() + " " + currentMonth[0].getYear());
        int dayOfWeek = currentMonth[0].getDayOfWeek().getValue() % 7;
        LocalDate currentDay = currentMonth[0].minusDays(dayOfWeek);
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                final LocalDate cellDate = currentDay;
                VBox cell = new VBox(6); cell.setAlignment(Pos.TOP_CENTER);
                cell.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-width: 0 1px 1px " + (col == 0 ? "1px" : "0") + "; -fx-padding: 8px; -fx-cursor: hand;");
                
                StackPane datePane = new StackPane(); Label dateLbl = new Label(String.valueOf(cellDate.getDayOfMonth())); dateLbl.setFont(Font.font(18));
                dateLbl.setTextFill(Color.web("#292929"));
                
                if (cellDate.equals(LocalDate.now())) { 
                    Circle bg = new Circle(15, Color.web("#FFE341")); 
                    datePane.getChildren().addAll(bg, dateLbl); 
                } else if (cellDate.getMonth() != currentMonth[0].getMonth()) { 
                    dateLbl.setStyle(MUTED_STYLE); 
                    datePane.getChildren().add(dateLbl); 
                } else { 
                    datePane.getChildren().add(dateLbl); 
                }
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
        
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX()); stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth()); stage.setHeight(screenBounds.getHeight());

        StackPane backdrop = new StackPane(); 
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        backdrop.setOnMouseClicked(e -> { if (e.getTarget() == backdrop) stage.close(); });
        
        VBox rootCard = new VBox(20); 
        rootCard.getStyleClass().add("modal-card");
        rootCard.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 20, 0, 0, 4);");
        rootCard.setPrefWidth(Region.USE_COMPUTED_SIZE); rootCard.setMinWidth(350); rootCard.setMaxWidth(480);
        rootCard.setAlignment(Pos.TOP_LEFT);
        rootCard.setFillWidth(true);
        rootCard.setMaxHeight(Region.USE_PREF_SIZE); // DYNAMIC HEIGHT

        HBox header = new HBox(12); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Target Self-Care"); 
        title.setFont(Font.font("Outfit", FontWeight.NORMAL, 22));
        title.setTextFill(Color.web("#292929"));
        
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label dateLbl = new Label(date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US))); 
        dateLbl.setFont(Font.font("Outfit", 16)); dateLbl.setTextFill(Color.web("#5a5a5a"));
        
        StackPane moreBtn = new StackPane();
        moreBtn.getStyleClass().add("more-menu-button");
        ImageView moreIcon = new ImageView(new Image("file:img/icons/more.png"));
        moreIcon.setFitWidth(20); moreIcon.setPreserveRatio(true);
        moreBtn.getChildren().add(moreIcon);
        
        moreBtn.setOnMouseClicked(e -> showDetailMenu(moreBtn, date, stage, backdrop));
        
        header.getChildren().addAll(title, spacer, dateLbl, moreBtn);
        
        VBox listContainer = new VBox(12);
        List<SelfCareGoal> goals = goalService.getGoalsForDate(date);
        if (goals.isEmpty()) {
            Label emptyLbl = new Label("Tidak ada target untuk tanggal ini.");
            emptyLbl.setFont(Font.font("Outfit", 14));
            emptyLbl.setTextFill(Color.web("#767676"));
            listContainer.getChildren().add(emptyLbl);
        } else {
            for (SelfCareGoal g : goals) {
                HBox item = new HBox(12); item.setAlignment(Pos.CENTER_LEFT);
                item.setPadding(new Insets(4, 0, 4, 0));
                
                Label lbl = new Label(g.getTitle()); 
                lbl.getStyleClass().add("target-label");
                lbl.setFont(Font.font("Outfit", 16));
                lbl.setTextFill(Color.web("#292929"));
                lbl.setWrapText(true);
                lbl.setMaxWidth(350);
                
                if (g.isCompleted()) {
                    lbl.getStyleClass().add("target-label-completed");
                    lbl.setStyle("-fx-text-decoration: line-through;");
                    lbl.setTextFill(Color.web("#767676"));
                }

                Region spacerRight = new Region();
                HBox.setHgrow(spacerRight, Priority.ALWAYS);
                
                Circle status = new Circle(12, g.isCompleted() ? Color.web("#82DD55") : Color.TRANSPARENT); 
                status.setStroke(Color.web("#82DD55"));
                status.setStyle("-fx-cursor: hand;");
                status.setOnMouseClicked(e -> { 
                    goalService.toggleGoalStatus(g); 
                    stage.close(); 
                    showDetailModal(date); 
                });
                
                item.getChildren().addAll(lbl, spacerRight, status); listContainer.getChildren().add(item);
            }
        }

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(Region.USE_COMPUTED_SIZE);
        scrollPane.setMaxHeight(300); 
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        
        Button btnClose = new Button("Tutup");
        btnClose.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 10px; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 25;");
        btnClose.setOnAction(e -> stage.close());
        HBox btnRow = new HBox(); btnRow.setAlignment(Pos.CENTER_RIGHT); btnRow.getChildren().add(btnClose);

        rootCard.getChildren().addAll(header, scrollPane, btnRow); 
        
        backdrop.getChildren().add(rootCard);
        StackPane.setAlignment(rootCard, Pos.CENTER);
        
        Scene scene = new Scene(backdrop);
        scene.setFill(Color.TRANSPARENT);
        try { scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); } catch (Exception e) {}
        
        stage.setScene(scene);
        FadeTransition ft = new FadeTransition(Duration.millis(300), backdrop);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();
        stage.show();
    }

    private void showDetailMenu(StackPane source, LocalDate date, Stage detailStage, StackPane backdrop) {
        VBox menu = new VBox();
        menu.getStyleClass().add("dropdown-menu");
        menu.setMinWidth(150);
        menu.setMaxWidth(Region.USE_PREF_SIZE);
        menu.setMaxHeight(Region.USE_PREF_SIZE); // DYNAMIC HEIGHT
        
        Label editItem = new Label("Edit Self-Care");
        editItem.getStyleClass().add("dropdown-item");
        editItem.setMaxWidth(Double.MAX_VALUE);
        editItem.setOnMouseClicked(e -> {
            backdrop.getChildren().remove(menu);
            detailStage.close();
            new GoalModal(currentUser, date, () -> {
                goalService.refreshDate(date);
                showDetailModal(date);
            }, true).show();
        });
        
        Label deleteItem = new Label("Hapus Self-Care");
        deleteItem.getStyleClass().add("dropdown-item");
        deleteItem.setMaxWidth(Double.MAX_VALUE);
        deleteItem.setOnMouseClicked(e -> {
            backdrop.getChildren().remove(menu);
            showDeleteConfirmation(date, detailStage);
        });
        
        menu.getChildren().addAll(editItem, deleteItem);
        
        javafx.geometry.Bounds bounds = source.localToScene(source.getBoundsInLocal());
        menu.setTranslateX(bounds.getMinX() - 130);
        menu.setTranslateY(bounds.getMinY() + 30);
        
        backdrop.getChildren().add(menu);
        StackPane.setAlignment(menu, Pos.TOP_LEFT);

        FadeTransition ft = new FadeTransition(Duration.millis(150), menu);
        ft.setFromValue(0); ft.setToValue(1);
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(Duration.millis(150), menu);
        st.setFromX(0.9); st.setFromY(0.9); st.setToX(1); st.setToY(1);
        ft.play(); st.play();
        
        backdrop.setOnMousePressed(e -> {
            if (!menu.getBoundsInParent().contains(e.getX(), e.getY())) {
                backdrop.getChildren().remove(menu);
                backdrop.setOnMousePressed(ev -> { if (ev.getTarget() == backdrop) detailStage.close(); });
            }
        });
    }

    private void showDeleteConfirmation(LocalDate date, Stage detailStage) {
        Stage stage = new Stage(); stage.initModality(Modality.APPLICATION_MODAL); stage.initStyle(StageStyle.TRANSPARENT);
        
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX()); stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth()); stage.setHeight(screenBounds.getHeight());

        StackPane backdrop = new StackPane(); backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        backdrop.setOnMouseClicked(e -> { if (e.getTarget() == backdrop) stage.close(); });
        
        VBox card = new VBox(25);
        card.getStyleClass().add("delete-modal");
        card.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 20, 0, 0, 4);");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE); card.setMaxWidth(480); 
        card.setAlignment(Pos.CENTER);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        ImageView warningIcon = new ImageView(new Image("file:img/icons/alert.png"));
        warningIcon.setFitWidth(64); warningIcon.setPreserveRatio(true);
        
        Label title = new Label("Apakah anda yakin mengahapus target ini hari ini?");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 20)); title.setTextFill(Color.web("#292929"));

        Label msg = new Label("Aksi ini tidak dapat dipulihkan.");
        msg.setFont(Font.font("Outfit", 16)); msg.setTextFill(Color.web("#767676"));
        
        HBox buttons = new HBox(20); buttons.setAlignment(Pos.CENTER);
        Button btnBack = new Button("Kembali"); btnBack.getStyleClass().add("btn-delete-cancel");
        btnBack.setStyle("-fx-background-color: white; -fx-border-color: #DC2626; -fx-border-radius: 12.5px; -fx-text-fill: #DC2626; -fx-font-family: 'Plus Jakarta Sans'; -fx-font-weight: 600; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 10 32;");
        btnBack.setOnAction(e -> stage.close());
        
        Button btnDelete = new Button("Hapus"); btnDelete.getStyleClass().add("btn-delete-confirm");
        btnDelete.setStyle("-fx-background-color: #DC2626; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-family: 'Plus Jakarta Sans'; -fx-font-weight: 600; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 10 32;");
        btnDelete.setOnAction(e -> {
            goalService.deleteAllForDate(date);
            stage.close();
            detailStage.close();
        });
        
        buttons.getChildren().addAll(btnBack, btnDelete);
        card.getChildren().addAll(warningIcon, title, msg, buttons);
        backdrop.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
        
        Scene scene = new Scene(backdrop); scene.setFill(Color.TRANSPARENT);
        try { scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); } catch (Exception e) {}
        stage.setScene(scene);
        
        FadeTransition ft = new FadeTransition(Duration.millis(200), backdrop);
        ft.setFromValue(0); ft.setToValue(1);
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(Duration.millis(200), card);
        st.setFromX(0.8); st.setFromY(0.8); st.setToX(1); st.setToY(1);
        ft.play(); st.play();
        
        stage.show();
    }
}
