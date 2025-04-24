package com.ugraks.project1.data.local.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ugraks.project1.R // R sınıfı için import
import com.ugraks.project1.Recipes.readRecipesFromAssets // Asset okuma fonksiyonu importu
import com.ugraks.project1.data.local.dao.RecipeDao // RecipeDao importu
import com.ugraks.project1.data.local.entity.RecipeEntity // RecipeEntity importu
import dagger.hilt.android.qualifiers.ApplicationContext // Context için import
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject // Inject için import


// RecipeRepository arayüzünün somut implementasyonu
// Bu sınıf, ViewModel ile veri kaynakları (DAO, Asset okuma vb.) arasındaki köprüyü kurar.
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao, // Room DAO bağımlılığı
    @ApplicationContext private val applicationContext: Context // Asset okuma ve Shared Prefs için Context bağımlılığı
) : RecipeRepository { // RecipeRepository arayüzünü implement eder

    // Shared Preferences sabitleri ve örneği (ViewModel'dan buraya taşındı)
    private val PREFS_NAME = "recipe_prefs"
    private val KEY_LAST_LOADED_ASSET_VERSION = "last_loaded_recipe_asset_version"
    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Repository Metotlarının Implementasyonu ---

    // Arama metnine göre filtreleme yapan metot
    // Doğrudan RecipeDao'daki ilgili metotları çağırır.
    override fun getFilteredRecipeNames(query: String): Flow<List<String>> {
        return if (query.isBlank()) {
            recipeDao.getAllRecipeNames() // Arama boşsa tüm adları getir
        } else {
            recipeDao.searchRecipesByName(query) // Arama varsa filtrelenmiş adları getir
        }
    }

    // Belirli bir tarifi adına göre getiren metot
    // Doğrudan RecipeDao'daki ilgili metodu çağırır.
    override fun getRecipeDetail(recipeName: String): Flow<RecipeEntity?> {
        return recipeDao.getRecipeByName(recipeName)
    }

    // --- İlk Dolum (Pre-population) ve Güncelleme Mantığı ---

    // Veritabanının asset'ten tariflerle doldurulmasını veya güncellenmesini sağlar.
    // Bu metod artık ViewModel'daki init bloğundaki mantığı içeriyor.
    override suspend fun ensureDatabasePopulated() {
        // Kaynaklardaki (resource) güncel asset versiyonunu al
        val currentAssetVersion = try {
            applicationContext.resources.getInteger(R.integer.recipes_asset_version)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "R.integer.recipes_asset_version bulunamadı! Asset yükleme sürümü kontrolü yapılamayacak.", e)
            // Kaynak bulunamazsa veya hata olursa versiyonu 0 kabul et
            0
        }


        // Shared Preferences'tan en son başarılı şekilde yüklenen asset versiyonunu al
        val lastLoadedAssetVersion = sharedPreferences.getInt(KEY_LAST_LOADED_ASSET_VERSION, 0)

        Log.d("RecipeRepository", "Current Asset Version: $currentAssetVersion, Last Loaded Version: $lastLoadedAssetVersion")

        // Güncelleme yapılıp yapılmayacağını belirleyen koşul:
        // 1. Room'daki tarif tablosu boşsa VEYA
        // 2. Kaynaklardaki asset versiyonu, Shared Preferences'taki son yüklenen versiyondan daha yüksekse
        //    (Eğer recipes_asset_version kaynağı bulunamazsa, currentAssetVersion 0 olur ve
        //     lastLoadedAssetVersion'dan yüksek olamaz (varsayılan 0), bu durumda sadece tablo boşsa yüklenir.)
        val recipeCount = recipeDao.getRecipeCount()
        val shouldUpdateFromAsset = recipeCount == 0 || currentAssetVersion > lastLoadedAssetVersion

        if (shouldUpdateFromAsset) {
            Log.d("RecipeRepository", "Tarifler güncelleniyor veya ilk kez yükleniyor...")

            try {
                // Mevcut tarifleri sil (güncel veri yükleneceği için)
                recipeDao.deleteAllRecipes()
                Log.d("RecipeRepository", "Mevcut tarifler Room'dan silindi.")

                val recipesFromAssets = readRecipesFromAssets(applicationContext) // Asset'ten oku

                if (recipesFromAssets.isNotEmpty()) {
                    // Okunan Recipe data class'larından RecipeEntity listesine dönüştür
                    val recipeEntities = recipesFromAssets.map { recipe ->
                        RecipeEntity(
                            name = recipe.name,
                            ingredients = recipe.ingredients,
                            instructions = recipe.instructions
                        )
                    }
                    recipeDao.insertRecipes(recipeEntities) // Room'a kaydet
                    Log.d("RecipeRepository", "Asset'ten ${recipeEntities.size} tarif Room'a kaydedildi.")

                    // Başarılı olursa, Shared Preferences'ta bu asset versiyonunu kaydet
                    if (currentAssetVersion > 0) { // Eğer geçerli bir asset versiyonu varsa kaydet
                        sharedPreferences.edit().putInt(KEY_LAST_LOADED_ASSET_VERSION, currentAssetVersion).apply()
                        Log.d("RecipeRepository", "Son başarılı yüklenen asset versiyonu kaydedildi: $currentAssetVersion")
                    } else {
                        Log.w("RecipeRepository", "Geçerli asset versiyonu 0 olduğu için Shared Preferences'a kaydedilemedi.")
                    }


                } else {
                    Log.w("RecipeRepository", "Asset dosyasından hiç tarif okunamadı veya parse edilemedi! Room tarif tablosu boş kalmış olabilir.")
                    // Hata durumunda SharedPrefs versiyonunu güncellemiyoruz ki bir sonraki başlatmada tekrar denensin.
                }

            } catch (e: Exception) {
                Log.e("RecipeRepository", "Tarif güncellenirken/yüklenirken beklenmeyen hata oluştu", e)
                // Hata yönetimi burada yapılabilir (örn. kullanıcıya bildirim gösterme)
            }

        } else {
            Log.d("RecipeRepository", "Tarifler Room'da mevcut ve güncel görünüyor.")
        }
    }
}