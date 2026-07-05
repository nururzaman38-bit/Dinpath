package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class DeenViewModel(application: Application) : AndroidViewModel(application) {

    val settingsManager = SettingsManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.dao()

    // Date controls
    private val _currentCalendar = MutableStateFlow(Calendar.getInstance())
    val currentCalendar: StateFlow<Calendar> = _currentCalendar

    private val _currentDateString = MutableStateFlow(getTodayDateString())
    val currentDateString: StateFlow<String> = _currentDateString

    // Prayer tracker state for the day
    val activePrayerTracker: StateFlow<PrayerTracker> = _currentDateString
        .flatMapLatest { date ->
            dao.getPrayerTracker(date).map { it ?: PrayerTracker(date) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrayerTracker(getTodayDateString()))

    // Calculated prayer times
    val prayerTimes: StateFlow<PrayerTimes> = combine(
        _currentCalendar,
        settingsManager.location,
        settingsManager.calculationMethod,
        settingsManager.madhab
    ) { cal, loc, method, madhab ->
        PrayerTimeHelper.calculatePrayerTimes(cal, loc, method, madhab)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PrayerTimeHelper.calculatePrayerTimes(Calendar.getInstance(), "ঢাকা")
    )

    // All tasbih logs
    val tasbihHistory: StateFlow<List<TasbihRecord>> = dao.getAllTasbihRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookmarks list
    val allBookmarks: StateFlow<List<Bookmark>> = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasbih Counter States
    private val _tasbihCount = MutableStateFlow(0)
    val tasbihCount: StateFlow<Int> = _tasbihCount

    private val _tasbihTarget = MutableStateFlow(33)
    val tasbihTarget: StateFlow<Int> = _tasbihTarget

    private val _selectedZikir = MutableStateFlow("সুবহানাল্লাহ")
    val selectedZikir: StateFlow<String> = _selectedZikir

    // Active screen index / routing flow
    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished

    // Audio streaming state simulation for Quran and duaa (ExoPlayer simulated via lightweight interactive controls)
    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio

    private val _isPlayingAzan = MutableStateFlow(false)
    val isPlayingAzan: StateFlow<Boolean> = _isPlayingAzan

    private val _isPlayingDua = MutableStateFlow(false)
    val isPlayingDua: StateFlow<Boolean> = _isPlayingDua

    private val _playingDuaTitle = MutableStateFlow("")
    val playingDuaTitle: StateFlow<String> = _playingDuaTitle

    private val _playingSurahName = MutableStateFlow("")
    val playingSurahName: StateFlow<String> = _playingSurahName

    private val _playingQari = MutableStateFlow(settingsManager.qari.value)
    val playingQari: StateFlow<String> = _playingQari

    private val _selectedDua = MutableStateFlow<Dua?>(null)
    val selectedDua: StateFlow<Dua?> = _selectedDua

    // Real Quran Offline Audio Download States
    private val _downloadingSurahName = MutableStateFlow("")
    val downloadingSurahName: StateFlow<String> = _downloadingSurahName

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress

    private val _surahDownloadPrompt = MutableStateFlow<Surah?>(null)
    val surahDownloadPrompt: StateFlow<Surah?> = _surahDownloadPrompt

    // Real Audio Progress Tracking
    private val _audioProgress = MutableStateFlow(0f)
    val audioProgress: StateFlow<Float> = _audioProgress

    // GPS Auto-Location & Permission States
    private val _locationAutoDetected = MutableStateFlow(settingsManager.locationAutoDetected.value)
    val locationAutoDetected: StateFlow<Boolean> = _locationAutoDetected

    private val _locationDetecting = MutableStateFlow(false)
    val locationDetecting: StateFlow<Boolean> = _locationDetecting

    private val _locationStatusMessage = MutableStateFlow<String?>(null)
    val locationStatusMessage: StateFlow<String?> = _locationStatusMessage

    // Cloud Config & Auto-Update Engine States
    val cloudConfig = MutableStateFlow<AppCloudConfig?>(null)
    val updateAvailablePrompt = MutableStateFlow<AppCloudConfig?>(null)
    val isDownloadingApk = MutableStateFlow(false)
    val apkDownloadProgress = MutableStateFlow(0f)

    // AI Chat Assistant States
    val chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(text = "আসসালামু আলাইকুম! আমি 'দ্বীনপথ AI' - আপনার ইসলামিক সহকারী। কুরআন, হাদিস, নামাজ, রোজা বা দৈনন্দিন মাসয়ালা-মাসায়েল সম্পর্কে আপনার কোনো জিজ্ঞাসা থাকলে আমাকে বলতে পারেন।", isUser = false)
    ))
    val isAiTyping = MutableStateFlow(false)

    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank() || isAiTyping.value) return
        val userMsg = ChatMessage(text = prompt.trim(), isUser = true)
        val currentList = chatMessages.value.toMutableList()
        currentList.add(userMsg)
        chatMessages.value = currentList
        
        isAiTyping.value = true
        viewModelScope.launch {
            val apiKey = cloudConfig.value?.openrouterApiKey ?: ""
            val modelId = cloudConfig.value?.openrouterModelId ?: "google/gemini-2.0-flash-lite-preview-02-05:free"
            val response = AiChatEngine.sendChatMessage(apiKey, modelId, currentList, prompt)
            val aiMsg = ChatMessage(text = response, isUser = false)
            val updatedList = chatMessages.value.toMutableList()
            updatedList.add(aiMsg)
            chatMessages.value = updatedList
            isAiTyping.value = false
        }
    }

    fun clearAiChat() {
        chatMessages.value = listOf(
            ChatMessage(text = "আসসালামু আলাইকুম! আমি 'দ্বীনপথ AI' - আপনার ইসলামিক সহকারী। কুরআন, হাদিস, নামাজ, রোজা বা দৈনন্দিন মাসয়ালা-মাসায়েল সম্পর্কে আপনার কোনো জিজ্ঞাসা থাকলে আমাকে বলতে পারেন।", isUser = false)
        )
    }

    // Dynamic Seasonal Event & Online Sync States
    val currentSeasonEvent: StateFlow<SeasonalBanner> = combine(_currentCalendar, _currentDateString, cloudConfig) { cal, _, config ->
        val hijri = PrayerTimeHelper.getHijriDate(cal)
        val defaultBanner = SeasonalEventEngine.getSeasonalBanner(hijri, cal)
        if (config != null && config.bannerTitle.isNotEmpty() && config.bannerTitle != "রমজান মোবারক ১৪৪৭") {
            defaultBanner.copy(
                emoji = if (config.bannerEmoji.isNotEmpty()) config.bannerEmoji else defaultBanner.emoji,
                title = config.bannerTitle,
                subtitle = if (config.bannerSubtitle.isNotEmpty()) config.bannerSubtitle else defaultBanner.subtitle,
                targetScreen = if (config.bannerTarget.isNotEmpty()) config.bannerTarget else defaultBanner.targetScreen,
                specialAmalList = if (config.specialAmals.isNotEmpty()) config.specialAmals else defaultBanner.specialAmalList
            )
        } else defaultBanner
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeasonalEventEngine.getSeasonalBanner(PrayerTimeHelper.getHijriDate(Calendar.getInstance()), Calendar.getInstance()))

    private val _isSyncingCalendar = MutableStateFlow(false)
    val isSyncingCalendar: StateFlow<Boolean> = _isSyncingCalendar

    private val _calendarSyncMessage = MutableStateFlow<String?>(null)
    val calendarSyncMessage: StateFlow<String?> = _calendarSyncMessage

    init {
        // Check for updates from Firebase Realtime Database
        checkForUpdates()

        // Automatically check date changes or updates
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60000) // check every minute
                val newToday = getTodayDateString()
                if (newToday != _currentDateString.value) {
                    _currentDateString.value = newToday
                    _currentCalendar.value = Calendar.getInstance()
                }
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            val config = CloudConfigEngine.fetchConfig()
            if (config != null) {
                cloudConfig.value = config
                try {
                    val pInfo = getApplication<Application>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
                    val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        pInfo.longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION") pInfo.versionCode
                    }
                    if (config.latestVersionCode > currentVersionCode && config.apkDownloadUrl.isNotEmpty()) {
                        updateAvailablePrompt.value = config
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (config.latestVersionCode > 1 && config.apkDownloadUrl.isNotEmpty()) {
                        updateAvailablePrompt.value = config
                    }
                }
            }
        }
    }

    fun dismissUpdatePrompt() {
        if (updateAvailablePrompt.value?.forceUpdate != true) {
            updateAvailablePrompt.value = null
        }
    }

    fun startApkDownloadAndInstall(context: Context, apkUrl: String) {
        if (isDownloadingApk.value) return
        isDownloadingApk.value = true
        apkDownloadProgress.value = 0f
        viewModelScope.launch {
            CloudConfigEngine.downloadAndInstallApk(
                context = context,
                apkUrl = apkUrl,
                onProgress = { progress ->
                    apkDownloadProgress.value = progress
                },
                onComplete = { file ->
                    isDownloadingApk.value = false
                    if (file != null && file.exists()) {
                        CloudConfigEngine.installApk(context, file)
                    } else {
                        CloudConfigEngine.openUrlInBrowser(context, apkUrl)
                    }
                }
            )
        }
    }

    fun openApkInBrowser(context: Context, apkUrl: String) {
        CloudConfigEngine.openUrlInBrowser(context, apkUrl)
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // Date change methods (navigating Hijri or Gregorian calendars)
    fun changeDay(offset: Int) {
        val cal = _currentCalendar.value.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, offset)
        _currentCalendar.value = cal
        _currentDateString.value = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun resetToToday() {
        _currentCalendar.value = Calendar.getInstance()
        _currentDateString.value = getTodayDateString()
    }

    // Toggle a standard daily prayer status (Fajr, Zuhr, Asr, Maghrib, Isha)
    fun togglePrayerStatus(prayerName: String) {
        viewModelScope.launch {
            val currentTracker = activePrayerTracker.value
            val updated = when (prayerName) {
                "ফজর" -> currentTracker.copy(fajr = !currentTracker.fajr)
                "যোহর" -> currentTracker.copy(zuhr = !currentTracker.zuhr)
                "আসর" -> currentTracker.copy(asr = !currentTracker.asr)
                "মাগরিব" -> currentTracker.copy(maghrib = !currentTracker.maghrib)
                "এশা" -> currentTracker.copy(isha = !currentTracker.isha)
                else -> currentTracker
            }
            dao.insertOrUpdatePrayerTracker(updated)
        }
    }

    // Tasbih count
    fun incrementTasbih() {
        _tasbihCount.value += 1
        if (_tasbihCount.value >= _tasbihTarget.value) {
            // Save log to database when target reached
            viewModelScope.launch {
                dao.insertTasbihRecord(
                    TasbihRecord(
                        date = _currentDateString.value,
                        zikirText = _selectedZikir.value,
                        count = _tasbihCount.value
                    )
                )
                // We keep counting, but play soft haptic/bell triggered by UI
            }
        }
    }

    fun resetTasbih() {
        if (_tasbihCount.value > 0) {
            val countToSave = _tasbihCount.value
            viewModelScope.launch {
                dao.insertTasbihRecord(
                    TasbihRecord(
                        date = _currentDateString.value,
                        zikirText = _selectedZikir.value,
                        count = countToSave
                    )
                )
                _tasbihCount.value = 0
            }
        } else {
            _tasbihCount.value = 0
        }
    }

    fun changeZikir(newZikir: String) {
        _selectedZikir.value = newZikir
        _tasbihCount.value = 0
    }

    fun updateTasbihTarget(newTarget: Int) {
        _tasbihTarget.value = newTarget
    }

    fun clearSavedTasbihHistory() {
        viewModelScope.launch {
            dao.clearTasbihHistory()
        }
    }

    // Bookmarking management
    fun isBookmarked(type: String, itemId: String): Flow<Boolean> {
        return dao.isBookmarked(type, itemId)
    }

    fun toggleBookmark(type: String, itemId: String, title: String, subtitle: String) {
        viewModelScope.launch {
            val exists = dao.isBookmarked(type, itemId).first()
            if (exists) {
                dao.deleteBookmark(type, itemId)
            } else {
                dao.insertBookmark(
                    Bookmark(
                        type = type,
                        itemId = itemId,
                        title = title,
                        subtitle = subtitle
                    )
                )
            }
        }
    }

    // Splash navigation helper
    fun finishSplash() {
        _splashFinished.value = true
    }

    fun dismissSurahDownloadPrompt() {
        _surahDownloadPrompt.value = null
    }

    fun checkSurahDownloaded(surahId: Int, qariName: String): Boolean {
        val surahIdFormatted = String.format(Locale.US, "%03d", surahId)
        val file = File(getApplication<Application>().filesDir, "quran_audio/$qariName/$surahIdFormatted.mp3")
        return file.exists()
    }

    fun downloadSurahAudio(surah: Surah) {
        _surahDownloadPrompt.value = null
        _downloadingSurahName.value = surah.banglaName
        _downloadProgress.value = 0f

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val surahIdFormatted = String.format(Locale.US, "%03d", surah.id)
                    val qariName = _playingQari.value
                    val serverBase = when (qariName) {
                        "Mishary Rashid Al-Afasy" -> "https://server8.mp3quran.net/afs/"
                        "Abdul Rahman Al-Sudais" -> "https://server11.mp3quran.net/sds/"
                        "Saad Al-Ghamdi" -> "https://server7.mp3quran.net/s_gmd/"
                        "Maher Al-Muaiqly" -> "https://server12.mp3quran.net/maher/"
                        "Abu Bakr Al-Shatri" -> "https://server11.mp3quran.net/shatri/"
                        else -> "https://server8.mp3quran.net/afs/"
                    }
                    val urlStr = "$serverBase$surahIdFormatted.mp3"
                    val url = URL(urlStr)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    connection.connect()

                    val totalLength = connection.contentLength
                    val dir = File(getApplication<Application>().filesDir, "quran_audio/$qariName")
                    if (!dir.exists()) dir.mkdirs()

                    val tmpFile = File(dir, "$surahIdFormatted.tmp")
                    val finalFile = File(dir, "$surahIdFormatted.mp3")

                    connection.inputStream.use { input ->
                        FileOutputStream(tmpFile).use { output ->
                            val buffer = ByteArray(4096)
                            var bytesRead = 0L
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                bytesRead += read
                                if (totalLength > 0) {
                                    val progress = (bytesRead.toFloat() / totalLength.toFloat()).coerceIn(0f, 1f)
                                    _downloadProgress.value = progress
                                }
                            }
                            output.flush()
                        }
                    }
                    if (tmpFile.exists()) {
                        tmpFile.renameTo(finalFile)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _downloadingSurahName.value = ""
                    _downloadProgress.value = 0f
                }
            }
            startAudioPlayback(surah.banglaName)
        }
    }

    fun startStreamingAudio(surahName: String) {
        _surahDownloadPrompt.value = null
        val surah = QuranData.surahs.find { it.banglaName == surahName } ?: return
        val surahIdFormatted = String.format(Locale.US, "%03d", surah.id)
        val qariName = _playingQari.value
        val serverBase = when (qariName) {
            "Mishary Rashid Al-Afasy" -> "https://server8.mp3quran.net/afs/"
            "Abdul Rahman Al-Sudais" -> "https://server11.mp3quran.net/sds/"
            "Saad Al-Ghamdi" -> "https://server7.mp3quran.net/s_gmd/"
            "Maher Al-Muaiqly" -> "https://server12.mp3quran.net/maher/"
            "Abu Bakr Al-Shatri" -> "https://server11.mp3quran.net/shatri/"
            else -> "https://server8.mp3quran.net/afs/"
        }
        val url = "$serverBase$surahIdFormatted.mp3"
        _isPlayingAzan.value = false
        _playingSurahName.value = surahName
        _isPlayingAudio.value = true
        playUrl(url, isSurah = true)
    }

    fun dismissLocationStatusMessage() {
        _locationStatusMessage.value = null
    }

    fun fetchAndUpdateCurrentLocation(context: Context) {
        if (_locationDetecting.value) return
        _locationDetecting.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    var bestLoc: android.location.Location? = null
                    
                    try {
                        val gpsLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        val netLoc = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        bestLoc = when {
                            gpsLoc != null && netLoc != null -> if (gpsLoc.time > netLoc.time) gpsLoc else netLoc
                            gpsLoc != null -> gpsLoc
                            else -> netLoc
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }

                    if (bestLoc != null) {
                        val lat = bestLoc.latitude
                        val lon = bestLoc.longitude
                        
                        var closestCity = PrayerTimeHelper.cities[0]
                        var minDist = Double.MAX_VALUE
                        for (city in PrayerTimeHelper.cities) {
                            val dist = sqrt((lat - city.latitude) * (lat - city.latitude) + (lon - city.longitude) * (lon - city.longitude))
                            if (dist < minDist) {
                                minDist = dist
                                closestCity = city
                            }
                        }

                        withContext(Dispatchers.Main) {
                            settingsManager.setLocation(closestCity.name)
                            settingsManager.setLocationAutoDetected(true)
                            _locationAutoDetected.value = true
                            _locationStatusMessage.value = "📍 আপনার অবস্থান '${closestCity.name}' শনাক্ত করা হয়েছে এবং সময়সূচী স্বয়ংক্রিয়ভাবে আপডেট করা হয়েছে।"
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _locationStatusMessage.value = "⚠️ জিপিএস অবস্থান পাওয়া যায়নি। দয়া করে জিপিএস চালু করুন অথবা সেটিংস থেকে জেলা নির্বাচন করুন।"
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _locationDetecting.value = false
                }
            }
        }
    }

    fun dismissCalendarSyncMessage() {
        _calendarSyncMessage.value = null
    }

    fun syncCalendarOnline() {
        if (_isSyncingCalendar.value) return
        _isSyncingCalendar.value = true
        viewModelScope.launch {
            val res = SeasonalEventEngine.syncHijriCalendarOnline(getApplication())
            if (res != null) {
                _currentCalendar.value = Calendar.getInstance()
                _calendarSyncMessage.value = "✅ আলহামদুলিল্লাহ! ২০২৬ সালের হিজরি ও বাংলা ক্যালেন্ডার এবং আমলসমূহ অনলাইন থেকে সফলভাবে হালনাগাদ করা হয়েছে।"
            } else {
                _calendarSyncMessage.value = "⚠️ অনলাইন সিঙ্ক ব্যর্থ হয়েছে। অফলাইন সঠিক ক্যালেন্ডার ও আমল প্রদর্শিত হচ্ছে।"
            }
            _isSyncingCalendar.value = false
        }
    }

    // Audio Controls
    private var mediaPlayer: MediaPlayer? = null
    private var currentLoadedUrl: String? = null
    private var progressJob: kotlinx.coroutines.Job? = null

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (mediaPlayer != null && (_isPlayingAudio.value || _isPlayingAzan.value || _isPlayingDua.value)) {
                try {
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying && mp.duration > 0) {
                            _audioProgress.value = (mp.currentPosition.toFloat() / mp.duration.toFloat()).coerceIn(0f, 1f)
                        }
                    }
                } catch (e: Exception) {}
                kotlinx.coroutines.delay(300)
            }
        }
    }

    private fun playUrl(url: String, isSurah: Boolean, isAzan: Boolean = false, isDua: Boolean = false) {
        viewModelScope.launch {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setOnPreparedListener { mp ->
                            mp.start()
                            if (isSurah) {
                                _isPlayingAudio.value = true
                            } else if (isAzan) {
                                _isPlayingAzan.value = true
                            } else if (isDua) {
                                _isPlayingDua.value = true
                            }
                            startProgressTracker()
                        }
                        setOnCompletionListener {
                            progressJob?.cancel()
                            _audioProgress.value = 0f
                            if (isSurah) {
                                stopAudio()
                            } else if (isAzan) {
                                _isPlayingAzan.value = false
                            } else if (isDua) {
                                _isPlayingDua.value = false
                            }
                        }
                        setOnErrorListener { _, _, _ ->
                            progressJob?.cancel()
                            _audioProgress.value = 0f
                            if (isSurah) {
                                stopAudio()
                            } else if (isAzan) {
                                _isPlayingAzan.value = false
                            } else if (isDua) {
                                _isPlayingDua.value = false
                            }
                            true
                        }
                    }
                }

                if (currentLoadedUrl == url) {
                    mediaPlayer?.let { mp ->
                        if (!mp.isPlaying) {
                            mp.start()
                            if (isSurah) {
                                _isPlayingAudio.value = true
                            } else if (isAzan) {
                                _isPlayingAzan.value = true
                            } else if (isDua) {
                                _isPlayingDua.value = true
                            }
                            startProgressTracker()
                        }
                        return@launch
                    }
                }

                mediaPlayer?.let { mp ->
                    try {
                        if (mp.isPlaying) {
                            mp.stop()
                        }
                    } catch (e: Exception) {}
                    progressJob?.cancel()
                    _audioProgress.value = 0f
                    mp.reset()
                    mp.setDataSource(url)
                    currentLoadedUrl = url
                    mp.prepareAsync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                progressJob?.cancel()
                _audioProgress.value = 0f
                if (isSurah) {
                    stopAudio()
                } else if (isAzan) {
                    _isPlayingAzan.value = false
                } else if (isDua) {
                    _isPlayingDua.value = false
                }
            }
        }
    }

    fun startAudioPlayback(surahName: String) {
        val surah = QuranData.surahs.find { it.banglaName == surahName } ?: return
        val surahIdFormatted = String.format(Locale.US, "%03d", surah.id)
        val qariName = _playingQari.value
        
        if (checkSurahDownloaded(surah.id, qariName)) {
            val file = File(getApplication<Application>().filesDir, "quran_audio/$qariName/$surahIdFormatted.mp3")
            _isPlayingAzan.value = false
            _playingSurahName.value = surahName
            _isPlayingAudio.value = true
            playUrl(file.absolutePath, isSurah = true)
            return
        }

        _surahDownloadPrompt.value = surah
    }

    fun pauseAudioPlayback() {
        _isPlayingAudio.value = false
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectQari(qariName: String) {
        settingsManager.setQari(qariName)
        _playingQari.value = qariName
        // If already playing, restart with new Qari
        val currentSurah = _playingSurahName.value
        if (currentSurah.isNotEmpty() && _isPlayingAudio.value) {
            startAudioPlayback(currentSurah)
        }
    }

    fun stopAudio() {
        progressJob?.cancel()
        _audioProgress.value = 0f
        _isPlayingAudio.value = false
        _playingSurahName.value = ""
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
            }
            currentLoadedUrl = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playAzan() {
        val azanTypeVal = settingsManager.azanType.value
        if (azanTypeVal == "শুধু নোটিফিকেশন") {
            return
        }
        val url = when (azanTypeVal) {
            "Makkah" -> "https://www.islamcan.com/audio/adhan/makkah.mp3"
            "Madinah" -> "https://www.islamcan.com/audio/adhan/madina.mp3"
            "Mishary Rashid" -> "https://www.islamcan.com/audio/adhan/mishary.mp3"
            else -> "https://www.islamcan.com/audio/adhan/makkah.mp3"
        }

        // Stop Surah or Dua if playing
        stopAudio()
        _isPlayingDua.value = false

        _isPlayingAzan.value = true
        playUrl(url, isSurah = false, isAzan = true)
    }

    fun stopAzan() {
        progressJob?.cancel()
        _audioProgress.value = 0f
        _isPlayingAzan.value = false
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
            }
            currentLoadedUrl = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playDuaAudio(title: String) {
        // Al-Fatihah is the greatest Dua in the Quran, let's play it beautifully!
        val url = "https://server8.mp3quran.net/afs/001.mp3"

        // Stop Surah or Azan if playing
        stopAudio()
        _isPlayingAzan.value = false

        _playingDuaTitle.value = title
        _isPlayingDua.value = true
        playUrl(url, isSurah = false, isAzan = false, isDua = true)
    }

    fun stopDuaAudio() {
        progressJob?.cancel()
        _audioProgress.value = 0f
        _isPlayingDua.value = false
        _playingDuaTitle.value = ""
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
            }
            currentLoadedUrl = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectDua(dua: Dua?) {
        _selectedDua.value = dua
    }

    override fun onCleared() {
        super.onCleared()
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
