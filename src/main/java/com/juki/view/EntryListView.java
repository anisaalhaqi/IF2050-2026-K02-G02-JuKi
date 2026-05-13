package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class EntryListView {
    protected EntryController controller;
    private VBox view;
    private VBox listContainer;
    private User user;
    private List<JournalEntry> currentEntries;
    private LocalDate selectedDateFilter = null;

    public EntryListView(User user) {
        this(user, null);
    }

    public EntryListView(User user, List<JournalEntry> entriesToDisplay) {
        controller = new EntryController();
        this.user = user;
        view = new VBox(30);
        view.setPadding(new Insets(50, 100, 50, 100));
        
        Label title = new Label("Riwayat Jurnal Kamu \uD83D\uDCD6");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 40));
        title.setTextFill(Color.web("#8D1395"));

        Button dateFilterButton = new Button("Cari Berdasarkan Tanggal");
        ImageView calendarIcon = new ImageView(new Image("file:img/icons/calendar.png"));
        calendarIcon.setFitWidth(20);
        calendarIcon.setFitHeight(20);
        dateFilterButton.setGraphic(calendarIcon);
        dateFilterButton.setContentDisplay(ContentDisplay.LEFT);
        dateFilterButton.setStyle("-fx-background-color: #FFFAC1; -fx-text-fill: #292929; -fx-font-family: 'Outfit'; -fx-font-size: 18.75px; -fx-font-weight: 300; -fx-background-radius: 12.5px; -fx-padding: 10px 20px; -fx-cursor: hand; -fx-border-color: #A66502; -fx-border-width: 1.25px; -fx-border-radius: 12.5px; -fx-text-alignment: center;");
        dateFilterButton.setOnAction(e -> showDatePickerModal());

        HBox filterBar = new HBox();
        filterBar.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        filterBar.getChildren().addAll(spacer, dateFilterButton);

        listContainer = new VBox(20);
        
        view.getChildren().addAll(title, filterBar, listContainer);
        
        if (entriesToDisplay == null) {
            loadEntries();
        } else if (entriesToDisplay.isEmpty()) {
            displayEmptyMessage();
        } else {
            currentEntries = entriesToDisplay;
            displayEntryList(entriesToDisplay);
        }
    }

    private void showDatePickerModal() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Pilih Tanggal");

        YearMonth[] monthShown = new YearMonth[]{selectedDateFilter != null ? YearMonth.from(selectedDateFilter) : YearMonth.now()};
        LocalDate[] activeDate = new LocalDate[]{selectedDateFilter != null ? selectedDateFilter : LocalDate.now()};

        VBox modalRoot = new VBox(52);
        modalRoot.setPadding(new Insets(39.2));
        modalRoot.setStyle("-fx-background-color: white; -fx-border-color: #D6D6D6; -fx-border-width: 1.4; -fx-border-radius: 28; -fx-background-radius: 28;");
        modalRoot.setPrefWidth(620.2);

        Label monthLabel = new Label();
        monthLabel.setTextFill(Color.web("#292929"));
        monthLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 35));

        ImageView prevArrow = new ImageView(new Image("file:img/icons/arrow-left.png"));
        prevArrow.setFitWidth(44.8);
        prevArrow.setFitHeight(44.8);
        prevArrow.setStyle("-fx-cursor: hand;");

        ImageView nextArrow = new ImageView(new Image("file:img/icons/arrow-left.png"));
        nextArrow.setFitWidth(44.8);
        nextArrow.setFitHeight(44.8);
        nextArrow.setRotate(180);
        nextArrow.setStyle("-fx-cursor: hand;");

        HBox monthHeader = new HBox(20, prevArrow, monthLabel, nextArrow);
        monthHeader.setAlignment(Pos.CENTER);
        monthHeader.setPrefWidth(620.2);

        String[] dayNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        HBox weekdayRow = new HBox(44.8);
        weekdayRow.setAlignment(Pos.CENTER_LEFT);
        for (String dayName : dayNames) {
            Label dayLabel = new Label(dayName);
            dayLabel.setTextFill(Color.web("#767676"));
            dayLabel.setFont(Font.font("Montserrat", FontWeight.MEDIUM, 22.4));
            dayLabel.setPrefWidth(44.8);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setStyle("-fx-cursor: hand;");
            weekdayRow.getChildren().add(dayLabel);
        }

        VBox dateGrid = new VBox(56);
        dateGrid.setAlignment(Pos.TOP_LEFT);

        Runnable[] rebuildCalendar = new Runnable[1];
        rebuildCalendar[0] = () -> {
            monthLabel.setText(monthShown[0].format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)));
            dateGrid.getChildren().clear();

            LocalDate firstOfMonth = monthShown[0].atDay(1);
            int startOffset = (firstOfMonth.getDayOfWeek().getValue() % 7);
            int daysInMonth = monthShown[0].lengthOfMonth();
            int day = 1;

            for (int week = 0; week < 6; week++) {
                HBox weekRow = new HBox(44.8);
                weekRow.setAlignment(Pos.CENTER_LEFT);
                for (int weekday = 0; weekday < 7; weekday++) {
                    StackPane dateCellPane = new StackPane();
                    dateCellPane.setPrefSize(44.8, 33.6);

                    Label dayCell = new Label();
                    dayCell.setTextFill(Color.web("#434343"));
                    dayCell.setFont(Font.font("Montserrat", FontWeight.NORMAL, 28));
                    dayCell.setAlignment(Pos.CENTER);
                    dayCell.setStyle("-fx-cursor: hand;");

                    if (week == 0 && weekday < startOffset || day > daysInMonth) {
                        dayCell.setText(" ");
                        dateCellPane.setOpacity(0);
                    } else {
                        dayCell.setText(String.valueOf(day));
                        LocalDate current = monthShown[0].atDay(day);
                        if (activeDate[0] != null && current.equals(activeDate[0])) {
                            Circle yellowBg = new Circle(20, Color.web("#FFE341"));
                            dateCellPane.getChildren().addAll(yellowBg, dayCell);
                        } else {
                            dateCellPane.getChildren().add(dayCell);
                        }
                        dateCellPane.setOnMouseClicked(e -> {
                            activeDate[0] = current;
                            rebuildCalendar[0].run();
                        });
                        day++;
                    }
                    if (dateCellPane.getChildren().isEmpty()) {
                        dateCellPane.getChildren().add(dayCell);
                    }
                    weekRow.getChildren().add(dateCellPane);
                }
                dateGrid.getChildren().add(weekRow);
            }
        };

        prevArrow.setOnMouseClicked(e -> {
            monthShown[0] = monthShown[0].minusMonths(1);
            rebuildCalendar[0].run();
        });
        nextArrow.setOnMouseClicked(e -> {
            monthShown[0] = monthShown[0].plusMonths(1);
            rebuildCalendar[0].run();
        });

        Button applyButton = new Button("Terapkan");
        applyButton.setStyle("-fx-background-color: #FFE341; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 31.25px; -fx-font-weight: 400; -fx-background-radius: 12.5px; -fx-padding: 20px 40px; -fx-cursor: hand;");
        applyButton.setOnAction(e -> {
            selectedDateFilter = activeDate[0];
            if (selectedDateFilter != null) {
                List<JournalEntry> filteredEntries = controller.getEntriesByDate(user.getId(), selectedDateFilter);
                currentEntries = filteredEntries;
                displayEntryList(filteredEntries);
            }
            modal.close();
        });

        Button clearButton = new Button("Hapus Filter");
        clearButton.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #292929; -fx-font-family: 'Outfit'; -fx-font-size: 31.25px; -fx-font-weight: 400; -fx-background-radius: 12.5px; -fx-padding: 20px 40px; -fx-cursor: hand; -fx-border-color: #D6D6D6; -fx-border-width: 1px; -fx-border-radius: 12.5px;");
        clearButton.setOnAction(e -> {
            selectedDateFilter = null;
            loadEntries();
            modal.close();
        });

        HBox actionRow = new HBox(20, clearButton, applyButton);
        actionRow.setAlignment(Pos.CENTER);

        modalRoot.getChildren().addAll(monthHeader, weekdayRow, dateGrid, actionRow);
        rebuildCalendar[0].run();

        modal.setScene(new javafx.scene.Scene(modalRoot));
        modal.show();
    }



    public ScrollPane getView() {
        ScrollPane scrollPane = new ScrollPane(view);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #FDF3FF;");
        return scrollPane;
    }

    private void loadEntries() {
        currentEntries = controller.getAllEntries(user.getId());
        if (currentEntries.isEmpty()) {
            displayEmptyMessage();
        } else {
            displayEntryList(currentEntries);
        }
    }

    public void displayEntryList(List<JournalEntry> entries) {
        listContainer.getChildren().clear();
        for (JournalEntry entry : entries) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15px; -fx-padding: 25px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-cursor: hand;");
            
            String dateStr = entry.getDate() != null ? entry.getDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id"))) : "Tanpa Tanggal";
            Label dateLbl = new Label(dateStr + " • " + (entry.getCategory() != null ? entry.getCategory() : "Umum"));
            dateLbl.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
            dateLbl.setTextFill(Color.GRAY);

            Label titleLbl = new Label(entry.getTitle() != null ? entry.getTitle() : "Tanpa Judul");
            titleLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 22));
            titleLbl.setTextFill(Color.web("#74400F"));
            titleLbl.setWrapText(true);
            titleLbl.setMaxWidth(480);

            Label descLbl = new Label(entry.getDescription() != null ? entry.getDescription() : "");
            descLbl.setFont(Font.font("Outfit", FontWeight.NORMAL, 20));
            descLbl.setTextFill(Color.BLACK);
            descLbl.setWrapText(true);
            descLbl.setMaxWidth(480);

            card.getChildren().addAll(dateLbl, titleLbl, descLbl);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15px; -fx-padding: 25px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-cursor: hand;");
            card.setOnMouseClicked(e -> selectEntry(entry.getId()));

            listContainer.getChildren().add(card);
        }
    }

    public void displayEmptyMessage() {
        Label emptyLbl = new Label("Belum ada jurnal yang ditulis. Yuk, tulis jurnal pertamamu hari ini!");
        emptyLbl.setFont(Font.font("Outfit", 18));
        emptyLbl.setTextFill(Color.GRAY);
        listContainer.getChildren().add(emptyLbl);
    }

    public void selectEntry(Integer id) {
        System.out.println("Membuka detail jurnal dengan ID: " + id);
    }
}