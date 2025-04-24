package com.ugraks.project1.ui.viewmodels // Kendi ViewModel paket adınız

import android.content.Context
import android.os.Build
import android.util.Log // Logcat için
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Coroutine scope için
import com.ugraks.project1.data.local.entity.CalorieRecordEntity
import com.ugraks.project1.data.local.entity.DailySummaryEntity
import com.ugraks.project1.KeepNoteComposable.FoodItemKeepNote
import com.ugraks.project1.R // R sınıfını import edin (asset_version'a erişmek için)
import com.ugraks.project1.data.repository.CalorieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow // Değerini değiştirebileceğimiz StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow // MutableStateFlow'u StateFlow'a dönüştürmek için
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// SharedPreferences için anahtar
private const val PREFS_NAME = "asset_prefs"
private const val LAST_LOADED_ASSET_VERSION_KEY = "last_loaded_asset_version"

@HiltViewModel
class CalorieViewModel @Inject constructor(
    private val repository: CalorieRepository,
    @ApplicationContext private val applicationContext: Context // YENİ: Context tekrar inject edildi
) : ViewModel() {

    // --- Food Items (Asset'ten Okuma ve Versiyon Kontrolü) ---

    // Asset'ten okunan yiyecek listesini tutacak ve güncellenecek MutableStateFlow
    private val _allFoodItems = MutableStateFlow<List<FoodItemKeepNote>>(emptyList())
    // UI'ın izleyeceği sadece okunabilir StateFlow
    val allFoodItems: StateFlow<List<FoodItemKeepNote>> = _allFoodItems.asStateFlow() // YENİ: List yerine StateFlow olarak sunulur


    // ViewModel başlatıldığında asset kontrolü ve ilk yükleme yapılır
    init {
        checkAssetVersionAndLoadFoodItems()
    }

    // Asset versiyonunu kontrol eden ve gerektiğinde yiyecekleri yeniden yükleyen metot
    private fun checkAssetVersionAndLoadFoodItems() {
        viewModelScope.launch {
            try {
                // 1. Güncel asset versiyonunu oku (res/values/asset_version'dan)
                val currentAssetVersion = applicationContext.resources.getInteger(R.integer.food_asset_version) // R.integer.food_asset_version

                // 2. SharedPreferences'tan daha önce yüklenen versiyonu oku
                val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val lastLoadedVersion = prefs.getInt(LAST_LOADED_ASSET_VERSION_KEY, 0) // Varsayılan 0

                Log.d("AssetVersion", "Current Version: $currentAssetVersion, Last Loaded: $lastLoadedVersion")

                // 3. Versiyonları karşılaştır
                if (currentAssetVersion > lastLoadedVersion) {
                    Log.d("AssetVersion", "New asset version detected. Reloading food items.")
                    // Yeni versiyon varsa yeniden yükle
                    loadFoodItems() // Yiyecekleri asset'ten yükle
                    // Yeni versiyonu SharedPreferences'a kaydet
                    with(prefs.edit()) {
                        putInt(LAST_LOADED_ASSET_VERSION_KEY, currentAssetVersion)
                        apply() // Asenkron kaydetme
                    }
                } else {
                    Log.d("AssetVersion", "Asset version unchanged. Loading food items.")
                    // Versiyon değişmemişse de yine de yükle (uygulama ilk defa açılıyorsa veya önbellek yoksa)
                    // İsteğe bağlı: Eğer Room'da FoodItemEntity kullanıyorsanız, burası Room'dan okuma yeri olabilir
                    loadFoodItems() // Yiyecekleri asset'ten yükle
                }
            } catch (e: Exception) {
                Log.e("AssetVersion", "Error checking or loading asset version", e)
                // Hata durumunda da yine de yüklemeye çalışabiliriz veya boş liste bırakabiliriz
                loadFoodItems() // Hata olsa bile yüklemeyi dene
            }
        }
    }

    // Asset'ten yiyecekleri yükleyen ve StateFlow'u güncelleyen suspend metot
    private suspend fun loadFoodItems() {
        try {
            // Repository'den asset'ten okunan listeyi al
            val items = repository.getFoodItemsFromAssets()
            // MutableStateFlow'un değerini güncelle
            _allFoodItems.value = items
            Log.d("AssetVersion", "Loaded ${items.size} food items from assets.")
        } catch (e: Exception) {
            Log.e("AssetVersion", "Error loading food items from assets", e)
            _allFoodItems.value = emptyList() // Hata durumunda boş liste
        }
    }


    // --- Calorie Records ---

    // Tüm kayıtları Repository'den Flow olarak al ve Compose StateFlow'a sun.
    // Kaynak Repository metodu.
    val calorieRecords: StateFlow<List<CalorieRecordEntity>> =
        repository.getAllCalorieRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Yeni kayıt ekleme (UI'dan çağrılır)
    fun addCalorieRecord(record: CalorieRecordEntity) {
        viewModelScope.launch {
            repository.insertCalorieRecord(record)
        }
    }

    // Kayıt silme (UI'dan çağrılır)
    fun deleteCalorieRecord(record: CalorieRecordEntity) {
        viewModelScope.launch {
            repository.deleteCalorieRecord(record)
        }
    }

    // Tüm kayıtları silme (UI'dan çağrılır)
    fun clearAllCalorieRecords() {
        viewModelScope.launch {
            repository.deleteAllCalorieRecords()
        }
    }

    // --- Daily Summaries ---

    // Tüm günlük özetleri Repository'den Flow olarak al ve Compose StateFlow'a sun.
    // Kaynak Repository metodu.
    val dailySummaries: StateFlow<List<DailySummaryEntity>> =
        repository.getAllDailySummaries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Günlük özet kaydetme veya güncelleme (UI'dan çağrılır)
    // DailySummaryEntity oluşturup Repository'e gönderiyoruz.
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveOrUpdateDailySummary(calories: Int, protein: Double, fat: Double, carbs: Double) {
        viewModelScope.launch {
            val date = LocalDate.now().toString()
            val summary = DailySummaryEntity(date, calories, protein, fat, carbs)
            repository.insertOrUpdateDailySummary(summary)
        }
    }

    // Belirli bir günlük özeti silme (UI'dan çağrılır)
    fun deleteDailySummary(summary: DailySummaryEntity) {
        viewModelScope.launch {
            repository.deleteDailySummary(summary)
        }
    }

    // Tüm günlük özetleri silme (UI'dan çağrılır)
    fun clearAllDailySummaries() {
        viewModelScope.launch {
            repository.deleteAllDailySummaries()
        }
    }

    // Bugün için özetin varlığını kontrol etme (UI'da Save/Update butonu için kullanılabilir)
    // Bu suspend fonksiyonu UI'dan bir Coroutine içinde çağırılmalıdır (LaunchedEffect veya rememberCoroutineScope).
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun checkTodaySummaryExists(): Boolean {
        val date = LocalDate.now().toString()
        return repository.getDailySummaryByDate(date) != null
    }
}