package com.ugraks.project1.data.local.repository

import android.content.Context
import com.ugraks.project1.Boxing.BoxingItem // BoxingItem data class importu
import com.ugraks.project1.Boxing.loadBoxingDataFromAssets // Asset okuma fonksiyonu importu
import com.ugraks.project1.data.local.dao.BoxingItemDao // BoxingItemDao importu
import com.ugraks.project1.data.local.entity.BoxingItemEntity // BoxingItemEntity importu
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Hilt ile tek örnek olarak sağlanacak (implementasyon sınıfı)
class BoxingRepositoryImpl @Inject constructor(
    // Boxing Repository Implementasyonu, boks verisi kaynaklarına (DAO ve asset okuyucu için Context) ihtiyaç duyar
    private val boxingItemDao: BoxingItemDao, // Boks Öğesi DAO'su
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: Context // Asset okuma için uygulama Context'i
) : BoxingRepository { // <-- BoxingRepository arayüzünü uyguladığını belirtiriz

    // --- Boks Öğesi Operasyonları (Room) ---

    // Tüm boks öğelerini Room'dan Flow olarak alır.
    override fun getAllBoxingItems(): Flow<List<BoxingItemEntity>> {
        return boxingItemDao.getAllBoxingItems() // DAO'daki metodu çağırır
    }

    // Belirli kategorilere ait boks öğelerini Room'dan Flow olarak alır.
    override fun getBoxingItemsByCategories(categories: List<String>): Flow<List<BoxingItemEntity>> {
        return boxingItemDao.getBoxingItemsByCategories(categories) // DAO'daki metodu çağırır
    }

    // Asset'ten boks öğelerini okuyup Room'a yükleme (Seed etme) metodu
    // ViewModel veya uygulama başlatma logic'i bu metodu çağıracak
    override suspend fun seedBoxingItemsFromAssets(context: Context) {
        // Asset'ten BoxingItem data class listesini oku
        val boxingItems = loadBoxingDataFromAssets(context)

        // BoxingItem data class listesini BoxingItemEntity listesine dönüştür (map et)
        val boxingItemEntities = boxingItems.map { item ->
            BoxingItemEntity( // BoxingItemEntity constructor'ına BoxingItem'ın property'lerini veriyoruz
                name = item.name,
                category = item.category,
                description = item.description,
                details = item.details
                // Eğer BoxingItemEntity'de otomatik ID varsa, burada 0 veya varsayılan değer kullanılır
            )
        }

        // Dönüştürülen Entity listesini Room'a ekle (DAO kullanarak)
        if (boxingItemEntities.isNotEmpty()) {
            boxingItemDao.insertBoxingItems(boxingItemEntities)
        }
    }

    // Veritabanında boks öğesi olup olmadığını kontrol etme (Seed kontrolü için kullanılabilir)
    override suspend fun getBoxingItemCount(): Int {
        return boxingItemDao.getBoxingItemCount()
    }
}