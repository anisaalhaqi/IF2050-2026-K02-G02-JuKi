package com.juki.controller;

import com.juki.model.JournalEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnalyticsController {
    private List<JournalEntry> raw_entries;
    private Float success_rate;
    private Map<String, Object> mood_trend;
    private Integer total_target_count;

    public Float calculateAverageMood() {
        return 0.0f;
    }

    public Float calculateSelfCareRate() {
        return success_rate;
    }

    public void filterByData(LocalDate startDate, LocalDate endDate) {
        // Logika penyaringan entri berdasarkan rentang tanggal
    }

    public void aggregateDate() {
        // Logika mengelompokkan data harian
    }

    public Map<String, Object> getMoodTrend() {
        return mood_trend;
    }

    public List<JournalEntry> getJournalEntry() {
        return raw_entries;
    }

    public List<JournalEntry> getVisualization() {
        return raw_entries; // Kembalikan data yang diformat
    }
}