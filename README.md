# Checkd — Native Android Hackathon Event Attendance App

Checkd is a clean, minimal, mobile-first Android application engineered for event volunteers and organizers to scan participant QR codes rapidly and reliably across hackathon activities (Registration, Food Distribution, Workshops, Technical Sessions).

The UI is designed to be pixel-accurate to the monochrome high-contrast design specification:
- **Home**: Single screen with rounded activity cards, bold progress counters, and real-time stats.
- **Event Detail**: Scanned participant feed with search and a prominent full-width **Scan QR Code** button.
- **Camera Scanner**: Dedicated full-screen CameraX scanner with corner bracket overlays, animated scan line, continuous scanning with ML Kit, and slide-up feedback states.
- **Offline-First Synchronization**: Pulls cloud updates on launch (detecting changes via `sync_metadata`), caches data in Room, performs instant offline validation and check-ins, and synchronizes batched check-ins via PostgreSQL RPC when network connectivity returns.
- **Discreet Admin Access**: Protected admin portal hidden behind the Settings menu, authenticated via Supabase Auth.

---

## Technology Stack

- **Language:** Kotlin (2.0+)
- **UI Framework:** Jetpack Compose with Material 3 (Monochrome design system)
- **Backend / Database:** Supabase PostgreSQL with `supabase-kt` 3.x
- **Camera & Scanning:** CameraX 1.4 + Google ML Kit Barcode Scanning
- **Local Persistence:** Room Database (Offline sync queue & cached entities)
- **Asynchronous Flow:** Kotlin Coroutines & StateFlow
- **Dependency Injection:** Dagger Hilt
- **Navigation:** Navigation Compose
- **Background Sync:** WorkManager + Android ConnectivityManager callbacks

---

## Project Structure

```text
com.example.eventattendance
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── EventDao.kt
│   │   │   ├── ParticipantDao.kt
│   │   │   ├── EligibilityDao.kt
│   │   │   ├── AttendanceDao.kt
│   │   │   ├── PendingScanDao.kt
│   │   │   └── SyncMetaDao.kt
│   │   └── entity/
│   │       ├── EventEntity.kt
│   │       ├── ParticipantEntity.kt
│   │       ├── EventEligibilityEntity.kt
│   │       ├── AttendanceRecordEntity.kt
│   │       ├── PendingScanEntity.kt
│   │       └── SyncMetaEntity.kt
│   │
│   ├── remote/
│   │   ├── datasource/
│   │   │   ├── EventRemoteDataSource.kt
│   │   │   ├── ParticipantRemoteDataSource.kt
│   │   │   ├── AttendanceRemoteDataSource.kt
│   │   │   ├── AuthRemoteDataSource.kt
│   │   │   └── SyncRemoteDataSource.kt
│   │   └── dto/
│   │       ├── EventDto.kt
│   │       ├── ParticipantDto.kt
│   │       ├── AttendanceRecordDto.kt
│   │       ├── EligibilityDto.kt
│   │       ├── SyncMetadataDto.kt
│   │       └── RpcAttendanceResponse.kt
│   │
│   └── repository/
│       ├── EventRepositoryImpl.kt
│       ├── AttendanceRepositoryImpl.kt
│       ├── ParticipantRepositoryImpl.kt
│       ├── AuthRepositoryImpl.kt
│       └── SyncRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Event.kt
│   │   ├── Participant.kt
│   │   ├── AttendanceRecord.kt
│   │   ├── EventWithStats.kt
│   │   ├── ScanResult.kt
│   │   └── SyncState.kt
│   │
│   ├── repository/
│   │   ├── EventRepository.kt
│   │   ├── AttendanceRepository.kt
│   │   ├── ParticipantRepository.kt
│   │   ├── AuthRepository.kt
│   │   └── SyncRepository.kt
│   │
│   └── usecase/
│       ├── GetEventsWithStatsUseCase.kt
│       ├── GetEventDetailUseCase.kt
│       ├── ProcessScanUseCase.kt
│       ├── SearchParticipantsUseCase.kt
│       ├── SyncDataUseCase.kt
│       ├── AdminLoginUseCase.kt
│       └── ManageEventsUseCase.kt
│
├── presentation/
│   ├── common/
│   │   ├── CheckdTopAppBar.kt
│   │   └── OfflineStatusBanner.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── components/
│   │       └── EventCard.kt
│   ├── eventdetails/
│   │   ├── EventDetailScreen.kt
│   │   ├── EventDetailViewModel.kt
│   │   └── components/
│   │       ├── ParticipantListItem.kt
│   │       └── StatusBadge.kt
│   ├── scanner/
│   │   ├── ScannerScreen.kt
│   │   ├── ScannerViewModel.kt
│   │   ├── BarcodeAnalyzer.kt
│   │   └── components/
│   │       ├── ScannerOverlay.kt
│   │       ├── ScanFeedbackBanner.kt
│   │       └── CameraControls.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   ├── authentication/
│   │   ├── AdminAuthScreen.kt
│   │   └── AdminAuthViewModel.kt
│   ├── admin/
│   │   ├── AdminDashboardScreen.kt
│   │   ├── AdminViewModel.kt
│   │   └── components/
│   │       └── EventFormDialog.kt
│   └── theme/
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
│
├── navigation/
│   ├── Screen.kt
│   └── AppNavigation.kt
│
├── sync/
│   ├── ConnectivityObserver.kt
│   ├── NetworkConnectivityObserver.kt
│   ├── SyncManager.kt
│   └── PendingScanSyncWorker.kt
│
├── util/
│   ├── HapticFeedbackUtil.kt
│   ├── AudioFeedbackUtil.kt
│   └── DateUtils.kt
│
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
│
├── CheckdApplication.kt
└── MainActivity.kt
```

---

## Supabase Configuration & Database Setup

### 1. Configure Credentials
Add your Supabase project credentials to `local.properties` (never committed to git):

```properties
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your-supabase-publishable-anon-key
```

### 2. Deploy Database Schema
Execute the SQL script located at `supabase/schema.sql` in the **Supabase SQL Editor**:
- Creates `participants`, `events`, `event_eligibility`, `attendance_records`, `users`, `attendance_audit_logs`, and `sync_metadata`.
- Installs triggers to automatically update `sync_metadata.last_modified_at` when events or participants are created or changed.
- Configures Row Level Security (RLS) policies.
- Installs atomic PostgreSQL RPC functions:
  - `mark_attendance(p_event_id, p_qr_token)`: Handles single scan check-ins.
  - `mark_bulk_attendance(p_event_id, p_student_ids)`: Handles batch synchronization of offline queues with `ON CONFLICT DO NOTHING`.

### 3. Deploy Demo / Test Data
Execute `supabase/seed.sql` in the Supabase SQL Editor:
- Creates an admin user (`admin@hackathon.org`).
- Populates the activities matching the UI reference:
  - **Event 1:** 0 / 200
  - **Breakfast:** 150 / 200 (75%)
  - **Lunch:** 120 / 200 (60%)
  - **Dinner:** 80 / 200 (40%)
- Inserts 200 participants with unique opaque QR tokens (`QR_TOK_1RV22CS001`, `QR_TOK_1RV22CS017`, etc.).
- Pre-seeds recent check-in records for Lunch (e.g. Aditya Sharma, Saanvi Rao, Mohan Kulkarni, Ishita Verma, Rohan Mehta).

---

## Offline Synchronization Architecture

```text
[App Launch / Refresh]
       │
       ▼
Check Cloud 'sync_metadata' timestamp
       │
 ┌─────┴────────────────────────────────────────────────┐
 │ Has cloud changed vs local?                          │
 ├──────────────────────────────┬───────────────────────┤
 │ YES                          │ NO                    │
 │ 1. Wipe events,              │ Keep existing Room    │
 │    participants, eligibility │ cache as-is           │
 │ 2. Re-insert fresh snapshot  │                       │
 │ 3. PRESERVE 'pending_scans'  │                       │
 └──────────────────────────────┴───────────────────────┘
       │
       ▼
[Offline QR Scan]
 1. Lookup participant by qr_token in local Room DB
 2. Validate eligibility & duplicate status locally
 3. Insert into 'pending_scans' (PENDING)
 4. Show instant UI confirmation (Green / Yellow / Red) + Haptic/Sound
       │
       ▼
[Internet Restored]
 1. ConnectivityObserver fires NetworkCallback
 2. Batch-push pending scans to Supabase RPC 'mark_bulk_attendance'
 3. Mark scans as SYNCED
```

---

## Sample QR Code Tokens for Testing

You can generate QR codes using any online QR generator (or phone screen) with these raw string values:

| Participant Name | USN | QR Code Content | Status for Lunch in Seed Data |
|---|---|---|---|
| Aditya Sharma | 1RV22CS001 | `QR_TOK_1RV22CS001` | Already Checked In (Yellow warning) |
| Saanvi Rao | 1RV22CS017 | `QR_TOK_1RV22CS017` | Already Checked In (Yellow warning) |
| Mohan Kulkarni | 1RV22CS023 | `QR_TOK_1RV22CS023` | Already Checked In (Yellow warning) |
| Participant 150 | 1RV22CS150 | `QR_TOK_1RV22CS150` | Not checked in (Successful Green Check-in) |
| Participant 151 | 1RV22CS151 | `QR_TOK_1RV22CS151` | Not checked in (Successful Green Check-in) |
| Unknown Person | N/A | `INVALID_CODE_12345` | Invalid QR (Red error) |

---

## Hidden Admin Mode

1. From the Home screen, tap the **Settings** gear icon in the top-right corner.
2. Scroll to the bottom of the Settings screen.
3. Tap **Admin Access**.
4. Log in using the administrator email (e.g., `admin@hackathon.org`) configured in Supabase.
5. Once verified against `users.role = 'admin'`, the **Admin Panel** unlocks:
   - Create new events
   - Toggle events Open/Closed
   - View real-time attendee statistics
   - Export attendance data
