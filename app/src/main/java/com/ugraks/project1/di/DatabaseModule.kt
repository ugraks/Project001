package com.ugraks.project1.di // Yeni paket

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext // Application Context sağlamak için
import dagger.hilt.components.SingletonComponent // Uygulama yaşam döngüsü boyunca geçerli olacak bileşen
import com.ugraks.project1.data.local.AppDatabase // Database sınıfını import edin
import com.ugraks.project1.data.local.dao.CalorieRecordDao // DAO'ları import edin
import com.ugraks.project1.data.local.dao.DailySummaryDao
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
            "calorie_app_database" // Veritabanı dosyasının adı (dahili depolamada)
        )
            // Eğer veritabanı versiyonu ileride artarsa migrasyon eklemelisiniz.
            // Migrasyon eklemeyecekseniz ve yapı değişiminde verinin silinip yeniden oluşturulmasını kabul ediyorsanız:
            // .fallbackToDestructiveMigration() // Bu senaryoda veri kaybı önemli değilse kullanılabilir
            .build()
    }

    // CalorieRecordDao örneğini sağlar
    @Provides
    fun provideCalorieRecordDao(db: AppDatabase): CalorieRecordDao {
        return db.calorieRecordDao() // Database örneğinden DAO'yu alır
    }

    // DailySummaryDao örneğini sağlar
    @Provides
    fun provideDailySummaryDao(db: AppDatabase): DailySummaryDao {
        return db.dailySummaryDao() // Database örneğinden DAO'yu alır
    }

    // Eğer FoodItem'ları asset'ten okuyan kodu Room'a taşımadıysanız ve ViewModel'da Context kullanmak istemiyorsanız,
    // o okuma işini yapan bir sınıf oluşturup onu da burada sağlayabilirsiniz.
    // @Provides
    // @Singleton // Eğer okuyucunun tek örneği yeterliyse
    // fun provideFoodItemReader(@ApplicationContext context: Context): FoodItemReader { // FoodItemReader Composable dosyasındaki fonksiyonu sarmalayan bir sınıf olabilir
    //    return FoodItemReader(context)
    // }
}

