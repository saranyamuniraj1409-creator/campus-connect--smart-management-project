package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: UserRole,
    val departmentId: String,
    val assignedClass: String, // e.g. "CSE-3A" for advisor or student, empty for Admin
    val registerNumberOrEmpId: String,
    val phone: String,
    val avatarInitials: String
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val registerNumber: String,
    val fullName: String,
    val email: String,
    val departmentId: String,
    val year: Int,
    val semester: Int,
    val section: String,
    val cgpa: Double,
    val overallAttendance: Double,
    val advisorId: String,
    val phone: String,
    val address: String
)

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val id: String, // e.g. "DEPT-CSE"
    val code: String, // "CSE"
    val name: String, // "Computer Science and Engineering"
    val hodName: String,
    val totalStudents: Int,
    val totalFaculty: Int,
    val establishedYear: Int
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val code: String, // e.g. "CS8601"
    val title: String,
    val departmentId: String,
    val semester: Int,
    val credits: Int,
    val facultyName: String,
    val category: String // "Core", "Elective", "Laboratory"
)

@Entity(tableName = "attendance_records")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentRegisterNumber: String,
    val subjectCode: String,
    val date: String, // YYYY-MM-DD
    val status: String, // "PRESENT", "ABSENT", "OD"
    val section: String // "CSE-3A"
)

@Entity(tableName = "exam_marks")
data class ExamMarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentRegisterNumber: String,
    val subjectCode: String,
    val examType: String, // "Internal Assessment 1", "Internal Assessment 2", "Model Examination", "Assignment"
    val marksScored: Double,
    val maxMarks: Double = 100.0,
    val grade: String
)

@Entity(tableName = "timetable_slots")
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val departmentId: String,
    val section: String,
    val dayOfWeek: String, // "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
    val period: Int, // 1 to 7
    val timeRange: String,
    val subjectCode: String,
    val subjectTitle: String,
    val facultyName: String,
    val roomNo: String
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectCode: String,
    val subjectTitle: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val targetSection: String,
    val maxPoints: Int = 100
)

@Entity(tableName = "assignment_submissions")
data class AssignmentSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assignmentId: Long,
    val studentRegisterNumber: String,
    val submissionText: String,
    val submittedAt: String,
    val status: String, // "SUBMITTED", "EVALUATED"
    val marksAwarded: Int? = null,
    val feedback: String? = null
)

@Entity(tableName = "leave_applications")
data class LeaveApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentRegisterNumber: String,
    val studentName: String,
    val department: String,
    val section: String,
    val leaveType: String, // "Medical Leave", "On-Duty (OD)", "Casual Leave"
    val startDate: String,
    val endDate: String,
    val reason: String,
    val advisorStatus: String, // "PENDING", "APPROVED", "REJECTED"
    val hodStatus: String, // "PENDING", "APPROVED", "REJECTED"
    val overallStatus: String, // "PENDING", "APPROVED", "REJECTED"
    val remarks: String = "",
    val appliedAt: String
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val studentName: String,
    val category: String, // "Academic", "Hostel & Mess", "Infrastructure & Lab", "Discipline & Anti-Ragging"
    val title: String,
    val description: String,
    val isAnonymous: Boolean,
    val status: String, // "OPEN", "IN_REVIEW", "RESOLVED"
    val officialResponse: String = "",
    val submittedAt: String
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val senderRole: String, // "ADMIN", "HOD", "ADVISOR"
    val senderName: String,
    val targetAudience: String, // "ALL", "CSE_DEPT", "CSE_3A"
    val priority: String, // "NORMAL", "HIGH", "URGENT"
    val publishedAt: String
)
