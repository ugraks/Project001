package com.ugraks.project1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ugraks.project1.data.local.dao.CalorieRecordDao // DAO'ları import edin
import com.ugraks.project1.data.local.dao.DailySummaryDao
import com.ugraks.project1.data.local.entity.CalorieRecordEntity // Entity'leri import edin
import com.ugraks.project1.data.local.entity.DailySummaryEntity

// Veritabanı sınıfı
@Database(
    entities = [CalorieRecordEntity::class, DailySummaryEntity::class], // Veritabanındaki tüm Entity'leri listele
    version = 1, // Veritabanı şeması değiştiğinde bu versiyon numarasını artırın
    exportSchema = false // Şema dosyalarını dışa aktarmak isterseniz true yapın (versiyonlama ve migrasyon için önerilir)
)
abstract class AppDatabase : RoomDatabase() {
    // DAO'lara erişim için soyut metodlar tanımlayın
    abstract fun calorieRecordDao(): CalorieRecordDao
    abstract fun dailySummaryDao(): DailySummaryDao

    // Genellikle burada Singleton companion object deseni oluşturulur,
    // ancak Hilt kullanacağımız için Hilt modülü veritabanı örneğini sağlayacaktır.
}