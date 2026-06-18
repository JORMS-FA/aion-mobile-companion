package com.aion.mobile.data.model

enum class ConnectionMode { LAN, TAILSCALE }

data class Server(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val isActive: Boolean = false,
    val connectionMode: ConnectionMode = ConnectionMode.LAN
)
