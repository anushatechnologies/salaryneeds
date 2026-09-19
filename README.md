# 🚀 SalaryNeeds Partner Operations — Backend Service

Spring Boot backend implementation matching the **SalaryNeeds Partner Operations Backend API Specification (Version 2.0)** for the Technician / Partner Mobile App (React Native Expo / TypeScript).

---

## 🏗️ Architecture & Features

1. **Authentication & Profile (Module A)**:
   - Partner registration (`POST /worker/auth/register`) with Indian mobile number regex (`^[6-9]\d{9}$`)
   - 4-digit SMS OTP issuance and validation (`POST /worker/auth/send-otp`, `POST /worker/auth/login`)
   - 30-day JWT Bearer token generation and scorecard retrieval (`GET /worker/profile/me`)

2. **Duty Radar & Telemetry (Module B)**:
   - On/Off duty toggle (`PATCH /worker/profile/duty`) with **zero prepaid wallet balance barrier**
   - High-frequency GPS telemetry pings (`POST /worker/location/ping`)

3. **Dispatch & Booking Engine (Module C & D)**:
   - Radius radar search (`GET /worker/bookings/nearby-leads`)
   - Atomic lead claiming (`POST /worker/bookings/{id}/accept`) with 409 conflict guard
   - Doorstep job execution state machine: `ACCEPTED` ➔ `EN_ROUTE` ➔ `ARRIVED` ➔ `IN_PROGRESS`
   - Real-time task checklists (`POST /worker/bookings/{id}/checklist/{itemId}/toggle`)
   - Billable replacement parts addition (`POST /worker/bookings/{id}/extra-parts`)
   - **Mandatory 4-digit customer completion OTP verification** (`POST /worker/bookings/{id}/verify-otp`) which completes the job and instantly credits 100% net labor fee to the technician's revenue ledger

4. **Work Schedule & Roster (Module E)**:
   - Daily 6 fixed 2-hour duty slots (08:00 AM – 08:00 PM) for `today`, `tomorrow`, or arbitrary dates (`GET /worker/schedule`)

5. **Automated Nightly Bank Settlement (Module F)**:
   - Real-time earnings ledger (`GET /worker/wallet`)
   - Bank account linking and verification (`POST /worker/bank/update`)
   - **Automated Nightly Batch Disbursement Cron** (`0 59 23 * * ?`) at 11:59 PM directly to verified bank accounts via IMPS/NEFT

6. **KYC & Documents (Module G)**:
   - S3 pre-signed upload URL generator (`POST /worker/documents/upload-url`)
   - Document verification confirmation (`POST /worker/documents`)

7. **Reviews & Device Alarms (Module H & I)**:
   - Rating stats and customer feedback (`GET /worker/reviews`)
   - Expo Push Token & FCM registration (`POST /worker/devices/register-token`)

8. **WebSockets (Section 5)**:
   - Telemetry and live radar stream at `/ws/worker?token=<jwt>`
   - Events: `location:update`, `dispatch:new_lead`, `booking:claimed`, `booking:cancelled`, `settlement:completed`

9. **Standard Error Envelope (Section 6)**:
   - Strict adherence to error response envelope with standard codes (`INVALID_PAYLOAD`, `INVALID_OTP`, `UNAUTHORIZED`, `ACCOUNT_NOT_ACTIVE`, `BOOKING_NOT_FOUND`, `PHONE_ALREADY_EXISTS`, `LEAD_ALREADY_CLAIMED`, `UNPROCESSABLE_STATUS`, `INTERNAL_SERVER_ERROR`).

---

## 🚀 Running the Application

### 1. Requirements
* Java 17, 21, or 24
* Maven 3.8+ (or use the bundled `apache-maven-3.9.9`)

### 2. Quick Local Start (Plug-and-Play H2 Database)
The application is preconfigured to start out-of-the-box using file-backed H2:
```bash
./apache-maven-3.9.9/bin/mvn spring-boot:run
```

### 3. Running with PostgreSQL or MySQL
Configure the following environment variables or add them to your `.env` / `application.properties`:

**PostgreSQL**:
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/salaryneeds?sslmode=prefer
DB_USER=postgres
DB_PASSWORD=your_password
DB_DRIVER=org.postgresql.Driver
```

**MySQL**:
```properties
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/salaryneeds?useSSL=false&allowPublicKeyRetrieval=true
DB_USER=root
DB_PASSWORD=your_password
DB_DRIVER=com.mysql.cj.jdbc.Driver
```

### 4. Running Tests
```bash
./apache-maven-3.9.9/bin/mvn test
```
