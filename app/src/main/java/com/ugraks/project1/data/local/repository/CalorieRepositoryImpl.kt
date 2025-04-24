package com.ugraks.project1.data.local.repository

import android.content.Context
import com.ugraks.project1.KeepNoteComposable.FoodItemKeepNote // FoodItemKeepNote importu
import com.ugraks.project1.KeepNoteComposable.readFoodItemsFromAssets // Asset okuma fonksiyonu importu
import com.ugraks.project1.data.local.dao.CalorieRecordDao // CalorieRecordDao importu
import com.ugraks.project1.data.local.dao.DailySummaryDao // DailySummaryDao importu
import com.ugraks.project1.data.local.dao.FoodItemDao // FoodItemDao importu (Veritabanında FoodItemEntity var)
import com.ugraks.project1.data.local.entity.CalorieRecordEntity // CalorieRecordEntity importu
import com.ugraks.project1.data.local.entity.DailySummaryEntity // DailySummaryEntity importu
import com.ugraks.project1.data.repository.CalorieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Hilt ile tek örnek olarak sağlanacak (implementasyon sınıfı)
class CalorieRepositoryImpl @Inject constructor(
    // Repository Implementasyonu, bağımlılıklarını burada alır
    private val calorieRecordDao: CalorieRecordDao, // Kalori kayıtları DAO'su
    private val dailySummaryDao: DailySummaryDao,   // Günlük özetler DAO'su
    private val foodItemDao: FoodItemDao,           // FoodItemEntity DAO'su
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: Context // Asset okuma için uygulama Context'i
) : CalorieRepository { // <-- CalorieRepository arayüzünü uyguladığını belirtiriz

    // --- Food Item Operasyonları (Şimdilik Asset'ten Okuma) ---
    // Arayüzden gelen metot implementasyonu
    override fun getFoodItemsFromAssets(): List<FoodItemKeepNote> {
        // Asset'ten okuma mantığı burada yer alır
        return readFoodItemsFromAssets(appContext)
    }

    // --- Kalori Kaydı Operasyonları (Room) ---
    // Arayüzden gelen metot implementasyonları
    override fun getAllCalorieRecords(): Flow<List<CalorieRecordEntity>> {
        return calorieRecordDao.getAllRecords() // DAO'daki metodu çağırır
    }

    override suspend fun insertCalorieRecord(record: CalorieRecordEntity) {
        calorieRecordDao.insertRecord(record) // DAO'daki metodu çağırır
    }

    override suspend fun deleteCalorieRecord(record: CalorieRecordEntity) {
        calorieRecordDao.deleteRecord(record) // DAO'daki metodu çağırır
    }

    override suspend fun deleteAllCalorieRecords() {
        calorieRecordDao.deleteAllRecords() // DAO'daki metotu çağırır
    }

    // --- Günlük Özet Operasyonları (Room) ---
    // Arayüzden gelen metot implementasyonları
    override fun getAllDailySummaries(): Flow<List<DailySummaryEntity>> {
        return dailySummaryDao.getAllSummaries() // DAO'daki metodu çağırır
    }

    override suspend fun getDailySummaryByDate(date: String): DailySummaryEntity? {
        return dailySummaryDao.getSummaryByDate(date) // DAO'daki metodu çağırır
    }

    override suspend fun insertOrUpdateDailySummary(summary: DailySummaryEntity) {
        dailySummaryDao.insertOrUpdateSummary(summary) // DAO'daki metodu çağırır
    }

    override suspend fun deleteAllDailySummaries() {
        dailySummaryDao.deleteAllSummaries() // DAO'daki metotu çağırır
    }
    override suspend fun deleteDailySummary(summary: DailySummaryEntity) {
        // DailySummaryDao'daki deleteSummary metodunu çağırarak implementasyonu sağlıyoruz
        dailySummaryDao.deleteSummary(summary)
    }

    // NOT: DailySummaryEntity yapınıza göre metot implementasyonlarını kontrol edin.
}