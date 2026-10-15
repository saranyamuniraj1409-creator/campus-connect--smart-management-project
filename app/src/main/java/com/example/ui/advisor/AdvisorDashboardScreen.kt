package com.example.ui.advisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AdvisorDashboardScreen(
    currentUser: UserEntity,
    assignedSection: String,
    students: List<StudentEntity>,
    courses: List<CourseEntity>,
    timetableSlots: List<TimetableSlotEntity>,
    leaves: List<LeaveApplicationEntity>,
    announcements: List<AnnouncementEntity>,
    onSaveAttendanceBatch: (List<AttendanceEntity>) -> Unit,
    onSaveExamMark: (ExamMarkEntity) -> Unit,
    onUpdateLeaveStatus: (LeaveApplicationEntity, isApproved: Boolean, remarks: String) -> Unit,
    onPostAnnouncement: (AnnouncementEntity) -> Unit,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val assignedClassLabel = if (currentUser.assignedClass.isNotBlank()) currentUser.assignedClass else "CSE-3A"

    val navTabs = listOf(
        Triple("Students", Icons.Filled.People, Icons.Outlined.People),
        Triple("Attendance", Icons.Filled.HowToReg, Icons.Outlined.HowToReg),
        Triple("Marks", Icons.Filled.Score, Icons.Outlined.Score),
        Triple("Leaves", Icons.Filled.Approval, Icons.Outlined.Approval),
        Triple("Circulars", Icons.Filled.Campaign, Icons.Outlined.Campaign)
    )

    var showPostNoticeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CampusTopAppBar(
                title = "Class Advisor Portal",
                subtitle = "${currentUser.fullName} • Class: $assignedClassLabel",
                currentRole = UserRole.ADVISOR,
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
                        modifier = Modifier.testTag("nav_advisor_tab_$index")
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
                0 -> AdvisorStudentsTab(
                    assignedClass = assignedClassLabel,
                    students = students
                )
                1 -> AdvisorMarkAttendanceTab(
                    assignedClass = assignedClassLabel,
                    students = students,
                    courses = courses,
                    onSaveAttendanceBatch = onSaveAttendanceBatch
                )
                2 -> AdvisorEnterMarksTab(
                    students = students,
                    courses = courses,
                    onSaveMark = onSaveExamMark
                )
                3 -> AdvisorLeavesReviewTab(
                    leaves = leaves,
                    onReviewLeave = onUpdateLeaveStatus
                )
                4 -> AdvisorCircularsTab(
                    assignedClass = assignedClassLabel,
                    announcements = announcements,
                    onOpenNewNoticeDialog = { showPostNoticeDialog = true }
                )
            }
        }
    }

    if (showPostNoticeDialog) {
        var noticeTitle by remember { mutableStateOf("") }
        var noticeContent by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("NORMAL") }

        AlertDialog(
            onDismissRequest = { showPostNoticeDialog = false },
            title = {
                Text(
                    text = "Post Class Notice to $assignedClassLabel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text("Notice Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noticeContent,
                        onValueChange = { noticeContent = it },
                        label = { Text("Announcement Details") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("NORMAL", "HIGH", "URGENT").forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noticeTitle.isNotBlank() && noticeContent.isNotBlank()) {
                            onPostAnnouncement(
                                AnnouncementEntity(
                                    title = noticeTitle,
                                    content = noticeContent,
                                    senderRole = "ADVISOR",
                                    senderName = "${currentUser.fullName} (Class Advisor)",
                                    targetAudience = assignedClassLabel,
                                    priority = priority,
                                    publishedAt = "2026-10-15"
                                )
                            )
                            showPostNoticeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                ) {
                    Text("Publish Notice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostNoticeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// 1. Students List Tab
@Composable
fun AdvisorStudentsTab(
    assignedClass: String,
    students: List<StudentEntity>
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterLowAttendanceOnly by remember { mutableStateOf(false) }

    val filteredList = students.filter { s ->
        val matchesQuery = s.fullName.contains(searchQuery, ignoreCase = true) ||
                s.registerNumber.contains(searchQuery, ignoreCase = true)
        val matchesAttendance = if (filterLowAttendanceOnly) s.overallAttendance < 75.0 else true
        matchesQuery && matchesAttendance
    }

    val lowAttendanceCount = students.count { it.overallAttendance < 75.0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Class Summary Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy700)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Assigned Class: $assignedClass",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Department of Computer Science & Engineering • 3rd Year",
                        style = MaterialTheme.typography.bodySmall,
                        color = Navy100
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ClassOverviewMetric(label = "Total Strength", value = "${students.size} Students")
                        ClassOverviewMetric(label = "Avg Attendance", value = "85.8%")
                        ClassOverviewMetric(label = "At-Risk (<75%)", value = "$lowAttendanceCount Flagged", isAlert = lowAttendanceCount > 0)
                    }
                }
            }
        }

        // Search & Filter
        item {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or register number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = filterLowAttendanceOnly,
                        onClick = { filterLowAttendanceOnly = !filterLowAttendanceOnly },
                        label = { Text("Show Low Attendance (<75%) Only") },
                        leadingIcon = if (filterLowAttendanceOnly) {
                            { Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        items(filteredList) { student ->
            val isLowAttendance = student.overallAttendance < 75.0
            var showWarnNoticeSent by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLowAttendance) Rose100.copy(alpha = 0.4f) else Color.White
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
                                text = student.fullName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Text(
                                text = "Reg: ${student.registerNumber} • CGPA: ${String.format("%.2f", student.cgpa)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${student.overallAttendance}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLowAttendance) Rose600 else Emerald600
                            )
                            StatusBadge(status = if (isLowAttendance) "DEFICIENT" else "REGULAR")
                        }
                    }

                    if (isLowAttendance) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Critical: Defaulter notice required",
                                style = MaterialTheme.typography.labelSmall,
                                color = Rose700,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (showWarnNoticeSent) {
                                Text(
                                    text = "Warning SMS/Email Dispatched",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Emerald700,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                OutlinedButton(
                                    onClick = { showWarnNoticeSent = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose700),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send Warning Alert", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Mark Attendance Tab
@Composable
fun AdvisorMarkAttendanceTab(
    assignedClass: String,
    students: List<StudentEntity>,
    courses: List<CourseEntity>,
    onSaveAttendanceBatch: (List<AttendanceEntity>) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(courses.firstOrNull()?.code ?: "CS8601") }
    var selectedDate by remember { mutableStateOf("2026-10-15") }
    val attendanceStates = remember { mutableStateMapOf<String, String>() }

    // Initialize all to PRESENT by default
    LaunchedEffect(students) {
        students.forEach { s ->
            if (!attendanceStates.containsKey(s.registerNumber)) {
                attendanceStates[s.registerNumber] = "PRESENT"
            }
        }
    }

    var showSavedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Roll Call & Attendance Register",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("Attendance Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Subject Code: $selectedSubject", style = MaterialTheme.typography.bodySmall, color = Navy700, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Students in $assignedClass (${students.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                TextButton(
                    onClick = {
                        students.forEach { attendanceStates[it.registerNumber] = "PRESENT" }
                    }
                ) {
                    Text("Mark All Present", fontWeight = FontWeight.Bold, color = Navy700)
                }
            }
        }

        items(students) { student ->
            val currentStatus = attendanceStates[student.registerNumber] ?: "PRESENT"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = student.registerNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("PRESENT", "ABSENT", "OD").forEach { statusOption ->
                            val isSelected = currentStatus == statusOption
                            val btnColor = when (statusOption) {
                                "PRESENT" -> Emerald600
                                "ABSENT" -> Rose600
                                else -> Cyan600
                            }
                            FilledTonalButton(
                                onClick = { attendanceStates[student.registerNumber] = statusOption },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) btnColor else Slate100,
                                    contentColor = if (isSelected) Color.White else Slate700
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = when (statusOption) {
                                        "PRESENT" -> "P"
                                        "ABSENT" -> "A"
                                        else -> "OD"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val records = students.map { s ->
                        AttendanceEntity(
                            studentRegisterNumber = s.registerNumber,
                            subjectCode = selectedSubject,
                            date = selectedDate,
                            status = attendanceStates[s.registerNumber] ?: "PRESENT",
                            section = assignedClass
                        )
                    }
                    onSaveAttendanceBatch(records)
                    showSavedMessage = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_attendance_batch_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy700)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save and Submit Attendance", fontWeight = FontWeight.Bold)
            }

            if (showSavedMessage) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Emerald100,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Attendance saved successfully in institution database!",
                        color = Emerald700,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

// 3. Enter Marks Tab
@Composable
fun AdvisorEnterMarksTab(
    students: List<StudentEntity>,
    courses: List<CourseEntity>,
    onSaveMark: (ExamMarkEntity) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(courses.firstOrNull()?.code ?: "CS8601") }
    var selectedExamType by remember { mutableStateOf("Internal Assessment 2") }
    var selectedStudentReg by remember { mutableStateOf(students.firstOrNull()?.registerNumber ?: "") }
    var markValue by remember { mutableStateOf("") }
    var maxMarkValue by remember { mutableStateOf("50") }
    var markSavedSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Upload Continuous Assessment Marks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select Exam Type", style = MaterialTheme.typography.labelSmall, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Internal 1", "Internal 2", "Model Exam").forEach { type ->
                            FilterChip(
                                selected = selectedExamType == type,
                                onClick = { selectedExamType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Student", style = MaterialTheme.typography.labelSmall, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    students.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedStudentReg == s.registerNumber,
                                onClick = { selectedStudentReg = s.registerNumber }
                            )
                            Text(
                                text = "${s.fullName} (${s.registerNumber})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = markValue,
                            onValueChange = { markValue = it },
                            label = { Text("Marks Scored") },
                            placeholder = { Text("e.g. 46") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxMarkValue,
                            onValueChange = { maxMarkValue = it },
                            label = { Text("Out of") },
                            placeholder = { Text("50") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val score = markValue.toDoubleOrNull() ?: 0.0
                            val max = maxMarkValue.toDoubleOrNull() ?: 50.0
                            val pct = (score / max) * 100.0
                            val grade = when {
                                pct >= 90 -> "O"
                                pct >= 80 -> "A+"
                                pct >= 70 -> "A"
                                pct >= 60 -> "B+"
                                pct >= 50 -> "B"
                                else -> "RA"
                            }
                            onSaveMark(
                                ExamMarkEntity(
                                    studentRegisterNumber = selectedStudentReg,
                                    subjectCode = selectedSubject,
                                    examType = selectedExamType,
                                    marksScored = score,
                                    maxMarks = max,
                                    grade = grade
                                )
                            )
                            markSavedSuccess = true
                            markValue = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_exam_marks_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                    ) {
                        Text("Record & Publish Mark")
                    }

                    if (markSavedSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Exam mark successfully updated!",
                            color = Emerald700,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// 4. Leave Review Tab
@Composable
fun AdvisorLeavesReviewTab(
    leaves: List<LeaveApplicationEntity>,
    onReviewLeave: (LeaveApplicationEntity, isApproved: Boolean, remarks: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Student Leave & OD Approvals (${leaves.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        if (leaves.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Pending Leave Requests",
                    subtitle = "All student applications have been processed.",
                    icon = Icons.Default.DoneAll
                )
            }
        } else {
            items(leaves) { leave ->
                var remarksText by remember { mutableStateOf(leave.remarks) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = leave.studentName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = "Reg: ${leave.studentRegisterNumber} • ${leave.leaveType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Cyan700,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            StatusBadge(status = leave.advisorStatus)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dates: ${leave.startDate} to ${leave.endDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                        Text(
                            text = "Reason: ${leave.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )

                        if (leave.advisorStatus == "PENDING") {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = remarksText,
                                onValueChange = { remarksText = it },
                                label = { Text("Advisor Remarks (optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onReviewLeave(leave, true, remarksText) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve")
                                }
                                Button(
                                    onClick = { onReviewLeave(leave, false, remarksText) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Rose600),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. Circulars Tab
@Composable
fun AdvisorCircularsTab(
    assignedClass: String,
    announcements: List<AnnouncementEntity>,
    onOpenNewNoticeDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Button(
                onClick = onOpenNewNoticeDialog,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy700)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast Class Notice to $assignedClass")
            }
        }

        items(announcements) { notice ->
            AnnouncementCard(announcement = notice)
        }
    }
}

@Composable
fun ClassOverviewMetric(label: String, value: String, isAlert: Boolean = false) {
    Column {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isAlert) Rose100 else Color.White)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Navy100)
    }
}
