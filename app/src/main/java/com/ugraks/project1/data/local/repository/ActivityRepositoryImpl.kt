package com.ugraks.project1.data.local.repository

import android.content.Context
import com.ugraks.project1.R // asset_versions.xml için import
import com.ugraks.project1.data.local.dao.ActivityDao // ActivityDao import
import com.ugraks.project1.data.local.entity.ActivityEntity // ActivityEntity import
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Hilt ile tek bir instance sağlanacak
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao, // Hilt tarafından inject edilecek DAO
    @ApplicationContext private val context: Context // Hilt tarafından inject edilecek Application Context
) : ActivityRepository { // ActivityRepository arayüzünü uyguluyoruz

    // Asset versiyon kontrolü için SharedPreferences kullanacağız
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val FITNESS_ASSET_VERSION_KEY = "sports_asset_version"

    override suspend fun loadActivitiesFromAssets(context: Context, assetFileName: String) {
        withContext(Dispatchers.IO) { // I/O işlemleri için IO thread'ine geçiş
            try {
                // asset_versions.xml'den güncel versiyonu oku
                val currentAssetVersion = context.resources.getInteger(R.integer.sports_asset_version)
                // SharedPreferences'tan en son yüklenen versiyonu oku
                val lastLoadedVersion = sharedPreferences.getInt(FITNESS_ASSET_VERSION_KEY, 0)

                // Eğer güncel versiyon, en son yüklenen versiyondan büyükse veya hiç yükleme yapılmamışsa
                if (currentAssetVersion > lastLoadedVersion) {
                    // TXT dosyasından aktiviteleri oku ve parse et
                    val activities = parseActivitiesFromAsset(context, assetFileName)

                    // Veritabanını temizle ve yeni verileri ekle
                    activityDao.deleteAllActivities()
                    activityDao.insertActivities(activities)

                    // SharedPreferences'ı güncel versiyonla güncelle
                    with(sharedPreferences.edit()) {
                        putInt(FITNESS_ASSET_VERSION_KEY, currentAssetVersion)
                        apply()
                    }
                }
            } catch (e: Exception) {
                // Hata yönetimi (loglama vb.)
                e.printStackTrace()
                // Toast göstermek isterseniz Context kullanabilirsiniz ancak repository'de UI işlemleri genellikle yapılmaz
                // withContext(Dispatchers.Main) {
                //     Toast.makeText(context, "Failed to load activities: ${e.message}", Toast.LENGTH_SHORT).show()
                // }
            }
        }
    }

    private fun parseActivitiesFromAsset(context: Context, assetFileName: String): List<ActivityEntity> {
        val activities = mutableListOf<ActivityEntity>()
        context.assets.open(assetFileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // Yorum satırlarını ve boş satırları atla
                    if (line.isNullOrBlank() || line!!.trim().startsWith("#")) {
                        continue
                    }
                    val parts = line!!.split(",")
                    if (parts.size == 2) {
                        try {
                            val name = parts[0].trim()
                            val metValue = parts[1].trim().toDouble()
                            if (name.isNotEmpty() && metValue >= 0) { // Basit bir doğrulama
                                activities.add(ActivityEntity(name = name, metValue = metValue))
                            }
                        } catch (e: NumberFormatException) {
                            // MET değeri parse hatası (loglama)
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        return activities
    }

    override fun getAllActivities(): Flow<List<ActivityEntity>> {
        return activityDao.getAllActivities()
    }

    override suspend fun getActivityByName(activityName: String): ActivityEntity? {
        return activityDao.getActivityByName(activityName)
    }
}