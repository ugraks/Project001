package com.ugraks.project1.data.local.dao

import androidx.room.*
import com.ugraks.project1.data.local.entity.DailyStepEntity // Entity'yi import edin
import kotlinx.coroutines.flow.Flow // Reaktif veri akışı için

@Dao // Data Access Object olduğunu belirtir
interface DailyStepDao {

    // Yeni bir adım girişi ekler. saveDailyStepCount fonksiyonundaki ekleme mantığını karşılar (ancak dosyayı baştan yazmaz).
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Çakışma olursa (aynı ID - olmamalı) yoksay
    suspend fun insertStepEntry(entry: DailyStepEntity) // suspend -> Coroutine'de çalışacak

    // Tüm adım girişlerini Room'dan alır. DailySummaryPage'deki loadDailySummaries mantığını karşılar (ancak gruplama burada değil).
    // Flow döndürerek veri değişimlerini reaktif olarak izlemeyi sağlar.
    @Query("SELECT * FROM daily_steps ORDER BY date DESC, timestamp DESC") // Önce tarihe, sonra zaman damgasına göre sırala
    fun getAllStepEntries(): Flow<List<DailyStepEntity>>

    // Belirli bir adım girişini siler (ID'sine göre). deleteEntry fonksiyonunun yerini alır (ancak index değil ID kullanır).
    @Delete
    suspend fun deleteStepEntry(entry: DailyStepEntity)

    // Tüm adım girişlerini siler. clearAllSummaries fonksiyonunun yerini alır.
    @Query("DELETE FROM daily_steps")
    suspend fun deleteAllStepEntries()

    // (Opsiyonel) Belirli bir güne ait tüm girişleri almak isterseniz
    @Query("SELECT * FROM daily_steps WHERE date = :date ORDER BY timestamp ASC")
    fun getStepEntriesByDate(date: String): Flow<List<DailyStepEntity>>
}