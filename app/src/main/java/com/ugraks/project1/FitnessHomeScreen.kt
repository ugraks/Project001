package com.ugraks.project1

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ugraks.project1.AppNavigation.Screens
import java.io.BufferedReader
import java.io.InputStreamReader


@Composable
fun MainScreen(navController: NavController, context: Context) {
    val exercises = loadExercisesFromAssets(context)
    val muscleGroups = exercises.map { it.muscleGroup }.distinct()
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

            // Checkbox Listesi
            muscleGroups.forEach { muscleGroup ->
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