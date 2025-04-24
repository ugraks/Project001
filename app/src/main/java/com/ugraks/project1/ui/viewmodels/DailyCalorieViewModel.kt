package com.ugraks.project1.presentation.dailycalorie // Paket adınızı projenize göre ayarlayın

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue // Bu import ViewModel içinde artık sadece selectedSport için gerekli olabilir
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugraks.project1.data.local.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class DailyCalorieViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

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

    // Seçilen aktivite adı (Composable'da State olarak gözlemlenecek)
    // Bu yine ViewModel içinde "by" ile tanımlanabilir çünkü değeri ViewModel içinde güncelleniyor
    var selectedSport by mutableStateOf("Yoga")


    init {
        loadActivities()

        // Aktiviteler yüklendikten sonra veya liste değiştiğinde varsayılan sporu ayarla
        viewModelScope.launch {
            activities.collect { activityList ->
                if (activityList.isNotEmpty() && selectedSport.isBlank()) {
                    selectedSport = activityList.first()
                } else if (activityList.isNotEmpty() && selectedSport !in activityList) {
                    selectedSport = activityList.first()
                }
            }
        }
    }

    private fun loadActivities() {
        viewModelScope.launch {
            val assetFileName = "sports.txt"
            activityRepository.loadActivitiesFromAssets(applicationContext, assetFileName)
        }
    }

    // Kullanıcı inputlarını güncelleyen fonksiyonlar
    fun onHeightChange(newValue: String) {
        if (newValue.all { c -> c.isDigit() } || newValue.isBlank()) {
            _height.value = newValue // private mutable state'in değerini güncelle
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
            selectedSport = activities.value.firstOrNull() ?: ""
        } else {
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

            val baseActivityFactor = 1.2

            val dailyCalorieWithoutExercise = bmr * baseActivityFactor

            var caloriesBurnedExercise = 0.0

            if (_isExercising.value && durationMinutes != null && durationMinutes > 0 && selectedSport.isNotBlank()) {
                val selectedActivity = activityRepository.getActivityByName(selectedSport)
                val metValue = selectedActivity?.metValue ?: 1.5

                val hours = durationMinutes / 60.0
                caloriesBurnedExercise = metValue * w * hours
            }

            _dailyCalorieIntake.value = (dailyCalorieWithoutExercise + caloriesBurnedExercise).roundToInt()
        }
    }

    fun toastShown() {
        _showErrorToast.value = false
    }

    override fun onCleared() {
        super.onCleared()
    }
}