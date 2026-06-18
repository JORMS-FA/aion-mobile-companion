package com.aion.mobile.data.model

data class Server(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val isActive: Boolean = false
)
