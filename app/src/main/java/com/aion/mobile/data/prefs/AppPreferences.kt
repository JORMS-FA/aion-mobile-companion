package com.aion.mobile.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aion.mobile.data.model.ConnectionMode
import com.aion.mobile.data.model.Server
import com.aion.mobile.notification.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "aion_preferences")

class AppPreferences(private val context: Context) {

    companion object {
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val SERVERS_JSON = stringPreferencesKey("servers_json")
        private val ACTIVE_SERVER_ID = stringPreferencesKey("active_server_id")
        private val REMINDERS_JSON = stringPreferencesKey("reminders_json")
        private val HAS_SEEN_WELCOME = booleanPreferencesKey("has_seen_welcome")
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE] ?: true
    }

    val servers: Flow<List<Server>> = context.dataStore.data.map { prefs ->
        val json = prefs[SERVERS_JSON] ?: "[]"
        parseServers(json)
    }

    val activeServerId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACTIVE_SERVER_ID]
    }

    val hasSeenWelcome: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_SEEN_WELCOME] ?: false
    }

    val reminders: Flow<List<Reminder>> = context.dataStore.data.map { prefs ->
        val json = prefs[REMINDERS_JSON] ?: "[]"
        parseReminders(json)
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }

    suspend fun saveServers(servers: List<Server>) {
        context.dataStore.edit { prefs ->
            prefs[SERVERS_JSON] = serversToJson(servers)
        }
    }

    suspend fun addServer(server: Server) {
        val current = servers.first().toMutableList()
        current.add(server)
        saveServers(current)
    }

    suspend fun updateServer(server: Server) {
        val current = servers.first().toMutableList()
        val index = current.indexOfFirst { it.id == server.id }
        if (index >= 0) {
            current[index] = server
            saveServers(current)
        }
    }

    suspend fun removeServer(serverId: String) {
        val current = servers.first().toMutableList()
        current.removeAll { it.id == serverId }
        saveServers(current)
    }

    suspend fun setActiveServer(serverId: String) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVE_SERVER_ID] = serverId
        }
        val current = servers.first().map {
            it.copy(isActive = it.id == serverId)
        }
        saveServers(current)
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACTIVE_SERVER_ID)
        }
    }

    suspend fun setHasSeenWelcome(seen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAS_SEEN_WELCOME] = seen
        }
    }

    suspend fun saveReminders(reminders: List<Reminder>) {
        context.dataStore.edit { prefs ->
            prefs[REMINDERS_JSON] = remindersToJson(reminders)
        }
    }

    suspend fun addReminder(reminder: Reminder) {
        val current = reminders.first().toMutableList()
        current.add(reminder)
        saveReminders(current)
    }

    suspend fun removeReminder(reminderId: String) {
        val current = reminders.first().toMutableList()
        current.removeAll { it.id == reminderId }
        saveReminders(current)
    }

    suspend fun updateReminder(reminder: Reminder) {
        val current = reminders.first().toMutableList()
        val index = current.indexOfFirst { it.id == reminder.id }
        if (index >= 0) {
            current[index] = reminder
            saveReminders(current)
        }
    }

    private fun parseServers(json: String): List<Server> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            Server(
                id = obj.getString("id"),
                name = obj.getString("name"),
                url = obj.getString("url"),
                isActive = obj.optBoolean("isActive", false),
                connectionMode = try {
                    ConnectionMode.valueOf(obj.optString("connectionMode", "LAN"))
                } catch (_: Exception) { ConnectionMode.LAN }
            )
        }
    }

    private fun serversToJson(servers: List<Server>): String {
        val arr = JSONArray()
        servers.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("url", s.url)
                put("isActive", s.isActive)
                put("connectionMode", s.connectionMode.name)
            })
        }
        return arr.toString()
    }

    private fun parseReminders(json: String): List<Reminder> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            Reminder(
                id = obj.getString("id"),
                title = obj.getString("title"),
                message = obj.getString("message"),
                triggerAtMillis = obj.getLong("triggerAtMillis"),
                isRecurring = obj.optBoolean("isRecurring", false),
                recurringIntervalMinutes = obj.optLong("recurringIntervalMinutes", 0),
                isEnabled = obj.optBoolean("isEnabled", true)
            )
        }
    }

    private fun remindersToJson(reminders: List<Reminder>): String {
        val arr = JSONArray()
        reminders.forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("message", r.message)
                put("triggerAtMillis", r.triggerAtMillis)
                put("isRecurring", r.isRecurring)
                put("recurringIntervalMinutes", r.recurringIntervalMinutes)
                put("isEnabled", r.isEnabled)
            })
        }
        return arr.toString()
    }
}
