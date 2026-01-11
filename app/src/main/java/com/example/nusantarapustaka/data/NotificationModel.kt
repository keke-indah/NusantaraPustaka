package com.example.nusantarapustaka.data

data class NotificationModel(
    val id: String = "",
    val judul: String = "",
    val pesan: String = "",
    val waktu: Long = 0,
    val type: String = ""
)