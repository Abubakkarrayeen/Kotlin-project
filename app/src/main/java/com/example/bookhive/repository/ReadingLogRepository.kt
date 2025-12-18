package com.example.bookhive.repository

import com.example.bookhive.model.ReadingLog

interface ReadingLogRepository {
    // Create
    fun createReadingLog(
        log: ReadingLog,
        callback: (Boolean, String) -> Unit
    )

    // Read
    fun getAllLogsByCurrentUser(
        callback: (Boolean, String, List<ReadingLog>?) -> Unit
    )

    fun getLogById(
        logId: String,
        callback: (Boolean, String, ReadingLog?) -> Unit
    )

    // Update
    fun updateReadingLog(
        logId: String,
        updatedLog: ReadingLog,
        callback: (Boolean, String) -> Unit
    )

    // Delete
    fun deleteReadingLog(
        logId: String,
        callback: (Boolean, String) -> Unit
    )

    // Analytics
    fun getTotalPagesReadToday(
        callback: (Boolean, String, Int) -> Unit
    )

    fun getTotalBooksReadThisMonth(
        callback: (Boolean, String, Int) -> Unit
    )

    // Real-time listener
    fun listenToUserLogs(
        callback: (List<ReadingLog>) -> Unit
    )

    fun stopListening()
}
