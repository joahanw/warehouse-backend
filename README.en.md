# Warehouse Management & Order System

**Language:** [Indonesia](README.md) | English

A Spring Boot–based warehouse management system that manages the product flow from **warehouse stock → distribution to merchants → sales to customers**, complete with QRIS payments and automated notifications via WhatsApp (WAHA), Telegram Bot, and Email (Mailtrap).

---

## Table of Contents

- [System Overview](#system-overview)
- [Tech Stack](#tech-stack)
- [Business Process](#business-process)
- [QRIS Payment Flow](#qris-payment-flow)
- [Notification Flow](#notification-flow)
- [Core Data Model](#core-data-model)
- [Setup & Installation](#setup--installation)
- [External Service Configuration](#external-service-configuration)
- [Environment Variables](#environment-variables)
- [Authentication & Roles](#authentication--roles)
- [API Documentation](#api-documentation)

---

## System Overview

This application is used by:

- **Manager (Admin)** — manages master data (categories, products, warehouses, merchants, users) and monitors all transactions.
- **Keeper** — the person managing a specific merchant (store keeper), manually verifies payments and receives transaction notifications via Telegram.
- **Customer** — checks out without logging in (public endpoint), pays via QRIS, and receives an invoice via WhatsApp or Email.

---

## Tech Stack

| Need | Technology |
|---|---|
| Language & Framework | Java 17, Spring Boot 4.1 (Web MVC, Security, Data JPA, Validation, Mail, Cache, Actuator) |
| Database | PostgreSQL 17 |
| Auth | JWT (jjwt) |
| QR Code Generator | **ZXing** (`core`, `javase`) — renders the QRIS payload into a PNG image |
| Payment Gateway | **Midtrans** (Core API `/v2/charge`, dynamic QRIS) as an alternative to **BCA Static QRIS** |
| WhatsApp Notification | **WAHA** (WhatsApp HTTP API, self-hosted gateway) |
| Merchant Notification | **Telegram Bot API** (created via BotFather) |
| Email Notification | **Mailtrap** (SMTP) + Thymeleaf email templates |
| Object Storage | Oracle Cloud Infrastructure (OCI) Object Storage — uploads for product/category/warehouse/merchant/user photos |
| Cache | Caffeine |
| API Documentation | springdoc-openapi (Swagger UI) |
| Secret Manager (optional) | Infisical |

---

## Business Process

The core flow of this system follows a **Warehouse → Merchant → Customer** pattern:

```
1. Admin creates a Product (name, barcode, price, category, photo)
        │
        ▼
2. Admin allocates Product stock to a Warehouse
   → entity: WarehouseProduct (warehouse + product + stock)
        │
        ▼
3. Admin distributes stock from the Warehouse to a Merchant for sale
   → entity: MerchantProduct (merchant + product + warehouse + stock)
        │
        ▼
4. Customer checks out products from one Merchant
   → entity: Transaction + TransactionProduct
        │
        ▼
5. System generates a QRIS code (BCA Static via ZXing, or dynamic via Midtrans)
        │
        ▼
6. Customer scans & pays
        │
        ▼
7. Payment confirmation:
   - automatically via Midtrans webhook (signature-verified), or
   - manually by the Manager/Keeper of the related merchant
        │
        ▼
8. On successful payment:
   - MerchantProduct stock is automatically reduced
   - a WhatsApp notification is sent to the customer (if a phone number was given)
   - a Telegram notification is sent to the merchant's Keeper
   - if the customer did not provide a phone number → the invoice/notification is sent via Email (Mailtrap) instead
```

Stock is tracked at two separate levels: **warehouse stock (WarehouseProduct)** vs. **merchant stock (MerchantProduct)**. During distribution, the admin moves a quantity of stock from the warehouse to a merchant; a transaction only reduces stock at the merchant level.

---

## QRIS Payment Flow

The `paymentMethod` field when creating a transaction (`POST /api/transactions`) determines the method used:

### 1. `bca_qris_static` — BCA Static QRIS + ZXing (dynamic amount)

- The merchant has a single static QRIS code issued for its BCA account (obtained from an EDC device or the BCA merchant portal), stored as the raw EMVCo payload in `bca.qris.staticCode`.
- `QrisUtil.injectDynamicAmount()` parses the EMV QRIS payload (tag-length-value), injects the transaction amount tag (tag `54`), switches the Point of Initiation Method to dynamic (tag `01` = `12`), then recalculates the CRC16-CCITT checksum (tag `63`) per the QRIS specification.
- The resulting per-transaction payload is rendered into a PNG QR image using **ZXing** (`QrCodeImageGenerator`), served through the public endpoint `GET /api/transactions/{id}/qr-image`.
- No third-party API call is needed to generate the QR — it's pure local computation. Ideal for merchants that haven't onboarded a payment gateway.

### 2. `qris` — Dynamic QRIS via Midtrans

- Calls the Midtrans Core API `/v2/charge` with order details, items, tax, and shipping cost.
- Midtrans returns a `qrCodeUrl` (a ready-made QR image from Midtrans), a `transactionId`, and an `expiryTime`.
- Incoming payment is verified via the webhook `POST /api/payments/notification`, with the signature verified using `SHA-512(orderId + statusCode + grossAmount + serverKey)`.

### Manual Confirmation

A Manager or Keeper (owner of the related merchant) can also manually confirm the payment status via `POST /api/transactions/{id}/confirm-payment` — useful for the `bca_qris_static` method, which has no automatic webhook (mutation verification is done manually by the Keeper after checking the bank account).

---

## Notification Flow

### 1. WhatsApp via WAHA (customer)

- **WAHA** is a self-hosted WhatsApp HTTP API gateway (can be run via Docker). This application connects to WAHA as a REST client (`wahaRestClient`) with an `X-Api-Key` header.
- Uses an **Outbox pattern**: every message that needs to be sent is first stored in the `notification_outbox` table (entity `NotificationOutbox`), then `NotificationOutboxScheduler` (`@Scheduled(fixedDelay = 15_000)`) processes it every 15 seconds.
- A random delay is applied between sends (`waha.min-delay-ms` to `waha.max-delay-ms`) to avoid spam detection/bans from WhatsApp.
- Message templates are defined in the `WhatsAppTemplate` enum (`PAYMENT_CREATED_V1`, `PAYMENT_CONFIRMED_V1`) with `{{1}}`, `{{2}}`, etc. placeholders.
- When a transaction is created → sends the QR image + order details (`sendImageUrl`). When payment succeeds → sends a confirmation (`sendTemplate`).

### 2. Telegram Bot (merchant) — created via BotFather

- Create a new bot via [@BotFather](https://t.me/BotFather) on Telegram to obtain a `TELEGRAM_BOT_TOKEN`.
- Register the bot's webhook to `https://<your-domain>/api/telegram/webhook`.
- Merchant registration: the Keeper opens a chat with the bot, sends `/start`, then sends `/daftar <merchant_code>`. The system stores the Telegram `chatId` in the `telegramChatId` column of the `Merchant` entity.
- Every time a payment succeeds, `TelegramService.sendPaymentSuccess()` asynchronously sends an order summary (order ID, items, subtotal, shipping, total, delivery date) to that merchant's Telegram chat.

### 3. Email via Mailtrap (fallback without a phone number)

- If the customer **does not enter a phone number** during checkout, the system automatically sends the invoice/delivery details via **Email** instead of WhatsApp.
- Uses `JavaMailSender` + **Thymeleaf** templates (`resources/templates/email/*.html`: `welcome`, `payment-pending`, `payment`).
- SMTP configuration:
  - Dev/testing: `sandbox.smtp.mailtrap.io:2525` (Mailtrap Sandbox — emails aren't actually delivered to a real inbox, they only land in the Mailtrap testing inbox).
  - Production: `live.smtp.mailtrap.io:587` (Mailtrap Sending — emails are actually delivered).

---

## Core Data Model

```
Category ──< Product ──< WarehouseProduct >── Warehouse
                  │
                  └──< MerchantProduct >── Merchant ──< Transaction >── TransactionProduct
                                              │                              │
                                            User (keeper, 1-1)          Product

Role >──< User  (MANAGER, KEEPER, USER)
```

- **Product** — product master data (name, unique barcode, price, category, photo).
- **WarehouseProduct** — product stock *at a specific warehouse*.
- **MerchantProduct** — product stock *already distributed to a specific merchant* (also records which warehouse it came from).
- **Transaction / TransactionProduct** — a customer's order/checkout against a single Merchant, containing payment status, QRIS method, and a price snapshot for each item.
- **Merchant.keeper** — a 1-1 relation to the `User` who acts as the keeper/manager of that merchant (role `KEEPER`).
- **NotificationOutbox** — queue of WhatsApp messages not yet/currently/already sent (status + attempt count).

---

## Setup & Installation

### Prerequisites

- JDK 17+
- Maven 3.9+
- Docker & Docker Compose (for PostgreSQL, and optionally WAHA)
- A [Midtrans Sandbox](https://dashboard.sandbox.midtrans.com/) account (for dynamic QRIS)
- A BCA merchant static QRIS code (for the `bca_qris_static` mode)
- A Telegram bot from [@BotFather](https://t.me/BotFather)
- A [Mailtrap](https://mailtrap.io/) account
- (Optional) An OCI Object Storage bucket — for photo uploads
- (Optional) An [Infisical](https://infisical.com/) project — for centralized secret management

### Installation Steps

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd warehouse

# 2. Start PostgreSQL via Docker
docker compose up -d

# 3. Set up environment variables (see the table below)
#    Via shell export, an .env file loaded by your IDE, or Infisical.

# 4. Run the application
./mvnw spring-boot:run
# or build a jar:
./mvnw clean package -DskipTests
java -jar target/warehouse-0.0.1-SNAPSHOT.jar
```

The application runs at `http://localhost:9090`, and all REST endpoints are prefixed with `/api` (configured in `WebConfig`). The default profile is `dev` (`spring.profiles.active=dev`); use `PROFILES=prod` for production mode (`ddl-auto: validate`, Mailtrap live SMTP).

Interactive Swagger documentation is available at:
```
http://localhost:9090/api/swagger-ui.html
```

---

## External Service Configuration

### BCA Static QRIS

1. Apply for/activate merchant QRIS with BCA (via EDC or the BCA Merchant App) to obtain the **static QRIS code** (the EMVCo payload, usually a long string produced by scanning the store's static QR).
2. Set it as the `BBCA_QRIS_STATIC_CODE` environment variable.
3. When checking out with `paymentMethod: "bca_qris_static"`, the system automatically injects the transaction amount into that payload and renders the QR itself (via ZXing) — no BCA API call is needed.

### Midtrans (Dynamic QRIS, optional)

1. Sign up at the [Midtrans Dashboard](https://dashboard.sandbox.midtrans.com/) and grab the **Server Key** from Settings > Access Keys.
2. Set `MIDTRANS_SERVER_KEY` and `MIDTRANS_IS_PRODUCTION=false` (sandbox) or `true` (live).
3. Register the Payment Notification URL in the Midtrans dashboard as:
   `https://<your-domain>/api/payments/notification`

### WAHA (WhatsApp HTTP API)

1. Run WAHA (see the [official WAHA docs](https://waha.devlike.pro/)), quick example:
   ```bash
   docker run -it -p 3000:3000 devlikeapro/waha
   ```
2. Scan the QR code in the WAHA dashboard (`http://localhost:3000`) to log in a WhatsApp session using the sending number.
3. Set the environment variables:
   - `WAHA_BASE_URL` — base URL of your WAHA instance (e.g. `http://localhost:3000`)
   - `WAHA_API_KEY_PLAIN` — WAHA API key
   - `WAHA_HMAC_KEY` — HMAC key (if the WAHA webhook is enabled)
   - The current WAHA session name is set to `erina` in `application.yaml` (`waha.session`) — adjust it to match your own WAHA session.

### Telegram Bot (BotFather)

1. Chat with [@BotFather](https://t.me/BotFather) → `/newbot` → follow the prompts (bot name & username) → obtain a **Bot Token**.
2. Set `TELEGRAM_BOT_TOKEN`.
3. Register the webhook (once the app is publicly/HTTPS accessible, e.g. via ngrok during development):
   ```bash
   curl -X POST "https://api.telegram.org/bot<TELEGRAM_BOT_TOKEN>/setWebhook" \
        -d "url=https://<your-domain>/api/telegram/webhook"
   ```
4. The merchant (Keeper) simply chats with the bot → `/start` → `/daftar <merchant_code>` to activate notifications.

### Mailtrap

1. Create a [Mailtrap](https://mailtrap.io/) account and grab SMTP credentials from the **Sandbox** (testing) for `MAIL_USERNAME` & `MAIL_PASSWORD`.
2. For production, create a **Sending Domain** in Mailtrap and use its live SMTP credentials (the host automatically differs via the `prod` profile).
3. `MAIL_FROM` and `MAIL_NAME` control the sender address/name shown in the email.

### OCI Object Storage (optional, for image uploads)

Set `OCI_CONFIG_PATH`, `OCI_CONFIG_PROFILE`, `OCI_OBJECTSTORAGE_NAMESPACE`, `OCI_OBJECTSTORAGE_REGION`, `OCI_OBJECTSTORAGE_BUCKET`, `OCI_CONFIG_AUTH_TYPE` according to your OCI credentials. Upload endpoints are available at `POST /api/upload/*`.

---

## Environment Variables

| Variable | Required | Example / Default | Description |
|---|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | ✅ | `localhost`, `5432`, `warehouse`, `postgres`, `password` | PostgreSQL connection |
| `JWT_SECRET` | ✅ | — | Secret key for signing/verifying JWTs |
| `APP_BASE_URL` | ✅ | `http://localhost:9090` | Used to build the public QR image URL |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | ✅ | — | Mailtrap SMTP credentials |
| `MAIL_FROM`, `MAIL_NAME` | ✅ | — | Sender name & address for emails |
| `TRANSACTION_TAX_RATE` | ❌ | `0` | Tax rate (e.g. `0.11` for 11% VAT) |
| `MIDTRANS_SERVER_KEY` | ✅ (if using dynamic QRIS) | — | Midtrans server key |
| `MIDTRANS_IS_PRODUCTION` | ❌ | `false` | `true` for live Midtrans |
| `BBCA_QRIS_STATIC_CODE` | ✅ (if using BCA static QRIS) | — | Static EMV QRIS payload from BCA |
| `TELEGRAM_BOT_TOKEN` | ✅ | — | Bot token from BotFather |
| `TELEGRAM_BASE_URL` | ❌ | `https://api.telegram.org` | Telegram API base URL |
| `WAHA_BASE_URL` | ✅ | `http://localhost:3000` | Base URL of the WAHA instance |
| `WAHA_API_KEY_PLAIN` | ✅ | — | WAHA API key |
| `WAHA_HMAC_KEY` | ❌ | — | WAHA webhook HMAC key |
| `OCI_CONFIG_PATH`, `OCI_CONFIG_PROFILE`, `OCI_OBJECTSTORAGE_NAMESPACE`, `OCI_OBJECTSTORAGE_REGION`, `OCI_OBJECTSTORAGE_BUCKET`, `OCI_CONFIG_AUTH_TYPE`, `OCI_OBJECTSTORAGE_BUCKET_PUBLIC` | ❌ | see `application.yaml` | OCI Object Storage configuration |
| `CORS_ALLOWED_ORIGINS`, `CORS_ALLOWED_METHODS`, `CORS_ALLOWED_HEADERS`, `CORS_ALLOWED_CREDENTIALS`, `CORS_MAX_AGE` | ❌ | see `application.yaml` | CORS configuration for the frontend |
| `INFISICAL_CLIENT_ID`, `INFISICAL_CLIENT_SECRET`, `INFISICAL_PROJECT_ID`, `INFISICAL_ENVIRONMENT` | ❌ | — | Only needed if using Infisical as the secret manager |

> Default values for dev mode live in `src/main/resources/application.yaml` and `application-dev.yaml`.

---

## Authentication & Roles

- Authentication uses **JWT Bearer Tokens**. Logging in via `POST /api/auth/login` returns a `token` that must be included in the `Authorization: Bearer <token>` header for endpoints that require it.
- Public endpoints (no token required): `/api/auth/**`, `/api/csrf-token`, `/api/payments/notification`, `/api/transactions/*/qr-image`, `/api/telegram/webhook`, Swagger UI, and Actuator health.
- Available roles (automatically seeded on first application startup):
  | Role | Description |
  |---|---|
  | `MANAGER` | Central admin/manager — full access to all master data & every merchant's transactions |
  | `KEEPER` | Manager of a specific Merchant — can confirm payments & receives Telegram notifications for their own merchant |
  | `USER` | Base role |
- API versioning uses a media type parameter, e.g. `Accept: application/vnd.johanwork+json;v=1.0` (defaults to `1.0` if not specified).

---

## API Documentation

Base path: `http://localhost:9090/api`
Standard response format: `{ "data": ..., "message": "...", "timestamp": "..." }`; list data is wrapped in a `PageResponse` (`content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrev`).

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/login` | Log in, returns a JWT token |
| POST | `/auth/register` | Register a new user |
| GET | `/csrf-token` | Get a CSRF token |

### Users
| Method | Endpoint | Description |
|---|---|---|
| GET | `/users` | List users |
| GET | `/users/{id}` | User detail |
| GET | `/users/role` | List users by role |
| POST | `/users` | Create a user |
| PUT | `/users/{id}` | Update a user |
| DELETE | `/users/{id}` | Delete a user |
| GET/POST/PUT | `/assign-role` | Manage role assignment to users |

### Categories
| Method | Endpoint |
|---|---|
| GET | `/categories`, `/categories/{id}` |
| POST | `/categories` |
| PUT | `/categories/{id}` |
| DELETE | `/categories/{id}` |

### Products
| Method | Endpoint | Description |
|---|---|---|
| GET | `/products` | List products (pagination: `pageNumber`, `pageSize`, `sortBy`, `sortDirection`, `search`) |
| GET | `/products/{id}` | Product detail |
| GET | `/products/barcode/{barcode}` | Look up a product by barcode |
| POST | `/products` | Create a product |
| PUT | `/products/{id}` | Update a product |
| DELETE | `/products/{id}` | Delete a product |

### Warehouses
| Method | Endpoint |
|---|---|
| GET | `/warehouses`, `/warehouses/{id}` |
| POST | `/warehouses` |
| PUT | `/warehouses/{id}` |
| DELETE | `/warehouses/{id}` |

### Warehouse Stock (WarehouseProduct) — allocating products to a warehouse
| Method | Endpoint | Description |
|---|---|---|
| GET | `/warehouse-products` | List all warehouse stock |
| GET | `/warehouse-products/{warehouseId}` | Stock for a specific warehouse |
| GET | `/warehouse-products/{warehouseId}/detail/{productId}` | Stock detail of a product in that warehouse |
| GET | `/warehouse-products/detail/{id}` | Detail by ID |
| GET | `/warehouse-products/detail/products/{productId}` | Stock detail by product |
| GET | `/warehouse-products/detail/products/{productId}/total-stock` | Total product stock across all warehouses |
| POST | `/warehouse-products` | Allocate new product stock to a warehouse |
| PUT | `/warehouse-products/{id}` | Update stock |
| DELETE | `/warehouse-products/{id}`, `/warehouse-products/detail/products/{productId}` | Remove an allocation |

### Merchants
| Method | Endpoint |
|---|---|
| GET | `/merchants`, `/merchants/{id}` |
| POST | `/merchants` |
| PUT | `/merchants/{id}` |
| DELETE | `/merchants/{id}` |

### Merchant Stock (MerchantProduct) — distribution from warehouse → merchant
| Method | Endpoint | Description |
|---|---|---|
| GET | `/merchant-products` | List merchant stock |
| GET | `/merchant-products/{id}` | Detail |
| GET | `/merchant-products/barcode/{barcode}/merchant/{merchantId}` | Look up a merchant product by barcode (for cashier/scanning) |
| GET | `/merchant-products/product/{productId}/total-stock` | Total product stock across all merchants |
| POST | `/merchant-products` | Distribute stock from a warehouse to a merchant |
| PUT | `/merchant-products/{id}` | Update distributed stock |
| DELETE | `/merchant-products/{id}`, `/merchant-products/porduct/{productId}` | Remove a distribution |

### Transactions & Payments
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/transactions/dashboard` | 🔒 | Dashboard statistics (differs for Manager vs. Keeper) |
| GET | `/transactions` | 🔒 | List transactions (filters: `search`, `merchantId`, `month`, `year`, plus pagination) |
| GET | `/transactions/summary` | 🔒 | Transaction summary (same filters as above) |
| GET | `/transactions/{id}` | 🔒 | Transaction item detail |
| POST | `/transactions` | 🌐 public | **Checkout** — creates a new order & generates the QRIS code |
| GET | `/transactions/{id}/qr-image` | 🌐 public | Fetch the QR image (PNG) generated by ZXing |
| POST | `/transactions/{id}/confirm-payment` | 🔒 (Manager/Keeper) | Manually confirm payment |
| POST | `/payments/notification` | 🌐 public (signature-verified) | Payment notification webhook from Midtrans |

### Telegram
| Method | Endpoint | Description |
|---|---|---|
| POST | `/telegram/webhook` | Telegram Bot update webhook (`/start`, `/daftar <code>` commands) |

### File Upload (to OCI Object Storage)
| Method | Endpoint |
|---|---|
| POST | `/upload/photo` |
| POST | `/upload/product-image` |
| POST | `/upload/category-image` |
| POST | `/upload/warehouse-image` |
| POST | `/upload/merchant-image` |

> All upload endpoints accept `multipart/form-data` with a `photo` field.

### Roles
| Method | Endpoint |
|---|---|
| GET | `/roles`, `/roles/{id}` |
| POST | `/roles` |
| PUT | `/roles/{id}` |
| DELETE | `/roles/{id}` |

---

For the full request/response body per endpoint (DTOs & validation schema), open Swagger UI at `/api/swagger-ui.html` once the application is running.
