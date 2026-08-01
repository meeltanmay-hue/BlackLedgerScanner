package com.blackledger.scanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.blackledger.scanner.capture.CaptureService
import com.blackledger.scanner.data.AppDatabase
import com.blackledger.scanner.ocr.TextRecognizer
import com.blackledger.scanner.parser.AllianceParser
import com.blackledger.scanner.repository.DataRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screenCaptureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val intent = Intent(this, CaptureService::class.java).apply {
                    action = CaptureService.ACTION_START
                    putExtra(CaptureService.EXTRA_RESULT_DATA, data)
                }
                startService(intent)
            }
        }

        setContent {
            MaterialTheme {
                MainScreen(
                    onCaptureClick = {
                        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(onCaptureClick: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repository = remember { DataRepository(db.allianceDao()) }
    val scope = rememberCoroutineScope()

    var alliances by remember { mutableStateOf(emptyList<com.blackledger.scanner.data.AllianceEntity>()) }
    var allianceCount by remember { mutableStateOf(0) }
    var playerCount by remember { mutableStateOf(0) }
    var captureResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            allianceCount = repository.getAllianceCount()
            playerCount = repository.getPlayerCount()
            repository.getAllAlliances().collect { list ->
                alliances = list
            }
        }
    }

    DisposableEffect(Unit) {
        CaptureService.onImageCaptured = { bitmap ->
            if (bitmap != null) {
                scope.launch {
                    try {
                        val recognizer = TextRecognizer()
                        val text = recognizer.recognizeText(bitmap)
                        val parser = AllianceParser()
                        val parsed = parser.parse(text)
                        if (parsed != null && parsed.members.isNotEmpty()) {
                            val success = repository.saveAllianceWithPlayers(
                                parsed.kingdomId,
                                parsed.allianceTag,
                                parsed.allianceName,
                                parsed.members
                            )
                            captureResult = if (success) "Saved ${parsed.allianceTag}" else "Duplicate or error"
                            allianceCount = repository.getAllianceCount()
                            playerCount = repository.getPlayerCount()
                        } else {
                            captureResult = "No alliance data found"
                        }
                    } catch (e: Exception) {
                        captureResult = "OCR error: ${e.message}"
                    }
                }
            }
        }
        onDispose { CaptureService.onImageCaptured = null }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard("Alliances", allianceCount.toString())
            StatCard("Players", playerCount.toString())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCaptureClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Capture Screen")
        }

        captureResult?.let { result ->
            Text(
                text = result,
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Saved Alliances", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(alliances) { alliance ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Tag: ${alliance.allianceTag}")
                        Text("Kingdom: ${alliance.kingdomId}")
                        Text("Members: ${alliance.memberCount}")
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.width(100.dp).padding(4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
