package com.ugraks.project1.data.local // Kendi paket adınız

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ugraks.project1.data.local.converter.Converters
import com.ugraks.project1.data.local.dao.CalorieRecordDao
import com.ugraks.project1.data.local.dao.DailySummaryDao
import com.ugraks.project1.data.local.dao.DailyStepDao // Yeni DAO'yu import edin
import com.ugraks.project1.data.local.dao.RecipeDao
import com.ugraks.project1.data.local.entity.CalorieRecordEntity
import com.ugraks.project1.data.local.entity.DailySummaryEntity
import com.ugraks.project1.data.local.entity.DailyStepEntity // Yeni Entity'yi import edin
import com.ugraks.project1.data.local.entity.RecipeEntity

@Database(
    // Yeni Entity'yi entities listesine ekleyin
    entities = [CalorieRecordEntity::class, DailySummaryEntity::class, DailyStepEntity::class,RecipeEntity::class],
    version = 1, // VERİTABANI VERSİYONUNU ARTIRIN!
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calorieRecordDao(): CalorieRecordDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun dailyStepDao(): DailyStepDao // Yeni DAO için abstract metot ekleyin
    abstract fun recipeDao(): RecipeDao
    // Eğer versiyonu artırdıysanız ama migrasyon yazmak istemiyorsanız (ve veri kaybı sorun değilse):
    // .fallbackToDestructiveMigration() çağrısını Room.databaseBuilder'a eklemeyi düşünebilirsiniz (Adım 5'te).
    // Ancak en doğru yol migrasyon yazmaktır. Boş başlangıçta sorun olmaz ama dikkatli olun.
}