package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object AiChatEngine {
    private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
    private const val SYSTEM_PROMPT = "আপনি হলেন 'দ্বীনপথ AI' (DinPath AI) - একজন অত্যন্ত বিনয়ী, জ্ঞানী ও নির্ভরযোগ্য ইসলামিক সহকারী। আপনি কুরআন, সহীহ হাদিস, ফিকহ, নামাজ, রোজা, রমজান এবং দৈনন্দিন ইসলামিক জীবনের যেকোনো বিষয়ে বাংলা ভাষায় সঠিক ও সুন্দর উত্তর দেন। কোনো উত্তর দেওয়ার সময় সম্ভব হলে কুরআন বা সহীহ হাদিসের রেফারেন্স উল্লেখ করুন। বিতর্কিত বিষয়ে বাড়াবাড়ি না করে চার মাজহাব ও মূলধারার ইসলামের মতামত তুলে ধরুন। সালামের উত্তর সুন্দরভাবে দিন।"

    suspend fun sendChatMessage(
        apiKey: String,
        modelId: String,
        history: List<ChatMessage>,
        userPrompt: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "⚠️ এডমিন প্যানেল থেকে OpenRouter API Key কনফিগার করা হয়নি। অনুগ্রহ করে এডমিন প্যানেলে API Key এবং Model ID সেভ করুন।"
        }

        try {
            val url = URL(OPENROUTER_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("HTTP-Referer", "https://dinpath.app")
            connection.setRequestProperty("X-Title", "DinPath Islamic App")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 45000

            val messagesArray = JSONArray()
            // System prompt
            val sysObj = JSONObject()
            sysObj.put("role", "system")
            sysObj.put("content", SYSTEM_PROMPT)
            messagesArray.put(sysObj)

            // Last 10 messages from history for context
            val recentHistory = history.takeLast(10)
            for (msg in recentHistory) {
                val msgObj = JSONObject()
                msgObj.put("role", if (msg.isUser) "user" else "assistant")
                msgObj.put("content", msg.text)
                messagesArray.put(msgObj)
            }

            // Current prompt
            val userObj = JSONObject()
            userObj.put("role", "user")
            userObj.put("content", userPrompt)
            messagesArray.put(userObj)

            val requestBody = JSONObject()
            var effectiveModel = if (modelId.isNotBlank()) modelId.trim() else "google/gemini-2.5-flash"
            if (effectiveModel.contains("preview-02-05")) {
                effectiveModel = "google/gemini-2.5-flash"
            }
            requestBody.put("model", effectiveModel)
            requestBody.put("messages", messagesArray)
            requestBody.put("temperature", 0.7)

            val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
            writer.write(requestBody.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val responseStr = reader.use { it.readText() }
                val responseJson = JSONObject(responseStr)
                
                if (responseJson.has("choices")) {
                    val choices = responseJson.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val firstChoice = choices.getJSONObject(0)
                        val messageObj = firstChoice.getJSONObject("message")
                        val content = messageObj.optString("content", "")
                        if (content.isNotBlank()) {
                            return@withContext content.trim()
                        }
                    }
                }
                return@withContext "দুঃখিত, কোনো উত্তর পাওয়া যায়নি। আবার চেষ্টা করুন।"
            } else {
                val errorReader = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream, "UTF-8"))
                val errorStr = errorReader.use { it.readText() }
                // Retry if model ID was rejected
                if ((responseCode == 400 || responseCode == 404) && errorStr.contains("not a valid model ID", ignoreCase = true) && effectiveModel != "google/gemini-2.5-flash") {
                    return@withContext sendChatMessage(apiKey, "google/gemini-2.5-flash", history, userPrompt)
                }
                return@withContext "⚠️ API Error ($responseCode): ${errorStr.take(150)}\n(টিপস: এডমিন প্যানেল থেকে সঠিক OpenRouter Model ID সেট করুন, যেমন: google/gemini-2.5-flash)"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "⚠️ নেটওয়ার্ক বা কানেকশন সমস্যা হয়েছে: ${e.localizedMessage}"
        }
    }
}
