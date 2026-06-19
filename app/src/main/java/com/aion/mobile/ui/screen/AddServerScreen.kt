package com.aion.mobile.ui.screen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aion.mobile.QRScannerActivity
import com.aion.mobile.data.model.Server
import com.aion.mobile.data.prefs.AppPreferences
import com.aion.mobile.network.AionUIDiscovery
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(
    appPreferences: AppPreferences,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Boolean?>(null) }
    var testMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedUrl = result.data?.getStringExtra("SCAN_RESULT")?.trim()
            if (!scannedUrl.isNullOrBlank()) {
                url = scannedUrl
                urlError = false
                testResult = null
                testMessage = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar servidor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Nombre") },
                placeholder = { Text("Ej: Mi servidor") },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("El nombre es requerido") }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = url,
                onValueChange = {
                    url = it
                    urlError = false
                    testResult = null
                    testMessage = null
                },
                label = { Text("URL del servidor") },
                placeholder = { Text("https://aionui.ejemplo.com o 192.168.1.100:25808") },
                isError = urlError,
                supportingText = if (urlError) {
                    { Text("Ingresa una URL válida") }
                } else if (testResult == true) {
                    { Text("Conexión exitosa", color = MaterialTheme.colorScheme.primary) }
                } else if (testResult == false && testMessage != null) {
                    { Text(testMessage!!, color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = {
                    val intent = android.content.Intent(context, QRScannerActivity::class.java)
                    qrScannerLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear QR")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    if (url.isBlank()) {
                        urlError = true
                        return@TextButton
                    }
                    isTesting = true
                    testResult = null
                    testMessage = null
                    scope.launch {
                        val testUrl = url.trim()
                            .removePrefix("http://").removePrefix("https://")
                            .split(":").first()
                        val port = url.trim()
                            .removePrefix("http://").removePrefix("https://")
                            .split(":").getOrNull(1)?.toIntOrNull() ?: 25808
                        val works = AionUIDiscovery.checkServer(testUrl, port)
                        testResult = works
                        testMessage = if (works) {
                            "Conexión exitosa"
                        } else {
                            "No se pudo conectar. Verifica la URL y que el servidor esté encendido."
                        }
                        isTesting = false
                    }
                },
                enabled = !isTesting && url.isNotBlank()
            ) {
                if (isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text("Probar conexión")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    nameError = name.isBlank()
                    urlError = url.isBlank()
                    if (!nameError && !urlError) {
                        scope.launch {
                            val serverUrl = if (url.trim().startsWith("http://") || url.trim().startsWith("https://")) {
                                url.trim()
                            } else {
                                "http://${url.trim()}"
                            }
                            val server = Server(name = name.trim(), url = serverUrl)
                            appPreferences.addServer(server)
                            appPreferences.setActiveServer(server.id)
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isTesting
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Guardar servidor")
            }
        }
    }
}
