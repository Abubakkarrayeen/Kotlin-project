package com.example.bookhive.model

data class UserModel(
    var userID:String = "",
    var userName:String = "",
    var email : String = "",
    var password : String = "",
    var photoUrl : String = "",
    var roles : Boolean = false,
)