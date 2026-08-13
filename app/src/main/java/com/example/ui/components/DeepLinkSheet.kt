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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeepLinkLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepLinkSheet(
    sheetState: SheetState,
    deepLinkLogs: List<DeepLinkLog>,
    onSimulateDeepLink: (String) -> Unit,
    onLoadUrlInWebView: (String) -> Unit,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var testUrlInput by remember { mutableStateOf("pwainspector://profile?user_id=101&mode=debug&ref=social") }
    var showGuide by remember { mutableStateOf(true) }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Deep Link Tester & Inspector",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Inspect incoming intents & test custom schemes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = { showGuide = !showGuide }) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Guide",
                            tint = if (showGuide) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    if (deepLinkLogs.isNotEmpty()) {
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

            // Guide / Help Card (راهنما)
            if (showGuide) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📖 راهنمای تست دیپ لینک (Deep Link Guide):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1️⃣ scheme‌های پشتیبانی شده:\n   • pwainspector:// (مثال: pwainspector://home?lang=fa)\n   • https://pwainspector.app/ (مثال: https://pwainspector.app/product/12)\n\n2️⃣ تست با ADB در ترمینال:\n   adb shell am start -a android.intent.action.VIEW -d \"pwainspector://profile?id=10\"\n\n3️⃣ تمام دیپ‌لینک‌های ورودی به سیستم به همراه پارامترها و زمان لاگ می‌شوند.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Deep Link Simulator Card
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
                    Text(
                        text = "Simulate Deep Link Intent",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = testUrlInput,
                        onValueChange = { testUrlInput = it },
                        label = { Text("Deep Link URL (Custom Scheme or HTTP)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset Chips
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
                            onClick = { testUrlInput = "pwainspector://home?lang=fa&theme=dark" },
                            label = { Text("pwainspector://home", fontSize = 11.sp) }
                        )

                        FilterChip(
                            selected = false,
                            onClick = { testUrlInput = "pwainspector://auth/callback?code=xyz_token_99" },
                            label = { Text("Auth Callback", fontSize = 11.sp) }
                        )

                        FilterChip(
                            selected = false,
                            onClick = { testUrlInput = "https://pwainspector.app/product/42?promo=summer" },
                            label = { Text("App Link (HTTPS)", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (testUrlInput.isNotBlank()) {
                                    onSimulateDeepLink(testUrlInput)
                                    Toast.makeText(context, "Deep link simulated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate")
                        }

                        OutlinedButton(
                            onClick = {
                                val adbCmd = "adb shell am start -a android.intent.action.VIEW -d \"$testUrlInput\""
                                copyToClipboard(context, "ADB Command", adbCmd)
                                Toast.makeText(context, "ADB CLI command copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy ADB")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Log List Header
            Text(
                text = "Received Deep Link History (${deepLinkLogs.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (deepLinkLogs.isEmpty()) {
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
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No deep links received yet. Send a deep link or click 'Simulate'.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deepLinkLogs.reversed(), key = { it.id }) { log ->
                        DeepLinkLogCard(
                            log = log,
                            onLoadInWebView = {
                                onLoadUrlInWebView(log.rawUrl)
                                onDismiss()
                            },
                            onCopyUrl = {
                                copyToClipboard(context, "Deep Link URL", log.rawUrl)
                                Toast.makeText(context, "URL copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeepLinkLogCard(
    log: DeepLinkLog,
    onLoadInWebView: () -> Unit,
    onCopyUrl: () -> Unit
) {
    val timeFormatted = remember(log.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    }

    val isWebUrl = log.scheme == "http" || log.scheme == "https"

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
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.scheme.ifBlank { "LINK" }.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = if (log.source == "INTENT") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.source,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    IconButton(
                        onClick = onCopyUrl,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.rawUrl,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Extracted Parameters
            if (log.queryParams.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("Extracted Query Params:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        log.queryParams.forEach { (k, v) ->
                            Text("$k = $v", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (isWebUrl) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = onLoadInWebView,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Load in WebView", fontSize = 11.sp)
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
