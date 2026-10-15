package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY role ASC, fullName ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE registerNumber = :regNo LIMIT 1")
    fun getStudentFlow(regNo: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE registerNumber = :regNo LIMIT 1")
    suspend fun getStudentByRegNo(regNo: String): StudentEntity?

    @Query("SELECT * FROM students WHERE section = :section ORDER BY registerNumber ASC")
    fun getStudentsBySectionFlow(section: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE departmentId = :deptId ORDER BY year ASC, section ASC, registerNumber ASC")
    fun getStudentsByDeptFlow(deptId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY registerNumber ASC")
    fun getAllStudentsFlow(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)
}

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments ORDER BY code ASC")
    fun getAllDepartmentsFlow(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE id = :deptId LIMIT 1")
    fun getDepartmentById(deptId: String): Flow<DepartmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartments(departments: List<DepartmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: DepartmentEntity)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY semester ASC, code ASC")
    fun getAllCoursesFlow(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE departmentId = :deptId ORDER BY semester ASC, code ASC")
    fun getCoursesByDeptFlow(deptId: String): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE studentRegisterNumber = :regNo ORDER BY date DESC")
    fun getStudentAttendanceFlow(regNo: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE section = :section AND date = :date")
    fun getSectionAttendanceByDateFlow(section: String, date: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(records: List<AttendanceEntity>)
}

@Dao
interface ExamMarkDao {
    @Query("SELECT * FROM exam_marks WHERE studentRegisterNumber = :regNo ORDER BY subjectCode ASC")
    fun getStudentMarksFlow(regNo: String): Flow<List<ExamMarkEntity>>

    @Query("SELECT * FROM exam_marks WHERE subjectCode = :subjectCode")
    fun getMarksBySubjectFlow(subjectCode: String): Flow<List<ExamMarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMark(mark: ExamMarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<ExamMarkEntity>)
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_slots WHERE section = :section ORDER BY period ASC")
    fun getTimetableForSectionFlow(section: String): Flow<List<TimetableSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlots(slots: List<TimetableSlotEntity>)

    @Update
    suspend fun updateTimetableSlot(slot: TimetableSlotEntity)
}

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignmentsFlow(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE targetSection = :section ORDER BY dueDate ASC")
    fun getAssignmentsForSectionFlow(section: String): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Query("SELECT * FROM assignment_submissions WHERE studentRegisterNumber = :regNo")
    fun getSubmissionsByStudentFlow(regNo: String): Flow<List<AssignmentSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: AssignmentSubmissionEntity)
}

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leave_applications WHERE studentRegisterNumber = :regNo ORDER BY appliedAt DESC")
    fun getLeavesForStudentFlow(regNo: String): Flow<List<LeaveApplicationEntity>>

    @Query("SELECT * FROM leave_applications WHERE section = :section ORDER BY appliedAt DESC")
    fun getLeavesForSectionFlow(section: String): Flow<List<LeaveApplicationEntity>>

    @Query("SELECT * FROM leave_applications ORDER BY appliedAt DESC")
    fun getAllLeavesFlow(): Flow<List<LeaveApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveApplicationEntity)

    @Update
    suspend fun updateLeave(leave: LeaveApplicationEntity)
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getComplaintsByUserFlow(userId: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints ORDER BY submittedAt DESC")
    fun getAllComplaintsFlow(): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY publishedAt DESC")
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)
}
