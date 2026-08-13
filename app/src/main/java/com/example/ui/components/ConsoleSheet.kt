package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConsoleLog
import com.example.data.LogLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsoleSheet(
    sheetState: SheetState,
    consoleLogs: List<ConsoleLog>,
    selectedFilter: LogLevel?,
    searchQuery: String,
    onFilterChange: (LogLevel?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearLogs: () -> Unit,
    onExecuteJs: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var jsInput by remember { mutableStateOf("") }

    val filteredLogs = consoleLogs.filter { log ->
        val matchesFilter = selectedFilter == null || log.level == selectedFilter
        val matchesSearch = searchQuery.isBlank() || log.message.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "JS Console Logs",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${filteredLogs.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row {
                    IconButton(onClick = {
                        if (consoleLogs.isEmpty()) return@IconButton
                        val textToCopy = consoleLogs.joinToString("\n") { "[${it.level}] ${it.message}" }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("JS Logs", textToCopy))
                        Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy All Logs")
                    }

                    IconButton(onClick = onClearLogs) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = MaterialTheme.colorScheme.error)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Filter console output...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("ALL (${consoleLogs.size})") }
                )
                FilterChip(
                    selected = selectedFilter == LogLevel.LOG,
                    onClick = { onFilterChange(if (selectedFilter == LogLevel.LOG) null else LogLevel.LOG) },
                    label = { Text("Log") }
                )
                FilterChip(
                    selected = selectedFilter == LogLevel.INFO,
                    onClick = { onFilterChange(if (selectedFilter == LogLevel.INFO) null else LogLevel.INFO) },
                    label = { Text("Info") }
                )
                FilterChip(
                    selected = selectedFilter == LogLevel.WARNING,
                    onClick = { onFilterChange(if (selectedFilter == LogLevel.WARNING) null else LogLevel.WARNING) },
                    label = { Text("Warn") }
                )
                FilterChip(
                    selected = selectedFilter == LogLevel.ERROR,
                    onClick = { onFilterChange(if (selectedFilter == LogLevel.ERROR) null else LogLevel.ERROR) },
                    label = { Text("Error") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Execute JS input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = jsInput,
                    onValueChange = { jsInput = it },
                    placeholder = { Text("Enter JavaScript code...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        if (jsInput.isNotBlank()) {
                            onExecuteJs(jsInput)
                            jsInput = "" // Optionally clear after execute
                        }
                    },
                    enabled = jsInput.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Run")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Console output list
            if (filteredLogs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (consoleLogs.isEmpty()) "No JS console logs recorded yet." else "No logs match current filter.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        ConsoleLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsoleLogItem(log: ConsoleLog) {
    val (bgColor, textColor, badgeColor) = when (log.level) {
        LogLevel.ERROR -> Triple(
            Color(0xFFFEF2F2),
            Color(0xFF991B1B),
            Color(0xFFEF4444)
        )
        LogLevel.WARNING -> Triple(
            Color(0xFFFFFBEB),
            Color(0xFF92400E),
            Color(0xFFF59E0B)
        )
        LogLevel.INFO -> Triple(
            Color(0xFFEFF6FF),
            Color(0xFF1E40AF),
            Color(0xFF3B82F6)
        )
        LogLevel.LOG -> Triple(
            Color(0xFFF8FAFC),
            Color(0xFF1E293B),
            Color(0xFF64748B)
        )
    }

    val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(badgeColor, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.level.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (log.sourceId.isNotBlank()) {
                    Text(
                        text = "${log.sourceId.substringAfterLast("/")}:${log.lineNumber}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.message,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = textColor
            )
        }
    }
}
