import sys

filepath = 'app/src/main/java/com/example/ui/components/WebViewContainer.kt'
with open(filepath, 'r') as f:
    content = f.read()

import re
pattern = re.compile(r'private const val NETWORK_INSPECTOR_JS = """(.*?)"""', re.DOTALL)

new_js = """(function() {
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
                    resHeadersStr.split('\\r\\n').forEach(line => {
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

content = pattern.sub(f'private const val NETWORK_INSPECTOR_JS = """{new_js}"""', content)
with open(filepath, 'w') as f:
    f.write(content)
print("Updated NETWORK_INSPECTOR_JS successfully")
