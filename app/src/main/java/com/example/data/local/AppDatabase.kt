package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        StudentEntity::class,
        DepartmentEntity::class,
        CourseEntity::class,
        AttendanceEntity::class,
        ExamMarkEntity::class,
        TimetableSlotEntity::class,
        AssignmentEntity::class,
        AssignmentSubmissionEntity::class,
        LeaveApplicationEntity::class,
        ComplaintEntity::class,
        AnnouncementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun courseDao(): CourseDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun examMarkDao(): ExamMarkDao
    abstract fun timetableDao(): TimetableDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun leaveDao(): LeaveDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun announcementDao(): AnnouncementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_connect.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
