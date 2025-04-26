package com.ugraks.project1.ui.viewmodels // Kendi ViewModel paket adınız

import android.content.Context
import android.content.SharedPreferences // SharedPreferences için import
import android.os.Build
import android.util.Log // Logcat için
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
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

// SharedPreferences için anahtar ve dosya adı tanımları
private const val PREFS_NAME = "app_settings" // Ortak bir ayarlar dosyası adı
private const val DAILY_CALORIE_NEED_KEY = "daily_calorie_need" // Kaydedilecek değerin anahtarı
// *** YENİ ANAHTAR: Yiyecek asset versiyonu için ***
private const val FOOD_ASSET_VERSION_PREF_KEY = "food_asset_version"
// *************************************************


@HiltViewModel
class CalorieViewModel @Inject constructor(
    private val repository: CalorieRepository,
    @ApplicationContext private val applicationContext: Context // Context inject edildi
) : ViewModel() {

    // SharedPreferences örneği
    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Food Items (Asset'ten Okuma ve Versiyon Kontrolü) ---

    // Asset'ten okunan yiyecek listesini tutacak ve güncellenecek MutableStateFlow
    private val _allFoodItems = MutableStateFlow<List<FoodItemKeepNote>>(emptyList())
    // UI'ın izleyeceği sadece okunabilir StateFlow
    val allFoodItems: StateFlow<List<FoodItemKeepNote>> = _allFoodItems.asStateFlow()


    // --- Günlük Kalori İhtiyacı (DailyCalories sayfasından kaydedilen değer) ---

    // Kaydedilen günlük kalori ihtiyacını tutacak MutableStateFlow
    private val _dailyCalculatedCalorieNeed = mutableIntStateOf(0) // Başlangıç değeri 0
    // UI'ın izleyeceği sadece okunabilir State
    val dailyCalculatedCalorieNeed: State<Int> = _dailyCalculatedCalorieNeed


    // ViewModel başlatıldığında asset kontrolü, ilk yükleme ve kaydedilmiş ihtiyacı okuma yapılır
    init {
        checkAssetVersionAndLoadFoodItems()
        loadDailyCalculatedCalorieNeed() // Kaydedilmiş ihtiyacı yükle
    }

    // Asset versiyonunu kontrol eden ve gerektiğinde yiyecekleri yeniden yükleyen metot
    private fun checkAssetVersionAndLoadFoodItems() {
        viewModelScope.launch {
            try {
                // 1. Güncel asset versiyonunu oku (res/values/asset_version'dan)
                // NOTE: R.integer.food_asset_version kaynağının projenizde tanımlı olması gerekir
                val currentAssetVersion = applicationContext.resources.getInteger(R.integer.food_asset_version) // R.integer.food_asset_version olmalı

                // 2. SharedPreferences'tan daha önce yüklenen versiyonu oku
                val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) // Aynı SharedPreferences dosyasını kullan
                // *** Düzeltme: FOOD_ASSET_VERSION_PREF_KEY kullanıldı ***
                val lastLoadedVersion = prefs.getInt(FOOD_ASSET_VERSION_PREF_KEY, 0) // Varsayılan 0
                // ****************************************************

                Log.d("AssetVersion", "Food Asset Current Version: $currentAssetVersion, Last Loaded: $lastLoadedVersion")

                // 3. Versiyonları karşılaştır
                if (currentAssetVersion > lastLoadedVersion) {
                    Log.d("AssetVersion", "New food asset version detected. Reloading food items.")
                    // Yeni versiyon varsa yeniden yükle
                    loadFoodItems() // Yiyecekleri asset'ten yükle
                    // Yeni versiyonu SharedPreferences'a kaydet
                    with(prefs.edit()) {
                        // *** Düzeltme: FOOD_ASSET_VERSION_PREF_KEY kullanıldı ***
                        putInt(FOOD_ASSET_VERSION_PREF_KEY, currentAssetVersion)
                        // ****************************************************
                        apply() // Asenkron kaydetme
                    }
                } else {
                    Log.d("AssetVersion", "Food asset version unchanged or initial load. Loading food items.")
                    loadFoodItems()
                }
            } catch (e: Exception) {
                Log.e("AssetVersion", "Error checking or loading food asset version", e)
                loadFoodItems() // Hata olsa bile yüklemeyi dene
            }
        }
    }

    // Asset'ten yiyecekleri yükleyen ve StateFlow'u güncelleyen suspend metot (Önceki kodla aynı)
    private suspend fun loadFoodItems() {
        try {
            val items = repository.getFoodItemsFromAssets()
            _allFoodItems.value = items
            Log.d("AssetVersion", "Loaded ${items.size} food items from assets into CalorieViewModel.")
        } catch (e: Exception) {
            Log.e("AssetVersion", "Error loading food items from assets in CalorieViewModel", e)
            _allFoodItems.value = emptyList()
        }
    }

    // Kaydedilmiş günlük kalori ihtiyacını SharedPreferences'tan okuma (Önceki kodla aynı)
    private fun loadDailyCalculatedCalorieNeed() {
        val savedNeed = sharedPreferences.getInt(DAILY_CALORIE_NEED_KEY, 0)
        _dailyCalculatedCalorieNeed.intValue = savedNeed
        Log.d("CalorieViewModel", "Loaded daily calorie need: $savedNeed kcal")
    }


    // --- Calorie Records --- (Önceki kodla aynı)

    val calorieRecords: StateFlow<List<CalorieRecordEntity>> =
        repository.getAllCalorieRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addCalorieRecord(record: CalorieRecordEntity) {
        viewModelScope.launch {
            repository.insertCalorieRecord(record)
        }
    }

    fun deleteCalorieRecord(record: CalorieRecordEntity) {
        viewModelScope.launch {
            repository.deleteCalorieRecord(record)
        }
    }

    fun clearAllCalorieRecords() {
        viewModelScope.launch {
            repository.deleteAllCalorieRecords()
        }
    }

    // --- Daily Summaries --- (Önceki kodla aynı)

    val dailySummaries: StateFlow<List<DailySummaryEntity>> =
        repository.getAllDailySummaries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveOrUpdateDailySummary(calories: Int, protein: Double, fat: Double, carbs: Double) {
        viewModelScope.launch {
            val date = LocalDate.now().toString()
            val summary = DailySummaryEntity(date, calories, protein, fat, carbs)
            repository.insertOrUpdateDailySummary(summary)
        }
    }

    fun deleteDailySummary(summary: DailySummaryEntity) {
        viewModelScope.launch {
            repository.deleteDailySummary(summary)
        }
    }

    fun clearAllDailySummaries() {
        viewModelScope.launch {
            repository.deleteAllDailySummaries()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun checkTodaySummaryExists(): Boolean {
        val date = LocalDate.now().toString()
        return repository.getDailySummaryByDate(date) != null
    }
}