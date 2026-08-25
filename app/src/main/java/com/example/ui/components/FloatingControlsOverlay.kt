package com.example.ui.components

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LogLevel
import com.example.ui.BottomSheetType
import com.example.ui.InspectorUiState

@Composable
fun TopControlsBar(
    state: InspectorUiState,
    webView: WebView?,
    onUrlChange: (String) -> Unit,
    onLoadUrl: () -> Unit,
    onToggleExpanded: () -> Unit,
    onOpenSheet: (BottomSheetType) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main URL Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back & Forward nav
                IconButton(
                    onClick = { webView?.goBack() },
                    enabled = webView?.canGoBack() == true,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (webView?.canGoBack() == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = { webView?.goForward() },
                    enabled = webView?.canGoForward() == true,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (webView?.canGoForward() == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = { webView?.reload() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload")
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Address Input
                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                    ) {
                        TextField(
                            value = state.urlInput,
                            onValueChange = onUrlChange,
                            placeholder = { Text("https://example.com", fontSize = 14.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                textDirection = androidx.compose.ui.text.style.TextDirection.Ltr
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                focusManager.clearFocus()
                                onLoadUrl()
                            }),
                            trailingIcon = {
                                IconButton(onClick = {
                                    focusManager.clearFocus()
                                    onLoadUrl()
                                }) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Go")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("url_input_field")
                                .onKeyEvent { event ->
                                    if (event.key == Key.Enter || event.key == Key.NumPadEnter) {
                                        if (event.type == KeyEventType.KeyUp) {
                                            focusManager.clearFocus()
                                            onLoadUrl()
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Support Heart Button
                IconButton(
                    onClick = { onOpenSheet(BottomSheetType.SUPPORT) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "حمایت مالی",
                        tint = Color(0xFFE91E63)
                    )
                }
            }

            // Progress bar
            if (state.isLoading) {
                LinearProgressIndicator(
                    progress = { state.loadingProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ToolChipButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeCount: Int = 0,
    badgeIsError: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = if (badgeIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ) {
                            Text(if (badgeCount > 99) "99+" else "$badgeCount")
                        }
                    }
                ) {
                    Icon(icon, contentDescription = title, modifier = Modifier.size(16.dp))
                }
            } else {
                Icon(icon, contentDescription = title, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomInspectorBar(
    state: InspectorUiState,
    onOpenSheet: (BottomSheetType) -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpanded = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bookmarks & History
                ToolChipButton(
                    title = "Bookmarks",
                    icon = Icons.Default.Bookmark,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenSheet(BottomSheetType.PROFILES) }
                )
                
                Spacer(modifier = Modifier.width(8.dp))

                // AI Assistant Button
                ToolChipButton(
                    title = "AI",
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f),
                    badgeCount = state.aiMessages.size,
                    onClick = { onOpenSheet(BottomSheetType.AI_ASSISTANT) }
                )
                
                Spacer(modifier = Modifier.width(8.dp))

                // Toggle Accordion Button
                ToolChipButton(
                    title = if (isExpanded.value) "Less" else "More",
                    icon = if (isExpanded.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    modifier = Modifier.weight(1f),
                    onClick = { isExpanded.value = !isExpanded.value }
                )
            }

            androidx.compose.animation.AnimatedVisibility(visible = isExpanded.value) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Desktop DOM & CSS Inspector Button
                    ToolChipButton(
                        title = "DOM / CSS",
                        icon = Icons.Default.DeveloperMode,
                        badgeCount = if (state.selectedElement != null) 1 else 0,
                        onClick = { onOpenSheet(BottomSheetType.DOM_INSPECTOR) }
                    )

                    // Network Inspector Button
                    ToolChipButton(
                        title = "Network",
                        icon = Icons.Default.Http,
                        badgeCount = state.networkLogs.size,
                        onClick = { onOpenSheet(BottomSheetType.NETWORK) }
                    )

                    // Console Button
                    val errorCount = state.consoleLogs.count { it.level == LogLevel.ERROR }
                    ToolChipButton(
                        title = "Console",
                        icon = Icons.Default.BugReport,
                        badgeCount = state.consoleLogs.size,
                        badgeIsError = errorCount > 0,
                        onClick = { onOpenSheet(BottomSheetType.CONSOLE) }
                    )

                    // Params Button
                    val activeParamsCount = state.queryParams.count { it.enabled && it.key.isNotBlank() }
                    ToolChipButton(
                        title = "Params",
                        icon = Icons.Default.Tune,
                        badgeCount = activeParamsCount,
                        onClick = { onOpenSheet(BottomSheetType.PARAMS) }
                    )

                    // postMessage Inspector Button
                    ToolChipButton(
                        title = "postMessage",
                        icon = Icons.Default.SwapHoriz,
                        badgeCount = state.postMessageLogs.size,
                        onClick = { onOpenSheet(BottomSheetType.POST_MESSAGE) }
                    )

                    // Deep Links Inspector Button
                    ToolChipButton(
                        title = "Deep Links",
                        icon = Icons.Default.Link,
                        badgeCount = state.deepLinkLogs.size,
                        onClick = { onOpenSheet(BottomSheetType.DEEP_LINK) }
                    )

                    // Headers Button
                    val activeHeadersCount = state.customHeaders.count { it.enabled && it.key.isNotBlank() }
                    ToolChipButton(
                        title = "Headers",
                        icon = Icons.Default.FilterList,
                        badgeCount = activeHeadersCount,
                        onClick = { onOpenSheet(BottomSheetType.HEADERS) }
                    )

                    // Clear Cache / Storage Button
                    ToolChipButton(
                        title = "Storage",
                        icon = Icons.Default.CleaningServices,
                        onClick = { onOpenSheet(BottomSheetType.STORAGE) }
                    )

                    // User Agent Switcher
                    ToolChipButton(
                        title = "User-Agent",
                        icon = Icons.Default.Devices,
                        onClick = { onOpenSheet(BottomSheetType.USER_AGENT) }
                    )
                }
            }
        }
    }
}

@Composable
fun CollapsedFloatingDock(
    isToolbarExpanded: Boolean,
    consoleLogsCount: Int,
    errorLogsCount: Int,
    networkLogsCount: Int = 0,
    activeParamsCount: Int,
    onToggleExpand: () -> Unit,
    onOpenConsole: () -> Unit,
    onOpenNetwork: () -> Unit = {},
    onOpenParams: () -> Unit,
    onOpenBookmarks: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Retained for backward compatibility if needed
}
