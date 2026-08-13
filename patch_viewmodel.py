import sys

filepath = 'app/src/main/java/com/example/ui/InspectorViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """    fun updateAiSettings(provider: String, baseUrl: String, apiKey: String, modelName: String) {
        viewModelScope.launch {
            val current = appSettings.value ?: return@launch
            val updated = current.copy(
                aiProvider = provider,
                aiBaseUrl = baseUrl,
                aiApiKey = apiKey,
                aiModelName = modelName
            )
            repository.updateSettings(updated)
        }
    }"""

replacement = """    fun updateAiSettings(provider: String, baseUrl: String, apiKey: String, modelName: String) {
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
    }"""

if target in content:
    with open(filepath, 'w') as f:
        f.write(content.replace(target, replacement))
    print("Patched InspectorViewModel successfully.")
else:
    print("Target not found in InspectorViewModel.")

