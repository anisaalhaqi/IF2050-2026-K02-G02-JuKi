package com.juki.view;

import com.juki.controller.SearchController;
import com.juki.model.JournalEntry;
import com.juki.model.SearchFilter;
import java.util.List;

public class SearchView {
    private SearchController controller;

    public void displaySearchForm() {
        // Render UI input pencarian
    }

    public SearchFilter getSearchInput() {
        return new SearchFilter(null, null, null);
    }

    public void displaySearchResult(List<JournalEntry> results) {
        // Render hasil pencarian ke tabel/list
    }

    public void displayEmptyResultMessage() {
        System.out.println("Hasil pencarian tidak ditemukan.");
    }

    public void showErrorMessage(String message) {
        System.err.println("Error: " + message);
    }
}