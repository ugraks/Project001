package com.ugraks.project1.ui.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Coroutine scope için
import com.ugraks.project1.data.local.dao.DailyStepDao // Yeni DAO'yu import edin
import com.ugraks.project1.data.local.entity.DailyStepEntity // Yeni Entity'yi import edin
import dagger.hilt.android.lifecycle.HiltViewModel // Hilt ViewModel annotation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map // Flow dönüşümleri için
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch // Coroutine başlatmak için
import java.time.LocalDate // Tarih için (API 26+)
import javax.inject.Inject // Bağımlılıkları inject etmek için
import java.util.Date // Timestamp için

// Hilt tarafından ViewModel olarak sağlanacağını belirtir
@HiltViewModel
class PedometerViewModel @Inject constructor(
    private val dailyStepDao: DailyStepDao // Yeni DAO'yu inject edin
) : ViewModel() {

    // --- Daily Steps ---

    // Tüm adım girişlerini Room'dan al, tarihe göre grupla ve StateFlow olarak UI'a sun
    // DailySummaryPage'deki loadDailySummaries mantığının yerini alır (gruplama ViewModel'da yapılır).
    val dailyStepSummariesByDate: StateFlow<Map<String, List<DailyStepEntity>>> =
        dailyStepDao.getAllStepEntries() // Tüm girişleri al
            .map { entries -> // Flow<List<DailyStepEntity>>'i işle
                entries.groupBy { it.date } // Tarihe göre grupla -> Map<String, List<DailyStepEntity>>
                // Opsiyonel: Eğer tarihler Map'te de sıralı gelmiyorsa, entrySet.toList.sortedBy... yapıp tekrar map'e çevirebilirsiniz
            }
            .stateIn(
                scope = viewModelScope, // ViewModel'ın yaşam döngüsüne bağlı scope
                started = SharingStarted.WhileSubscribed(5000), // UI aktifken veriyi paylaş
                initialValue = emptyMap() // Başlangıç değeri boş Map
            )

    // Sadece tüm girişleri liste halinde almak isterseniz bu Flow'u da ekleyebilirsiniz:
    /*
    val allStepEntries: StateFlow<List<DailyStepEntity>> =
        dailyStepDao.getAllStepEntries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    */


    // Yeni adım girişi ekleme (StepCounterPage'deki saveDailyStepCount çağrısının yerini alır)
    @RequiresApi(Build.VERSION_CODES.O)
    fun addStepEntry(steps: Int, target: Int?, goalReached: Boolean?) {
        viewModelScope.launch { // Veritabanı işlemi Coroutine'de
            val dateString = LocalDate.now().toString() // "YYYY-MM-DD" formatında
            val timestamp = System.currentTimeMillis() // Kaydedildiği anın zaman damgası

            val status = when (goalReached) {
                true -> "Successful"
                false -> "Unsuccessful"
                else -> "Unknown"
            }

            val newEntry = DailyStepEntity(
                date = dateString,
                timestamp = timestamp,
                steps = steps,
                target = target,
                status = status
            )
            dailyStepDao.insertStepEntry(newEntry)
            // Room Flow'u güncelleyecek, UI otomatik değişecek.
        }
    }

    // Belirli bir adım girişini silme (DailySummaryPage'deki deleteEntry çağrısının yerini alır)
    fun deleteStepEntry(entry: DailyStepEntity) { // Room Entity'si alır
        viewModelScope.launch { // Veritabanı işlemi Coroutine'de
            dailyStepDao.deleteStepEntry(entry) // DAO'ya Entity'yi ver
            // Room Flow'u güncelleyecek, UI otomatik değişecek.
        }
    }

    // Tüm adım girişlerini silme (DailySummaryPage'deki clearAllSummaries çağrısının yerini alır)
    fun clearAllStepEntries() {
        viewModelScope.launch { // Veritabanı işlemi Coroutine'de
            dailyStepDao.deleteAllStepEntries()
            // Room Flow'u güncelleyecek, UI otomatik değişecek.
        }
    }

    // (Opsiyonel) Belirli bir güne ait girişleri almak isterseniz ayrı bir Flow da ekleyebilirsiniz
    /*
    fun getStepEntriesForDate(date: String): StateFlow<List<DailyStepEntity>> =
        dailyStepDao.getStepEntriesByDate(date)
             .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    */

    // Bu ViewModel'da CalorieRecord veya DailySummary ile ilgili kodlar OLMAYACAK.
    // Eğer asset'ten FoodItem okuma işlemi bu ViewModel'da kalacaksa, o kodu buraya taşıyabilirsiniz.
    // Ancak FoodItem CalorieViewModel ile daha alakalı görünüyorsa, orada kalabilir.
    // val allFoodItems: List<FoodItemKeepNote> ... // Eğer buraya taşınacaksa, @ApplicationContext injection gerekir.

}