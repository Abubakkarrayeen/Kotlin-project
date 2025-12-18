package com.example.bookhive.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun BooksLibraryScreen(navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    // Fake data
    var books by remember {
        mutableStateOf(
            listOf(
                Book("1", "The Alchemist", "Paulo Coelho", "Fiction", 208),
                Book("2", "Atomic Habits", "James Clear", "Self-Help", 320),
                Book("3", "1984", "George Orwell", "Dystopian", 328)
            )
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Book", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize().background(LightBackground).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books.size) { index ->
                BookCard(
                    book = books[index],
                    onEdit = {
                        selectedBook = books[index]
                        showEditDialog = true
                    },
                    onDelete = {
                        books = books.filter { it.id != books[index].id }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddBookDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, author, genre, pages ->
                books = books + Book((books.size + 1).toString(), title, author, genre, pages.toIntOrNull() ?: 0)
                showAddDialog = false
            }
        )
    }

    if (showEditDialog && selectedBook != null) {
        EditBookDialog(
            book = selectedBook!!,
            onDismiss = { showEditDialog = false },
            onSave = { title, author, genre, pages ->
                books = books.map {
                    if (it.id == selectedBook!!.id) {
                        it.copy(title = title, author = author, genre = genre, totalPages = pages.toIntOrNull() ?: 0)
                    } else it
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun BookCard(book: Book, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WhiteBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(book.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(book.author, color = Color.Gray, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip(text = book.genre)
                        Chip(text = "${book.totalPages} pages")
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.Gray)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        color = PrimaryBlue.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, color = PrimaryBlue)
    }
}

@Composable
fun AddBookDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") })
                OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Genre") })
                OutlinedTextField(value = pages, onValueChange = { pages = it }, label = { Text("Total Pages") })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, author, genre, pages) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditBookDialog(book: Book, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var genre by remember { mutableStateOf(book.genre) }
    var pages by remember { mutableStateOf(book.totalPages.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") })
                OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Genre") })
                OutlinedTextField(value = pages, onValueChange = { pages = it }, label = { Text("Total Pages") })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, author, genre, pages) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("Update")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val genre: String,
    val totalPages: Int
)
