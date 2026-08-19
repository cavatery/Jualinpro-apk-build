# Jualin AI Pro 🚀

Platform SaaS AI Copywriting & Asisten Promosi Penjualan untuk UMKM Indonesia berbasis Kotlin & Jetpack Compose.

## ✨ Fitur Utama
- **AI Caption Generator**: 11 pilihan tone gaya bahasa (Viral, Persuasif, Santai, Gen Z, Ibu-Ibu, Storytelling, dll.).
- **Analisis Foto Produk Multimodal (AI Vision)**: Deteksi otomatis karakteristik, warna, kemasan, dan rekomendasi hook visual.
- **Studio Iklan Multi-Platform**: Format siap pakai untuk WhatsApp Broadcast, WhatsApp Status, Instagram Feed/Story, Facebook, Naskah Video TikTok, dan Deskripsi Marketplace SEO.
- **Kalender Konten 7 Hari**: Jadwal tema dan copywriting harian terstruktur.
- **Asisten Balas Chat & Follow-Up CS**: Template respon cepat dan reminder transaksi.
- **Penyimpanan Lokal (Room Database)**: Simpan, cari, favoritkan, dan salin materi promosi secara offline di perangkat.

---

## 🛠️ Cara Build di GitHub (CI/CD Otomatis)

Proyek ini telah dilengkapi dengan workflow **GitHub Actions** (`.github/workflows/android-build.yml`). Setiap kali Anda melakukan `git push` ke GitHub:
1. GitHub Actions akan otomatis mengompilasi kode program menggunakan JDK 17 & Gradle.
2. Menjalankan seluruh pengujian unit (*Unit Tests*).
3. Menghasilkan file **APK Debug** yang dapat langsung diunduh di tab **Actions > Artifacts**.

### Menambahkan API Key di GitHub Secrets (Opsional)
Jika ingin menyertakan Gemini API Key bawaan saat build di GitHub:
1. Buka Repository Anda di GitHub -> **Settings** -> **Secrets and variables** -> **Actions**.
2. Klik **New repository secret**.
3. Name: `GEMINI_API_KEY`, Value: Kunci API Gemini Anda.

---

## 💻 Cara Build & Run Lokal (Android Studio / Terminal)

### Menggunakan Terminal
```bash
# Memberi izin eksekusi gradle
chmod +x gradlew

# Menjalankan unit tests
./gradlew testDebugUnitTest

# Membangun file APK Debug
./gradlew assembleDebug
```
File APK yang dihasilkan berada di folder: `app/build/outputs/apk/debug/app-debug.apk`

### Menggunakan Android Studio
1. Buka Android Studio -> Pilih **Open** -> Arahkan ke folder proyek ini.
2. Tunggu Gradle Sync selesai.
3. Klik tombol **Run (▶)** untuk menjalankan aplikasi di Emulator atau HP Android fisik.
