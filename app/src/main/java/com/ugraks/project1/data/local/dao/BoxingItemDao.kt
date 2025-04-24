package com.ugraks.project1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ugraks.project1.data.local.entity.BoxingItemEntity // BoxingItemEntity importu
import kotlinx.coroutines.flow.Flow // Room'dan Flow almak için

@Dao // Data Access Object olduğunu belirtir
interface BoxingItemDao {

    // Boks öğelerini veritabanına ekler. Seed etme veya güncelleme için kullanılır.
    // OnConflictStrategy.REPLACE: Eğer aynı primary key'e sahip (aynı isimde) öğe varsa üzerine yazar.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoxingItems(items: List<BoxingItemEntity>) // Birden fazla öğeyi list olarak eklemek için

    // Tüm boks öğelerini veritabanından Flow olarak alır.
    @Query("SELECT * FROM boxing_items ORDER BY name ASC") // Öğe adına göre alfabetik sıralama
    fun getAllBoxingItems(): Flow<List<BoxingItemEntity>>

    // Belirli kategorilere ait boks öğelerini Flow olarak alır.
    // IN operatörü ile listedeki kategorilerden birine ait olanları seçeriz.
    @Query("SELECT * FROM boxing_items WHERE category IN (:categories) ORDER BY name ASC")
    fun getBoxingItemsByCategories(categories: List<String>): Flow<List<BoxingItemEntity>>

    // Eğer tüm boks öğelerini silmek isterseniz (örneğin asset güncellemesi öncesi)
    @Query("DELETE FROM boxing_items")
    suspend fun deleteAllBoxingItems()

    // Veritabanında kaç boks öğesi olduğunu kontrol etmek için (seed etme kontrolü için kullanılabilir)
    @Query("SELECT COUNT(*) FROM boxing_items")
    suspend fun getBoxingItemCount(): Int
}