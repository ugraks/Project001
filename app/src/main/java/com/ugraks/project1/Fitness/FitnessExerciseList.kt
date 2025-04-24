package com.ugraks.project1.Fitness // Kendi paket adınız

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // StateFlow'u izlemek için
import androidx.compose.runtime.getValue // State değerini almak için
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // ViewModel'ı inject etmek için
import androidx.navigation.NavController
import com.ugraks.project1.R
// Exercise data class'ı yerine Entity kullanılacak
// import kotlin.collections.filter // Filter fonksiyonu hala kullanılabilir ama ViewModel'da yapılıyor
import com.ugraks.project1.data.local.entity.ExerciseEntity // YENİ: ExerciseEntity importu
import com.ugraks.project1.ui.viewmodels.FitnessViewModel // YENİ: FitnessViewModel importu


@Composable
fun ExerciseListScreen(
    navController: NavController,
    muscleGroups: List<String>, // Başlık için hala parametre olarak gelebilir (NavArgs'tan)
    // Egzersiz listesi artık ViewModel'dan gelecek, parametre kaldırıldı
    // exercises: List<Exercise>, // Bu parametre kaldırıldı
    viewModel: FitnessViewModel = hiltViewModel() // YENİ: ViewModel inject et
) {
    // Egzersiz listesi artık ViewModel'dan geliyor ve ViewModel'da filtreleniyor
    // val filteredExercises = exercises.filter { it.muscleGroup in muscleGroups } // Bu satır kaldırıldı

    // ViewModel'dan filtrelenmiş egzersiz listesini StateFlow olarak izle
    val filteredExercises by viewModel.filteredExercises.collectAsState() // YENİ: ViewModel'dan al


    // Genişletilmiş egzersiz state'i (UI state'i olarak kalır, tipi ExerciseEntity olacak)
    val expandedExercise = remember { mutableStateOf<ExerciseEntity?>(null) } // YENİ: Tipi ExerciseEntity?
    Log.d("ExerciseListScreen", "NavArgs Muscle Groups: ${muscleGroups.joinToString(",")}") // <-- Bu satırı ekleyin

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

            // 🏷 Başlık (Parametreden gelen kas gruplarını kullanır)
            Text(
                text = "Exercises for ${muscleGroups.joinToString(", ")}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // 📋 Egzersiz Kartları (ViewModel'dan gelen filteredExercises listesini kullanır)
            // Liste boşsa (ViewModel henüz yüklemediyse veya filtre sonucu boşsa) boş liste gösterilir
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // itemsIndexed ViewModel'dan gelen filteredExercises listesini kullanır (List<ExerciseEntity>)
                itemsIndexed(filteredExercises, key = { _, exercise -> exercise.name }) { _, exercise -> // exercise artık ExerciseEntity tipinde
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable {
                                expandedExercise.value =
                                    if (expandedExercise.value == exercise) null else exercise
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Egzersiz Adı (exercise.name - ExerciseEntity'de mevcut)
                            Text(
                                text = exercise.name, // YENİ: ExerciseEntity'den adı al
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            // Resim veya İkon (exercise.name - ExerciseEntity'de mevcut)
                            val currentImageResource = if (expandedExercise.value == exercise) {
                                // Kart açıksa, egzersize özel resmi al
                                getExerciseImageResource(exercise.name) // exercise.name hala String
                            } else {
                                // Kart kapalıysa, varsayılan fitness ikonunu göster
                                R.drawable.baseline_fitness_center_24 // Sizin varsayılan ikonunuz
                            }

                            Image(
                                painter = painterResource(id = currentImageResource),
                                contentDescription = if (expandedExercise.value == exercise) exercise.name else "Fitness Icon",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                // contentScale = ContentScale.Crop
                            )


                            // Egzersiz Açıklamaları ve Adımlar (ExerciseEntity'den alınır)
                            // AnimatedVisibility'nin visible kontrolü hala ExerciseEntity tipini kullanır
                            AnimatedVisibility(visible = expandedExercise.value == exercise) {
                                Column {
                                    // muscleGroup (exercise.muscleGroup - ExerciseEntity'de mevcut)
                                    Text(
                                        text = "Muscle Group: ${exercise.muscleGroup}", // YENİ: ExerciseEntity'den al
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    // description (exercise.description - ExerciseEntity'de mevcut)
                                    Text(
                                        text = "Description: ${exercise.description}", // YENİ: ExerciseEntity'den al
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

                                    // howToDo (exercise.howToDo - ExerciseEntity'de mevcut)
                                    exercise.howToDo.split(" | ").forEach { step -> // YENİ: ExerciseEntity'den al
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
                // Liste boşsa bilgi mesajı göster
                item {
                    if (filteredExercises.isEmpty()) {
                        Box(
                            modifier = Modifier.fillParentMaxSize(), // LazyColumn içinde tam alanı kapla
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No exercises found for selected muscle groups.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// getExerciseImageResource fonksiyonu bu dosyadan kaldırılmadı, hala burada veya ayrı bir Utils dosyasında olabilir.
// fun getExerciseImageResource(exerciseName: String): Int { ... }

// Exercise data class'ı bu dosyadan kaldırıldı, kendi dosyasında (Exercise.kt) tanımlı
// data class Exercise(...)