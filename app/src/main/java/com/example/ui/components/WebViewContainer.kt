package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.HeaderParam
import com.example.data.LogLevel
import com.example.data.NetworkLog
import com.example.data.SelectedElementInfo
import com.example.data.UserAgentPreset

class NetworkInspectorBridge(
    private val onLog: (NetworkLog) -> Unit
) {
    @android.webkit.JavascriptInterface
    fun logNetworkRequest(
        url: String,
        method: String,
        statusCode: Int,
        statusText: String,
        reqHeadersJson: String,
        reqBody: String,
        resHeadersJson: String,
        resBody: String,
        durationMs: Long,
        isMocked: Boolean = false
    ) {
        val reqHeaders = parseHeadersJson(reqHeadersJson)
        val resHeaders = parseHeadersJson(resHeadersJson)

        val log = NetworkLog(
            url = url,
            method = method.ifBlank { "GET" },
            statusCode = if (statusCode == 0) 200 else statusCode,
            statusText = statusText.ifBlank { "OK" },
            requestHeaders = reqHeaders,
            requestBody = reqBody,
            responseHeaders = resHeaders,
            responseBody = resBody,
            durationMs = durationMs,
            resourceType = if (isMocked) "XHR/Fetch (Mocked)" else "XHR/Fetch",
            isMocked = isMocked
        )
        onLog(log)
    }

    private fun parseHeadersJson(jsonStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val obj = org.json.JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optString(key)
            }
        } catch (_: Exception) {}
        return map
    }
}

private const val NETWORK_INSPECTOR_JS = """(function() {
    if (!window.__mockRules) { window.__mockRules = []; }
    if (window.__networkInspectorInjected) return;
    window.__networkInspectorInjected = true;
    
    function findMatchingMock(url, method) {
        if (!window.__mockRules || !Array.isArray(window.__mockRules)) return null;
        for (let rule of window.__mockRules) {
            if (!rule.enabled) continue;
            if (rule.method && rule.method !== 'ALL' && rule.method.toUpperCase() !== method.toUpperCase()) continue;
            if (!rule.urlPattern || rule.urlPattern === '*' || (url && url.toLowerCase().includes(rule.urlPattern.toLowerCase()))) {
                return rule;
            }
        }
        return null;
    }
    
    if (window.fetch) {
        const origFetch = window.fetch;
        window.fetch = async function(...args) {
            let url = typeof args[0] === 'string' ? args[0] : (args[0] && args[0].url ? args[0].url : '');
            let options = args[1] || {};
            let method = (options.method || (args[0] && args[0].method) || 'GET').toUpperCase();
            let reqBody = '';
            if (options.body) {
                if (typeof options.body === 'string') reqBody = options.body;
                else if (options.body instanceof FormData) reqBody = '[FormData]';
                else if (options.body instanceof Blob) reqBody = '[Blob]';
                else if (options.body instanceof ArrayBuffer) reqBody = '[ArrayBuffer]';
                else {
                    try { reqBody = JSON.stringify(options.body) || ''; } catch(e) {}
                }
            }
            let reqHeaders = {};
            if (options.headers) {
                try {
                    if (typeof options.headers.forEach === 'function') {
                        options.headers.forEach((v, k) => reqHeaders[k] = v);
                    } else if (typeof options.headers === 'object') {
                        reqHeaders = options.headers;
                    }
                } catch(e) {}
            }
            
            const mockRule = findMatchingMock(url, method);
            if (mockRule) {
                try {
                    if (window.AndroidNetworkInspector) {
                        window.AndroidNetworkInspector.logNetworkRequest(
                            url, method, mockRule.statusCode, 'OK (Mocked)',
                            JSON.stringify(reqHeaders), reqBody,
                            JSON.stringify({'Content-Type': mockRule.contentType || 'application/json'}),
                            mockRule.responseBody || '', 0, true
                        );
                    }
                } catch(e) {}
                return new Response(mockRule.responseBody || '', {
                    status: mockRule.statusCode || 200,
                    statusText: 'OK (Mocked)',
                    headers: {'Content-Type': mockRule.contentType || 'application/json'}
                });
            }
            
            const startTime = Date.now();
            try {
                const response = await origFetch.apply(window, args);
                
                // Do not block fetch! Do logging in background.
                setTimeout(async () => {
                    let clone = null;
                    try { clone = response.clone(); } catch(e) {}
                    let resText = '';
                    if (clone) {
                        try { 
                            const ct = clone.headers.get('content-type') || '';
                            if (ct.includes('application/json') || ct.includes('text/')) {
                                resText = await clone.text(); 
                            } else {
                                resText = '[Binary/Other Data]';
                            }
                        } catch(e) {}
                    }
                    
                    let resHeaders = {};
                    try {
                        if (typeof response.headers.forEach === 'function') {
                            response.headers.forEach((v, k) => resHeaders[k] = v);
                        }
                    } catch(e) {}
                    
                    try {
                        if (window.AndroidNetworkInspector) {
                            window.AndroidNetworkInspector.logNetworkRequest(
                                url, method, response.status, response.statusText || 'OK',
                                JSON.stringify(reqHeaders), reqBody,
                                JSON.stringify(resHeaders), resText.substring(0, 100000),
                                Date.now() - startTime, false
                            );
                        }
                    } catch(e) {}
                }, 10);
                
                return response;
            } catch(err) {
                try {
                    if (window.AndroidNetworkInspector) {
                        window.AndroidNetworkInspector.logNetworkRequest(
                            url, method, 0, err.message || 'Failed',
                            JSON.stringify(reqHeaders), reqBody,
                            '{}', '',
                            Date.now() - startTime, false
                        );
                    }
                } catch(e) {}
                throw err;
            }
        };
    }
    
    if (window.XMLHttpRequest) {
        const origOpen = XMLHttpRequest.prototype.open;
        const origSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;
        const origSend = XMLHttpRequest.prototype.send;
        
        XMLHttpRequest.prototype.open = function(method, url) {
            this._reqMethod = (method || 'GET').toUpperCase();
            this._reqUrl = url || '';
            this._reqHeaders = {};
            return origOpen.apply(this, arguments);
        };
        
        XMLHttpRequest.prototype.setRequestHeader = function(header, value) {
            if (this._reqHeaders) {
                this._reqHeaders[header] = value;
            }
            return origSetRequestHeader.apply(this, arguments);
        };
        
        XMLHttpRequest.prototype.send = function(body) {
            let reqBody = '';
            if (body) {
                if (typeof body === 'string') reqBody = body;
                else if (body instanceof FormData) reqBody = '[FormData]';
                else if (body instanceof Blob) reqBody = '[Blob]';
                else if (body instanceof ArrayBuffer) reqBody = '[ArrayBuffer]';
                else {
                    try { reqBody = String(body); } catch(e) {}
                }
            }
            
            const url = this._reqUrl || '';
            const method = this._reqMethod || 'GET';
            const reqHeaders = this._reqHeaders || {};
            const startTime = Date.now();
            
            const mockRule = findMatchingMock(url, method);
            if (mockRule) {
                try {
                    if (window.AndroidNetworkInspector) {
                        window.AndroidNetworkInspector.logNetworkRequest(
                            url, method, mockRule.statusCode, 'OK (Mocked)',
                            JSON.stringify(reqHeaders), reqBody,
                            JSON.stringify({'Content-Type': mockRule.contentType || 'application/json'}),
                            mockRule.responseBody || '', 0, true
                        );
                    }
                } catch(e) {}
                setTimeout(() => {
                    try {
                        Object.defineProperty(this, 'status', { value: mockRule.statusCode || 200, writable: true });
                        Object.defineProperty(this, 'statusText', { value: 'OK (Mocked)', writable: true });
                        Object.defineProperty(this, 'responseText', { value: mockRule.responseBody || '', writable: true });
                        Object.defineProperty(this, 'response', { value: mockRule.responseBody || '', writable: true });
                        Object.defineProperty(this, 'readyState', { value: 4, writable: true });
                        if (typeof this.onreadystatechange === 'function') this.onreadystatechange();
                        if (typeof this.onload === 'function') this.onload();
                        this.dispatchEvent(new Event('readystatechange'));
                        this.dispatchEvent(new Event('load'));
                        this.dispatchEvent(new Event('loadend'));
                    } catch(e) {}
                }, 10);
                return;
            }
            
            this.addEventListener('loadend', function() {
                try {
                    let resHeadersStr = this.getAllResponseHeaders() || '';
                    let resHeaders = {};
                    resHeadersStr.split('
').forEach(line => {
                        let parts = line.split(': ');
                        if (parts.length === 2) resHeaders[parts[0]] = parts[1];
                    });
                    
                    let resText = '';
                    if (!this.responseType || this.responseType === 'text' || this.responseType === '') {
                        resText = (this.responseText || '').substring(0, 100000);
                    } else {
                        resText = '[' + this.responseType + ']';
                    }
                    
                    if (window.AndroidNetworkInspector) {
                        window.AndroidNetworkInspector.logNetworkRequest(
                            url, method, this.status, this.statusText || '',
                            JSON.stringify(reqHeaders), reqBody,
                            JSON.stringify(resHeaders), resText,
                            Date.now() - startTime, false
                        );
                    }
                } catch(e) {}
            });
            
            return origSend.apply(this, arguments);
        };
    }
})();"""

class PostMessageBridge(
    private val onPostMessageReceived: (payload: String, origin: String) -> Unit
) {
    @android.webkit.JavascriptInterface
    fun postMessage(data: String, origin: String = "*") {
        onPostMessageReceived(data, origin)
    }
}

class DOMInspectorBridge(
    private val onElementSelected: (SelectedElementInfo) -> Unit
) {
    @android.webkit.JavascriptInterface
    fun onElementSelected(jsonStr: String) {
        try {
            val json = org.json.JSONObject(jsonStr)
            val tagName = json.optString("tagName", "element")
            val id = json.optString("id", "")
            val className = json.optString("className", "")
            val innerText = json.optString("innerText", "")
            val outerHTML = json.optString("outerHTML", "")

            val stylesMap = mutableMapOf<String, String>()
            val stylesObj = json.optJSONObject("styles")
            stylesObj?.keys()?.forEach { key ->
                stylesMap[key] = stylesObj.optString(key, "")
            }

            onElementSelected(
                SelectedElementInfo(
                    tagName = tagName,
                    id = id,
                    className = className,
                    innerText = innerText,
                    outerHTML = outerHTML,
                    styles = stylesMap
                )
            )
        } catch (_: Exception) {}
    }
}

private const val POST_MESSAGE_INSPECTOR_JS = """
(function() {
    if (window.__postMessageInspectorInjected) return;
    window.__postMessageInspectorInjected = true;

    window.addEventListener('message', function(event) {
        try {
            if (!event || event.data === undefined || event.data === null) return;
            let dataStr = typeof event.data === 'string' ? event.data : JSON.stringify(event.data);
            if (window.AndroidPostMessage) {
                window.AndroidPostMessage.postMessage(dataStr, event.origin || '*');
            }
        } catch(e) {}
    });

    if (!window.postMessageToAndroid) {
        window.postMessageToAndroid = function(data, origin) {
            let dataStr = typeof data === 'string' ? data : JSON.stringify(data);
            if (window.AndroidPostMessage) {
                window.AndroidPostMessage.postMessage(dataStr, origin || window.location.origin || '*');
            }
        };
    }
})();
"""

private const val ELEMENT_INSPECTOR_JS = """
(function() {
    if (window.__elementInspectorInjected) return;
    window.__elementInspectorInjected = true;

    window.__activateElementPicker = function() {
        let prevOutline = '';
        let targetEl = null;

        function handleMouseOver(e) {
            if (targetEl) targetEl.style.outline = prevOutline;
            targetEl = e.target;
            if (targetEl) {
                prevOutline = targetEl.style.outline;
                targetEl.style.outline = '2px dashed #FF5722';
            }
        }

        function handleClick(e) {
            e.preventDefault();
            e.stopPropagation();

            if (targetEl) {
                targetEl.style.outline = prevOutline;
                let computed = window.getComputedStyle(targetEl);
                let info = {
                    tagName: targetEl.tagName.toLowerCase(),
                    id: targetEl.id || '',
                    className: typeof targetEl.className === 'string' ? targetEl.className : '',
                    innerText: (targetEl.innerText || '').trim().substring(0, 200),
                    outerHTML: (targetEl.outerHTML || '').substring(0, 800),
                    styles: {
                        display: computed.display,
                        position: computed.position,
                        color: computed.color,
                        backgroundColor: computed.backgroundColor,
                        fontSize: computed.fontSize,
                        fontFamily: computed.fontFamily,
                        margin: computed.margin,
                        padding: computed.padding,
                        width: computed.width,
                        height: computed.height,
                        flexDirection: computed.flexDirection
                    }
                };

                if (window.AndroidDOMInspector) {
                    window.AndroidDOMInspector.onElementSelected(JSON.stringify(info));
                }
            }

            document.removeEventListener('mouseover', handleMouseOver, true);
            document.removeEventListener('click', handleClick, true);
        }

        document.addEventListener('mouseover', handleMouseOver, true);
        document.addEventListener('click', handleClick, true);
    };

    window.__extractDomTree = function() {
        function parseNode(node, depth) {
            if (!node || depth > 4) return null;
            if (node.nodeType === 3) {
                let txt = node.textContent.trim();
                if (!txt) return null;
                return { type: 'text', text: txt.substring(0, 50) };
            }
            if (node.nodeType !== 1) return null;
            let children = [];
            for (let i = 0; i < node.childNodes.length; i++) {
                let child = parseNode(node.childNodes[i], depth + 1);
                if (child) children.push(child);
                if (children.length >= 12) break;
            }
            return {
                type: 'element',
                tagName: node.tagName.toLowerCase(),
                id: node.id || '',
                className: typeof node.className === 'string' ? node.className : '',
                children: children
            };
        }
        return JSON.stringify(parseNode(document.body, 0));
    };
})();
"""

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    activeUrl: String,
    urlReloadTrigger: Int = 0,
    customHeaders: List<HeaderParam>,
    userAgentPreset: UserAgentPreset,
    customUserAgent: String,
    mockRules: List<com.example.data.MockRule> = emptyList(),
    onConsoleLog: (LogLevel, String, String, Int) -> Unit,
    onNetworkLog: (NetworkLog) -> Unit,
    onPostMessageReceived: (payload: String, origin: String) -> Unit = { _, _ -> },
    onElementSelected: (SelectedElementInfo) -> Unit = {},
    onLoadingStateChanged: (Boolean, Int) -> Unit,
    onPageTitleChanged: (String, String) -> Unit,
    onWebViewReady: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                mediaPlaybackRequiresUserGesture = false
                javaScriptCanOpenWindowsAutomatically = true
            }

            val currentWebView = this
            android.webkit.CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(currentWebView, true)
            }

            addJavascriptInterface(
                NetworkInspectorBridge { log ->
                    onNetworkLog(log)
                },
                "AndroidNetworkInspector"
            )

            addJavascriptInterface(
                PostMessageBridge { data, origin ->
                    onPostMessageReceived(data, origin)
                },
                "AndroidPostMessage"
            )

            addJavascriptInterface(
                DOMInspectorBridge { info ->
                    onElementSelected(info)
                },
                "AndroidDOMInspector"
            )

            // Enable debugging if on modern android
            WebView.setWebContentsDebuggingEnabled(true)
            setNetworkAvailable(true)
        }
    }

    DisposableEffect(webView) {
        onWebViewReady(webView)
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    val defaultUserAgent = remember { WebSettings.getDefaultUserAgent(context) }
    
    // Configure user agent
    val targetUserAgent = when (userAgentPreset) {
        UserAgentPreset.DEFAULT_MOBILE -> defaultUserAgent
        UserAgentPreset.CUSTOM -> customUserAgent.ifBlank { defaultUserAgent }
        else -> userAgentPreset.userAgentString ?: defaultUserAgent
    }

    androidx.compose.runtime.LaunchedEffect(targetUserAgent) {
        if (webView.settings.userAgentString != targetUserAgent) {
            webView.settings.userAgentString = targetUserAgent
            webView.reload()
        }
    }

    fun generateMockRulesScript(rules: List<com.example.data.MockRule>): String {
        val rulesJson = org.json.JSONArray().apply {
            rules.filter { it.enabled }.forEach { rule ->
                put(org.json.JSONObject().apply {
                    put("id", rule.id)
                    put("urlPattern", rule.urlPattern)
                    put("method", rule.method)
                    put("statusCode", rule.statusCode)
                    put("responseBody", rule.responseBody)
                    put("contentType", rule.contentType)
                    put("enabled", rule.enabled)
                })
            }
        }.toString()
        return "window.__mockRules = $rulesJson;"
    }

    androidx.compose.runtime.LaunchedEffect(mockRules) {
        val script = generateMockRulesScript(mockRules)
        webView.evaluateJavascript(script, null)
    }

    // Set clients
    val client = remember(customHeaders, mockRules) {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onLoadingStateChanged(true, 10)
                view?.evaluateJavascript(NETWORK_INSPECTOR_JS, null)
                view?.evaluateJavascript(POST_MESSAGE_INSPECTOR_JS, null)
                view?.evaluateJavascript(ELEMENT_INSPECTOR_JS, null)
                view?.evaluateJavascript(generateMockRulesScript(mockRules), null)
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    val enabledHeadersMap = customHeaders
                        .filter { it.enabled && it.key.isNotBlank() }
                        .associate { it.key to it.value }
                    onNetworkLog(
                        NetworkLog(
                            url = url,
                            method = "GET",
                            statusCode = 200,
                            statusText = "Loading",
                            requestHeaders = enabledHeadersMap,
                            resourceType = "Document"
                        )
                    )
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onLoadingStateChanged(false, 100)
                view?.evaluateJavascript(NETWORK_INSPECTOR_JS, null)
                view?.evaluateJavascript(POST_MESSAGE_INSPECTOR_JS, null)
                view?.evaluateJavascript(generateMockRulesScript(mockRules), null)
                val title = view?.title ?: ""
                val currentUrl = url ?: ""
                onPageTitleChanged(title, currentUrl)
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: android.webkit.SslErrorHandler?,
                error: android.net.http.SslError?
            ) {
                // Ignore SSL certificate errors to allow testing self-signed certs and local HTTPS
                handler?.proceed()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                // Keep HTTP/HTTPS inside the WebView
                return if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
                    false
                } else {
                    true
                }
            }
        }
    }
    
    // Only set if different to prevent cancelling loads
    if (webView.webViewClient != client) {
        webView.webViewClient = client
    }

    val chromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onLoadingStateChanged(newProgress < 100, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                val currentUrl = view?.url ?: ""
                onPageTitleChanged(title ?: "", currentUrl)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    val level = when (it.messageLevel()) {
                        ConsoleMessage.MessageLevel.ERROR -> LogLevel.ERROR
                        ConsoleMessage.MessageLevel.WARNING -> LogLevel.WARNING
                        ConsoleMessage.MessageLevel.LOG -> LogLevel.LOG
                        ConsoleMessage.MessageLevel.TIP, ConsoleMessage.MessageLevel.DEBUG -> LogLevel.INFO
                        else -> LogLevel.LOG
                    }
                    onConsoleLog(
                        level,
                        it.message() ?: "",
                        it.sourceId() ?: "",
                        it.lineNumber()
                    )
                }
                return true
            }
        }
    }

    var lastLoadedUrl by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var lastTrigger by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

    AndroidView(
        factory = { webView },
        update = { view ->
            if (activeUrl.isNotBlank() && (activeUrl != lastLoadedUrl || urlReloadTrigger != lastTrigger)) {
                lastLoadedUrl = activeUrl
                lastTrigger = urlReloadTrigger
                val enabledHeadersMap = customHeaders
                    .filter { it.enabled && it.key.isNotBlank() }
                    .associate { it.key to it.value }
                if (enabledHeadersMap.isNotEmpty()) {
                    view.loadUrl(activeUrl, enabledHeadersMap)
                } else {
                    view.loadUrl(activeUrl)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
