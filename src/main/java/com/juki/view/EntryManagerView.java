package com.juki.view;

import com.juki.controller.EntryController;
import com.juki.model.JournalEntry;
import java.util.List;

public class EntryManagerView {
    protected EntryController controller;

    public void displayEntryOptions(JournalEntry entry) {
        // Menampilkan tombol update/hapus
    }

    public Boolean confirmDelete() {
        return true; // Modal konfirmasi
    }

    public String getUserAction() {
        return "Aksi";
    }

    public void displayEntryList(List<JournalEntry> entries) {
        // Menampilkan tabel entri
    }

    public void displayEntryDetail(JournalEntry entry) {
        // Menampilkan detail
    }

    public void selectMenu() {
        // Menangani pemilihan navigasi
    }
}