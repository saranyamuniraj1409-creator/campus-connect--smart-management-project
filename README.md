# CampusConnect – Smart College Management System

A production-grade, responsive College Management Portal with 4 distinct user roles:
- **Student**
- **Class Advisor**
- **Head of Department (HOD)**
- **College Admin**

---

## 🚀 Live Demo & Ready-to-Test Accounts

CampusConnect comes preloaded with realistic collegiate records for testing each role:

| Role | Email | Password | Name / Title | Scope / Permissions |
| :--- | :--- | :--- | :--- | :--- |
| **Student** | `saranya@campus.edu` / `student@campus.edu` | `student123` | Saranya (Reg: 711025205043) | View own profile, attendance gauge (91.5%), internal marks, CGPA (9.12), timetable, assignments & submissions, leave application, grievances. |
| **Class Advisor** | `advisor@campus.edu` | `advisor123` | Dr. Priya Raman (CSE Dept) | Class CSE-3A: roster, roll call & attendance register, marks upload, timetable, leave approval, low attendance alerts (<75%). |
| **HOD** | `hod@campus.edu` | `hod123` | Dr. Rajesh Natarajan (HOD CSE) | Department CSE: analytics, faculty records, pass rate trends, escalated approvals, audit reports. |
| **College Admin** | `admin@campus.edu` | `admin123` | Dr. K. S. Sundaram (Dean) | Institution-wide: user account provisioning, department & course management, college broadcast, system policies. |

*Tip: You can also use the **Quick Demo Switcher** button at any time to switch roles with a single tap.*

---

## 🏛️ System Features by Role

### 1. Student Dashboard
- **Profile & Register Number**: Personal student identity, department, semester, and section.
- **Attendance Percentage**: Circular animated progress gauge with subject-wise breakdown. Defaulter alert badge if attendance drops below 75%.
- **Internal Assessment Marks**: Scores and grades (O, A+, A, B+) for Continuous Internal Assessments and Model Examinations.
- **Semester Results & CGPA**: Cumulative Grade Point Average (8.74) with historical semester GPA progression.
- **Class Timetable**: Interactive Day Selector (Monday to Friday) displaying lecture periods, room numbers, and faculty names.
- **Assignments & Tasks**: Filter by Pending/Submitted status with interactive solution submission dialog.
- **Leave & On-Duty Applications**: Apply for Medical Leave, OD, or Casual Leave with live multi-level approval tracking (Advisor & HOD).
- **Grievances & Complaints**: Lodge academic, hostel, or lab complaints with anonymous submission toggle and response tracking.
- **College Circulars**: Real-time announcement board with priority badges.

### 2. Class Advisor Dashboard
- **Class Student Roster**: Filter students in assigned section (CSE-3A), search by name/register number, and inspect individual performance.
- **Mark & Update Attendance**: Interactive roll-call toggle (Present, Absent, On-Duty) for all class students with batch submission.
- **Upload Internal Marks**: Upload scores for internal assessments, automatically calculate grades, and record them in the database.
- **Review Student Leaves**: Review, approve, or reject student leave requests with advisor remarks.
- **Defaulter Attendance Monitoring**: Automatic flagging of students with attendance < 75% with a one-tap warning alert trigger.
- **Class Circulars**: Broadcast announcements targeted exclusively to the assigned section.

### 3. Head of Department (HOD) Dashboard
- **Department Overview**: Department statistics including total enrolled students (480), faculty members (32), pass percentage (94.2%), and accreditation status.
- **Department Attendance Analytics**: Year-wise attendance comparison charts and subject pass rates.
- **Faculty Records**: Complete directory of department faculty and course allocations.
- **Escalated Approvals**: Final review and sign-off on escalated student OD leaves, medical leaves, and hall permissions.
- **Audit Report Generator**: Generate a formal audit report summary with student metrics and compliance certification.

### 4. College Admin Dashboard
- **User Account Provisioning**: Provision Student, Class Advisor, and HOD accounts with role-based credentials and class assignments.
- **Course Curriculum Management**: Add and manage courses, subject codes, faculty assignments, and credit allocations.
- **Department Oversight**: Directory of college engineering branches and department heads.
- **College-Wide Broadcast**: Publish official notices and circulars to the entire student body and faculty.
- **Institutional Settings**: Configure college name, active semester term, and minimum mandatory attendance threshold.

---

## 🔐 Architecture & Security

- **Row Level Security (RLS)**: Enforced via `SCHEMA.sql` for PostgreSQL/Supabase and role-guarded DAOs in the Android Room persistence layer.
- **Strict Role Separation**: Students cannot view or modify other students' marks or administrative dashboards. Class advisors are restricted to their assigned sections, and HODs to their departments.
- **Edge-to-Edge Material 3 UI**: Follows modern Android M3 guidelines with dynamic elevation, responsive layouts, accessible 48dp touch targets, and high-contrast academic colors.

---

## 🛠️ Tech Stack

- **Platform**: Android Jetpack Compose (Kotlin)
- **Local Persistence**: Android Room Database with reactive Kotlin Flows
- **Cloud Backend Schema**: PostgreSQL & Supabase DDL with Row Level Security (`SCHEMA.sql`)
- **Design System**: Material Design 3 (M3)
- **Build System**: Gradle Kotlin DSL with KSP
