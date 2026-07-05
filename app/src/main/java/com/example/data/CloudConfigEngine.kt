package com.example.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppCloudConfig(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0",
    val updateTitle: String = "নতুন আপডেট এসেছে!",
    val updateNotes: String = "অ্যাপে নতুন ইসলামিক ফিচার, রমজানের সময়সূচী ও বাগ ফিক্স যুক্ত করা হয়েছে।",
    val apkDownloadUrl: String = "",
    val forceUpdate: Boolean = false,
    val bannerEmoji: String = "🌙",
    val bannerTitle: String = "রমজান মোবারক ১৪৪৭",
    val bannerSubtitle: String = "সাহরি এবং ইফতারের সময়সূচী ও আমল ট্র্যাকার দেখতে ট্যাপ করুন।",
    val bannerTarget: String = "ramadan",
    val specialAmals: List<String> = emptyList(),
    val announcementText: String = "",
    val openrouterApiKey: String = "",
    val openrouterModelId: String = "google/gemini-2.0-flash-lite-preview-02-05:free"
)

object CloudConfigEngine {
    // Firebase Realtime Database REST API endpoint
    private const val FIREBASE_RTDB_URL = "https://dinpath-c241f-default-rtdb.firebaseio.com/app_config.json"

    suspend fun fetchConfig(): AppCloudConfig? = withContext(Dispatchers.IO) {
        try {
            val url = URL(FIREBASE_RTDB_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonStr = reader.use { it.readText() }
                if (jsonStr.isNotBlank() && jsonStr != "null") {
                    val json = JSONObject(jsonStr)
                    
                    val amalsList = mutableListOf<String>()
                    if (json.has("special_amals")) {
                        val arr = json.getJSONArray("special_amals")
                        for (i in 0 until arr.length()) {
                            amalsList.add(arr.getString(i))
                        }
                    }

                    return@withContext AppCloudConfig(
                        latestVersionCode = json.optInt("latest_version_code", 1),
                        latestVersionName = json.optString("latest_version_name", "1.0"),
                        updateTitle = json.optString("update_title", "নতুন আপডেট এসেছে!"),
                        updateNotes = json.optString("update_notes", "অ্যাপে নতুন ইসলামিক ফিচার, রমজানের সময়সূচী ও বাগ ফিক্স যুক্ত করা হয়েছে।"),
                        apkDownloadUrl = json.optString("apk_download_url", ""),
                        forceUpdate = json.optBoolean("force_update", false),
                        bannerEmoji = json.optString("banner_emoji", "🌙"),
                        bannerTitle = json.optString("banner_title", "রমজান মোবারক ১৪৪৭"),
                        bannerSubtitle = json.optString("banner_subtitle", "সাহরি এবং ইফতারের সময়সূচী ও আমল ট্র্যাকার দেখতে ট্যাপ করুন।"),
                        bannerTarget = json.optString("banner_target", "ramadan"),
                        specialAmals = amalsList,
                        announcementText = json.optString("announcement_text", ""),
                        openrouterApiKey = json.optString("openrouter_api_key", ""),
                        openrouterModelId = json.optString("openrouter_model_id", "google/gemini-2.0-flash-lite-preview-02-05:free")
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit,
        onComplete: (File?) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val url = URL(apkUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            val totalLength = connection.contentLength
            val dir = File(context.externalCacheDir ?: context.cacheDir, "updates")
            if (!dir.exists()) dir.mkdirs()

            val apkFile = File(dir, "update_dinpath.apk")
            if (apkFile.exists()) apkFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesRead += read
                        if (totalLength > 0) {
                            val progress = (bytesRead.toFloat() / totalLength.toFloat()).coerceIn(0f, 1f)
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                    output.flush()
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(1f)
                onComplete(apkFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onComplete(null)
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: Open in browser if uri permission fails
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(apkFile.toURI().toString()))
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(browserIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun openUrlInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
