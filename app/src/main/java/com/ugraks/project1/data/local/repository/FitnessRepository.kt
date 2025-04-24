package com.ugraks.project1.data.local.repository

import android.content.Context // Seed etme fonksiyonu için gerekebilir
import com.ugraks.project1.data.local.entity.ExerciseEntity // ExerciseEntity importu
import kotlinx.coroutines.flow.Flow

// Fitness Repository arayüzü - Sadece egzersiz verileriyle ilgili işlemleri tanımlar
interface FitnessRepository {

    // --- Egzersiz Operasyonları (Room) ---

    // Tüm egzersizleri Room'dan Flow olarak al
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    // Belirli kas gruplarına ait egzersizleri Room'dan Flow olarak al
    fun getExercisesByMuscleGroups(muscleGroups: List<String>): Flow<List<ExerciseEntity>>

    // Asset'ten egzersizleri okuyup Room'a yükleme (Seed etme) metodu
    // ViewModel veya uygulama başlatma logic'i bu metodu çağıracak
    suspend fun seedExercisesFromAssets(context: Context) // Context'i parametre olarak alabilir

    // Veritabanında egzersiz olup olmadığını kontrol etme (Seed kontrolü için kullanılabilir)
    suspend fun getExerciseCount(): Int
}