package com.aion.mobile.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException

data class DiscoveredServer(
    val ip: String,
    val port: Int = 25808,
    val name: String = "AionUI"
)

object AionUIDiscovery {

    private const val AIONUI_PORT = 25808
    private const val TIMEOUT_MS = 500

    /**
     * Obtiene la IP local del dispositivo en la red WiFi
     */
    fun getLocalIpAddress(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiInfo: WifiInfo? = wifiManager?.connectionInfo
        val ipInt = wifiInfo?.ipAddress ?: return null
        return String.format(
            "%d.%d.%d.%d",
            ipInt and 0xff,
            ipInt shr 8 and 0xff,
            ipInt shr 16 and 0xff,
            ipInt shr 24 and 0xff
        )
    }

    /**
     * Obtiene la puerta de enlace (gateway) de la red local
     */
    fun getGateway(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        val dhcpInfo = wifiManager.dhcpInfo ?: return null
        val gateway = dhcpInfo.gateway
        return String.format(
            "%d.%d.%d.%d",
            gateway and 0xff,
            gateway shr 8 and 0xff,
            gateway shr 16 and 0xff,
            gateway shr 24 and 0xff
        )
    }

    /**
     * Escanea la red local para encontrar servidores AionUI
     * Escanea el rango 192.168.x.1-254 y verifica si el puerto 25808 responde
     */
    suspend fun scanNetwork(context: Context): List<DiscoveredServer> = withContext(Dispatchers.IO) {
        val servers = mutableListOf<DiscoveredServer>()
        val localIp = getLocalIpAddress(context) ?: return@withContext servers

        val parts = localIp.split(".")
        if (parts.size != 4) return@withContext servers

        val subnet = "${parts[0]}.${parts[1]}.${parts[2]}."
        val localLastOctet = parts[3].toIntOrNull() ?: return@withContext servers

        // Crear una lista de IPs a escanear (priorizar las cercanas)
        val ipsToScan = mutableListOf<String>()
        // Gateway primero
        val gateway = getGateway(context)
        if (gateway != null && gateway != localIp) ipsToScan.add(gateway)

        // El rango completo
        for (i in 1..254) {
            val ip = "$subnet$i"
            if (ip != localIp && ip != gateway) {
                ipsToScan.add(ip)
            }
        }

        // Escanear en paralelo (lotes de 20)
        ipsToScan.chunked(20).forEach { batch ->
            batch.map { ip ->
                async {
                    try {
                        val url = URL("http://$ip:$AIONUI_PORT/health")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.connectTimeout = TIMEOUT_MS
                        connection.readTimeout = TIMEOUT_MS
                        connection.requestMethod = "GET"
                        connection.doInput = true

                        val responseCode = connection.responseCode
                        connection.disconnect()

                        if (responseCode == 200) {
                            DiscoveredServer(ip = ip, port = AIONUI_PORT, name = "AionUI ($ip)")
                        } else null
                    } catch (_: Exception) {
                        null
                    }
                }
            }.forEach { deferred ->
                try {
                    val result = deferred.await()
                    if (result != null) {
                        servers.add(result)
                    }
                } catch (_: CancellationException) {
                    throw CancellationException()
                } catch (_: Exception) {
                    // Ignorar errores individuales
                }
            }
        }

        servers
    }

    /**
     * Verifica si una IP específica tiene un servidor AionUI corriendo
     */
    suspend fun checkServer(ip: String, port: Int = AIONUI_PORT): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$ip:$port/health")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 2000
            connection.readTimeout = 2000
            connection.requestMethod = "GET"
            val code = connection.responseCode
            connection.disconnect()
            code == 200
        } catch (_: Exception) {
            false
        }
    }
}