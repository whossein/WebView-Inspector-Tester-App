import sys

filepath = 'app/src/main/java/com/example/ui/components/WebViewContainer.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """                if (window.AndroidNetworkInspector) {
                    window.AndroidNetworkInspector.logNetworkRequest(
                        url, method, response.status, response.statusText || 'OK',
                        JSON.stringify(reqHeaders), reqBody,
                        JSON.stringify(resHeaders), resText,
                        Date.now() - startTime, false
                    );
                }
                return response;
            } catch(err) {"""
replacement = """                try {
                    if (window.AndroidNetworkInspector) {
                        window.AndroidNetworkInspector.logNetworkRequest(
                            url, method, response.status, response.statusText || 'OK',
                            JSON.stringify(reqHeaders), reqBody,
                            JSON.stringify(resHeaders), resText.substring(0, 100000), // Limit size
                            Date.now() - startTime, false
                        );
                    }
                } catch(e) {}
                return response;
            } catch(err) {"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w') as f:
        f.write(content)
    print("Patched fetch success")
else:
    print("Target 1 not found")

target2 = """            try {
                const response = await origFetch.apply(window, args);
                const clone = response.clone();
                let resText = '';
                try { 
                     const ct = clone.headers.get('content-type') || '';
                    if (ct.includes('application/json') || ct.includes('text/')) {
                        resText = await clone.text(); 
                     } else {
                        resText = '[Binary/Other Data]';
                    }
                } catch(e) {}"""
replacement2 = """            try {
                const response = await origFetch.apply(window, args);
                let clone = null;
                try { clone = response.clone(); } catch(e) {}
                
                // Do not block the original response!
                // Read clone in background
                setTimeout(async () => {
                    let resText = '';
                    try { 
                        if (clone) {
                            const ct = clone.headers.get('content-type') || '';
                            if (ct.includes('application/json') || ct.includes('text/')) {
                                resText = await clone.text(); 
                            } else {
                                resText = '[Binary/Other Data]';
                            }
                        }
                    } catch(e) {}
                    
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
                }, 0);
                
                return response;
"""

# We need a different approach to replace the entire try block.
