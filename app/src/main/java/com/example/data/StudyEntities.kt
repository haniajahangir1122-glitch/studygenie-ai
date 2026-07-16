package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val name: String,
    val password: String,
    val role: String, // "Student", "Teacher", "Admin", "Super Admin"
    val level: Int = 1,
    val xp: Int = 0,
    val isVerified: Boolean = false,
    val profilePicPath: String? = null,
    // Streak & Persistence tracking
    val streakCount: Int = 0,
    val lastActiveTimestamp: Long = 0L,
    // Daily Study Goals
    val dailyGoalQuizzes: Int = 2,
    val dailyGoalMinutes: Int = 15,
    val quizzesCompletedToday: Int = 0,
    // Privacy Controls
    val allowDataSharing: Boolean = true,
    val publicProfile: Boolean = false,
    val anonymizeAnalytics: Boolean = false
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val isAutoSaved: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val userId: String // email address of user
)

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "PDF", "DOCX", "TXT"
    val size: Long,
    val pathOrContent: String,
    val summary: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val questionText: String,
    val selectedAnswer: String,
    val correctAnswer: String = "",
    val explanation: String = "",
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val subject: String = "General"
)

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val id: String, // format: "courseId_userId"
    val courseId: String,
    val title: String,
    val progress: Float,
    val currentChapter: String,
    val timeRemaining: String,
    val userId: String
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val front: String,
    val back: String,
    val userId: String,
    val subject: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val itemId: String,
    val itemType: String, // "Note", "File", "User"
    val title: String,
    val reason: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val reportedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)
