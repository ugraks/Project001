package com.ugraks.project1.DailyCalorie // Paket adınızı projenize göre ayarlayın

import android.content.Context
import android.content.SharedPreferences // SharedPreferences için import
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue // Bu import hala expanded state'i için gerekli
import androidx.compose.runtime.setValue // Bu import hala expanded state'i için gerekli
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Coroutine scope için
import com.ugraks.project1.data.local.repository.ActivityRepository // ActivityRepository import edin
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

// SharedPreferences için anahtar ve dosya adı tanımları
private const val PREFS_NAME = "app_settings" // Ortak bir ayarlar dosyası adı
private const val DAILY_CALORIE_NEED_KEY = "daily_calorie_need" // Kaydedilecek değerin anahtarı

@HiltViewModel
class DailyCalorieViewModel @Inject constructor(
    private val activityRepository: ActivityRepository, // ActivityRepository inject edildi
    @ApplicationContext private val applicationContext: Context // Application Context inject edildi
) : ViewModel() {

    // SharedPreferences örneği
    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // UI State'ini yönetecek Mutable State değişkenleri (private olarak)
    private val _height = mutableStateOf("")
    private val _weight = mutableStateOf("")
    private val _age = mutableStateOf("")
    private val _isMale = mutableStateOf(false)
    private val _isFemale = mutableStateOf(false)
    private val _isExercising = mutableStateOf(false)
    private val _exerciseDuration = mutableStateOf("")
    private val _dailyCalorieIntake = mutableIntStateOf(0)
    private val _showErrorToast = mutableStateOf(false)

    // Composable'ların gözlemleyebileceği şekilde State olarak dışarıya açılıyor
    val height: State<String> = _height
    val weight: State<String> = _weight
    val age: State<String> = _age
    val isMale: State<Boolean> = _isMale
    val isFemale: State<Boolean> = _isFemale
    val isExercising: State<Boolean> = _isExercising
    val exerciseDuration: State<String> = _exerciseDuration
    val dailyCalorieIntake: State<Int> = _dailyCalorieIntake
    val showErrorToast: State<Boolean> = _showErrorToast


    // Seçilebilir aktiviteler listesi (Room'dan gelecek ve Composable'da State olarak gözlemlenecek)
    val activities: StateFlow<List<String>> = activityRepository
        .getAllActivities()
        .map { activityList ->
            activityList.map { it.name }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Seçilen aktivite adı
    var selectedSport by mutableStateOf("Yoga") // Varsayılan değer atandı


    init {
        loadActivities()

        // Aktiviteler yüklendikten sonra veya liste değiştiğinde varsayılan sporu ayarla
        viewModelScope.launch {
            activities.collect { activityList ->
                if (activityList.isNotEmpty() && (selectedSport.isBlank() || selectedSport !in activityList)) {
                    selectedSport = activityList.first()
                }
            }
        }
    }

    private fun loadActivities() {
        viewModelScope.launch {
            val assetFileName = "sports.txt" // Asset dosya adı
            activityRepository.loadActivitiesFromAssets(applicationContext, assetFileName)
        }
    }

    // Kullanıcı inputlarını güncelleyen fonksiyonlar (önceki kodla aynı, sıfırlama mantığı korunuyor)
    fun onHeightChange(newValue: String) {
        if (newValue.all { c -> c.isDigit() } || newValue.isBlank()) {
            _height.value = newValue
            _dailyCalorieIntake.value = 0
            _showErrorToast.value = false
        }
    }

    fun onWeightChange(newValue: String) {
        if (newValue.all { c -> c.isDigit() } || newValue.isBlank()) {
            _weight.value = newValue
            _dailyCalorieIntake.value = 0
            _showErrorToast.value = false
        }
    }

    fun onAgeChange(newValue: String) {
        if (newValue.all { c -> c.isDigit() } || newValue.isBlank()) {
            _age.value = newValue
            _dailyCalorieIntake.value = 0
            _showErrorToast.value = false
        }
    }

    fun onGenderChange(isMale: Boolean, isFemale: Boolean) {
        _isMale.value = isMale
        _isFemale.value = isFemale
        _dailyCalorieIntake.value = 0
        _showErrorToast.value = false
    }

    fun onExerciseStatusChange(isExercising: Boolean) {
        _isExercising.value = isExercising
        if (!isExercising) {
            _exerciseDuration.value = ""
            // Egzersiz bırakıldığında seçili sporu sıfırla veya ilkine ayarla
            selectedSport = activities.value.firstOrNull() ?: ""
        } else {
            // Egzersize başlandığında ve spor seçili değilse varsayılanı ayarla
            if (selectedSport.isBlank()) {
                selectedSport = activities.value.firstOrNull() ?: ""
            }
        }
        _dailyCalorieIntake.value = 0
        _showErrorToast.value = false
    }

    fun onSportSelect(sportName: String) {
        selectedSport = sportName
        _dailyCalorieIntake.value = 0
        _showErrorToast.value = false
    }

    fun onExerciseDurationChange(newValue: String) {
        if (newValue.all { c -> c.isDigit() } || newValue.isBlank()) {
            _exerciseDuration.value = newValue
            _dailyCalorieIntake.value = 0
            _showErrorToast.value = false
        }
    }

    // Hesaplama fonksiyonu (Buraya SharedPreferences kaydetme ekleniyor)
    fun calculateCalories() {
        if (_height.value.isBlank() || _weight.value.isBlank() || _age.value.isBlank() || (!_isMale.value && !_isFemale.value)) {
            _dailyCalorieIntake.value = 0
            _showErrorToast.value = true
            return
        }

        if (_isExercising.value && (_exerciseDuration.value.isBlank() || selectedSport.isBlank())) {
            _dailyCalorieIntake.value = 0
            _showErrorToast.value = true
            return
        }

        val h = _height.value.toDoubleOrNull()
        val w = _weight.value.toDoubleOrNull()
        val a = _age.value.toDoubleOrNull()
        val durationMinutes = _exerciseDuration.value.toDoubleOrNull()

        if (h == null || w == null || a == null || (_isExercising.value && durationMinutes == null)) {
            _dailyCalorieIntake.value = 0
            _showErrorToast.value = true
            return
        }

        _showErrorToast.value = false

        viewModelScope.launch {
            val bmr = if (_isMale.value) {
                (10 * w) + (6.25 * h) - (5 * a) + 5
            } else {
                (10 * w) + (6.25 * h) - (5 * a) - 161
            }

            val baseActivityFactor = 1.2 // Sedentary (little or no exercise)

            // Aktivite seviyesine göre faktörü ayarlayabilirsiniz, veya MET değeri ile egzersiz kalorisini eklemeye devam edebilirsiniz.
            // Mevcut kod MET değeri ile egzersiz kalorisini ekliyor, bu yaklaşımı koruyalım.

            val dailyCalorieWithoutExercise = bmr * baseActivityFactor // Bazal Metabolizma + Temel Aktivite

            var caloriesBurnedExercise = 0.0

            if (_isExercising.value && durationMinutes != null && durationMinutes > 0 && selectedSport.isNotBlank()) {
                val selectedActivity = activityRepository.getActivityByName(selectedSport)
                // Eğer aktivite bulunamazsa veya metValue null ise varsayılan bir MET değeri kullanın (örn: 1.5 veya 3.0)
                val metValue = selectedActivity?.metValue ?: 3.0 // Varsayılan MET değeri, hafif aktivite için 3.0 uygun olabilir

                val hours = durationMinutes / 60.0
                caloriesBurnedExercise = metValue * w * hours // Kalori yakımı = MET * Vücut Ağırlığı (kg) * Süre (saat)
            }

            val totalCalculatedDailyNeed = (dailyCalorieWithoutExercise + caloriesBurnedExercise).roundToInt()

            _dailyCalorieIntake.value = totalCalculatedDailyNeed

            // *** YENİ KOD: Hesaplanan değeri SharedPreferences'a kaydet ***
            with(sharedPreferences.edit()) {
                putInt(DAILY_CALORIE_NEED_KEY, totalCalculatedDailyNeed)
                apply() // Asenkron olarak kaydet
            }
            // ************************************************************
        }
    }

    fun toastShown() {
        _showErrorToast.value = false
    }

    // ViewModel temizlendiğinde SharedPreferences örneğini kapatmaya gerek yok, sistem halleder.
    // override fun onCleared() {
    //    super.onCleared()
    // }
}