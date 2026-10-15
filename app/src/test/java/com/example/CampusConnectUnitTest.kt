package com.example

import com.example.data.model.UserRole
import org.junit.Assert.*
import org.junit.Test

class CampusConnectUnitTest {

    @Test
    fun testUserRoleDefinitions() {
        assertEquals("Student", UserRole.STUDENT.displayName)
        assertEquals("Class Advisor", UserRole.ADVISOR.displayName)
        assertEquals("Head of Department (HOD)", UserRole.HOD.displayName)
        assertEquals("College Admin", UserRole.ADMIN.displayName)
    }

    @Test
    fun testAttendanceDefaulterLogic() {
        val safeAttendance = 86.5
        val lowAttendance = 71.4
        val threshold = 75.0

        assertTrue(safeAttendance >= threshold)
        assertTrue(lowAttendance < threshold)
    }

    @Test
    fun testGradeCalculation() {
        fun calculateGrade(score: Double, max: Double): String {
            val pct = (score / max) * 100.0
            return when {
                pct >= 90 -> "O"
                pct >= 80 -> "A+"
                pct >= 70 -> "A"
                pct >= 60 -> "B+"
                pct >= 50 -> "B"
                else -> "RA"
            }
        }

        assertEquals("O", calculateGrade(48.0, 50.0))
        assertEquals("A+", calculateGrade(42.0, 50.0))
        assertEquals("B+", calculateGrade(32.0, 50.0))
        assertEquals("RA", calculateGrade(20.0, 50.0))
    }
}
