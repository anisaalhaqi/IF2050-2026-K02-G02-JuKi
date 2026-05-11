package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.controller.GoalController;
import com.juki.model.JournalEntry;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class CalendarView {
    private User currentUser;
    private LocalDate[] currentMonth;
    private GridPane grid;
    private Label monthLabel;
    
    private EntryController entryController;
    private GoalController goalController;
    private List<JournalEntry> entries;

    public CalendarView(User user) {
        this.currentUser = user;
        this.currentMonth = new LocalDate[]{LocalDate.now().withDayOfMonth(1)};
        this.entryController = new EntryController();
        this.goalController = new GoalController();
        this.entries = entryController.getAllEntries(user.getId());
    }

    public HBox getView() {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: white;");

        // =====================================
        // 1. LEFT SIDEBAR (Streak & Targets)
        // =====================================
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(294);
        sidebar.setStyle("-fx-background-color: #FAE7FF; -fx-padding: 16px;");
        sidebar.setAlignment(Pos.TOP_CENTER);

        VBox contentSidebar = new VBox(24);
        contentSidebar.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-padding: 24px 16px;");
        contentSidebar.setAlignment(Pos.TOP_CENTER);

        // Streak Area
        int streakCountVal = (int) entries.stream().map(JournalEntry::getDate).distinct().count();
        VBox streakBox = new VBox(8);
        streakBox.setAlignment(Pos.CENTER);
        
        HBox streakValueBox = new HBox(8);
        streakValueBox.setAlignment(Pos.CENTER);
        Label streakNum = new Label(String.valueOf(streakCountVal));
        streakNum.setFont(Font.font("Outfit", FontWeight.MEDIUM, 50));
        streakNum.setTextFill(Color.BLACK);
        ImageView fireImage = new ImageView(new Image("file:img/beranda/streak_fire.png"));
        fireImage.setFitWidth(42); fireImage.setPreserveRatio(true);
        streakValueBox.getChildren().addAll(streakNum, fireImage);
        
        Label streakText = new Label("day streak");
        streakText.setFont(Font.font("Outfit", FontWeight.MEDIUM, 20));
        streakBox.getChildren().addAll(streakValueBox, streakText);

        // Today Targets
        VBox targetBox = new VBox(16);
        Label targetTitle = new Label("Target Self-care");
        targetTitle.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        targetTitle.setTextFill(Color.web("#292929"));
        targetBox.getChildren().add(targetTitle);
        
        List<SelfCareGoal> todayGoals = goalController.getGoalsByDate(LocalDate.now());
        if (todayGoals.isEmpty()) {
            Label noTarget = new Label("Belum ada target hari ini.");
            noTarget.setTextFill(Color.web("#434343"));
            targetBox.getChildren().add(noTarget);
        } else {
            for (SelfCareGoal goal : todayGoals) {
                HBox tItem = new HBox(16);
                tItem.setAlignment(Pos.CENTER_LEFT);
                Circle dot = new Circle(17.5, Color.web("#82DD55"));
                Label tLbl = new Label(goal.getTitle());
                tLbl.setFont(Font.font("Outfit", FontWeight.NORMAL, 20));
                tLbl.setTextFill(Color.web("#434343"));
                if (goal.isCompleted()) tLbl.setStyle("-fx-text-decoration: line-through;");
                tItem.getChildren().addAll(dot, tLbl);
                targetBox.getChildren().add(tItem);
            }
        }
        contentSidebar.getChildren().addAll(streakBox, targetBox);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Profile Area
        HBox profileBox = new HBox(16);
        profileBox.setAlignment(Pos.CENTER_LEFT);
        profileBox.setPadding(new Insets(16, 0, 0, 0));
        Circle avatar = new Circle(35, Color.web("#D9D9D9")); // Placeholder Avatar
        Label userName = new Label(currentUser.getFullName());
        userName.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        profileBox.getChildren().addAll(avatar, userName);

        sidebar.getChildren().addAll(contentSidebar, spacer, profileBox);

        // =====================================
        // 2. MAIN CALENDAR AREA
        // =====================================
        VBox mainArea = new VBox();
        HBox.setHgrow(mainArea, Priority.ALWAYS);
        mainArea.setStyle("-fx-background-color: white;");

        // Month Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-border-color: #D6D6D6; -fx-border-width: 0 0 1px 1px; -fx-padding: 38px 0;");
        
        ImageView btnPrev = new ImageView(new Image("file:img/icons/arrow-left.png"));
        btnPrev.setFitWidth(32); btnPrev.setPreserveRatio(true);
        btnPrev.setStyle("-fx-cursor: hand;");
        
        monthLabel = new Label();
        monthLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 35));
        monthLabel.setTextFill(Color.web("#292929"));
        monthLabel.setMinWidth(250);
        monthLabel.setAlignment(Pos.CENTER);
        
        ImageView btnNext = new ImageView(new Image("file:img/icons/arrow-right.png"));
        btnNext.setFitWidth(32); btnNext.setPreserveRatio(true);
        btnNext.setStyle("-fx-cursor: hand;");
        
        header.getChildren().addAll(btnPrev, monthLabel, btnNext);

        // Calendar Grid
        grid = new GridPane();
        VBox.setVgrow(grid, Priority.ALWAYS);
        
        // Constraint column & row untuk mengisi ruang rata
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7.0);
            grid.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < 6; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 6.0);
            grid.getRowConstraints().add(rc);
        }

        btnPrev.setOnMouseClicked(e -> { currentMonth[0] = currentMonth[0].minusMonths(1); renderCalendar(); });
        btnNext.setOnMouseClicked(e -> { currentMonth[0] = currentMonth[0].plusMonths(1); renderCalendar(); });

        renderCalendar();

        mainArea.getChildren().addAll(header, grid);
        root.getChildren().addAll(sidebar, mainArea);

        return root;
    }

    private void renderCalendar() {
        grid.getChildren().clear();
        
        String mName = currentMonth[0].getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("en", "US"));
        monthLabel.setText(mName + " " + currentMonth[0].getYear());

        int dayOfWeek = currentMonth[0].getDayOfWeek().getValue() % 7; // 0 = Sunday
        LocalDate startDay = currentMonth[0].minusDays(dayOfWeek);

        LocalDate currentDay = startDay;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                VBox cell = new VBox(6);
                cell.setAlignment(Pos.TOP_CENTER);
                cell.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-width: 0 1px 1px " + (col == 0 ? "1px" : "0") + "; -fx-padding: 16px;");

                // Day Names in First Row (Mockup Style)
                if (row == 0) {
                    Label dName = new Label(currentDay.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new Locale("en", "US")));
                    dName.setFont(Font.font("Outfit", FontWeight.LIGHT, 15));
                    dName.setTextFill(Color.web("rgba(0, 0, 0, 0.20)"));
                    cell.getChildren().add(dName);
                }

                StackPane datePane = new StackPane();
                Label dateLbl = new Label(String.valueOf(currentDay.getDayOfMonth()));
                dateLbl.setFont(Font.font("Outfit", FontWeight.MEDIUM, 20));

                // Highlight Logic
                if (currentDay.equals(LocalDate.now())) {
                    dateLbl.setTextFill(Color.web("#292929"));
                    Circle bg = new Circle(20, Color.web("#FFE341")); // Lingkaran kuning
                    datePane.getChildren().addAll(bg, dateLbl);
                } else if (currentDay.getMonth() != currentMonth[0].getMonth()) {
                    dateLbl.setTextFill(Color.web("rgba(0, 0, 0, 0.20)"));
                    datePane.getChildren().add(dateLbl);
                } else {
                    dateLbl.setTextFill(Color.web("#292929"));
                    datePane.getChildren().add(dateLbl);
                }
                cell.getChildren().add(datePane);

                // Render Targets if exists in current month
                List<SelfCareGoal> dayGoals = goalController.getGoalsByDate(currentDay);
                if (!dayGoals.isEmpty() && currentDay.getMonth() == currentMonth[0].getMonth()) {
                    VBox targetList = new VBox(4);
                    for (int i = 0; i < Math.min(dayGoals.size(), 3); i++) {
                        HBox tBox = new HBox(8);
                        tBox.setAlignment(Pos.CENTER_LEFT);
                        tBox.setStyle("-fx-background-color: #FFFAC1; -fx-background-radius: 10px; -fx-padding: 5px 8px;");
                        Circle dot = new Circle(10, Color.web("#82DD55"));
                        Label tLbl = new Label(dayGoals.get(i).getTitle());
                        tLbl.setFont(Font.font("Outfit", 15));
                        tLbl.setTextFill(Color.web("#434343"));
                        tBox.getChildren().addAll(dot, tLbl);
                        targetList.getChildren().add(tBox);
                    }
                    cell.getChildren().add(targetList);
                }

                grid.add(cell, col, row);
                currentDay = currentDay.plusDays(1);
            }
        }
    }
}