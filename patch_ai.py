import sys

filepath = 'app/src/main/java/com/example/data/AiService.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """                "GEMINI" -> {
                    val mName = if (modelName.isNotBlank()) modelName else "gemini-1.5-flash"
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$mName:generateContent?key=$keyToUse\""""

replacement = """                "GEMINI" -> {
                    val mName = if (modelName.isNotBlank()) modelName else "gemini-1.5-flash"
                    val host = if (baseUrl.isNotBlank()) baseUrl.trimEnd('/') else "https://generativelanguage.googleapis.com"
                    val url = "$host/v1beta/models/$mName:generateContent?key=$keyToUse\""""

if target in content:
    with open(filepath, 'w') as f:
        f.write(content.replace(target, replacement))
    print("Patched AiService successfully.")
else:
    print("Target not found in AiService.")
