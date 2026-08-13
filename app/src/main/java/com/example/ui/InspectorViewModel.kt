package com.example.ui

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ConsoleLog
import com.example.data.HeaderParam
import com.example.data.LogLevel
import com.example.data.MockRule
import com.example.data.NetworkLog
import com.example.data.PwaRepository
import com.example.data.QueryParam
import com.example.data.TestProfile
import com.example.data.UserAgentPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

import com.example.data.PostMessageDirection
import com.example.data.PostMessageLog
import com.example.data.DeepLinkLog
import com.example.data.AiChatMessage
import com.example.data.AiChatSender
import com.example.data.SelectedElementInfo
import com.example.data.DomTreeNode
import com.example.data.AiService
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class InspectorUiState(
    val urlInput: String = "https://daramet.com/whossein",
    val activeUrl: String = "",
    val queryParams: List<QueryParam> = emptyList(),
    val customHeaders: List<HeaderParam> = emptyList(),
    val userAgentPreset: UserAgentPreset = UserAgentPreset.DEFAULT_MOBILE,
    val customUserAgent: String = "",
    val consoleLogs: List<ConsoleLog> = emptyList(),
    val logFilter: LogLevel? = null,
    val logSearchQuery: String = "",
    val isLoading: Boolean = false,
    val loadingProgress: Int = 0,
    val pageTitle: String = "",
    val isToolbarExpanded: Boolean = false,
    val activeBottomSheet: BottomSheetType? = null,
    val networkLogs: List<NetworkLog> = emptyList(),
    val networkMethodFilter: String? = null,
    val networkSearchQuery: String = "",
    val mockRules: List<MockRule> = emptyList(),
    val postMessageLogs: List<PostMessageLog> = emptyList(),
    val deepLinkLogs: List<DeepLinkLog> = emptyList(),
    val aiMessages: List<AiChatMessage> = emptyList(),
    val isAiLoading: Boolean = false,
    val networkAiMessages: List<AiChatMessage> = emptyList(),
    val isNetworkAiLoading: Boolean = false,
    val selectedElement: SelectedElementInfo? = null,
    val domTreeRoot: DomTreeNode? = null,
    val isElementPickerActive: Boolean = false,
    val keySuggestions: List<String> = listOf("theme", "mode", "lang", "v", "env", "debug", "token", "user_id"),
    val suggestedValuesMap: Map<String, List<String>> = mapOf(
        "theme" to listOf("dark", "light", "system", "auto"),
        "mode" to listOf("pwa_test", "standalone", "webview", "debug"),
        "env" to listOf("dev", "staging", "prod"),
        "lang" to listOf("fa", "en", "ar", "es")
    )
)

enum class BottomSheetType {
    PARAMS, HEADERS, CONSOLE, STORAGE, PROFILES, USER_AGENT, NETWORK, POST_MESSAGE, DEEP_LINK, AI_ASSISTANT, DOM_INSPECTOR, SUPPORT
}

class InspectorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PwaRepository(AppDatabase.getDatabase(application).pwaDao())

    private val _uiState = MutableStateFlow(InspectorUiState())
    val uiState: StateFlow<InspectorUiState> = _uiState.asStateFlow()

    val profiles: StateFlow<List<TestProfile>> = repository.allProfiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history = repository.urlHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bookmarks = repository.allBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appSettings = repository.appSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            repository.appSettings.collect { settings ->
                if (settings != null && _uiState.value.urlInput == "https://daramet.com/whossein") {
                    _uiState.update { it.copy(urlInput = settings.homePageUrl) }
                    rebuildActiveUrl()
                }
            }
        }
        rebuildActiveUrl()
    }

    fun onUrlInputChanged(newUrl: String) {
        _uiState.update { it.copy(urlInput = newUrl) }
    }

    fun applyUrlAndLoad() {
        rebuildActiveUrl()
    }

    fun toggleToolbarExpanded() {
        _uiState.update { it.copy(isToolbarExpanded = !it.isToolbarExpanded) }
    }

    fun setBottomSheet(type: BottomSheetType?) {
        _uiState.update { it.copy(activeBottomSheet = type) }
    }

    fun addQueryParam(key: String = "", value: String = "") {
        val newParam = QueryParam(key = key, value = value, enabled = true)
        _uiState.update { state ->
            state.copy(queryParams = state.queryParams + newParam)
        }
        updateSuggestions(key, value)
    }

    fun updateQueryParam(id: String, key: String, value: String, enabled: Boolean) {
        _uiState.update { state ->
            val updated = state.queryParams.map { param ->
                if (param.id == id) param.copy(key = key, value = value, enabled = enabled)
                else param
            }
            state.copy(queryParams = updated)
        }
        updateSuggestions(key, value)
    }

    fun removeQueryParam(id: String) {
        _uiState.update { state ->
            state.copy(queryParams = state.queryParams.filterNot { it.id == id })
        }
    }

    fun toggleQueryParam(id: String) {
        _uiState.update { state ->
            val updated = state.queryParams.map { param ->
                if (param.id == id) param.copy(enabled = !param.enabled)
                else param
            }
            state.copy(queryParams = updated)
        }
    }

    fun addHeaderParam(key: String = "", value: String = "") {
        _uiState.update { state ->
            state.copy(customHeaders = state.customHeaders + HeaderParam(key = key, value = value))
        }
    }

    fun updateHeaderParam(id: String, key: String, value: String, enabled: Boolean) {
        _uiState.update { state ->
            val updated = state.customHeaders.map { param ->
                if (param.id == id) param.copy(key = key, value = value, enabled = enabled)
                else param
            }
            state.copy(customHeaders = updated)
        }
    }

    fun removeHeaderParam(id: String) {
        _uiState.update { state ->
            state.copy(customHeaders = state.customHeaders.filterNot { it.id == id })
        }
    }

    fun setUserAgent(preset: UserAgentPreset, customString: String = "") {
        _uiState.update { it.copy(userAgentPreset = preset, customUserAgent = customString) }
    }

    fun addConsoleLog(level: LogLevel, message: String, sourceId: String = "", lineNumber: Int = 0) {
        val newLog = ConsoleLog(
            level = level,
            message = message,
            sourceId = sourceId,
            lineNumber = lineNumber
        )
        _uiState.update { state ->
            // keep up to last 300 logs
            val current = state.consoleLogs
            val nextList = if (current.size >= 300) current.drop(1) + newLog else current + newLog
            state.copy(consoleLogs = nextList)
        }
    }

    fun clearConsoleLogs() {
        _uiState.update { it.copy(consoleLogs = emptyList()) }
    }

    fun setLogFilter(level: LogLevel?) {
        _uiState.update { it.copy(logFilter = level) }
    }

    fun setLogSearchQuery(query: String) {
        _uiState.update { it.copy(logSearchQuery = query) }
    }

    fun addNetworkLog(log: NetworkLog) {
        _uiState.update { state ->
            val current = state.networkLogs
            val nextList = if (current.size >= 250) current.drop(1) + log else current + log
            state.copy(networkLogs = nextList)
        }
    }

    fun clearNetworkLogs() {
        _uiState.update { it.copy(networkLogs = emptyList()) }
    }

    fun setNetworkSearchQuery(query: String) {
        _uiState.update { it.copy(networkSearchQuery = query) }
    }

    fun setNetworkMethodFilter(method: String?) {
        _uiState.update { it.copy(networkMethodFilter = method) }
    }

    fun addMockRule(
        urlPattern: String = "",
        method: String = "ALL",
        statusCode: Int = 200,
        responseBody: String = "{\n  \"message\": \"Mocked response\"\n}",
        contentType: String = "application/json"
    ) {
        val rule = MockRule(
            urlPattern = urlPattern,
            method = method,
            statusCode = statusCode,
            responseBody = responseBody,
            contentType = contentType,
            enabled = true
        )
        _uiState.update { state ->
            state.copy(mockRules = state.mockRules + rule)
        }
    }

    fun updateMockRule(
        id: String,
        urlPattern: String,
        method: String,
        statusCode: Int,
        responseBody: String,
        contentType: String,
        enabled: Boolean
    ) {
        _uiState.update { state ->
            val updated = state.mockRules.map { rule ->
                if (rule.id == id) {
                    rule.copy(
                        urlPattern = urlPattern,
                        method = method,
                        statusCode = statusCode,
                        responseBody = responseBody,
                        contentType = contentType,
                        enabled = enabled
                    )
                } else rule
            }
            state.copy(mockRules = updated)
        }
    }

    fun toggleMockRule(id: String) {
        _uiState.update { state ->
            val updated = state.mockRules.map { rule ->
                if (rule.id == id) rule.copy(enabled = !rule.enabled)
                else rule
            }
            state.copy(mockRules = updated)
        }
    }

    fun removeMockRule(id: String) {
        _uiState.update { state ->
            state.copy(mockRules = state.mockRules.filterNot { it.id == id })
        }
    }

    fun createMockFromNetworkLog(log: NetworkLog) {
        val rule = MockRule(
            urlPattern = log.url,
            method = if (log.method.isNotBlank()) log.method else "ALL",
            statusCode = if (log.statusCode != 0) log.statusCode else 200,
            responseBody = if (log.responseBody.isNotBlank()) log.responseBody else "{\n  \"status\": \"success\"\n}",
            contentType = if (log.responseBody.trim().startsWith("{") || log.responseBody.trim().startsWith("[")) "application/json" else "text/plain",
            enabled = true
        )
        _uiState.update { state ->
            state.copy(mockRules = state.mockRules + rule)
        }
    }

    fun onLoadingStateChanged(isLoading: Boolean, progress: Int) {
        _uiState.update { it.copy(isLoading = isLoading, loadingProgress = progress) }
    }

    fun onPageTitleChanged(title: String, url: String) {
        _uiState.update { it.copy(pageTitle = title) }
        viewModelScope.launch {
            repository.addHistory(url, title)
        }
    }

    fun addPostMessageLog(payload: String, origin: String = "*", direction: PostMessageDirection = PostMessageDirection.RECEIVED) {
        val log = PostMessageLog(
            payload = payload,
            origin = origin,
            direction = direction
        )
        _uiState.update { state ->
            state.copy(postMessageLogs = state.postMessageLogs + log)
        }
    }

    fun sendPostMessageToWeb(webView: WebView?, payload: String, origin: String = "*") {
        if (payload.isBlank() || webView == null) return

        // Dispatch postMessage event into WebView window
        val escapedPayload = org.json.JSONObject.quote(payload)
        val escapedOrigin = if (origin.isBlank()) "'*'" else org.json.JSONObject.quote(origin)
        val script = """
            (function() {
                try {
                    let rawData = $escapedPayload;
                    let parsedData = rawData;
                    try { parsedData = JSON.parse(rawData); } catch(e) {}
                    
                    window.postMessage(parsedData, $escapedOrigin);
                    
                    const evt = new MessageEvent('message', {
                        data: parsedData,
                        origin: $escapedOrigin === '*' ? window.location.origin : $escapedOrigin
                    });
                    window.dispatchEvent(evt);
                } catch(err) {
                    console.error('Failed to dispatch postMessage from Android:', err);
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(script, null)
        addPostMessageLog(payload = payload, origin = origin, direction = PostMessageDirection.SENT)
    }

    fun clearPostMessageLogs() {
        _uiState.update { it.copy(postMessageLogs = emptyList()) }
    }

    fun clearWebViewCache(webView: WebView, clearStorage: Boolean, clearCookies: Boolean) {
        webView.clearCache(true)
        if (clearStorage) {
            WebStorage.getInstance().deleteAllData()
        }
        if (clearCookies) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
        addConsoleLog(LogLevel.INFO, "Cleared Cache/Storage (Cache: true, Storage: $clearStorage, Cookies: $clearCookies)")
        webView.reload()
    }

    fun saveCurrentProfile(profileName: String) {
        if (profileName.isBlank()) return
        val currentState = uiState.value

        val paramsJson = JSONArray().apply {
            currentState.queryParams.forEach {
                put(JSONObject().apply {
                    put("key", it.key)
                    put("value", it.value)
                    put("enabled", it.enabled)
                })
            }
        }.toString()

        val headersJson = JSONArray().apply {
            currentState.customHeaders.forEach {
                put(JSONObject().apply {
                    put("key", it.key)
                    put("value", it.value)
                    put("enabled", it.enabled)
                })
            }
        }.toString()

        val profile = TestProfile(
            name = profileName,
            baseUrl = currentState.urlInput,
            queryParamsJson = paramsJson,
            customHeadersJson = headersJson,
            userAgentType = currentState.userAgentPreset.name,
            customUserAgent = currentState.customUserAgent
        )

        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun loadProfile(profile: TestProfile) {
        val parsedParams = mutableListOf<QueryParam>()
        try {
            val jsonArray = JSONArray(profile.queryParamsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                parsedParams.add(
                    QueryParam(
                        key = obj.optString("key"),
                        value = obj.optString("value"),
                        enabled = obj.optBoolean("enabled", true)
                    )
                )
            }
        } catch (_: Exception) {}

        val parsedHeaders = mutableListOf<HeaderParam>()
        try {
            val jsonArray = JSONArray(profile.customHeadersJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                parsedHeaders.add(
                    HeaderParam(
                        key = obj.optString("key"),
                        value = obj.optString("value"),
                        enabled = obj.optBoolean("enabled", true)
                    )
                )
            }
        } catch (_: Exception) {}

        val preset = try {
            UserAgentPreset.valueOf(profile.userAgentType)
        } catch (_: Exception) {
            UserAgentPreset.DEFAULT_MOBILE
        }

        _uiState.update {
            it.copy(
                urlInput = profile.baseUrl,
                queryParams = parsedParams,
                customHeaders = parsedHeaders,
                userAgentPreset = preset,
                customUserAgent = profile.customUserAgent,
                activeBottomSheet = null
            )
        }
        rebuildActiveUrl()
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            repository.deleteProfile(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun addBookmark(title: String, url: String) {
        viewModelScope.launch {
            repository.addBookmark(title, url)
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun setHomePage(url: String) {
        viewModelScope.launch {
            repository.updateHomePage(url, appSettings.value)
        }
    }

    private fun rebuildActiveUrl() {
        val currentState = uiState.value
        var rawUrl = currentState.urlInput.trim()
        if (rawUrl.isEmpty()) return

        if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://") && !rawUrl.startsWith("file://")) {
            rawUrl = "https://$rawUrl"
        }

        val active = try {
            val uri = URI(rawUrl)
            val existingQuery = uri.rawQuery
            val existingParamsMap = mutableMapOf<String, String>()

            if (!existingQuery.isNull_orEmpty()) {
                existingQuery.split("&").forEach { pair ->
                    val parts = pair.split("=", limit = 2)
                    if (parts.size == 2) {
                        existingParamsMap[parts[0]] = parts[1]
                    } else if (parts.isNotEmpty()) {
                        existingParamsMap[parts[0]] = ""
                    }
                }
            }

            val baseWithoutQuery = if (rawUrl.contains("?")) rawUrl.substringBefore("?") else rawUrl

            val activeParams = currentState.queryParams.filter { it.enabled && it.key.isNotBlank() }
            if (activeParams.isEmpty()) {
                baseWithoutQuery
            } else {
                val queryString = activeParams.joinToString("&") { param ->
                    val encodedKey = java.net.URLEncoder.encode(param.key, "UTF-8")
                    val encodedVal = java.net.URLEncoder.encode(param.value, "UTF-8")
                    "$encodedKey=$encodedVal"
                }
                if (baseWithoutQuery.contains("#")) {
                    val beforeHash = baseWithoutQuery.substringBefore("#")
                    val afterHash = baseWithoutQuery.substringAfter("#")
                    "$beforeHash?$queryString#$afterHash"
                } else {
                    "$baseWithoutQuery?$queryString"
                }
            }
        } catch (e: Exception) {
            rawUrl
        }

        _uiState.update { it.copy(activeUrl = active) }
    }

    fun handleDeepLinkIntent(intent: Intent?) {
        if (intent == null || intent.action != Intent.ACTION_VIEW) return
        val data = intent.data ?: return
        processDeepLinkUri(data, source = "INTENT")
    }

    fun simulateDeepLink(url: String) {
        if (url.isBlank()) return
        try {
            val uri = Uri.parse(url)
            processDeepLinkUri(uri, source = "SIMULATED")
        } catch (_: Exception) {}
    }

    private fun processDeepLinkUri(uri: Uri, source: String) {
        val scheme = uri.scheme ?: ""
        val host = uri.host ?: ""
        val path = uri.path ?: ""
        val queryMap = mutableMapOf<String, String>()
        try {
            uri.queryParameterNames.forEach { name ->
                queryMap[name] = uri.getQueryParameter(name) ?: ""
            }
        } catch (_: Exception) {}

        val log = DeepLinkLog(
            rawUrl = uri.toString(),
            scheme = scheme,
            host = host,
            path = path,
            queryParams = queryMap,
            source = source
        )

        _uiState.update { state ->
            state.copy(
                deepLinkLogs = state.deepLinkLogs + log,
                activeBottomSheet = BottomSheetType.DEEP_LINK
            )
        }

        addConsoleLog(LogLevel.INFO, "Deep Link Received [$source]: ${uri.toString()}")

        // If it's a web URL (http/https), optionally update URL input
        if (scheme == "http" || scheme == "https") {
            onUrlInputChanged(uri.toString())
        }
    }

    fun clearDeepLinkLogs() {
        _uiState.update { it.copy(deepLinkLogs = emptyList()) }
    }

    fun sendAiMessage(prompt: String, attachedContext: String? = null) {
        if (prompt.isBlank()) return
        val userMsg = AiChatMessage(
            sender = AiChatSender.USER,
            text = prompt,
            attachedCodeContext = attachedContext
        )

        _uiState.update { state ->
            state.copy(
                aiMessages = state.aiMessages + userMsg,
                isAiLoading = true
            )
        }

        viewModelScope.launch {
            val appSettings = appSettings.value
            val provider = appSettings?.aiProvider ?: "GEMINI"
            val baseUrl = appSettings?.aiBaseUrl ?: ""
            val apiKey = appSettings?.aiApiKey ?: ""
            val modelName = appSettings?.aiModelName ?: ""
            
            val result = AiService.queryWebAssistant(prompt, attachedContext, provider, baseUrl, apiKey, modelName)
            val aiMsg = if (result.isSuccess) {
                AiChatMessage(
                    sender = AiChatSender.AI,
                    text = result.getOrDefault("No response received.")
                )
            } else {
                AiChatMessage(
                    sender = AiChatSender.SYSTEM,
                    text = "⚠️ Error: ${result.exceptionOrNull()?.message}"
                )
            }

            _uiState.update { state ->
                state.copy(
                    aiMessages = state.aiMessages + aiMsg,
                    isAiLoading = false
                )
            }
        }
    }

    fun clearAiMessages() {
        _uiState.update { it.copy(aiMessages = emptyList()) }
    }

    fun sendNetworkAiMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = AiChatMessage(
            sender = AiChatSender.USER,
            text = prompt
        )

        _uiState.update { state ->
            state.copy(
                networkAiMessages = state.networkAiMessages + userMsg,
                isNetworkAiLoading = true
            )
        }

        viewModelScope.launch {
            val logsContext = _uiState.value.networkLogs.takeLast(5).joinToString("\n") {
                "${it.method} ${it.url} -> ${it.statusCode}"
            }

            val appSettings = appSettings.value
            val provider = appSettings?.aiProvider ?: "GEMINI"
            val baseUrl = appSettings?.aiBaseUrl ?: ""
            val apiKey = appSettings?.aiApiKey ?: ""
            val modelName = appSettings?.aiModelName ?: ""

            val result = AiService.generateNetworkMock(prompt, logsContext, provider, baseUrl, apiKey, modelName)
            val aiMsg = if (result.isSuccess) {
                AiChatMessage(
                    sender = AiChatSender.AI,
                    text = result.getOrDefault("No mock generated.")
                )
            } else {
                AiChatMessage(
                    sender = AiChatSender.SYSTEM,
                    text = "⚠️ Error: ${result.exceptionOrNull()?.message}"
                )
            }

            _uiState.update { state ->
                state.copy(
                    networkAiMessages = state.networkAiMessages + aiMsg,
                    isNetworkAiLoading = false
                )
            }
        }
    }

    fun clearNetworkAiMessages() {
        _uiState.update { it.copy(networkAiMessages = emptyList()) }
    }

    fun setSelectedElement(info: SelectedElementInfo) {
        _uiState.update { 
            it.copy(
                selectedElement = info, 
                isElementPickerActive = false,
                activeBottomSheet = BottomSheetType.DOM_INSPECTOR
            ) 
        }
    }

    fun setDomTreeRoot(root: DomTreeNode?) {
        _uiState.update { it.copy(domTreeRoot = root) }
    }

    fun toggleElementPicker() {
        _uiState.update { state ->
            val newState = !state.isElementPickerActive
            state.copy(
                isElementPickerActive = newState,
                activeBottomSheet = if (newState) null else state.activeBottomSheet
            )
        }
    }

    fun updateAiSettings(provider: String, baseUrl: String, apiKey: String, modelName: String) {
        viewModelScope.launch {
            val current = appSettings.value
            val updated = current?.copy(
                aiProvider = provider,
                aiBaseUrl = baseUrl,
                aiApiKey = apiKey,
                aiModelName = modelName
            ) ?: com.example.data.AppSettings(
                id = 1,
                aiProvider = provider,
                aiBaseUrl = baseUrl,
                aiApiKey = apiKey,
                aiModelName = modelName
            )
            repository.updateSettings(updated)
        }
    }

    private fun updateSuggestions(key: String, value: String) {
        if (key.isBlank()) return
        _uiState.update { state ->
            val keys = if (!state.keySuggestions.contains(key)) state.keySuggestions + key else state.keySuggestions
            val currentVals = state.suggestedValuesMap[key] ?: emptyList()
            val updatedVals = if (value.isNotBlank() && !currentVals.contains(value)) currentVals + value else currentVals
            val newMap = state.suggestedValuesMap.toMutableMap()
            if (updatedVals.isNotEmpty()) {
                newMap[key] = updatedVals
            }
            state.copy(keySuggestions = keys, suggestedValuesMap = newMap)
        }
    }

    private fun String?.isNull_orEmpty(): Boolean = this == null || this.trim().isEmpty()
}
