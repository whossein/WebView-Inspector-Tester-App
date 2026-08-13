package com.example.ui.components

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiChatMessage
import com.example.data.AiChatSender
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantSheet(
    sheetState: SheetState,
    messages: List<AiChatMessage>,
    isLoading: Boolean,
    webView: WebView?,
    aiProvider: String,
    aiBaseUrl: String,
    aiApiKey: String,
    aiModelName: String,
    onSendMessage: (prompt: String, context: String?) -> Unit,
    onClearMessages: () -> Unit,
    onUpdateSettings: (provider: String, baseUrl: String, apiKey: String, modelName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var attachedCodeContext by remember { mutableStateOf<String?>(null) }
    var isExtractingContext by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    
    // Settings fields
    var draftProvider by remember(aiProvider) { mutableStateOf(aiProvider) }
    var draftBaseUrl by remember(aiBaseUrl) { mutableStateOf(aiBaseUrl) }
    var draftApiKey by remember(aiApiKey) { mutableStateOf(aiApiKey) }
    var draftModelName by remember(aiModelName) { mutableStateOf(aiModelName) }
    
    val providers = listOf("GEMINI", "OPENAI", "CLAUDE", "CUSTOM")

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "بررسی ساختار HTML و تگ‌های سئو",
        "تست و عیب‌یابی استایل‌های CSS",
        "چرا صفحه‌بندی ریسپانسیو بهم ریخته؟",
        "بررسی خطاهای احتمالی اسکریپت",
        "How to inspect CSS Flexbox layout?"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "دستیار هوشمند AI (کد و CSS)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تحلیل هوشمند DOM، CSS و JavaScript وب‌سایت",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (showSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = onClearMessages) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Chat",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (showSettings) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text("تنظیمات ارائه دهنده هوش مصنوعی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Provider", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        items(providers) { p ->
                            FilterChip(
                                selected = draftProvider == p,
                                onClick = { draftProvider = p },
                                label = { Text(p, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = draftBaseUrl,
                        onValueChange = { draftBaseUrl = it },
                        label = { Text("Base URL (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = draftApiKey,
                        onValueChange = { draftApiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = draftModelName,
                        onValueChange = { draftModelName = it },
                        label = { Text("Model Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            onUpdateSettings(draftProvider, draftBaseUrl, draftApiKey, draftModelName)
                            showSettings = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ذخیره تنظیمات")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // Quick Extract Page HTML/DOM Context Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (attachedCodeContext != null)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (attachedCodeContext != null) "✅ کد DOM و HTML وب‌سایت ضمیمه شد" else "پیوست خودکار کد HTML/DOM صفحه جاری",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (attachedCodeContext != null) {
                            Text(
                                text = "${attachedCodeContext?.length ?: 0} کاراکتر از کد صفحه پیوست شده است",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (attachedCodeContext == null) {
                        Button(
                            onClick = {
                                if (webView != null) {
                                    isExtractingContext = true
                                    webView.evaluateJavascript(
                                        "(function() { return document.documentElement.outerHTML.substring(0, 3500); })();"
                                    ) { html ->
                                        isExtractingContext = false
                                        val cleanHtml = html?.removeSurrounding("\"")
                                            ?.replace("\\u003C", "<")
                                            ?.replace("\\\"", "\"")
                                            ?.replace("\\n", "\n")
                                        if (!cleanHtml.isNullOrBlank()) {
                                            attachedCodeContext = cleanHtml
                                        }
                                    }
                                }
                            },
                            enabled = !isExtractingContext,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isExtractingContext) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استخراج HTML", fontSize = 11.sp)
                            }
                        }
                    } else {
                        IconButton(onClick = { attachedCodeContext = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Context", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            // Closes else
            }

            if (!showSettings) {
                Spacer(modifier = Modifier.height(10.dp))

                // Quick Prompts Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(quickPrompts) { prompt ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                inputText = prompt
                            },
                            label = { Text(prompt, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Messages List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty() && !isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "سوال خود را در مورد CSS، HTML یا خطاهای وب‌سایت بپرسید",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "میتوانید کد DOM وب‌سایت را با دکمه بالا ضمیمه کنید تا AI دقیق‌تر پاسخ دهد.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(messages) { msg ->
                                AiMessageItem(msg)
                            }

                            if (isLoading) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "در حال تحلیل کد و پاسخگویی به سوال شما...",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("سوال در مورد کد، CSS، فریم‌ورک...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp),
                            maxLines = 3,
                            singleLine = false,
                            shape = RoundedCornerShape(12.dp)
                        )

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    val prompt = inputText
                                    val ctx = attachedCodeContext
                                    inputText = ""
                                    onSendMessage(prompt, ctx)
                                }
                            },
                            enabled = inputText.isNotBlank() && !isLoading,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiMessageItem(message: AiChatMessage) {
    val isUser = message.sender == AiChatSender.USER
    val isSystem = message.sender == AiChatSender.SYSTEM

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSystem) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSystem) Icons.Default.Warning else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSystem) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isUser) 14.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 14.dp
                ),
                color = when {
                    isUser -> MaterialTheme.colorScheme.primary
                    isSystem -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                contentColor = when {
                    isUser -> MaterialTheme.colorScheme.onPrimary
                    isSystem -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!message.attachedCodeContext.isNullOrBlank() && isUser) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "📎 با ضمیمه کد DOM وب‌سایت",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFamily = if (!isUser) FontFamily.Monospace else FontFamily.Default
                    )
                }
            }
        }
    }
}
