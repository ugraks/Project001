package com.ugraks.project1.data.local.dao

import androidx.room.*
import com.ugraks.project1.data.local.entity.DailySummaryEntity
import kotlinx.coroutines.flow.Flow // Veri değişimlerini reaktif izlemek için

@Dao // Data Access Object olduğunu belirtir
interface DailySummaryDao {

    // saveTodaySummary fonksiyonunun ekleme/güncelleme mantığını karşılar.
    // Eğer aynı 'date' primary key'ine sahip özet varsa üzerine yazar (günceller).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSummary(summary: DailySummaryEntity)

    // readDailySummaries fonksiyonunun yerini alır. Tüm özetleri tarihe göre sıralayarak alır.
    // Flow döndürerek Room'daki değişiklikler olduğunda otomatik olarak UI'ı güncellemeyi sağlar.
    @Query("SELECT * FROM daily_summaries ORDER BY date DESC") // En yeni özet üste gelir
    fun getAllSummaries(): Flow<List<DailySummaryEntity>>

    // saveTodaySummary içindeki özetin varlığını kontrol etme veya DailySummariesPage'de belirli tarihi çekme için
    @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
    suspend fun getSummaryByDate(date: String): DailySummaryEntity?

    // deleteSummary fonksiyonunun yerini alır
    @Delete
    suspend fun deleteSummary(summary: DailySummaryEntity)

    // clearAllSummaries (DailySummary kısmı) fonksiyonunun yerini alır
    @Query("DELETE FROM daily_summaries")
    suspend fun deleteAllSummaries()
}