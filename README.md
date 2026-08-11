# Warehouse Management & Order System

**Bahasa:** Indonesia | [English](README.en.md)

Sistem manajemen gudang (warehouse) berbasis Spring Boot yang mengelola alur produk mulai dari **stok gudang → distribusi ke merchant → penjualan ke customer**, lengkap dengan pembayaran QRIS dan notifikasi otomatis lewat WhatsApp (WAHA), Telegram Bot, dan Email (Mailtrap).

---

## Daftar Isi

- [Ringkasan Sistem](#ringkasan-sistem)
- [Tech Stack](#tech-stack)
- [Proses Bisnis](#proses-bisnis)
- [Alur Pembayaran QRIS](#alur-pembayaran-qris)
- [Alur Notifikasi](#alur-notifikasi)
- [Model Data Inti](#model-data-inti)
- [Persiapan & Instalasi](#persiapan--instalasi)
- [Konfigurasi Layanan Eksternal](#konfigurasi-layanan-eksternal)
- [Environment Variables](#environment-variables)
- [Autentikasi & Role](#autentikasi--role)
- [Dokumentasi API](#dokumentasi-api)

---

## Ringkasan Sistem

Aplikasi ini dipakai oleh:

- **Manager (Admin)** — mengelola master data (kategori, produk, gudang, merchant, user) dan memonitor seluruh transaksi.
- **Keeper** — pengelola merchant tertentu (penjaga toko/merchant), memverifikasi pembayaran manual dan menerima notifikasi transaksi via Telegram.
- **Customer** — melakukan checkout tanpa login (public endpoint), membayar via QRIS, menerima invoice via WhatsApp atau Email.

---

## Tech Stack

| Kebutuhan | Teknologi |
|---|---|
| Bahasa & Framework | Java 17, Spring Boot 4.1 (Web MVC, Security, Data JPA, Validation, Mail, Cache, Actuator) |
| Database | PostgreSQL 17 |
| Auth | JWT (jjwt) |
| QR Code Generator | **ZXing** (`core`, `javase`) — merender payload QRIS menjadi gambar PNG |
| Payment Gateway | **Midtrans** (Core API `/v2/charge`, QRIS dinamis) sebagai alternatif dari **QRIS Statis BCA** |
| Notifikasi WhatsApp | **WAHA** (WhatsApp HTTP API, self-hosted gateway) |
| Notifikasi Merchant | **Telegram Bot API** (dibuat via BotFather) |
| Notifikasi Email | **Mailtrap** (SMTP) + Thymeleaf email template |
| Object Storage | Oracle Cloud Infrastructure (OCI) Object Storage — upload foto produk/kategori/gudang/merchant/user |
| Cache | Caffeine |
| Dokumentasi API | springdoc-openapi (Swagger UI) |
| Secret Manager (opsional) | Infisical |

---

## Proses Bisnis

Alur inti sistem ini mengikuti pola **Warehouse → Merchant → Customer**:

```
1. Admin input Produk (nama, barcode, harga, kategori, foto)
        │
        ▼
2. Admin alokasikan stok Produk ke Gudang (Warehouse)
   → entity: WarehouseProduct (warehouse + product + stock)
        │
        ▼
3. Admin distribusikan stok dari Gudang ke Merchant untuk dijual
   → entity: MerchantProduct (merchant + product + warehouse + stock)
        │
        ▼
4. Customer checkout produk dari satu Merchant
   → entity: Transaction + TransactionProduct
        │
        ▼
5. Sistem generate QRIS (Statis BCA via ZXing atau QRIS dinamis via Midtrans)
        │
        ▼
6. Customer scan & bayar
        │
        ▼
7. Konfirmasi pembayaran:
   - otomatis via webhook Midtrans (signature-verified), atau
   - manual oleh Manager/Keeper merchant terkait
        │
        ▼
8. Saat pembayaran sukses:
   - stok MerchantProduct dikurangi otomatis
   - notifikasi WhatsApp ke customer (jika ada no. HP)
   - notifikasi Telegram ke Keeper merchant
   - jika customer tidak beri no. HP → invoice/notifikasi dikirim via Email (Mailtrap)
```

Setiap level stok terpisah: **stok gudang (WarehouseProduct)** ≠ **stok merchant (MerchantProduct)**. Saat distribusi, admin memindahkan sejumlah stok dari gudang ke merchant; transaksi hanya mengurangi stok di level merchant.

---

## Alur Pembayaran QRIS

Field `paymentMethod` pada saat membuat transaksi (`POST /api/transactions`) menentukan metode:

### 1. `bca_qris_static` — QRIS Statis BCA + ZXing (dynamic amount)

- Merchant memiliki satu kode QRIS statis dari rekening BCA (didapat dari EDC/merchant portal BCA), disimpan sebagai payload EMVCo mentah di `bca.qris.staticCode`.
- `QrisUtil.injectDynamicAmount()` mem-parsing payload EMV QRIS (tag-length-value), menyisipkan tag jumlah (tag `54`), mengubah Point of Initiation Method menjadi dinamis (tag `01` = `12`), lalu menghitung ulang checksum CRC16-CCITT (tag `63`) sesuai spesifikasi QRIS.
- Hasil payload per-transaksi ini dirender menjadi gambar QR PNG menggunakan **ZXing** (`QrCodeImageGenerator`), diakses lewat endpoint publik `GET /api/transactions/{id}/qr-image`.
- Tidak butuh API pihak ketiga saat generate QR — murni kalkulasi lokal. Cocok untuk merchant yang belum onboarding payment gateway.

### 2. `qris` — QRIS Dinamis via Midtrans

- Memanggil Midtrans Core API `/v2/charge` dengan detail order, item, pajak, dan ongkir.
- Midtrans mengembalikan `qrCodeUrl` (gambar QR sudah jadi dari Midtrans), `transactionId`, dan `expiryTime`.
- Verifikasi pembayaran masuk lewat webhook `POST /api/payments/notification`, signature diverifikasi dengan `SHA-512(orderId + statusCode + grossAmount + serverKey)`.

### Konfirmasi Manual

Manager atau Keeper (pemilik merchant terkait) juga bisa mengonfirmasi status pembayaran secara manual lewat `POST /api/transactions/{id}/confirm-payment` — berguna untuk metode `bca_qris_static` yang tidak memiliki webhook otomatis (verifikasi mutasi dilakukan manual oleh Keeper setelah cek rekening).

---

## Alur Notifikasi

### 1. WhatsApp via WAHA (customer)

- **WAHA** adalah gateway WhatsApp HTTP API self-hosted (bisa dijalankan via Docker). Aplikasi ini terhubung ke WAHA sebagai REST client (`wahaRestClient`) dengan header `X-Api-Key`.
- Menggunakan pola **Outbox**: setiap pesan yang perlu dikirim disimpan dulu ke tabel `notification_outbox` (entity `NotificationOutbox`), lalu `NotificationOutboxScheduler` (`@Scheduled(fixedDelay = 15_000)`) memprosesnya setiap 15 detik.
- Antar pengiriman diberi jeda random (`waha.min-delay-ms` s/d `waha.max-delay-ms`) untuk menghindari deteksi spam/ban dari WhatsApp.
- Template pesan didefinisikan di enum `WhatsAppTemplate` (`PAYMENT_CREATED_V1`, `PAYMENT_CONFIRMED_V1`) dengan placeholder `{{1}}`, `{{2}}`, dst.
- Saat transaksi dibuat → kirim QR + detail order (`sendImageUrl`). Saat pembayaran sukses → kirim konfirmasi (`sendTemplate`).

### 2. Telegram Bot (merchant) — dibuat via BotFather

- Buat bot baru lewat [@BotFather](https://t.me/BotFather) di Telegram, dapatkan `TELEGRAM_BOT_TOKEN`.
- Set webhook bot ke `https://<domain-anda>/api/telegram/webhook`.
- Registrasi merchant: Keeper membuka chat bot, kirim `/start`, lalu kirim `/daftar <kode_merchant>`. Sistem akan menyimpan `chatId` Telegram ke kolom `telegramChatId` pada entity `Merchant`.
- Setiap kali pembayaran berhasil, `TelegramService.sendPaymentSuccess()` mengirim ringkasan order (order ID, item, subtotal, ongkir, total, tanggal kirim) secara async ke chat Telegram merchant tersebut.

### 3. Email via Mailtrap (fallback tanpa nomor HP)

- Jika saat checkout customer **tidak mengisi nomor telepon**, sistem otomatis mengirim invoice/detail pengiriman lewat **Email** (bukan WhatsApp).
- Menggunakan `JavaMailSender` + template **Thymeleaf** (`resources/templates/email/*.html`: `welcome`, `payment-pending`, `payment`).
- Konfigurasi SMTP:
  - Dev/testing: `sandbox.smtp.mailtrap.io:2525` (Mailtrap Sandbox — email tidak benar-benar terkirim ke inbox asli, hanya masuk ke Mailtrap inbox testing).
  - Production: `live.smtp.mailtrap.io:587` (Mailtrap Sending — email benar-benar terkirim).

---

## Model Data Inti

```
Category ──< Product ──< WarehouseProduct >── Warehouse
                  │
                  └──< MerchantProduct >── Merchant ──< Transaction >── TransactionProduct
                                              │                              │
                                            User (keeper, 1-1)          Product

Role >──< User  (MANAGER, KEEPER, USER)
```

- **Product** — master produk (nama, barcode unik, harga, kategori, foto).
- **WarehouseProduct** — stok produk *di gudang tertentu*.
- **MerchantProduct** — stok produk *yang sudah didistribusikan ke merchant tertentu* (mencatat dari gudang mana asalnya).
- **Transaction / TransactionProduct** — order/checkout customer terhadap satu Merchant, berisi status pembayaran, metode QRIS, dan snapshot harga tiap item.
- **Merchant.keeper** — relasi 1-1 ke `User` yang berperan sebagai penjaga/pengelola merchant tersebut (role `KEEPER`).
- **NotificationOutbox** — antrian pesan WhatsApp yang belum/sedang/sudah terkirim (status + jumlah percobaan).

---

## Persiapan & Instalasi

### Prasyarat

- JDK 17+
- Maven 3.9+
- Docker & Docker Compose (untuk PostgreSQL, dan opsional WAHA)
- Akun [Midtrans Sandbox](https://dashboard.sandbox.midtrans.com/) (untuk QRIS dinamis)
- Kode QRIS statis merchant BCA (untuk mode `bca_qris_static`)
- Bot Telegram dari [@BotFather](https://t.me/BotFather)
- Akun [Mailtrap](https://mailtrap.io/)
- (Opsional) Bucket OCI Object Storage — untuk upload foto
- (Opsional) Project [Infisical](https://infisical.com/) — pengelolaan secret terpusat

### Langkah Instalasi

```bash
# 1. Clone repository
git clone <url-repo-anda>
cd warehouse

# 2. Jalankan PostgreSQL via Docker
docker compose up -d

# 3. Siapkan environment variables (lihat tabel di bawah)
#    Bisa lewat export shell, file .env yang di-load IDE, atau Infisical.

# 4. Jalankan aplikasi
./mvnw spring-boot:run
# atau build jar:
./mvnw clean package -DskipTests
java -jar target/warehouse-0.0.1-SNAPSHOT.jar
```

Aplikasi berjalan di `http://localhost:9090`, seluruh endpoint REST diprefix dengan `/api` (dikonfigurasi di `WebConfig`). Profile default adalah `dev` (`spring.profiles.active=dev`), gunakan `PROFILES=prod` untuk mode production (`ddl-auto: validate`, Mailtrap live SMTP).

Dokumentasi interaktif Swagger tersedia di:
```
http://localhost:9090/api/swagger-ui.html
```

---

## Konfigurasi Layanan Eksternal

### QRIS Statis BCA

1. Ajukan/aktifkan QRIS merchant di BCA (lewat EDC atau BCA Merchant App), dapatkan **kode QRIS statis** (payload EMVCo, biasanya berupa string panjang hasil scan QR statis toko).
2. Set ke environment variable `BBCA_QRIS_STATIC_CODE`.
3. Saat checkout dengan `paymentMethod: "bca_qris_static"`, sistem akan otomatis menyisipkan nominal transaksi ke payload tersebut dan merender QR-nya sendiri (via ZXing) — tidak perlu API call ke BCA.

### Midtrans (QRIS Dinamis, opsional)

1. Daftar di [Midtrans Dashboard](https://dashboard.sandbox.midtrans.com/), ambil **Server Key** dari menu Settings > Access Keys.
2. Set `MIDTRANS_SERVER_KEY` dan `MIDTRANS_IS_PRODUCTION=false` (sandbox) atau `true` (live).
3. Daftarkan Payment Notification URL di dashboard Midtrans ke:
   `https://<domain-anda>/api/payments/notification`

### WAHA (WhatsApp HTTP API)

1. Jalankan WAHA (lihat [dokumentasi resmi WAHA](https://waha.devlike.pro/)), contoh cepat:
   ```bash
   docker run -it -p 3000:3000 devlikeapro/waha
   ```
2. Scan QR di dashboard WAHA (`http://localhost:3000`) untuk login sesi WhatsApp menggunakan nomor pengirim.
3. Set environment:
   - `WAHA_BASE_URL` — base URL instance WAHA (mis. `http://localhost:3000`)
   - `WAHA_API_KEY_PLAIN` — API key WAHA
   - `WAHA_HMAC_KEY` — HMAC key (jika webhook WAHA diaktifkan)
   - Nama sesi WAHA saat ini diset `erina` di `application.yaml` (`waha.session`) — sesuaikan dengan nama sesi WAHA Anda.

### Telegram Bot (BotFather)

1. Chat [@BotFather](https://t.me/BotFather) → `/newbot` → ikuti instruksi (nama & username bot) → dapatkan **Bot Token**.
2. Set `TELEGRAM_BOT_TOKEN`.
3. Daftarkan webhook (setelah aplikasi dapat diakses publik/HTTPS, mis. via ngrok saat development):
   ```bash
   curl -X POST "https://api.telegram.org/bot<TELEGRAM_BOT_TOKEN>/setWebhook" \
        -d "url=https://<domain-anda>/api/telegram/webhook"
   ```
4. Merchant (Keeper) tinggal chat bot → `/start` → `/daftar <kode_merchant>` untuk mengaktifkan notifikasi.

### Mailtrap

1. Buat akun di [Mailtrap](https://mailtrap.io/), ambil kredensial SMTP dari **Sandbox** (testing) untuk `MAIL_USERNAME` & `MAIL_PASSWORD`.
2. Untuk production, buat **Sending Domain** di Mailtrap dan gunakan kredensial live SMTP-nya (host otomatis berbeda lewat profile `prod`).
3. `MAIL_FROM` dan `MAIL_NAME` menentukan alamat & nama pengirim yang tampil di email.

### OCI Object Storage (opsional, untuk upload gambar)

Set variabel `OCI_CONFIG_PATH`, `OCI_CONFIG_PROFILE`, `OCI_OBJECTSTORAGE_NAMESPACE`, `OCI_OBJECTSTORAGE_REGION`, `OCI_OBJECTSTORAGE_BUCKET`, `OCI_CONFIG_AUTH_TYPE` sesuai kredensial OCI Anda. Endpoint upload tersedia di `POST /api/upload/*`.

---

## Environment Variables

| Variabel | Wajib | Contoh / Default | Keterangan |
|---|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | ✅ | `localhost`, `5432`, `warehouse`, `postgres`, `password` | Koneksi PostgreSQL |
| `JWT_SECRET` | ✅ | — | Secret key untuk sign/verify JWT |
| `APP_BASE_URL` | ✅ | `http://localhost:9090` | Dipakai untuk membangun URL QR image publik |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | ✅ | — | Kredensial SMTP Mailtrap |
| `MAIL_FROM`, `MAIL_NAME` | ✅ | — | Nama & alamat pengirim email |
| `TRANSACTION_TAX_RATE` | ❌ | `0` | Rate pajak (mis. `0.11` untuk PPN 11%) |
| `MIDTRANS_SERVER_KEY` | ✅ (jika pakai QRIS dinamis) | — | Server key Midtrans |
| `MIDTRANS_IS_PRODUCTION` | ❌ | `false` | `true` untuk live Midtrans |
| `BBCA_QRIS_STATIC_CODE` | ✅ (jika pakai QRIS statis BCA) | — | Payload EMV QRIS statis dari BCA |
| `TELEGRAM_BOT_TOKEN` | ✅ | — | Token bot dari BotFather |
| `TELEGRAM_BASE_URL` | ❌ | `https://api.telegram.org` | Base URL API Telegram |
| `WAHA_BASE_URL` | ✅ | `http://localhost:3000` | Base URL instance WAHA |
| `WAHA_API_KEY_PLAIN` | ✅ | — | API key WAHA |
| `WAHA_HMAC_KEY` | ❌ | — | HMAC key webhook WAHA |
| `OCI_CONFIG_PATH`, `OCI_CONFIG_PROFILE`, `OCI_OBJECTSTORAGE_NAMESPACE`, `OCI_OBJECTSTORAGE_REGION`, `OCI_OBJECTSTORAGE_BUCKET`, `OCI_CONFIG_AUTH_TYPE`, `OCI_OBJECTSTORAGE_BUCKET_PUBLIC` | ❌ | lihat `application.yaml` | Konfigurasi OCI Object Storage |
| `CORS_ALLOWED_ORIGINS`, `CORS_ALLOWED_METHODS`, `CORS_ALLOWED_HEADERS`, `CORS_ALLOWED_CREDENTIALS`, `CORS_MAX_AGE` | ❌ | lihat `application.yaml` | Konfigurasi CORS untuk frontend |
| `INFISICAL_CLIENT_ID`, `INFISICAL_CLIENT_SECRET`, `INFISICAL_PROJECT_ID`, `INFISICAL_ENVIRONMENT` | ❌ | — | Hanya jika menggunakan Infisical sebagai secret manager |

> Nilai default untuk mode dev ada di `src/main/resources/application.yaml` dan `application-dev.yaml`.

---

## Autentikasi & Role

- Autentikasi menggunakan **JWT Bearer Token**. Login lewat `POST /api/auth/login` mengembalikan `token` yang harus disertakan di header `Authorization: Bearer <token>` untuk endpoint yang membutuhkan.
- Endpoint publik (tanpa token): `/api/auth/**`, `/api/csrf-token`, `/api/payments/notification`, `/api/transactions/*/qr-image`, `/api/telegram/webhook`, Swagger UI, dan Actuator health.
- Role yang tersedia (di-seed otomatis saat pertama kali aplikasi start):
  | Role | Deskripsi |
  |---|---|
  | `MANAGER` | Admin/pengelola pusat — akses penuh ke semua master data & seluruh transaksi merchant |
  | `KEEPER` | Pengelola satu Merchant tertentu — bisa konfirmasi pembayaran & menerima notifikasi Telegram untuk merchant miliknya |
  | `USER` | Role dasar |
- API versioning menggunakan media type parameter, mis. `Accept: application/vnd.johanwork+json;v=1.0` (default `1.0` jika tidak disebutkan).

---

## Dokumentasi API

Base path: `http://localhost:9090/api`
Format response standar: `{ "data": ..., "message": "...", "timestamp": "..." }`, list data dibungkus `PageResponse` (`content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrev`).

### Auth
| Method | Endpoint | Keterangan |
|---|---|---|
| POST | `/auth/login` | Login, mengembalikan JWT token |
| POST | `/auth/register` | Registrasi user baru |
| GET | `/csrf-token` | Ambil CSRF token |

### Users
| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/users` | List user |
| GET | `/users/{id}` | Detail user |
| GET | `/users/role` | List user berdasarkan role |
| POST | `/users` | Tambah user |
| PUT | `/users/{id}` | Update user |
| DELETE | `/users/{id}` | Hapus user |
| GET/POST/PUT | `/assign-role` | Kelola penugasan role ke user |

### Kategori
| Method | Endpoint |
|---|---|
| GET | `/categories`, `/categories/{id}` |
| POST | `/categories` |
| PUT | `/categories/{id}` |
| DELETE | `/categories/{id}` |

### Produk
| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/products` | List produk (pagination: `pageNumber`, `pageSize`, `sortBy`, `sortDirection`, `search`) |
| GET | `/products/{id}` | Detail produk |
| GET | `/products/barcode/{barcode}` | Cari produk via barcode |
| POST | `/products` | Tambah produk |
| PUT | `/products/{id}` | Update produk |
| DELETE | `/products/{id}` | Hapus produk |

### Gudang (Warehouse)
| Method | Endpoint |
|---|---|
| GET | `/warehouses`, `/warehouses/{id}` |
| POST | `/warehouses` |
| PUT | `/warehouses/{id}` |
| DELETE | `/warehouses/{id}` |

### Stok Gudang (WarehouseProduct) — alokasi produk ke gudang
| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/warehouse-products` | List semua stok gudang |
| GET | `/warehouse-products/{warehouseId}` | Stok per gudang |
| GET | `/warehouse-products/{warehouseId}/detail/{productId}` | Detail stok produk di gudang tsb |
| GET | `/warehouse-products/detail/{id}` | Detail by ID |
| GET | `/warehouse-products/detail/products/{productId}` | Detail stok berdasarkan produk |
| GET | `/warehouse-products/detail/products/{productId}/total-stock` | Total stok produk di semua gudang |
| POST | `/warehouse-products` | Alokasikan stok produk baru ke gudang |
| PUT | `/warehouse-products/{id}` | Update stok |
| DELETE | `/warehouse-products/{id}`, `/warehouse-products/detail/products/{productId}` | Hapus alokasi |

### Merchant
| Method | Endpoint |
|---|---|
| GET | `/merchants`, `/merchants/{id}` |
| POST | `/merchants` |
| PUT | `/merchants/{id}` |
| DELETE | `/merchants/{id}` |

### Stok Merchant (MerchantProduct) — distribusi gudang → merchant
| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/merchant-products` | List stok merchant |
| GET | `/merchant-products/{id}` | Detail |
| GET | `/merchant-products/barcode/{barcode}/merchant/{merchantId}` | Cari produk merchant via barcode (untuk kasir/scan) |
| GET | `/merchant-products/product/{productId}/total-stock` | Total stok produk di semua merchant |
| POST | `/merchant-products` | Distribusikan stok dari gudang ke merchant |
| PUT | `/merchant-products/{id}` | Update stok distribusi |
| DELETE | `/merchant-products/{id}`, `/merchant-products/porduct/{productId}` | Hapus distribusi |

### Transaksi & Pembayaran
| Method | Endpoint | Auth | Keterangan |
|---|---|---|---|
| GET | `/transactions/dashboard` | 🔒 | Statistik dashboard (beda hasil untuk Manager vs Keeper) |
| GET | `/transactions` | 🔒 | List transaksi (filter: `search`, `merchantId`, `month`, `year`, + pagination) |
| GET | `/transactions/summary` | 🔒 | Ringkasan transaksi (filter sama dengan di atas) |
| GET | `/transactions/{id}` | 🔒 | Detail item transaksi |
| POST | `/transactions` | 🌐 publik | **Checkout** — membuat order baru & generate QRIS |
| GET | `/transactions/{id}/qr-image` | 🌐 publik | Ambil gambar QR (PNG) hasil generate ZXing |
| POST | `/transactions/{id}/confirm-payment` | 🔒 (Manager/Keeper) | Konfirmasi pembayaran secara manual |
| POST | `/payments/notification` | 🌐 publik (signature-verified) | Webhook notifikasi pembayaran dari Midtrans |

### Telegram
| Method | Endpoint | Keterangan |
|---|---|---|
| POST | `/telegram/webhook` | Webhook update dari Telegram Bot (perintah `/start`, `/daftar <kode>`) |

### Upload File (ke OCI Object Storage)
| Method | Endpoint |
|---|---|
| POST | `/upload/photo` |
| POST | `/upload/product-image` |
| POST | `/upload/category-image` |
| POST | `/upload/warehouse-image` |
| POST | `/upload/merchant-image` |

> Semua endpoint upload menerima `multipart/form-data` dengan field `photo`.

### Roles
| Method | Endpoint |
|---|---|
| GET | `/roles`, `/roles/{id}` |
| POST | `/roles` |
| PUT | `/roles/{id}` |
| DELETE | `/roles/{id}` |

---

Untuk detail request/response body lengkap per endpoint (DTO & skema validasi), buka Swagger UI di `/api/swagger-ui.html` setelah aplikasi berjalan.
