package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.UserRole
import com.example.data.repository.CampusRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CampusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CampusRepository

    // Current Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Database reactive streams
    val users: StateFlow<List<UserEntity>>
    val allStudents: StateFlow<List<StudentEntity>>
    val departments: StateFlow<List<DepartmentEntity>>
    val courses: StateFlow<List<CourseEntity>>
    val timetableSlots: StateFlow<List<TimetableSlotEntity>>
    val assignments: StateFlow<List<AssignmentEntity>>
    val allLeaves: StateFlow<List<LeaveApplicationEntity>>
    val allComplaints: StateFlow<List<ComplaintEntity>>
    val announcements: StateFlow<List<AnnouncementEntity>>

    // Dynamic filtered states based on logged in user
    val currentStudentProfile: StateFlow<StudentEntity?>
    val studentExamMarks: StateFlow<List<ExamMarkEntity>>
    val studentSubmissions: StateFlow<List<AssignmentSubmissionEntity>>
    val studentLeaves: StateFlow<List<LeaveApplicationEntity>>
    val studentComplaints: StateFlow<List<ComplaintEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CampusRepository(database)

        // Seed data asynchronously
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            repository.ensureSaranyaStudentExists()
            _isLoading.value = false
            // Default logged in as Student for immediate preview
            loginWithRole(UserRole.STUDENT)
        }

        users = repository.getAllUsersFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allStudents = repository.getAllStudentsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        departments = repository.getAllDepartmentsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        courses = repository.getAllCoursesFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        timetableSlots = repository.getTimetableForSectionFlow("CSE-3A").stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        assignments = repository.getAllAssignmentsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allLeaves = repository.getAllLeavesFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allComplaints = repository.getAllComplaintsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        announcements = repository.getAllAnnouncementsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        currentStudentProfile = _currentUser.flatMapLatest { user ->
            if (user != null && user.role == UserRole.STUDENT) {
                repository.getStudentFlow(user.registerNumberOrEmpId)
            } else {
                flowOf(null)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

        studentExamMarks = _currentUser.flatMapLatest { user ->
            if (user != null && user.role == UserRole.STUDENT) {
                repository.getStudentMarksFlow(user.registerNumberOrEmpId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        studentSubmissions = _currentUser.flatMapLatest { user ->
            if (user != null && user.role == UserRole.STUDENT) {
                repository.getSubmissionsByStudentFlow(user.registerNumberOrEmpId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        studentLeaves = _currentUser.flatMapLatest { user ->
            if (user != null && user.role == UserRole.STUDENT) {
                repository.getLeavesForStudentFlow(user.registerNumberOrEmpId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        studentComplaints = _currentUser.flatMapLatest { user ->
            if (user != null) {
                repository.getComplaintsByUserFlow(user.registerNumberOrEmpId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun login(email: String, passwordEntered: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = repository.authenticateUser(email, passwordEntered)
            if (user != null) {
                _currentUser.value = user
                _loginError.value = null
            } else {
                _loginError.value = "Invalid email or password. Please verify credentials."
            }
            _isLoading.value = false
        }
    }

    fun loginWithRole(role: UserRole) {
        viewModelScope.launch {
            val targetEmail = when (role) {
                UserRole.STUDENT -> "saranya@campus.edu"
                UserRole.ADVISOR -> "advisor@campus.edu"
                UserRole.HOD -> "hod@campus.edu"
                UserRole.ADMIN -> "admin@campus.edu"
            }
            var user = repository.getUserById(targetEmail)
            if (user == null && role == UserRole.STUDENT) {
                user = repository.getUserById("student@campus.edu")
            }
            if (user != null) {
                _currentUser.value = user
                _loginError.value = null
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
    }

    // Actions
    fun applyLeave(leave: LeaveApplicationEntity) {
        viewModelScope.launch {
            repository.applyLeave(leave)
        }
    }

    fun updateLeaveStatus(leave: LeaveApplicationEntity, isApproved: Boolean, remarks: String) {
        viewModelScope.launch {
            val status = if (isApproved) "APPROVED" else "REJECTED"
            val updated = leave.copy(
                advisorStatus = status,
                overallStatus = if (isApproved && leave.hodStatus == "APPROVED") "APPROVED" else if (!isApproved) "REJECTED" else "PENDING",
                remarks = remarks
            )
            repository.updateLeaveStatus(updated)
        }
    }

    fun updateHodLeaveStatus(leave: LeaveApplicationEntity, isApproved: Boolean) {
        viewModelScope.launch {
            val status = if (isApproved) "APPROVED" else "REJECTED"
            val updated = leave.copy(
                hodStatus = status,
                overallStatus = if (isApproved) "APPROVED" else "REJECTED"
            )
            repository.updateLeaveStatus(updated)
        }
    }

    fun submitComplaint(complaint: ComplaintEntity) {
        viewModelScope.launch {
            repository.submitComplaint(complaint)
        }
    }

    fun submitAssignment(submission: AssignmentSubmissionEntity) {
        viewModelScope.launch {
            repository.submitAssignment(submission)
        }
    }

    fun saveAttendanceBatch(records: List<AttendanceEntity>) {
        viewModelScope.launch {
            repository.recordBatchAttendance(records, "CSE-3A")
        }
    }

    fun saveExamMark(mark: ExamMarkEntity) {
        viewModelScope.launch {
            repository.insertExamMark(mark)
        }
    }

    fun createUser(user: UserEntity) {
        viewModelScope.launch {
            repository.createUser(user)
            if (user.role == UserRole.STUDENT) {
                repository.addStudent(
                    StudentEntity(
                        registerNumber = user.registerNumberOrEmpId,
                        fullName = user.fullName,
                        email = user.email,
                        departmentId = user.departmentId,
                        year = 3,
                        semester = 6,
                        section = user.assignedClass.ifBlank { "CSE-3A" },
                        cgpa = 8.0,
                        overallAttendance = 85.0,
                        advisorId = "EMP-CSE-102",
                        phone = user.phone,
                        address = "Campus Residence"
                    )
                )
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun addCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.addCourse(course)
        }
    }

    fun postAnnouncement(announcement: AnnouncementEntity) {
        viewModelScope.launch {
            repository.postAnnouncement(announcement)
        }
    }
}
