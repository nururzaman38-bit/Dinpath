package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

data class SeasonalBanner(
    val title: String,
    val subtitle: String,
    val description: String,
    val emoji: String,
    val ayatOrHadith: String,
    val reference: String,
    val actionText: String,
    val targetScreen: String,
    val specialAmalList: List<String>
)

object SeasonalEventEngine {

    /**
     * Fetches current Hijri Date from Aladhan API online to keep Arabic calendar 100% accurate every year.
     * Fallback to local astronomical calculation if offline or API fails.
     */
    suspend fun syncHijriCalendarOnline(context: Context): PrayerTimeHelper.HijriDate? = withContext(Dispatchers.IO) {
        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val todayStr = sdf.format(Date())
            val urlStr = "https://api.aladhan.com/v1/gToH?date=$todayStr"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                connection.disconnect()

                val json = JSONObject(response)
                val data = json.getJSONObject("data").getJSONObject("hijri")
                val day = data.getString("day").toInt()
                val year = data.getString("year").toInt()
                val monthObj = data.getJSONObject("month")
                val monthNumber = monthObj.getInt("number")

                val banglaMonthNames = listOf(
                    "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জুমাদাল উলা", "জুমাদাল আখিরাহ",
                    "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
                )
                val monthName = if (monthNumber in 1..12) banglaMonthNames[monthNumber - 1] else "রমজান"

                // Save synced date in preferences
                val prefs = context.getSharedPreferences("deenpath_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putInt("synced_hijri_day", day)
                    .putString("synced_hijri_month", monthName)
                    .putInt("synced_hijri_year", year)
                    .putLong("synced_timestamp", System.currentTimeMillis())
                    .apply()

                return@withContext PrayerTimeHelper.HijriDate(day, monthName, year)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    /**
     * Determines the seasonal banner and amals based on current Hijri month, day, and day of week.
     */
    fun getSeasonalBanner(hijri: PrayerTimeHelper.HijriDate, calendar: Calendar): SeasonalBanner {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val month = hijri.monthName
        val day = hijri.day

        return when {
            // ১. পবিত্র রমজান মাস
            month == "রমজান" -> {
                SeasonalBanner(
                    title = "🌙 রমজানুল মোবারক ${hijri.year} হিজরি",
                    subtitle = "আজ রমজানের ${day}তম দিন — সিয়াম ও তাকওয়ার মাস",
                    description = "সাহরি ও ইফতারের সময়সূচী মেনে চলুন এবং বেশি বেশি কুরআন তিলাওয়াত ও ইস্তিগফার করুন।",
                    emoji = "🌙",
                    ayatOrHadith = "“রমজান মাস, যাতে কুরআন নাযিল করা হয়েছে মানুষের জন্য হেদায়েতস্বরূপ...”",
                    reference = "সূরা আল-বাকারাহ : ১৮৫",
                    actionText = "রমজান ট্র্যাকার ও আমল দেখুন",
                    targetScreen = "ramadan",
                    specialAmalList = listOf(
                        "সময়মতো সাহরি গ্রহণ ও ইফতার করা",
                        "পাঁচ ওয়াক্ত নামাজের সাথে তারাবীহ আদায় করা",
                        "কমপক্ষে ১ পারা বা নির্দিষ্ট পরিমাণ কুরআন তিলাওয়াত",
                        "বেশি বেশি ইস্তিগফার ও দুরুদ শরীফ পাঠ করা",
                        "দান-সাদাকা বৃদ্ধি করা ও অভাবীকে ইফতার করানো"
                    )
                )
            }
            // ২. জিলহজ মাস (হজ ও কোরবানি)
            month == "জিলহজ" && day in 1..13 -> {
                val isEid = day == 10
                val isArafa = day == 9
                val sub = when {
                    isEid -> "ঈদুল আজহা মোবারক! ত্যাগের মহিমায় সমুজ্জ্বল উৎসব।"
                    isArafa -> "পবিত্র আরাফাহর দিন — গুনাহ মাফের শ্রেষ্ঠ সুযোগ।"
                    else -> "জিলহজ মাসের শ্রেষ্ঠ ১০ দিন — ইবাদতের সুবর্ণ সময়।"
                }
                SeasonalBanner(
                    title = if (isEid) "🕋 ঈদুল আজহা মোবারক ${hijri.year}" else "🕋 জিলহজ মাসের বিশেষ আমল",
                    subtitle = sub,
                    description = "আল্লাহর নিকট জিলহজের প্রথম ১০ দিনের ইবাদতের চেয়ে অধিক প্রিয় অন্য কোনো দিনের ইবাদত নেই।",
                    emoji = "🕋",
                    ayatOrHadith = "“শপথ ভোরের এবং দশ রাতের...”",
                    reference = "সূরা আল-ফজর : ১-২",
                    actionText = "জিলহজের আমল ও তাকবীর দেখুন",
                    targetScreen = "dua",
                    specialAmalList = listOf(
                        "তাকবীরে তাশরীফ পাঠ: আল্লাহু আকবার, আল্লাহু আকবার, লা ইলাহা ইল্লাল্লাহু...",
                        if (isArafa) "আরাফাহর দিনে (৯ জিলহজ) রোজা রাখা (২ বছরের গুনাহ মাফ হয়)" else "১ থেকে ৯ জিলহজ নফল রোজা রাখা",
                        "কোরবানি দাতার জন্য জিলহজের চাঁদ দেখার পর থেকে চুল ও নখ না কাটা",
                        "ঈদের নামাজ আদায় ও আল্লাহর সন্তুষ্টির উদ্দেশ্যে কোরবানি করা",
                        "আত্মীয়-স্বজন ও দরিদ্রদের মাঝে কোরবানির মাংস বণ্টন করা"
                    )
                )
            }
            // ৩. মহররম ও আশুরা
            month == "মুহাররম" && day in 1..15 -> {
                val isAshura = day == 10
                SeasonalBanner(
                    title = if (isAshura) "🖤 পবিত্র আশুরা (১০ মহররম)" else "☪️ সম্মানিত মহররম মাস ও নতুন হিজরি বর্ষ",
                    subtitle = if (isAshura) "ঐতিহাসিক আশুরার তাৎপর্য ও রোজা" else "আল্লাহর মাস মহররম — ইবাদতের উত্তম সময়",
                    description = "রমজানের পর সর্বাধিক ফজিলতপূর্ণ রোজা হলো আল্লাহর মাস মহররমের রোজা।",
                    emoji = "☪️",
                    ayatOrHadith = "“রমজানের পর উত্তম রোজা হলো আল্লাহর মাস মহররমের রোজা...”",
                    reference = "সহীহ মুসলিম — ২৬১",
                    actionText = "আশুরার রোজা ও দোয়া দেখুন",
                    targetScreen = "hadith",
                    specialAmalList = listOf(
                        "আশুরার দিনে (১০ মহররম) এর সাথে ৯ বা ১১ মহররম মিলিয়ে মোট ২টি রোজা রাখা",
                        "পরিবারের সদস্যদের জন্য আশুরার দিনে উত্তম ও সাধ্যমতো ভালো খাবারের ব্যবস্থা করা",
                        "বিগত বছরের গুনাহ মাফের জন্য আল্লাহর দরবারে কায়মনোবাক্যে তওবা করা",
                        "কারবালার ঐতিহাসিক শিক্ষা থেকে সত্য ও ন্যায়ের ওপর অটল থাকার শপথ নেওয়া"
                    )
                )
            }
            // ৪. রবিউল আউয়াল (ঈদে মিলাদুন্নবী ও সিরাতুন্নবী)
            month == "রবিউল আউয়াল" -> {
                SeasonalBanner(
                    title = "🌹 পবিত্র রবিউল আউয়াল মাস",
                    subtitle = "বিশ্বনবী হযরত মুহাম্মদ (সাঃ) এর সিরাত ও জীবনাদর্শ",
                    description = "রাহমাতুল্লিল আলামিন হযরত মুহাম্মদ (সাঃ) এর প্রতি অধিক পরিমাণে দুরুদ ও সালাম পেশ করুন।",
                    emoji = "🌹",
                    ayatOrHadith = "“নিশ্চয়ই আল্লাহ ও তাঁর ফেরেশতাগণ নবীর প্রতি দরুদ প্রেরণ করেন। হে ঈমানদারগণ! তোমরাও তাঁর প্রতি দরুদ ও সালাম পেশ করো।”",
                    reference = "সূরা আল-আহযাব : ৫৬",
                    actionText = "দুরুদ শরীফ ও সিরাত পড়ুন",
                    targetScreen = "tasbih",
                    specialAmalList = listOf(
                        "প্রতিদিন অধিক পরিমাণে দুরুদ শরীফ (দুরুদে ইব্রাহিম বা ছোট দুরুদ) পাঠ করা",
                        "রাসূলুল্লাহ (সাঃ) এর পবিত্র জীবনী ও সুন্নাহ সম্পর্কে অধ্যয়ন করা",
                        "ব্যক্তিগত, পারিবারিক ও সামাজিক জীবনে রাসূলের (সাঃ) সুন্নাহ বাস্তবায়ন করা",
                        "উত্তম চরিত্র ও সুন্দর ব্যবহারের মাধ্যমে ইসলামের সৌন্দর্য ফুটিয়ে তোলা"
                    )
                )
            }
            // ৫. শাবান মাস (শবে বরাত ও রমজানের প্রস্তুতি)
            month == "শাবান" -> {
                val isBarat = day in 14..15
                SeasonalBanner(
                    title = if (isBarat) "✨ পবিত্র শবে বরাত (লাইলাতুল বারাআত)" else "🤲 শাবান মাস — রমজানের প্রস্তুতি",
                    subtitle = if (isBarat) "ক্ষমা প্রার্থনার মহিমান্বিত রজনী" else "রমজানের আগমনে মন ও আত্মার প্রস্তুতি",
                    description = "রাসূলুল্লাহ (সাঃ) শাবান মাসে সর্বাধিক নফল রোজা রাখতেন এবং রমজানের জন্য প্রস্তুতি নিতেন।",
                    emoji = "✨",
                    ayatOrHadith = "“হে আল্লাহ! রজব ও শাবান মাসে আমাদের বরকত দিন এবং আমাদেরকে রমজান পর্যন্ত পৌঁছে দিন।”",
                    reference = "মুসনাদে আহমাদ",
                    actionText = "শবে বরাতের আমল ও দোয়া",
                    targetScreen = "dua",
                    specialAmalList = listOf(
                        "শবে বরাতের রাতে একনিষ্ঠভাবে আল্লাহর দরবারে তওবা ও ইস্তিগফার করা",
                        "নফল নামাজ, কুরআন তিলাওয়াত ও জিকিরের মাধ্যমে রাত অতিবাহিত করা",
                        "পরদিন (১৫ শাবান) নফল রোজা রাখা",
                        "রমজানের রোজার প্রস্তুতি নেওয়া এবং বিগত বছরের কাজা রোজা থাকলে আদায় করা"
                    )
                )
            }
            // ৬. রজব মাস (শবে মেরাজ)
            month == "রজব" -> {
                val isMeraj = day in 26..27
                SeasonalBanner(
                    title = if (isMeraj) "🌌 পবিত্র শবে মেরাজ (২৭ রজব)" else "🌙 সম্মানিত রজব মাস",
                    subtitle = if (isMeraj) "রাসূল (সাঃ) এর ঊর্ধ্বগমন ও নামাজের উপহার" else "চারটি সম্মানিত মাসের একটি রজব",
                    description = "মেরাজের রজনীতে আল্লাহ তাআলা উম্মতে মুহাম্মদীর জন্য পাঁচ ওয়াক্ত নামাজ উপহার দিয়েছেন।",
                    emoji = "🌌",
                    ayatOrHadith = "“পবিত্র মহান সে সত্তা, যিনি তাঁর বান্দাকে রাতে ভ্রমণ করিয়েছেন মাসজিদুল হারাম থেকে মাসজিদুল আকসা পর্যন্ত...”",
                    reference = "সূরা বনী ইসরাঈল : ১",
                    actionText = "মেরাজের শিক্ষা ও নামাজ ট্র্যাকার",
                    targetScreen = "salah",
                    specialAmalList = listOf(
                        "পাঁচ ওয়াক্ত নামাজ অত্যন্ত গুরুত্ব ও খুশু-খুযুর সাথে আদায় করা",
                        "রজব মাসের বরকত লাভের দোয়া বেশি বেশি পাঠ করা",
                        "নফল রোজা ও দান-সাদাকা করা",
                        "মেরাজের ঐতিহাসিক শিক্ষা ও নামাজের গুরুত্ব উপলব্ধি করা"
                    )
                )
            }
            // ৭. শাওয়াল মাস (ঈদুল ফিতর ও ৬ রোজা)
            month == "শাওয়াল" && day in 1..15 -> {
                val isEid = day == 1
                SeasonalBanner(
                    title = if (isEid) "🎉 ঈদুল ফিতর মোবারক!" else "🌟 শাওয়াল মাসের ৬ রোজা",
                    subtitle = if (isEid) "দীর্ঘ ১ মাস সিয়াম সাধনার আনন্দময় সমাপ্তি" else "রমজানের পর ৬ রোজা — সারা বছর রোজা রাখার সওয়াব",
                    description = "রমজানের রোজা রাখার পর শাওয়াল মাসে ৬টি রোজা রাখলে পুরো বছর রোজা রাখার সওয়াব পাওয়া যায়।",
                    emoji = "🎉",
                    ayatOrHadith = "“যে ব্যক্তি রমজানের রোজা রাখল, অতঃপর শাওয়াল মাসে ৬টি রোজা রাখল, সে যেন সারা বছর রোজা রাখল।”",
                    reference = "সহীহ মুসলিম — ১১৬৪",
                    actionText = "শাওয়ালের আমল ও হাদিস",
                    targetScreen = "hadith",
                    specialAmalList = listOf(
                        "ঈদের দিন সদকাতুল ফিতর আদায় করা ও ঈদের নামাজ পড়া",
                        "শাওয়াল মাসের যেকোনো সময়ে (ঈদের পর) ৬টি নফল রোজা রাখা",
                        "রমজানে অর্জিত তাকওয়া ও ইবাদতের ধারাবাহিকতা বজায় রাখা"
                    )
                )
            }
            // ৮. আইয়ামে বীজ (মাসের ১৩, ১৪, ১৫ তারিখ)
            day in 13..15 -> {
                SeasonalBanner(
                    title = "🌕 আইয়ামে বীজের নফল রোজা",
                    subtitle = "হিজরি মাসের ১৩, ১৪ ও ১৫ তারিখের সুন্নত রোজা",
                    description = "প্রতি আরবি মাসের ১৩, ১৪ ও ১৫ তারিখে রোজা রাখলে সারা মাস রোজা রাখার সমান সওয়াব পাওয়া যায়।",
                    emoji = "🌕",
                    ayatOrHadith = "“প্রতি মাসে ৩ দিন রোজা রাখা সারা বছর রোজা রাখার সমতুল্য।”",
                    reference = "সহীহ বুখারী — ১৯৭৪",
                    actionText = "নফল রোজা ও ফজিলত",
                    targetScreen = "hadith",
                    specialAmalList = listOf(
                        "আজ এবং আগামীকাল নফল রোজা রাখার নিয়ত করা",
                        "বেশি বেশি কুরআন তিলাওয়াত ও জিকির করা",
                        "আল্লাহর দরবারে শুকরিয়া আদায় করা"
                    )
                )
            }
            // ৯. জুম্মাবার (শুক্রবার)
            dayOfWeek == Calendar.FRIDAY -> {
                SeasonalBanner(
                    title = "🕌 পবিত্র জুম্মাবার — সপ্তাহের সেরা দিন",
                    subtitle = "জুম্মার বিশেষ আমল ও দোয়া কবুলের সময়",
                    description = "শুক্রবার সপ্তাহের শ্রেষ্ঠ দিন। এই দিনে সূরা কাহফ তিলাওয়াত ও বেশি বেশি দুরুদ পাঠ অত্যন্ত ফজিলতপূর্ণ।",
                    emoji = "🕌",
                    ayatOrHadith = "“যে ব্যক্তি জুম্মার দিনে সূরা আল-কাহফ তিলাওয়াত করবে, তার জন্য এক জুম্মা থেকে অপর জুম্মা পর্যন্ত নূর আলোকিত থাকবে।”",
                    reference = "সুনানে নাসায়ী",
                    actionText = "জুম্মার আমল ও সূরা কাহফ পড়ুন",
                    targetScreen = "quran",
                    specialAmalList = listOf(
                        "উত্তমরূপে গোসল করা, পরিষ্কার বা নতুন পোশাক পরা ও সুগন্ধি ব্যবহার করা",
                        "আগে আগে মসজিদে যাওয়া ও মনোযোগ দিয়ে খুতবা শোনা",
                        "সূরা আল-কাহফ তিলাওয়াত করা (কুরআন সেকশনের ১৮ নং সূরা)",
                        "রাসূলুল্লাহ (সাঃ) এর প্রতি বেশি বেশি দুরুদ শরীফ পাঠ করা",
                        "আসর থেকে মাগরিবের মধ্যবর্তী সময়ে দোয়া কবুলের মুহূর্তে বিশেষ মুনাজাত করা"
                    )
                )
            }
            // ১০. সাধারণ দিন (Default Daily Guidance)
            else -> {
                SeasonalBanner(
                    title = "✨ দৈনন্দিন ইবাদত ও আত্মশুদ্ধি",
                    subtitle = "${hijri.day} ${hijri.monthName}, ${hijri.year} হিজরি",
                    description = "প্রতিটি দিন আল্লাহর নিয়ামত। পাঁচ ওয়াক্ত নামাজ সময়মতো আদায় করুন এবং জিকিরে জিহ্বা সিক্ত রাখুন।",
                    emoji = "✨",
                    ayatOrHadith = "“তোমরা আমাকে স্মরণ করো, আমিও তোমাদের স্মরণ করব। আর আমার কৃতজ্ঞতা প্রকাশ করো, অকৃতজ্ঞ হইও না।”",
                    reference = "সূরা আল-বাকারাহ : ১৫২",
                    actionText = "দৈনন্দিন দোয়া ও জিকির করুন",
                    targetScreen = "dua",
                    specialAmalList = listOf(
                        "পাঁচ ওয়াক্ত নামাজ জামাতের সাথে সময়মতো আদায় করা",
                        "সকাল-সন্ধ্যার মাসনুন জিকির ও দোয়াগুলো পাঠ করা",
                        "পিতা-মাতা ও পরিবারের সাথে উত্তম আচরণ করা",
                        "হালাল উপার্জন ও সৎ কাজের আদেশ দেওয়া"
                    )
                )
            }
        }
    }
}
