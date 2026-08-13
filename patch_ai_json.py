import sys

filepath = 'app/src/main/java/com/example/data/AiService.kt'
with open(filepath, 'r') as f:
    content = f.read()

replacement = """                    val responseJson = try {
                        JSONObject(responseStr)
                    } catch (e: org.json.JSONException) {
                        if (responseStr.trim().startsWith("<", ignoreCase = true)) {
                            throw Exception("Server returned an HTML page instead of API JSON. Your Base URL might be incorrect or your network/ISP is blocking the request and showing a captive portal/filtering page.")
                        }
                        throw Exception("Failed to parse API response: ${e.message}")
                    }"""

content = content.replace("val responseJson = JSONObject(responseStr)", replacement)

with open(filepath, 'w') as f:
    f.write(content)
print("Patched AiService JSON parsing successfully.")
