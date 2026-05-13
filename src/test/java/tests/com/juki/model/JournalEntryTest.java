package tests.com.juki.model;

import org.junit.jupiter.api.Test;

import com.juki.model.*;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime ;
import java.util.ArrayList;
import java.util.List;

public class JournalEntryTest {

    @Test
    public void testCreateJournalEntry() {
        // 1. Siapkan data palsu untuk testing
        JournalEntry entry = new JournalEntry();
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.now();
        
        List<Photo> testPhotos = new ArrayList<>();
        testPhotos.add(new Photo(1, "path/to/photo.png"));

        // 2. Masukkan data ke dalam objek JournalEntry
        entry.setId(1);
        entry.setUserId(99);
        entry.setTitle("Test Judul");
        entry.setCategory("Pendidikan");
        entry.setDescription("Test Deskripsi");
        entry.setTrigger("Test Pemicu");
        entry.setTarget("Test Target");
        entry.setDate(testDate);
        entry.setTime(testTime);
        entry.setPhotos(testPhotos);

        // 3. Verifikasi apakah datanya tersimpan dengan benar (TIDAK TERTUKAR)
        assertEquals(1, entry.getId(), "ID harusnya 1");
        assertEquals(99, entry.getUserId(), "User ID harusnya 99");
        assertEquals("Test Judul", entry.getTitle(), "Judul tidak sesuai");
        assertEquals("Pendidikan", entry.getCategory(), "Kategori tidak sesuai");
        assertEquals("Test Deskripsi", entry.getDescription());
        assertEquals("Test Pemicu", entry.getTrigger());
        assertEquals("Test Target", entry.getTarget());
        assertEquals(testDate, entry.getDate());
        assertEquals(testTime, entry.getTime());
        
        // Verifikasi list foto
        assertNotNull(entry.getPhotos(), "List foto tidak boleh null");
        assertEquals(1, entry.getPhotos().size(), "Jumlah foto harusnya 1");
        assertEquals("path/to/photo.png", entry.getPhotos().get(0).getFilePath());
    }
}