package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
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

        // Filter bar
        HBox filterBar = new HBox(20);
        filterBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Button dateFilterButton = new Button("Cari Berdasarkan Tanggal");
        dateFilterButton.setStyle("-fx-background-color: #FFFAC1; -fx-text-fill: #292929; -fx-font-family: 'Outfit'; -fx-font-size: 18.75px; -fx-font-weight: 300; -fx-background-radius: 12.5px; -fx-padding: 10px 20px; -fx-cursor: hand; -fx-border-color: #A66502; -fx-border-width: 1.25px; -fx-border-radius: 12.5px;");
        dateFilterButton.setOnAction(e -> showDatePickerModal());

        filterBar.getChildren().add(dateFilterButton);

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

        VBox modalRoot = new VBox(20);
        modalRoot.setPadding(new Insets(20));
        modalRoot.setStyle("-fx-background-color: white;");

        Label instruction = new Label("Pilih tanggal untuk mencari jurnal:");
        instruction.setFont(Font.font("Outfit", FontWeight.NORMAL, 16));

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(selectedDateFilter != null ? selectedDateFilter : LocalDate.now());

        Button applyButton = new Button("Terapkan");
        applyButton.setStyle("-fx-background-color: #FFE341; -fx-text-fill: #74400F; -fx-font-family: 'Outfit'; -fx-font-size: 31.25px; -fx-background-radius: 12.5px; -fx-padding: 20px 40px; -fx-cursor: hand;");
        applyButton.setOnAction(e -> {
            selectedDateFilter = datePicker.getValue();
            if (selectedDateFilter != null) {
                List<JournalEntry> filteredEntries = controller.getEntriesByDate(user.getId(), selectedDateFilter);
                currentEntries = filteredEntries;
                displayEntryList(filteredEntries);
            }
            modal.close();
        });

        Button clearButton = new Button("Hapus Filter");
        clearButton.setStyle("-fx-background-color: #D6D6D6; -fx-text-fill: #292929; -fx-font-family: 'Outfit'; -fx-font-size: 16px; -fx-background-radius: 12.5px; -fx-padding: 10px 20px; -fx-cursor: hand;");
        clearButton.setOnAction(e -> {
            selectedDateFilter = null;
            loadEntries();
            modal.close();
        });

        HBox buttonBox = new HBox(10, applyButton, clearButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        modalRoot.getChildren().addAll(instruction, datePicker, buttonBox);

        modal.setScene(new javafx.scene.Scene(modalRoot, 400, 250));
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