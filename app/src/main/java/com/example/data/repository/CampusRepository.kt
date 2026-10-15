package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CampusRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val studentDao = database.studentDao()
    private val departmentDao = database.departmentDao()
    private val courseDao = database.courseDao()
    private val attendanceDao = database.attendanceDao()
    private val examMarkDao = database.examMarkDao()
    private val timetableDao = database.timetableDao()
    private val assignmentDao = database.assignmentDao()
    private val leaveDao = database.leaveDao()
    private val complaintDao = database.complaintDao()
    private val announcementDao = database.announcementDao()

    // --- Authentication & User Operations ---
    suspend fun authenticateUser(email: String, passwordEntered: String): UserEntity? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.trim().lowercase())
        if (user != null && user.passwordHash == passwordEntered.trim()) {
            user
        } else {
            null
        }
    }

    suspend fun getUserById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(userId) // or query
    }

    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    suspend fun createUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        userDao.deleteUser(userId)
    }

    // --- Students ---
    fun getStudentFlow(regNo: String): Flow<StudentEntity?> = studentDao.getStudentFlow(regNo)

    fun getStudentsBySectionFlow(section: String): Flow<List<StudentEntity>> = studentDao.getStudentsBySectionFlow(section)

    fun getStudentsByDeptFlow(deptId: String): Flow<List<StudentEntity>> = studentDao.getStudentsByDeptFlow(deptId)

    fun getAllStudentsFlow(): Flow<List<StudentEntity>> = studentDao.getAllStudentsFlow()

    suspend fun updateStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
    }

    suspend fun addStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }

    // --- Departments & Courses ---
    fun getAllDepartmentsFlow(): Flow<List<DepartmentEntity>> = departmentDao.getAllDepartmentsFlow()

    fun getAllCoursesFlow(): Flow<List<CourseEntity>> = courseDao.getAllCoursesFlow()

    fun getCoursesByDeptFlow(deptId: String): Flow<List<CourseEntity>> = courseDao.getCoursesByDeptFlow(deptId)

    suspend fun addCourse(course: CourseEntity) = withContext(Dispatchers.IO) {
        courseDao.insertCourse(course)
    }

    suspend fun addDepartment(department: DepartmentEntity) = withContext(Dispatchers.IO) {
        departmentDao.insertDepartment(department)
    }

    // --- Attendance ---
    fun getStudentAttendanceFlow(regNo: String): Flow<List<AttendanceEntity>> = attendanceDao.getStudentAttendanceFlow(regNo)

    fun getSectionAttendanceByDateFlow(section: String, date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getSectionAttendanceByDateFlow(section, date)

    suspend fun recordAttendance(record: AttendanceEntity) = withContext(Dispatchers.IO) {
        attendanceDao.insertAttendance(record)
    }

    suspend fun recordBatchAttendance(records: List<AttendanceEntity>, section: String) = withContext(Dispatchers.IO) {
        attendanceDao.insertAllAttendance(records)
    }

    // --- Exam Marks ---
    fun getStudentMarksFlow(regNo: String): Flow<List<ExamMarkEntity>> = examMarkDao.getStudentMarksFlow(regNo)

    fun getMarksBySubjectFlow(subjectCode: String): Flow<List<ExamMarkEntity>> = examMarkDao.getMarksBySubjectFlow(subjectCode)

    suspend fun insertExamMark(mark: ExamMarkEntity) = withContext(Dispatchers.IO) {
        examMarkDao.insertMark(mark)
    }

    // --- Timetable ---
    fun getTimetableForSectionFlow(section: String): Flow<List<TimetableSlotEntity>> = timetableDao.getTimetableForSectionFlow(section)

    suspend fun updateTimetableSlot(slot: TimetableSlotEntity) = withContext(Dispatchers.IO) {
        timetableDao.updateTimetableSlot(slot)
    }

    // --- Assignments ---
    fun getAssignmentsForSectionFlow(section: String): Flow<List<AssignmentEntity>> = assignmentDao.getAssignmentsForSectionFlow(section)

    fun getAllAssignmentsFlow(): Flow<List<AssignmentEntity>> = assignmentDao.getAllAssignmentsFlow()

    suspend fun createAssignment(assignment: AssignmentEntity) = withContext(Dispatchers.IO) {
        assignmentDao.insertAssignment(assignment)
    }

    fun getSubmissionsByStudentFlow(regNo: String): Flow<List<AssignmentSubmissionEntity>> =
        assignmentDao.getSubmissionsByStudentFlow(regNo)

    suspend fun submitAssignment(submission: AssignmentSubmissionEntity) = withContext(Dispatchers.IO) {
        assignmentDao.insertSubmission(submission)
    }

    // --- Leaves ---
    fun getLeavesForStudentFlow(regNo: String): Flow<List<LeaveApplicationEntity>> = leaveDao.getLeavesForStudentFlow(regNo)

    fun getLeavesForSectionFlow(section: String): Flow<List<LeaveApplicationEntity>> = leaveDao.getLeavesForSectionFlow(section)

    fun getAllLeavesFlow(): Flow<List<LeaveApplicationEntity>> = leaveDao.getAllLeavesFlow()

    suspend fun applyLeave(leave: LeaveApplicationEntity) = withContext(Dispatchers.IO) {
        leaveDao.insertLeave(leave)
    }

    suspend fun updateLeaveStatus(leave: LeaveApplicationEntity) = withContext(Dispatchers.IO) {
        leaveDao.updateLeave(leave)
    }

    // --- Complaints / Grievances ---
    fun getComplaintsByUserFlow(userId: String): Flow<List<ComplaintEntity>> = complaintDao.getComplaintsByUserFlow(userId)

    fun getAllComplaintsFlow(): Flow<List<ComplaintEntity>> = complaintDao.getAllComplaintsFlow()

    suspend fun submitComplaint(complaint: ComplaintEntity) = withContext(Dispatchers.IO) {
        complaintDao.insertComplaint(complaint)
    }

    suspend fun updateComplaint(complaint: ComplaintEntity) = withContext(Dispatchers.IO) {
        complaintDao.updateComplaint(complaint)
    }

    // --- Announcements ---
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncementsFlow()

    suspend fun postAnnouncement(announcement: AnnouncementEntity) = withContext(Dispatchers.IO) {
        announcementDao.insertAnnouncement(announcement)
    }

    // --- Initial Seed Data ---
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingAdmin = userDao.getUserByEmail("admin@campus.edu")
        if (existingAdmin != null) return@withContext

        // 1. Seed Users
        val users = listOf(
            UserEntity(
                id = "USR-STU-711025205043",
                email = "saranya@campus.edu",
                passwordHash = "student123",
                fullName = "Saranya",
                role = UserRole.STUDENT,
                departmentId = "DEPT-CSE",
                assignedClass = "CSE-3A",
                registerNumberOrEmpId = "711025205043",
                phone = "+1 (555) 711-2050",
                avatarInitials = "SM"
            ),
            UserEntity(
                id = "USR-STU-101",
                email = "student@campus.edu",
                passwordHash = "student123",
                fullName = "Saranya",
                role = UserRole.STUDENT,
                departmentId = "DEPT-CSE",
                assignedClass = "CSE-3A",
                registerNumberOrEmpId = "711025205043",
                phone = "+1 (555) 711-2050",
                avatarInitials = "SM"
            ),
            UserEntity(
                id = "USR-STU-GMAIL",
                email = "saranyamuniraj1409@gmail.com",
                passwordHash = "student123",
                fullName = "Saranya",
                role = UserRole.STUDENT,
                departmentId = "DEPT-CSE",
                assignedClass = "CSE-3A",
                registerNumberOrEmpId = "711025205043",
                phone = "+1 (555) 711-2050",
                avatarInitials = "SM"
            ),
            UserEntity(
                id = "USR-ADV-102",
                email = "advisor@campus.edu",
                passwordHash = "advisor123",
                fullName = "Dr. Priya Raman",
                role = UserRole.ADVISOR,
                departmentId = "DEPT-CSE",
                assignedClass = "CSE-3A",
                registerNumberOrEmpId = "EMP-CSE-102",
                phone = "+1 (555) 345-6789",
                avatarInitials = "PR"
            ),
            UserEntity(
                id = "USR-HOD-001",
                email = "hod@campus.edu",
                passwordHash = "hod123",
                fullName = "Dr. Rajesh Natarajan",
                role = UserRole.HOD,
                departmentId = "DEPT-CSE",
                assignedClass = "",
                registerNumberOrEmpId = "EMP-CSE-001",
                phone = "+1 (555) 456-7890",
                avatarInitials = "RN"
            ),
            UserEntity(
                id = "USR-ADM-001",
                email = "admin@campus.edu",
                passwordHash = "admin123",
                fullName = "Dr. K. S. Sundaram",
                role = UserRole.ADMIN,
                departmentId = "DEPT-ADM",
                assignedClass = "",
                registerNumberOrEmpId = "EMP-ADM-001",
                phone = "+1 (555) 567-8901",
                avatarInitials = "KS"
            )
        )
        userDao.insertUsers(users)

        // 2. Seed Departments
        val departments = listOf(
            DepartmentEntity("DEPT-CSE", "CSE", "Computer Science & Engineering", "Dr. Rajesh Natarajan", 480, 32, 2004),
            DepartmentEntity("DEPT-ECE", "ECE", "Electronics & Communication Eng.", "Dr. S. Meenakshi", 420, 28, 2005),
            DepartmentEntity("DEPT-MECH", "MECH", "Mechanical Engineering", "Dr. Arvind Swamy", 360, 24, 2002),
            DepartmentEntity("DEPT-IT", "IT", "Information Technology", "Dr. N. Deepa", 310, 20, 2008)
        )
        departmentDao.insertDepartments(departments)

        // 3. Seed Students
        val students = listOf(
            StudentEntity(
                registerNumber = "711025205043",
                fullName = "Saranya",
                email = "saranya@campus.edu",
                departmentId = "DEPT-CSE",
                year = 3,
                semester = 6,
                section = "CSE-3A",
                cgpa = 9.12,
                overallAttendance = 91.5,
                advisorId = "EMP-CSE-102",
                phone = "+1 (555) 711-2050",
                address = "Campus Block 3, University City"
            ),
            StudentEntity(
                registerNumber = "717622CS102",
                fullName = "Bhavana Reddy",
                email = "bhavana.r@campus.edu",
                departmentId = "DEPT-CSE",
                year = 3,
                semester = 6,
                section = "CSE-3A",
                cgpa = 9.15,
                overallAttendance = 92.0,
                advisorId = "EMP-CSE-102",
                phone = "+1 (555) 888-1122",
                address = "Apt 2B, Greenfield Residency"
            ),
            StudentEntity(
                registerNumber = "717622CS103",
                fullName = "Chidambaram M",
                email = "chidam.m@campus.edu",
                departmentId = "DEPT-CSE",
                year = 3,
                semester = 6,
                section = "CSE-3A",
                cgpa = 7.30,
                overallAttendance = 71.4, // Alert: Low Attendance (<75%)
                advisorId = "EMP-CSE-102",
                phone = "+1 (555) 777-3344",
                address = "12 Lakeview Avenue, City West"
            ),
            StudentEntity(
                registerNumber = "717622CS104",
                fullName = "Divya Sri",
                email = "divya.sri@campus.edu",
                departmentId = "DEPT-CSE",
                year = 3,
                semester = 6,
                section = "CSE-3A",
                cgpa = 8.50,
                overallAttendance = 88.2,
                advisorId = "EMP-CSE-102",
                phone = "+1 (555) 666-4455",
                address = "Campus Hostel Block 2, Room 304"
            ),
            StudentEntity(
                registerNumber = "717622CS105",
                fullName = "Ezhil Arasan",
                email = "ezhil.a@campus.edu",
                departmentId = "DEPT-CSE",
                year = 3,
                semester = 6,
                section = "CSE-3A",
                cgpa = 6.85,
                overallAttendance = 68.0, // Alert: Low Attendance (<75%)
                advisorId = "EMP-CSE-102",
                phone = "+1 (555) 555-6677",
                address = "45 Temple Road, East Gate"
            ),
            StudentEntity(
                registerNumber = "717622CS106",
                fullName = "Farhan Akhtar",
                email = "farhan.a@campus.edu",
                departmentId = "DEPT-CSE",
                year = 3,
                semester = 6,
                section = "CSE-3A",
                cgpa = 9.40,
                overallAttendance = 94.5,
                advisorId = "EMP-CSE-102",
                phone = "+1 (555) 444-7788",
                address = "Oak Ridge Towers, Unit 5C"
            )
        )
        studentDao.insertStudents(students)

        // 4. Seed Courses
        val courses = listOf(
            CourseEntity("CS8601", "Mobile Application Development", "DEPT-CSE", 6, 4, "Dr. Priya Raman", "Core"),
            CourseEntity("CS8602", "Artificial Intelligence & Machine Learning", "DEPT-CSE", 6, 4, "Dr. Rajesh Natarajan", "Core"),
            CourseEntity("CS8603", "Distributed Cloud Computing", "DEPT-CSE", 6, 3, "Prof. Ananya Roy", "Core"),
            CourseEntity("CS8604", "Cryptography and Network Security", "DEPT-CSE", 6, 3, "Prof. Karthik V", "Core"),
            CourseEntity("CS8651", "Internet of Things Laboratory", "DEPT-CSE", 6, 2, "Prof. Suresh B", "Laboratory"),
            CourseEntity("HS8581", "Professional Communication & Ethics", "DEPT-CSE", 6, 2, "Dr. Malathi S", "Humanities")
        )
        courseDao.insertCourses(courses)

        // 5. Seed Exam Marks for Saranya
        val examMarks = listOf(
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8601", examType = "Internal Assessment 1", marksScored = 48.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8601", examType = "Internal Assessment 2", marksScored = 49.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8602", examType = "Internal Assessment 1", marksScored = 46.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8602", examType = "Internal Assessment 2", marksScored = 47.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8603", examType = "Internal Assessment 1", marksScored = 47.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8604", examType = "Internal Assessment 1", marksScored = 44.0, maxMarks = 50.0, grade = "A+"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8651", examType = "Internal Assessment 1", marksScored = 50.0, maxMarks = 50.0, grade = "O")
        )
        examMarkDao.insertMarks(examMarks)

        // 6. Seed Timetable for CSE-3A
        val timetable = listOf(
            // Monday
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Monday", period = 1, timeRange = "09:00 - 09:50 AM", subjectCode = "CS8601", subjectTitle = "Mobile App Development", facultyName = "Dr. Priya Raman", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Monday", period = 2, timeRange = "09:55 - 10:45 AM", subjectCode = "CS8602", subjectTitle = "AI & Machine Learning", facultyName = "Dr. Rajesh Natarajan", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Monday", period = 3, timeRange = "11:00 - 11:50 AM", subjectCode = "CS8603", subjectTitle = "Cloud Computing", facultyName = "Prof. Ananya Roy", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Monday", period = 4, timeRange = "11:55 - 12:45 PM", subjectCode = "CS8604", subjectTitle = "Cryptography & Security", facultyName = "Prof. Karthik V", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Monday", period = 5, timeRange = "01:30 - 03:10 PM", subjectCode = "CS8651", subjectTitle = "IoT Systems Lab", facultyName = "Prof. Suresh B", roomNo = "Lab-4"),

            // Tuesday
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Tuesday", period = 1, timeRange = "09:00 - 09:50 AM", subjectCode = "CS8603", subjectTitle = "Cloud Computing", facultyName = "Prof. Ananya Roy", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Tuesday", period = 2, timeRange = "09:55 - 10:45 AM", subjectCode = "CS8601", subjectTitle = "Mobile App Development", facultyName = "Dr. Priya Raman", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Tuesday", period = 3, timeRange = "11:00 - 11:50 AM", subjectCode = "CS8602", subjectTitle = "AI & Machine Learning", facultyName = "Dr. Rajesh Natarajan", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Tuesday", period = 4, timeRange = "11:55 - 12:45 PM", subjectCode = "HS8581", subjectTitle = "Professional Ethics", facultyName = "Dr. Malathi S", roomNo = "LH-301"),

            // Wednesday
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Wednesday", period = 1, timeRange = "09:00 - 09:50 AM", subjectCode = "CS8604", subjectTitle = "Cryptography & Security", facultyName = "Prof. Karthik V", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Wednesday", period = 2, timeRange = "09:55 - 10:45 AM", subjectCode = "CS8603", subjectTitle = "Cloud Computing", facultyName = "Prof. Ananya Roy", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Wednesday", period = 3, timeRange = "11:00 - 11:50 AM", subjectCode = "CS8601", subjectTitle = "Mobile App Development", facultyName = "Dr. Priya Raman", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Wednesday", period = 4, timeRange = "01:30 - 03:10 PM", subjectCode = "CS8602", subjectTitle = "AI Lab Sessions", facultyName = "Dr. Rajesh Natarajan", roomNo = "Lab-2"),

            // Thursday
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Thursday", period = 1, timeRange = "09:00 - 09:50 AM", subjectCode = "CS8602", subjectTitle = "AI & Machine Learning", facultyName = "Dr. Rajesh Natarajan", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Thursday", period = 2, timeRange = "09:55 - 10:45 AM", subjectCode = "CS8604", subjectTitle = "Cryptography & Security", facultyName = "Prof. Karthik V", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Thursday", period = 3, timeRange = "11:00 - 11:50 AM", subjectCode = "HS8581", subjectTitle = "Communication Lab", facultyName = "Dr. Malathi S", roomNo = "Lang-Lab"),

            // Friday
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Friday", period = 1, timeRange = "09:00 - 09:50 AM", subjectCode = "CS8601", subjectTitle = "Mobile App Development", facultyName = "Dr. Priya Raman", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Friday", period = 2, timeRange = "09:55 - 10:45 AM", subjectCode = "CS8603", subjectTitle = "Cloud Architecture", facultyName = "Prof. Ananya Roy", roomNo = "LH-301"),
            TimetableSlotEntity(departmentId = "DEPT-CSE", section = "CSE-3A", dayOfWeek = "Friday", period = 3, timeRange = "11:00 - 11:50 AM", subjectCode = "CS8602", subjectTitle = "Machine Learning Project", facultyName = "Dr. Rajesh Natarajan", roomNo = "LH-301")
        )
        timetableDao.insertTimetableSlots(timetable)

        // 7. Seed Assignments
        val assignments = listOf(
            AssignmentEntity(
                subjectCode = "CS8601",
                subjectTitle = "Mobile Application Development",
                title = "Compose Navigation & Room Implementation",
                description = "Develop an MVVM Jetpack Compose application featuring Room persistence and responsive M3 design.",
                dueDate = "2026-10-20",
                targetSection = "CSE-3A",
                maxPoints = 100
            ),
            AssignmentEntity(
                subjectCode = "CS8602",
                subjectTitle = "AI & Machine Learning",
                title = "Heuristic Search & Minimax Lab Assignment",
                description = "Implement A* search algorithm and Alpha-Beta pruning for adversarial board games.",
                dueDate = "2026-10-24",
                targetSection = "CSE-3A",
                maxPoints = 50
            ),
            AssignmentEntity(
                subjectCode = "CS8603",
                subjectTitle = "Distributed Cloud Computing",
                title = "Kubernetes Cluster Configuration Analysis",
                description = "Configure multi-node microservices deployment with ingress routing and horizontal pod autoscaler.",
                dueDate = "2026-10-28",
                targetSection = "CSE-3A",
                maxPoints = 100
            )
        )
        assignmentDao.insertAssignments(assignments)

        // 8. Seed Leave Applications
        val leaves = listOf(
            LeaveApplicationEntity(
                studentRegisterNumber = "711025205043",
                studentName = "Saranya",
                department = "CSE",
                section = "CSE-3A",
                leaveType = "On-Duty (OD)",
                startDate = "2026-10-18",
                endDate = "2026-10-19",
                reason = "Presenting research paper on Mobile Edge Computing at IEEE Conference.",
                advisorStatus = "APPROVED",
                hodStatus = "APPROVED",
                overallStatus = "APPROVED",
                remarks = "All the best! Keep faculty and department updated.",
                appliedAt = "2026-10-12"
            ),
            LeaveApplicationEntity(
                studentRegisterNumber = "717622CS103",
                studentName = "Chidambaram M",
                department = "CSE",
                section = "CSE-3A",
                leaveType = "Medical Leave",
                startDate = "2026-10-16",
                endDate = "2026-10-17",
                reason = "Severe viral fever and physician recommended rest.",
                advisorStatus = "PENDING",
                hodStatus = "PENDING",
                overallStatus = "PENDING",
                remarks = "",
                appliedAt = "2026-10-14"
            )
        )
        leaves.forEach { leaveDao.insertLeave(it) }

        // 9. Seed Grievances
        val complaints = listOf(
            ComplaintEntity(
                userId = "711025205043",
                studentName = "Saranya",
                category = "Infrastructure & Lab",
                title = "Lab 4 Workstation GPU Driver Update Needed",
                description = "Workstations 12 to 18 in IoT Lab need CUDA toolkit update for machine learning coursework.",
                isAnonymous = false,
                status = "IN_REVIEW",
                officialResponse = "Lab technician scheduled for driver update this Saturday.",
                submittedAt = "2026-10-10"
            ),
            ComplaintEntity(
                userId = "717622CS104",
                studentName = "Anonymous Student",
                category = "Hostel & Mess",
                title = "Hostel Wi-Fi Signal Weak on 3rd Floor",
                description = "Bandwidth drops below 1 Mbps during evening study hours (8 PM - 11 PM).",
                isAnonymous = true,
                status = "OPEN",
                officialResponse = "",
                submittedAt = "2026-10-13"
            )
        )
        complaints.forEach { complaintDao.insertComplaint(it) }

        // 10. Seed Announcements
        val announcements = listOf(
            AnnouncementEntity(
                title = "National Level Technical Symposium 'InnoTech 2026' Registrations Open",
                content = "Department of CSE invites all students to participate in 24-hour Hackathon, Coding Relay, and Paper Presentations. Cash prizes worth $5,000.",
                senderRole = "ADMIN",
                senderName = "Office of Academic Affairs",
                targetAudience = "ALL",
                priority = "HIGH",
                publishedAt = "2026-10-14"
            ),
            AnnouncementEntity(
                title = "Internal Assessment 2 Schedule Announced",
                content = "Internal Assessment 2 examinations will commence from November 2nd. The detailed time table has been uploaded to student portal.",
                senderRole = "HOD",
                senderName = "Dr. Rajesh Natarajan (HOD CSE)",
                targetAudience = "CSE_DEPT",
                priority = "URGENT",
                publishedAt = "2026-10-13"
            ),
            AnnouncementEntity(
                title = "Campus Placement Drive: Google & TCS Campus Visit",
                content = "Pre-placement talk for eligible 3rd and 4th year engineering students scheduled this Friday in University Auditorium at 10:00 AM.",
                senderRole = "ADMIN",
                senderName = "Placement & Training Cell",
                targetAudience = "ALL",
                priority = "HIGH",
                publishedAt = "2026-10-11"
            ),
            AnnouncementEntity(
                title = "Class CSE-3A: Extra Lab Hours for Mobile App Projects",
                content = "Special lab access will be open on Thursday 3:30 PM to 5:30 PM in Lab-4 for project reviews.",
                senderRole = "ADVISOR",
                senderName = "Dr. Priya Raman (Class Advisor)",
                targetAudience = "CSE_3A",
                priority = "NORMAL",
                publishedAt = "2026-10-10"
            )
        )
        announcementDao.insertAnnouncements(announcements)
    }

    suspend fun ensureSaranyaStudentExists() = withContext(Dispatchers.IO) {
        val saranyaUser = UserEntity(
            id = "USR-STU-711025205043",
            email = "saranya@campus.edu",
            passwordHash = "student123",
            fullName = "Saranya",
            role = UserRole.STUDENT,
            departmentId = "DEPT-CSE",
            assignedClass = "CSE-3A",
            registerNumberOrEmpId = "711025205043",
            phone = "+1 (555) 711-2050",
            avatarInitials = "SM"
        )
        userDao.insertUser(saranyaUser)

        // Also alias student@campus.edu and saranyamuniraj1409@gmail.com
        userDao.insertUser(saranyaUser.copy(id = "USR-STU-101", email = "student@campus.edu"))
        userDao.insertUser(saranyaUser.copy(id = "USR-STU-GMAIL", email = "saranyamuniraj1409@gmail.com"))

        val student = StudentEntity(
            registerNumber = "711025205043",
            fullName = "Saranya",
            email = "saranya@campus.edu",
            departmentId = "DEPT-CSE",
            year = 3,
            semester = 6,
            section = "CSE-3A",
            cgpa = 9.12,
            overallAttendance = 91.5,
            advisorId = "EMP-CSE-102",
            phone = "+1 (555) 711-2050",
            address = "Campus Block 3, University City"
        )
        studentDao.insertStudent(student)

        // Ensure marks exist for 711025205043
        val marks = listOf(
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8601", examType = "Internal Assessment 1", marksScored = 48.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8601", examType = "Internal Assessment 2", marksScored = 49.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8602", examType = "Internal Assessment 1", marksScored = 46.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8602", examType = "Internal Assessment 2", marksScored = 47.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8603", examType = "Internal Assessment 1", marksScored = 47.0, maxMarks = 50.0, grade = "O"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8604", examType = "Internal Assessment 1", marksScored = 44.0, maxMarks = 50.0, grade = "A+"),
            ExamMarkEntity(studentRegisterNumber = "711025205043", subjectCode = "CS8651", examType = "Internal Assessment 1", marksScored = 50.0, maxMarks = 50.0, grade = "O")
        )
        examMarkDao.insertMarks(marks)

        // Ensure assignment submission exists
        val sub = AssignmentSubmissionEntity(
            assignmentId = 1,
            studentRegisterNumber = "711025205043",
            submissionText = "https://github.com/saranya/campusconnect-mobile-app-solution.git",
            submittedAt = "2026-10-14 04:30 PM",
            status = "GRADED",
            marksAwarded = 98,
            feedback = "Outstanding UI architecture and Room database integration!"
        )
        assignmentDao.insertSubmission(sub)
    }
}
