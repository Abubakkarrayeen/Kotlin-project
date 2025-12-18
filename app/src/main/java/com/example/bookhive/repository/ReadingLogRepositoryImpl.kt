package com.example.bookhive.repository

import android.util.Log
import com.example.bookhive.model.ReadingLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ReadingLogRepositoryImpl : ReadingLogRepository {
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.reference.child("reading_logs")
    private val auth = FirebaseAuth.getInstance()
    private var logsListener: ValueEventListener? = null

    override fun createReadingLog(log: ReadingLog, callback: (Boolean, String) -> Unit) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                callback(false, "User not authenticated")
                return
            }

            val logId = ref.push().key ?: UUID.randomUUID().toString()
            val logData = log.copy(
                id = logId,
                userId = currentUser.uid,
                timestamp = System.currentTimeMillis()
            )

            ref.child(logId).setValue(logData)
                .addOnSuccessListener {
                    Log.d("ReadingLogRepository", "Log created successfully: $logId")
                    callback(true, "Reading log added successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("ReadingLogRepository", "Failed to create log", exception)
                    callback(false, "Failed to add log: ${exception.message}")
                }

        } catch (e: Exception) {
            Log.e("ReadingLogRepository", "Error creating log", e)
            callback(false, "Error: ${e.message}")
        }
    }

    override fun getAllLogsByCurrentUser(callback: (Boolean, String, List<ReadingLog>?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "User not authenticated", null)
            return
        }

        ref.orderByChild("userId").equalTo(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val logs = mutableListOf<ReadingLog>()
                        for (logSnapshot in snapshot.children) {
                            val log = logSnapshot.getValue(ReadingLog::class.java)
                            log?.let { logs.add(it) }
                        }
                        val sortedLogs = logs.sortedByDescending { it.timestamp }
                        callback(true, "Logs loaded successfully", sortedLogs)
                    } catch (e: Exception) {
                        callback(false, "Error parsing logs: ${e.message}", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Database error: ${error.message}", null)
                }
            })
    }

    override fun getLogById(logId: String, callback: (Boolean, String, ReadingLog?) -> Unit) {
        ref.child(logId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val log = snapshot.getValue(ReadingLog::class.java)
                if (log != null) {
                    callback(true, "Log loaded", log)
                } else {
                    callback(false, "Log not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", null)
            }
        })
    }

    override fun updateReadingLog(logId: String, updatedLog: ReadingLog, callback: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "User not authenticated")
            return
        }

        if (updatedLog.userId != currentUser.uid) {
            callback(false, "Unauthorized to edit this log")
            return
        }

        ref.child(logId).setValue(updatedLog)
            .addOnSuccessListener {
                callback(true, "Reading log updated successfully")
            }
            .addOnFailureListener { exception ->
                callback(false, "Failed to update log: ${exception.message}")
            }
    }

    override fun deleteReadingLog(logId: String, callback: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "User not authenticated")
            return
        }

        ref.child(logId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val log = snapshot.getValue(ReadingLog::class.java)
                if (log != null && log.userId == currentUser.uid) {
                    ref.child(logId).removeValue()
                        .addOnSuccessListener {
                            callback(true, "Reading log deleted successfully")
                        }
                        .addOnFailureListener { exception ->
                            callback(false, "Failed to delete log: ${exception.message}")
                        }
                } else {
                    callback(false, "Unauthorized to delete this log")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}")
            }
        })
    }

    override fun getTotalPagesReadToday(callback: (Boolean, String, Int) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "User not authenticated", 0)
            return
        }

        val today = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())

        ref.orderByChild("userId").equalTo(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalPages = 0
                    for (logSnapshot in snapshot.children) {
                        val log = logSnapshot.getValue(ReadingLog::class.java)
                        if (log?.date == today || log?.date == "Today") {
                            totalPages += log.pagesRead
                        }
                    }
                    callback(true, "Total pages calculated", totalPages)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Database error: ${error.message}", 0)
                }
            })
    }

    override fun getTotalBooksReadThisMonth(callback: (Boolean, String, Int) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "User not authenticated", 0)
            return
        }

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        ref.orderByChild("userId").equalTo(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val booksRead = mutableSetOf<String>()
                    for (logSnapshot in snapshot.children) {
                        val log = logSnapshot.getValue(ReadingLog::class.java)
                        log?.let {
                            val logDate = Date(it.timestamp)
                            val logCalendar = Calendar.getInstance()
                            logCalendar.time = logDate

                            if (logCalendar.get(Calendar.MONTH) == currentMonth &&
                                logCalendar.get(Calendar.YEAR) == currentYear) {
                                booksRead.add(it.bookTitle)
                            }
                        }
                    }
                    callback(true, "Books counted", booksRead.size)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Database error: ${error.message}", 0)
                }
            })
    }

    override fun listenToUserLogs(callback: (List<ReadingLog>) -> Unit) {
        val currentUser = auth.currentUser ?: return

        logsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val logs = mutableListOf<ReadingLog>()
                    for (logSnapshot in snapshot.children) {
                        val log = logSnapshot.getValue(ReadingLog::class.java)
                        if (log?.userId == currentUser.uid) {
                            logs.add(log)
                        }
                    }
                    callback(logs.sortedByDescending { it.timestamp })
                } catch (e: Exception) {
                    Log.e("ReadingLogRepository", "Error in real-time listener", e)
                    callback(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReadingLogRepository", "Real-time listener cancelled: ${error.message}")
                callback(emptyList())
            }
        }

        ref.addValueEventListener(logsListener!!)
    }

    override fun stopListening() {
        logsListener?.let { ref.removeEventListener(it) }
    }
}
