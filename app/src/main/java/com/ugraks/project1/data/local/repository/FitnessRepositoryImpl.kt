package com.ugraks.project1.data.local.repository

import android.content.Context
import com.ugraks.project1.Fitness.Exercise // Exercise data class importu
import com.ugraks.project1.Fitness.loadExercisesFromAssets // Asset okuma fonksiyonu importu
import com.ugraks.project1.data.local.dao.ExerciseDao // ExerciseDao importu
import com.ugraks.project1.data.local.entity.ExerciseEntity // ExerciseEntity importu
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Hilt ile tek örnek olarak sağlanacak (implementasyon sınıfı)
class FitnessRepositoryImpl @Inject constructor(
    // Fitness Repository Implementasyonu, egzersiz veri kaynaklarına (DAO ve asset okuyucu için Context) ihtiyaç duyar
    private val exerciseDao: ExerciseDao, // Egzersiz DAO'su
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: Context // Asset okuma için uygulama Context'i
) : FitnessRepository { // <-- FitnessRepository arayüzünü uyguladığını belirtiriz

    // --- Egzersiz Operasyonları (Room) ---

    // Tüm egzersizleri Room'dan Flow olarak alır.
    override fun getAllExercises(): Flow<List<ExerciseEntity>> {
        return exerciseDao.getAllExercises() // DAO'daki metodu çağırır
    }

    // Belirli kas gruplarına ait egzersizleri Room'dan Flow olarak alır.
    override fun getExercisesByMuscleGroups(muscleGroups: List<String>): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByMuscleGroups(muscleGroups) // DAO'daki metodu çağırır
    }

    // Asset'ten egzersizleri okuyup Room'a yükleme (Seed etme) metodu
    // ViewModel veya uygulama başlatma logic'i bu metodu çağıracak
    override suspend fun seedExercisesFromAssets(context: Context) {
        // Asset'ten Exercise data class listesini oku
        val exercises = loadExercisesFromAssets(context)

        // Exercise data class listesini ExerciseEntity listesine dönüştür (map et)
        val exerciseEntities = exercises.map { exercise ->
            ExerciseEntity( // ExerciseEntity constructor'ına Exercise'ın property'lerini veriyoruz
                name = exercise.name,
                muscleGroup = exercise.muscleGroup,
                description = exercise.description,
                howToDo = exercise.howToDo
                // Eğer ExerciseEntity'de otomatik ID varsa, burada 0 veya varsayılan değer kullanılır
            )
        }

        // Dönüştürülen Entity listesini Room'a ekle (DAO kullanarak)
        if (exerciseEntities.isNotEmpty()) {
            exerciseDao.insertExercises(exerciseEntities)
        }
    }

    // Veritabanında egzersiz olup olmadığını kontrol etme (Seed kontrolü için kullanılabilir)
    override suspend fun getExerciseCount(): Int {
        return exerciseDao.getExerciseCount()
    }
}