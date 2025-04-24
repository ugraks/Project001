package com.ugraks.project1.data.repository // Kendi repository paket adınız

import com.ugraks.project1.KeepNoteComposable.FoodItemKeepNote // FoodItemKeepNote importu
import com.ugraks.project1.data.local.entity.CalorieRecordEntity // CalorieRecordEntity importu
import com.ugraks.project1.data.local.entity.DailySummaryEntity // DailySummaryEntity importu
import kotlinx.coroutines.flow.Flow

// Repository arayüzü - Repository'nin ne yapacağını tanımlar
interface CalorieRepository {

    // --- Food Item Operasyonları (Şimdilik Asset'ten Okuma) ---
    fun getFoodItemsFromAssets(): List<FoodItemKeepNote>

    // --- Kalori Kaydı Operasyonları (Room) ---
    fun getAllCalorieRecords(): Flow<List<CalorieRecordEntity>>
    suspend fun insertCalorieRecord(record: CalorieRecordEntity)
    suspend fun deleteCalorieRecord(record: CalorieRecordEntity)
    suspend fun deleteAllCalorieRecords()

    // --- Günlük Özet Operasyonları (Room) ---
    fun getAllDailySummaries(): Flow<List<DailySummaryEntity>>
    suspend fun getDailySummaryByDate(date: String): DailySummaryEntity?
    suspend fun insertOrUpdateDailySummary(summary: DailySummaryEntity)
    suspend fun deleteDailySummary(summary: DailySummaryEntity) // <-- BU SATIRI EKLİYORUZ
    suspend fun deleteAllDailySummaries()

    // NOT: DailySummaryEntity yapınıza göre metot imzalarını kontrol edin.
}