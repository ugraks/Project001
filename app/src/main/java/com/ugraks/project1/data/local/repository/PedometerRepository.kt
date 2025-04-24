package com.ugraks.project1.data.local.repository

import com.ugraks.project1.data.local.entity.DailyStepEntity
import kotlinx.coroutines.flow.Flow

// Pedometer Repository katmanının sözleşmesini tanımlar
// ViewModel, adımsayar verileriyle ilgili işlemleri bu arayüz üzerinden yapar.
interface PedometerRepository {

    // Tüm adım girişlerini Room'dan alır, tarihe göre gruplar ve reaktif olarak sağlar.
    // UI'daki özet ekranı için kullanılır.
    fun getDailyStepSummariesByDate(): Flow<Map<String, List<DailyStepEntity>>>

    // Yeni bir adım girişi (DailyStepEntity) ekler.
    // Adım sayma ekranında "Reset and Save" ile tetiklenir.
    suspend fun addStepEntry(entry: DailyStepEntity)

    // Belirli bir adım girişini (DailyStepEntity) siler.
    // Özet ekranında tekil girişleri silmek için kullanılır.
    suspend fun deleteStepEntry(entry: DailyStepEntity)

    // Tüm adım girişlerini siler.
    // Özet ekranında "Clear All" ile tetiklenir.
    suspend fun clearAllStepEntries()
}