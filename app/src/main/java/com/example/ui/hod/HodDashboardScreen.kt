package com.example.ui.hod

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
fun HodDashboardScreen(
    currentUser: UserEntity,
    department: DepartmentEntity?,
    students: List<StudentEntity>,
    courses: List<CourseEntity>,
    leaves: List<LeaveApplicationEntity>,
    announcements: List<AnnouncementEntity>,
    onApproveLeave: (LeaveApplicationEntity, isApproved: Boolean) -> Unit,
    onPublishAnnouncement: (AnnouncementEntity) -> Unit,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val deptName = department?.name ?: "Computer Science & Engineering"

    val navTabs = listOf(
        Triple("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        Triple("Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
        Triple("Faculty", Icons.Filled.PeopleAlt, Icons.Outlined.PeopleAlt),
        Triple("Approvals", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser),
        Triple("Reports", Icons.Filled.Summarize, Icons.Outlined.Summarize)
    )

    var showPostCircularDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CampusTopAppBar(
                title = "HOD Department Portal",
                subtitle = "Dr. Rajesh Natarajan • Head of $deptName",
                currentRole = UserRole.HOD,
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
                        modifier = Modifier.testTag("nav_hod_tab_$index")
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
                0 -> HodOverviewTab(
                    department = department,
                    students = students,
                    announcements = announcements,
                    onOpenNoticeDialog = { showPostCircularDialog = true }
                )
                1 -> HodAnalyticsTab(
                    students = students,
                    courses = courses
                )
                2 -> HodFacultyRecordsTab(
                    courses = courses
                )
                3 -> HodApprovalsTab(
                    leaves = leaves,
                    onApproveLeave = onApproveLeave
                )
                4 -> HodReportsTab(
                    deptName = deptName,
                    students = students,
                    courses = courses
                )
            }
        }
    }

    if (showPostCircularDialog) {
        var circularTitle by remember { mutableStateOf("") }
        var circularContent by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("HIGH") }

        AlertDialog(
            onDismissRequest = { showPostCircularDialog = false },
            title = {
                Text(
                    text = "Publish Department Circular",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = circularTitle,
                        onValueChange = { circularTitle = it },
                        label = { Text("Circular Heading") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = circularContent,
                        onValueChange = { circularContent = it },
                        label = { Text("Circular Body & Directives") },
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
                        if (circularTitle.isNotBlank() && circularContent.isNotBlank()) {
                            onPublishAnnouncement(
                                AnnouncementEntity(
                                    title = circularTitle,
                                    content = circularContent,
                                    senderRole = "HOD",
                                    senderName = "Dr. Rajesh Natarajan (HOD CSE)",
                                    targetAudience = "CSE_DEPT",
                                    priority = priority,
                                    publishedAt = "2026-10-15"
                                )
                            )
                            showPostCircularDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                ) {
                    Text("Publish to Department")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostCircularDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// 1. Overview Tab
@Composable
fun HodOverviewTab(
    department: DepartmentEntity?,
    students: List<StudentEntity>,
    announcements: List<AnnouncementEntity>,
    onOpenNoticeDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy700),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = department?.name ?: "Computer Science & Engineering",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Established ${department?.establishedYear ?: 2004} • Accreditation: NBA Tier 1",
                        style = MaterialTheme.typography.bodySmall,
                        color = Navy100
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "${department?.totalStudents ?: 480}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Total Enrolled", style = MaterialTheme.typography.labelSmall, color = Navy100)
                        }
                        Column {
                            Text(text = "${department?.totalFaculty ?: 32}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Faculty Members", style = MaterialTheme.typography.labelSmall, color = Navy100)
                        }
                        Column {
                            Text(text = "94.2%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Gold500)
                            Text(text = "Pass Rate", style = MaterialTheme.typography.labelSmall, color = Navy100)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Dept Attendance",
                    value = "87.6%",
                    subtitle = "+2.1% from last month",
                    icon = Icons.Default.TrendingUp,
                    iconTint = Emerald600,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Placements",
                    value = "88.4%",
                    subtitle = "142 Offers secured",
                    icon = Icons.Default.Work,
                    iconTint = Cyan600,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Button(
                onClick = onOpenNoticeDialog,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy700)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Publish Department Circular / Notice")
            }
        }

        item {
            Text(
                text = "Department Circulars & Bulletins",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        items(announcements.filter { it.targetAudience == "CSE_DEPT" || it.targetAudience == "ALL" }) { notice ->
            AnnouncementCard(announcement = notice)
        }
    }
}

// 2. Analytics Tab
@Composable
fun HodAnalyticsTab(
    students: List<StudentEntity>,
    courses: List<CourseEntity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Department Attendance Trends (By Year)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AnalyticsBarItem(label = "1st Year (B.E. CSE)", percentage = 92.4, color = Emerald600)
                    Spacer(modifier = Modifier.height(10.dp))
                    AnalyticsBarItem(label = "2nd Year (B.E. CSE)", percentage = 88.0, color = Cyan600)
                    Spacer(modifier = Modifier.height(10.dp))
                    AnalyticsBarItem(label = "3rd Year (B.E. CSE)", percentage = 85.8, color = Navy700)
                    Spacer(modifier = Modifier.height(10.dp))
                    AnalyticsBarItem(label = "4th Year (B.E. CSE)", percentage = 84.1, color = Gold600)
                }
            }
        }

        item {
            Text(
                text = "Subject Pass Percentage (Semester 6)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AnalyticsBarItem(label = "CS8601 - Mobile App Dev", percentage = 96.5, color = Emerald600)
                    Spacer(modifier = Modifier.height(10.dp))
                    AnalyticsBarItem(label = "CS8602 - Artificial Intelligence", percentage = 91.0, color = Cyan600)
                    Spacer(modifier = Modifier.height(10.dp))
                    AnalyticsBarItem(label = "CS8603 - Cloud Computing", percentage = 94.0, color = Emerald600)
                    Spacer(modifier = Modifier.height(10.dp))
                    AnalyticsBarItem(label = "CS8604 - Cryptography & Security", percentage = 82.5, color = Gold600)
                }
            }
        }

        item {
            Text(
                text = "Semester Top Performers (Merit List)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        items(students.sortedByDescending { it.cgpa }.take(4)) { s ->
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
                    Column {
                        Text(text = s.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Navy900)
                        Text(text = "Reg No: ${s.registerNumber}", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    }
                    Surface(
                        color = Gold100,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "CGPA: ${String.format("%.2f", s.cgpa)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gold600,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// 3. Faculty Records Tab
@Composable
fun HodFacultyRecordsTab(
    courses: List<CourseEntity>
) {
    val facultyMembers = listOf(
        Triple("Dr. Priya Raman", "Associate Professor", "Mobile Application Development (CS8601)"),
        Triple("Dr. Rajesh Natarajan", "Professor & HOD", "Artificial Intelligence & ML (CS8602)"),
        Triple("Prof. Ananya Roy", "Assistant Professor", "Distributed Cloud Computing (CS8603)"),
        Triple("Prof. Karthik V", "Assistant Professor", "Cryptography & Network Security (CS8604)"),
        Triple("Prof. Suresh B", "Lab Instructor / AP", "Internet of Things Laboratory (CS8651)"),
        Triple("Dr. Malathi S", "Associate Professor", "Professional Ethics & Communication (HS8581)")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Faculty Course Allocations (Semester 6)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        items(facultyMembers) { member ->
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Navy100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.first.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Navy700
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = member.first,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = member.second,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = member.third,
                            style = MaterialTheme.typography.bodySmall,
                            color = Cyan700,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// 4. Approvals Tab
@Composable
fun HodApprovalsTab(
    leaves: List<LeaveApplicationEntity>,
    onApproveLeave: (LeaveApplicationEntity, isApproved: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Escalated On-Duty & Medical Approvals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        if (leaves.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Pending Approvals",
                    subtitle = "All departmental leave and hall booking permissions have been addressed.",
                    icon = Icons.Default.TaskAlt
                )
            }
        } else {
            items(leaves) { leave ->
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
                                    text = "Class: ${leave.section} • ${leave.leaveType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Cyan700
                                )
                            }
                            StatusBadge(status = leave.hodStatus)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Dates: ${leave.startDate} to ${leave.endDate}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text(text = "Reason: ${leave.reason}", style = MaterialTheme.typography.bodySmall, color = Slate700)

                        if (leave.hodStatus == "PENDING") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onApproveLeave(leave, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Grant HOD Approval")
                                }
                                Button(
                                    onClick = { onApproveLeave(leave, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Rose600),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Decline")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. Reports Tab
@Composable
fun HodReportsTab(
    deptName: String,
    students: List<StudentEntity>,
    courses: List<CourseEntity>
) {
    var reportGenerated by remember { mutableStateOf(false) }

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
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Department Performance Report Generator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Text(
                        text = "Compile semester audit, attendance metrics, and faculty reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { reportGenerated = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Comprehensive Audit Report")
                    }
                }
            }
        }

        if (reportGenerated) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy50),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OFFICIAL AUDIT REPORT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Navy700
                            )
                            StatusBadge(status = "VERIFIED")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Department of $deptName",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = "Semester 6 Academic Year 2025-2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        ReportStatLine("Total Students Audited:", "${students.size} Active Candidates")
                        ReportStatLine("Class Average Attendance:", "85.8%")
                        ReportStatLine("Department Pass Margin:", "94.2%")
                        ReportStatLine("Remedial Attention Required:", "${students.count { it.overallAttendance < 75.0 }} Students")
                        ReportStatLine("Accreditation Compliance:", "100% Satisfactory")

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Certified by Dr. Rajesh Natarajan (Head of Department) on 2026-10-15",
                            style = MaterialTheme.typography.labelSmall,
                            color = Navy700,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Slate700)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Navy900)
    }
}

@Composable
fun AnalyticsBarItem(label: String, percentage: Double, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Navy900)
            Text(text = "${percentage}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Slate300.copy(alpha = 0.4f)
        )
    }
}
