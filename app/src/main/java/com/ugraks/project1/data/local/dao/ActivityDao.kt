package com.ugraks.project1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ugraks.project1.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao // Bu arayüzün bir DAO olduğunu belirtir
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Çakışma durumunda mevcut veriyi güncelle
    suspend fun insertActivities(activities: List<ActivityEntity>)
    // TXT dosyasından okunan aktivitelerin listesini eklemek için

    @Query("SELECT * FROM activities ORDER BY name ASC") // Tüm aktiviteleri alfabetik sıraya göre getir
    fun getAllActivities(): Flow<List<ActivityEntity>>
    // Veritabanından tüm aktiviteleri almak için. Flow kullanarak veri değişimlerini gözlemleyebiliriz.

    @Query("SELECT * FROM activities WHERE name = :activityName LIMIT 1") // Belirli bir aktiviteyi adına göre getir
    suspend fun getActivityByName(activityName: String): ActivityEntity?
    // Seçilen aktivitenin MET değerini almak için

    @Query("DELETE FROM activities") // Tüm aktiviteleri silmek için (aset güncellemelerinde kullanışlı olabilir)
    suspend fun deleteAllActivities()
}