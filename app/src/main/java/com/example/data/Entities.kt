package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_profiles")
data class TestProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val queryParamsJson: String, // JSON string of List<QueryParam>
    val customHeadersJson: String, // JSON string of List<HeaderParam>
    val userAgentType: String = "MOBILE_CHROME",
    val customUserAgent: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "url_history")
data class UrlHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullUrl: String,
    val pageTitle: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val homePageUrl: String = "https://daramet.com/whossein",
    val aiProvider: String = "GEMINI", // "GEMINI", "OPENAI", "CLAUDE", "CUSTOM"
    val aiBaseUrl: String = "",
    val aiApiKey: String = "",
    val aiModelName: String = ""
)

data class QueryParam(
    val id: String = java.util.UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val enabled: Boolean = true
)

data class HeaderParam(
    val id: String = java.util.UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val enabled: Boolean = true
)

enum class LogLevel {
    LOG, INFO, WARNING, ERROR
}

data class ConsoleLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val level: LogLevel,
    val message: String,
    val sourceId: String = "",
    val lineNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class UserAgentPreset(val displayName: String, val userAgentString: String?) {
    DEFAULT_MOBILE("Chrome Mobile (Android)", null),
    PWA_STANDALONE("PWA Standalone Mode", "Mozilla/5.0 (Linux; Android 14; Mobile; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/128.0.6613.127 Mobile Safari/537.36 Display/Standalone"),
    DESKTOP_CHROME("Desktop Chrome", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"),
    CUSTOM("Custom User Agent", "")
}

data class NetworkLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val method: String = "GET",
    val statusCode: Int = 200,
    val statusText: String = "OK",
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String = "",
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String = "",
    val durationMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val resourceType: String = "XHR/Fetch",
    val isMocked: Boolean = false
)

data class MockRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val urlPattern: String = "",
    val method: String = "ALL", // "ALL", "GET", "POST", "PUT", "DELETE", "PATCH"
    val statusCode: Int = 200,
    val responseBody: String = "{\n  \"message\": \"Mocked response\"\n}",
    val contentType: String = "application/json",
    val enabled: Boolean = true
)

enum class PostMessageDirection {
    SENT, RECEIVED
}

data class PostMessageLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val payload: String,
    val origin: String = "*",
    val direction: PostMessageDirection,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeepLinkLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rawUrl: String,
    val scheme: String = "",
    val host: String = "",
    val path: String = "",
    val queryParams: Map<String, String> = emptyMap(),
    val source: String = "INTENT", // "INTENT" or "SIMULATED"
    val timestamp: Long = System.currentTimeMillis()
)

enum class AiChatSender {
    USER, AI, SYSTEM
}

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: AiChatSender,
    val text: String,
    val attachedCodeContext: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class SelectedElementInfo(
    val tagName: String,
    val id: String = "",
    val className: String = "",
    val innerText: String = "",
    val outerHTML: String = "",
    val styles: Map<String, String> = emptyMap()
)

data class DomTreeNode(
    val type: String, // "element" or "text"
    val tagName: String = "",
    val id: String = "",
    val className: String = "",
    val text: String = "",
    val children: List<DomTreeNode> = emptyList()
)

