package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class EntryListView {
    protected EntryController controller;
    private VBox view;
    private VBox listContainer;
    private User user;
    private List<JournalEntry> currentEntries;
    private LocalDate selectedDateFilter = null;
    private Consumer<Integer> onEntrySelected;

    public EntryListView(User user) {
        this(user, id -> System.out.println("Membuka detail jurnal dengan ID: " + id));
    }

    public EntryListView(User user, Consumer<Integer> onEntrySelected) {
        controller = new EntryController();
        this.user = user;
        this.onEntrySelected = onEntrySelected;
        
        view = new VBox(30);
        view.setPadding(new Insets(50, 100, 50, 100));
        
        Label title = new Label("Riwayat Jurnal Kamu ");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 40));
        title.setTextFill(Color.web("#8D1395"));
        
        ImageView notesIcon = new ImageView();
        try {
            notesIcon.setImage(new Image("file:img/icons/notes.png"));
            notesIcon.setFitHeight(40);
            notesIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Could not load notes icon: " + e.getMessage());
        }
        
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(title, notesIcon);

        // Fitur Filter Tanggal dari HEAD
        Button dateFilterButton = new Button("Cari Berdasarkan Tanggal");
        try {
            ImageView calendarIcon = new ImageView(new Image("file:img/icons/calendar.png"));
            calendarIcon.setFitWidth(20);
            calendarIcon.setFitHeight(20);
            dateFilterButton.setGraphic(calendarIcon);
        } catch (Exception e) {
            System.out.println("Icon tidak ditemukan, menggunakan teks saja.");
        }
        
        dateFilterButton.setContentDisplay(ContentDisplay.LEFT);
        dateFilterButton.setStyle("-fx-background-color: #FFFAC1; -fx-text-fill: #292929; -fx-font-family: 'Outfit'; -fx-font-size: 18.75px; -fx-font-weight: 300; -fx-background-radius: 12.5px; -fx-padding: 10px 20px; -fx-cursor: hand; -fx-border-color: #A66502; -fx-border-width: 1.25px; -fx-border-radius: 12.5px;");
        dateFilterButton.setOnAction(e -> showDatePickerModal());

        HBox filterBar = new HBox();
        filterBar.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        filterBar.getChildren().addAll(spacer, dateFilterButton);

        listContainer = new VBox(20);
        view.getChildren().addAll(titleBox, filterBar, listContainer);
        
        loadEntries();
    }

    private void showDatePickerModal() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        YearMonth[] monthShown = new YearMonth[]{selectedDateFilter != null ? YearMonth.from(selectedDateFilter) : YearMonth.now()};
        LocalDate[] activeDate = new LocalDate[]{selectedDateFilter != null ? selectedDateFilter : LocalDate.now()};

        // Backdrop (dimmed overlay, click outside to close)
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        backdrop.setPrefSize(1920, 1080);
        backdrop.setOnMouseClicked(e -> { if (e.getTarget() == backdrop) modal.close(); });

        VBox modalRoot = new VBox(24);
        modalRoot.setPadding(new Insets(39.2));
        modalRoot.setStyle("-fx-background-color: white; -fx-background-radius: 34.78px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 24, 0, 0, 4);");
        modalRoot.setPrefWidth(620.2);
        modalRoot.setMaxHeight(Region.USE_PREF_SIZE);

        // HEADER: title + X button
        HBox modalHeader = new HBox();
        modalHeader.setAlignment(Pos.CENTER_LEFT);
        Label modalTitle = new Label("Pilih Tanggal");
        modalTitle.setFont(Font.font("Outfit", FontWeight.NORMAL, 29.57));
        modalTitle.setTextFill(Color.web("#292929"));
        Region titleSpacer = new Region(); HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #767676; -fx-font-size: 20px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> modal.close());
        modalHeader.getChildren().addAll(modalTitle, titleSpacer, btnClose);

        Separator headerDivider = new Separator();

        Label monthLabel = new Label();
        monthLabel.setTextFill(Color.web("#292929"));
        monthLabel.setFont(Font.font("Outfit", FontWeight.NORMAL, 35));

        // Navigation Arrows
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

        // Weekdays Header
        String[] dayNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        HBox weekdayRow = new HBox(44.8);
        weekdayRow.setAlignment(Pos.CENTER_LEFT);
        for (String dayName : dayNames) {
            Label dayLabel = new Label(dayName);
            dayLabel.setTextFill(Color.web("#767676"));
            dayLabel.setFont(Font.font("Montserrat", FontWeight.MEDIUM, 22.4));
            dayLabel.setPrefWidth(44.8);
            dayLabel.setAlignment(Pos.CENTER);
            weekdayRow.getChildren().add(dayLabel);
        }

        VBox dateGrid = new VBox(56);
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
                    dayCell.setStyle("-fx-cursor: hand;");

                    if (!(week == 0 && weekday < startOffset || day > daysInMonth)) {
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
                    weekRow.getChildren().add(dateCellPane);
                }
                dateGrid.getChildren().add(weekRow);
            }
        };

        prevArrow.setOnMouseClicked(e -> { monthShown[0] = monthShown[0].minusMonths(1); rebuildCalendar[0].run(); });
        nextArrow.setOnMouseClicked(e -> { monthShown[0] = monthShown[0].plusMonths(1); rebuildCalendar[0].run(); });

        Button applyButton = new Button("Terapkan");
        applyButton.setStyle("-fx-background-color: #FFE341; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 31.25px; -fx-background-radius: 12.5px; -fx-padding: 10px 40px; -fx-cursor: hand;");
        applyButton.setOnAction(e -> {
            selectedDateFilter = activeDate[0];
            if (selectedDateFilter != null) {
                currentEntries = controller.getEntriesByDate(user.getId(), selectedDateFilter);
                displayEntryList(currentEntries);
            }
            modal.close();
        });

        Button clearButton = new Button("Hapus Filter");
        clearButton.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #292929; -fx-font-family: 'Outfit'; -fx-font-size: 31.25px; -fx-background-radius: 12.5px; -fx-padding: 10px 40px; -fx-cursor: hand; -fx-border-color: #D6D6D6;");
        clearButton.setOnAction(e -> {
            selectedDateFilter = null;
            loadEntries();
            modal.close();
        });

        HBox actionRow = new HBox(20, clearButton, applyButton);
        actionRow.setAlignment(Pos.CENTER);

        modalRoot.getChildren().addAll(modalHeader, headerDivider, monthHeader, weekdayRow, dateGrid, actionRow);
        rebuildCalendar[0].run();

        backdrop.getChildren().add(modalRoot); StackPane.setAlignment(modalRoot, Pos.CENTER);
        javafx.scene.Scene scene = new javafx.scene.Scene(backdrop);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        modal.setScene(scene);
        modal.show(); modal.centerOnScreen();
    }

    public ScrollPane getView() {
        ScrollPane scrollPane = new ScrollPane(view);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #FDF3FF;");
        view.setStyle("-fx-background-color: #FDF3FF;");
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
            HBox card = new HBox();
            card.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-border-color: #D6D6D6; -fx-border-width: 1px; -fx-border-radius: 20px; -fx-padding: 32px; -fx-cursor: hand;");
            card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            VBox textContent = new VBox(24);
            HBox.setHgrow(textContent, Priority.ALWAYS);

            Label titleLbl = new Label(entry.getTitle() != null ? entry.getTitle() : "Tanpa Judul");
            titleLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 36));
            titleLbl.setTextFill(Color.BLACK);
            titleLbl.setWrapText(true);

            Label descLbl = new Label(entry.getDescription() != null ? entry.getDescription() : "");
            descLbl.setFont(Font.font("Outfit", FontWeight.NORMAL, 18));
            descLbl.setTextFill(Color.web("#434343"));
            descLbl.setWrapText(true);

            HBox metaRow = new HBox(16);
            metaRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            String dateStr = entry.getDate() != null ?
                entry.getDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id"))) : "Tanpa Tanggal";
            if (entry.getTime() != null) {
                dateStr += " " + entry.getTime().format(java.time.format.DateTimeFormatter.ofPattern("HH.mm"));
            }
            Label dateLbl = new Label(dateStr);
            dateLbl.setFont(Font.font("Outfit", FontWeight.LIGHT, 16));
            dateLbl.setTextFill(Color.web("#767676"));

            metaRow.getChildren().add(dateLbl);
            if (entry.getCategory() != null && !entry.getCategory().isEmpty()) {
                Label tag = new Label(entry.getCategory());
                tag.setStyle("-fx-background-color: #FFFAC1; -fx-border-color: #A66502; -fx-border-width: 0.56px; -fx-border-radius: 55.56px; -fx-background-radius: 55.56px; -fx-padding: 4px 17px; -fx-font-family: 'Outfit'; -fx-font-size: 14px; -fx-font-weight: 300;");
                metaRow.getChildren().add(tag);
            }

            textContent.getChildren().addAll(titleLbl, descLbl, metaRow);

            // Thumbnail (if photo exists)
            if (entry.getPhotos() != null && !entry.getPhotos().isEmpty()) {
                String photoPath = entry.getPhotos().get(0).getFilePath();
                if (photoPath != null && !photoPath.isEmpty()) {
                    try {
                        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(
                            new javafx.scene.image.Image("file:" + photoPath));
                        iv.setFitWidth(220); iv.setFitHeight(160); iv.setPreserveRatio(false);
                        VBox thumb = new VBox(iv);
                        thumb.setStyle("-fx-background-radius: 16px;");
                        thumb.setPrefSize(220, 160); thumb.setMinSize(220, 160); thumb.setMaxSize(220, 160);
                        card.getChildren().addAll(textContent, thumb);
                    } catch (Exception ex) {
                        card.getChildren().add(textContent);
                    }
                } else {
                    card.getChildren().add(textContent);
                }
            } else {
                card.getChildren().add(textContent);
            }

            card.setOnMouseClicked(e -> selectEntry(entry.getId()));
            listContainer.getChildren().add(card);
        }
    }

    public void displayEmptyMessage() {
        listContainer.getChildren().clear();
        Label emptyLbl = new Label("Belum ada jurnal yang ditulis. Yuk, tulis jurnal pertamamu hari ini!");
        emptyLbl.setFont(Font.font("Outfit", 18));
        emptyLbl.setTextFill(Color.GRAY);
        listContainer.getChildren().add(emptyLbl);
    }

    public void selectEntry(Integer id) {
        if (onEntrySelected != null) {
            onEntrySelected.accept(id);
        } else {
            System.out.println("Membuka detail jurnal dengan ID: " + id);
        }
    }
}