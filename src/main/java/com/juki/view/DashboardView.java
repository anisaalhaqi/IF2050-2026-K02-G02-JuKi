package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.controller.GoalController;
import com.juki.controller.MoodController;
import com.juki.model.JournalEntry;
import com.juki.model.SelfCareGoal;
import com.juki.model.DailyMood;
import com.juki.model.User;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardView {

    private User currentUser;
    private BorderPane mainRoot;
    private MoodController moodController = new MoodController();

    public ScrollPane getDashboardView(User user, BorderPane root) {
        this.currentUser = user;
        this.mainRoot = root;
        EntryController entryController = new EntryController();
        List<JournalEntry> entries = entryController.getAllEntries(user.getId());
        
        GoalController goalController = new GoalController();
        List<SelfCareGoal> todayGoals = goalController.getGoalsByDate(LocalDate.now(), user.getId());

        VBox content = new VBox(48);
        content.setPadding(new Insets(52, 100, 52, 100));
        content.setStyle("-fx-background-color: white;");

        // Row 1: Greeting
        HBox greetingBox = new HBox(5);
        greetingBox.setAlignment(Pos.CENTER_LEFT);
        Label greetingText = new Label("Halo, " + user.getFullName() + "!  Gimana perasaanmu hari ini? ");
        greetingText.setTextFill(Color.web("#74400F"));
        greetingText.setFont(Font.font("Outfit", FontWeight.MEDIUM, 50));
        
        Label emojiLabel = new Label("\uD83E\uDD14"); // Thinking Face 🤔
        // Fallback font style for Linux/Windows/Mac emoji support
        emojiLabel.setStyle("-fx-font-size: 50px; -fx-font-family: 'Noto Color Emoji', 'Segoe UI Emoji', 'Apple Color Emoji', 'sans-serif';");
        emojiLabel.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        
        greetingBox.getChildren().addAll(greetingText, emojiLabel);

        // Row 2: Streak, Graph, Calendar, Mood Selector
        HBox row2 = new HBox(40);
        row2.setAlignment(Pos.BOTTOM_LEFT);

        // Column 1: Streak + Mood Graph
        VBox col1 = new VBox(10);
        col1.setPrefWidth(689);
        col1.getChildren().addAll(createStreakWidget(entries), createMoodGraphWidget(entries));

        // Column 2: Calendar
        VBox col2 = createCalendarWidget();

        // Column 3: Mood Selector
        VBox col3 = createMoodSelectorWidget();

        row2.getChildren().addAll(col1, col2, col3);

        // Row 3: Journal History + Daily Targets
        HBox row3 = new HBox(64);
        row3.setAlignment(Pos.TOP_LEFT);

        VBox journalHistory = createJournalHistoryWidget(entries);
        VBox dailyTargets = createDailyTargetsWidget(todayGoals);

        row3.getChildren().addAll(journalHistory, dailyTargets);

        content.getChildren().addAll(greetingBox, row2, row3);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: white;");
        
        return scrollPane;
    }

    private HBox createStreakWidget(List<JournalEntry> entries) {
        HBox container = new HBox(56);
        container.setPrefSize(689, 137);
        container.setPadding(new Insets(32));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        container.setAlignment(Pos.CENTER);

        // Simple streak calculation
        int streakCountVal = (int) entries.stream().map(JournalEntry::getDate).distinct().count();

        // Streak Day Count
        VBox streakCount = new VBox(0);
        streakCount.setAlignment(Pos.CENTER);
        
        HBox streakIcon = new HBox(8); // Agar sejajar ke samping dengan jarak 8px
        streakIcon.setAlignment(Pos.CENTER);
        
        ImageView fireImage = new ImageView(new Image("file:img/dashboard/streak_fire.png"));
        fireImage.setFitWidth(42.15);
        fireImage.setFitHeight(60.45);
        fireImage.setPreserveRatio(true);
        
        Label dayLabel = new Label(String.valueOf(streakCountVal));
        dayLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 50));
        dayLabel.setTextFill(Color.web("#292929"));
        
        streakIcon.getChildren().addAll(dayLabel, fireImage);
        
        Label streakText = new Label("day streak");
        streakText.setFont(Font.font("Outfit", FontWeight.MEDIUM, 20));
        streakText.setTextFill(Color.web("#434343"));
        streakCount.getChildren().addAll(streakIcon, streakText);

        // Target Self-care
        VBox targetSelfCare = new VBox(16);
        targetSelfCare.setAlignment(Pos.CENTER);
        Label targetTitle = new Label("Target Self-care");
        targetTitle.setFont(Font.font("Outfit", FontWeight.MEDIUM, 25));
        targetTitle.setTextFill(Color.web("#292929"));

        HBox daysRow = new HBox(24);
        String[] days = {"S", "M", "T", "W", "T", "F", "S"};
        // Use real activity for the last 7 days
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            boolean hasEntry = entries.stream().anyMatch(e -> e.getDate().equals(d));
            
            VBox dayCol = new VBox(4);
            dayCol.setAlignment(Pos.CENTER);
            
            StackPane dotPane = new StackPane();
            Circle dot = new Circle(12);
            if (hasEntry) {
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
        container.setPrefHeight(333); // Agar Graph (333) + Streak (137) + Spacing (10) = 480px
        container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Grafik Suasana Hati");
        title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 25));
        title.setTextFill(Color.web("#292929"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox dateFilter = new HBox(10);
        dateFilter.setPadding(new Insets(8, 16, 8, 16));
        dateFilter.setStyle("-fx-background-color: #FFFAC1; -fx-border-color: #F1B900; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        dateFilter.setAlignment(Pos.CENTER);
        
        ImageView calendarIcon = new ImageView(new Image("file:img/icons/calendar.png"));
        calendarIcon.setFitWidth(24);
        calendarIcon.setFitHeight(24);
        calendarIcon.setPreserveRatio(true);
        
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMM", new Locale("id", "ID"));
        Label dateRange = new Label(start.format(dtf) + " - " + end.format(dtf) + " " + end.getYear());
        dateRange.setFont(Font.font("Outfit", FontWeight.LIGHT, 15));
        dateFilter.getChildren().addAll(calendarIcon, dateRange);
        
        header.getChildren().addAll(title, spacer, dateFilter);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 5, 1);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        
        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setPrefHeight(228);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.getXAxis().setTickLabelsVisible(false);
        chart.getXAxis().setOpacity(0);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] dayShorts = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int i = 6; i >= 0; i--) {
            LocalDate d = end.minusDays(i);
            int moodValue = entries.stream()
                .filter(e -> e.getDate().equals(d))
                .mapToInt(e -> {
                    String cat = e.getCategory();
                    if (cat == null) return 3;
                    if (cat.contains("Excited") || cat.contains("Senang")) return 5;
                    if (cat.contains("Sedih")) return 2;
                    if (cat.contains("Marah")) return 1;
                    return 3;
                }).findFirst().orElse(0);
            
            series.getData().add(new XYChart.Data<>(dayShorts[d.getDayOfWeek().getValue() % 7], moodValue));
        }
        chart.getData().add(series);

        HBox daysRow = new HBox(32);
        daysRow.setAlignment(Pos.CENTER);
        for (int i = 6; i >= 0; i--) {
            LocalDate d = end.minusDays(i);
            Label dayLabel = new Label(dayShorts[d.getDayOfWeek().getValue() % 7]);
            dayLabel.setFont(Font.font("Montserrat", FontWeight.MEDIUM, 16));
            dayLabel.setTextFill(Color.web("#767676"));
            daysRow.getChildren().add(dayLabel);
        }

        container.getChildren().addAll(header, chart, daysRow);
        return container;
    }

    private VBox createCalendarWidget() {
        VBox container = new VBox(24);
        container.setPrefSize(500, 480);
        container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        container.setAlignment(Pos.TOP_CENTER);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        
        ImageView btnPrev = new ImageView(new Image("file:img/icons/arrow-left.png"));
        btnPrev.setFitWidth(32);
        btnPrev.setPreserveRatio(true);
        btnPrev.setStyle("-fx-cursor: hand;");
        
        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        Label monthLabel = new Label();
        monthLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);
        
        ImageView btnNext = new ImageView(new Image("file:img/icons/arrow-right.png"));
        btnNext.setFitWidth(32);
        btnNext.setPreserveRatio(true);
        btnNext.setStyle("-fx-cursor: hand;");
        
        header.getChildren().addAll(btnPrev, s1, monthLabel, s2, btnNext);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(32); // Jarak vertikal yang pas agar memenuhi kotak 480px
        grid.setAlignment(Pos.CENTER);
        VBox.setVgrow(grid, Priority.ALWAYS); // Memaksa grid mengisi sisa ruang vertikal ke bawah

        // State untuk melacak bulan yang sedang dilihat
        LocalDate[] currentMonth = { LocalDate.now().withDayOfMonth(1) };

        Runnable updateCalendar = () -> {
            grid.getChildren().clear(); // Bersihkan kalender lama
            
            // Tambahkan tahun ke label agar user tahu mereka ada di tahun berapa
            String monthName = currentMonth[0].getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("id", "ID"));
            monthLabel.setText(monthName + " " + currentMonth[0].getYear());

            String[] headers = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
            for (int i = 0; i < 7; i++) {
                Label h = new Label(headers[i]);
                h.setFont(Font.font("Montserrat", FontWeight.MEDIUM, 16));
                h.setTextFill(Color.web("#767676"));
                grid.add(h, i, 0);
            }

            int dayOfWeek = currentMonth[0].getDayOfWeek().getValue() % 7;
            int daysInMonth = currentMonth[0].lengthOfMonth();
            
            LocalDate today = LocalDate.now();
            boolean isCurrentMonth = (today.getYear() == currentMonth[0].getYear() && today.getMonth() == currentMonth[0].getMonth());

            // Load moods for this month
            List<DailyMood> monthlyMoods = moodController.getMoodsByMonth(currentUser.getId(), currentMonth[0]);
            Map<LocalDate, String> moodMap = monthlyMoods.stream().collect(Collectors.toMap(DailyMood::getDate, DailyMood::getMoodName));

            int day = 1;
            for (int row = 1; row <= 6; row++) {
                for (int col = 0; col < 7; col++) {
                    if (row == 1 && col < dayOfWeek) continue;
                    if (day <= daysInMonth) {
                        StackPane cell = new StackPane();
                        cell.setMinWidth(40);
                        cell.setAlignment(Pos.CENTER);

                        LocalDate cellDate = currentMonth[0].withDayOfMonth(day);
                        if (moodMap.containsKey(cellDate)) {
                            // Show mood icon
                            String moodName = moodMap.get(cellDate).toLowerCase();
                            try {
                                ImageView moodIcon = new ImageView(new Image("file:img/emotions/" + moodName + ".png"));
                                moodIcon.setFitWidth(30);
                                moodIcon.setPreserveRatio(true);
                                cell.getChildren().add(moodIcon);
                            } catch (Exception e) {
                                Label d = new Label(String.valueOf(day));
                                d.setFont(Font.font("Montserrat", FontWeight.NORMAL, 20));
                                cell.getChildren().add(d);
                            }
                        } else {
                            Label d = new Label(String.valueOf(day));
                            d.setFont(Font.font("Montserrat", FontWeight.NORMAL, 20));
                            
                            if (isCurrentMonth && day == today.getDayOfMonth()) {
                                d.setTextFill(Color.WHITE);
                                d.setFont(Font.font("Montserrat", FontWeight.BOLD, 20));
                                Circle bg = new Circle(20, Color.web("#F1B900"));
                                cell.getChildren().addAll(bg, d);
                            } else {
                                d.setTextFill(Color.web("#434343"));
                                cell.getChildren().add(d);
                            }
                        }
                        
                        grid.add(cell, col, row);
                        day++;
                    }
                }
            }
        };

        btnPrev.setOnMouseClicked(e -> {
            currentMonth[0] = currentMonth[0].minusMonths(1);
            updateCalendar.run();
        });

        btnNext.setOnMouseClicked(e -> {
            currentMonth[0] = currentMonth[0].plusMonths(1);
            updateCalendar.run();
        });

        // Render kalender untuk pertama kali
        updateCalendar.run();

        container.getChildren().addAll(header, grid);
        return container;
    }

    private VBox createMoodSelectorWidget() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(28, 32, 28, 32));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        container.setPrefSize(500, 480); // Samakan dengan kotak kalender
        container.setAlignment(Pos.TOP_LEFT);

        VBox titleArea = new VBox(4);
        Label title = new Label("Moodmu Hari Ini");
        title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        title.setTextFill(Color.web("#292929"));
        Label subtitle = new Label("Pilih emosi yang paling mewakilimu saat ini!");
        subtitle.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
        subtitle.setTextFill(Color.web("#434343"));
        subtitle.setWrapText(true);
        subtitle.setMinHeight(Region.USE_PREF_SIZE);
        titleArea.getChildren().addAll(title, subtitle);

        // Daftar nama file gambar sesuai yang kamu request
        ArrayList<String> fileNames = new ArrayList<>(Arrays.asList(
            "angry.png", "bored.png", "confused.png", "excited.png", 
            "guilty.png", "hurt.png", "hyperactive.png", "insecure.png", 
            "joyful.png", "sensitive.png", "stressed.png", "tired.png"
        ));
        
        // Daftar label teks emosi untuk ditampilkan di bawah gambar
        ArrayList<String> moodNames = new ArrayList<>(Arrays.asList(
            "Angry", "Bored", "Confused", "Excited", 
            "Guilty", "Hurt", "Hyperactive", "Insecure", 
            "Joyful", "Sensitive", "Stressed", "Tired"
        ));
        
        // Cek mood hari ini
        DailyMood todayMood = moodController.getMoodByDate(currentUser.getId(), LocalDate.now());
        
        // Menggunakan array untuk currentIndex agar nilainya bisa diubah di dalam fungsi click (lambda)
        int[] currentIndex = {3}; // Default: Excited
        if (todayMood != null) {
            String existingMood = todayMood.getMoodName();
            for (int i = 0; i < moodNames.size(); i++) {
                if (moodNames.get(i).equalsIgnoreCase(existingMood)) {
                    currentIndex[0] = i;
                    break;
                }
            }
        }

        VBox moodSelection = new VBox(8);
        moodSelection.setAlignment(Pos.CENTER);
        
        HBox selector = new HBox(20);
        selector.setPrefSize(387, 241.96);
        selector.setAlignment(Pos.CENTER);
        
        ImageView btnLeft = new ImageView(new Image("file:img/icons/arrow-left.png"));
        btnLeft.setFitWidth(32);
        btnLeft.setPreserveRatio(true);
        btnLeft.setStyle("-fx-cursor: hand;");
        
        ImageView moodImg = new ImageView(new Image("file:img/emotions/" + fileNames.get(currentIndex[0])));
        moodImg.setFitWidth(150);
        moodImg.setPreserveRatio(true);
        
        ImageView btnRight = new ImageView(new Image("file:img/icons/arrow-right.png"));
        btnRight.setFitWidth(32);
        btnRight.setPreserveRatio(true);
        btnRight.setStyle("-fx-cursor: hand;");
        
        selector.getChildren().addAll(btnLeft, moodImg, btnRight);
        
        Label moodName = new Label(moodNames.get(currentIndex[0]));
        moodName.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        moodName.setTextFill(Color.web("#292929"));
        moodSelection.getChildren().addAll(selector, moodName);

        // Logika Loop Prev (Ke Kiri)
        btnLeft.setOnMouseClicked(e -> {
            currentIndex[0]--;
            if (currentIndex[0] < 0) {
                currentIndex[0] = fileNames.size() - 1; // Balik ke gambar terakhir
            }
            moodImg.setImage(new Image("file:img/emotions/" + fileNames.get(currentIndex[0])));
            moodName.setText(moodNames.get(currentIndex[0]));
        });

        // Logika Loop Next (Ke Kanan)
        btnRight.setOnMouseClicked(e -> {
            currentIndex[0]++;
            if (currentIndex[0] >= fileNames.size()) {
                currentIndex[0] = 0; // Balik ke gambar pertama
            }
            moodImg.setImage(new Image("file:img/emotions/" + fileNames.get(currentIndex[0])));
            moodName.setText(moodNames.get(currentIndex[0]));
        });

        Button btnAction = new Button(todayMood == null ? "Catat" : "Ubah");
        if (todayMood == null) {
            try {
                ImageView notesIcon = new ImageView(new Image("file:img/icons/notes.png"));
                notesIcon.setFitWidth(32);
                notesIcon.setFitHeight(32);
                notesIcon.setPreserveRatio(true);
                btnAction.setGraphic(notesIcon);
                btnAction.setGraphicTextGap(10);
            } catch (Exception e) {}
            btnAction.setStyle("-fx-background-color: #FFE341; -fx-background-radius: 10px; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-padding: 16px 64px; -fx-cursor: hand;");
        } else {
            btnAction.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-radius: 10px; -fx-text-fill: black; -fx-font-family: 'Outfit'; -fx-font-size: 20px; -fx-padding: 16px 64px; -fx-cursor: hand;");
        }
        btnAction.setMaxWidth(Double.MAX_VALUE);
        btnAction.setPrefHeight(52);
        
        btnAction.setOnAction(e -> {
            String selectedMood = moodNames.get(currentIndex[0]);
            moodController.saveOrUpdateMood(currentUser.getId(), selectedMood, LocalDate.now());
            // Refresh Dashboard to update UI
            if (mainRoot != null) {
                mainRoot.setCenter(getDashboardView(currentUser, mainRoot));
            }
        });
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS); // Mendorong tombol catat ke dasar kotak
        
        container.getChildren().addAll(titleArea, moodSelection, spacer, btnAction);
        return container;
    }

    private VBox createJournalHistoryWidget(List<JournalEntry> entries) {
        VBox container = new VBox(24);
        container.setPrefWidth(1158);

        Label title = new Label("Riwayat Jurnal");
        title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        title.setTextFill(Color.web("#292929"));

        HBox cards = new HBox(18);
        if (entries.isEmpty()) {
            Label empty = new Label("Belum ada jurnal. Yuk mulai menulis!");
            empty.setFont(Font.font("Outfit", 20));
            cards.getChildren().add(empty);
        } else {
            for (int i = 0; i < Math.min(entries.size(), 2); i++) {
                JournalEntry entry = entries.get(i);
                cards.getChildren().add(createJournalCard(
                    entry.getDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"))), 
                    entry.getTitle(), 
                    entry.getDescription()
                ));
            }
        }

        container.getChildren().addAll(title, cards);
        return container;
    }

    private VBox createJournalCard(String date, String title, String content) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(28));
        card.setPrefWidth(480);
        card.setStyle("-fx-background-color: #FFFAC1; -fx-background-radius: 20px;");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Label dateLabel = new Label(date);
        dateLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 25));
        dateLabel.setTextFill(Color.web("#292929"));
        
        Circle dot = new Circle(5, Color.web("#FFA930"));
        header.getChildren().addAll(dateLabel, dot);

        VBox body = new VBox(8);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Outfit", FontWeight.MEDIUM, 22));
        titleLabel.setTextFill(Color.web("#434343"));
        
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxHeight(100);
        contentLabel.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
        contentLabel.setTextFill(Color.BLACK);
        body.getChildren().addAll(titleLabel, contentLabel);

        card.getChildren().addAll(header, body);
        return card;
    }

    private VBox createDailyTargetsWidget(List<SelfCareGoal> goals) {
        VBox container = new VBox(16);
        container.setPrefSize(500, 302);
        container.setPadding(new Insets(28));
        container.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-radius: 20px; -fx-background-radius: 20px;");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Circle iconPlaceholder = new Circle(35, Color.web("#D9D9D9"));
        VBox titleArea = new VBox(4);
        Label title = new Label("Target Hari Ini");
        title.setFont(Font.font("Outfit", FontWeight.MEDIUM, 30));
        title.setTextFill(Color.web("#292929"));
        Label subtitle = new Label("Peluk dirimu dengan kegiatan ini!");
        subtitle.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
        subtitle.setTextFill(Color.web("#434343"));
        titleArea.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconPlaceholder, titleArea);

        VBox list = new VBox(8);
        if (goals.isEmpty()) {
            list.getChildren().add(new Label("Belum ada target hari ini."));
        } else {
            for (SelfCareGoal goal : goals) {
                list.getChildren().add(createTargetItem(goal.getTitle(), goal.isCompleted()));
            }
        }

        container.getChildren().addAll(header, list);
        return container;
    }

    private HBox createTargetItem(String text, boolean completed) {
        HBox item = new HBox();
        item.setPrefHeight(40);
        item.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(text);
        label.setFont(Font.font("Outfit", FontWeight.LIGHT, 20));
        label.setTextFill(Color.BLACK);
        if (completed) {
            label.setStyle("-fx-text-decoration: line-through;");
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        ImageView statusImg = new ImageView();
        if (completed) {
            statusImg.setImage(new Image("file:img/selfcare/status_done.png"));
        } else {
            statusImg.setImage(new Image("file:img/selfcare/status_undone.png"));
        }
        statusImg.setFitWidth(35);
        statusImg.setFitHeight(35);
        statusImg.setPreserveRatio(true);
        
        item.getChildren().addAll(label, spacer, statusImg);
        return item;
    }
}
