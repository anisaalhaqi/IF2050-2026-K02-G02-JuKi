package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Consumer;

public class EntryListView {
    protected EntryController controller;
    private VBox view;
    private VBox listContainer;
    private User user;
    private Consumer<Integer> onEntrySelected;

    public EntryListView(User user) {
        this(user, id -> {});
    }

    public EntryListView(User user, Consumer<Integer> onEntrySelected) {
        controller = new EntryController();
        this.user = user;
        this.onEntrySelected = onEntrySelected;
        view = new VBox(30);
        view.setPadding(new Insets(50, 100, 50, 100));
        
        Label title = new Label("Riwayat Jurnal Kamu \uD83D\uDCD6");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 40));
        title.setTextFill(Color.web("#8D1395"));

        listContainer = new VBox(20);
        
        view.getChildren().addAll(title, listContainer);
        
        loadEntries();
    }

    public ScrollPane getView() {
        ScrollPane scrollPane = new ScrollPane(view);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #FDF3FF;");
        return scrollPane;
    }

    private void loadEntries() {
        List<JournalEntry> entries = controller.getAllEntries(user.getId());
        if (entries.isEmpty()) {
            displayEmptyMessage();
        } else {
            displayEntryList(entries);
        }
    }

    public void displayEntryList(List<JournalEntry> entries) {
        listContainer.getChildren().clear();
        for (JournalEntry entry : entries) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15px; -fx-padding: 25px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-cursor: hand;");
            
            String dateStr = entry.getDate() != null ? entry.getDate().toString() : "Tanpa Tanggal";
            Label dateLbl = new Label(dateStr + " • " + (entry.getCategory() != null ? entry.getCategory() : "Umum"));
            dateLbl.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
            dateLbl.setTextFill(Color.GRAY);

            Label titleLbl = new Label(entry.getTitle() != null ? entry.getTitle() : "Tanpa Judul");
            titleLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 22));
            titleLbl.setTextFill(Color.web("#74400F"));

            card.getChildren().addAll(dateLbl, titleLbl);
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
        if (onEntrySelected != null) {
            onEntrySelected.accept(id);
        } else {
            System.out.println("Membuka detail jurnal dengan ID: " + id);
        }
    }
}