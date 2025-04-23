package com.ugraks.project1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ugraks.project1.data.local.entity.RecipeEntity // RecipeEntity'yi import edin
import kotlinx.coroutines.flow.Flow // Reaktif veri akışı için gerekli


// Tarif listesi ekranı için Entity'nin tüm detaylarını çekmek yerine
// sadece ihtiyacımız olan minimalist bilgiyi (adı) tutacak bir sınıf.
// Bu sınıf bir Entity değildir, sadece sorgu sonucu için bir POJO (Plain Old Java Object).
data class RecipeMinimal(
    val name: String
    // Eğer Entity'ye id ekleseydik: val id: Int
)

@Dao // Data Access Object olduğunu belirtir
interface RecipeDao {

    // Birden fazla tarifi Room'a ekler.
    // OnConflictStrategy.REPLACE: Eğer aynı Primary Key'e (yani aynı isme sahip) sahip bir tarif zaten varsa, eskisini silip yenisini ekler.
    // Bu, hem ilk ön-dolumda hem de gelecekte yapılacak güncellemelerde (yeni versiyonda asset'ten okuyup tekrar kaydederken) kullanışlıdır.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>) // Coroutine'de çalışacak

    // Veritabanındaki tüm tariflerin sadece adını (veya RecipeMinimal'i) reaktif olarak alır.
    // Bu, Liste ekranı için tüm tarif detaylarını yüklememizi engeller.
    // Adlarına göre alfabetik olarak sıralanır.
    @Query("SELECT name FROM recipes ORDER BY name ASC")
    fun getAllRecipeNames(): Flow<List<String>>

    /*
    // Eğer RecipeMinimal objeleri almak isterseniz:
    @Query("SELECT name FROM recipes ORDER BY name ASC")
    fun getAllRecipesMinimal(): Flow<List<RecipeMinimal>>
     */

    // Belirli bir tarifi adına göre Room'dan reaktif olarak alır.
    // Bu, Detay ekranı için kullanılır.
    @Query("SELECT * FROM recipes WHERE name = :recipeName LIMIT 1")
    fun getRecipeByName(recipeName: String): Flow<RecipeEntity?> // Tarif bulunamazsa null dönebilir

    // Arama kutusuna yazılan metne göre tarif adlarını reaktif olarak filtreler.
    // SQL'deki LIKE operatörü ve wildcard (%) kullanılır.
    @Query("SELECT name FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchRecipesByName(query: String): Flow<List<String>>

    /*
    // Eğer RecipeMinimal objeleri almak isterseniz:
    @Query("SELECT name FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchRecipesMinimal(query: String): Flow<List<RecipeMinimal>>
     */

    // Room'daki tariflerin toplam sayısını verir.
    // Bu, ViewModel'da ön-dolumun gerekip gerekmediğini kontrol etmek için kullanılır.
    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int

    // Tüm tarifleri siler. Gelecekteki güncellemelerde tablonun içeriğini boşaltmak için kullanılabilir.
    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()
}