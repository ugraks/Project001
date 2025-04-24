package com.ugraks.project1.ui.viewmodel // Kendi paket adınız

import android.util.Log // Debug logları için
import androidx.lifecycle.ViewModel // ViewModel temel sınıfı
import androidx.lifecycle.viewModelScope // Coroutine scope için gerekli
import com.ugraks.project1.data.local.entity.RecipeEntity
import com.ugraks.project1.data.local.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel // Hilt ViewModel annotation
import kotlinx.coroutines.ExperimentalCoroutinesApi // flatMapLatest için gerekli
import kotlinx.coroutines.flow.* // Flow, StateFlow, MutableStateFlow, Flow operasyonları için gerekli
import kotlinx.coroutines.launch // Coroutine başlatmak için gerekli
import javax.inject.Inject // Bağımlılıkları inject etmek için gerekli


// Hilt tarafından ViewModel olarak sağlanacağını belirtir
@HiltViewModel
class RecipeViewModel @Inject constructor(
    // YENİ: RecipeDao ve Context yerine RecipeRepository'yi inject et
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    // --- İlk Dolum (Pre-population) Mantığı ---
    // ViewModel'ın init bloğu artık sadece Repository'deki ilgili metodu çağırıyor.
    // Asıl mantık (asset okuma, versiyon kontrolü, Room'a kaydetme) Repository'ye taşındı.
    init {
        viewModelScope.launch { // Coroutine başlat
            // Repository'deki veritabanı dolum/güncelleme metodunu çağır.
            // Repository bu işlemin gerekli olup olmadığına kendi karar verir.
            Log.d("RecipeViewModel", "init: ensureDatabasePopulated() çağrılıyor...")
            recipeRepository.ensureDatabasePopulated()
            Log.d("RecipeViewModel", "init: ensureDatabasePopulated() tamamlandı.")
        }
    }


    // --- Tarif Listesi ve Arama State'leri ve Flow'ları ---

    // Arama metnini tutacak MutableStateFlow (Aynı kalır)
    private val _searchText = MutableStateFlow("")
    // UI'ın izleyeceği sadece okunabilir StateFlow (Aynı kalır)
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // Arama metni değiştikçe Room'dan filtrelenmiş minimal tarif listesini reaktif olarak al
    @OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest için annotation (Aynı kalır)
    val filteredRecipes: StateFlow<List<String>> = // Flow<List<String>> yayınlar
        _searchText
            .debounce(300L) // (Aynı kalır)
            .distinctUntilChanged() // (Aynı kalır)
            .flatMapLatest { query -> // (Aynı kalır)
                // YENİ: recipeDao yerine recipeRepository kullan
                recipeRepository.getFilteredRecipeNames(query) // Repository'deki metodu çağır
            }
            // Yukarıdaki Flow'u Composable'ın izleyebileceği bir StateFlow'a dönüştür (Aynı kalır)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Arama metnini güncelleme metodu (Aynı kalır)
    fun updateSearchText(query: String) {
        _searchText.value = query
    }


    // --- Tarif Detayı Flow'u ---

    // Belirli bir tarifi adına göre Room'dan reaktif olarak alma
    // Bu metot, Detay ekranında belirli bir tarifin adıyla çağrılır.
    fun getRecipeDetail(recipeName: String): StateFlow<RecipeEntity?> {
        // YENİ: recipeDao yerine recipeRepository kullan
        return recipeRepository.getRecipeDetail(recipeName) // Repository'deki metodu çağır
            .stateIn( // (Aynı kalır)
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    // Gelecekte bu ViewModel'a tarif silme, düzenleme vb. metodlar da eklenebilir eğer sabit veri
    // fikri değişirse. Ancak şu anki plan sadece ön-dolum ve okuma üzerine.
}