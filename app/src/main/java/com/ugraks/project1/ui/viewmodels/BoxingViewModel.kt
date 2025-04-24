package com.ugraks.project1.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle // YENİ: SavedStateHandle importu
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugraks.project1.R // R sınıfını import edin (boxing_asset_version'a erişmek için)
import com.ugraks.project1.data.local.entity.BoxingItemEntity // BoxingItemEntity importu
import com.ugraks.project1.data.local.repository.BoxingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine // İki StateFlow'u birleştirmek için (filtreleme için kullanılacak)
import kotlinx.coroutines.flow.map // StateFlow'u dönüştürmek için (kategorileri çıkarmak için)
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted

// SharedPreferences için anahtar (Boks için ayrı)
private const val BOXING_PREFS_NAME = "boxing_asset_prefs"
private const val LAST_LOADED_BOXING_VERSION_KEY = "last_loaded_boxing_version"

@HiltViewModel
class BoxingViewModel @Inject constructor(
    private val repository: BoxingRepository, // BoxingRepository bağımlılığı
    @ApplicationContext private val applicationContext: Context, // Context inject edildi
    private val savedStateHandle: SavedStateHandle // YENİ: SavedStateHandle inject edildi (NavArgs'tan veri almak için)
) : ViewModel() {

    // Tüm boks öğelerini Room'dan Repository aracılığıyla alacak StateFlow
    private val _allBoxingItems = repository.getAllBoxingItems() // Repository'den Flow alınır, henüz StateFlow değil

    // UI'a sunulacak, Room'daki tüm boks öğelerini yansıtan StateFlow
    val allBoxingItems: StateFlow<List<BoxingItemEntity>> = _allBoxingItems
        .stateIn( // Flow'u StateFlow'a dönüştür
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // UI aktifken veriyi paylaş
            initialValue = emptyList() // Başlangıç değeri
        )

    // Yüklenen boks öğelerinden elde edilen benzersiz kategorilerin StateFlow'u
    // allBoxingItems StateFlow'u değiştiğinde otomatik güncellenir
    val boxingCategories: StateFlow<List<String>> = allBoxingItems
        .map { items -> // allBoxingItems listesi geldiğinde bu map çalışır
            items.map { it.category }.distinct().sorted() // Kategorileri al, benzersiz yap, sırala
        }
        .stateIn( // Sonucu StateFlow'a dönüştür
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Başlangıç değeri
        )

    // YENİ: Filtreleme için kullanılacak seçili kategoriler (NavArgs'tan gelen değerle başlatılır)
    // Bu değer sadece ViewModel başlatıldığında bir kere NavArgs'tan okunur.
    private val categoriesFromNavArgs: List<String> = savedStateHandle.get<String>("selectedCategoriesString") // NavArg adıyla String'i al ("selectedCategoriesString" NavArgs adınız olmalı)
        ?.split(",") // Koma ile ayır
        ?.filter { it.isNotEmpty() } // Boş stringleri temizle
        ?: emptyList() // String null veya boşsa boş liste

    // Seçili kategorileri ayarlamak için metot KALDIRILDI, çünkü NavArgs'tan geliyor
    // fun setSelectedBoxingCategories(categories: List<String>) { ... }

    // Seçili kategorilere göre filtrelenmiş boks öğelerinin StateFlow'u
    // allBoxingItems veya ViewModel'ın başlatıldığı andaki categoriesFromNavArgs değeri ile birleşir.
    val filteredBoxingItems: StateFlow<List<BoxingItemEntity>> = combine(
        allBoxingItems, // Tüm boks öğeleri listesi
        // ViewModel oluşturulduğunda NavArgs'tan okunan değeri içeren bir StateFlow kullanacağız.
        MutableStateFlow(categoriesFromNavArgs) // YENİ: NavArgs değerini içeren bir StateFlow
    ) { items, selectedCategories -> // Lambda her çalıştığında (allBoxingItems veya NavArgs StateFlow'u değişince)
        Log.d("BoxingFilter", "Combine Triggered - items count: ${items.size}, selectedCategories: ${selectedCategories.joinToString(",")}")

        if (selectedCategories.isEmpty()) {
            Log.d("BoxingFilter", "Selected categories are empty. Showing all boxing items.")
            items // Seçili kategori yoksa tüm öğeleri göster (NavArgs boş geldiyse)
        } else {
            // Filtreleme mantığı selectedCategories (NavArgs'tan gelen) listesini kullanır
            val filtered = items.filter { it.category in selectedCategories }
            Log.d("BoxingFilter", "Selected categories are NOT empty. Filtered count: ${filtered.size}")
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
        checkAssetVersionAndSeedBoxingItems()
        // NavArgs'tan gelen categoriesFromNavArgs değeri combine'daki MutableStateFlow'u başlattığı için
        // ayrıca bir state güncellemesi yapmaya gerek yok.
    }

    // Asset versiyonunu kontrol eden ve gerektiğinde Room'u seed eden metot
    private fun checkAssetVersionAndSeedBoxingItems() {
        viewModelScope.launch {
            try {
                // 1. Güncel asset versiyonunu oku (res/values/boxing_asset_version'dan)
                // Eğer R.integer.boxing_asset_version yoksa hata alırsınız, eklediğinizden emin olun!
                val currentAssetVersion = applicationContext.resources.getInteger(R.integer.boxing_asset_version)

                // 2. SharedPreferences'tan daha önce yüklenen versiyonu oku (Boks için ayrı anahtar)
                val prefs = applicationContext.getSharedPreferences(BOXING_PREFS_NAME, Context.MODE_PRIVATE)
                val lastLoadedVersion = prefs.getInt(LAST_LOADED_BOXING_VERSION_KEY, 0)

                // 3. Veritabanında boks öğesi olup olmadığını kontrol et
                val itemCount = repository.getBoxingItemCount()

                Log.d("BoxingAsset", "Current Version: $currentAssetVersion, Last Loaded: $lastLoadedVersion, DB Count: $itemCount")

                // 4. Seed etme koşulunu kontrol et: Yeni versiyon varsa VEYA veritabanı boşsa seed et
                if (currentAssetVersion > lastLoadedVersion || itemCount == 0) {
                    Log.d("BoxingAsset", "New asset version detected or DB is empty. Seeding boxing items.")
                    // Repository'den seed etme işlemini çağır
                    repository.seedBoxingItemsFromAssets(applicationContext)

                    // Yeni versiyonu SharedPreferences'a kaydet
                    with(prefs.edit()) {
                        putInt(LAST_LOADED_BOXING_VERSION_KEY, currentAssetVersion)
                        apply() // Asenkron kaydetme
                    }
                } else {
                    Log.d("BoxingAsset", "Asset version unchanged and DB not empty. No seeding needed.")
                }
            } catch (e: Exception) {
                Log.e("BoxingAsset", "Error checking asset version or seeding boxing items", e)
                // Hata durumunda logla
            }
        }
    }

    // --- Diğer Boks İle İlgili ViewModel Metotları (Gerekirse) ---
    // Örneğin, belirli bir boks öğesi detayını çekmek gibi metotlar buraya eklenebilir.
}