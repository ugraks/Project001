package com.ugraks.project1.ui.viewmodels // Kendi paket adınız

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Coroutine scope için
import com.ugraks.project1.data.local.entity.DailyStepEntity // DailyStepEntity import edin (Repository metodları Entity döneceği için hala gerekli)
import com.ugraks.project1.data.local.repository.PedometerRepository
import dagger.hilt.android.lifecycle.HiltViewModel // Hilt ViewModel annotation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map // Flow dönüşümleri için (belki artık gerekmez, silinebilir)
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch // Coroutine başlatmak için
import java.time.LocalDate // Tarih için (API 26+) (addStepEntry metodu kullanıyor)
import javax.inject.Inject // Bağımlılıkları inject etmek için


// Hilt tarafından ViewModel olarak sağlanacağını belirtir
@HiltViewModel
class PedometerViewModel @Inject constructor(
    // YENİ: DailyStepDao yerine PedometerRepository'yi inject et
    private val pedometerRepository: PedometerRepository
) : ViewModel() {

    // --- Daily Steps ---

    // Tüm adım girişlerini Room'dan al, tarihe göre grupla ve StateFlow olarak UI'a sun
    // Gruplama mantığı artık Repository'de olduğu için ViewModel'da map işlemi kaldırıldı.
    val dailyStepSummariesByDate: StateFlow<Map<String, List<DailyStepEntity>>> =
        // YENİ: pedometerDao.getAllStepEntries().map { ... } yerine
        pedometerRepository.getDailyStepSummariesByDate() // Repository'den doğrudan gruplanmış Flow'u al
            .stateIn( // (Aynı kalır)
                scope = viewModelScope, // ViewModel'ın yaşam döngüsüne bağlı scope
                started = SharingStarted.WhileSubscribed(5000), // UI aktifken veriyi paylaş
                initialValue = emptyMap() // Başlangıç değeri boş Map
            )

    // Eğer tüm girişleri liste halinde almak isterseniz (örneğin başka bir ekran için),
    // Repository'ye getAllStepEntries() gibi bir metot ekleyip buradan çağırabilirsiniz.
    /*
    // (Opsiyonel) Tüm girişleri liste halinde alma Flow'u
    val allStepEntries: StateFlow<List<DailyStepEntity>> =
        pedometerRepository.getAllStepEntries() // Repository'den getirecek
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    */

    // Yeni adım girişi ekleme
    @RequiresApi(Build.VERSION_CODES.O)
    fun addStepEntry(steps: Int, target: Int?, goalReached: Boolean?) {
        viewModelScope.launch { // Coroutine başlat (Aynı kalır)
            val dateString = LocalDate.now().toString() // "YYYY-MM-DD" formatında (Aynı kalır)
            val timestamp = System.currentTimeMillis() // Kaydedildiği anın zaman damgası (Aynı kalır)

            val status = when (goalReached) { // (Aynı kalır)
                true -> "Successful"
                false -> "Unsuccessful"
                else -> "Unknown"
            }

            val newEntry = DailyStepEntity( // DailyStepEntity oluşturma (Aynı kalır)
                date = dateString,
                timestamp = timestamp,
                steps = steps,
                target = target,
                status = status
            )
            // YENİ: dailyStepDao.insertStepEntry(newEntry) yerine
            pedometerRepository.addStepEntry(newEntry) // Repository'deki metodu çağır
            // Room Flow'u güncelleyecek, UI otomatik değişecek.
        }
    }

    // Belirli bir adım girişini silme
    fun deleteStepEntry(entry: DailyStepEntity) { // Room Entity'si alır (Aynı kalır)
        viewModelScope.launch { // Coroutine başlat (Aynı kalır)
            // YENİ: dailyStepDao.deleteStepEntry(entry) yerine
            pedometerRepository.deleteStepEntry(entry) // Repository'deki metodu çağır
            // Room Flow'u güncelleyecek, UI otomatik değişecek.
        }
    }

    // Tüm adım girişlerini silme
    fun clearAllStepEntries() {
        viewModelScope.launch { // Coroutine başlat (Aynı kalır)
            // YENİ: dailyStepDao.deleteAllStepEntries() yerine
            pedometerRepository.clearAllStepEntries() // Repository'deki metodu çağır
            // Room Flow'u güncelleyecek, UI otomatik değişecek.
        }
    }

    // (Opsiyonel) Belirli bir güne ait girişleri almak isterseniz
    /*
    // YENİ: dailyStepDao yerine pedometerRepository kullanacak
    fun getStepEntriesForDate(date: String): StateFlow<List<DailyStepEntity>> =
        pedometerRepository.getStepEntriesForDate(date) // Repository'den getirecek
             .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    */

    // CalorieRecord veya DailySummary ile ilgili kodlar burada OLMAYACAK (Aynı kalır)

    // Asset'ten FoodItem okuma işlemi bu ViewModel'da kalmayacaksa (CalorieViewModel'a taşınacaksa)
    // ilgili kodlar ve @ApplicationContext injection'ı burada gerekmez. (Aynı kalır)

}