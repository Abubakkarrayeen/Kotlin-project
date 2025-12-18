package com.example.bookhive.repository

import com.example.bookhive.model.Book

interface BookRepository {
    // Create
    fun createBook(
        book: Book,
        callback: (Boolean, String) -> Unit
    )

    // Read
    fun getAllBooksByCurrentUser(
        callback: (Boolean, String, List<Book>?) -> Unit
    )

    fun getBookById(
        bookId: String,
        callback: (Boolean, String, Book?) -> Unit
    )

    // Update
    fun updateBook(
        bookId: String,
        updatedBook: Book,
        callback: (Boolean, String) -> Unit
    )

    // Delete
    fun deleteBook(
        bookId: String,
        callback: (Boolean, String) -> Unit
    )

    // Real-time listener
    fun listenToUserBooks(
        callback: (List<Book>) -> Unit
    )

    fun stopListening()
}
