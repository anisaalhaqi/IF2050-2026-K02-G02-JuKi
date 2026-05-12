package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.controller.GoalController;
import com.juki.model.JournalEntry;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
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
    
    private EntryController entryController;
    private GoalController goalController;
    private List<JournalEntry> entries;

    private static final String MUTED_STYLE = "-fx-text-fill: #A5A5A5; -fx-opacity: 0.8;"; // Approximating overlay effect

    public CalendarView(User user) {
        this.currentUser = user;
        this.currentMonth = new LocalDate[]{LocalDate.now().withDayOfMonth(1)};
        this.selectedDate = LocalDate.now();
        this.entryController = new EntryController();
        this.goalController = new GoalController();
        this.entries = entryController.getAllEntries(user.getId());
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

        int streakCountVal = (int) entries.stream().map(JournalEntry::getDate).distinct().count();
        VBox streakBox = new VBox(8);
        streakBox.setAlignment(Pos.CENTER);
        HBox streakValueBox = new HBox(8);
        streakValueBox.setAlignment(Pos.CENTER);
        Label streakNum = new Label(String.valueOf(streakCountVal));
        streakNum.setFont(Font.font("Outfit", FontWeight.MEDIUM, 50));
        ImageView fireImage = new ImageView(new Image("file:img/beranda/streak_fire.png"));
        fireImage.setFitWidth(42); fireImage.setPreserveRatio(true);
        streakValueBox.getChildren().addAll(streakNum, fireImage);
        Label streakText = new Label("day streak");
        streakText.setFont(Font.font("Outfit", FontWeight.MEDIUM, 20));
        streakBox.getChildren().addAll(streakValueBox, streakText);

        targetBox = new VBox(16);
        updateSidebarTargets();

        contentSidebar.getChildren().addAll(streakBox, targetBox);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        HBox profileBox = new HBox(16);
        profileBox.setAlignment(Pos.CENTER_LEFT);
        Circle avatar = new Circle(35, Color.web("#D9D9D9"));
        Label userName = new Label(currentUser.getFullName());
        userName.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        profileBox.getChildren().addAll(avatar, userName);
        sidebar.getChildren().addAll(contentSidebar, spacer, profileBox);

        // 2. MAIN CALENDAR AREA
        VBox mainArea = new VBox();
        HBox.setHgrow(mainArea, Priority.ALWAYS);
        mainArea.setStyle("-fx-background-color: white;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-border-color: #D6D6D6; -fx-border-width: 0 0 1px 1px; -fx-padding: 38px 0;");
        ImageView btnPrev = new ImageView(new Image("file:img/icons/arrow-left.png"));
        btnPrev.setFitWidth(32); btnPrev.setPreserveRatio(true); btnPrev.setStyle("-fx-cursor: hand;");
        monthLabel = new Label();
        monthLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 35));
        monthLabel.setMinWidth(250); monthLabel.setAlignment(Pos.CENTER);
        ImageView btnNext = new ImageView(new Image("file:img/icons/arrow-right.png"));
        btnNext.setFitWidth(32); btnNext.setPreserveRatio(true); btnNext.setStyle("-fx-cursor: hand;");
        header.getChildren().addAll(btnPrev, monthLabel, btnNext);

        grid = new GridPane();
        VBox.setVgrow(grid, Priority.ALWAYS);
        for (int i = 0; i < 7; i++) { ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(100.0/7.0); grid.getColumnConstraints().add(cc); }
        for (int i = 0; i < 6; i++) { RowConstraints rc = new RowConstraints(); rc.setPercentHeight(100.0/6.0); grid.getRowConstraints().add(rc); }

        btnPrev.setOnMouseClicked(e -> { currentMonth[0] = currentMonth[0].minusMonths(1); renderCalendar(); });
        btnNext.setOnMouseClicked(e -> { currentMonth[0] = currentMonth[0].plusMonths(1); renderCalendar(); });

        renderCalendar();
        mainArea.getChildren().addAll(header, grid);
        root.getChildren().addAll(sidebar, mainArea);
        return root;
    }

    private void updateSidebarTargets() {
        targetBox.getChildren().clear();
        Label targetTitle = new Label("Target Self-care");
        targetTitle.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        targetBox.getChildren().add(targetTitle);
        List<SelfCareGoal> goals = goalController.getGoalsByDate(selectedDate);
        if (goals.isEmpty()) {
            Label noTarget = new Label("Belum ada target.");
            noTarget.setStyle(MUTED_STYLE);
            targetBox.getChildren().add(noTarget);
        } else {
            for (SelfCareGoal goal : goals) {
                HBox tItem = new HBox(16);
                tItem.setAlignment(Pos.CENTER_LEFT);
                Circle dot = new Circle(10, goal.isCompleted() ? Color.web("#82DD55") : Color.TRANSPARENT);
                dot.setStroke(Color.web("#82DD55"));
                Label tLbl = new Label(goal.getTitle());
                tLbl.setFont(Font.font("Outfit", 18));
                if (goal.isCompleted()) {
                    tLbl.setStyle("-fx-text-decoration: line-through; -fx-text-fill: #767676;");
                }
                tItem.getChildren().addAll(dot, tLbl);
                targetBox.getChildren().add(tItem);
            }
        }
    }

    private void renderCalendar() {
        grid.getChildren().clear();
        String mName = currentMonth[0].getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.US);
        monthLabel.setText(mName + " " + currentMonth[0].getYear());
        int dayOfWeek = currentMonth[0].getDayOfWeek().getValue() % 7;
        LocalDate currentDay = currentMonth[0].minusDays(dayOfWeek);
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                final LocalDate cellDate = currentDay;
                VBox cell = new VBox(6);
                cell.setAlignment(Pos.TOP_CENTER);
                cell.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-width: 0 1px 1px " + (col == 0 ? "1px" : "0") + "; -fx-padding: 8px; -fx-cursor: hand;");
                
                if (row == 0) {
                    Label dName = new Label(cellDate.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.US));
                    dName.setFont(Font.font("Outfit", FontWeight.LIGHT, 12));
                    dName.setStyle(MUTED_STYLE);
                    cell.getChildren().add(dName);
                }

                StackPane datePane = new StackPane();
                Label dateLbl = new Label(String.valueOf(cellDate.getDayOfMonth()));
                dateLbl.setFont(Font.font("Outfit", FontWeight.MEDIUM, 18));
                
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

                List<SelfCareGoal> dayGoals = goalController.getGoalsByDate(cellDate);
                if (!dayGoals.isEmpty()) {
                    VBox targetList = new VBox(2);
                    for (int i = 0; i < Math.min(dayGoals.size(), 3); i++) {
                        HBox tBox = new HBox(4);
                        tBox.setAlignment(Pos.CENTER_LEFT);
                        Circle dot = new Circle(5, dayGoals.get(i).isCompleted() ? Color.web("#82DD55") : Color.TRANSPARENT);
                        dot.setStroke(Color.web("#82DD55"));
                        Label tLbl = new Label(dayGoals.get(i).getTitle());
                        tLbl.setFont(Font.font("Outfit", 12));
                        tBox.getChildren().addAll(dot, tLbl);
                        targetList.getChildren().add(tBox);
                    }
                    if (dayGoals.size() > 3) {
                        Label moreLbl = new Label("+ " + (dayGoals.size() - 3) + " lainnya");
                        moreLbl.setStyle(MUTED_STYLE);
                        targetList.getChildren().add(moreLbl);
                    }
                    cell.getChildren().add(targetList);
                }

                cell.setOnMouseClicked(e -> {
                    selectedDate = cellDate;
                    updateSidebarTargets();
                    if (!dayGoals.isEmpty()) showDetailModal(cellDate);
                });

                grid.add(cell, col, row);
                currentDay = currentDay.plusDays(1);
            }
        }
    }

    private void showDetailModal(LocalDate date) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.TRANSPARENT);
        
        VBox root = new VBox(24);
        root.setStyle("-fx-background-color: white; -fx-padding: 55px; -fx-background-radius: 34.78px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.20), 10, 0, 0, 0);");
        root.setPrefWidth(600);

        // HEADER ROW
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Target Self-Care");
        title.setFont(Font.font("Outfit", FontWeight.NORMAL, 29.57));
        title.setTextFill(Color.web("#292929"));
        title.setStyle("-fx-line-spacing: 38.26px; -fx-letter-spacing: 0.30px;");
        
        Region s = new Region(); 
        HBox.setHgrow(s, Priority.ALWAYS);
        
        HBox rightHeader = new HBox(16);
        rightHeader.setAlignment(Pos.CENTER_RIGHT);
        
        Label dateStr = new Label(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)));
        dateStr.setFont(Font.font("Outfit", FontWeight.NORMAL, 29.57));
        dateStr.setTextFill(Color.web("#767676"));
        dateStr.setStyle("-fx-line-spacing: 38.26px; -fx-letter-spacing: 0.30px;");
        
        Button btnMore = new Button("⋮");
        btnMore.setPrefSize(39, 39);
        btnMore.setStyle("-fx-background-color: transparent; -fx-font-size: 25px; -fx-padding: 0; -fx-cursor: hand;");
        
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit Self-Care");
        edit.setOnAction(e -> { 
            modal.close(); 
            new GoalModal(currentUser, date, this::renderCalendar).show(); 
        });
        MenuItem delete = new MenuItem("Hapus Self-Care");
        delete.setOnAction(e -> {
            new DeleteConfirmationModal(() -> {
                goalController.deleteAllGoalsForDate(date);
                renderCalendar();
                updateSidebarTargets();
                modal.close();
            }).show();
        });
        menu.getItems().addAll(edit, delete);
        btnMore.setOnAction(e -> menu.show(btnMore, javafx.geometry.Side.BOTTOM, 0, 0));

        rightHeader.getChildren().addAll(dateStr, btnMore);
        header.getChildren().addAll(title, s, rightHeader);
        
        // TARGET LIST
        VBox list = new VBox(8);
        List<SelfCareGoal> goals = goalController.getGoalsByDate(date);
        for (SelfCareGoal g : goals) {
            HBox item = new HBox();
            item.setPrefHeight(40);
            item.setAlignment(Pos.CENTER_LEFT);
            
            Label lbl = new Label(g.getTitle());
            lbl.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
            lbl.setTextFill(Color.BLACK);
            
            if (g.isCompleted()) {
                lbl.setStyle("-fx-text-decoration: line-through;");
            }
            
            Region sp = new Region(); 
            HBox.setHgrow(sp, Priority.ALWAYS);
            
            StackPane toggle = new StackPane();
            toggle.setPrefSize(35, 35);
            toggle.setStyle("-fx-cursor: hand;");
            
            Circle circle = new Circle(17.5);
            if (g.isCompleted()) {
                circle.setFill(Color.web("#82DD55"));
                // Add Checkmark icon inside if possible, for now just filled circle per spec "filled green"
            } else {
                circle.setFill(Color.TRANSPARENT);
                circle.setStroke(Color.web("#82DD55"));
                circle.setStrokeWidth(2);
            }
            
            toggle.getChildren().add(circle);
            toggle.setOnMouseClicked(e -> {
                goalController.updateGoalStatus(g.getId(), !g.isCompleted());
                modal.close();
                showDetailModal(date);
                renderCalendar();
                updateSidebarTargets();
            });
            
            item.getChildren().addAll(lbl, sp, toggle);
            list.getChildren().add(item);
        }
        
        root.getChildren().addAll(header, list);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        modal.setScene(scene);
        modal.show();
    }
}