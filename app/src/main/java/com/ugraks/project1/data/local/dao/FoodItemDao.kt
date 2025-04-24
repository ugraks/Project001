package com.ugraks.project1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ugraks.project1.data.local.entity.FoodItemEntity // FoodItemEntity'yi import edin
import kotlinx.coroutines.flow.Flow // Reaktif veri akışı için

// Yemek öğelerine erişim için Data Access Object (DAO)
@Dao
interface FoodItemDao {

    // Birden fazla yemek öğesini Room'a ekler.
    // OnConflictStrategy.REPLACE: Eğer aynı Primary Key'e (yani aynı isme) sahip bir öğe zaten varsa, eskisini silip yenisini ekler.
    // Bu, ilk ön-dolumda ve asset'ten yapılacak güncellemelerde kullanışlıdır.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItemEntity>) // Coroutine'de çalışacak

    // Veritabanındaki tüm yemek öğelerinin sadece adını reaktif olarak alır.
    // Arama ekranı için tüm detayları yüklememizi engeller ve UI'ı günceller.
    // Adlarına göre alfabetik olarak sıralanır.
    @Query("SELECT name FROM food_items ORDER BY name ASC")
    fun getAllFoodItemNames(): Flow<List<String>>

    // Belirli bir yemek öğesini adına göre Room'dan reaktif olarak alır.
    // Kalori hesaplama ekranı için kullanılır.
    @Query("SELECT * FROM food_items WHERE name = :itemName LIMIT 1")
    fun getFoodItemByName(itemName: String): Flow<FoodItemEntity?> // Öğe bulunamazsa null dönebilir

    // Arama kutusuna yazılan metne göre yemek öğesi adlarını reaktif olarak filtreler.
    // SQL'deki LIKE operatörü ve wildcard (%) kullanılır.
    @Query("SELECT name FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFoodItemNames(query: String): Flow<List<String>>

    // Room'daki yemek öğelerinin toplam sayısını verir.
    // Bu, Repository'de ön-dolumun gerekip gerekmediğini kontrol etmek için kullanılır.
    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun getFoodItemCount(): Int

    // (Opsiyonel) Tüm yemek öğelerini siler. Asset'ten tamamen yeni veri yüklerken kullanılabilir.
    @Query("DELETE FROM food_items")
    suspend fun deleteAllFoodItems()
}