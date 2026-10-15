package com.example.data.model

enum class UserRole(val displayName: String, val roleCode: String) {
    STUDENT("Student", "STUDENT"),
    ADVISOR("Class Advisor", "ADVISOR"),
    HOD("Head of Department (HOD)", "HOD"),
    ADMIN("College Admin", "ADMIN")
}
