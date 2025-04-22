package com.ugraks.project1.ui.viewmodels

import android.content.Context // Asset okumak için gerekirse
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Coroutine scope için
import com.ugraks.project1.data.local.dao.CalorieRecordDao // DAO'ları import edin
import com.ugraks.project1.data.local.dao.DailySummaryDao
import com.ugraks.project1.data.local.entity.CalorieRecordEntity // Entity'leri import edin
import com.ugraks.project1.data.local.entity.DailySummaryEntity
import com.ugraks.project1.KeepNoteComposable.FoodItemKeepNote // Eğer hala asset'ten okunuyorsa data class'ı
import com.ugraks.project1.KeepNoteComposable.readFoodItemsFromAssets // Asset okuma fonksiyonu
import dagger.hilt.android.lifecycle.HiltViewModel // Hilt ViewModel annotation
import dagger.hilt.android.qualifiers.ApplicationContext // Context inject etmek için
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch // Coroutine başlatmak için
import java.time.LocalDate // Tarih için (API 26+)
import javax.inject.Inject // Bağımlılıkları inject etmek için

// Hilt tarafından ViewModel olarak sağlanacağını belirtir
@HiltViewModel
class CalorieViewModel @Inject constructor(
    private val calorieRecordDao: CalorieRecordDao, // Hilt bu DAO'yu sağlar
    private val dailySummaryDao: DailySummaryDao,   // Hilt bu DAO'yu sağlar
    @ApplicationContext private val applicationContext: Context // Asset okumak için Application Context
) : ViewModel() {

    // --- Calorie Records ---

    // Tüm kayıtları Room'dan Flow olarak al ve Compose StateFlow'a dönüştürerek UI'a sun.
    // Room'daki her değişiklikte bu Flow yeni bir liste yayınlar ve UI otomatik güncellenir.
    val calorieRecords: StateFlow<List<CalorieRecordEntity>> =
        calorieRecordDao.getAllRecords()
            .stateIn(
                scope = viewModelScope, // ViewModel'ın yaşam döngüsüne bağlı scope
                started = SharingStarted.WhileSubscribed(5000), // UI aktifken veriyi paylaş
                initialValue = emptyList() // Başlangıç değeri (UI ilk açıldığında)
            )

    // Yeni kayıt ekleme (UI'dan çağrılır)
    fun addCalorieRecord(record: CalorieRecordEntity) {
        viewModelScope.launch { // Veritabanı işlemleri için Coroutine başlat
            calorieRecordDao.insertRecord(record)
            // Kayıt eklenince Room Flow'u günceller, UI otomatik değişir. Eski save fonksiyonunu çağırmaya gerek YOK.
        }
    }

    // Kayıt silme (UI'dan çağrılır)
    fun deleteCalorieRecord(record: CalorieRecordEntity) {
        viewModelScope.launch { // Veritabanı işlemleri için Coroutine başlat
            calorieRecordDao.deleteRecord(record)
            // UI otomatik güncellenir. Eski save fonksiyonunu çağırmaya gerek YOK.
        }
    }

    // Tüm kayıtları silme (UI'dan çağrılır)
    fun clearAllCalorieRecords() {
        viewModelScope.launch { // Veritabanı işlemleri için Coroutine başlat
            calorieRecordDao.deleteAllRecords()
            // UI otomatik güncellenir.
        }
    }

    // --- Daily Summaries ---

    // Tüm günlük özetleri Room'dan Flow olarak al ve Compose StateFlow'a sun.
    val dailySummaries: StateFlow<List<DailySummaryEntity>> =
        dailySummaryDao.getAllSummaries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Günlük özet kaydetme veya güncelleme (UI'dan çağrılır)
    // saveTodaySummary fonksiyonunun yerine geçer. Room'daki insertOnConflict = REPLACE sayesinde ekleme ve güncelleme tek metodla yapılır.
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveOrUpdateDailySummary(calories: Int, protein: Double, fat: Double, carbs: Double) {
        viewModelScope.launch { // Veritabanı işlemleri için Coroutine başlat
            val date = LocalDate.now().toString() // Bugünün tarihi
            val summary = DailySummaryEntity(date, calories, protein, fat, carbs)
            dailySummaryDao.insertOrUpdateSummary(summary)
            // Özet eklenince/güncellenince Room Flow'u günceller, UI otomatik değişir. Eski saveTodaySummary fonksiyonunu çağırmaya gerek YOK.
        }
    }

    // Belirli bir günlük özeti silme (UI'dan çağrılır)
    fun deleteDailySummary(summary: DailySummaryEntity) {
        viewModelScope.launch { // Veritabanı işlemleri için Coroutine başlat
            dailySummaryDao.deleteSummary(summary)
            // UI otomatik güncellenir. Eski deleteSummary ve saveSummariesToFile fonksiyonlarını çağırmaya gerek YOK.
        }
    }

    // Tüm günlük özetleri silme (UI'dan çağrılır)
    fun clearAllDailySummaries() {
        viewModelScope.launch { // Veritabanı işlemleri için Coroutine başlat
            dailySummaryDao.deleteAllSummaries()
            // UI otomatik güncellenir. Eski clearAllSummaries fonksiyonunu çağırmaya gerek YOK.
        }
    }

    // Bugün için özetin varlığını kontrol etme (UI'da Save/Update butonu için kullanılabilir)
    // Bu suspend fonksiyonu UI'dan bir Coroutine içinde çağırılmalıdır (LaunchedEffect veya rememberCoroutineScope).
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun checkTodaySummaryExists(): Boolean {
        val date = LocalDate.now().toString()
        return dailySummaryDao.getSummaryByDate(date) != null
    }

    // --- Food Items (Asset'ten Okuma) ---
    // Eğer bu veri Room'a taşınmadıysa, burada okunabilir ve UI'a sunulabilir.
    // ViewModel başlatıldığında bir kere okunur.
    val allFoodItems: List<FoodItemKeepNote> by lazy { // lazy delegate ile ilk erişimde okunur
        readFoodItemsFromAssets(applicationContext) // Asset okuma fonksiyonunu çağır
    }
}