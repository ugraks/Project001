package com.ugraks.project1.data.local.repository

import com.ugraks.project1.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

// Aktivite verilerine erişim için Repository arayüzü
interface ActivityRepository {

    // Uygulama başlangıcında veya asset versiyonu değiştiğinde veriyi yükleme metodu
    // Bu metodun implementasyonu asset'ten okuma ve Room'a yazma logic'ini içerecek
    suspend fun loadActivitiesFromAssets(context: android.content.Context, assetFileName: String)

    // Tüm aktiviteleri Flow olarak getiren metot (UI gözlemleyebilir)
    fun getAllActivities(): Flow<List<ActivityEntity>>

    // Belirli bir aktiviteyi adına göre getiren metot
    suspend fun getActivityByName(activityName: String): ActivityEntity?
}