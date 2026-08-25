package com.example

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BottomSheetType
import com.example.ui.InspectorViewModel
import com.example.ui.components.AiAssistantSheet
import com.example.ui.components.BottomInspectorBar
import com.example.ui.components.ConsoleSheet
import com.example.ui.components.DeepLinkSheet
import com.example.ui.components.DomInspectorSheet
import com.example.ui.components.HeadersSheet
import com.example.ui.components.NetworkSheet
import com.example.ui.components.ParamEditorSheet
import com.example.ui.components.PostMessageSheet
import com.example.ui.components.ProfilesSheet
import com.example.ui.components.StorageSheet
import com.example.ui.components.SupportSheet
import com.example.ui.components.TopControlsBar
import com.example.ui.components.UserAgentSheet
import com.example.ui.components.WebViewContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: InspectorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle initial deep link intent
        viewModel.handleDeepLinkIntent(intent)

        setContent {
            MyApplicationTheme {
                PwaInspectorApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleDeepLinkIntent(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PwaInspectorApp(viewModel: InspectorViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Collapsible Bar & URL Navigation
            TopControlsBar(
                state = uiState,
                webView = webViewInstance,
                onUrlChange = viewModel::onUrlInputChanged,
                onLoadUrl = viewModel::applyUrlAndLoad,
                onToggleExpanded = viewModel::toggleToolbarExpanded,
                onOpenSheet = viewModel::setBottomSheet,
                modifier = Modifier.fillMaxWidth()
            )

            // Full-bleed WebView Container (takes remaining height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                WebViewContainer(
                    activeUrl = uiState.activeUrl,
                    urlReloadTrigger = uiState.urlReloadTrigger,
                    customHeaders = uiState.customHeaders,
                    userAgentPreset = uiState.userAgentPreset,
                    customUserAgent = uiState.customUserAgent,
                    mockRules = uiState.mockRules,
                    onConsoleLog = viewModel::addConsoleLog,
                    onNetworkLog = viewModel::addNetworkLog,
                    onPostMessageReceived = viewModel::addPostMessageLog,
                    onElementSelected = viewModel::setSelectedElement,
                    onLoadingStateChanged = viewModel::onLoadingStateChanged,
                    onPageTitleChanged = viewModel::onPageTitleChanged,
                    onWebViewReady = { webViewInstance = it },
                    modifier = Modifier.fillMaxSize()
                )
                
                if (uiState.isElementPickerActive) {
                    androidx.compose.material3.Surface(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(androidx.compose.ui.Alignment.BottomCenter),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("روی المان مورد نظر در صفحه ضربه بزنید...", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                            androidx.compose.material3.TextButton(onClick = viewModel::toggleElementPicker) {
                                Text("لغو", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Bottom Inspector Dock Bar (webpage content stays strictly above this bar)
            BottomInspectorBar(
                state = uiState,
                onOpenSheet = viewModel::setBottomSheet,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bottom Sheets Handling
        when (uiState.activeBottomSheet) {
            BottomSheetType.PARAMS -> {
                ParamEditorSheet(
                    sheetState = sheetState,
                    queryParams = uiState.queryParams,
                    keySuggestions = uiState.keySuggestions,
                    suggestedValuesMap = uiState.suggestedValuesMap,
                    onAddParam = viewModel::addQueryParam,
                    onUpdateParam = viewModel::updateQueryParam,
                    onRemoveParam = viewModel::removeQueryParam,
                    onApplyAndClose = {
                        viewModel.applyUrlAndLoad()
                        viewModel.setBottomSheet(null)
                    },
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.CONSOLE -> {
                ConsoleSheet(
                    sheetState = sheetState,
                    consoleLogs = uiState.consoleLogs,
                    selectedFilter = uiState.logFilter,
                    searchQuery = uiState.logSearchQuery,
                    onFilterChange = viewModel::setLogFilter,
                    onSearchQueryChange = viewModel::setLogSearchQuery,
                    onClearLogs = viewModel::clearConsoleLogs,
                    onExecuteJs = { js -> webViewInstance?.evaluateJavascript(js, null) },
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.NETWORK -> {
                NetworkSheet(
                    sheetState = sheetState,
                    networkLogs = uiState.networkLogs,
                    selectedMethodFilter = uiState.networkMethodFilter,
                    searchQuery = uiState.networkSearchQuery,
                    mockRules = uiState.mockRules,
                    aiMessages = uiState.networkAiMessages,
                    isAiLoading = uiState.isNetworkAiLoading,
                    aiProvider = appSettings?.aiProvider ?: "GEMINI",
                    aiBaseUrl = appSettings?.aiBaseUrl ?: "",
                    aiApiKey = appSettings?.aiApiKey ?: "",
                    aiModelName = appSettings?.aiModelName ?: "",
                    onFilterMethodChange = viewModel::setNetworkMethodFilter,
                    onSearchQueryChange = viewModel::setNetworkSearchQuery,
                    onClearLogs = viewModel::clearNetworkLogs,
                    onAddMockRule = viewModel::addMockRule,
                    onUpdateMockRule = viewModel::updateMockRule,
                    onToggleMockRule = viewModel::toggleMockRule,
                    onRemoveMockRule = viewModel::removeMockRule,
                    onCreateMockFromLog = viewModel::createMockFromNetworkLog,
                    onSendAiMessage = viewModel::sendNetworkAiMessage,
                    onClearAiMessages = viewModel::clearNetworkAiMessages,
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.STORAGE -> {
                StorageSheet(
                    sheetState = sheetState,
                    webView = webViewInstance,
                    onClearCacheStorage = { storage, cookies ->
                        webViewInstance?.let {
                            viewModel.clearWebViewCache(it, clearStorage = storage, clearCookies = cookies)
                        }
                    },
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.PROFILES -> {
                ProfilesSheet(
                    sheetState = sheetState,
                    profiles = profiles,
                    history = history,
                    bookmarks = bookmarks,
                    currentUrl = uiState.activeUrl,
                    onSaveProfile = viewModel::saveCurrentProfile,
                    onLoadProfile = viewModel::loadProfile,
                    onDeleteProfile = viewModel::deleteProfile,
                    onSelectHistoryUrl = { url ->
                        viewModel.onUrlInputChanged(url)
                        viewModel.applyUrlAndLoad()
                    },
                    onClearHistory = viewModel::clearHistory,
                    onAddBookmark = viewModel::addBookmark,
                    onDeleteBookmark = viewModel::deleteBookmark,
                    onSetHomePage = viewModel::setHomePage,
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.USER_AGENT -> {
                UserAgentSheet(
                    sheetState = sheetState,
                    currentPreset = uiState.userAgentPreset,
                    customUserAgent = uiState.customUserAgent,
                    onSelectUserAgent = { preset, customStr ->
                        viewModel.setUserAgent(preset, customStr)
                        viewModel.applyUrlAndLoad()
                    },
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.HEADERS -> {
                HeadersSheet(
                    sheetState = sheetState,
                    headers = uiState.customHeaders,
                    onAddHeader = viewModel::addHeaderParam,
                    onUpdateHeader = viewModel::updateHeaderParam,
                    onRemoveHeader = viewModel::removeHeaderParam,
                    onApplyAndClose = {
                        viewModel.applyUrlAndLoad()
                        viewModel.setBottomSheet(null)
                    },
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.POST_MESSAGE -> {
                PostMessageSheet(
                    sheetState = sheetState,
                    postMessageLogs = uiState.postMessageLogs,
                    onSendPostMessage = { payload, origin ->
                        viewModel.sendPostMessageToWeb(webViewInstance, payload, origin)
                    },
                    onClearLogs = viewModel::clearPostMessageLogs,
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.DEEP_LINK -> {
                DeepLinkSheet(
                    sheetState = sheetState,
                    deepLinkLogs = uiState.deepLinkLogs,
                    onSimulateDeepLink = viewModel::simulateDeepLink,
                    onLoadUrlInWebView = { url ->
                        viewModel.onUrlInputChanged(url)
                        viewModel.applyUrlAndLoad()
                    },
                    onClearLogs = viewModel::clearDeepLinkLogs,
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.AI_ASSISTANT -> {
                AiAssistantSheet(
                    sheetState = sheetState,
                    messages = uiState.aiMessages,
                    isLoading = uiState.isAiLoading,
                    webView = webViewInstance,
                    aiProvider = appSettings?.aiProvider ?: "GEMINI",
                    aiBaseUrl = appSettings?.aiBaseUrl ?: "",
                    aiApiKey = appSettings?.aiApiKey ?: "",
                    aiModelName = appSettings?.aiModelName ?: "",
                    onSendMessage = { prompt, context ->
                        viewModel.sendAiMessage(prompt, context)
                    },
                    onClearMessages = viewModel::clearAiMessages,
                    onUpdateSettings = viewModel::updateAiSettings,
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.DOM_INSPECTOR -> {
                DomInspectorSheet(
                    sheetState = sheetState,
                    selectedElement = uiState.selectedElement,
                    domTreeRoot = uiState.domTreeRoot,
                    isElementPickerActive = uiState.isElementPickerActive,
                    webView = webViewInstance,
                    onToggleElementPicker = viewModel::toggleElementPicker,
                    onSetDomTreeRoot = viewModel::setDomTreeRoot,
                    onSelectElement = viewModel::setSelectedElement,
                    onAskAiAboutElement = { element ->
                        val codeContext = "Element: <${element.tagName} id=\"${element.id}\" class=\"${element.className}\">\nOuterHTML: ${element.outerHTML}\nStyles: ${element.styles}"
                        viewModel.setBottomSheet(BottomSheetType.AI_ASSISTANT)
                        viewModel.sendAiMessage("این المان وب‌سایت رو تحلیل کن و استایل و کدهای اون رو بررسی کن:", codeContext)
                    },
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            BottomSheetType.SUPPORT -> {
                SupportSheet(
                    sheetState = sheetState,
                    onLoadUrlInApp = { url ->
                        viewModel.onUrlInputChanged(url)
                        viewModel.applyUrlAndLoad()
                    },
                    onDismiss = { viewModel.setBottomSheet(null) }
                )
            }

            null -> {}
        }
    }
}
// test commit trigger
