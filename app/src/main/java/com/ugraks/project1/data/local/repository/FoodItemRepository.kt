package com.ugraks.project1.data.local.repository


import com.ugraks.project1.data.local.entity.FoodItemEntity // FoodItemEntity importu
import kotlinx.coroutines.flow.Flow

// Food Item Repository katmanının sözleşmesini (arayüzünü) tanımlar
// ViewModel, yemek öğeleri verilerine bu arayüz üzerinden erişir.
interface FoodItemRepository {

    // Arama metnine göre filtrelenmiş yemek öğesi adlarının listesini reaktif olarak sağlar.
    // Arama ekranı ViewModel'ı bu Flow'u kullanır.
    fun getFilteredFoodItemNames(query: String): Flow<List<String>>

    // Belirli bir yemek öğesine ait tüm detayları (FoodItemEntity) reaktif olarak sağlar.
    // Kalori hesaplama ekranı ViewModel'ı bu Flow'u kullanır.
    fun getFoodItemByName(itemName: String): Flow<FoodItemEntity?>

    // Veritabanının ilk kez asset'ten yemek öğeleriyle doldurulması veya güncellenmesi
    // gibi veri senkronizasyon işlemlerini başlatmak için bir metot.
    // ViewModel veya uygulamanın başlangıcında çağrılabilir.
    suspend fun ensureDatabasePopulated()
}