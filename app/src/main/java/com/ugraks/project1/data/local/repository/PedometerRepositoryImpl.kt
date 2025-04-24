package com.ugraks.project1.data.local.repository

import com.ugraks.project1.data.local.dao.DailyStepDao // DailyStepDao importu
import com.ugraks.project1.data.local.entity.DailyStepEntity // DailyStepEntity importu
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // Flow dönüşümleri için
import javax.inject.Inject // Inject için import

// PedometerRepository arayüzünün somut implementasyonu
// Bu sınıf, ViewModel ile Room DAO arasındaki köprüyü kurar.
class PedometerRepositoryImpl @Inject constructor(
    private val dailyStepDao: DailyStepDao // Room DAO bağımlılığı
) : PedometerRepository { // PedometerRepository arayüzünü implement eder

    // --- Repository Metotlarının Implementasyonu ---

    // Tüm adım girişlerini Room'dan alır, tarihe göre gruplar ve reaktif olarak sağlar.
    // ViewModel'daki gruplama mantığı buraya taşındı.
    override fun getDailyStepSummariesByDate(): Flow<Map<String, List<DailyStepEntity>>> {
        return dailyStepDao.getAllStepEntries() // Tüm girişleri DAO'dan al (Flow<List<DailyStepEntity>>)
            .map { entries -> // Gelen listeyi işle
                // Burası artık Repository'nin sorumluluğunda:
                entries.groupBy { it.date } // Tarihe göre grupla -> Map<String, List<DailyStepEntity>>
                // DAO'daki sıralama sayesinde tarihler zaten tersten sıralı gelecektir,
                // bu yüzden Map'in entrySetini ayrıca sıralamaya gerek kalmaz.
            }
    }

    // Yeni adım girişi ekleme
    // Doğrudan DailyStepDao'daki ilgili metodu çağırır.
    override suspend fun addStepEntry(entry: DailyStepEntity) {
        dailyStepDao.insertStepEntry(entry)
    }

    // Belirli bir adım girişini silme
    // Doğrudan DailyStepDao'daki ilgili metodu çağırır.
    override suspend fun deleteStepEntry(entry: DailyStepEntity) {
        dailyStepDao.deleteStepEntry(entry)
    }

    // Tüm adım girişlerini silme
    // Doğrudan DailyStepDao'daki ilgili metodu çağırır.
    override suspend fun clearAllStepEntries() {
        dailyStepDao.deleteAllStepEntries()
    }
}