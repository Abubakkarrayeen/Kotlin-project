package com.example.bookhive.repository

import android.util.Log
import com.example.bookhive.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File

val auth: FirebaseAuth = FirebaseAuth.getInstance()
val database: FirebaseDatabase = FirebaseDatabase.getInstance()
val ref: DatabaseReference = database.reference.child("users")
class UserRepsitoryImpl : UserRepository {
    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Registration success", "${auth.currentUser?.uid}")
                } else {
                    callback(false, "${it.exception?.message}", "")
                }
            }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "User successfully added")
            } else {
                callback(false, "${it.exception?.message}")

            }
        }
    }

// Add these methods to your UserRepositoryImpl class

    override fun reauthenticateUser(
        currentPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            val currentUser = auth.currentUser
            if (currentUser?.email == null) {
                callback(false, "User not authenticated")
                return
            }

            val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)

            currentUser.reauthenticate(credential)
                .addOnSuccessListener {
                    callback(true, "Authentication successful")
                }
                .addOnFailureListener { exception ->
                    callback(false, exception.message ?: "Current password is incorrect")
                }
        } catch (e: Exception) {
            callback(false, "Authentication error: ${e.message}")
        }
    }

    override fun changePassword(
        currentPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            // Validate new password length (8+ characters)
            if (newPassword.length < 8) {
                callback(false, "Password must be at least 8 characters")
                return
            }

            reauthenticateUser(currentPassword) { success, message ->
                if (success) {
                    // Update password
                    auth.currentUser?.updatePassword(newPassword)
                        ?.addOnSuccessListener {
                            callback(true, "Password changed successfully")
                        }
                        ?.addOnFailureListener { exception ->
                            callback(false, exception.message ?: "Failed to update password")
                        }
                } else {
                    callback(false, message)
                }
            }
        } catch (e: Exception) {
            callback(false, "Error: ${e.message}")
        }
    }

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { res ->
                if (res.isSuccessful) {
                    callback(true, "Login successfull")
                } else {
                    callback(false, "${res.exception?.message}")
                }

            }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Reset email sent to $email")
                } else {
                    callback(false, "${it.exception?.message}")

                }
            }
    }

    override fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            callback(false, "No user is currently signed in")
            return
        }
        // First delete from Firebase Auth
        currentUser.delete()
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // If auth deletion successful, then delete from database
                    ref.child(userId).removeValue()
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                callback(true, "Account deleted successfully")
                            } else {
                                // Auth was deleted but database deletion failed
                                callback(
                                    false,
                                    "Account deleted from auth but database cleanup failed: ${dbTask.exception?.message}"
                                )
                            }
                        }
                } else {
                    // Auth deletion failed
                    callback(false, "Failed to delete account: ${authTask.exception?.message}")
                }
            }
    }

    override fun editProfile(
        userId: String,
        data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        if (data.isEmpty()) {
            callback(false, "No changes to update")
            return
        }

        ref.child(userId).updateChildren(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Profile updated successfully")
            } else {
                callback(false, task.exception?.message ?: "Failed to update profile")
            }
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var users = snapshot.getValue(UserModel::class.java)
                    if (users != null) {
                        callback(true, "Fetched", users)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }

        })
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "logout successfully")
        } catch (e: Exception) {
            callback(false, e.message.toString())

        }
    }
}