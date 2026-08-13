package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.NetworkLog
import org.json.JSONArray
import org.json.JSONObject

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButtonDefaults
import com.example.data.AiChatMessage
import com.example.data.AiChatSender
import com.example.data.MockRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSheet(
    sheetState: SheetState,
    networkLogs: List<NetworkLog>,
    selectedMethodFilter: String?,
    searchQuery: String,
    mockRules: List<MockRule> = emptyList(),
    aiMessages: List<AiChatMessage> = emptyList(),
    isAiLoading: Boolean = false,
    aiProvider: String = "GEMINI",
    aiBaseUrl: String = "",
    aiApiKey: String = "",
    aiModelName: String = "",
    onFilterMethodChange: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearLogs: () -> Unit,
    onAddMockRule: (urlPattern: String, method: String, statusCode: Int, responseBody: String, contentType: String) -> Unit = { _, _, _, _, _ -> },
    onUpdateMockRule: (id: String, urlPattern: String, method: String, statusCode: Int, responseBody: String, contentType: String, enabled: Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onToggleMockRule: (id: String) -> Unit = {},
    onRemoveMockRule: (id: String) -> Unit = {},
    onCreateMockFromLog: (NetworkLog) -> Unit = {},
    onSendAiMessage: (String) -> Unit = {},
    onClearAiMessages: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Logs, 1: Mocks
    var selectedLogForDetail by remember { mutableStateOf<NetworkLog?>(null) }
    var selectedStatusCategory by remember { mutableStateOf<String?>(null) }
    var editingMockRule by remember { mutableStateOf<MockRule?>(null) }
    var isAddingNewMock by remember { mutableStateOf(false) }

    val filteredLogs = networkLogs.filter { log ->
        val matchesSearch = searchQuery.isBlank() ||
                log.url.contains(searchQuery, ignoreCase = true) ||
                log.method.contains(searchQuery, ignoreCase = true) ||
                log.statusCode.toString().contains(searchQuery) ||
                log.requestBody.contains(searchQuery, ignoreCase = true) ||
                log.responseBody.contains(searchQuery, ignoreCase = true)

        val matchesMethod = selectedMethodFilter == null || log.method.equals(selectedMethodFilter, ignoreCase = true)

        val matchesStatus = when (selectedStatusCategory) {
            "2xx" -> log.statusCode in 200..299
            "3xx" -> log.statusCode in 300..399
            "4xx" -> log.statusCode in 400..499
            "5xx" -> log.statusCode in 500..599
            else -> true
        }

        matchesSearch && matchesMethod && matchesStatus
    }.reversed() // Most recent first

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.93f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Http,
                        contentDescription = "Network Inspector",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Network Inspector",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    if (networkLogs.isNotEmpty() && activeSubTab == 0) {
                        IconButton(onClick = onClearLogs) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Logs",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-tabs: Requests Logs vs Mock Rules vs AI Assistant
            TabRow(
                selectedTabIndex = activeSubTab,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Http, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Logs (${networkLogs.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mocks (${mockRules.count { it.enabled }}/${mockRules.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("چت با AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeSubTab == 0) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search URL, method, status, payload...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Method:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                    FilterChip(
                        selected = selectedMethodFilter == null,
                        onClick = { onFilterMethodChange(null) },
                        label = { Text("ALL") }
                    )
                    listOf("GET", "POST", "PUT", "DELETE", "PATCH").forEach { m ->
                        FilterChip(
                            selected = selectedMethodFilter.equals(m, ignoreCase = true),
                            onClick = {
                                if (selectedMethodFilter.equals(m, ignoreCase = true)) onFilterMethodChange(null)
                                else onFilterMethodChange(m)
                            },
                            label = { Text(m) }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)

                    listOf("2xx", "3xx", "4xx", "5xx").forEach { codeCat ->
                        FilterChip(
                            selected = selectedStatusCategory == codeCat,
                            onClick = {
                                selectedStatusCategory = if (selectedStatusCategory == codeCat) null else codeCat
                            },
                            label = { Text(codeCat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Network Request List
                if (filteredLogs.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (networkLogs.isEmpty()) "No network activity logged yet." else "No requests match the current filters.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            NetworkLogCard(
                                log = log,
                                onClick = { selectedLogForDetail = log }
                            )
                        }
                    }
                }
            } else if (activeSubTab == 1) {
                // Mock Rules View
                Column(modifier = Modifier.fillMaxWidth()) {
                    // AI Mock Chat Callout Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeSubTab = 2 },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("ساخت و تغییر ماک با هوش مصنوعی (AI)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("به صورت چت به AI بگو چه ماکی برات اضافه یا ویرایش کنه", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intercept & Mock Network Responses",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { isAddingNewMock = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Mock Rule", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (mockRules.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(28.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No Mock Rules configured",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Create rules manually or ask AI in chat tab to generate mocks for you.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { activeSubTab = 2 }
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("چت با AI")
                                    }
                                    Button(
                                        onClick = {
                                            onAddMockRule("api/test", "ALL", 200, "{\n  \"status\": \"success\",\n  \"mocked\": true\n}", "application/json")
                                            Toast.makeText(context, "Sample mock rule added!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Add Sample Rule")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(mockRules, key = { it.id }) { rule ->
                                MockRuleCard(
                                    rule = rule,
                                    onToggle = { onToggleMockRule(rule.id) },
                                    onEdit = { editingMockRule = rule },
                                    onDelete = { onRemoveMockRule(rule.id) }
                                )
                            }
                        }
                    }
                }
            } else {
                // AI Mock Generator Chat View
                NetworkAiChatTab(
                    aiMessages = aiMessages,
                    isLoading = isAiLoading,
                    onSendMessage = onSendAiMessage,
                    onClearMessages = onClearAiMessages,
                    onAddMockRule = { url, method, status, body, contentType ->
                        onAddMockRule(url, method, status, body, contentType)
                        Toast.makeText(context, "ماک جدید به لیست اضافه شد!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Detailed Request Modal Dialog
    selectedLogForDetail?.let { log ->
        NetworkDetailDialog(
            log = log,
            aiProvider = aiProvider,
            aiBaseUrl = aiBaseUrl,
            aiApiKey = aiApiKey,
            aiModelName = aiModelName,
            onCreateMock = {
                onCreateMockFromLog(log)
                selectedLogForDetail = null
                activeSubTab = 1
                Toast.makeText(context, "Created Mock Rule from request!", Toast.LENGTH_SHORT).show()
            },
            onAddMockRule = onAddMockRule,
            onDismiss = { selectedLogForDetail = null }
        )
    }

    // Create / Edit Mock Rule Dialog
    if (isAddingNewMock || editingMockRule != null) {
        val isEdit = editingMockRule != null
        val targetRule = editingMockRule ?: MockRule()

        MockRuleEditDialog(
            initialRule = targetRule,
            isEdit = isEdit,
            onSave = { urlPattern, method, statusCode, responseBody, contentType, enabled ->
                if (isEdit) {
                    onUpdateMockRule(targetRule.id, urlPattern, method, statusCode, responseBody, contentType, enabled)
                    Toast.makeText(context, "Mock rule updated!", Toast.LENGTH_SHORT).show()
                } else {
                    onAddMockRule(urlPattern, method, statusCode, responseBody, contentType)
                    Toast.makeText(context, "Mock rule created!", Toast.LENGTH_SHORT).show()
                }
                editingMockRule = null
                isAddingNewMock = false
            },
            onDismiss = {
                editingMockRule = null
                isAddingNewMock = false
            }
        )
    }
}

@Composable
private fun NetworkLogCard(
    log: NetworkLog,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    val methodColor = when (log.method.uppercase()) {
        "GET" -> Color(0xFF2E7D32)
        "POST" -> Color(0xFF1565C0)
        "PUT" -> Color(0xFFE65100)
        "DELETE" -> Color(0xFFC62828)
        "PATCH" -> Color(0xFF6A1B9A)
        else -> MaterialTheme.colorScheme.secondary
    }

    val statusColor = when (log.statusCode) {
        in 200..299 -> Color(0xFF2E7D32)
        in 300..399 -> Color(0xFF1565C0)
        in 400..499 -> Color(0xFFE65100)
        in 500..599 -> Color(0xFFC62828)
        else -> Color.Gray
    }

    val urlPath = try {
        val uri = java.net.URI(log.url)
        val path = uri.path.ifBlank { "/" }
        val query = if (uri.query != null) "?${uri.query}" else ""
        "$path$query"
    } catch (_: Exception) {
        log.url
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Method Tag
            Surface(
                color = methodColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = log.method.uppercase(),
                    color = methodColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Tag
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (log.statusCode > 0) "${log.statusCode}" else "ERR",
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // URL & details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.isMocked) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "⚡ MOCKED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = urlPath,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = log.url,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Duration
            if (log.durationMs > 0) {
                Text(
                    text = "${log.durationMs}ms",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NetworkDetailDialog(
    log: NetworkLog,
    aiProvider: String,
    aiBaseUrl: String,
    aiApiKey: String,
    aiModelName: String,
    onCreateMock: () -> Unit = {},
    onAddMockRule: (urlPattern: String, method: String, statusCode: Int, responseBody: String, contentType: String) -> Unit = { _, _, _, _, _ -> },
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Dialog Title & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = log.method.uppercase(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Request Inspector",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Bar (cURL & Mock Response)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val curl = buildCurlCommand(log)
                            copyToClipboard(context, "cURL Command", curl)
                            Toast.makeText(context, "cURL copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy cURL", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onCreateMock,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mock Response", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Response", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Request", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Headers", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Overview", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("چت با AI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> ResponseBodyView(log = log, context = context)
                        1 -> RequestBodyView(log = log, context = context)
                        2 -> HeadersView(log = log, context = context)
                        3 -> OverviewView(log = log, context = context)
                        4 -> SingleRequestAiChatTab(
                            log = log, 
                            aiProvider = aiProvider,
                            aiBaseUrl = aiBaseUrl,
                            aiApiKey = aiApiKey,
                            aiModelName = aiModelName,
                            onAddMockRule = onAddMockRule
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MockRuleCard(
    rule: MockRule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val methodColor = when (rule.method.uppercase()) {
        "GET" -> Color(0xFF2E7D32)
        "POST" -> Color(0xFF1565C0)
        "PUT" -> Color(0xFFE65100)
        "DELETE" -> Color(0xFFC62828)
        "ALL" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    val statusColor = when (rule.statusCode) {
        in 200..299 -> Color(0xFF2E7D32)
        in 300..399 -> Color(0xFF1565C0)
        in 400..499 -> Color(0xFFE65100)
        in 500..599 -> Color(0xFFC62828)
        else -> Color.Gray
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.enabled) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = rule.enabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.padding(end = 8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = methodColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = rule.method.uppercase(),
                            color = methodColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${rule.statusCode}",
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = rule.urlPattern,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (rule.responseBody.isNotBlank()) {
                    Text(
                        text = rule.responseBody.replace("\n", " ").take(60),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Rule", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Rule", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun MockRuleEditDialog(
    initialRule: MockRule,
    isEdit: Boolean,
    onSave: (urlPattern: String, method: String, statusCode: Int, responseBody: String, contentType: String, enabled: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var urlPattern by remember { mutableStateOf(initialRule.urlPattern) }
    var method by remember { mutableStateOf(initialRule.method) }
    var statusCodeStr by remember { mutableStateOf(initialRule.statusCode.toString()) }
    var responseBody by remember { mutableStateOf(initialRule.responseBody) }
    var contentType by remember { mutableStateOf(initialRule.contentType) }
    var enabled by remember { mutableStateOf(initialRule.enabled) }

    val methods = listOf("ALL", "GET", "POST", "PUT", "DELETE", "PATCH")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "Edit Mock Rule" else "New Mock Rule",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = urlPattern,
                    onValueChange = { urlPattern = it },
                    label = { Text("URL Pattern / Keyword") },
                    placeholder = { Text("e.g. api/users or https://example.com/api") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = statusCodeStr,
                        onValueChange = { statusCodeStr = it },
                        label = { Text("Status Code") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = contentType,
                        onValueChange = { contentType = it },
                        label = { Text("Content-Type") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Text("Method Filter:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    methods.forEach { m ->
                        FilterChip(
                            selected = method.equals(m, ignoreCase = true),
                            onClick = { method = m },
                            label = { Text(m, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = responseBody,
                    onValueChange = { responseBody = it },
                    label = { Text("Mocked Response Body (JSON/Text)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (enabled) "Rule Enabled" else "Rule Disabled", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val code = statusCodeStr.toIntOrNull() ?: 200
                            if (urlPattern.isNotBlank()) {
                                onSave(urlPattern, method, code, responseBody, contentType, enabled)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(if (isEdit) "Save" else "Create")
                    }
                }
            }
        }
    }
}

@Composable
private fun ResponseBodyView(log: NetworkLog, context: Context) {
    val formattedResponse = remember(log.responseBody) {
        prettyPrintJson(log.responseBody)
    }
    val isJson = remember(log.responseBody) {
        val trimmed = log.responseBody.trim()
        (trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isJson) "Response Body (Formatted JSON)" else "Response Body",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (isJson) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "JSON",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (formattedResponse.isNotBlank()) {
                IconButton(onClick = {
                    copyToClipboard(context, "Response Body", formattedResponse)
                    Toast.makeText(context, "Response body copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Response", modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (formattedResponse.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No response body returned", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = formattedResponse,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestBodyView(log: NetworkLog, context: Context) {
    val formattedRequest = remember(log.requestBody) {
        prettyPrintJson(log.requestBody)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Request Payload / Body", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            if (formattedRequest.isNotBlank()) {
                IconButton(onClick = {
                    copyToClipboard(context, "Request Body", formattedRequest)
                    Toast.makeText(context, "Request body copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Request", modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (formattedRequest.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No request body sent (${log.method})", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = formattedRequest,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeadersView(log: NetworkLog, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Request Headers (${log.requestHeaders.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        if (log.requestHeaders.isEmpty()) {
            Text("None specified", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        } else {
            log.requestHeaders.forEach { (k, v) ->
                HeaderPairRow(key = k, value = v, context = context)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Response Headers (${log.responseHeaders.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        if (log.responseHeaders.isEmpty()) {
            Text("None returned", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        } else {
            log.responseHeaders.forEach { (k, v) ->
                HeaderPairRow(key = k, value = v, context = context)
            }
        }
    }
}

@Composable
private fun HeaderPairRow(key: String, value: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = key, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(
            onClick = {
                copyToClipboard(context, key, "$key: $value")
                Toast.makeText(context, "Header copied!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Header", modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun OverviewView(log: NetworkLog, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OverviewDetailItem("Full URL", log.url, context = context)
        OverviewDetailItem("Method", log.method, context = null)
        OverviewDetailItem("Status Code", "${log.statusCode} ${log.statusText}", context = null)
        OverviewDetailItem("Duration", "${log.durationMs} ms", context = null)
        OverviewDetailItem("Resource Type", log.resourceType, context = null)
        OverviewDetailItem("Timestamp", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp)), context = null)
    }
}

@Composable
private fun OverviewDetailItem(label: String, value: String, context: Context?) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
            }
            if (context != null) {
                IconButton(
                    onClick = {
                        copyToClipboard(context, label, value)
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

private fun buildCurlCommand(log: NetworkLog): String {
    val sb = StringBuilder("curl -X ${log.method.uppercase()}")
    sb.append(" \"${log.url}\"")

    log.requestHeaders.forEach { (key, value) ->
        val escapedValue = value.replace("\"", "\\\"")
        sb.append(" \\\n  -H \"$key: $escapedValue\"")
    }

    if (log.requestBody.isNotBlank()) {
        val escapedBody = log.requestBody.replace("'", "'\\''")
        sb.append(" \\\n  --data-raw '$escapedBody'")
    }

    return sb.toString()
}

private fun prettyPrintJson(json: String): String {
    if (json.isBlank()) return json
    val trimmed = json.trim()
    return try {
        if (trimmed.startsWith("{")) {
            JSONObject(trimmed).toString(2)
        } else if (trimmed.startsWith("[")) {
            JSONArray(trimmed).toString(2)
        } else {
            json
        }
    } catch (_: Exception) {
        json
    }
}

@Composable
fun NetworkAiChatTab(
    aiMessages: List<AiChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearMessages: () -> Unit,
    onAddMockRule: (urlPattern: String, method: String, statusCode: Int, responseBody: String, contentType: String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "ماک /api/users با استاتوس 200 و لیست ۳ کاربر نمونه",
        "خطای 500 برای درخواست /api/checkout",
        "پاسخ 401 Unauthorized برای /api/auth/login",
        "پاسخ موفق با JSON سفارشی برای /api/profile"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "دستیار AI تولید و ویرایش Mock",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (aiMessages.isNotEmpty()) {
                IconButton(onClick = onClearMessages, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "پاک کردن چت",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Suggestion Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickPrompts) { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { onSendMessage(prompt) },
                    label = { Text(prompt, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            if (aiMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "از هوش مصنوعی بخواهید ماک رول جدید بپذیرد!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مثال: \"برای درخواست POST به /api/login یک ماک با توکن JWT و کد 200 بساز\"",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(aiMessages) { msg ->
                        AiMockChatMessageCard(
                            message = msg,
                            onAddMockRule = onAddMockRule
                        )
                    }

                    if (isLoading) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("هوش مصنوعی در حال تولید ماک...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field & Send Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("مثال: ماک ۴۰۴ برای /api/items...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank() && !isLoading,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "ارسال", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun AiMockChatMessageCard(
    message: AiChatMessage,
    onAddMockRule: (urlPattern: String, method: String, statusCode: Int, responseBody: String, contentType: String) -> Unit
) {
    val isUser = message.sender == AiChatSender.USER
    val isSystem = message.sender == AiChatSender.SYSTEM

    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = when {
        isUser -> MaterialTheme.colorScheme.primaryContainer
        isSystem -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                SelectionContainer {
                    Text(
                        text = message.text,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // If AI response contains mock rule JSON block
                if (!isUser && !isSystem) {
                    val mock = extractMockFromJsonBlock(message.text)
                    if (mock != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${mock.method} ${mock.urlPattern}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        color = if (mock.statusCode in 200..299) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${mock.statusCode}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mock.responseBody,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        onAddMockRule(mock.urlPattern, mock.method, mock.statusCode, mock.responseBody, mock.contentType)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("افزودن این قانون به ماک‌ها", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun extractMockFromJsonBlock(aiText: String): MockRule? {
    try {
        val pattern = Regex("```json_mock\\s*(\\{[\\s\\S]*?\\})\\s*```", RegexOption.IGNORE_CASE)
        val match = pattern.find(aiText) ?: Regex("```json\\s*(\\{[\\s\\S]*?\\})\\s*```", RegexOption.IGNORE_CASE).find(aiText)
        val jsonStr = match?.groupValues?.get(1) ?: return null

        val json = JSONObject(jsonStr)
        return MockRule(
            urlPattern = json.optString("urlPattern", "api/*"),
            method = json.optString("method", "ALL"),
            statusCode = json.optInt("statusCode", 200),
            responseBody = json.optString("responseBody", "{}"),
            contentType = json.optString("contentType", "application/json"),
            enabled = true
        )
    } catch (_: Exception) {
        return null
    }
}

@Composable
fun SingleRequestAiChatTab(
    log: NetworkLog,
    aiProvider: String,
    aiBaseUrl: String,
    aiApiKey: String,
    aiModelName: String,
    onAddMockRule: (urlPattern: String, method: String, statusCode: Int, responseBody: String, contentType: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var aiMessages by remember { mutableStateOf<List<AiChatMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(log.id) {
        if (aiMessages.isEmpty()) {
            val urlPath = try {
                val uri = java.net.URI(log.url)
                val path = uri.path.ifBlank { log.url }
                val query = if (uri.query != null) "?${uri.query}" else ""
                "$path$query"
            } catch (_: Exception) { log.url }

            val initialMsg = AiChatMessage(
                sender = AiChatSender.AI,
                text = "سلام! من اطلاعات این درخواست را بررسی کردم:\n`" + log.method.uppercase() + " " + urlPath + "` (کد وضعیت: " + log.statusCode + ")\n\nمی‌توانید درباره این پاسخ سوال بپرسید یا از من بخواهید یک قانون ماک (Mock) سفارشی برای آن بسازم."
            )
            aiMessages = listOf(initialMsg)
        }
    }

    val quickPrompts = listOf(
        "توضیح کامل این درخواست و پاسخ",
        "ایجاد ماک ۲۰۰ با دیتای موفق",
        "ایجاد ماک ۵۰۰ برای این اندپوئینت",
        "ویرایش پاسخ و تغییر فیلدهای JSON"
    )

    fun sendMessage(userPrompt: String) {
        if (userPrompt.isBlank() || isLoading) return

        val userMsg = AiChatMessage(sender = AiChatSender.USER, text = userPrompt)
        aiMessages = aiMessages + userMsg
        isLoading = true

        scope.launch {
            val logContext = """
                URL: ${log.url}
                HTTP Method: ${log.method}
                Status Code: ${log.statusCode}
                Request Headers: ${log.requestHeaders}
                Response Headers: ${log.responseHeaders}
                Request Body: ${log.requestBody}
                Response Body: ${log.responseBody}
            """.trimIndent()

            val result = com.example.data.AiService.generateNetworkMock(userPrompt, logContext, provider = aiProvider, baseUrl = aiBaseUrl, apiKey = aiApiKey, modelName = aiModelName)
            val aiMsg = if (result.isSuccess) {
                AiChatMessage(
                    sender = AiChatSender.AI,
                    text = result.getOrDefault("پاسخی تولید نشد.")
                )
            } else {
                AiChatMessage(
                    sender = AiChatSender.SYSTEM,
                    text = "⚠️ خطا: ${result.exceptionOrNull()?.message}"
                )
            }

            aiMessages = aiMessages + aiMsg
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
    ) {
        // Quick Prompts
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickPrompts) { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { sendMessage(prompt) },
                    label = { Text(prompt, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Messages Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(aiMessages) { msg ->
                    AiMockChatMessageCard(
                        message = msg,
                        onAddMockRule = { url, method, status, body, contentType ->
                            onAddMockRule(url, method, status, body, contentType)
                            Toast.makeText(context, "ماک جدید به لیست اضافه شد!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("در حال بررسی درخواست توسط AI...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field & Send Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("سوال یا دستور تغییر ماک این درخواست...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    val prompt = inputText
                    inputText = ""
                    sendMessage(prompt)
                },
                enabled = inputText.isNotBlank() && !isLoading,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "ارسال", modifier = Modifier.size(18.dp))
            }
        }
    }
}
