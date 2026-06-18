package com.aion.mobile.notification

import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val triggerAtMillis: Long,
    val isRecurring: Boolean = false,
    val recurringIntervalMinutes: Long = 0,
    val isEnabled: Boolean = true
)
