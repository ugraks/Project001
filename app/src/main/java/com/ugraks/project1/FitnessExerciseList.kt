package com.ugraks.project1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.collections.filter

@Composable
fun ExerciseListScreen(
    navController: NavController,
    muscleGroups: List<String>,
    exercises: List<Exercise>
) {
    val filteredExercises = exercises.filter { it.muscleGroup in muscleGroups }
    val expandedExercise = remember { mutableStateOf<Exercise?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔙 Geri Butonu
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 90.dp) // geri butonuyla başlık arasında boşluk
        ) {

            // 🏷 Başlık
            Text(
                text = "Exercises for ${muscleGroups.joinToString(", ")}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif, // Cursive yerine SansSerif kullanıldı
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp) // Başlıkla kartlar arasında boşluk arttırıldı
            )

            // 📋 Egzersiz Kartları
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(filteredExercises) { _, exercise ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp) // Kartlar arasındaki boşluk arttırıldı
                            .clickable {
                                expandedExercise.value =
                                    if (expandedExercise.value == exercise) null else exercise
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Kartlara gölge eklendi
                        shape = RoundedCornerShape(16.dp) // Kart köşeleri yuvarlatıldı
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Egzersiz Adı
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
                            )

                            // Resim
                            val imageResId = getExerciseImageResource(exercise.name)
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = exercise.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp) // Resmin boyutunu ayarlayabilirsiniz
                                    .padding(bottom = 12.dp) // Resim ile metin arasındaki boşluk arttırıldı
                                    .clip(RoundedCornerShape(16.dp)) // Resmin köşeleri yuvarlatıldı
                            )

                            // Egzersiz Açıklamaları ve Adımlar
                            AnimatedVisibility(visible = expandedExercise.value == exercise) {
                                Column {
                                    Text(
                                        text = "Muscle Group: ${exercise.muscleGroup}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = "Description: ${exercise.description}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = "How to do this exercise:",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    exercise.howToDo.split(" | ").forEach { step ->
                                        Text(
                                            text = step.trim(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Resmi dosya adından almak için yardımcı fonksiyon
fun getExerciseImageResource(exerciseName: String): Int {
    return when (exerciseName) {
        //"Push-up" -> R.drawable.push_up
        //"Incline Bench Press" -> R.drawable.bench_press
        //"Deadlift" -> R.drawable.deadlift
        // Diğer egzersizler için benzer şekilde ekleyin
        else -> R.drawable.baseline_fitness_center_24 // Varsayılan bir resim
    }
}