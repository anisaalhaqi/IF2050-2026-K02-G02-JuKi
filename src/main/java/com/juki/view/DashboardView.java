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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardView {

    private User currentUser;
    private BorderPane mainRoot;
    private final GoalService goalService = GoalService.getInstance();
    private final EntryController entryController = new EntryController();

    public ScrollPane getDashboardView(User user, BorderPane root) {
        this.currentUser = user;
        this.mainRoot = root;
        
        List<JournalEntry> entries = entryController.getAllEntries(user.getId());

        VBox content = new VBox(48);
        content.setPadding(new Insets(52, 100, 52, 100));
        content.setStyle("-fx-background-color: white;");

        // UI Components
        HBox greetingBox = createGreeting(user);
        
        VBox col1 = new VBox(10);
        col1.setPrefWidth(689);
        col1.getChildren().addAll(createStreakWidget(), createMoodGraphWidget(entries));
        
        HBox row2 = new HBox(40);
        row2.setAlignment(Pos.BOTTOM_LEFT);
        row2.getChildren().addAll(col1, createCalendarWidget(), createMoodSelectorWidget());

        HBox row3 = new HBox(64);
        row3.setAlignment(Pos.TOP_LEFT);
        row3.getChildren().addAll(createJournalHistoryWidget(entries), createDailyTargetsWidget());

        content.getChildren().addAll(greetingBox, row2, row3);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: white;");
        
        // Setup reactive updates
        goalService.getGoalsCache().addListener((MapChangeListener<LocalDate, List<SelfCareGoal>>) change -> {
            // When any date's goals change, we might need to refresh parts of the dashboard
            // For simplicity in this demo, we re-render the rows that depend on targets
            // In a production app, use granular property bindings
            content.getChildren().clear();
            content.getChildren().addAll(createGreeting(user), row2, row3);
            
            // Re-render Row 2 and Row 3 children
            col1.getChildren().setAll(createStreakWidget(), createMoodGraphWidget(entries));
            row3.getChildren().setAll(createJournalHistoryWidget(entries), createDailyTargetsWidget());
        });

        return scrollPane;
    }

    private HBox createGreeting(User user) {
        HBox greetingBox = new HBox(5);
        greetingBox.setAlignment(Pos.CENTER_LEFT);
        Label greetingText = new Label("Halo, " + user.getFullName() + "!  Gimana perasaanmu hari ini? ");
        greetingText.setTextFill(Color.web("#74400F"));
        greetingText.setFont(Font.font("Outfit", FontWeight.MEDIUM, 50));
        Label emojiLabel = new Label("\uD83E\uDD14"); 
        emojiLabel.setStyle("-fx-font-size: 50px; -fx-font-family: 'Noto Color Emoji', 'Segoe UI Emoji', 'Apple Color Emoji', 'sans-serif';");
        greetingBox.getChildren().addAll(greetingText, emojiLabel);
        return greetingBox;
    }

    private HBox createStreakWidget() {
        HBox container = new HBox(56);
        container.setPrefSize(689, 137);
        container.setPadding(new Insets(32));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        container.setAlignment(Pos.CENTER);

        // Streak Count bound to GoalService
        VBox streakCount = new VBox(0);
        streakCount.setAlignment(Pos.CENTER);
        HBox streakIcon = new HBox(8);
        streakIcon.setAlignment(Pos.CENTER);
        ImageView fireImage = new ImageView(new Image("file:img/beranda/streak_fire.png"));
        fireImage.setFitWidth(42.15); fireImage.setPreserveRatio(true);
        
        Label dayLabel = new Label();
        dayLabel.textProperty().bind(goalService.streakProperty().asString());
        dayLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 50));
        
        streakIcon.getChildren().addAll(dayLabel, fireImage);
        Label streakText = new Label("day streak");
        streakText.setFont(Font.font("Outfit", FontWeight.MEDIUM, 20));
        streakCount.getChildren().addAll(streakIcon, streakText);

        // Daily Indicators [T][F][S][S][M][T][W]
        VBox targetSelfCare = new VBox(16);
        targetSelfCare.setAlignment(Pos.CENTER);
        Label targetTitle = new Label("Target Self-care");
        targetTitle.setFont(Font.font("Outfit", FontWeight.MEDIUM, 25));
        
        HBox daysRow = new HBox(24);
        String[] days = {"S", "M", "T", "W", "T", "F", "S"};
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            VBox dayCol = new VBox(4);
            dayCol.setAlignment(Pos.CENTER);
            
            StackPane dotPane = new StackPane();
            dotPane.setStyle("-fx-cursor: hand;");
            Circle dot = new Circle(12);
            
            if (goalService.isDayCompleted(d)) {
                dot.setFill(Color.web("#82DD55"));
                Label check = new Label("✔");
                check.setTextFill(Color.WHITE);
                check.setFont(Font.font("System", FontWeight.BOLD, 14));
                dotPane.getChildren().addAll(dot, check);
            } else {
                dot.setFill(Color.TRANSPARENT);
                dot.setStroke(Color.web("#82DD55"));
                dot.setStrokeWidth(1.6);
                dotPane.getChildren().add(dot);
            }
            
            dotPane.setOnMouseClicked(e -> {
                goalService.toggleAllForDate(d);
            });

            Label dayChar = new Label(days[d.getDayOfWeek().getValue() % 7]);
            dayChar.setFont(Font.font("Plus Jakarta Sans", FontWeight.SEMI_BOLD, 16));
            dayCol.getChildren().addAll(dotPane, dayChar);
            daysRow.getChildren().add(dayCol);
        }
        
        targetSelfCare.getChildren().addAll(targetTitle, daysRow);
        container.getChildren().addAll(streakCount, targetSelfCare);
        return container;
    }

    private VBox createDailyTargetsWidget() {
        VBox container = new VBox(16);
        container.setPrefSize(500, 302);
        container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        
        VBox titleArea = new VBox(4);
        Label title = new Label("Target Hari Ini");
        title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        Label subtitle = new Label("Peluk dirimu dengan kegiatan ini!");
        subtitle.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
        titleArea.getChildren().addAll(title, subtitle);
        container.getChildren().add(titleArea);

        VBox list = new VBox(8);
        List<SelfCareGoal> goals = goalService.getGoalsForDate(LocalDate.now());
        if (goals.isEmpty()) {
            list.getChildren().add(new Label("Belum ada target hari ini."));
        } else {
            for (SelfCareGoal goal : goals) {
                HBox item = new HBox();
                item.setPrefHeight(40);
                item.setAlignment(Pos.CENTER_LEFT);
                Label label = new Label(goal.getTitle());
                label.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
                if (goal.isCompleted()) label.setStyle("-fx-text-decoration: line-through;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                StackPane toggle = new StackPane();
                toggle.setPrefSize(35, 35);
                toggle.setStyle("-fx-cursor: hand;");
                Circle circle = new Circle(17.5);
                if (goal.isCompleted()) {
                    circle.setFill(Color.web("#82DD55"));
                } else {
                    circle.setFill(Color.TRANSPARENT);
                    circle.setStroke(Color.web("#82DD55"));
                    circle.setStrokeWidth(2);
                }
                toggle.getChildren().add(circle);
                toggle.setOnMouseClicked(e -> goalService.toggleGoalStatus(goal));
                
                item.getChildren().addAll(label, spacer, toggle);
                list.getChildren().add(item);
            }
        }
        container.getChildren().add(list);
        return container;
    }

    // Static widgets (don't change often)
    private VBox createMoodGraphWidget(List<JournalEntry> entries) {
        VBox container = new VBox(8);
        container.setPrefHeight(333);
        container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        Label title = new Label("Grafik Suasana Hati");
        title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 25));
        container.getChildren().add(title);
        return container;
    }

    private VBox createCalendarWidget() {
        VBox container = new VBox(24);
        container.setPrefSize(500, 480);
        container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        container.setAlignment(Pos.TOP_CENTER);
        Label monthLabel = new Label(LocalDate.now().getMonth().name() + " " + LocalDate.now().getYear());
        monthLabel.setFont(Font.font("Outfit", 25));
        container.getChildren().add(monthLabel);
        return container;
    }

    private VBox createMoodSelectorWidget() {
        VBox container = new VBox(24);
        container.setPrefSize(500, 480);
        container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        Label title = new Label("Moodmu Hari Ini");
        title.setFont(Font.font("Outfit", 30));
        Button btnCatat = new Button("Catat");
        btnCatat.setStyle("-fx-background-color: #FFE341; -fx-padding: 10 40; -fx-background-radius: 10;");
        btnCatat.setOnAction(e -> {
            mainRoot.setCenter(new EntryFormView(currentUser, () -> mainRoot.setCenter(getDashboardView(currentUser, mainRoot))).getView().getCenter());
        });
        container.getChildren().addAll(title, btnCatat);
        return container;
    }

    private VBox createJournalHistoryWidget(List<JournalEntry> entries) {
        VBox container = new VBox(24);
        container.setPrefWidth(1158);
        Label title = new Label("Riwayat Jurnal");
        title.setFont(Font.font("Outfit", 30));
        HBox cards = new HBox(18);
        for (int i = 0; i < Math.min(entries.size(), 2); i++) {
            JournalEntry e = entries.get(i);
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: #FFFAC1; -fx-padding: 20; -fx-background-radius: 20;");
            card.setPrefWidth(480);
            card.getChildren().addAll(new Label(e.getDate().toString()), new Label(e.getTitle()), new Label(e.getDescription()));
            cards.getChildren().add(card);
        }
        container.getChildren().addAll(title, cards);
        return container;
    }
}