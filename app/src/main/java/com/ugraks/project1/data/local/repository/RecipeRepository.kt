package com.ugraks.project1.data.local.repository

import com.ugraks.project1.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

// Repository katmanının sözleşmesini (arayüzünü) tanımlar
// ViewModel, verilere bu arayüz üzerinden erişir, implementasyon detaylarından haberi olmaz.
interface RecipeRepository {

    // Arama metnine göre filtrelenmiş tarif adlarının listesini reaktif olarak sağlar.
    // ViewModel bu Flow'u toplayarak UI'ı günceller.
    fun getFilteredRecipeNames(query: String): Flow<List<String>>

    // Belirli bir tarife ait tüm detayları (RecipeEntity) reaktif olarak sağlar.
    // Detay ekranı bu Flow'u toplayarak ilgili tarifi gösterir.
    fun getRecipeDetail(recipeName: String): Flow<RecipeEntity?>

    // Veritabanının ilk kez asset'ten tariflerle doldurulması veya güncellenmesi
    // gibi veri senkronizasyon işlemlerini başlatmak için bir metot.
    // ViewModel veya uygulamanın başlangıcında çağrılabilir.
    suspend fun ensureDatabasePopulated()
}