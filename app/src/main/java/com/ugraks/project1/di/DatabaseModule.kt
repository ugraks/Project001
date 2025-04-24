package com.ugraks.project1.di // Yeni paket

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext // Application Context sağlamak için
import dagger.hilt.components.SingletonComponent // Uygulama yaşam döngüsü boyunca geçerli olacak bileşen
import com.ugraks.project1.data.local.AppDatabase // Database sınıfını import edin
import com.ugraks.project1.data.local.dao.ActivityDao
import com.ugraks.project1.data.local.dao.BoxingItemDao
import com.ugraks.project1.data.local.dao.CalorieRecordDao // DAO'ları import edin
import com.ugraks.project1.data.local.dao.DailyStepDao
import com.ugraks.project1.data.local.dao.DailySummaryDao
import com.ugraks.project1.data.local.dao.ExerciseDao
import com.ugraks.project1.data.local.dao.FoodItemDao
import com.ugraks.project1.data.local.dao.RecipeDao
import javax.inject.Singleton // Tek örnek olacağını belirtir

@Module // Bu bir Hilt modülüdür
@InstallIn(SingletonComponent::class) // Uygulama yaşam döngüsü boyunca geçerli olacak bağımlılıkları sağlar
object DatabaseModule { // object -> Singleton olmasını sağlar

    // Room Database örneğini sağlar. Uygulama boyunca tek bir örnek olacak.
    @Provides // Bu metod bir bağımlılık sağlar
    @Singleton // Sağlanan bağımlılığın tek bir örneği olacak
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context, // Application Context
            AppDatabase::class.java, // Database sınıfı
            "app_database" // Veritabanı dosyasının adı (dahili depolamada)
        )
            // Eğer veritabanı versiyonu ileride artarsa migrasyon eklemelisiniz.
            // Migrasyon eklemeyecekseniz ve yapı değişiminde verinin silinip yeniden oluşturulmasını kabul ediyorsanız:
            // .fallbackToDestructiveMigration() // Bu senaryoda veri kaybı önemli değilse kullanılabilir



            // TEST AŞAMASINDA: Versiyon uyuşmazlığında veritabanını sil ve yeniden oluştur.
            // YAYINLAMADAN ÖNCE BU SATIRI SİLİN!
            .fallbackToDestructiveMigration() // TEST AMAÇLI EKLENDİ

            // Migrasyonları ekleyin. fallback aktifken bunlar ÇALIŞMAZ, ama kodda dursun.
        /*.addMigrations(
                Migrations.MIGRATION_1_2, // Versiyon 1 -> 2 migrasyonu (Adımsayar)
                Migrations.MIGRATION_2_3, // Versiyon 2 -> 3 migrasyonu (Tarif String ingredients)
                Migrations.MIGRATION_3_4  // Versiyon 3 -> 4 migrasyonu (Tarif List<String> ingredients)
            )*/
            .build()
    }

    // CalorieRecordDao örneğini sağlar
    @Provides
    @Singleton
    fun provideCalorieRecordDao(db: AppDatabase): CalorieRecordDao {
        return db.calorieRecordDao() // Database örneğinden DAO'yu alır
    }

    // DailySummaryDao örneğini sağlar
    @Provides
    @Singleton
    fun provideDailySummaryDao(db: AppDatabase): DailySummaryDao {
        return db.dailySummaryDao() // Database örneğinden DAO'yu alır
    }

    @Provides // Yeni metot
    @Singleton
    fun provideDailyStepDao(db: AppDatabase): DailyStepDao {
        return db.dailyStepDao() // Database örneğinden yeni DAO'yu sağlar
    }

    @Provides // Yeni metot
    @Singleton
    fun provideRecipeDao(db: AppDatabase): RecipeDao {
        return db.recipeDao() // Database örneğinden yeni DAO'yu sağlar
    }

    @Provides // Yeni metot
    @Singleton
    fun provideFoodItemDao(db: AppDatabase): FoodItemDao {
        return db.foodItemDao() // Database örneğinden FoodItem DAO'yu alır
    }

    @Provides // <-- YENİ METOT
    @Singleton // <-- Singleton olarak sağlayın
    fun provideExerciseDao(db: AppDatabase): ExerciseDao {
        return db.exerciseDao() // Database örneğinden yeni DAO'yu sağlar
    }

    @Provides // <-- YENİ METOT
    @Singleton // <-- Singleton olarak sağlayın
    fun provideBoxingItemDao(db: AppDatabase): BoxingItemDao {
        return db.boxingItemDao() // Database örneğinden yeni DAO'yu sağlar
    }

    @Provides // Bu fonksiyonun bir bağımlılık sağladığını belirtir
    @Singleton // Sağlanan bağımlılığın uygulamanın ömrü boyunca tek bir instance olacağını belirtir
    fun provideActivityDao(db: AppDatabase): ActivityDao {
        return db.activityDao()
    }

    // Eğer FoodItem'ları asset'ten okuyan kodu Room'a taşımadıysanız ve ViewModel'da Context kullanmak istemiyorsanız,
    // o okuma işini yapan bir sınıf oluşturup onu da burada sağlayabilirsiniz.
    // @Provides
    // @Singleton // Eğer okuyucunun tek örneği yeterliyse
    // fun provideFoodItemReader(@ApplicationContext context: Context): FoodItemReader { // FoodItemReader Composable dosyasındaki fonksiyonu sarmalayan bir sınıf olabilir
    //    return FoodItemReader(context)
    // }
}

