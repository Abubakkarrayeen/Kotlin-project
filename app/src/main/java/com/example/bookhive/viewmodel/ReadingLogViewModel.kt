package com.example.bookhive.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookhive.model.ReadingLog
import com.example.bookhive.repository.ReadingLogRepository

class ReadingLogViewModel(private val repo: ReadingLogRepository) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _operationStatus = MutableLiveData<Pair<Boolean, String>>()
    val operationStatus: LiveData<Pair<Boolean, String>> get() = _operationStatus

    private val _logs = MutableLiveData<List<ReadingLog>>()
    val logs: LiveData<List<ReadingLog>> get() = _logs

    private val _totalPagesToday = MutableLiveData<Int>()
    val totalPagesToday: LiveData<Int> get() = _totalPagesToday

    private val _booksThisMonth = MutableLiveData<Int>()
    val booksThisMonth: LiveData<Int> get() = _booksThisMonth

    fun createReadingLog(log: ReadingLog) {
        _loading.postValue(true)
        repo.createReadingLog(log) { success, message ->
            _loading.postValue(false)
            _operationStatus.postValue(Pair(success, message))
            if (success) {
                getUserLogs()
                refreshStats()
            }
            Log.d("ReadingLogViewModel", "Log creation: $success - $message")
        }
    }

    fun getUserLogs() {
        _loading.postValue(true)
        repo.getAllLogsByCurrentUser { success, message, logs ->
            _loading.postValue(false)
            if (success && logs != null) {
                _logs.postValue(logs)
            } else {
                _logs.postValue(emptyList())
            }
            Log.d("ReadingLogViewModel", "User logs: $success - $message")
        }
    }

    fun updateReadingLog(logId: String, updatedLog: ReadingLog) {
        _loading.postValue(true)
        repo.updateReadingLog(logId, updatedLog) { success, message ->
            _loading.postValue(false)
            _operationStatus.postValue(Pair(success, message))
            if (success) {
                getUserLogs()
                refreshStats()
            }
            Log.d("ReadingLogViewModel", "Log update: $success - $message")
        }
    }

    fun deleteReadingLog(logId: String) {
        _loading.postValue(true)
        repo.deleteReadingLog(logId) { success, message ->
            _loading.postValue(false)
            _operationStatus.postValue(Pair(success, message))
            if (success) {
                getUserLogs()
                refreshStats()
            }
            Log.d("ReadingLogViewModel", "Log deletion: $success - $message")
        }
    }

    fun refreshStats() {
        repo.getTotalPagesReadToday { success, _, pages ->
            if (success) {
                _totalPagesToday.postValue(pages)
            }
        }

        repo.getTotalBooksReadThisMonth { success, _, count ->
            if (success) {
                _booksThisMonth.postValue(count)
            }
        }
    }

    fun clearStatus() {
        _operationStatus.postValue(Pair(false, ""))
    }

    override fun onCleared() {
        super.onCleared()
        repo.stopListening()
    }
}
