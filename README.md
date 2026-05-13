# JuKi (Jurnal Kita)

**JuKi** adalah platform jurnal digital modern yang dirancang untuk membantu pengguna dalam mengelola kesehatan mental dan rutinitas perawatan diri (*self-care*). Platform ini mengintegrasikan fitur penulisan jurnal tradisional dengan sistem pelacakan target *self-care* dan visualisasi data emosi untuk memberikan gambaran menyeluruh tentang kesejahteraan pengguna.

## 🚀 Fitur Utama

- **Manajemen Jurnal:** Tulis, simpan, dan cari entri jurnal harian Anda dengan mudah.
- **Target Self-Care:** Atur dan pantau tujuan harian Anda untuk menjaga kesehatan fisik dan mental.
- **Kalender Aktivitas:** Pantau konsistensi journaling dan target Anda melalui tampilan kalender yang intuitif.
- **Visualisasi Analytics:** Lihat grafik perkembangan emosi dan aktivitas Anda dari waktu ke waktu.
- **Manajemen Profil:** Personalisasi akun Anda dengan foto profil dan detail pengguna.

## 📂 Struktur Folder

```text
IF2050-2026-K02-G02-JuKi/
├── data/                       # Database SQLite
│   └── juki.db                 # File database utama
├── doc/                        # Dokumentasi teknis proyek
├── img/                        # Aset gambar & UI
│   ├── dashboard/              # Ikon & grafis dashboard
│   ├── emojis/                 # Aset emoji
│   ├── emotions/               # Ikon mood (angry, joyful, dll)
│   ├── icons/                  # Ikon navigasi (panah, kalender, dll)
│   └── selfcare/               # Ikon status target self-care
├── src/
│   └── main/java/com/juki/
│       ├── controller/         # Logika kontrol (Bridge antara UI & Data)
│       │   ├── AnalyticsController.java
│       │   ├── EntryController.java
│       │   ├── GoalController.java
│       │   ├── MoodController.java
│       │   └── RegistrationFormController.java
│       ├── db/                 # Konfigurasi Database
│       │   └── DatabaseHelper.java
│       ├── model/              # Struktur data (POJO)
│       │   ├── DailyMood.java
│       │   ├── JournalEntry.java
│       │   ├── SelfCareGoal.java
│       │   └── User.java
│       ├── view/               # Antarmuka Pengguna (JavaFX)
│       │   ├── DashboardView.java
│       │   ├── CalendarView.java
│       │   ├── EntryFormView.java
│       │   ├── ProfileView.java
│       │   └── RegistrationFormView.java
│       └── MainApp.java        # Entry point aplikasi
├── tests/                      # Unit testing
├── pom.xml                     # Manajemen dependensi Maven
└── README.md                   # Dokumentasi proyek
```

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
