package com.juki.controller;

import com.juki.model.JournalEntry;
import com.juki.model.SearchFilter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SearchController {
    private SearchFilter filter;
    private String keyword;
    private LocalDate date;

    public List<JournalEntry> searchEntries(SearchFilter filter) {
        List<JournalEntry> results = new ArrayList<>();
        // Eksekusi pencarian
        return results;
    }

    public Boolean validateSearchInput(SearchFilter filter) {
        return filter != null && filter.isValidFilter();
    }
}