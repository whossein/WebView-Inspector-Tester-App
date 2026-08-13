package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PostMessageDirection
import com.example.data.PostMessageLog
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostMessageSheet(
    sheetState: SheetState,
    postMessageLogs: List<PostMessageLog>,
    onSendPostMessage: (payload: String, origin: String) -> Unit,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var payloadInput by remember { mutableStateOf("{\n  \"type\": \"PING\",\n  \"data\": \"Hello from Android\"\n}") }
    var originInput by remember { mutableStateOf("*") }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Received, 2: Sent
    var showJsHelpDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(postMessageLogs, selectedFilterTab) {
        when (selectedFilterTab) {
            1 -> postMessageLogs.filter { it.direction == PostMessageDirection.RECEIVED }
            2 -> postMessageLogs.filter { it.direction == PostMessageDirection.SENT }
            else -> postMessageLogs
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "postMessage Inspector",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Test postMessage send & receive with WebView JS",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = { showJsHelpDialog = !showJsHelpDialog }) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "JS Code Guide",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (postMessageLogs.isNotEmpty()) {
                        IconButton(onClick = onClearLogs) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear logs",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // JS Quick Help Banner
            if (showJsHelpDialog) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 How to receive or send from Web Page:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "// Listen to messages from Android:\nwindow.addEventListener('message', e => console.log('Received:', e.data));\n\n// Send message to Android:\nwindow.postMessage({ type: 'TEST' }, '*');\n// or: window.postMessageToAndroid({ type: 'TEST' });",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val jsSnippet = "window.addEventListener('message', e => console.log('Received:', e.data));"
                                copyToClipboard(context, "JS Snippet", jsSnippet)
                                Toast.makeText(context, "JS Receiver snippet copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Receiver Snippet", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Send Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Send Message to WebView",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target Origin: $originInput",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = payloadInput,
                        onValueChange = { payloadInput = it },
                        label = { Text("Message Payload (JSON / Text)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset Quick Action Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Presets:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                        FilterChip(
                            selected = false,
                            onClick = { payloadInput = "{\n  \"type\": \"PING\",\n  \"time\": ${System.currentTimeMillis()}\n}" },
                            label = { Text("PING", fontSize = 11.sp) }
                        )

                        FilterChip(
                            selected = false,
                            onClick = { payloadInput = "{\n  \"action\": \"SET_THEME\",\n  \"theme\": \"dark\"\n}" },
                            label = { Text("SET_THEME", fontSize = 11.sp) }
                        )

                        FilterChip(
                            selected = false,
                            onClick = { payloadInput = "{\n  \"event\": \"USER_AUTH\",\n  \"token\": \"mock_jwt_123456\"\n}" },
                            label = { Text("USER_AUTH", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (payloadInput.isNotBlank()) {
                                onSendPostMessage(payloadInput, originInput)
                                Toast.makeText(context, "postMessage dispatched to WebView!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send postMessage", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-tabs for Logs Filter
            TabRow(
                selectedTabIndex = selectedFilterTab,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = selectedFilterTab == 0,
                    onClick = { selectedFilterTab = 0 },
                    text = { Text("All (${postMessageLogs.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedFilterTab == 1,
                    onClick = { selectedFilterTab = 1 },
                    text = {
                        val rxCount = postMessageLogs.count { it.direction == PostMessageDirection.RECEIVED }
                        Text("📥 Received ($rxCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                )
                Tab(
                    selected = selectedFilterTab == 2,
                    onClick = { selectedFilterTab = 2 },
                    text = {
                        val txCount = postMessageLogs.count { it.direction == PostMessageDirection.SENT }
                        Text("📤 Sent ($txCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Logs List
            if (filteredLogs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (postMessageLogs.isEmpty()) "No postMessages exchanged yet." else "No messages match the current tab filter.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredLogs.reversed(), key = { it.id }) { log ->
                        PostMessageLogCard(
                            log = log,
                            onCopyPayload = {
                                copyToClipboard(context, "postMessage payload", log.payload)
                                Toast.makeText(context, "Payload copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostMessageLogCard(
    log: PostMessageLog,
    onCopyPayload: () -> Unit
) {
    val isReceived = log.direction == PostMessageDirection.RECEIVED
    val badgeColor = if (isReceived) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
    val badgeTextColor = if (isReceived) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
    val directionLabel = if (isReceived) "📥 RECEIVED" else "📤 SENT"

    val timeFormatted = remember(log.timestamp) {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
    }

    val prettyPayload = remember(log.payload) {
        try {
            if (log.payload.trim().startsWith("{")) {
                JSONObject(log.payload).toString(2)
            } else if (log.payload.trim().startsWith("[")) {
                org.json.JSONArray(log.payload).toString(2)
            } else {
                log.payload
            }
        } catch (_: Exception) {
            log.payload
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = directionLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Origin: ${log.origin}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onCopyPayload,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = prettyPayload,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
