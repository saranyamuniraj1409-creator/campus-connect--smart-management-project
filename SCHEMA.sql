-- ==============================================================================
-- CampusConnect: Smart College Management System - Database Schema (PostgreSQL / Supabase)
-- ==============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. ROLES ENUM
CREATE TYPE user_role AS ENUM ('STUDENT', 'ADVISOR', 'HOD', 'ADMIN');
CREATE TYPE leave_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE attendance_status AS ENUM ('PRESENT', 'ABSENT', 'OD');
CREATE TYPE complaint_status AS ENUM ('OPEN', 'IN_REVIEW', 'RESOLVED');

-- 2. DEPARTMENTS
CREATE TABLE departments (
    id VARCHAR(50) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    hod_name VARCHAR(255),
    total_students INT DEFAULT 0,
    total_faculty INT DEFAULT 0,
    established_year INT DEFAULT 2004,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. USERS (Profiles linked to auth.users)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'STUDENT',
    department_id VARCHAR(50) REFERENCES departments(id),
    assigned_class VARCHAR(50),
    register_number_or_emp_id VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(50),
    avatar_initials VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. STUDENTS
CREATE TABLE students (
    register_number VARCHAR(50) PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    department_id VARCHAR(50) REFERENCES departments(id),
    year INT NOT NULL,
    semester INT NOT NULL,
    section VARCHAR(10) NOT NULL,
    cgpa NUMERIC(4, 2) DEFAULT 0.00,
    overall_attendance NUMERIC(5, 2) DEFAULT 100.00,
    advisor_id VARCHAR(50),
    phone VARCHAR(50),
    address TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. COURSES
CREATE TABLE courses (
    code VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    department_id VARCHAR(50) REFERENCES departments(id),
    semester INT NOT NULL,
    credits INT NOT NULL DEFAULT 3,
    faculty_name VARCHAR(255),
    category VARCHAR(50) DEFAULT 'Core'
);

-- 6. ATTENDANCE RECORDS
CREATE TABLE attendance_records (
    id BIGSERIAL PRIMARY KEY,
    student_register_number VARCHAR(50) REFERENCES students(register_number),
    subject_code VARCHAR(50) REFERENCES courses(code),
    date DATE NOT NULL,
    status attendance_status NOT NULL DEFAULT 'PRESENT',
    section VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 7. EXAM MARKS
CREATE TABLE exam_marks (
    id BIGSERIAL PRIMARY KEY,
    student_register_number VARCHAR(50) REFERENCES students(register_number),
    subject_code VARCHAR(50) REFERENCES courses(code),
    exam_type VARCHAR(100) NOT NULL,
    marks_scored NUMERIC(5, 2) NOT NULL,
    max_marks NUMERIC(5, 2) NOT NULL DEFAULT 100.0,
    grade VARCHAR(10) NOT NULL
);

-- 8. TIMETABLE SLOTS
CREATE TABLE timetable_slots (
    id BIGSERIAL PRIMARY KEY,
    department_id VARCHAR(50) REFERENCES departments(id),
    section VARCHAR(50) NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    period INT NOT NULL,
    time_range VARCHAR(50) NOT NULL,
    subject_code VARCHAR(50) REFERENCES courses(code),
    subject_title VARCHAR(255) NOT NULL,
    faculty_name VARCHAR(255) NOT NULL,
    room_no VARCHAR(50) NOT NULL
);

-- 9. ASSIGNMENTS
CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    subject_code VARCHAR(50) REFERENCES courses(code),
    subject_title VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE NOT NULL,
    target_section VARCHAR(50) NOT NULL,
    max_points INT DEFAULT 100
);

-- 10. ASSIGNMENT SUBMISSIONS
CREATE TABLE assignment_submissions (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT REFERENCES assignments(id) ON DELETE CASCADE,
    student_register_number VARCHAR(50) REFERENCES students(register_number),
    submission_text TEXT NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status VARCHAR(50) DEFAULT 'SUBMITTED',
    marks_awarded INT,
    feedback TEXT
);

-- 11. LEAVE APPLICATIONS
CREATE TABLE leave_applications (
    id BIGSERIAL PRIMARY KEY,
    student_register_number VARCHAR(50) REFERENCES students(register_number),
    student_name VARCHAR(255) NOT NULL,
    department VARCHAR(50) NOT NULL,
    section VARCHAR(50) NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT NOT NULL,
    advisor_status leave_status DEFAULT 'PENDING',
    hod_status leave_status DEFAULT 'PENDING',
    overall_status leave_status DEFAULT 'PENDING',
    remarks TEXT DEFAULT '',
    applied_at DATE DEFAULT CURRENT_DATE
);

-- 12. COMPLAINTS & GRIEVANCES
CREATE TABLE complaints (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    is_anonymous BOOLEAN DEFAULT FALSE,
    status complaint_status DEFAULT 'OPEN',
    official_response TEXT DEFAULT '',
    submitted_at DATE DEFAULT CURRENT_DATE
);

-- 13. ANNOUNCEMENTS
CREATE TABLE announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sender_role user_role NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    target_audience VARCHAR(50) NOT NULL DEFAULT 'ALL',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    published_at DATE DEFAULT CURRENT_DATE
);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================

ALTER TABLE students ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE exam_marks ENABLE ROW LEVEL SECURITY;
ALTER TABLE leave_applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE complaints ENABLE ROW LEVEL SECURITY;

-- 1. Student Access: Only view own profile, marks, and attendance
CREATE POLICY "Students can only access their own records"
    ON students
    FOR SELECT
    USING (auth.uid() = user_id OR auth.jwt() ->> 'role' IN ('ADVISOR', 'HOD', 'ADMIN'));

CREATE POLICY "Students can only view their own attendance"
    ON attendance_records
    FOR SELECT
    USING (
        student_register_number IN (
            SELECT register_number FROM students WHERE user_id = auth.uid()
        )
        OR auth.jwt() ->> 'role' IN ('ADVISOR', 'HOD', 'ADMIN')
    );

CREATE POLICY "Students can only view their own marks"
    ON exam_marks
    FOR SELECT
    USING (
        student_register_number IN (
            SELECT register_number FROM students WHERE user_id = auth.uid()
        )
        OR auth.jwt() ->> 'role' IN ('ADVISOR', 'HOD', 'ADMIN')
    );

-- 2. Advisor Access: Advisors can manage their assigned class
CREATE POLICY "Advisors manage assigned class attendance"
    ON attendance_records
    FOR ALL
    USING (
        section IN (
            SELECT assigned_class FROM users WHERE id = auth.uid() AND role = 'ADVISOR'
        )
        OR auth.jwt() ->> 'role' IN ('HOD', 'ADMIN')
    );

-- 3. HOD Access: HODs manage their department records
CREATE POLICY "HODs manage department students"
    ON students
    FOR ALL
    USING (
        department_id IN (
            SELECT department_id FROM users WHERE id = auth.uid() AND role = 'HOD'
        )
        OR auth.jwt() ->> 'role' = 'ADMIN'
    );

-- 4. Admin Access: Full unrestricted access
CREATE POLICY "Admin full access"
    ON students
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'ADMIN');
