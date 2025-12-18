package com.example.bookhive.repository


import com.example.bookhive.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepository {
    //auth ko part
    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    )
    //database ko part for storing username and roles
    fun addUserToDatabase(
        userId: String, model: UserModel,
        callback: (Boolean, String) -> Unit
    )
    // Add these methods to your existing UserRepository interface

    fun reauthenticateUser(
        currentPassword: String,
        callback: (Boolean, String) -> Unit
    )

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    )

    //login part
    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    )
    fun forgetPassword(
        email: String, callback: (Boolean, String) -> Unit
    )
    fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    fun editProfile(
        userId: String, data: MutableMap<String, Any>,
        callback: (Boolean, String) -> Unit
    )

    fun getCurrentUser(): FirebaseUser?

    fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

    fun logout(callback: (Boolean, String) -> Unit)
}