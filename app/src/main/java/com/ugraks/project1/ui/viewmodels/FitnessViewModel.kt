package com.ugraks.project1.ui.viewmodels // Kendi ViewModel paket adınız

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle // YENİ: SavedStateHandle importu
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugraks.project1.R
import com.ugraks.project1.data.local.entity.ExerciseEntity
import com.ugraks.project1.data.local.repository.FitnessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted

// SharedPreferences için anahtar (Fitness için ayrı)
private const val FITNESS_PREFS_NAME = "fitness_asset_prefs"
private const val LAST_LOADED_FITNESS_VERSION_KEY = "last_loaded_fitness_version"

@HiltViewModel
class FitnessViewModel @Inject constructor(
    private val repository: FitnessRepository,
    @ApplicationContext private val applicationContext: Context,
    private val savedStateHandle: SavedStateHandle // YENİ: SavedStateHandle inject edildi
) : ViewModel() {

    // Tüm egzersizleri Room'dan Repository aracılığıyla alacak StateFlow
    private val _allExercises = repository.getAllExercises()

    // UI'a sunulacak, Room'daki tüm egzersizleri yansıtan StateFlow
    val allExercises: StateFlow<List<ExerciseEntity>> = _allExercises
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Yüklenen egzersizlerden elde edilen benzersiz kas gruplarının StateFlow'u
    val muscleGroups: StateFlow<List<String>> = allExercises
        .map { exercises ->
            exercises.map { it.muscleGroup }.distinct().sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Kullanıcı tarafından seçilen kas gruplarını tutacak MutableStateFlow
    // BU STATE ARTIK NAVARGS'TAN BAŞLATILACAK VE MAINSCREEN TARAFINDAN SET EDİLMEYECEK
    private val _selectedMuscleGroups = MutableStateFlow<List<String>>(emptyList())
    // val selectedMuscleGroups: StateFlow<List<String>> = _selectedMuscleGroups.asStateFlow() // UI bu State'i doğrudan kullanmayacak (filter için kullanılacak)

    // Filtreleme için kullanılacak seçili kas grupları (NavArgs'tan gelen değerle başlatılır)
    // Bu değer sadece ViewModel başlatıldığında bir kere NavArgs'tan okunur.
    private val muscleGroupsFromNavArgs: List<String> = savedStateHandle.get<String>("muscleGroupsString") // NavArg adıyla String'i al
        ?.split(",") // Koma ile ayır
        ?.filter { it.isNotEmpty() } // Boş stringleri temizle
        ?: emptyList() // String null veya boşsa boş liste

    // Seçili kas gruplarını ayarlamak için metot KALDIRILDI, çünkü NavArgs'tan geliyor
    // fun setSelectedMuscleGroups(groups: List<String>) { ... }

    // Seçili kas gruplarına göre filtrelenmiş egzersizlerin StateFlow'u
    // allExercises veya ViewModel'ın başlatıldığı andaki muscleGroupsFromNavArgs değeri ile birleşir.
    val filteredExercises: StateFlow<List<ExerciseEntity>> = combine(
        allExercises, // Tüm egzersizler listesi
        // Artık ViewModel'daki selectedMuscleGroups StateFlow'u yerine,
        // ViewModel oluşturulduğunda NavArgs'tan okunan değeri kullanacağız.
        // Bunu combine'a doğrudan bir StateFlow olarak vermek için MutableStateFlow kullanabiliriz.
        MutableStateFlow(muscleGroupsFromNavArgs) // YENİ: NavArgs değerini içeren bir StateFlow
    ) { exercises, selectedGroups -> // Lambda her çalıştığında (allExercises veya NavArgs StateFlow'u değişince)
        Log.d("FitnessFilter", "Combine Triggered - exercises count: ${exercises.size}, selectedGroups: ${selectedGroups.joinToString(",")}")

        if (selectedGroups.isEmpty()) {
            Log.d("FitnessFilter", "Selected groups are empty. Showing all exercises.")
            exercises // Seçili grup yoksa tüm egzersizleri göster (NavArgs boş geldiyse)
        } else {
            // Filtreleme mantığı selectedGroups (NavArgs'tan gelen) listesini kullanır
            val filtered = exercises.filter { it.muscleGroup in selectedGroups }
            Log.d("FitnessFilter", "Selected groups are NOT empty. Filtered count: ${filtered.size}")
            filtered // Filtrelenmiş liste
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // ViewModel başlatıldığında asset kontrolü ve seed etme yapılır
    init {
        checkAssetVersionAndSeedExercises()
        // NavArgs'tan gelen muscleGroupsFromNavArgs değeri combine'daki MutableStateFlow'u başlattığı için
        // ayrıca bir state güncellemesi yapmaya gerek yok.
    }

    // ... checkAssetVersionAndSeedExercises metodu (değişiklik yok) ...
    private fun checkAssetVersionAndSeedExercises() {
        viewModelScope.launch {
            try {
                val currentAssetVersion = applicationContext.resources.getInteger(R.integer.fitness_asset_version)
                val prefs = applicationContext.getSharedPreferences(FITNESS_PREFS_NAME, Context.MODE_PRIVATE)
                val lastLoadedVersion = prefs.getInt(LAST_LOADED_FITNESS_VERSION_KEY, 0)
                val exerciseCount = repository.getExerciseCount()

                Log.d("FitnessAsset", "Current Version: $currentAssetVersion, Last Loaded: $lastLoadedVersion, DB Count: $exerciseCount")

                if (currentAssetVersion > lastLoadedVersion || exerciseCount == 0) {
                    Log.d("FitnessAsset", "New asset version detected or DB is empty. Seeding exercises.")
                    repository.seedExercisesFromAssets(applicationContext)

                    with(prefs.edit()) {
                        putInt(LAST_LOADED_FITNESS_VERSION_KEY, currentAssetVersion)
                        apply()
                    }
                } else {
                    Log.d("FitnessAsset", "Asset version unchanged and DB not empty. No seeding needed.")
                }
            } catch (e: Exception) {
                Log.e("FitnessAsset", "Error checking asset version or seeding exercises", e)
            }
        }
    }

    // ... diğer ViewModel metotları (Daily Summaries, Calorie Records) ...
    // CalorieViewModel'daki bu metotlar FitnessViewModel'a taşınmadıysa burada olmaz
    // Eğer FitnessViewModel sadece fitness içinse, CalorieViewModel'daki diğer metotlar CalorieViewModel'da kalır.
    // Eğer tüm uygulama için tek ViewModel yapıyorsanız, bu metotlar zaten burada olabilir.
    // Sizin FitnessViewModel'ınızın sadece Fitness için olduğunu varsayarak devam ediyorum.

}