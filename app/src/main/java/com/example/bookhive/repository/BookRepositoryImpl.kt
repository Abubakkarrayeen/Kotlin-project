package com.example.bookhive.repository

import android.util.Log
import com.example.bookhive.model.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.UUID

class BookRepositoryImpl : BookRepository {
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.reference.child("books")
    private val auth = FirebaseAuth.getInstance()
    private var booksListener: ValueEventListener? = null

    override fun createBook(book: Book, callback: (Boolean, String) -> Unit) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                callback(false, "User not authenticated")
                return
            }

            val bookId = ref.push().key ?: UUID.randomUUID().toString()
            val bookData = book.copy(
                id = bookId,
                userId = currentUser.uid,
                addedTimestamp = System.currentTimeMillis()
            )

            ref.child(bookId).setValue(bookData)
                .addOnSuccessListener {
                    Log.d("BookRepository", "Book created successfully: $bookId")
                    callback(true, "Book added successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("BookRepository", "Failed to create book", exception)
                    callback(false, "Failed to add book: ${exception.message}")
                }

        } catch (e: Exception) {
            Log.e("BookRepository", "Error creating book", e)
            callback(false, "Error: ${e.message}")
        }
    }

    override fun getAllBooksByCurrentUser(callback: (Boolean, String, List<Book>?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "User not authenticated", null)
            return
        }

        ref.orderByChild("userId").equalTo(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val books = mutableListOf<Book>()
                        for (bookSnapshot in snapshot.children) {
                            val book = bookSnapshot.getValue(Book::class.java)
                            book?.let { books.add(it) }
                        }
                        val sortedBooks = books.sortedByDescending { it.addedTimestamp }
                        callback(true, "Books loaded successfully", sortedBooks)
                    } catch (e: Exception) {
                        callback(false, "Error parsing books: ${e.message}", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Database error: ${error.message}", null)
                }
            })
    }

    override fun getBookById(bookId: String, callback: (Boolean, String, Book?) -> Unit) {
        ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val book = snapshot.getValue(Book::class.java)
                if (book != null) {
                    callback(true, "Book loaded", book)
                } else {
                    callback(false, "Book not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", null)
            }
        })
    }

    override fun updateBook(bookId: String, updatedBook: Book, callback: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "User not authenticated")
            return
        }

        if (updatedBook.userId != currentUser.uid) {
            callback(false, "Unauthorized to edit this book")
            return
        }

        ref.child(bookId).setValue(updatedBook)
            .addOnSuccessListener {
                callback(true, "Book updated successfully")
            }
            .addOnFailureListener { exception ->
                callback(false, "Failed to update book: ${exception.message}")
            }
    }

    override fun deleteBook(bookId: String, callback: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "User not authenticated")
            return
        }

        ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val book = snapshot.getValue(Book::class.java)
                if (book != null && book.userId == currentUser.uid) {
                    ref.child(bookId).removeValue()
                        .addOnSuccessListener {
                            callback(true, "Book deleted successfully")
                        }
                        .addOnFailureListener { exception ->
                            callback(false, "Failed to delete book: ${exception.message}")
                        }
                } else {
                    callback(false, "Unauthorized to delete this book")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}")
            }
        })
    }

    override fun listenToUserBooks(callback: (List<Book>) -> Unit) {
        val currentUser = auth.currentUser ?: return

        booksListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val books = mutableListOf<Book>()
                    for (bookSnapshot in snapshot.children) {
                        val book = bookSnapshot.getValue(Book::class.java)
                        if (book?.userId == currentUser.uid) {
                            books.add(book)
                        }
                    }
                    callback(books.sortedByDescending { it.addedTimestamp })
                } catch (e: Exception) {
                    Log.e("BookRepository", "Error in real-time listener", e)
                    callback(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BookRepository", "Real-time listener cancelled: ${error.message}")
                callback(emptyList())
            }
        }

        ref.addValueEventListener(booksListener!!)
    }

    override fun stopListening() {
        booksListener?.let { ref.removeEventListener(it) }
    }
}
