# JuKi (Jurnal Masa Kini)

**JuKi** adalah platform jurnal digital modern yang dirancang untuk membantu pengguna dalam mengelola kesehatan mental dan rutinitas perawatan diri (*self-care*). Platform ini mengintegrasikan fitur penulisan jurnal tradisional dengan sistem pelacakan target *self-care* dan visualisasi data emosi untuk memberikan gambaran menyeluruh tentang kesejahteraan pengguna.

## Fitur Utama

- **Manajemen Jurnal:** Tulis, simpan, dan cari entri jurnal harian Anda dengan mudah.
- **Target Self-Care:** Atur dan pantau tujuan harian Anda untuk menjaga kesehatan fisik dan mental.
- **Kalender Aktivitas:** Pantau konsistensi journaling dan target Anda melalui tampilan kalender yang intuitif.
- **Visualisasi Analytics:** Lihat grafik perkembangan emosi dan aktivitas Anda dari waktu ke waktu.
- **Manajemen Profil:** Personalisasi akun Anda dengan foto profil dan detail pengguna.

## Struktur Folder

```text
JuKi-Desktop/
├── data/               # Penyimpanan database SQLite (juki.db)
├── doc/                # Dokumentasi proyek
├── img/                # Aset gambar (ikon, emoji, emosi, dashboard)
├── src/
│   └── main/
│       └── java/
│           └── com/juki/
│               ├── controller/  # Logika bisnis dan penghubung View-Model
│               ├── db/          # Helper untuk koneksi database SQLite
│               ├── model/       # Entitas data (User, Entry, Goal, dll)
│               ├── view/        # Antarmuka pengguna (JavaFX)
│               └── MainApp.java # Titik masuk utama aplikasi
├── tests/              # File pengujian unit
├── pom.xml             # Konfigurasi Maven dan dependensi
└── README.md           # Dokumentasi utama proyek
```

## Prasyarat

Sebelum menjalankan aplikasi, pastikan Anda telah menginstal:

- **Java Development Kit (JDK) 21** atau versi yang lebih baru.
- **Apache Maven** untuk manajemen proyek.

## Cara Menjalankan Aplikasi

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

1. **Anisa Aulia Alhaqi** - 18224080
2. **Danya Soe** - 18224024
3. **Nadine Octavia** - 18224012
4. **Riko Satriya Giovanni** - 18224108 
5. **Zahra Nur Azizah** - 18224092

---

*Proyek ini dikembangkan sebagai bagian dari tugas mata kuliah Rekayasa Perangkat Lunak (IF2050).*