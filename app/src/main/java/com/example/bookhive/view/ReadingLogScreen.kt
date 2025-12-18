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
fun ReadingLogScreen(navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedLog by remember { mutableStateOf<ReadingLog?>(null) }

    // Fake data for now (will be replaced with Firebase)
    var readingLogs by remember {
        mutableStateOf(
            listOf(
                ReadingLog("1", "The Alchemist", "Dec 17, 2025", 23, "Inspiring chapter"),
                ReadingLog("2", "Atomic Habits", "Dec 16, 2025", 15, "Great insights"),
                ReadingLog("3", "1984", "Dec 15, 2025", 42, "Dystopian masterpiece")
            )
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reading Log", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(readingLogs.size) { index ->
                ReadingLogCard(
                    log = readingLogs[index],
                    onEdit = {
                        selectedLog = readingLogs[index]
                        showEditDialog = true
                    },
                    onDelete = {
                        readingLogs = readingLogs.filter { it.id != readingLogs[index].id }
                    }
                )
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddReadingLogDialog(
            onDismiss = { showAddDialog = false },
            onSave = { bookTitle, pages, notes ->
                val newLog = ReadingLog(
                    id = (readingLogs.size + 1).toString(),
                    bookTitle = bookTitle,
                    date = "Today",
                    pagesRead = pages.toIntOrNull() ?: 0,
                    notes = notes
                )
                readingLogs = readingLogs + newLog
                showAddDialog = false
            }
        )
    }

    // Edit Dialog
    if (showEditDialog && selectedLog != null) {
        EditReadingLogDialog(
            log = selectedLog!!,
            onDismiss = { showEditDialog = false },
            onSave = { bookTitle, pages, notes ->
                readingLogs = readingLogs.map {
                    if (it.id == selectedLog!!.id) {
                        it.copy(bookTitle = bookTitle, pagesRead = pages.toIntOrNull() ?: 0, notes = notes)
                    } else it
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun ReadingLogCard(log: ReadingLog, onEdit: () -> Unit, onDelete: () -> Unit) {
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
                    Text(log.bookTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${log.pagesRead} pages", color = PrimaryBlue, fontSize = 14.sp)
                    Text(log.date, color = Color.Gray, fontSize = 12.sp)
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
            if (log.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes: ${log.notes}", color = Color.DarkGray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AddReadingLogDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var bookTitle by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Reading Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = bookTitle, onValueChange = { bookTitle = it }, label = { Text("Book Title") })
                OutlinedTextField(value = pages, onValueChange = { pages = it }, label = { Text("Pages Read") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Optional)") }, maxLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(bookTitle, pages, notes) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditReadingLogDialog(log: ReadingLog, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var bookTitle by remember { mutableStateOf(log.bookTitle) }
    var pages by remember { mutableStateOf(log.pagesRead.toString()) }
    var notes by remember { mutableStateOf(log.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Reading Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = bookTitle, onValueChange = { bookTitle = it }, label = { Text("Book Title") })
                OutlinedTextField(value = pages, onValueChange = { pages = it }, label = { Text("Pages Read") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, maxLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(bookTitle, pages, notes) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Data Model
data class ReadingLog(
    val id: String,
    val bookTitle: String,
    val date: String,
    val pagesRead: Int,
    val notes: String
)
