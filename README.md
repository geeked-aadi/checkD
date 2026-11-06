# Checkd — Native Android Hackathon Event Attendance App

Checkd is a clean, minimal, mobile-first Android application engineered for event volunteers and organizers to scan participant QR codes rapidly and reliably across hackathon activities (Registration, Food Distribution, Workshops, Technical Sessions).

The UI is designed to be pixel-accurate to the monochrome high-contrast design specification:
- **Home**: Single screen with rounded activity cards, bold progress counters, and real-time stats referring directly to the Supabase `events` table.
- **Event Detail**: Scanned participant feed with search and a prominent full-width **Scan QR Code** button.
- **Camera Scanner**: Dedicated full-screen CameraX scanner with corner bracket overlays, animated scan line, continuous scanning with Google ML Kit, and slide-up feedback states.
- **Direct Supabase Cloud Integration**: Direct PostgREST queries against Supabase PostgreSQL (Room DB removed), validating participant IDs (`p_id`) and recording live attendance timestamps.
- **Discreet Admin Access**: Protected admin portal hidden behind the Settings menu, authenticated via Supabase Auth.

---

## Technology Stack

- **Language:** Kotlin (2.0+)
- **UI Framework:** Jetpack Compose with Material 3 (Monochrome design system)
- **Backend / Database:** Supabase PostgreSQL with `supabase-kt` 3.x
- **Camera & Scanning:** CameraX 1.4 + Google ML Kit Barcode Scanning
- **Asynchronous Flow:** Kotlin Coroutines & StateFlow
- **Dependency Injection:** Dagger Hilt
- **Navigation:** Navigation Compose
- **Connectivity:** Android ConnectivityManager callbacks

---

## Database Schema

Checkd integrates directly with Supabase PostgreSQL using the following relational schema:

### 1. `public.teams`
- `team_id` (VARCHAR, PK)
- `team_name` (VARCHAR)
- `team_leader` (VARCHAR, FK -> `participants.p_id`)
- `spoc_id` (VARCHAR, FK -> `spoc.spoc_id`)

### 2. `public.participants`
- `p_id` (VARCHAR, PK) — Unique participant identifier encoded in QR codes
- `name` (VARCHAR) — Participant's full name
- `team_id` (VARCHAR, FK -> `teams.team_id`)
- `phonenumber` (NUMERIC)

### 3. `public.spoc`
- `spoc_id` (VARCHAR, PK)
- `name` (VARCHAR)
- `phonenumber` (NUMERIC)

### 4. `public.events`
- `event_id` (INTEGER, PK)
- `event_name` (VARCHAR)
- `starts_from` (TIMESTAMP WITHOUT TIME ZONE)
- `ends_on` (TIMESTAMP WITHOUT TIME ZONE)

### 5. `public.attendance`
- `p_id` (VARCHAR, FK -> `participants.p_id`)
- `event_id` (INTEGER, FK -> `events.event_id`)
- `time` (TIMESTAMP WITHOUT TIME ZONE)

---

## Project Structure

```text
com.example.eventattendance
│
├── data/
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
│   │       ├── TeamDto.kt
│   │       ├── SpocDto.kt
│   │       ├── AttendanceDto.kt
│   │       ├── FlexibleStringSerializer.kt
│   │       └── SyncMetadataDto.kt
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
│   └── SyncManager.kt
│
├── util/
│   ├── HapticFeedbackUtil.kt
│   ├── AudioFeedbackUtil.kt
│   └── DateUtils.kt
│
├── di/
│   ├── AppModule.kt
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
- Creates `spoc`, `teams`, `participants`, `events`, and `attendance`.
- Configures Row Level Security (RLS) policies.

### 3. Deploy Demo / Test Data
Execute `supabase/seed.sql` in the Supabase SQL Editor to populate sample SPOCs, teams, participants, events, and attendance records.

---

## Sample QR Code Tokens for Testing

When testing the QR barcode scanner, participants' QR codes encode their `p_id` values:

| Participant Name | Participant ID (`p_id`) | Team | Seed Status for Event 1 |
|---|---|---|---|
| Aditya Sharma | `P101` | TEAM_A | Already Checked In (Yellow warning) |
| Saanvi Rao | `P102` | TEAM_A | Already Checked In (Yellow warning) |
| Mohan Kulkarni | `P103` | TEAM_B | Already Checked In (Yellow warning) |
| Ishita Verma | `P104` | TEAM_B | Not checked in (Successful Green Check-in) |
| Rohan Mehta | `P105` | TEAM_C | Not checked in (Successful Green Check-in) |
| Unknown / Invalid | `INVALID_CODE_999` | N/A | Invalid QR (Red error) |
