package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.advisor.AdvisorDashboardScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.components.RoleBadge
import com.example.ui.hod.HodDashboardScreen
import com.example.ui.student.StudentDashboardScreen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusApp(
    viewModel: CampusViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showRoleSwitchDialog by remember { mutableStateOf(false) }

    // Collect module data streams
    val users by viewModel.users.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val timetableSlots by viewModel.timetableSlots.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val allLeaves by viewModel.allLeaves.collectAsStateWithLifecycle()
    val allComplaints by viewModel.allComplaints.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()

    val studentProfile by viewModel.currentStudentProfile.collectAsStateWithLifecycle()
    val studentExamMarks by viewModel.studentExamMarks.collectAsStateWithLifecycle()
    val studentSubmissions by viewModel.studentSubmissions.collectAsStateWithLifecycle()
    val studentLeaves by viewModel.studentLeaves.collectAsStateWithLifecycle()
    val studentComplaints by viewModel.studentComplaints.collectAsStateWithLifecycle()

    // Handle back button
    BackHandler(enabled = currentUser != null) {
        // If logged in, prompt role switcher or logout
        showRoleSwitchDialog = true
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (currentUser == null) {
            LoginScreen(
                onLoginSuccess = { email, pass ->
                    viewModel.login(email, pass)
                },
                onQuickRoleSelect = { role ->
                    viewModel.loginWithRole(role)
                }
            )
        } else {
            val user = currentUser!!
            Crossfade(targetState = user.role, label = "role_navigation") { role ->
                when (role) {
                    UserRole.STUDENT -> {
                        StudentDashboardScreen(
                            currentUser = user,
                            studentProfile = studentProfile,
                            courses = courses,
                            examMarks = studentExamMarks,
                            timetableSlots = timetableSlots,
                            assignments = assignments,
                            submissions = studentSubmissions,
                            leaves = studentLeaves,
                            complaints = studentComplaints,
                            announcements = announcements,
                            onApplyLeave = { viewModel.applyLeave(it) },
                            onSubmitComplaint = { viewModel.submitComplaint(it) },
                            onSubmitAssignment = { viewModel.submitAssignment(it) },
                            onSwitchRole = { showRoleSwitchDialog = true },
                            onLogout = { viewModel.logout() }
                        )
                    }
                    UserRole.ADVISOR -> {
                        AdvisorDashboardScreen(
                            currentUser = user,
                            assignedSection = user.assignedClass.ifBlank { "CSE-3A" },
                            students = allStudents.filter { it.section == (user.assignedClass.ifBlank { "CSE-3A" }) },
                            courses = courses,
                            timetableSlots = timetableSlots,
                            leaves = allLeaves.filter { it.section == (user.assignedClass.ifBlank { "CSE-3A" }) },
                            announcements = announcements,
                            onSaveAttendanceBatch = { viewModel.saveAttendanceBatch(it) },
                            onSaveExamMark = { viewModel.saveExamMark(it) },
                            onUpdateLeaveStatus = { leave, approved, remarks ->
                                viewModel.updateLeaveStatus(leave, approved, remarks)
                            },
                            onPostAnnouncement = { viewModel.postAnnouncement(it) },
                            onSwitchRole = { showRoleSwitchDialog = true },
                            onLogout = { viewModel.logout() }
                        )
                    }
                    UserRole.HOD -> {
                        HodDashboardScreen(
                            currentUser = user,
                            department = departments.firstOrNull { it.id == "DEPT-CSE" },
                            students = allStudents,
                            courses = courses,
                            leaves = allLeaves,
                            announcements = announcements,
                            onApproveLeave = { leave, approved ->
                                viewModel.updateHodLeaveStatus(leave, approved)
                            },
                            onPublishAnnouncement = { viewModel.postAnnouncement(it) },
                            onSwitchRole = { showRoleSwitchDialog = true },
                            onLogout = { viewModel.logout() }
                        )
                    }
                    UserRole.ADMIN -> {
                        AdminDashboardScreen(
                            currentUser = user,
                            users = users,
                            departments = departments,
                            courses = courses,
                            announcements = announcements,
                            onCreateUser = { viewModel.createUser(it) },
                            onDeleteUser = { viewModel.deleteUser(it) },
                            onAddCourse = { viewModel.addCourse(it) },
                            onBroadcastAnnouncement = { viewModel.postAnnouncement(it) },
                            onSwitchRole = { showRoleSwitchDialog = true },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }
    }

    // Role Switcher Modal Dialog
    if (showRoleSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showRoleSwitchDialog = false },
            title = {
                Text(
                    text = "Switch User Account & Role",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Instant Portal Role Switcher for Evaluation:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    RoleSwitchOption(
                        role = UserRole.STUDENT,
                        name = "Saranya",
                        detail = "Reg: 711025205043 • 3rd Year CSE-A",
                        isSelected = currentUser?.role == UserRole.STUDENT,
                        onClick = {
                            viewModel.loginWithRole(UserRole.STUDENT)
                            showRoleSwitchDialog = false
                        }
                    )

                    RoleSwitchOption(
                        role = UserRole.ADVISOR,
                        name = "Dr. Priya Raman",
                        detail = "Class Advisor • 3rd Year CSE-A",
                        isSelected = currentUser?.role == UserRole.ADVISOR,
                        onClick = {
                            viewModel.loginWithRole(UserRole.ADVISOR)
                            showRoleSwitchDialog = false
                        }
                    )

                    RoleSwitchOption(
                        role = UserRole.HOD,
                        name = "Dr. Rajesh Natarajan",
                        detail = "Head of Department • CSE",
                        isSelected = currentUser?.role == UserRole.HOD,
                        onClick = {
                            viewModel.loginWithRole(UserRole.HOD)
                            showRoleSwitchDialog = false
                        }
                    )

                    RoleSwitchOption(
                        role = UserRole.ADMIN,
                        name = "Dr. K. S. Sundaram",
                        detail = "Chief Dean & College Administrator",
                        isSelected = currentUser?.role == UserRole.ADMIN,
                        onClick = {
                            viewModel.loginWithRole(UserRole.ADMIN)
                            showRoleSwitchDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleSwitchDialog = false }) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showRoleSwitchDialog = false
                    }
                ) {
                    Text("Sign Out", color = Rose600)
                }
            }
        )
    }
}

@Composable
fun RoleSwitchOption(
    role: UserRole,
    name: String,
    detail: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("switch_to_${role.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Navy100 else Slate100
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Navy700 else Slate300),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (role) {
                        UserRole.STUDENT -> Icons.Default.School
                        UserRole.ADVISOR -> Icons.Default.SupervisorAccount
                        UserRole.HOD -> Icons.Default.AccountBalance
                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                    },
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Slate700,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    RoleBadge(role = role)
                }
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = Navy700,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
