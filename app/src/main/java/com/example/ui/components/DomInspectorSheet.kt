package com.example.ui.components

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DomTreeNode
import com.example.data.SelectedElementInfo
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomInspectorSheet(
    sheetState: SheetState,
    selectedElement: SelectedElementInfo?,
    domTreeRoot: DomTreeNode?,
    isElementPickerActive: Boolean,
    webView: WebView?,
    onToggleElementPicker: () -> Unit,
    onSetDomTreeRoot: (DomTreeNode?) -> Unit,
    onSelectElement: (SelectedElementInfo) -> Unit,
    onAskAiAboutElement: (SelectedElementInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var isExtractingDom by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val tabs = listOf("DOM Tree", "Styles & Metrics", "Outer HTML")

    // Extract DOM Tree on launch if empty
    LaunchedEffect(Unit) {
        if (domTreeRoot == null && webView != null) {
            isExtractingDom = true
            webView.evaluateJavascript("window.__extractDomTree ? window.__extractDomTree() : '';") { jsonStr ->
                isExtractingDom = false
                val cleanJson = jsonStr?.removeSurrounding("\"")
                    ?.replace("\\u003C", "<")
                    ?.replace("\\\"", "\"")
                    ?.replace("\\\\", "\\")
                if (!cleanJson.isNullOrBlank()) {
                    try {
                        val parsedNode = parseDomJson(cleanJson)
                        onSetDomTreeRoot(parsedNode)
                    } catch (_: Exception) {}
                }
            }
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
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeveloperMode,
                        contentDescription = "Desktop DevTools Inspector",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "اینسپکتور دسکتاپ DOM & CSS",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مشاهده درخت DOM، استایل‌های Computed و ویرایش کد",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    // Element Picker button
                    Button(
                        onClick = {
                            onToggleElementPicker()
                            if (webView != null) {
                                webView.evaluateJavascript("if(window.__activateElementPicker) window.__activateElementPicker();", null)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isElementPickerActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isElementPickerActive) "انتخاب المان..." else "انتخاب روی صفحه",
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Selected Element Summary Card
            if (selectedElement != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "<${selectedElement.tagName}>",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (selectedElement.id.isNotBlank()) {
                                    Text(
                                        text = " #${selectedElement.id}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFFF9800),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                if (selectedElement.className.isNotBlank()) {
                                    Text(
                                        text = " .${selectedElement.className.take(25)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4CAF50),
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (selectedElement.innerText.isNotBlank()) {
                                Text(
                                    text = "\"${selectedElement.innerText.take(60)}\"",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Ask AI button
                        Button(
                            onClick = { onAskAiAboutElement(selectedElement) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تحلیل با AI", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tab Selector
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTabIndex) {
                    0 -> DomTreeTab(
                        domTreeRoot = domTreeRoot,
                        isLoading = isExtractingDom,
                        onRefreshDom = {
                            if (webView != null) {
                                isExtractingDom = true
                                webView.evaluateJavascript("window.__extractDomTree ? window.__extractDomTree() : '';") { jsonStr ->
                                    isExtractingDom = false
                                    val cleanJson = jsonStr?.removeSurrounding("\"")
                                        ?.replace("\\u003C", "<")
                                        ?.replace("\\\"", "\"")
                                        ?.replace("\\\\", "\\")
                                    if (!cleanJson.isNullOrBlank()) {
                                        try {
                                            val parsedNode = parseDomJson(cleanJson)
                                            onSetDomTreeRoot(parsedNode)
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        },
                        onNodeClick = { node ->
                            onSelectElement(
                                SelectedElementInfo(
                                    tagName = node.tagName,
                                    id = node.id,
                                    className = node.className,
                                    innerText = node.text
                                )
                            )
                        }
                    )
                    1 -> StylesAndMetricsTab(selectedElement = selectedElement)
                    2 -> OuterHtmlTab(
                        selectedElement = selectedElement,
                        onCopy = { code ->
                            clipboardManager.setText(AnnotatedString(code))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DomTreeTab(
    domTreeRoot: DomTreeNode?,
    isLoading: Boolean,
    onRefreshDom: () -> Unit,
    onNodeClick: (DomTreeNode) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "درخت عناصر HTML (DOM Tree)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onRefreshDom, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

        if (domTreeRoot == null && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "برای نمایش درخت DOM روی دکمه بروزرسانی کلیک کنید",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else if (domTreeRoot != null) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    DomNodeItem(node = domTreeRoot, depth = 0, onNodeClick = onNodeClick)
                }
            }
        }
    }
}

@Composable
fun DomNodeItem(
    node: DomTreeNode,
    depth: Int,
    onNodeClick: (DomTreeNode) -> Unit
) {
    var isExpanded by remember { mutableStateOf(depth < 2) }

    Column(modifier = Modifier.padding(start = (depth * 12).dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    if (node.children.isNotEmpty()) {
                        isExpanded = !isExpanded
                    }
                    onNodeClick(node)
                }
                .padding(vertical = 3.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (node.children.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            if (node.type == "element") {
                Text(
                    text = "<${node.tagName}>",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (node.id.isNotBlank()) {
                    Text(
                        text = " id=\"${node.id}\"",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFF9800)
                    )
                }
                if (node.className.isNotBlank()) {
                    Text(
                        text = " class=\"${node.className.take(20)}\"",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF4CAF50),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = "\"${node.text}\"",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isExpanded && node.children.isNotEmpty()) {
            node.children.forEach { child ->
                DomNodeItem(node = child, depth = depth + 1, onNodeClick = onNodeClick)
            }
        }
    }
}

@Composable
fun StylesAndMetricsTab(selectedElement: SelectedElementInfo?) {
    if (selectedElement == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "برای مشاهده استایل‌های CSS، ابتدا یک المان را از روی صفحه لمس کنید.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
    ) {
        // Box Model Diagram
        Text(text = "مدل جعبه المان (Box Model)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFFFB74D), shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("margin: ${selectedElement.styles["margin"] ?: "0px"}", fontSize = 10.sp, color = Color(0xFFE65100))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF81C784), shape = RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("padding: ${selectedElement.styles["padding"] ?: "0px"}", fontSize = 10.sp, color = Color(0xFF2E7D32))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF64B5F6), shape = RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${selectedElement.styles["width"] ?: "auto"} × ${selectedElement.styles["height"] ?: "auto"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Computed Styles Table
        Text(text = "ویژگی‌های محاسبه‌شده CSS (Computed Styles)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                selectedElement.styles.forEach { (property, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = property,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = value,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun OuterHtmlTab(selectedElement: SelectedElementInfo?, onCopy: (String) -> Unit) {
    if (selectedElement == null || selectedElement.outerHTML.isBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "هیچ کد OuterHTML انتخاب نشده است",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "کد HTML المان (OuterHTML)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { onCopy(selectedElement.outerHTML) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("کپی کد", fontSize = 11.sp)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = selectedElement.outerHTML,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

fun parseDomJson(jsonStr: String): DomTreeNode {
    val json = JSONObject(jsonStr)
    val type = json.optString("type", "element")
    val tagName = json.optString("tagName", "")
    val id = json.optString("id", "")
    val className = json.optString("className", "")
    val text = json.optString("text", "")

    val childrenList = mutableListOf<DomTreeNode>()
    val childrenArr = json.optJSONArray("children")
    if (childrenArr != null) {
        for (i in 0 until childrenArr.length()) {
            val childObj = childrenArr.getJSONObject(i)
            childrenList.add(parseDomJson(childObj.toString()))
        }
    }

    return DomTreeNode(
        type = type,
        tagName = tagName,
        id = id,
        className = className,
        text = text,
        children = childrenList
    )
}
