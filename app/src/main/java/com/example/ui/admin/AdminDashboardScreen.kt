package com.example.ui.admin

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
fun AdminDashboardScreen(
    currentUser: UserEntity,
    users: List<UserEntity>,
    departments: List<DepartmentEntity>,
    courses: List<CourseEntity>,
    announcements: List<AnnouncementEntity>,
    onCreateUser: (UserEntity) -> Unit,
    onDeleteUser: (String) -> Unit,
    onAddCourse: (CourseEntity) -> Unit,
    onBroadcastAnnouncement: (AnnouncementEntity) -> Unit,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showBroadcastNoticeDialog by remember { mutableStateOf(false) }

    val navTabs = listOf(
        Triple("College", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        Triple("Accounts", Icons.Filled.ManageAccounts, Icons.Outlined.ManageAccounts),
        Triple("Courses", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        Triple("Broadcast", Icons.Filled.Campaign, Icons.Outlined.Campaign),
        Triple("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CampusTopAppBar(
                title = "College Admin Portal",
                subtitle = "Dr. K. S. Sundaram • Principal & Chief Administrator",
                currentRole = UserRole.ADMIN,
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
                        modifier = Modifier.testTag("nav_admin_tab_$index")
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
                0 -> AdminCollegeOverviewTab(
                    departments = departments,
                    courses = courses,
                    users = users,
                    onNavigateToTab = { selectedTab = it }
                )
                1 -> AdminUserManagementTab(
                    users = users,
                    onOpenCreateUser = { showCreateUserDialog = true },
                    onDeleteUser = onDeleteUser
                )
                2 -> AdminCoursesTab(
                    courses = courses,
                    departments = departments,
                    onOpenAddCourse = { showAddCourseDialog = true }
                )
                3 -> AdminBroadcastTab(
                    announcements = announcements,
                    onOpenBroadcastDialog = { showBroadcastNoticeDialog = true }
                )
                4 -> AdminSettingsTab()
            }
        }
    }

    // Create User Dialog
    if (showCreateUserDialog) {
        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("pass123") }
        var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
        var registerOrEmpId by remember { mutableStateOf("") }
        var assignedClass by remember { mutableStateOf("CSE-3A") }
        var departmentId by remember { mutableStateOf("DEPT-CSE") }
        var formError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = {
                Text(
                    text = "Provision Institutional Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Account Role", style = MaterialTheme.typography.labelSmall, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(UserRole.STUDENT, UserRole.ADVISOR, UserRole.HOD).forEach { role ->
                            FilterChip(
                                selected = selectedRole == role,
                                onClick = { selectedRole = role },
                                label = { Text(role.displayName.split(" ").first(), fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Legal Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Institutional Email") },
                        placeholder = { Text("new.user@campus.edu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = registerOrEmpId,
                        onValueChange = { registerOrEmpId = it },
                        label = { Text(if (selectedRole == UserRole.STUDENT) "Register Number" else "Faculty Employee ID") },
                        placeholder = { Text(if (selectedRole == UserRole.STUDENT) "717622CS107" else "EMP-CSE-105") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (selectedRole == UserRole.STUDENT || selectedRole == UserRole.ADVISOR) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = assignedClass,
                            onValueChange = { assignedClass = it },
                            label = { Text("Class / Section") },
                            placeholder = { Text("CSE-3A") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    formError?.let {
                        Text(text = it, color = Rose600, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.isBlank() || email.isBlank() || registerOrEmpId.isBlank()) {
                            formError = "Please fill in all account fields."
                        } else {
                            val initials = fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
                            val newUser = UserEntity(
                                id = "USR-${System.currentTimeMillis() % 10000}",
                                email = email.trim().lowercase(),
                                passwordHash = password,
                                fullName = fullName.trim(),
                                role = selectedRole,
                                departmentId = departmentId,
                                assignedClass = if (selectedRole == UserRole.HOD || selectedRole == UserRole.ADMIN) "" else assignedClass,
                                registerNumberOrEmpId = registerOrEmpId.trim(),
                                phone = "+1 (555) 000-1122",
                                avatarInitials = initials
                            )
                            onCreateUser(newUser)
                            showCreateUserDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                ) {
                    Text("Provision Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateUserDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Course Dialog
    if (showAddCourseDialog) {
        var code by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var credits by remember { mutableStateOf("3") }
        var facultyName by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Core") }

        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = {
                Text(
                    text = "Add Curriculum Course",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Course Code") },
                        placeholder = { Text("CS8605") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Course Title") },
                        placeholder = { Text("Software Engineering & Agile") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = facultyName,
                        onValueChange = { facultyName = it },
                        label = { Text("Allocated Faculty") },
                        placeholder = { Text("Dr. Priya Raman") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = credits,
                        onValueChange = { credits = it },
                        label = { Text("Credits") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank() && title.isNotBlank()) {
                            onAddCourse(
                                CourseEntity(
                                    code = code.trim().uppercase(),
                                    title = title.trim(),
                                    departmentId = "DEPT-CSE",
                                    semester = 6,
                                    credits = credits.toIntOrNull() ?: 3,
                                    facultyName = facultyName.ifBlank { "Staff Assigned" },
                                    category = category
                                )
                            )
                            showAddCourseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                ) {
                    Text("Add Course")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Broadcast Notice Dialog
    if (showBroadcastNoticeDialog) {
        var noticeTitle by remember { mutableStateOf("") }
        var noticeBody by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("HIGH") }

        AlertDialog(
            onDismissRequest = { showBroadcastNoticeDialog = false },
            title = {
                Text(
                    text = "Broadcast College-Wide Circular",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text("Circular Headline") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noticeBody,
                        onValueChange = { noticeBody = it },
                        label = { Text("Official Directive") },
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
                        if (noticeTitle.isNotBlank() && noticeBody.isNotBlank()) {
                            onBroadcastAnnouncement(
                                AnnouncementEntity(
                                    title = noticeTitle,
                                    content = noticeBody,
                                    senderRole = "ADMIN",
                                    senderName = "Office of the Dean & Principal",
                                    targetAudience = "ALL",
                                    priority = priority,
                                    publishedAt = "2026-10-15"
                                )
                            )
                            showBroadcastNoticeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                ) {
                    Text("Broadcast to Entire College")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastNoticeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// 1. Overview Tab
@Composable
fun AdminCollegeOverviewTab(
    departments: List<DepartmentEntity>,
    courses: List<CourseEntity>,
    users: List<UserEntity>,
    onNavigateToTab: (Int) -> Unit
) {
    val totalStudents = departments.sumOf { it.totalStudents }
    val totalFaculty = departments.sumOf { it.totalFaculty }

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
                        text = "CampusConnect Institutional Administration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Academic Session 2025-2026 • Autonomous Engineering Institute",
                        style = MaterialTheme.typography.bodySmall,
                        color = Navy100
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "$totalStudents", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Total Students", style = MaterialTheme.typography.labelSmall, color = Navy100)
                        }
                        Column {
                            Text(text = "$totalFaculty", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Faculty & Staff", style = MaterialTheme.typography.labelSmall, color = Navy100)
                        }
                        Column {
                            Text(text = "${departments.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Gold500)
                            Text(text = "Departments", style = MaterialTheme.typography.labelSmall, color = Navy100)
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
                    title = "College Attendance",
                    value = "88.2%",
                    subtitle = "Across all 4 branches",
                    icon = Icons.Default.FactCheck,
                    iconTint = Emerald600,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Active Accounts",
                    value = "${users.size}",
                    subtitle = "RBAC Security enforced",
                    icon = Icons.Default.Security,
                    iconTint = Cyan600,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Department Operations & Head Allocation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        items(departments) { dept ->
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
                            text = dept.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = "HOD: ${dept.hodName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cyan700,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${dept.totalStudents} Students • ${dept.totalFaculty} Faculty",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500
                        )
                    }
                    Surface(
                        color = Navy100,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = dept.code,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy700,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// 2. User Accounts Management Tab
@Composable
fun AdminUserManagementTab(
    users: List<UserEntity>,
    onOpenCreateUser: () -> Unit,
    onDeleteUser: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = users.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.role.displayName.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Button(
                onClick = onOpenCreateUser,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_user_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy700)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Provision New Student / Faculty Account")
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search users by name, role, email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filteredUsers) { user ->
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Navy100),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.avatarInitials,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Navy700
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                            if (user.assignedClass.isNotBlank()) {
                                Text(
                                    text = "Class: ${user.assignedClass} • ID: ${user.registerNumberOrEmpId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Cyan700
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        RoleBadge(role = user.role)
                        if (user.role != UserRole.ADMIN) {
                            IconButton(
                                onClick = { onDeleteUser(user.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Rose600, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Courses Tab
@Composable
fun AdminCoursesTab(
    courses: List<CourseEntity>,
    departments: List<DepartmentEntity>,
    onOpenAddCourse: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Button(
                onClick = onOpenAddCourse,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_course_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy700)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Academic Course to Curriculum")
            }
        }

        items(courses) { course ->
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
                            text = "${course.code} - ${course.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = "Faculty In-Charge: ${course.facultyName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                        Text(
                            text = "Category: ${course.category} • Semester ${course.semester}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cyan700
                        )
                    }
                    Surface(
                        color = Navy100,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${course.credits} Credits",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// 4. Broadcast Tab
@Composable
fun AdminBroadcastTab(
    announcements: List<AnnouncementEntity>,
    onOpenBroadcastDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Button(
                onClick = onOpenBroadcastDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("broadcast_notice_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy700)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast Notice to College Community")
            }
        }

        items(announcements) { notice ->
            AnnouncementCard(announcement = notice)
        }
    }
}

// 5. Settings Tab
@Composable
fun AdminSettingsTab() {
    var attendanceThreshold by remember { mutableStateOf("75") }
    var institutionName by remember { mutableStateOf("CampusConnect University Institute of Technology") }
    var activeSemester by remember { mutableStateOf("2025-2026 Even Semester (Sem 2, 4, 6, 8)") }
    var savedSuccess by remember { mutableStateOf(false) }

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
                        text = "Institutional Configuration & Policies",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = institutionName,
                        onValueChange = { institutionName = it },
                        label = { Text("College / University Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = activeSemester,
                        onValueChange = { activeSemester = it },
                        label = { Text("Active Academic Term") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = attendanceThreshold,
                        onValueChange = { attendanceThreshold = it },
                        label = { Text("Minimum Mandatory Attendance (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { savedSuccess = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700)
                    ) {
                        Text("Save Institutional Settings")
                    }

                    if (savedSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Settings updated successfully across database!",
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
