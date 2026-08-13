package com.example.data


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun queryWebAssistant(
        prompt: String,
        codeContext: String? = null,
        provider: String = "GEMINI",
        baseUrl: String = "",
        apiKey: String = "",
        modelName: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val systemInstruction = "You are a professional Web Development & Frontend Inspector Assistant. You answer questions about HTML, CSS, JavaScript, DOM structure, responsiveness, and web performance. Respond in the same language as the user query (Persian or English). Keep answers clear, technical, concise, and helpful."
        
        val fullPromptBuilder = java.lang.StringBuilder()
        if (!codeContext.isNullOrBlank()) {
            fullPromptBuilder.append("--- Webpage Code/DOM Context ---\n")
            fullPromptBuilder.append(codeContext)
            fullPromptBuilder.append("\n--------------------------------\n\n")
        }
        fullPromptBuilder.append("User Question: ").append(prompt)
        val userContent = fullPromptBuilder.toString()

        callAiApi(provider, baseUrl, apiKey, modelName, systemInstruction, userContent)
    }

    suspend fun generateNetworkMock(
        prompt: String,
        recentLogsContext: String? = null,
        provider: String = "GEMINI",
        baseUrl: String = "",
        apiKey: String = "",
        modelName: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val systemInstruction = """
            You are a professional API & Network Mocking Assistant.
            Your task is to generate mock rules for HTTP network requests based on the user's prompt.
            ALWAYS include a ```json_mock code block in your response with the following JSON schema:
            ```json_mock
            {
              "urlPattern": "URL path or pattern to match (e.g. api/users or /api/login)",
              "method": "GET" or "POST" or "PUT" or "DELETE" or "ALL",
              "statusCode": 200,
              "responseBody": "Valid JSON or text payload",
              "contentType": "application/json"
            }
            ```
            Provide a friendly, helpful explanation in the user's language (Persian or English) explaining what this mock rule does and how it will intercept the request.
        """.trimIndent()
        
        val fullPromptBuilder = java.lang.StringBuilder()
        if (!recentLogsContext.isNullOrBlank()) {
            fullPromptBuilder.append("--- Recent Network Activity Context ---\n")
            fullPromptBuilder.append(recentLogsContext)
            fullPromptBuilder.append("\n----------------------------------------\n\n")
        }
        fullPromptBuilder.append("User Request for Mock Rule: ").append(prompt)
        val userContent = fullPromptBuilder.toString()

        callAiApi(provider, baseUrl, apiKey, modelName, systemInstruction, userContent)
    }

    private fun callAiApi(
        provider: String,
        baseUrl: String,
        apiKey: String,
        modelName: String,
        systemInstruction: String,
        userContent: String
    ): Result<String> {
        try {
            val keyToUse = if (apiKey.isNotBlank()) apiKey else ""
            if (keyToUse.isBlank() || keyToUse == "MY_GEMINI_API_KEY") {
                return Result.failure(IllegalStateException("API Key is missing. Please configure it in AI Settings or Secrets."))
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()

            when (provider) {
                "GEMINI" -> {
                    val mName = if (modelName.isNotBlank()) modelName else "gemini-1.5-flash"
                    val host = if (baseUrl.isNotBlank()) baseUrl.trimEnd('/') else "https://generativelanguage.googleapis.com"
                    val url = "$host/v1beta/models/$mName:generateContent?key=$keyToUse"
                    
                    val jsonBody = JSONObject().apply {
                        val contentsArr = JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply { put("text", userContent) })
                                })
                            })
                        }
                        put("contents", contentsArr)

                        val sysInstructionObj = JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", systemInstruction) })
                            })
                        }
                        put("systemInstruction", sysInstructionObj)
                    }

                    val requestBody = jsonBody.toString().toRequestBody(mediaType)
                    val request = Request.Builder().url(url).post(requestBody).build()
                    val response = client.newCall(request).execute()
                    val responseStr = response.body?.string() ?: ""

                    if (!response.isSuccessful) return Result.failure(Exception("API Error (${response.code}): $responseStr"))

                                        val responseJson = try {
                        JSONObject(responseStr)
                    } catch (e: org.json.JSONException) {
                        if (responseStr.trim().startsWith("<", ignoreCase = true)) {
                            throw Exception("Server returned an HTML page instead of API JSON. Your Base URL might be incorrect or your network/ISP is blocking the request and showing a captive portal/filtering page.")
                        }
                        throw Exception("Failed to parse API response: ${e.message}")
                    }
                    val candidates = responseJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return Result.success(parts.getJSONObject(0).optString("text", ""))
                        }
                    }
                    return Result.failure(Exception("No answer text generated by Gemini."))
                }
                "CLAUDE" -> {
                    val mName = if (modelName.isNotBlank()) modelName else "claude-3-opus-20240229"
                    val url = if (baseUrl.isNotBlank()) baseUrl else "https://api.anthropic.com/v1/messages"
                    
                    val jsonBody = JSONObject().apply {
                        put("model", mName)
                        put("max_tokens", 2048)
                        put("system", systemInstruction)
                        put("messages", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", userContent)
                            })
                        })
                    }

                    val requestBody = jsonBody.toString().toRequestBody(mediaType)
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-api-key", keyToUse)
                        .addHeader("anthropic-version", "2023-06-01")
                        .addHeader("content-type", "application/json")
                        .post(requestBody)
                        .build()
                        
                    val response = client.newCall(request).execute()
                    val responseStr = response.body?.string() ?: ""

                    if (!response.isSuccessful) return Result.failure(Exception("API Error (${response.code}): $responseStr"))

                                        val responseJson = try {
                        JSONObject(responseStr)
                    } catch (e: org.json.JSONException) {
                        if (responseStr.trim().startsWith("<", ignoreCase = true)) {
                            throw Exception("Server returned an HTML page instead of API JSON. Your Base URL might be incorrect or your network/ISP is blocking the request and showing a captive portal/filtering page.")
                        }
                        throw Exception("Failed to parse API response: ${e.message}")
                    }
                    val contentArr = responseJson.optJSONArray("content")
                    if (contentArr != null && contentArr.length() > 0) {
                        return Result.success(contentArr.getJSONObject(0).optString("text", ""))
                    }
                    return Result.failure(Exception("No answer text generated by Claude."))
                }
                "OPENAI", "CUSTOM" -> {
                    val mName = if (modelName.isNotBlank()) modelName else "gpt-4o"
                    val url = if (baseUrl.isNotBlank()) baseUrl else "https://api.openai.com/v1/chat/completions"
                    
                    val jsonBody = JSONObject().apply {
                        put("model", mName)
                        put("messages", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "system")
                                put("content", systemInstruction)
                            })
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", userContent)
                            })
                        })
                    }

                    val requestBody = jsonBody.toString().toRequestBody(mediaType)
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $keyToUse")
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody)
                        .build()
                        
                    val response = client.newCall(request).execute()
                    val responseStr = response.body?.string() ?: ""

                    if (!response.isSuccessful) return Result.failure(Exception("API Error (${response.code}): $responseStr"))

                                        val responseJson = try {
                        JSONObject(responseStr)
                    } catch (e: org.json.JSONException) {
                        if (responseStr.trim().startsWith("<", ignoreCase = true)) {
                            throw Exception("Server returned an HTML page instead of API JSON. Your Base URL might be incorrect or your network/ISP is blocking the request and showing a captive portal/filtering page.")
                        }
                        throw Exception("Failed to parse API response: ${e.message}")
                    }
                    val choices = responseJson.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val message = choices.getJSONObject(0).optJSONObject("message")
                        if (message != null) {
                            return Result.success(message.optString("content", ""))
                        }
                    }
                    return Result.failure(Exception("No answer text generated by OpenAI/Custom API."))
                }
                else -> {
                    return Result.failure(Exception("Unknown AI Provider: $provider"))
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
