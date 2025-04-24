package com.ugraks.project1.data.local.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ugraks.project1.R // R sınıfı için import (Asset versiyonu için)
import com.ugraks.project1.Foods.readAndParseItemsFromAssets // Asset okuma fonksiyonu importu
import com.ugraks.project1.data.local.dao.FoodItemDao // FoodItemDao importu
import com.ugraks.project1.data.local.entity.FoodItemEntity // FoodItemEntity importu
// Eğer FoodItem data class'ına ihtiyacınız olursa import edin, ancak dönüşümü burada yapacağız.
// import com.ugraks.project1.Foods.FoodItem
import dagger.hilt.android.qualifiers.ApplicationContext // Context için import
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject // Inject için import

// FoodItemRepository arayüzünün somut implementasyonu
// Bu sınıf, ViewModel ile veri kaynakları (DAO, Asset okuma vb.) arasındaki köprüyü kurar.
class FoodItemRepositoryImpl @Inject constructor(
    private val foodItemDao: FoodItemDao, // Room DAO bağımlılığı
    @ApplicationContext private val applicationContext: Context // Asset okuma ve Shared Prefs için Context bağımlılığı
) : FoodItemRepository { // FoodItemRepository arayüzünü implement eder

    // Shared Preferences sabitleri ve örneği (İlk dolum versiyonu için)
    // YENİ: Yemek öğeleri için farklı bir Shared Preferences anahtarı kullanalım
    private val PREFS_NAME = "food_item_prefs" // Shared Preferences dosya adı
    private val KEY_LAST_LOADED_ASSET_VERSION = "last_loaded_food_item_asset_version" // Versiyonu saklayacağımız anahtar
    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Repository Metotlarının Implementasyonu ---

    // Arama metnine göre filtreleme yapan metot
    // Doğrudan FoodItemDao'daki ilgili metotları çağırır.
    override fun getFilteredFoodItemNames(query: String): Flow<List<String>> {
        return if (query.isBlank()) {
            foodItemDao.getAllFoodItemNames() // Arama boşsa tüm adları getir
        } else {
            foodItemDao.searchFoodItemNames(query) // Arama varsa filtrelenmiş adları getir
        }
    }

    // Belirli bir yemek öğesini adına göre getiren metot
    // Doğrudan FoodItemDao'daki ilgili metodu çağırır.
    override fun getFoodItemByName(itemName: String): Flow<FoodItemEntity?> {
        return foodItemDao.getFoodItemByName(itemName)
    }

    // --- İlk Dolum (Pre-population) ve Güncelleme Mantığı ---

    // Veritabanının asset'ten yemek öğeleriyle doldurulmasını veya güncellenmesini sağlar.
    // Bu metod, ViewModel'dan çağrılacak.
    override suspend fun ensureDatabasePopulated() {
        // YENİ: Kaynaklardaki yemek öğeleri asset versiyonunu al
        val currentAssetVersion = try {
            // asset_versions.xml dosyanızda <integer name="food_asset_version">1</integer> gibi bir tanım olmalı
            applicationContext.resources.getInteger(R.integer.food_asset_version)
        } catch (e: Exception) {
            Log.e("FoodItemRepository", "R.integer.food_asset_version bulunamadı! Asset yükleme sürüm kontrolü yapılamayacak.", e)
            0 // Kaynak bulunamazsa versiyonu 0 kabul et
        }

        // Shared Preferences'tan en son başarılı şekilde yüklenen asset versiyonunu al
        val lastLoadedAssetVersion = sharedPreferences.getInt(KEY_LAST_LOADED_ASSET_VERSION, 0)

        Log.d("FoodItemRepository", "Current Food Asset Version: $currentAssetVersion, Last Loaded Version: $lastLoadedAssetVersion")

        // Güncelleme yapılıp yapılmayacağını belirleyen koşul:
        // 1. Room'daki yemek öğeleri tablosu boşsa VEYA
        // 2. Kaynaklardaki asset versiyonu, Shared Preferences'taki son yüklenen versiyondan daha yüksekse
        val itemCount = foodItemDao.getFoodItemCount()
        val shouldUpdateFromAsset = itemCount == 0 || currentAssetVersion > lastLoadedAssetVersion


        if (shouldUpdateFromAsset) {
            Log.d("FoodItemRepository", "Yemek öğeleri güncelleniyor veya ilk kez yükleniyor...")

            try {
                // Mevcut yemek öğelerini sil (güncel veri yükleneceği için)
                foodItemDao.deleteAllFoodItems() // Eğer DAO'ya eklemediyseniz bu satırı kaldırın veya DAO'yu güncelleyin.
                Log.d("FoodItemRepository", "Mevcut yemek öğeleri Room'dan silindi.")

                // Asset'ten okuma fonksiyonunu çağır
                val foodItemsFromAssets = readAndParseItemsFromAssets(applicationContext)

                if (foodItemsFromAssets.isNotEmpty()) {
                    // Okunan FoodItem data class'larından FoodItemEntity listesine dönüştür
                    val foodItemEntities = foodItemsFromAssets.map { item ->
                        FoodItemEntity(
                            name = item.name,
                            calories = item.calories,
                            type = item.type,
                            proteinPerKgL = item.proteinPerKgL,
                            fatPerKgL = item.fatPerKgL,
                            carbPerKgL = item.carbPerKgL
                        )
                    }
                    foodItemDao.insertFoodItems(foodItemEntities) // Room'a topluca kaydet
                    Log.d("FoodItemRepository", "Asset'ten ${foodItemEntities.size} yemek öğesi Room'a kaydedildi.")

                    // Başarılı olursa, Shared Preferences'ta bu asset versiyonunu kaydet
                    if (currentAssetVersion > 0) {
                        sharedPreferences.edit().putInt(KEY_LAST_LOADED_ASSET_VERSION, currentAssetVersion).apply()
                        Log.d("FoodItemRepository", "Son başarılı yüklenen yemek öğesi asset versiyonu kaydedildi: $currentAssetVersion")
                    } else {
                        Log.w("FoodItemRepository", "Geçerli yemek öğesi asset versiyonu 0 olduğu için Shared Preferences'a kaydedilemedi.")
                    }


                } else {
                    Log.w("FoodItemRepository", "Asset dosyasından hiç yemek öğesi okunamadı veya parse edilemedi! Room tablosu boş kalmış olabilir.")
                    // Hata durumunda SharedPrefs versiyonunu güncellemiyoruz ki bir sonraki başlatmada tekrar denensin.
                }

            } catch (e: Exception) {
                Log.e("FoodItemRepository", "Yemek öğeleri güncellenirken/yüklenirken beklenmeyen hata oluştu", e)
                // Hata yönetimi burada yapılabilir
            }

        } else {
            Log.d("FoodItemRepository", "Yemek öğeleri Room'da mevcut ve güncel görünüyor.")
        }
    }
}