package com.ugraks.project1.data.local.dao

import androidx.room.*
import com.ugraks.project1.data.local.entity.CalorieRecordEntity
import kotlinx.coroutines.flow.Flow // Veri değişimlerini reaktif izlemek için

@Dao // Data Access Object olduğunu belirtir
interface CalorieRecordDao {

    // saveCalorieRecords içindeki ekleme mantığını karşılar
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Çakışma olursa yeni kaydı yoksay (varsa üzerine yazmak isterseniz REPLACE)
    suspend fun insertRecord(record: CalorieRecordEntity) // suspend -> Coroutine'de çalışacak

    // loadCalorieRecords fonksiyonunun yerini alır. Tüm kayıtları tarihe göre (veya ID'ye göre) sıralayarak alır.
    // Flow döndürerek Room'daki değişiklikler olduğunda otomatik olarak UI'ı güncellemeyi sağlar.
    @Query("SELECT * FROM calorie_records ORDER BY id DESC") // En son eklenen üste gelir
    fun getAllRecords(): Flow<List<CalorieRecordEntity>>

    // deleteRecord fonksiyonunun yerini alır
    @Delete
    suspend fun deleteRecord(record: CalorieRecordEntity)

    // clearAllCalorieRecords (CalorieRecord kısmı) fonksiyonunun yerini alır
    @Query("DELETE FROM calorie_records")
    suspend fun deleteAllRecords()

    // Belirli bir kaydı ID'sine göre getirmek isterseniz
    @Query("SELECT * FROM calorie_records WHERE id = :recordId LIMIT 1")
    suspend fun getRecordById(recordId: Int): CalorieRecordEntity?
}