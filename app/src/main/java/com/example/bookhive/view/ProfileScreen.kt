package com.example.bookhive.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookhive.repository.UserRepsitoryImpl
import com.example.bookhive.viewmodel.UserViewModel

@Composable
fun ProfileScreen() {
    val repo = remember { UserRepsitoryImpl() }
    val userViewModel = remember { UserViewModel(repo) }
    val context = LocalContext.current
    val activity = context as? Activity
    val sharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE)

    // Dialog states
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Get current user data
    val currentUser = userViewModel.getCurrentUser()
    var userName by remember { mutableStateOf("User") }
    var userEmail by remember { mutableStateOf("") }

    // Fetch user data from Firebase
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            userEmail = user.email ?: ""
            userViewModel.getUserById(user.uid)
        }
    }

    // Observe user data from ViewModel
    val userData by userViewModel.users.observeAsState()

    LaunchedEffect(userData) {
        userData?.let {
            userName = it.userName ?: "User"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Profile Picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(PrimaryBlue.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(userEmail, fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat("Books Read", "42")
            ProfileStat("Total Pages", "12,340")
            ProfileStat("Streak", "14 days")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Options
        ProfileOption(
            icon = Icons.Filled.Settings,
            text = "Edit Profile",
            onClick = { showSettingsDialog = true }
        )

        ProfileOption(
            icon = Icons.Filled.ExitToApp,
            text = "Logout",
            onClick = { showLogoutDialog = true },
            isDestructive = true
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        userViewModel.logout { success, message ->
                            if (success) {
                                // Clear SharedPreferences
                                sharedPreferences.edit().clear().apply()

                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                                // Navigate to Login
                                val intent = Intent(context, LoginView::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                activity?.finish()
                            } else {
                                Toast.makeText(context, "Logout failed: $message", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showSettingsDialog) {
        EditProfileDialog(
            currentUsername = userName,
            userId = currentUser?.uid ?: "",
            userViewModel = userViewModel,
            onDismiss = { showSettingsDialog = false },
            onSuccess = { newUsername ->
                userName = newUsername
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                showSettingsDialog = false
            }
        )
    }
}

@Composable
fun EditProfileDialog(
    currentUsername: String,
    userId: String,
    userViewModel: UserViewModel,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Update your username", fontSize = 14.sp, color = Color.Gray)

                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        cursorColor = PrimaryBlue
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newUsername.isBlank()) {
                        Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (newUsername == currentUsername) {
                        Toast.makeText(context, "No changes made", Toast.LENGTH_SHORT).show()
                        onDismiss()
                        return@Button
                    }

                    // Update profile in Firebase
                    val updates = mutableMapOf<String, Any>("username" to newUsername)

                    userViewModel.editProfile(userId, updates) { success, message ->
                        if (success) {
                            onSuccess(newUsername)
                        } else {
                            Toast.makeText(context, "Update failed: $message", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = WhiteBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive) Color.Red else PrimaryBlue
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) Color.Red else Color.Black
            )
        }
    }
}
