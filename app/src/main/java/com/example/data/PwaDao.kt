package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PwaDao {
    @Query("SELECT * FROM test_profiles ORDER BY timestamp DESC")
    fun getAllProfiles(): Flow<List<TestProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: TestProfile): Long

    @Query("DELETE FROM test_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Long)

    @Query("SELECT * FROM url_history ORDER BY timestamp DESC LIMIT 30")
    fun getRecentHistory(): Flow<List<UrlHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: UrlHistory): Long

    @Query("DELETE FROM url_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: AppSettings)
}
