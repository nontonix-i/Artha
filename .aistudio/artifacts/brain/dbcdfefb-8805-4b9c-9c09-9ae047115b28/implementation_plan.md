# ArthaStock: Sistem Manajemen Inventaris & Finansial Real-Time

ArthaStock adalah aplikasi Android terintegrasi untuk tim operasional dan sales yang menggabungkan manajemen stok berbasis jatah mingguan (*weekly pool roll-over*), mutasi transfer & hutang barang antar-pengguna, tabungan otomatis per produk terjual, kalkulasi **Setoran Wajib (HPP + Tabungan)** dengan checklist fleksibel, serta dashboard analitik arus kas bergaya fintech modern.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Keputusan Kunci yang Dikonfirmasi & Diperbarui (Revisi Phase 1):**
> 1. **Autentikasi & Multi-User:** Akun terpisah via Google Sign-In (`CredentialManager`) dengan sinkronisasi real-time cloud Firebase Firestore.
> 2. **Pelacakan Jatah & Asal Barang (*Batch Lineage*):** Stok agregat untuk transaksi kasir cepat, dilengkapi rincian transparan per sumber stok (Sisa Jatah Minggu Lalu, Jatah Baru Minggu Ini, Transfer Masuk, atau Pinjaman).
> 3. **Mekanisme Setoran Wajib & Toggle Tabungan:**
>    - **Rumus Dasar Setoran Wajib**: HPP (Modal Pabrik) + Tabungan (Contoh: HPP Rp 11.000 + Tabungan Rp 500 = **Rp 11.500**).
>    - **Checklist Fleksibel**: Tersedia switch/checklist *"Sertakan Tabungan"* saat input penjualan/setoran. Jika aktif, setoran menjadi Rp 11.500 dan Rp 500 masuk ke kas tabungan. Jika tidak aktif, setoran hanya HPP murni (Rp 11.000).
>    - **Margin Laba Bersih Sales**: Dihitung dari `Harga Jual - Setoran Wajib` (misal Harga Jual Rp 15.000 - Rp 11.500 = Rp 3.500).
> 4. **Dashboard Finansial:** Minimalis & berdaya pantau tinggi: Arus Kas Masuk, Setoran Wajib Terkumpul vs Tertunggak (Merah/Hijau), Total Kas Tabungan, dan Komposisi Modal HPP.

---

### 1. Overview & Core Concept

- **What It Does**: Memungkinkan admin dan sales mengelola distribusi jatah barang mingguan (*pool*), mengkombinasikan sisa stok minggu lalu dengan kiriman baru, mencatat penjualan dengan pemisahan otomatis antara **Modal HPP**, **Tabungan Wajib**, dan **Margin Keuntungan Sales**, melakukan transfer stok antar-rekan sales, serta mencatat hutang setoran maupun hutang fisik barang.
- **Target Audience / Persona**: Pemilik usaha distribusi/reseller, koordinator gudang/pool, dan tim sales lapangan dengan sistem setoran terjadwal dan tabungan cadangan.
- **Key Value**: Transparansi penuh dalam perhitungan setoran pabrik dan tabungan (tanpa salah hitung nominal setoran wajib), pelacakan sisa barang mingguan yang akurat, serta monitoring piutang/setoran tertunda secara real-time.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Google Sign-In & Dashboard Utama**:
   - Masuk cepat dengan 1-Tap Google Sign-In.
   - Kartu Metrik Finansial:
     - **Total Kas Masuk (Gross Sales)**
     - **Setoran Wajib Terkumpul** (HPP + Tabungan)
     - **Hutang / Tunggakan Setoran** (Badge Merah Tajam jika ada yang belum disetor)
     - **Total Pundi Tabungan** (Badge Biru/Hijau)
     - **Estimasi Laba Bersih Sales**
   - Grafik Interaktif: Perbandingan Arus Kas (Modal HPP vs Tabungan vs Keuntungan) dan Grafik Batang Tren Penjualan 7 Hari.
2. **Katalog Produk & Konfigurasi Setoran**:
   - Input/Edit Produk: Nama Produk, Harga Jual Konsumen, HPP / Modal Pabrik (misal Rp 11.000), dan Default Tabungan per unit (misal Rp 500).
   - Indikator otomatis menampilkan: **Nilai Setoran Wajib Standar: Rp 11.500 / pcs**.
3. **Pencatatan Penjualan & Setoran Cepat**:
   - Form penjualan produk dengan pemilihan kuantitas.
   - **Checklist Toggle "Sertakan Tabungan"** (Default: Aktif):
     - Saat Aktif: Setor Wajib = Rp 11.500/pcs (Rp 11.000 disiapkan untuk pabrik, Rp 500 dialokasikan ke Kas Tabungan).
     - Saat Nonaktif: Setor Wajib = Rp 11.000/pcs (hanya modal HPP, tabungan 0).
   - Kalkulasi otomatis Margin Laba Sales langsung tampil di layar secara real-time.
4. **Distribusi Jatah Mingguan & Rincian Asal Stok (*Batch Breakdown*)**:
   - Distribusi Pool Baru: Admin/Koordinator mengalokasikan stok kiriman baru setiap minggu.
   - Kartu Asal Stok (*Origin Ledger*): Sales melihat total stok yang bisa dijual (misal 50 pcs), dengan kartu rincian asal:
     - *Sisa Jatah Minggu Lalu*: 12 pcs
     - *Jatah Kiriman Baru Minggu Ini*: 30 pcs
     - *Transfer Masuk dari Budi*: 8 pcs
5. **Transfer Barang & Manajemen Hutang / Pinjam Barang**:
   - **Transfer Antar-Sales**: Kirim stok ke sales lain secara langsung, otomatis mengurangi stok pengirim dan menambah stok penerima dengan catatan riwayat.
   - **Hutang Barang & Pinjaman**: Catat pinjaman barang fisik atau hutang setoran belum lunas dengan badge tanggal dan tombol aksi cepat *"Pelunasan Setoran"*.

#### Visual Identity & Theme
- **Aesthetic Direction**: *Fintech Minimalist & Ultra-Clean*. Dominan putih dengan kontras kartu abu-abu lembut, sudut rounded 16dp, dan hierarki tipografi tegas tanpa teks penjelasan bertele-tele.
- **Color Palette & Mood**:
  - `Background`: Putih murni (`#FFFFFF`) dengan kartu container `#F8FAFC`.
  - `Primary / Accent`: Cobalt Blue (`#0D6EFD` / `#2563EB`) untuk navigasi dan tombol primer.
  - `Positive / Lunas / Laba`: Vibrant Emerald Green (`#16A34A` / `#22C55E`) untuk setoran lunas, laba positif, dan pundi tabungan.
  - `Negative / Hutang / Tunggakan`: Crimson Red (`#DC2626` / `#EF4444`) khusus untuk hutang barang, pinjaman tertunda, dan setoran belum lunas.
  - `Typography`: Charcoal Slate (`#0F172A`) tebal untuk angka nominal rupiah finansial.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Perhitungan Setoran Wajib Dinamis dengan Checklist Tabungan**
  - *Chosen Approach*: Setiap transaksi penjualan mencatat `hppAmount`, `savingsAmount`, `mandatoryDepositAmount`, dan boolean `isSavingsApplied`.
  - *Why*: Memenuhi kebutuhan fleksibilitas saat sales/pengguna ingin menyetor dengan atau tanpa tabungan pada transaksi tertentu.
- **Decision 2: Pemisahan 3 Rekening Kas Virtual di Dashboard**
  - *Chosen Approach*: Sistem memecah total kas masuk menjadi 3 pos terpisah secara real-time:
    1. **Kas HPP / Modal Pabrik**: Dana aman untuk modal kiriman berikutnya.
    2. **Kas Tabungan**: Akumulasi tabungan cadangan (misal Rp 500/unit) yang tidak boleh terpakai operasional harian.
    3. **Kas Margin Keuntungan**: Keuntungan bersih operasional/sales.
- **Decision 3: Sinkronisasi Multi-User Cloud & Batch Origin Tracking**
  - *Chosen Approach*: Menggunakan Firebase Authentication (Google Sign-In) dan Firestore. Stok yang tersedia disatukan untuk kemudahan transaksi, tetapi mutasi stok mempertahankan metadata sumber (*Rollover*, *New Pool*, *Transfer In*, *Borrowed*).

---

### 4. Technical Architecture & Data Strategy (Technical Reference)

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ARTHASTOCK UI LAYER                             │
│                                                                        │
│  ┌────────────────────┐  ┌───────────────────┐  ┌───────────────────┐  │
│  │ DashboardScreen    │  │ InventoryScreen   │  │ OperationsScreen  │  │
│  │ - Real-time Cash   │  │ - Product List    │  │ - Transfer Barang │  │
│  │ - Setoran Wajib    │  │ - Batch Origin    │  │ - Hutang/Pinjaman │  │
│  │ - Tabungan Metric  │  │ - Weekly Pool Form│  │ - Quick Settle    │  │
│  │ - Financial Charts │  │ - Setor Checklist │  │                   │  │
│  └─────────┬──────────┘  └─────────┬─────────┘  └─────────┬─────────┘  │
└────────────┼───────────────────────┼──────────────────────┼────────────┘
             │                       │                      │
             ▼                       ▼                      ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        VIEWMODELS & STATEFLOW                          │
│   • DashboardViewModel (Cash Flow, Setoran Wajib, Tabungan, Charts)    │
│   • InventoryViewModel (Products, Weekly Allocations, Batch Lineage)   │
│   • OperationsViewModel (Transfers, Debt Ledgers, Settlements)         │
│   • AuthViewModel (Google Sign-In Session, User Profile)               │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        REPOSITORY & SERVICE LAYER                      │
│   • InventoryRepository  • FinanceRepository  • OperationsRepository   │
│   • CredentialManagerAuthClient (Google Sign-In API 36 Responsive)     │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      FIREBASE FIRESTORE DATA LAYER                     │
│                                                                        │
│  /users/{userId}                   -> Profile & Assigned Role          │
│  /products/{productId}             -> Info, SellPrice, HPP, SavingsVal │
│  /user_stock/{userStockId}         -> Aggregate Stock & Source Details │
│  /weekly_pools/{poolId}            -> Pool Allocation Records & Weeks  │
│  /transfers/{transferId}           -> Sender, Receiver, Qty, Status    │
│  /debts/{debtId}                   -> Borrower, Lender, Items, Settled │
│  /transactions/{transactionId}     -> Sales, HPP, Savings, SetoranWajib│
└────────────────────────────────────────────────────────────────────────┘
```

#### Firestore Data Model Specification

1. **`products` Collection**:
   - `id: String`, `name: String`, `code: String`, `sellPrice: Double`, `hppPrice: Double` (misal 11.000), `savingsPerUnit: Double` (misal 500), `mandatoryDepositDefault: Double` (11.500), `category: String`, `active: Boolean`.
2. **`transactions` Collection**:
   - `id: String`, `userId: String`, `userName: String`, `productId: String`, `productName: String`,
   - `quantity: Long`,
   - `sellPricePerUnit: Double`,
   - `hppPerUnit: Double` (11.000),
   - `savingsPerUnit: Double` (500),
   - `isSavingsIncluded: Boolean` (true/false dari checklist),
   - `totalRevenue: Double` (`quantity * sellPrice`),
   - `totalHpp: Double` (`quantity * hppPerUnit`),
   - `totalSavings: Double` (jika checklist aktif: `quantity * savingsPerUnit`, jika tidak: `0`),
   - `totalMandatoryDeposit: Double` (`totalHpp + totalSavings`),
   - `netMarginSales: Double` (`totalRevenue - totalMandatoryDeposit`),
   - `isDepositSettled: Boolean` (sudah disetor ke bos/admin atau masih tertunda),
   - `timestamp: Timestamp`.
3. **`user_stock` Collection**:
   - `id: String`, `userId: String`, `productId: String`, `productName: String`,
   - `totalAvailableStock: Long`,
   - `rolloverFromLastWeek: Long`,
   - `currentWeekAllocation: Long`,
   - `transfersReceived: Long`,
   - `transfersSent: Long`,
   - `debtBorrowed: Long`,
   - `soldCount: Long`,
   - `updatedAt: Timestamp`.
4. **`weekly_pools` Collection**:
   - `id: String`, `weekIdentifier: String (misal 2026-W45)`, `productId: String`, `totalPoolQty: Long`, `allocatedQty: Long`, `createdAt: Timestamp`.
5. **`transfers` Collection**:
   - `id: String`, `fromUserId: String`, `fromUserName: String`, `toUserId: String`, `toUserName: String`, `productId: String`, `quantity: Long`, `note: String`, `timestamp: Timestamp`.
6. **`debts` Collection**:
   - `id: String`, `userId: String`, `userName: String`, `type: String ("SETORAN_TERTUNDA" | "PINJAM_BARANG")`, `productId: String`, `quantity: Long`, `totalNominal: Double`, `isSettled: Boolean`, `settledAt: Timestamp?`, `createdAt: Timestamp`.

---

### Implementation Phasing (Upon Approval)

1. **Firebase Backend Provisioning**:
   - Keystore SHA-1 & SHA-256 extraction dan inisialisasi Firebase Auth + Firestore.
   - Aturan keamanan `firestore.rules` untuk multi-user dengan validasi skema.
2. **Data & Repository Layer**:
   - Model data Kotlin type-safe dengan kalkulasi otomatis Setoran Wajib (`HPP + (if (isSavings) savings else 0)`).
   - Google Sign-In Credential Manager dengan tombol interaktif fallback.
3. **Finance & Inventory UI/UX**:
   - Dashboard Finansial: Kartu Kas Masuk, Setoran Wajib, Tabungan, dan Tunggakan Hutang.
   - Dialog Pencatatan Penjualan: Input jumlah dan **Checkbox / Switch "Sertakan Tabungan (Rp 500)"** yang langsung memperbarui estimasi setoran wajib (misal Rp 11.500) dan margin laba sales.
   - Halaman Stok & Jatah: Manajemen Weekly Pool, kalkulasi roll-over sisa minggu lalu, dan kartu rincian asal barang.
   - Halaman Operasional: Transfer antar-user dan pelacakan hutang/tunggakan setoran dengan tombol pelunasan instan.
   - Verifikasi build dan pengujian lokal.
