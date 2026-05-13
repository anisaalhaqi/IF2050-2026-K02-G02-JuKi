# JuKi (Jurnal Kita)

**JuKi** adalah platform jurnal digital modern yang dirancang untuk membantu pengguna dalam mengelola kesehatan mental dan rutinitas perawatan diri (*self-care*). Platform ini mengintegrasikan fitur penulisan jurnal tradisional dengan sistem pelacakan target *self-care* dan visualisasi data emosi untuk memberikan gambaran menyeluruh tentang kesejahteraan pengguna.

## 🚀 Fitur Utama

- **Manajemen Jurnal:** Tulis, simpan, dan cari entri jurnal harian Anda dengan mudah.
- **Target Self-Care:** Atur dan pantau tujuan harian Anda untuk menjaga kesehatan fisik dan mental.
- **Kalender Aktivitas:** Pantau konsistensi journaling dan target Anda melalui tampilan kalender yang intuitif.
- **Visualisasi Analytics:** Lihat grafik perkembangan emosi dan aktivitas Anda dari waktu ke waktu.
- **Manajemen Profil:** Personalisasi akun Anda dengan foto profil dan detail pengguna.

## 📂 Struktur Proyek & Arsitektur

### Struktur Folder Mendetail
```text
C:\Users\hp\IF2050-2026-K02-G02-JuKi\
├───data\                           # Penyimpanan persisten
│   ├───juki.db                     # Database utama SQLite
│   └───img\                        # Foto yang diunggah pengguna
├───img\                            # Aset UI statis
│   ├───dashboard\                  # Ikon & grafis dashboard
│   ├───emojis\                     # Ikon emoji
│   ├───emotions\                   # Ikon kategori emosi
│   ├───icons\                      # Ikon navigasi & tombol
│   └───selfcare\                   # Ikon status self-care
├───src\
│   └───main\
│       ├───java\
│       │   └───com\juki\
│       │       ├───MainApp.java            # Titik masuk utama aplikasi
│       │       ├───controller\            # Logika kontrol aplikasi
│       │       │   ├───AnalyticsController.java      # Logika visualisasi data
│       │       │   ├───EntryController.java          # Manajemen entri jurnal
│       │       │   ├───GoalController.java           # Logika target self-care
│       │       │   ├───MoodController.java           # Pelacakan mood harian
│       │       │   ├───ProfileManager.java           # Manajemen profil pengguna
│       │       │   ├───RegistrationFormController.java # Logika login/regis
│       │       │   └───SearchController.java         # Logika pencarian jurnal
│       │       ├───db\                    # Akses database
│       │       │   └───DatabaseHelper.java           # Operasi CRUD & skema
│       │       ├───model\                 # Model data (POJO)
│       │       │   ├───DailyMood.java                # Entity mood harian
│       │       │   ├───JournalEntry.java             # Entity entri jurnal
│       │       │   ├───Photo.java                    # Entity foto lampiran
│       │       │   ├───SearchFilter.java             # Filter kriteria pencarian
│       │       │   ├───SelfCareGoal.java             # Entity target harian
│       │       │   └───User.java                     # Entity data pengguna
│       │       ├───service\               # Layanan bisnis
│       │       │   └───GoalService.java              # Layanan logika target
│       │       └───view\                  # Komponen antarmuka pengguna
│       │           ├───CalendarView.java             # Tampilan kalender
│       │           ├───DashboardView.java            # Tampilan utama
│       │           ├───EntryDetailView.java          # Detail isi jurnal
│       │           ├───EntryFormView.java            # Form tambah/edit jurnal
│       │           ├───EntryListView.java            # Daftar entri jurnal
│       │           ├───EntryManagerView.java         # Kontainer manajemen jurnal
│       │           ├───GoalModal.java                # Modal tambah target
│       │           ├───ProfileView.java              # Tampilan edit profil
│       │           ├───RegistrationFormView.java     # Tampilan login/regis
│       │           ├───SearchView.java               # Tampilan pencarian
│       │           └───VisualizerView.java           # Tampilan grafis analitik
│       └───resources\
│           └───css\                       # Styling aplikasi
│               └───style.css              # File CSS utama
├───pom.xml                         # Konfigurasi dependensi Maven
└───README.md                       # Dokumentasi utama proyek
```

### Arsitektur (MVC)
JuKi mengikuti pola arsitektur **Model-View-Controller (MVC)** untuk memastikan pemisahan tanggung jawab yang jelas, sehingga kode lebih mudah dikelola dan dikembangkan.

1.  **Model (`com.juki.model`)**: Mewakili struktur data aplikasi. Berupa objek Java (POJO) yang memetakan tabel database, seperti `User`, `JournalEntry`, `DailyMood`, dan `SelfCareGoal`.
2.  **View (`com.juki.view`)**: Menangani lapisan presentasi menggunakan **JavaFX**. Setiap kelas view bertanggung jawab untuk membangun bagian tertentu dari antarmuka pengguna, seperti `DashboardView` dan `CalendarView`.
3.  **Controller (`com.juki.controller`)**: Bertindak sebagai perantara antara Model dan View. Menangani input pengguna dan memperbarui UI, misalnya `EntryController` dan `MoodController`.

### Tanggung Jawab Folder

| Direktori | Tanggung Jawab |
| :--- | :--- |
| `controller/` | Logika untuk menangani interaksi pengguna dan menjembatani model dan view. |
| `db/` | Berisi `DatabaseHelper` untuk mengelola operasi SQLite dan inisialisasi skema. |
| `model/` | Mendefinisikan entitas data yang digunakan di seluruh aplikasi. |
| `view/` | Layout UI, komponen kustom, dan manajemen scene menggunakan JavaFX. |
| `service/` | Lapisan logika bisnis tambahan untuk operasi kompleks seperti pelacakan target. |
| `resources/` | Konfigurasi eksternal dan file CSS untuk gaya aplikasi global. |
| `data/` | Menyimpan database SQLite lokal dan media yang diunggah pengguna. |
| `img/` | Berisi semua aset gambar statis yang digunakan dalam UI aplikasi. |

### Komponen Penting

*   **`MainApp.java`**: Kelas inti aplikasi yang menginisialisasi database dan mengelola stage serta transisi scene utama.
*   **`DatabaseHelper.java`**: Kelas terpusat untuk semua interaksi database, memastikan koneksi yang aman dan menyediakan metode CRUD.
*   **`style.css`**: Stylesheet utama yang menentukan estetika visual aplikasi, termasuk warna, font (Outfit), dan styling komponen.

## 🛠️ Prasyarat

Sebelum menjalankan aplikasi, pastikan Anda telah menginstal:

- **Java Development Kit (JDK) 21** atau versi yang lebih baru.
- **Apache Maven** untuk manajemen proyek.

## ⚙️ Cara Menjalankan Aplikasi

1. **Clone Repositori**
   ```bash
   git clone <url-repository-anda>
   cd IF2050-2026-K02-G02-JuKi
   ```

2. **Instal Dependensi**
   ```bash
   mvn install
   ```

3. **Jalankan Aplikasi**
   Gunakan plugin JavaFX Maven untuk menjalankan aplikasi:
   ```bash
   mvn javafx:run
   ```

## 👥 Anggota Kelompok (G02)

| No | Nama | NIM |
|:---:|:---|:---:|
| 1 | Nadine Octavia | 18224012 |
| 2 | Danya Soe | 18224024 |
| 3 | Anisa Aulia Alhaqi | 18224080 |
| 4 | Zahra Nur Azizah | 18224092 |
| 5 | Riko Satriya Giovanni | 18224108 |

---

*Proyek ini dikembangkan sebagai bagian dari tugas mata kuliah Rekayasa Perangkat Lunak (IF2050).*
