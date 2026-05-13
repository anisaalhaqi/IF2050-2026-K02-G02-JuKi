# JuKi (Jurnal Kita)

**JuKi** adalah platform jurnal digital modern yang dirancang untuk membantu pengguna dalam mengelola kesehatan mental dan rutinitas perawatan diri (*self-care*). Platform ini mengintegrasikan fitur penulisan jurnal tradisional dengan sistem pelacakan target *self-care* dan visualisasi data emosi untuk memberikan gambaran menyeluruh tentang kesejahteraan pengguna.

## 🚀 Fitur Utama

- **Manajemen Jurnal:** Tulis, simpan, dan cari entri jurnal harian Anda dengan mudah.
- **Target Self-Care:** Atur dan pantau tujuan harian Anda untuk menjaga kesehatan fisik dan mental.
- **Kalender Aktivitas:** Pantau konsistensi journaling dan target Anda melalui tampilan kalender yang intuitif.
- **Visualisasi Analytics:** Lihat grafik perkembangan emosi dan aktivitas Anda dari waktu ke waktu.
- **Manajemen Profil:** Personalisasi akun Anda dengan foto profil dan detail pengguna.

## 🛠️ Prasyarat

Sebelum menjalankan aplikasi, pastikan Anda telah menginstal:

- **Java Development Kit (JDK) 21** atau versi yang lebih baru.
- **Apache Maven** untuk manajemen proyek.
- **SQLite** (biasanya sudah termasuk dalam library JDBC).

## ⚙️ Instalasi dan Cara Menjalankan

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

## 🧩 Modul yang Diimplementasikan

| Nama Modul | Deskripsi |
|:---|:---|
| **Autentikasi & Registrasi** | Mengelola proses pendaftaran akun baru dan login pengguna. |
| **Manajemen Jurnal** | Memungkinkan pengguna membuat, membaca, memperbarui, dan menghapus (CRUD) entri jurnal. |
| **Target Self-Care** | Sistem untuk menetapkan dan melacak status penyelesaian target harian. |
| **Dashboard & Kalender** | Tampilan utama yang merangkum aktivitas harian dan navigasi berbasis tanggal. |
| **Profil Pengguna** | Mengelola informasi pribadi pengguna, termasuk foto profil. |
| **Analisis & Mood** | Visualisasi tren emosi harian dan statistik pencapaian target. |
| **Pencarian & Filter** | Mempermudah pencarian entri jurnal berdasarkan kriteria tertentu. |

## 🗄️ Basis Data

Aplikasi menggunakan SQLite dengan skema tabel sebagai berikut:

- **User**: Menyimpan data akun pengguna.
  - `id` (INTEGER, PK): ID unik pengguna.
  - `full_name` (TEXT): Nama lengkap.
  - `username` (TEXT, UNIQUE): Username untuk login.
  - `password` (TEXT): Password akun.
  - `profile_photo_path` (TEXT): Path file foto profil.

- **JournalEntry**: Menyimpan entri jurnal harian.
  - `id` (INTEGER, PK): ID unik entri.
  - `category` (TEXT): Kategori jurnal.
  - `title` (TEXT): Judul entri.
  - `description` (TEXT): Isi jurnal.
  - `trigger` (TEXT): Pemicu emosi.
  - `target` (TEXT): Target terkait.
  - `date` (TEXT): Tanggal entri.
  - `time` (TEXT): Waktu entri.
  - `photo_id` (TEXT): Referensi ke foto.
  - `user_id` (INTEGER, FK): Referensi ke pemilik entri.

- **SelfCareGoal**: Menyimpan target perawatan diri.
  - `id` (INTEGER, PK): ID unik target.
  - `title` (TEXT): Nama target.
  - `is_completed` (INTEGER): Status penyelesaian (0/1).
  - `date` (TEXT): Tanggal target.
  - `user_id` (INTEGER, FK): Referensi ke pemilik target.

- **DailyMood**: Menyimpan catatan mood harian.
  - `id` (INTEGER, PK): ID unik mood.
  - `mood_name` (TEXT): Nama emosi.
  - `date` (TEXT): Tanggal pencatatan.
  - `user_id` (INTEGER, FK): Referensi ke pemilik data.

- **Photo**: Menyimpan referensi file gambar.
  - `id` (INTEGER, PK): ID unik foto.
  - `filePath` (TEXT): Path file gambar di penyimpanan lokal.

## 👥 Pembagian Tugas Implementasi (G02)

Setiap anggota bertanggung jawab minimal atas satu bagian yang mencakup Model, Antarmuka (View), dan Logika (Controller/Service).

| No | Nama | NIM | Model | View | Controller / Helper |
|:---:|:---|:---:|:---|:---|:---|
| 1 | Nadine Octavia | 18224012 | `SelfCareGoal` | `CalendarView`, `GoalModal` | `GoalController`, `GoalService` |
| 2 | Danya Soe | 18224024 | `JournalEntry`, `Photo` | `EntryFormView`, `EntryDetailView` | `EntryController`, `DatabaseHelper` |
| 3 | Anisa Aulia Alhaqi | 18224080 | `User` (Profile) | `ProfileView`, `EntryManagerView` | `ProfileManager` |
| 4 | Zahra Nur Azizah | 18224092 | `SearchFilter` | `RegistrationFormView`, `EntryListView`, `SearchView` | `RegistrationFormController`, `SearchController` |
| 5 | Riko Satriya Giovanni | 18224108 | `DailyMood` | `DashboardView`, `VisualizerView` | `AnalyticsController`, `MoodController` |

---

*Proyek ini dikembangkan sebagai bagian dari tugas mata kuliah Rekayasa Perangkat Lunak (IF2050).*
