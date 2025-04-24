package com.ugraks.project1.data.local.repository

import android.content.Context // Seed etme fonksiyonu için gerekebilir
import com.ugraks.project1.data.local.entity.BoxingItemEntity // BoxingItemEntity importu
import kotlinx.coroutines.flow.Flow

// Boks Repository arayüzü - Sadece boks verileriyle ilgili işlemleri tanımlar
interface BoxingRepository {

    // --- Boks Öğesi Operasyonları (Room) ---

    // Tüm boks öğelerini Room'dan Flow olarak al
    fun getAllBoxingItems(): Flow<List<BoxingItemEntity>>

    // Belirli kategorilere ait boks öğelerini Room'dan Flow olarak al
    fun getBoxingItemsByCategories(categories: List<String>): Flow<List<BoxingItemEntity>>

    // Asset'ten boks öğelerini okuyup Room'a yükleme (Seed etme) metodu
    // ViewModel veya uygulama başlatma logic'i bu metodu çağıracak
    suspend fun seedBoxingItemsFromAssets(context: Context) // Context'i parametre olarak alabilir

    // Veritabanında boks öğesi olup olmadığını kontrol etme (Seed kontrolü için kullanılabilir)
    suspend fun getBoxingItemCount(): Int
}