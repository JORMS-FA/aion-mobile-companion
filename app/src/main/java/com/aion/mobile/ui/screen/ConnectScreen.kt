package com.aion.mobile.ui.screen

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aion.mobile.QRScannerActivity
import com.aion.mobile.data.model.ConnectionMode
import com.aion.mobile.data.model.Server
import com.aion.mobile.data.prefs.AppPreferences
import com.aion.mobile.network.AionUIDiscovery
import com.aion.mobile.network.DiscoveredServer
import kotlinx.coroutines.launch

@Composable
fun ConnectScreen(
    appPreferences: AppPreferences,
    onConnected: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var ipAddress by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }
    var discoveredServers by remember { mutableStateOf<List<DiscoveredServer>>(emptyList()) }
    var scanDone by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showManualEntry by remember { mutableStateOf(false) }
    var connectionMode by remember { mutableStateOf(ConnectionMode.LAN) }
    var showTailscaleHelp by remember { mutableStateOf(false) }

    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedUrl = result.data?.getStringExtra("SCAN_RESULT")?.trim()
            if (!scannedUrl.isNullOrBlank()) {
                isConnecting = true
                scope.launch {
                    val server = Server(
                        name = "Mi PC (QR)",
                        url = scannedUrl,
                        connectionMode = connectionMode
                    )
                    appPreferences.addServer(server)
                    appPreferences.setActiveServer(server.id)
                    onConnected()
                }
            } else {
                error = "No se pudo leer el código QR"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Conectar a AionUI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tu PC debe estar en la misma red WiFi\ncon AionUI WebUI corriendo (puerto 25808)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mode selector
        Text(
            text = "Modo de conexión",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = connectionMode == ConnectionMode.LAN,
                onClick = { connectionMode = ConnectionMode.LAN },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("LAN")
            }
            SegmentedButton(
                selected = connectionMode == ConnectionMode.TAILSCALE,
                onClick = { connectionMode = ConnectionMode.TAILSCALE },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tailscale")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (connectionMode == ConnectionMode.TAILSCALE) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ayuda Tailscale",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showTailscaleHelp = !showTailscaleHelp }) {
                            Icon(
                                if (showTailscaleHelp) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle"
                            )
                        }
                    }
                    AnimatedVisibility(visible = showTailscaleHelp) {
                        Column {
                            Text(
                                text = "Cómo conectar con Tailscale:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Instala Tailscale en tu PC y en este dispositivo\n" +
                                        "2. Inicia sesión con la misma cuenta en ambos\n" +
                                        "3. Busca la IP de Tailscale de tu PC (100.x.x.x)\n" +
                                        "   en la lista de máquinas de la app de Tailscale\n" +
                                        "4. Asegúrate que el puerto 25808 esté abierto\n" +
                                        "5. Ingresa la IP 100.x.x.x:25808 manualmente",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!scanDone && !showManualEntry) {
            Button(
                onClick = {
                    isScanning = true
                    error = null
                    scope.launch {
                        try {
                            val servers = AionUIDiscovery.scanNetwork(context)
                            discoveredServers = servers
                            scanDone = true
                            if (servers.isEmpty()) {
                                error = "No se encontró AionUI en la red"
                            }
                        } catch (e: Exception) {
                            error = "Error al escanear: ${e.message}"
                        } finally {
                            isScanning = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isScanning
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isScanning) "Buscando..." else "Buscar AionUI en la red",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (isScanning) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Escaneando red local...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = {
                    val intent = Intent(context, QRScannerActivity::class.java)
                    qrScannerLauncher.launch(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear QR", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { showManualEntry = true }) {
                Text("O ingresar IP manualmente")
            }
        }

        if (scanDone && !showManualEntry) {
            if (discoveredServers.isNotEmpty()) {
                Text(
                    text = "Servidores encontrados:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                discoveredServers.forEach { server ->
                    Card(
                        onClick = {
                            isConnecting = true
                            scope.launch {
                                val newServer = Server(
                                    name = "Mi PC",
                                    url = "http://${server.ip}:${server.port}",
                                    connectionMode = connectionMode
                                )
                                appPreferences.addServer(newServer)
                                appPreferences.setActiveServer(newServer.id)
                                onConnected()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AionUI",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${server.ip}:${server.port}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            if (isConnecting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { showManualEntry = true }) {
                    Text("Ingresar IP manualmente")
                }
                TextButton(onClick = {
                    scanDone = false
                    error = null
                    discoveredServers = emptyList()
                }) {
                    Text("Intentar de nuevo")
                }
            }
        }

        if (showManualEntry) {
            Text(
                text = "Ingresa la IP de tu PC",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (connectionMode == ConnectionMode.TAILSCALE) {
                    "Encuentra la IP de Tailscale de tu PC (100.x.x.x)"
                } else {
                    "En tu PC abre una terminal y escribe: ipconfig\nBusca la IPv4 en tu conexión WiFi"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP del PC") },
                placeholder = {
                    Text(
                        if (connectionMode == ConnectionMode.TAILSCALE) "100.x.x.x" else "192.168.1.100"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Link, contentDescription = null)
                }
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (ipAddress.isBlank()) {
                        error = "Ingresa una dirección IP"
                        return@Button
                    }
                    isConnecting = true
                    error = null
                    scope.launch {
                        val url = "http://${ipAddress.trim()}:25808"
                        val works = AionUIDiscovery.checkServer(ipAddress.trim())
                        if (works) {
                            val server = Server(
                                name = if (connectionMode == ConnectionMode.TAILSCALE) "Mi PC (Tailscale)" else "Mi PC",
                                url = url,
                                connectionMode = connectionMode
                            )
                            appPreferences.addServer(server)
                            appPreferences.setActiveServer(server.id)
                            onConnected()
                        } else {
                            error = "No se pudo conectar a $url\nVerifica que AionUI esté corriendo"
                            isConnecting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isConnecting
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Link, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conectar")
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                showManualEntry = false
                scanDone = false
                error = null
            }) {
                Text("Volver")
            }
        }
    }
}
