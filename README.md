# CareLink 🏥

> **Welfare Check-In Android Application**  
> Northampton Council — Non-Emergency Welfare Coordination Platform

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose)
![Database](https://img.shields.io/badge/Database-SQLite-003B57?style=flat&logo=sqlite)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-FF6F00?style=flat)
![Min SDK](https://img.shields.io/badge/Min%20SDK-API%2026-brightgreen?style=flat)
![Assessment](https://img.shields.io/badge/CSY2091-PJ1%2070%25-1565C0?style=flat)

---

## 📋 Overview

CareLink is a secure, role-based Android mobile application built for **Northampton Council** to coordinate non-emergency welfare check-ins for vulnerable residents. The application digitises the entire welfare request lifecycle — from initial submission by a resident or carer, through coordinator review and prioritisation, to field visit completion by a Community Support Worker and final safeguarding verification.

### The problem it solves

Before CareLink, welfare check-in coordination relied on informal paper-based processes with no centralised visibility, no audit trail, no structured escalation pathway, and no status communication back to residents.

---

## ✨ Features

### Core Features (Assessment Requirements)

| Feature | Role | Status |
|---------|------|--------|
| Email/password authentication | All | ✅ |
| Self-registration for residents/carers | Resident/Carer | ✅ |
| Session persistence across restarts | All | ✅ |
| Role-based navigation and access control | All | ✅ |
| Submit welfare request (draft + submit) | Resident/Carer | ✅ |
| Status timeline with timestamps | Resident/Carer | ✅ |
| Coordinator request inbox with filters | Coordinator | ✅ |
| Priority setting (Low/Medium/High) | Coordinator | ✅ |
| Worker assignment with deadline | Coordinator | ✅ |
| Visit list sorted by priority + deadline | Support Worker | ✅ |
| Visit completion with outcome recording | Support Worker | ✅ |
| Safeguarding review queue | Reviewer | ✅ |
| Verify or escalate completed visits | Reviewer | ✅ |
| Full audit trail (who did what and when) | All | ✅ |
| Deadline indicators ("Due in X days") | All | ✅ |
| Overdue request highlighting | All | ✅ |

### Additional Features (Very Strong Pass)

| # | Feature | Description |
|---|---------|-------------|
| A | **Deadline Templates** | Auto-set deadline from category — Wellbeing: 4hrs, Missed Contact: 24hrs, Routine: 72hrs |
| B | **Overdue Queue** | Dedicated coordinator screen showing all requests past their deadline |
| C | **Mandatory Notes Gate** | Support worker cannot mark visit complete without entering outcome notes |
| D | **Structured Escalation Reasons** | Reviewer selects from 5 predefined escalation categories |
| E | **Dark Mode Toggle** | Global light/dark theme toggle accessible from coordinator dashboard |

---

## 🏗️ Architecture

```
CareLink/
├── data/
│   ├── DatabaseHelper.kt       ← SQLiteOpenHelper — 5 tables + indexes + seed data
│   ├── dao/
│   │   ├── UserDao.kt
│   │   ├── RequestDao.kt       ← Core CRUD + filtered queries
│   │   ├── VisitDao.kt
│   │   ├── ReviewDao.kt
│   │   └── AuditDao.kt         ← Governance audit trail
│   └── model/
│       └── Models.kt           ← Data classes + constants (UserRole, RequestStatus, etc.)
├── viewmodel/
│   ├── AuthViewModel.kt        ← Login, register, session management
│   └── RequestViewModel.kt     ← All request lifecycle business logic
├── ui/
│   ├── auth/                   ← LoginScreen, RegisterScreen
│   ├── resident/               ← ResidentHomeScreen, NewRequestScreen, RequestDetailScreen
│   ├── coordinator/            ← CoordInboxScreen, AssignScreen, OverdueQueueScreen
│   ├── worker/                 ← WorkerVisitScreen, CompleteVisitScreen
│   ├── reviewer/               ← ReviewQueueScreen, ReviewDetailScreen
│   ├── shared/                 ← StatusTimeline, DeadlineBadge, StatusBadge, PriorityBadge
│   └── theme/                  ← Theme.kt — Material 3 light/dark color schemes
├── navigation/
│   └── AppNavGraph.kt          ← Single NavHost, all routes, session-driven start destination
└── util/
    ├── SessionManager.kt       ← SharedPreferences wrapper for session persistence
    └── DeadlineUtils.kt        ← Deadline label generation + template logic
```

### Pattern: MVVM

```
UI Layer (Composable screens)
    ↕ StateFlow / mutableStateOf
ViewModel Layer (business logic, survives rotation)
    ↕ ContentValues / Cursor
Data Layer (DAOs → SQLiteOpenHelper → SQLite)
```

---

## 🗄️ Database Schema

```sql
users           — id, email, password, full_name, role
requests        — id, resident_id, title, description, category, address,
                  status, priority, deadline_ts, assigned_to, coordinator_notes
visit_outcomes  — id, request_id, worker_id, notes, started_at, completed_at
reviews         — id, request_id, reviewer_id, outcome, escalation_reason, notes
audit_log       — id, request_id, actor_id, action, detail, timestamp

-- Indexes for performance (NFR 8)
CREATE INDEX idx_requests_status   ON requests(status);
CREATE INDEX idx_requests_deadline ON requests(deadline_ts);
CREATE INDEX idx_requests_assigned ON requests(assigned_to);
CREATE INDEX idx_audit_request     ON audit_log(request_id);
```

### Status Lifecycle

```
DRAFT → SUBMITTED → UNDER_REVIEW → ASSIGNED → VISIT_COMPLETED → VERIFIED
                                                               ↘ ESCALATED
```

Every status transition writes a timestamped entry to `audit_log`.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Panda (2025.3.4) or later
- JDK 11 (bundled with Android Studio)
- Android device or emulator running API 26+

### Setup

```bash
# Clone the repository
git clone https://github.com/Derahenry/CareLink.git

# Open in Android Studio
# File → Open → select the CareLink folder

# Sync Gradle
# File → Sync Project with Gradle Files

# Run the app
# Select your device/emulator → Press Run (▶)
```

### First Launch

The database is seeded automatically on first launch with the following staff accounts:

| Name | Email | Password | Role |
|------|-------|----------|------|
| Jane Cooper | coordinator@carelink.com | password123 | Care Coordinator |
| Tom Harris | worker1@carelink.com | password123 | Support Worker |
| Amy Singh | worker2@carelink.com | password123 | Support Worker |
| Dr. Paul Webb | reviewer@carelink.com | password123 | Safeguarding Reviewer |

Residents and carers **self-register** via the Register screen.

---

## 📱 Screens

| Screen | Role | Description |
|--------|------|-------------|
| LoginScreen | All | Email/password authentication |
| RegisterScreen | Resident/Carer | Self-registration |
| ResidentHomeScreen | Resident/Carer | Request list + FAB to create |
| NewRequestScreen | Resident/Carer | Request form with draft/submit |
| RequestDetailScreen | Resident/Carer | Status timeline with timestamps |
| CoordInboxScreen | Coordinator | Filterable inbox + dark mode toggle |
| AssignScreen | Coordinator | Priority, deadline template, worker picker |
| OverdueQueueScreen | Coordinator | Red-themed overdue request list |
| WorkerVisitScreen | Worker | Priority-sorted visit assignments |
| CompleteVisitScreen | Worker | Mandatory outcome notes + completion |
| ReviewQueueScreen | Reviewer | Completed visits awaiting review |
| ReviewDetailScreen | Reviewer | Verify or escalate with structured reason |

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Kotlin | 2.x | Primary language |
| Jetpack Compose | BOM latest | UI framework |
| Navigation Compose | 2.9.8 | Screen navigation |
| ViewModel Compose | 2.10.0 | State management |
| Material Icons Extended | 1.7.8 | Icon set |
| SQLite (built-in) | API 26+ | Local database |
| SharedPreferences (built-in) | API 26+ | Session management |

> **Note:** Room ORM is intentionally **not used**. Raw `SQLiteOpenHelper` with `ContentValues` and `Cursor` is used to match the CSY2091 module teaching.

---

## 🔀 Git Strategy

```
main                    ← stable, always runnable
  └── dev               ← integration branch
        ├── feature/sprint1-foundation
        ├── feature/sprint2-auth
        └── feature/sprint3-screens
```

### Commit format

```
feat: add deadline template selector to coordinator assign screen
fix: crash when audit log is empty on first launch
test: add black box test for mandatory notes gate
refactor: extract StatusBadge into shared composable
```

---

## 🧪 Testing

Black box testing was conducted across all 6 functional areas:

| Area | Test Cases | Passed | Failed |
|------|-----------|--------|--------|
| Authentication & Session | 11 | 11 | 0 |
| Resident / Request Submission | 6 | 6 | 0 |
| Coordinator Inbox & Assignment | 11 | 11 | 0 |
| Support Worker Visit Management | 6 | 6 | 0 |
| Safeguarding Review & Escalation | 6 | 6 | 0 |
| Audit Trail & Cross-cutting | 10 | 10 | 0 |
| **Total** | **50** | **50** | **0** |

---

## ⚠️ Known Limitations

- Passwords stored as plain text — acceptable at module level; production implementation would use bcrypt hashing
- No multi-device synchronisation — all data is local to the device (by design, per assessment scope)
- No push notifications — status updates visible on app reopen (per assessment specification)
- Data lost on app uninstall — local SQLite only

---

## 🔮 Future Enhancements

- Password hashing (bcrypt) for production security
- Cloud backend (Firebase / REST API) for multi-device sync
- Push notifications for deadline alerts and status changes
- Data export to CSV for council reporting
- Biometric authentication for staff roles
- Offline-first with background sync queue

---

## 📚 References

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Android SQLite Guide](https://developer.android.com/training/data-storage/sqlite)
- [Material Design 3](https://m3.material.io/)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [SharedPreferences Guide](https://developer.android.com/training/data-storage/shared-preferences)

---

## 👤 Author

**Chidera Nwokolo**  
University of Northampton — BSc Computer Science  
Module: CSY2091 Mobile Application Development  
Assessment: PJ1 (70%)  
Submission: May 2026

---

*CareLink — Built for Northampton Council. CSY2091 Mobile Application Development.*
