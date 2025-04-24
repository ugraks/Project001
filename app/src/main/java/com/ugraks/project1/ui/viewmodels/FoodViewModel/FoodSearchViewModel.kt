package com.ugraks.project1.ui.viewmodels.FoodViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Coroutine scope için gerekli
import com.ugraks.project1.data.local.repository.FoodItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel // Hilt ViewModel annotation
import kotlinx.coroutines.ExperimentalCoroutinesApi // flatMapLatest için gerekli
import kotlinx.coroutines.flow.* // Flow, StateFlow, MutableStateFlow, Flow operasyonları için gerekli
import kotlinx.coroutines.launch // Coroutine başlatmak için gerekli
import javax.inject.Inject // Bağımlılıkları inject etmek için gerekli

// Hilt tarafından ViewModel olarak sağlanacağını belirtir
@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    // YENİ: FoodItemRepository'yi inject et
    private val foodItemRepository: FoodItemRepository
) : ViewModel() {

    // --- İlk Dolum (Pre-population) Mantığı ---
    // ViewModel'ın init bloğu artık sadece Repository'deki ilgili metodu çağırıyor.
    // Asıl mantık (asset okuma, versiyon kontrolü, Room'a kaydetme) Repository'ye taşındı.
    init {
        viewModelScope.launch { // Coroutine başlat
            // Repository'deki veritabanı dolum/güncelleme metodunu çağır.
            // Repository bu işlemin gerekli olup olmadığına kendi karar verir.
            Log.d("FoodSearchViewModel", "init: ensureDatabasePopulated() çağrılıyor...")
            foodItemRepository.ensureDatabasePopulated()
            Log.d("FoodSearchViewModel", "init: ensureDatabasePopulated() tamamlandı.")
        }
    }

    // --- Yemek Öğesi Arama State'leri ve Flow'ları ---

    // Arama metnini tutacak MutableStateFlow
    private val _searchText = MutableStateFlow("")
    // UI'ın izleyeceği sadece okunabilir StateFlow
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // Arama metni değiştikçe Repository'den filtrelenmiş yemek öğesi adları listesini reaktif olarak al
    @OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest için annotation
    val filteredFoodItemNames: StateFlow<List<String>> =
        _searchText
            .debounce(300L) // Kullanıcı yazmayı bıraktıktan 300ms sonra tetikle
            .distinctUntilChanged() // Sadece arama metni gerçekten değişirse tetikle
            .flatMapLatest { query -> // Her yeni sorgu geldiğinde önceki sorguyu iptal et ve yenisini başlat
                // YENİ: foodItemDao yerine foodItemRepository kullan
                foodItemRepository.getFilteredFoodItemNames(query) // Repository'deki metodu çağır
            }
            // Yukarıdaki Flow'u Composable'ın izleyebileceği bir StateFlow'a dönüştür
            .stateIn(
                scope = viewModelScope, // ViewModel'ın yaşam döngüsüne bağlı scope
                started = SharingStarted.WhileSubscribed(5000), // UI aktifken veriyi paylaş
                initialValue = emptyList() // Başlangıç değeri olarak boş liste yayınlar
            )

    // Arama metnini güncelleme metodu (Composable'dan arama kutusuna metin girildiğinde çağrılır)
    fun updateSearchText(query: String) {
        _searchText.value = query // MutableStateFlow'un değerini günceller
    }
}