package com.ugraks.project1.ui.viewmodel // Kendi paket adınız

import android.content.Context // Asset okumak için gerekli
import android.util.Log // Debug logları için
import androidx.lifecycle.ViewModel // ViewModel temel sınıfı
import androidx.lifecycle.viewModelScope // Coroutine scope için gerekli
import com.ugraks.project1.data.local.dao.RecipeDao // RecipeDao import edin
import com.ugraks.project1.data.local.dao.RecipeMinimal // RecipeMinimal import edin (eğer DAO'da kullanıyorsanız)
import com.ugraks.project1.data.local.entity.RecipeEntity // RecipeEntity import edin
import com.ugraks.project1.Recipes.readRecipesFromAssets // Asset okuma fonksiyonunu import edin (package değişmiş olabilir)
import dagger.hilt.android.lifecycle.HiltViewModel // Hilt ViewModel annotation
import dagger.hilt.android.qualifiers.ApplicationContext // Context inject etmek için
import kotlinx.coroutines.ExperimentalCoroutinesApi // flatMapLatest için gerekli
import kotlinx.coroutines.flow.* // Flow, StateFlow, MutableStateFlow, Flow operasyonları için gerekli
import kotlinx.coroutines.launch // Coroutine başlatmak için gerekli
import javax.inject.Inject // Bağımlılıkları inject etmek için gerekli
import android.content.SharedPreferences // Shared Preferences için
import com.ugraks.project1.R // R sınıfı (resources'a erişim) için


// Hilt tarafından ViewModel olarak sağlanacağını belirtir
@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeDao: RecipeDao, // Recipe DAO'yu inject et
    @ApplicationContext private val applicationContext: Context // Asset okumak için Context'i inject et
) : ViewModel() {

    // YENİ: Shared Preferences için sabitler
    private val PREFS_NAME = "recipe_prefs" // Shared Preferences dosya adı
    private val KEY_LAST_LOADED_ASSET_VERSION = "last_loaded_recipe_asset_version" // Versiyonu saklayacağımız anahtar

    // YENİ: Shared Preferences örneği
    // lazy ile ilk erişildiğinde oluşturulmasını sağlıyoruz
    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- İlk Dolum (Pre-population) Mantığı ---
    // Uygulama/Veritabanı ilk açıldığında (veya tablo boşsa) asset'ten tarifleri oku ve Room'a kaydet.
    // fallbackToDestructiveMigration kullanırken bu her uygulama silinip yüklendiğinde veya verileri temizlendiğinde çalışır.
    init {
        viewModelScope.launch { // Coroutine başlat (ViewModel'ın yaşam döngüsüne bağlı)

            // Kaynaklardaki (resource) güncel asset versiyonunu al
            val currentAssetVersion = applicationContext.resources.getInteger(R.integer.recipes_asset_version)
            // Shared Preferences'tan en son başarılı şekilde yüklenen asset versiyonunu al
            // Eğer daha önce hiç kaydedilmemişse varsayılan değer 0 olacaktır.
            val lastLoadedAssetVersion = sharedPreferences.getInt(KEY_LAST_LOADED_ASSET_VERSION, 0)

            Log.d("RecipeViewModel", "Current Asset Version: $currentAssetVersion, Last Loaded Version: $lastLoadedAssetVersion")

            // Güncelleme yapılıp yapılmayacağını belirleyen koşul:
            // 1. Room'daki tarif tablosu boşsa (ilk kurulum gibi) VEYA
            // 2. Kaynaklardaki asset versiyonu, Shared Preferences'taki son yüklenen versiyondan daha yüksekse
            val recipeCount = recipeDao.getRecipeCount() // Room'daki tarif sayısını al (güncel kontrol)
            val shouldUpdateFromAsset = recipeCount == 0 || currentAssetVersion > lastLoadedAssetVersion

            if (shouldUpdateFromAsset) {
                Log.d("RecipeViewModel", "Tarifler güncelleniyor veya ilk kez yükleniyor...")

                try {
                    // --- Güncelleme İşlemi ---

                    // Mevcut tarifleri sil (asset'ten yeni veri yükleneceği için eskisini siliyoruz)
                    // Eğer tablo zaten boşsa (recipeCount == 0 ise) bu adım atlanabilir veya koşul eklenebilir.
                    // recipeDao.deleteAllRecipes() her halükarda güvenlidir.
                    recipeDao.deleteAllRecipes()
                    Log.d("RecipeViewModel", "Mevcut tarifler Room'dan silindi.")


                    val recipesFromAssets = readRecipesFromAssets(applicationContext) // Asset'ten güncel tarifleri oku

                    if (recipesFromAssets.isNotEmpty()) {
                        // Okunan Recipe data class'larından RecipeEntity listesine dönüştür
                        val recipeEntities = recipesFromAssets.map { recipe ->
                            RecipeEntity(
                                name = recipe.name,
                                ingredients = recipe.ingredients, // List<String>
                                instructions = recipe.instructions
                            )
                        }
                        recipeDao.insertRecipes(recipeEntities) // Room'a topluca kaydet
                        Log.d("RecipeViewModel", "Asset'ten ${recipeEntities.size} tarif Room'a kaydedildi.")

                        // Başarılı olursa, Shared Preferences'ta bu asset versiyonunu kaydet
                        // Bir sonraki başlatmada bu versiyonun yüklü olduğu bilinecek.
                        sharedPreferences.edit().putInt(KEY_LAST_LOADED_ASSET_VERSION, currentAssetVersion).apply()
                        Log.d("RecipeViewModel", "Son başarılı yüklenen asset versiyonu kaydedildi: $currentAssetVersion")

                    } else {
                        // Asset boşsa veya parse edilemezse ne yapılmalı?
                        // Hata logu yeterli olabilir veya kullanıcıya bir mesaj gösterilebilir.
                        Log.w("RecipeViewModel", "Asset dosyasından hiç tarif okunamadı veya parse edilemedi! Room tarif tablosu boş olabilir.")
                        // Bu durumda SharedPrefs versiyonunu güncellemiyoruz ki bir sonraki başlatmada tekrar denensin.
                    }

                } catch (e: Exception) {
                    // Dosya okuma, parse etme veya Room'a kaydetme sırasında bir hata oluşursa
                    Log.e("RecipeViewModel", "Tarif güncellenirken/yüklenirken beklenmeyen hata oluştu", e)
                    // Hata durumunda ne yapılacağı burada yönetilebilir (örn. kullanıcıya hata mesajı gösterme)
                }

            } else {
                // Güncelleme gerekmiyorsa (tablo dolu ve asset versiyonu aynı veya düşükse)
                Log.d("RecipeViewModel", "Tarifler Room'da mevcut ve güncel görünüyor.")
            }
        } // Coroutine sonu
    } // Init bloğu sonu


    // --- Tarif Listesi ve Arama State'leri ve Flow'ları ---

    // Arama metnini tutacak MutableStateFlow (ViewModel içinde değiştirilebilir)
    private val _searchText = MutableStateFlow("")
    // UI'ın izleyeceği sadece okunabilir StateFlow (ViewModel dışından değiştirilemez)
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // Arama metni değiştikçe Room'dan filtrelenmiş minimal tarif listesini reaktif olarak al
    @OptIn(ExperimentalCoroutinesApi::class) // flatMapLatest için annotation
    val filteredRecipes: StateFlow<List<String>> = // Flow<List<String>> yayınlar (Sadece isim listesi)
        _searchText
            .debounce(300L) // Kullanıcı yazmayı bıraktıktan 300ms sonra bir sonraki adımı tetikle (opsiyonel ama önerilir)
            .distinctUntilChanged() // Sadece arama metni gerçekten değişirse sonraki adımı tetikle (yinelenen aynı sorguları önler)
            .flatMapLatest { query -> // Her yeni sorgu geldiğinde önceki sorguyu iptal et ve yenisini başlat (en son sorgunun sonucunu gösterir)
                // query (arama metni) değiştikçe bu blok tekrar çalışır.
                if (query.isBlank()) {
                    // Arama boşsa tüm tarif adlarını getir (Ada göre sıralı)
                    recipeDao.getAllRecipeNames() // DAO'dan tüm adları getiren Flow'u alır
                } else {
                    // Arama metni varsa filtrelenmiş adları getir (LIKE sorgusu ile)
                    recipeDao.searchRecipesByName(query) // DAO'dan filtrelenmiş adları getiren Flow'u alır
                }
            }
            // Yukarıdaki Flow'u Composable'ın izleyebileceği bir StateFlow'a dönüştür
            .stateIn(
                scope = viewModelScope, // ViewModel'ın yaşam döngüsüne bağlı scope
                started = SharingStarted.WhileSubscribed(5000), // UI aktifken veriyi paylaş (UI görünür durumdayken en az 5 saniye aktif kalır)
                initialValue = emptyList() // Başlangıç değeri olarak boş liste yayınlar
            )

    // Arama metnini güncelleme metodu (Composable'dan arama kutusuna metin girildiğinde çağrılır)
    fun updateSearchText(query: String) {
        _searchText.value = query // MutableStateFlow'un değerini günceller, bu da filteredRecipes Flow'unu tetikler
    }


    // --- Tarif Detayı Flow'u ---

    // Belirli bir tarifi adına göre Room'dan reaktif olarak alma
    // Bu metot, Detay ekranında belirli bir tarifin adıyla çağrılır.
    fun getRecipeDetail(recipeName: String): StateFlow<RecipeEntity?> {
        // recipeDao.getRecipeByName(recipeName) Room'dan zaten bir Flow<RecipeEntity?> döndürür.
        // Bu Flow'u doğrudan bir StateFlow'a dönüştürüp döndürebiliriz.
        return recipeDao.getRecipeByName(recipeName)
            .stateIn(
                scope = viewModelScope, // ViewModel'ın yaşam döngüsüne bağlı scope
                started = SharingStarted.WhileSubscribed(5000), // UI aktifken veriyi paylaş
                initialValue = null // Başlangıçta null (tarif yüklenene kadar veya bulunamazsa)
            )
        // Eğer tarif adı boş gelirse veya DAO sorgusu sonuç döndürmezse (tarif bulunamazsa), null yayınlanacaktır.
        // Detay ekranı bu null durumu (yükleniyor veya bulunamadı) yönetmelidir.
    }

    // Gelecekte bu ViewModel'a tarif silme, düzenleme vb. metodlar da eklenebilir eğer sabit veri
    // fikri değişirse. Ancak şu anki plan sadece ön-dolum ve okuma üzerine.
}