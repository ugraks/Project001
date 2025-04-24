package com.ugraks.project1.Fitness // Kendi paket adınız

import android.content.Context // Artık Context parametresi Composable'a gerek YOK
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // StateFlow'u izlemek için
import androidx.compose.runtime.getValue // State değerini almak için
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Toast mesajı için
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // ViewModel'ı inject etmek için
import androidx.navigation.NavController
import com.ugraks.project1.AppNavigation.Screens
// Asset okuma importu artık Composable'da gerek YOK
// import java.io.BufferedReader
// import java.io.InputStreamReader
import com.ugraks.project1.ui.viewmodels.FitnessViewModel // YENİ: FitnessViewModel importu


@Composable
fun MainScreen(
    navController: NavController,
    viewModel: FitnessViewModel = hiltViewModel() // YENİ: ViewModel inject et
) {
    val context = LocalContext.current // Toast mesajı için Context

    // Egzersiz listesi ve kas grupları artık ViewModel'dan geliyor
    // val exercises = loadExercisesFromAssets(context) // Bu satır kaldırıldı
    // val muscleGroups = exercises.map { it.muscleGroup }.distinct() // Bu satır kaldırıldı

    // ViewModel'dan kas gruplarını StateFlow olarak izle
    val muscleGroups by viewModel.muscleGroups.collectAsState() // YENİ: ViewModel'dan al

    // Kullanıcı tarafından seçilen kas gruplarını Composable state'inde tut
    // ViewModel'daki selectedMuscleGroups state'ini de kullanabiliriz,
    // ancak basit bir seçim listesi için UI'da tutmak da uygundur.
    val selectedMuscleGroups = remember { mutableStateListOf<String>() }


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

        // Ana İçerik
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(120.dp)) // başlıkla geri butonu arasında boşluk

            // Başlık
            Text(
                text = "Select Muscle Groups",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 26.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // Checkbox Listesi (ViewModel'dan gelen muscleGroups listesini kullanır)
            // Eğer muscleGroups boşsa (ViewModel henüz yüklemediyse), boş liste gösterilir
            muscleGroups.forEach { muscleGroup -> // YENİ: ViewModel'dan gelen listeyi kullan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedMuscleGroups.contains(muscleGroup),
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedMuscleGroups.add(muscleGroup)
                            } else {
                                selectedMuscleGroups.remove(muscleGroup)
                            }
                            // Seçim değiştiğinde ViewModel'a bildirebiliriz (isteğe bağlı,
                            // navigasyondan önce toplu bildirme de yapılabilir)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurface,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = muscleGroup,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }

            // Göster Butonu
            Button(
                onClick = {
                    if (selectedMuscleGroups.isNotEmpty()) {
                        // Seçilen grupları ViewModel'a bildirme satırı kaldırıldı
                        // viewModel.setSelectedMuscleGroups(selectedMuscleGroups.toList()) // <-- BU SATIRI SİLİN

                        // Navigasyon (muscleGroupsString'i NavArgs olarak iletir)
                        val route = Screens.ExerciseListScreen.createRoute(
                            selectedMuscleGroups.joinToString(",")
                        )
                        navController.navigate(route)
                    } else {
                        Toast.makeText(
                            context,
                            "Please select at least one muscle group",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Show Exercises",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
        }
    }
}

// loadExercisesFromAssets fonksiyonu artık MainScreen'de değil, Repository veya ViewModel'da olmalı
// Bu fonksiyon MainScreen.kt dosyasından kaldırıldı
/*
fun loadExercisesFromAssets(context: Context): List<Exercise> {
    // ... fonksiyon içeriği ...
}
*/

// Exercise data class'ı bu dosyadan kaldırıldı, kendi dosyasında (Exercise.kt) tanımlı
// data class Exercise(...)