package com.ugraks.project1.Fitness

import android.content.Context
import com.ugraks.project1.R
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.sequences.forEach

fun getExerciseImageResource(exerciseName: String): Int {
    return when (exerciseName) {
        "Push-up" -> R.drawable.push_up
        //"Incline Bench Press" -> R.drawable.bench_press
        "Deadlift" -> R.drawable.deadlift

        else -> R.drawable.baseline_fitness_center_24
    }
}

data class Exercise(
    val name: String,             // Egzersizin adı
    val muscleGroup: String,      // Kas grubu (Back, Legs, vb.)
    val description: String,      // Egzersizin açıklaması
    val howToDo: String           // Egzersizin nasıl yapılacağı açıklaması
)

fun loadExercisesFromAssets(context: Context): List<Exercise> {
    val exercises = mutableListOf<Exercise>()
    try {
        val inputStream = context.assets.open("fitness.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(";")
                if (parts.size == 4) {
                    val name = parts[0]
                    val muscleGroup = parts[1]
                    val description = parts[2]
                    val howToDo = parts[3] // Nasıl yapılacağı kısmı
                    exercises.add(Exercise(name, muscleGroup, description, howToDo))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return exercises
}