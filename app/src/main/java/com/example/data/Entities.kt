package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Prayer Tracker Entity
@Entity(tableName = "prayer_tracker")
data class PrayerTracker(
    @PrimaryKey val date: String, // format: "yyyy-MM-dd"
    val fajr: Boolean = false,
    val zuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false
)

// 2. Tasbih History Entity
@Entity(tableName = "tasbih_history")
data class TasbihRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format: "yyyy-MM-dd"
    val zikirText: String,
    val count: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// 3. Bookmarks Entity
@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "quran", "dua", "hadith"
    val itemId: String, // can be surah_id:ayah_id, or dua_id, etc.
    val title: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface DeenDao {
    // Prayer Tracker
    @Query("SELECT * FROM prayer_tracker WHERE date = :date")
    fun getPrayerTracker(date: String): Flow<PrayerTracker?>

    @Query("SELECT * FROM prayer_tracker")
    fun getAllPrayerTrackers(): Flow<List<PrayerTracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrayerTracker(tracker: PrayerTracker)

    // Tasbih Record
    @Query("SELECT * FROM tasbih_history ORDER BY timestamp DESC")
    fun getAllTasbihRecords(): Flow<List<TasbihRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbihRecord(record: TasbihRecord)

    @Query("DELETE FROM tasbih_history")
    suspend fun clearTasbihHistory()

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE type = :type AND itemId = :itemId")
    suspend fun deleteBookmark(type: String, itemId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE type = :type AND itemId = :itemId)")
    fun isBookmarked(type: String, itemId: String): Flow<Boolean>
}
