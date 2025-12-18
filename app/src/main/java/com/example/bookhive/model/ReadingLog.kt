package com.example.bookhive.model

data class ReadingLog(
    val id: String = "",
    val userId: String = "",
    val bookTitle: String = "",
    val bookId: String = "", // Optional: Link to Book if exists
    val date: String = "",
    val pagesRead: Int = 0,
    val notes: String = "",
    val timestamp: Long = 0L
) {
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
}
