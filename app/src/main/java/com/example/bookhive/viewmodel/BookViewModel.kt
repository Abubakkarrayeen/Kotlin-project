package com.example.bookhive.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookhive.model.Book
import com.example.bookhive.repository.BookRepository

class BookViewModel(private val repo: BookRepository) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _operationStatus = MutableLiveData<Pair<Boolean, String>>()
    val operationStatus: LiveData<Pair<Boolean, String>> get() = _operationStatus

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> get() = _books

    fun createBook(book: Book) {
        _loading.postValue(true)
        repo.createBook(book) { success, message ->
            _loading.postValue(false)
            _operationStatus.postValue(Pair(success, message))
            if (success) {
                getUserBooks()
            }
            Log.d("BookViewModel", "Book creation: $success - $message")
        }
    }

    fun getUserBooks() {
        _loading.postValue(true)
        repo.getAllBooksByCurrentUser { success, message, books ->
            _loading.postValue(false)
            if (success && books != null) {
                _books.postValue(books)
            } else {
                _books.postValue(emptyList())
            }
            Log.d("BookViewModel", "User books: $success - $message")
        }
    }

    fun updateBook(bookId: String, updatedBook: Book) {
        _loading.postValue(true)
        repo.updateBook(bookId, updatedBook) { success, message ->
            _loading.postValue(false)
            _operationStatus.postValue(Pair(success, message))
            if (success) {
                getUserBooks()
            }
            Log.d("BookViewModel", "Book update: $success - $message")
        }
    }

    fun deleteBook(bookId: String) {
        _loading.postValue(true)
        repo.deleteBook(bookId) { success, message ->
            _loading.postValue(false)
            _operationStatus.postValue(Pair(success, message))
            if (success) {
                getUserBooks()
            }
            Log.d("BookViewModel", "Book deletion: $success - $message")
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
