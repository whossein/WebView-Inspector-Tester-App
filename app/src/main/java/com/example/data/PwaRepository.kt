package com.example.data

import kotlinx.coroutines.flow.Flow

class PwaRepository(private val dao: PwaDao) {
    val allProfiles: Flow<List<TestProfile>> = dao.getAllProfiles()
    val urlHistory: Flow<List<UrlHistory>> = dao.getRecentHistory()
    val allBookmarks: Flow<List<Bookmark>> = dao.getAllBookmarks()
    val appSettings: Flow<AppSettings?> = dao.getSettings()

    suspend fun saveProfile(profile: TestProfile) = dao.insertProfile(profile)
    suspend fun deleteProfile(id: Long) = dao.deleteProfile(id)
    suspend fun addHistory(url: String, title: String) {
        if (url.isBlank() || url.startsWith("about:")) return
        dao.insertHistory(UrlHistory(fullUrl = url, pageTitle = title))
    }
    suspend fun clearHistory() = dao.clearHistory()
    
    suspend fun addBookmark(title: String, url: String) = dao.insertBookmark(Bookmark(title = title, url = url))
    suspend fun deleteBookmark(id: Long) = dao.deleteBookmark(id)
    suspend fun updateSettings(settings: AppSettings) = dao.updateSettings(settings)
    
    suspend fun updateHomePage(url: String, currentSettings: AppSettings?) {
        val settings = currentSettings?.copy(homePageUrl = url) ?: AppSettings(id = 1, homePageUrl = url)
        dao.updateSettings(settings)
    }
}
