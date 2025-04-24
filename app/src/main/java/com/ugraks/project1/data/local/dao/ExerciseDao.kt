package com.ugraks.project1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ugraks.project1.data.local.entity.ExerciseEntity // ExerciseEntity importu
import kotlinx.coroutines.flow.Flow // Room'dan Flow almak için

@Dao // Data Access Object olduğunu belirtir
interface ExerciseDao {

    // Egzersizleri veritabanına ekler. Seed etme veya güncelleme için kullanılır.
    // OnConflictStrategy.REPLACE: Eğer aynı primary key'e sahip (aynı isimde) egzersiz varsa üzerine yazar.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>) // Birden fazla egzersizi list olarak eklemek için

    // Tüm egzersizleri veritabanından Flow olarak alır.
    @Query("SELECT * FROM exercises ORDER BY name ASC") // Egzersiz adına göre alfabetik sıralama
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    // Belirli kas gruplarına ait egzersizleri Flow olarak alır.
    // IN operatörü ile listedeki kas gruplarından birine ait olanları seçeriz.
    @Query("SELECT * FROM exercises WHERE muscleGroup IN (:muscleGroups) ORDER BY name ASC")
    fun getExercisesByMuscleGroups(muscleGroups: List<String>): Flow<List<ExerciseEntity>>

    // Eğer tüm egzersizleri silmek isterseniz (örneğin asset güncellemesi öncesi)
    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    // Veritabanında kaç egzersiz olduğunu kontrol etmek için (seed etme kontrolü için kullanılabilir)
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int
}