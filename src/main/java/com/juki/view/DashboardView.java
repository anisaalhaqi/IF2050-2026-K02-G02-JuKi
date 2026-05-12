package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.SelfCareGoal;
import com.juki.model.User;
import com.juki.service.GoalService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.MapChangeListener;
import javafx.application.Platform;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Arrays;

public class DashboardView {

    private User currentUser;
    private BorderPane mainRoot;
    private final GoalService goalService = GoalService.getInstance();
    private final EntryController entryController = new EntryController();

    public ScrollPane getDashboardView(User user, BorderPane root) {
        this.currentUser = user;
        this.mainRoot = root;
        
        goalService.setCurrentUser(user);
        List<JournalEntry> entries = entryController.getAllEntries(user.getId());

        VBox content = new VBox(48);
        content.setPadding(new Insets(52, 100, 52, 100));
        content.setStyle("-fx-background-color: white;");

        // Row 1: Greeting
        HBox greetingBox = createGreeting(user);
        
        // Row 2 setup
        VBox col1 = new VBox(10); col1.setPrefWidth(689);
        VBox streakContainer = new VBox();
        VBox moodGraphContainer = new VBox();
        col1.getChildren().addAll(streakContainer, moodGraphContainer);

        HBox row2 = new HBox(40);
        row2.setAlignment(Pos.BOTTOM_LEFT);
        row2.getChildren().addAll(col1, createCalendarWidget(), createMoodSelectorWidget());

        // Row 3 setup
        HBox row3 = new HBox(64);
        row3.setAlignment(Pos.TOP_LEFT);
        VBox dailyTargetsContainer = new VBox();
        row3.getChildren().addAll(createJournalHistoryWidget(entries), dailyTargetsContainer);

        content.getChildren().addAll(greetingBox, row2, row3);

        // Reactive logic for targets
        Runnable refreshTargetWidgets = () -> {
            streakContainer.getChildren().setAll(createStreakWidget());
            dailyTargetsContainer.getChildren().setAll(createDailyTargetsWidget());
            moodGraphContainer.getChildren().setAll(createMoodGraphWidget(entries));
        };

        refreshTargetWidgets.run();

        goalService.getGoalsCache().addListener((MapChangeListener<LocalDate, List<SelfCareGoal>>) change -> {
            Platform.runLater(refreshTargetWidgets);
        });

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: white;");
        return scrollPane;
    }

    private HBox createGreeting(User user) {
        HBox greetingBox = new HBox(5);
        greetingBox.setAlignment(Pos.CENTER_LEFT);
        Label greetingText = new Label("Halo, " + user.getFullName() + "!  Gimana perasaanmu hari ini? ");
        greetingText.setTextFill(Color.web("#74400F"));
        greetingText.setFont(Font.font("Outfit", FontWeight.MEDIUM, 50));
        Label emojiLabel = new Label("\uD83E\uDD14"); 
        emojiLabel.setStyle("-fx-font-size: 50px; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif;");
        greetingBox.getChildren().addAll(greetingText, emojiLabel);
        return greetingBox;
    }

    private HBox createStreakWidget() {
        HBox container = new HBox(56);
        container.setPrefSize(689, 137);
        container.setPadding(new Insets(32));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        container.setAlignment(Pos.CENTER);

        VBox streakCount = new VBox(0); streakCount.setAlignment(Pos.CENTER);
        HBox streakIcon = new HBox(8); streakIcon.setAlignment(Pos.CENTER);
        ImageView fireImage = new ImageView(new Image("file:img/beranda/streak_fire.png"));
        fireImage.setFitWidth(42.15); fireImage.setPreserveRatio(true);
        
        Label dayLabel = new Label(String.valueOf(goalService.getStreak()));
        dayLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 50));
        dayLabel.setTextFill(Color.web("#292929"));
        
        streakIcon.getChildren().addAll(dayLabel, fireImage);
        Label streakText = new Label("day streak");
        streakText.setFont(Font.font("Outfit", FontWeight.MEDIUM, 20));
        streakText.setTextFill(Color.web("#434343"));
        streakCount.getChildren().addAll(streakIcon, streakText);

        VBox targetSelfCare = new VBox(16); targetSelfCare.setAlignment(Pos.CENTER);
        Label targetTitle = new Label("Target Self-care");
        targetTitle.setFont(Font.font("Outfit", FontWeight.MEDIUM, 25));
        targetTitle.setTextFill(Color.web("#292929"));

        HBox daysRow = new HBox(24);
        String[] days = {"S", "M", "T", "W", "T", "F", "S"};
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            VBox dayCol = new VBox(4); dayCol.setAlignment(Pos.CENTER);
            StackPane dotPane = new StackPane(); dotPane.setStyle("-fx-cursor: hand;");
            Circle dot = new Circle(12);
            if (goalService.isDayCompleted(d)) {
                dot.setFill(Color.web("#82DD55"));
                Label check = new Label("✔"); check.setTextFill(Color.WHITE); check.setFont(Font.font("System", FontWeight.BOLD, 14));
                dotPane.getChildren().addAll(dot, check);
            } else {
                dot.setFill(Color.TRANSPARENT); dot.setStroke(Color.web("#82DD55")); dot.setStrokeWidth(1.6);
                dotPane.getChildren().add(dot);
            }
            dotPane.setOnMouseClicked(e -> goalService.toggleAllForDate(d));
            Label dayChar = new Label(days[d.getDayOfWeek().getValue() % 7]);
            dayChar.setFont(Font.font("Plus Jakarta Sans", FontWeight.SEMI_BOLD, 16));
            dayChar.setTextFill(Color.web("#434343"));
            dayCol.getChildren().addAll(dotPane, dayChar);
            daysRow.getChildren().add(dayCol);
        }
        targetSelfCare.getChildren().addAll(targetTitle, daysRow);
        container.getChildren().addAll(streakCount, targetSelfCare);
        return container;
    }

    private VBox createMoodGraphWidget(List<JournalEntry> entries) {
        VBox container = new VBox(8);
        container.setPrefHeight(333);
        container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Grafik Suasana Hati");
        title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 25));
        title.setTextFill(Color.web("#292929"));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox dateFilter = new HBox(10);
        dateFilter.setPadding(new Insets(8, 16));
        dateFilter.setStyle("-fx-background-color: #FFFAC1; -fx-border-color: #F1B900; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        ImageView calIcon = new ImageView(new Image("file:img/icons/calendar.png")); calIcon.setFitWidth(24); calIcon.setPreserveRatio(true);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMM", new Locale("id", "ID"));
        Label dateRange = new Label(start.format(dtf) + " - " + end.format(dtf) + " " + end.getYear());
        dateRange.setFont(Font.font("Outfit", FontWeight.LIGHT, 15));
        dateFilter.getChildren().addAll(calIcon, dateRange);
        header.getChildren().addAll(title, spacer, dateFilter);

        CategoryAxis xAxis = new CategoryAxis(); NumberAxis yAxis = new NumberAxis(0, 5, 1);
        yAxis.setTickLabelsVisible(false); yAxis.setTickMarkVisible(false); yAxis.setMinorTickVisible(false);
        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setPrefHeight(228); chart.setLegendVisible(false); chart.setCreateSymbols(true);
        chart.setHorizontalGridLinesVisible(false); chart.setVerticalGridLinesVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] dayShorts = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int i = 6; i >= 0; i--) {
            LocalDate d = end.minusDays(i);
            int moodValue = entries.stream().filter(e -> e.getDate().equals(d)).mapToInt(e -> 3).findFirst().orElse(0);
            series.getData().add(new XYChart.Data<>(dayShorts[d.getDayOfWeek().getValue() % 7], moodValue));
        }
        chart.getData().add(series);
        
        HBox xDays = new HBox(32); xDays.setAlignment(Pos.CENTER);
        for (int i = 6; i >= 0; i--) {
            Label l = new Label(dayShorts[end.minusDays(i).getDayOfWeek().getValue() % 7]);
            l.setFont(Font.font("Montserrat", FontWeight.MEDIUM, 16)); l.setTextFill(Color.web("#767676"));
            xDays.getChildren().add(l);
        }
        container.getChildren().addAll(header, chart, xDays);
        return container;
    }

    private VBox createCalendarWidget() {
        VBox container = new VBox(24);
        container.setPrefSize(500, 480); container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        container.setAlignment(Pos.TOP_CENTER);

        HBox header = new HBox(); header.setAlignment(Pos.CENTER);
        ImageView btnPrev = new ImageView(new Image("file:img/icons/arrow-left.png")); btnPrev.setFitWidth(32); btnPrev.setPreserveRatio(true); btnPrev.setStyle("-fx-cursor: hand;");
        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        Label monthLabel = new Label(); monthLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);
        ImageView btnNext = new ImageView(new Image("file:img/icons/arrow-right.png")); btnNext.setFitWidth(32); btnNext.setPreserveRatio(true); btnNext.setStyle("-fx-cursor: hand;");
        header.getChildren().addAll(btnPrev, s1, monthLabel, s2, btnNext);

        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(32); grid.setAlignment(Pos.CENTER);
        VBox.setVgrow(grid, Priority.ALWAYS);

        LocalDate[] currentMonth = { LocalDate.now().withDayOfMonth(1) };
        Runnable updateCalendar = () -> {
            grid.getChildren().clear();
            monthLabel.setText(currentMonth[0].getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("id", "ID")) + " " + currentMonth[0].getYear());
            String[] headers = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
            for (int i = 0; i < 7; i++) {
                Label h = new Label(headers[i]); h.setFont(Font.font("Montserrat", FontWeight.MEDIUM, 16));
                h.setTextFill(Color.web("#767676")); grid.add(h, i, 0);
            }
            int dayOfWeek = currentMonth[0].getDayOfWeek().getValue() % 7;
            int daysInMonth = currentMonth[0].lengthOfMonth();
            LocalDate today = LocalDate.now();
            int day = 1;
            for (int row = 1; row <= 6; row++) {
                for (int col = 0; col < 7; col++) {
                    if (row == 1 && col < dayOfWeek) continue;
                    if (day <= daysInMonth) {
                        StackPane cell = new StackPane(); cell.setMinWidth(40); cell.setAlignment(Pos.CENTER);
                        Label d = new Label(String.valueOf(day)); d.setFont(Font.font("Montserrat", FontWeight.NORMAL, 20));
                        if (currentMonth[0].getYear() == today.getYear() && currentMonth[0].getMonth() == today.getMonth() && day == today.getDayOfMonth()) {
                            Circle bg = new Circle(20, Color.web("#F1B900")); d.setTextFill(Color.WHITE);
                            d.setFont(Font.font("Montserrat", FontWeight.BOLD, 20)); cell.getChildren().addAll(bg, d);
                        } else { d.setTextFill(Color.web("#434343")); cell.getChildren().add(d); }
                        grid.add(cell, col, row); day++;
                    }
                }
            }
        };
        btnPrev.setOnMouseClicked(e -> { currentMonth[0] = currentMonth[0].minusMonths(1); updateCalendar.run(); });
        btnNext.setOnMouseClicked(e -> { currentMonth[0] = currentMonth[0].plusMonths(1); updateCalendar.run(); });
        updateCalendar.run();
        container.getChildren().addAll(header, grid);
        return container;
    }

    private VBox createMoodSelectorWidget() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(28, 32, 28, 32));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        container.setPrefSize(500, 480); container.setAlignment(Pos.TOP_LEFT);

        VBox titleArea = new VBox(4);
        Label title = new Label("Moodmu Hari Ini"); title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        title.setTextFill(Color.web("#292929"));
        Label subtitle = new Label("Pilih emosi yang paling mewakilimu saat ini!");
        subtitle.setFont(Font.font("Outfit", FontWeight.LIGHT, 20)); subtitle.setTextFill(Color.web("#434343"));
        subtitle.setWrapText(true); titleArea.getChildren().addAll(title, subtitle);

        ArrayList<String> fileNames = new ArrayList<>(Arrays.asList("angry.png", "bored.png", "confused.png", "excited.png", "guilty.png", "hurt.png", "hyperactive.png", "insecure.png", "joyful.png", "sensitive.png", "stressed.png", "tired.png"));
        ArrayList<String> moodNames = new ArrayList<>(Arrays.asList("Angry", "Bored", "Confused", "Excited", "Guilty", "Hurt", "Hyperactive", "Insecure", "Joyful", "Sensitive", "Stressed", "Tired"));
        int[] currentIndex = {3};

        VBox moodSelection = new VBox(8); moodSelection.setAlignment(Pos.CENTER);
        HBox selector = new HBox(20); selector.setAlignment(Pos.CENTER);
        ImageView btnL = new ImageView(new Image("file:img/icons/arrow-left.png")); btnL.setFitWidth(32); btnL.setPreserveRatio(true); btnL.setStyle("-fx-cursor: hand;");
        ImageView moodImg = new ImageView(new Image("file:img/emotions/" + fileNames.get(currentIndex[0]))); moodImg.setFitWidth(150); moodImg.setPreserveRatio(true);
        ImageView btnR = new ImageView(new Image("file:img/icons/arrow-right.png")); btnR.setFitWidth(32); btnR.setPreserveRatio(true); btnR.setStyle("-fx-cursor: hand;");
        selector.getChildren().addAll(btnL, moodImg, btnR);
        Label moodName = new Label(moodNames.get(currentIndex[0])); moodName.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        moodSelection.getChildren().addAll(selector, moodName);

        btnL.setOnMouseClicked(e -> { currentIndex[0] = (currentIndex[0] - 1 < 0) ? fileNames.size() - 1 : currentIndex[0] - 1; moodImg.setImage(new Image("file:img/emotions/" + fileNames.get(currentIndex[0]))); moodName.setText(moodNames.get(currentIndex[0])); });
        btnR.setOnMouseClicked(e -> { currentIndex[0] = (currentIndex[0] + 1 >= fileNames.size()) ? 0 : currentIndex[0] + 1; moodImg.setImage(new Image("file:img/emotions/" + fileNames.get(currentIndex[0]))); moodName.setText(moodNames.get(currentIndex[0])); });

        Button btnCatat = new Button("Catat");
        btnCatat.setStyle("-fx-background-color: #FFE341; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-padding: 16 64; -fx-background-radius: 10px; -fx-cursor: hand;");
        btnCatat.setMaxWidth(Double.MAX_VALUE);
        btnCatat.setOnAction(e -> mainRoot.setCenter(new EntryFormView(currentUser, () -> mainRoot.setCenter(getDashboardView(currentUser, mainRoot))).getView().getCenter()));
        
        Region s = new Region(); VBox.setVgrow(s, Priority.ALWAYS);
        container.getChildren().addAll(titleArea, moodSelection, s, btnCatat);
        return container;
    }

    private VBox createDailyTargetsWidget() {
        VBox container = new VBox(16);
        container.setPrefSize(500, 302); container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        Label title = new Label("Target Hari Ini"); title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        title.setTextFill(Color.web("#292929"));
        VBox list = new VBox(12);
        List<SelfCareGoal> goals = goalService.getGoalsForDate(LocalDate.now());
        if (goals.isEmpty()) { list.getChildren().add(new Label("Belum ada target hari ini.")); }
        else {
            for (SelfCareGoal goal : goals) {
                HBox item = new HBox(); item.setAlignment(Pos.CENTER_LEFT);
                Label label = new Label(goal.getTitle()); label.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
                label.setTextFill(Color.BLACK);
                if (goal.isCompleted()) label.setStyle("-fx-text-decoration: line-through;");
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                StackPane toggle = new StackPane(); toggle.setPrefSize(35, 35); toggle.setStyle("-fx-cursor: hand;");
                Circle circle = new Circle(17.5);
                if (goal.isCompleted()) {
                    circle.setFill(Color.web("#82DD55"));
                    Label check = new Label("✔"); check.setTextFill(Color.WHITE); check.setFont(Font.font("System", FontWeight.BOLD, 16));
                    toggle.getChildren().addAll(circle, check);
                } else {
                    circle.setFill(Color.TRANSPARENT); circle.setStroke(Color.web("#82DD55")); circle.setStrokeWidth(2);
                    toggle.getChildren().add(circle);
                }
                toggle.setOnMouseClicked(e -> goalService.toggleGoalStatus(goal));
                item.getChildren().addAll(label, sp, toggle); list.getChildren().add(item);
            }
        }
        container.getChildren().addAll(title, list);
        return container;
    }

    private VBox createJournalHistoryWidget(List<JournalEntry> entries) {
        VBox container = new VBox(24); container.setPrefWidth(1158);
        Label title = new Label("Riwayat Jurnal"); title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        title.setTextFill(Color.web("#292929"));
        HBox cards = new HBox(18);
        if (entries.isEmpty()) {
            Label empty = new Label("Belum ada jurnal."); empty.setFont(Font.font("Outfit", 20));
            cards.getChildren().add(empty);
        } else {
            for (int i = 0; i < Math.min(entries.size(), 2); i++) {
                JournalEntry e = entries.get(i);
                VBox card = new VBox(16); card.setStyle("-fx-background-color: #FFFAC1; -fx-padding: 28; -fx-background-radius: 20px;");
                card.setPrefWidth(480);
                Label d = new Label(e.getDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID")))); d.setFont(Font.font("Outfit", 25));
                Label t = new Label(e.getTitle()); t.setFont(Font.font("Outfit", FontWeight.MEDIUM, 22));
                Label c = new Label(e.getDescription()); c.setFont(Font.font("Outfit", FontWeight.LIGHT, 20)); c.setWrapText(true);
                card.getChildren().addAll(d, t, c);
                cards.getChildren().add(card);
            }
        }
        container.getChildren().addAll(title, cards);
        return container;
    }
}