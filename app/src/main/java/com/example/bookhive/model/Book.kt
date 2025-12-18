package com.example.bookhive.model

data class Book(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val totalPages: Int = 0,
    val coverImageUrl: String = "",
    val addedTimestamp: Long = 0L
) {
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(addedTimestamp))
    }
}
