package com.example.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun StudentDashboardScreen(
    currentUser: UserEntity,
    studentProfile: StudentEntity?,
    courses: List<CourseEntity>,
    examMarks: List<ExamMarkEntity>,
    timetableSlots: List<TimetableSlotEntity>,
    assignments: List<AssignmentEntity>,
    submissions: List<AssignmentSubmissionEntity>,
    leaves: List<LeaveApplicationEntity>,
    complaints: List<ComplaintEntity>,
    announcements: List<AnnouncementEntity>,
    onApplyLeave: (LeaveApplicationEntity) -> Unit,
    onSubmitComplaint: (ComplaintEntity) -> Unit,
    onSubmitAssignment: (AssignmentSubmissionEntity) -> Unit,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showApplyLeaveDialog by remember { mutableStateOf(false) }
    var showSubmitComplaintDialog by remember { mutableStateOf(false) }
    var selectedAssignmentForSubmission by remember { mutableStateOf<AssignmentEntity?>(null) }

    val navTabs = listOf(
        Triple("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        Triple("Attendance", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
        Triple("Academics", Icons.Filled.School, Icons.Outlined.School),
        Triple("Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        Triple("Tasks", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        Triple("Services", Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CampusTopAppBar(
                title = "Student Portal",
                subtitle = "${studentProfile?.fullName ?: currentUser.fullName} (${studentProfile?.registerNumber ?: currentUser.registerNumberOrEmpId})",
                currentRole = UserRole.STUDENT,
                userName = currentUser.fullName,
                onSwitchRoleClick = onSwitchRole,
                onLogoutClick = onLogout
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                navTabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.second else item.third,
                                contentDescription = item.first
                            )
                        },
                        label = {
                            Text(
                                text = item.first,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy700,
                            selectedTextColor = Navy700,
                            indicatorColor = Navy100
                        ),
                        modifier = Modifier.testTag("nav_student_tab_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate50)
        ) {
            when (selectedTab) {
                0 -> StudentOverviewTab(
                    student = studentProfile,
                    announcements = announcements,
                    assignments = assignments,
                    submissions = submissions,
                    onNavigateToTab = { selectedTab = it },
                    onApplyLeaveClick = { showApplyLeaveDialog = true }
                )
                1 -> StudentAttendanceTab(
                    student = studentProfile,
                    courses = courses
                )
                2 -> StudentAcademicsTab(
                    student = studentProfile,
                    marks = examMarks,
                    courses = courses
                )
                3 -> StudentTimetableTab(
                    slots = timetableSlots
                )
                4 -> StudentAssignmentsTab(
                    assignments = assignments,
                    submissions = submissions,
                    onSubmitClick = { selectedAssignmentForSubmission = it }
                )
                5 -> StudentServicesTab(
                    leaves = leaves,
                    complaints = complaints,
                    onApplyLeaveClick = { showApplyLeaveDialog = true },
                    onNewComplaintClick = { showSubmitComplaintDialog = true }
                )
            }
        }
    }

    // Apply Leave Dialog
    if (showApplyLeaveDialog && studentProfile != null) {
        var leaveType by remember { mutableStateOf("Medical Leave") }
        var startDate by remember { mutableStateOf("2026-10-18") }
        var endDate by remember { mutableStateOf("2026-10-19") }
        var reason by remember { mutableStateOf("") }
        var formError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showApplyLeaveDialog = false },
            title = {
                Text(
                    text = "Apply for Student Leave",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Leave Category", style = MaterialTheme.typography.labelSmall, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Medical Leave", "On-Duty (OD)", "Casual Leave").forEach { type ->
                            FilterChip(
                                selected = leaveType == type,
                                onClick = { leaveType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = {
                            reason = it
                            formError = null
                        },
                        label = { Text("Reason for Leave") },
                        placeholder = { Text("Provide details for advisor approval") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    formError?.let {
                        Text(text = it, color = Rose600, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.isBlank()) {
                            formError = "Please describe the reason for your leave request."
                        } else {
                            val leave = LeaveApplicationEntity(
                                studentRegisterNumber = studentProfile.registerNumber,
                                studentName = studentProfile.fullName,
                                department = "CSE",
                                section = studentProfile.section,
                                leaveType = leaveType,
                                startDate = startDate,
                                endDate = endDate,
                                reason = reason,
                                advisorStatus = "PENDING",
                                hodStatus = "PENDING",
                                overallStatus = "PENDING",
                                remarks = "",
                                appliedAt = "2026-10-15"
                            )
                            onApplyLeave(leave)
                            showApplyLeaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                ) {
                    Text("Submit Application")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Submit Complaint / Grievance Dialog
    if (showSubmitComplaintDialog) {
        var category by remember { mutableStateOf("Academic") }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var isAnonymous by remember { mutableStateOf(false) }
        var formError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSubmitComplaintDialog = false },
            title = {
                Text(
                    text = "Lodge Grievance / Complaint",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Category", style = MaterialTheme.typography.labelSmall, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Academic", "Hostel", "Lab", "Discipline").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            formError = null
                        },
                        label = { Text("Subject / Title") },
                        placeholder = { Text("Brief issue headline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            formError = null
                        },
                        label = { Text("Detailed Description") },
                        placeholder = { Text("Explain clearly what assistance is required") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isAnonymous,
                            onCheckedChange = { isAnonymous = it }
                        )
                        Text(
                            text = "Submit Anonymously (Hide Name & Reg No)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }

                    formError?.let {
                        Text(text = it, color = Rose600, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isBlank() || description.isBlank()) {
                            formError = "Please enter both subject and description."
                        } else {
                            val comp = ComplaintEntity(
                                userId = studentProfile?.registerNumber ?: currentUser.id,
                                studentName = if (isAnonymous) "Anonymous Student" else (studentProfile?.fullName ?: currentUser.fullName),
                                category = category,
                                title = title,
                                description = description,
                                isAnonymous = isAnonymous,
                                status = "OPEN",
                                officialResponse = "",
                                submittedAt = "2026-10-15"
                            )
                            onSubmitComplaint(comp)
                            showSubmitComplaintDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                ) {
                    Text("Submit Grievance")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitComplaintDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Submit Assignment Dialog
    selectedAssignmentForSubmission?.let { assignment ->
        var submissionText by remember { mutableStateOf("") }
        var formError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { selectedAssignmentForSubmission = null },
            title = {
                Text(
                    text = "Submit Assignment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Navy700
                    )
                    Text(
                        text = "${assignment.subjectCode} • Due: ${assignment.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = submissionText,
                        onValueChange = {
                            submissionText = it
                            formError = null
                        },
                        label = { Text("Submission Solution / Link / Code") },
                        placeholder = { Text("Paste your solution URL, git repo, or written report here...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                    formError?.let {
                        Text(text = it, color = Rose600, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (submissionText.isBlank()) {
                            formError = "Please enter your submission text or repository link."
                        } else {
                            val sub = AssignmentSubmissionEntity(
                                assignmentId = assignment.id,
                                studentRegisterNumber = studentProfile?.registerNumber ?: currentUser.registerNumberOrEmpId,
                                submissionText = submissionText,
                                submittedAt = "2026-10-15 10:30 AM",
                                status = "SUBMITTED"
                            )
                            onSubmitAssignment(sub)
                            selectedAssignmentForSubmission = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAssignmentForSubmission = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// 1. Overview Tab
@Composable
fun StudentOverviewTab(
    student: StudentEntity?,
    announcements: List<AnnouncementEntity>,
    assignments: List<AssignmentEntity>,
    submissions: List<AssignmentSubmissionEntity>,
    onNavigateToTab: (Int) -> Unit,
    onApplyLeaveClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy700),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("") ?: "ST",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student?.fullName ?: "Student",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Reg No: ${student?.registerNumber ?: "711025205043"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "B.E. Computer Science • Year ${student?.year ?: 3}, Sec ${student?.section ?: "A"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Navy100
                        )
                    }
                }
            }
        }

        // Quick Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Attendance",
                    value = "${student?.overallAttendance ?: 91.5}%",
                    subtitle = if ((student?.overallAttendance ?: 91.5) < 75.0) "Critical Alert!" else "Eligible for Exams",
                    icon = Icons.Default.EventAvailable,
                    iconTint = if ((student?.overallAttendance ?: 91.5) < 75.0) Rose600 else Emerald600,
                    isAlert = (student?.overallAttendance ?: 91.5) < 75.0,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "CGPA",
                    value = String.format("%.2f", student?.cgpa ?: 9.12),
                    subtitle = "Semester 6 Rank: #1",
                    icon = Icons.Default.Stars,
                    iconTint = Gold600,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Portal Actions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActionPillButton(
                            title = "View Timetable",
                            icon = Icons.Default.CalendarMonth,
                            color = Navy700,
                            onClick = { onNavigateToTab(3) }
                        )
                        ActionPillButton(
                            title = "Check Marks",
                            icon = Icons.Default.Grade,
                            color = Cyan700,
                            onClick = { onNavigateToTab(2) }
                        )
                        ActionPillButton(
                            title = "Apply Leave",
                            icon = Icons.Default.FlightTakeoff,
                            color = Emerald700,
                            onClick = onApplyLeaveClick
                        )
                    }
                }
            }
        }

        // Pending Tasks / Assignments Banner
        item {
            val pendingCount = assignments.size - submissions.size
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Cyan100.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentLate,
                        contentDescription = null,
                        tint = Cyan700,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Course Assignments",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Cyan700
                        )
                        Text(
                            text = "$pendingCount active assignments pending submission",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }
                    TextButton(onClick = { onNavigateToTab(4) }) {
                        Text("View Tasks", fontWeight = FontWeight.Bold, color = Cyan700)
                    }
                }
            }
        }

        // Latest Campus Announcements
        item {
            Text(
                text = "Recent Campus Announcements",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Navy900,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(announcements.take(3)) { announcement ->
            AnnouncementCard(announcement = announcement)
        }
    }
}

// 2. Attendance Tab
@Composable
fun StudentAttendanceTab(
    student: StudentEntity?,
    courses: List<CourseEntity>
) {
    val overallPercentage = student?.overallAttendance ?: 86.5

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Semester Attendance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Text(
                        text = "Minimum 75% required for University Exam Hall Ticket",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    CircularAttendanceGauge(
                        percentage = overallPercentage,
                        size = 140.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AttendanceSummaryStat(label = "Total Classes", value = "240")
                        AttendanceSummaryStat(label = "Attended", value = "208")
                        AttendanceSummaryStat(label = "Leaves / OD", value = "12")
                        AttendanceSummaryStat(label = "Absent", value = "20")
                    }
                }
            }
        }

        item {
            Text(
                text = "Subject-Wise Attendance Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        // Subject breakdown cards
        val sampleStats = listOf(
            Triple("CS8601 - Mobile App Dev", 88.0, Pair(44, 50)),
            Triple("CS8602 - Artificial Intelligence", 92.5, Pair(37, 40)),
            Triple("CS8603 - Cloud Computing", 85.0, Pair(34, 40)),
            Triple("CS8604 - Cryptography & Security", 73.5, Pair(25, 34)), // < 75% Warning!
            Triple("CS8651 - IoT Laboratory", 95.0, Pair(19, 20)),
            Triple("HS8581 - Professional Ethics", 87.5, Pair(28, 32))
        )

        items(sampleStats) { item ->
            val (subject, pct, counts) = item
            val isLow = pct < 75.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLow) Rose100.copy(alpha = 0.4f) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subject,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isLow) Rose700 else Navy900
                            )
                            Text(
                                text = "${counts.first} attended out of ${counts.second} periods",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                        Text(
                            text = "${pct}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLow) Rose600 else Emerald600
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (pct / 100.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isLow) Rose600 else Emerald600,
                        trackColor = Slate300.copy(alpha = 0.4f)
                    )

                    if (isLow) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Rose600,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Attendance below 75% threshold! Remedial action required.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Rose700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. Academics Tab (Marks & Results)
@Composable
fun StudentAcademicsTab(
    student: StudentEntity?,
    marks: List<ExamMarkEntity>,
    courses: List<CourseEntity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // CGPA & Semester Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy700),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cumulative Grade Point Average",
                            style = MaterialTheme.typography.bodySmall,
                            color = Navy100
                        )
                        Text(
                            text = String.format("%.2f", student?.cgpa ?: 9.12),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Semester 6 • 138 Credits Earned",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Surface(
                        color = Gold500,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "FIRST CLASS WITH DISTINCTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Navy900,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Continuous Internal Assessment (CIA) Marks",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        if (marks.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Marks Published",
                    subtitle = "Internal examination results will appear here once faculty uploads them.",
                    icon = Icons.Default.Grade
                )
            }
        } else {
            items(marks) { mark ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mark.subjectCode,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Text(
                                text = mark.examType,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${mark.marksScored.toInt()} / ${mark.maxMarks.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Navy700
                            )
                            Surface(
                                color = Navy100,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Grade: ${mark.grade}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Semester Wise GPA Progression",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SemesterGpaItem(sem = "Sem 1", gpa = "8.40")
                    SemesterGpaItem(sem = "Sem 2", gpa = "8.65")
                    SemesterGpaItem(sem = "Sem 3", gpa = "8.80")
                    SemesterGpaItem(sem = "Sem 4", gpa = "8.92")
                    SemesterGpaItem(sem = "Sem 5", gpa = "8.75")
                }
            }
        }
    }
}

// 4. Timetable Tab
@Composable
fun StudentTimetableTab(
    slots: List<TimetableSlotEntity>
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    var selectedDay by remember { mutableStateOf("Monday") }

    val filteredSlots = slots.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Day selector tabs
        ScrollableTabRow(
            selectedTabIndex = days.indexOf(selectedDay).coerceAtLeast(0),
            containerColor = Color.White,
            contentColor = Navy700,
            edgePadding = 16.dp
        ) {
            days.forEach { day ->
                Tab(
                    selected = selectedDay == day,
                    onClick = { selectedDay = day },
                    text = {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredSlots.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Classes Scheduled",
                        subtitle = "No lecture periods listed for $selectedDay.",
                        icon = Icons.Default.EventBusy
                    )
                }
            } else {
                items(filteredSlots) { slot ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Navy100),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "P${slot.period}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy700
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = slot.subjectTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = "${slot.subjectCode} • ${slot.facultyName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Cyan700,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = slot.timeRange,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Cyan700
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(
                                        imageVector = Icons.Default.Room,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = slot.roomNo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. Assignments Tab
@Composable
fun StudentAssignmentsTab(
    assignments: List<AssignmentEntity>,
    submissions: List<AssignmentSubmissionEntity>,
    onSubmitClick: (AssignmentEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Course Assignments & Project Submissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        if (assignments.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Assignments Assigned",
                    subtitle = "You are all caught up! Faculty will post assignments here.",
                    icon = Icons.Default.AssignmentTurnedIn
                )
            }
        } else {
            items(assignments) { assignment ->
                val submission = submissions.firstOrNull { it.assignmentId == assignment.id }
                val isSubmitted = submission != null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = if (isSubmitted) "SUBMITTED" else "PENDING")
                            Text(
                                text = "Due: ${assignment.dueDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSubmitted) Slate500 else Rose600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = assignment.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )

                        Text(
                            text = "${assignment.subjectCode} • ${assignment.subjectTitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cyan700,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = assignment.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isSubmitted) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Emerald100.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Emerald700,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Solution submitted on ${submission?.submittedAt}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Emerald700,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { onSubmitClick(assignment) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                            ) {
                                Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Solution")
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. Services Tab (Leaves & Grievance)
@Composable
fun StudentServicesTab(
    leaves: List<LeaveApplicationEntity>,
    complaints: List<ComplaintEntity>,
    onApplyLeaveClick: () -> Unit,
    onNewComplaintClick: () -> Unit
) {
    var serviceSection by remember { mutableIntStateOf(0) } // 0: Leaves, 1: Grievance

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = serviceSection,
            containerColor = Color.White,
            contentColor = Navy700
        ) {
            Tab(
                selected = serviceSection == 0,
                onClick = { serviceSection = 0 },
                text = { Text("Leave Applications (${leaves.size})") }
            )
            Tab(
                selected = serviceSection == 1,
                onClick = { serviceSection = 1 },
                text = { Text("Complaints & Grievances (${complaints.size})") }
            )
        }

        if (serviceSection == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Button(
                        onClick = onApplyLeaveClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply New Leave / On-Duty")
                    }
                }

                if (leaves.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Leave Applications",
                            subtitle = "You have not submitted any leave or on-duty requests.",
                            icon = Icons.Default.FlightTakeoff
                        )
                    }
                } else {
                    items(leaves) { leave ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = leave.leaveType,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                    StatusBadge(status = leave.overallStatus)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Duration: ${leave.startDate} to ${leave.endDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Reason: ${leave.reason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                                if (leave.remarks.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Advisor Remark: ${leave.remarks}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Navy600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Button(
                        onClick = onNewComplaintClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                    ) {
                        Icon(imageVector = Icons.Default.AddComment, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lodge New Grievance / Complaint")
                    }
                }

                if (complaints.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Grievances Lodged",
                            subtitle = "Have an issue with lab equipment, hostel, or academics? Submit securely here.",
                            icon = Icons.Default.SupportAgent
                        )
                    }
                } else {
                    items(complaints) { comp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = comp.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                    StatusBadge(status = comp.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Category: ${comp.category} • Submitted: ${comp.submittedAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = comp.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                                if (comp.officialResponse.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = Cyan100.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Official Action: ${comp.officialResponse}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Cyan700,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helpers
@Composable
fun ActionPillButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = Slate700
        )
    }
}

@Composable
fun AttendanceSummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Navy900)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate500)
    }
}

@Composable
fun SemesterGpaItem(sem: String, gpa: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = gpa, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Navy700)
        Text(text = sem, style = MaterialTheme.typography.labelSmall, color = Slate500)
    }
}

