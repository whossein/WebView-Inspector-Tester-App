package com.example.ui.components

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageSheet(
    sheetState: SheetState,
    webView: WebView?,
    onClearCacheStorage: (clearStorage: Boolean, clearCookies: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var localStorageData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var sessionStorageData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var cookiesData by remember { mutableStateOf("No cookies found") }

    fun fetchData() {
        val url = webView?.url ?: ""
        cookiesData = android.webkit.CookieManager.getInstance().getCookie(url) ?: "No cookies found"

        webView?.evaluateJavascript(
            """
            (function() {
                try {
                    var ls = {};
                    for (var i = 0; i < localStorage.length; i++) {
                        var key = localStorage.key(i);
                        ls[key] = localStorage.getItem(key);
                    }
                    var ss = {};
                    for (var i = 0; i < sessionStorage.length; i++) {
                        var key = sessionStorage.key(i);
                        ss[key] = sessionStorage.getItem(key);
                    }
                    return JSON.stringify({ localStorage: ls, sessionStorage: ss });
                } catch(e) {
                    return JSON.stringify({ error: e.message });
                }
            })();
            """.trimIndent()
        ) { result ->
            if (result != null && result != "null") {
                try {
                    val unescaped = result.removeSurrounding("\"").replace("\\\"", "\"").replace("\\\\", "\\")
                    val json = JSONObject(unescaped)
                    
                    val lsMap = mutableMapOf<String, String>()
                    json.optJSONObject("localStorage")?.let { lsObj ->
                        val keys = lsObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            lsMap[k] = lsObj.optString(k)
                        }
                    }
                    localStorageData = lsMap

                    val ssMap = mutableMapOf<String, String>()
                    json.optJSONObject("sessionStorage")?.let { ssObj ->
                        val keys = ssObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            ssMap[k] = ssObj.optString(k)
                        }
                    }
                    sessionStorageData = ssMap
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchData()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Web Storage & Cookies",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Inspect or clear local browser data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = { fetchData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Inspect Data") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Clear Data") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f, fill = false)) {
                if (selectedTabIndex == 0) {
                    InspectDataTab(
                        localStorageData = localStorageData,
                        sessionStorageData = sessionStorageData,
                        cookiesData = cookiesData
                    )
                } else {
                    ClearDataTab(
                        webView = webView,
                        onClearCacheStorage = onClearCacheStorage,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
fun InspectDataTab(
    localStorageData: Map<String, String>,
    sessionStorageData: Map<String, String>,
    cookiesData: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Cookies", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Text(
                text = cookiesData.ifBlank { "No cookies found for current URL" },
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        Text("LocalStorage", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        if (localStorageData.isEmpty()) {
            Text("Empty", modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            localStorageData.forEach { (key, value) ->
                StorageItemCard(key, value)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("SessionStorage", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        if (sessionStorageData.isEmpty()) {
            Text("Empty", modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            sessionStorageData.forEach { (key, value) ->
                StorageItemCard(key, value)
            }
        }
    }
}

@Composable
fun StorageItemCard(key: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = key, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ClearDataTab(
    webView: WebView?,
    onClearCacheStorage: (clearStorage: Boolean, clearCookies: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var clearCache by remember { mutableStateOf(true) }
    var clearStorage by remember { mutableStateOf(true) }
    var clearCookies by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = clearCache,
                        onCheckedChange = { clearCache = it }
                    )
                    Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Clear HTTP & Web Cache", fontWeight = FontWeight.SemiBold)
                        Text("Removes cached JS, CSS, and images", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = clearStorage,
                        onCheckedChange = { clearStorage = it }
                    )
                    Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Clear WebStorage / LocalStorage / IndexedDB", fontWeight = FontWeight.SemiBold)
                        Text("Resets PWA client offline state and databases", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = clearCookies,
                        onCheckedChange = { clearCookies = it }
                    )
                    Icon(Icons.Default.Cookie, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Clear Session Cookies", fontWeight = FontWeight.SemiBold)
                        Text("Logs out active web sessions in WebView", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    webView?.reload()
                    onDismiss()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Soft Reload")
            }
            Button(
                onClick = {
                    onClearCacheStorage(clearStorage, clearCookies)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Purge Selected & Reload")
            }
        }
    }
}
