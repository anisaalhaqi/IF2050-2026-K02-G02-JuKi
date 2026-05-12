package com.juki.controller;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import com.juki.model.SearchFilter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SearchController {
    private final EntryController entryController;

    public SearchController() {
        this.entryController = new EntryController();
    }

    public List<JournalEntry> searchEntries(SearchFilter filter, int userId) {
        if (filter == null) {
            return new ArrayList<>();
        }
        filter.applyFilter();

        String keyword = filter.getKeyword();
        LocalDate date = filter.getDate();

        if (keyword != null && !keyword.isBlank() && date != null) {
            List<JournalEntry> results = entryController.searchEntries(userId, keyword);
            results.removeIf(entry -> entry.getDate() == null || !entry.getDate().equals(date));
            return results;
        }

        if (keyword != null && !keyword.isBlank()) {
            return entryController.searchEntries(userId, keyword);
        }

        if (date != null) {
            return entryController.getEntriesByDate(userId, date);
        }

        return entryController.getAllEntries(userId);
    }

    public Boolean validateSearchInput(SearchFilter filter) {
        return filter != null && filter.isValidFilter();
    }
}