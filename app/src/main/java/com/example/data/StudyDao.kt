package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- Users ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE role = 'Student' ORDER BY level DESC, xp DESC")
    fun getAllStudents(): Flow<List<UserEntity>>

    // --- Notes ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY lastModified DESC")
    fun getNotesForUser(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    // --- Files ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)

    @Query("SELECT * FROM files WHERE userId = :userId ORDER BY timestamp DESC")
    fun getFilesForUser(userId: String): Flow<List<FileEntity>>

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteFileById(id: String)

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): FileEntity?

    // --- Chats ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("SELECT * FROM chats WHERE userId = :userId ORDER BY timestamp ASC")
    fun getChatsForUser(userId: String): Flow<List<ChatEntity>>

    @Query("DELETE FROM chats WHERE userId = :userId")
    suspend fun clearChatsForUser(userId: String)

    // --- Quizzes ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM quizzes WHERE userId = :userId ORDER BY timestamp DESC")
    fun getQuizzesForUser(userId: String): Flow<List<QuizEntity>>

    // --- Progress ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Query("SELECT * FROM progress WHERE userId = :userId")
    fun getProgressForUser(userId: String): Flow<List<ProgressEntity>>

    // --- Flashcards ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity)

    @Query("SELECT * FROM flashcards WHERE userId = :userId ORDER BY timestamp DESC")
    fun getFlashcardsForUser(userId: String): Flow<List<FlashcardEntity>>

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcardById(id: String)

    // --- Activity Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity)

    @Query("SELECT * FROM activity_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT 30")
    fun getActivityLogsForUser(userId: String): Flow<List<ActivityLogEntity>>

    // --- Reports / Admin Workflows ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Update
    suspend fun updateReport(report: ReportEntity)
}
