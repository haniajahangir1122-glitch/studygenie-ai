package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {

    // --- Users ---
    suspend fun insertUser(user: UserEntity) = studyDao.insertUser(user)
    suspend fun getUserByEmail(email: String): UserEntity? = studyDao.getUserByEmail(email)
    suspend fun updateUser(user: UserEntity) = studyDao.updateUser(user)
    fun getAllStudents(): Flow<List<UserEntity>> = studyDao.getAllStudents()

    // --- Notes ---
    suspend fun insertNote(note: NoteEntity) = studyDao.insertNote(note)
    fun getNotesForUser(userId: String): Flow<List<NoteEntity>> = studyDao.getNotesForUser(userId)
    suspend fun getNoteById(id: String): NoteEntity? = studyDao.getNoteById(id)
    suspend fun deleteNoteById(id: String) = studyDao.deleteNoteById(id)

    // --- Files ---
    suspend fun insertFile(file: FileEntity) = studyDao.insertFile(file)
    fun getFilesForUser(userId: String): Flow<List<FileEntity>> = studyDao.getFilesForUser(userId)
    suspend fun deleteFileById(id: String) = studyDao.deleteFileById(id)
    suspend fun getFileById(id: String): FileEntity? = studyDao.getFileById(id)

    // --- Chats ---
    suspend fun insertChat(chat: ChatEntity) = studyDao.insertChat(chat)
    fun getChatsForUser(userId: String): Flow<List<ChatEntity>> = studyDao.getChatsForUser(userId)
    suspend fun clearChatsForUser(userId: String) = studyDao.clearChatsForUser(userId)

    // --- Quizzes ---
    suspend fun insertQuiz(quiz: QuizEntity) = studyDao.insertQuiz(quiz)
    fun getQuizzesForUser(userId: String): Flow<List<QuizEntity>> = studyDao.getQuizzesForUser(userId)

    // --- Progress ---
    suspend fun insertProgress(progress: ProgressEntity) = studyDao.insertProgress(progress)
    fun getProgressForUser(userId: String): Flow<List<ProgressEntity>> = studyDao.getProgressForUser(userId)

    // --- Flashcards ---
    suspend fun insertFlashcard(flashcard: FlashcardEntity) = studyDao.insertFlashcard(flashcard)
    fun getFlashcardsForUser(userId: String): Flow<List<FlashcardEntity>> = studyDao.getFlashcardsForUser(userId)
    suspend fun deleteFlashcardById(id: String) = studyDao.deleteFlashcardById(id)

    // --- Activity Logs ---
    suspend fun insertActivityLog(log: ActivityLogEntity) = studyDao.insertActivityLog(log)
    fun getActivityLogsForUser(userId: String): Flow<List<ActivityLogEntity>> = studyDao.getActivityLogsForUser(userId)

    // --- Reports ---
    suspend fun insertReport(report: ReportEntity) = studyDao.insertReport(report)
    fun getAllReports(): Flow<List<ReportEntity>> = studyDao.getAllReports()
    suspend fun updateReport(report: ReportEntity) = studyDao.updateReport(report)
}
