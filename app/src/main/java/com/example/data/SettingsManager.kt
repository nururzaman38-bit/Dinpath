package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("deenpath_prefs", Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(isOnboardingCompleted())
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted

    private val _language = MutableStateFlow(getLanguage())
    val language: StateFlow<String> = _language

    private val _theme = MutableStateFlow(getTheme())
    val theme: StateFlow<String> = _theme
    val themeName: StateFlow<String> = _theme

    private val _calculationMethod = MutableStateFlow(getCalculationMethod())
    val calculationMethod: StateFlow<String> = _calculationMethod

    private val _madhab = MutableStateFlow(getMadhab())
    val madhab: StateFlow<String> = _madhab

    private val _location = MutableStateFlow(getLocation())
    val location: StateFlow<String> = _location

    private val _fajrNotification = MutableStateFlow(getNotificationEnabled("fajr"))
    val fajrNotification: StateFlow<Boolean> = _fajrNotification

    private val _zuhrNotification = MutableStateFlow(getNotificationEnabled("zuhr"))
    val zuhrNotification: StateFlow<Boolean> = _zuhrNotification

    private val _asrNotification = MutableStateFlow(getNotificationEnabled("asr"))
    val asrNotification: StateFlow<Boolean> = _asrNotification

    private val _maghribNotification = MutableStateFlow(getNotificationEnabled("maghrib"))
    val maghribNotification: StateFlow<Boolean> = _maghribNotification

    private val _ishaNotification = MutableStateFlow(getNotificationEnabled("isha"))
    val ishaNotification: StateFlow<Boolean> = _ishaNotification

    private val _azanType = MutableStateFlow(getAzanType())
    val azanType: StateFlow<String> = _azanType

    private val _fontSize = MutableStateFlow(getFontSize())
    val fontSize: StateFlow<String> = _fontSize

    private val _arabicFont = MutableStateFlow(getArabicFont())
    val arabicFont: StateFlow<String> = _arabicFont

    private val _showTranslation = MutableStateFlow(getShowTranslation())
    val showTranslation: StateFlow<Boolean> = _showTranslation

    private val _showPronunciation = MutableStateFlow(getShowPronunciation())
    val showPronunciation: StateFlow<Boolean> = _showPronunciation

    private val _completedRoshasCount = MutableStateFlow(getCompletedRoshasCount())
    val completedRoshasCount: StateFlow<Int> = _completedRoshasCount

    private val _qari = MutableStateFlow(getQari())
    val qari: StateFlow<String> = _qari

    private val _locationAutoDetected = MutableStateFlow(isLocationAutoDetected())
    val locationAutoDetected: StateFlow<Boolean> = _locationAutoDetected

    fun getQari(): String = prefs.getString("selected_qari", "Mishary Rashid Al-Afasy") ?: "Mishary Rashid Al-Afasy"
    fun setQari(qariName: String) {
        prefs.edit().putString("selected_qari", qariName).apply()
        _qari.value = qariName
    }

    fun isLocationAutoDetected(): Boolean = prefs.getBoolean("location_auto_detected", false)
    fun setLocationAutoDetected(auto: Boolean) {
        prefs.edit().putBoolean("location_auto_detected", auto).apply()
        _locationAutoDetected.value = auto
    }

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean("onboarding_completed", false)
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _onboardingCompleted.value = completed
    }

    fun getLanguage(): String = prefs.getString("language", "BN") ?: "BN"
    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _language.value = lang
    }

    fun getTheme(): String = prefs.getString("theme", "DARK") ?: "DARK"
    fun setTheme(theme: String) {
        prefs.edit().putString("theme", theme).apply()
        _theme.value = theme
    }
    fun setThemeName(theme: String) {
        setTheme(theme)
    }

    fun getCalculationMethod(): String = prefs.getString("cal_method", "University of Islamic Sciences, Karachi") ?: "University of Islamic Sciences, Karachi"
    fun setCalculationMethod(method: String) {
        prefs.edit().putString("cal_method", method).apply()
        _calculationMethod.value = method
    }

    fun getMadhab(): String = prefs.getString("madhab", "হানাফী") ?: "হানাফী"
    fun setMadhab(madhab: String) {
        prefs.edit().putString("madhab", madhab).apply()
        _madhab.value = madhab
    }

    fun getLocation(): String = prefs.getString("location", "ঢাকা") ?: "ঢাকা"
    fun setLocation(loc: String) {
        prefs.edit().putString("location", loc).apply()
        _location.value = loc
    }

    fun getNotificationEnabled(prayer: String): Boolean = prefs.getBoolean("notif_$prayer", true)
    fun setNotificationEnabled(prayer: String, enabled: Boolean) {
        prefs.edit().putBoolean("notif_$prayer", enabled).apply()
        when (prayer) {
            "fajr" -> _fajrNotification.value = enabled
            "zuhr" -> _zuhrNotification.value = enabled
            "asr" -> _asrNotification.value = enabled
            "maghrib" -> _maghribNotification.value = enabled
            "isha" -> _ishaNotification.value = enabled
        }
    }

    fun getAzanType(): String = prefs.getString("azan_type", "Makkah") ?: "Makkah"
    fun setAzanType(type: String) {
        prefs.edit().putString("azan_type", type).apply()
        _azanType.value = type
    }

    fun getFontSize(): String = prefs.getString("font_size", "মাঝারি") ?: "মাঝারি"
    fun setFontSize(size: String) {
        prefs.edit().putString("font_size", size).apply()
        _fontSize.value = size
    }

    fun getArabicFont(): String = prefs.getString("arabic_font", "Uthmanic") ?: "Uthmanic"
    fun setArabicFont(font: String) {
        prefs.edit().putString("arabic_font", font).apply()
        _arabicFont.value = font
    }

    fun getShowTranslation(): Boolean = prefs.getBoolean("show_translation", true)
    fun setShowTranslation(show: Boolean) {
        prefs.edit().putBoolean("show_translation", show).apply()
        _showTranslation.value = show
    }

    fun getShowPronunciation(): Boolean = prefs.getBoolean("show_pronunciation", true)
    fun setShowPronunciation(show: Boolean) {
        prefs.edit().putBoolean("show_pronunciation", show).apply()
        _showPronunciation.value = show
    }

    fun getCompletedRoshasCount(): Int = prefs.getInt("completed_roshas_count", 14)
    fun setCompletedRoshasCount(count: Int) {
        prefs.edit().putInt("completed_roshas_count", count).apply()
        _completedRoshasCount.value = count
    }

    private val _customApiKey = MutableStateFlow(getCustomApiKey())
    val customApiKey: StateFlow<String> = _customApiKey

    private val _customModelId = MutableStateFlow(getCustomModelId())
    val customModelId: StateFlow<String> = _customModelId

    private val _customDuasJson = MutableStateFlow(getCustomDuasJson())
    val customDuasJson: StateFlow<String> = _customDuasJson

    fun getCustomApiKey(): String = prefs.getString("custom_openrouter_api_key", "") ?: ""
    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_openrouter_api_key", key).apply()
        _customApiKey.value = key
    }

    fun getCustomModelId(): String = prefs.getString("custom_openrouter_model_id", "") ?: ""
    fun setCustomModelId(modelId: String) {
        prefs.edit().putString("custom_openrouter_model_id", modelId).apply()
        _customModelId.value = modelId
    }

    fun getCustomDuasJson(): String = prefs.getString("custom_duas_json", "[]") ?: "[]"
    fun setCustomDuasJson(jsonStr: String) {
        prefs.edit().putString("custom_duas_json", jsonStr).apply()
        _customDuasJson.value = jsonStr
    }

    // Admin Secret Access PIN & Status
    private val _adminSecretPin = MutableStateFlow(getAdminSecretPin())
    val adminSecretPin: StateFlow<String> = _adminSecretPin

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked

    fun getAdminSecretPin(): String = prefs.getString("admin_secret_pin", "050126") ?: "050126"
    fun setAdminSecretPin(pin: String) {
        prefs.edit().putString("admin_secret_pin", pin).apply()
        _adminSecretPin.value = pin
    }
    fun setAdminUnlocked(unlocked: Boolean) {
        _isAdminUnlocked.value = unlocked
    }

    // Custom Scraped / Admin Feed & Quizzes
    private val _customFeedJson = MutableStateFlow(getCustomFeedJson())
    val customFeedJson: StateFlow<String> = _customFeedJson

    private val _customQuizJson = MutableStateFlow(getCustomQuizJson())
    val customQuizJson: StateFlow<String> = _customQuizJson

    private val _quizScore = MutableStateFlow(getQuizScore())
    val quizScore: StateFlow<Int> = _quizScore

    fun getCustomFeedJson(): String = prefs.getString("custom_feed_json", "[]") ?: "[]"
    fun setCustomFeedJson(jsonStr: String) {
        prefs.edit().putString("custom_feed_json", jsonStr).apply()
        _customFeedJson.value = jsonStr
    }

    fun getCustomQuizJson(): String = prefs.getString("custom_quiz_json", "[]") ?: "[]"
    fun setCustomQuizJson(jsonStr: String) {
        prefs.edit().putString("custom_quiz_json", jsonStr).apply()
        _customQuizJson.value = jsonStr
    }

    fun getQuizScore(): Int = prefs.getInt("quiz_score", 0)
    fun addQuizScore(points: Int) {
        val newScore = getQuizScore() + points
        prefs.edit().putInt("quiz_score", newScore).apply()
        _quizScore.value = newScore
    }
}

