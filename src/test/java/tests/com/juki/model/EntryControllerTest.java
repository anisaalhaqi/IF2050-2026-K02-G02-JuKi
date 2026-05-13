package tests.com.juki.model;

import com.juki.controller.*;
import com.juki.model.JournalEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class EntryControllerTest {
    private EntryController controller;

    @BeforeEach
    void setUp() {
        // Inisialisasi controller sebelum setiap test dijalankan
        controller = new EntryController();
    }

    @Test
    public void testGetEntriesByDate_WhenDateNull() {
        // Mengetes perilaku sistem jika user tidak memilih tanggal (null)
        // Seharusnya mengembalikan semua entri jurnal user
        List<JournalEntry> results = controller.getEntriesByDate(1, null);
        
        assertNotNull(results, "Hasil pencarian tidak boleh null");
    }

    @Test
    public void testSearchEntries_WithKeyword() {
        // Mengetes fitur pencarian jurnal dengan kata kunci tertentu
        String keyword = "Tubes";
        int userId = 1;
        
        List<JournalEntry> results = controller.searchEntries(userId, keyword);
        
        // Memastikan hasil pencarian didefinisikan (tidak error)
        assertNotNull(results);
    }
}