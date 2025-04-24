package com.ugraks.project1.ui.viewmodels.FoodViewModel

import androidx.lifecycle.SavedStateHandle // Navigasyon argümanlarını almak için
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ugraks.project1.data.local.entity.FoodItemEntity // FoodItemEntity importu
import com.ugraks.project1.data.local.repository.FoodItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull // null olmayan değerleri filtrelemek için (isteğe bağlı)
import kotlinx.coroutines.flow.flatMapLatest // Gerekirse
import kotlinx.coroutines.flow.flowOf // Gerekirse
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Hilt tarafından ViewModel olarak sağlanacağını belirtir
@HiltViewModel
class FoodCaloriesViewModel @Inject constructor(
    // YENİ: FoodItemRepository'yi inject et
    private val foodItemRepository: FoodItemRepository,
    // Navigasyon argümanlarını almak için SavedStateHandle'ı inject et
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Navigasyon argümanlarından yemek ismini al
    // NavGraph'ınızda bu ViewModel'a giderken "foodItemName" argümanını pass ettiğinizi varsayarız.
    private val foodItemName: String? = savedStateHandle["foodItemName"]

    // Belirli bir yemek öğesinin detaylarını Room'dan reaktif olarak al
    // Repository'deki metodu çağırarak Flow<FoodItemEntity?>'i alır ve StateFlow'a dönüştürür.
    val selectedFoodItem: StateFlow<FoodItemEntity?> =
    // Eğer foodItemName null ise veya boşsa, null yayınlayan bir Flow döndür.
        // Aksi halde Repository'den ilgili öğeyi getiren Flow'u al.
        if (!foodItemName.isNullOrBlank()) {
            foodItemRepository.getFoodItemByName(foodItemName)
        } else {
            flowOf(null) // Eğer isim yoksa null yayınlayan bir Flow
        }
            .stateIn(
                scope = viewModelScope, // ViewModel'ın yaşam döngüsüne bağlı scope
                started = SharingStarted.WhileSubscribed(5000), // UI aktifken veriyi paylaş
                initialValue = null // Başlangıç değeri null (veri yüklenene kadar veya bulunamazsa)
            )

    // Not: Miktar, birim seçimi ve besin değeri hesaplama mantığı
    // şu an için UI katmanında (Composable içinde) kalabilir.
    // Eğer bu hesaplamalar Room'dan gelen veriye karmaşık bir şekilde bağlanırsa
    // veya bu değerlerin ViewModel içinde yönetilmesi gerekirse, buraya taşınabilir.
    // Şu anki ihtiyaçlar için Composable'daki @Composable fonksiyonlar yeterli görünüyor.

    // Gelecekte bu ViewModel'a "Yemeği Günlüğe Ekle" gibi aksiyonlar eklenebilir.
    // Bu aksiyonlar da ilgili Repository veya DAO'yu (muhtemelen ayrı bir CalorieRecordRepository) kullanacaktır.
}