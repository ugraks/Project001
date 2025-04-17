package com.ugraks.project1

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun DailyCalories(navController: NavController) {
    val context = LocalContext.current // Toast için context

    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(false) }
    var isFemale by remember { mutableStateOf(false) }
    var isExercising by remember { mutableStateOf(false) }
    var selectedSport by remember { mutableStateOf("Yoga") }
    var exerciseDuration by remember { mutableStateOf("") }
    var dailyCalorieIntake by remember { mutableStateOf(0) }

    val sportsList = listOf(
        "Yoga", "Running (Normal)", "Cycling", "Swimming", "Basketball", "Football", "Tennis",
        "Badminton", "Boxing", "Hiking", "Dance", "Rowing", "Martial Arts", "Skiing", "Snowboarding",
        "Weightlifting", "Pilates", "Crossfit", "Jogging", "Power Walking", "Speed Walking",
        "Trail Running", "Nordic Walking", "Long-Distance Running", "Walking (4 km/h)",
        "Walking (5 km/h)", "Walking (6 km/h)", "Walking (7 km/h)", "Running (5 km/h)",
        "Running (10 km/h)", "Running (12 km/h)", "Running (14 km/h)", "Running (16 km/h)",
        "Wrestling", "Karate", "Judo", "Kickboxing", "Muay Thai", "Taekwondo",
        "Mountaineering (Dağcılık)"
    )

    val sportActivityFactors = mapOf(
        "Yoga" to 1.3, "Running (Normal)" to 1.6, "Running (5 km/h)" to 1.6, "Running (10 km/h)" to 1.9,
        "Running (12 km/h)" to 2.0, "Running (14 km/h)" to 2.2, "Running (16 km/h)" to 2.3,
        "Cycling" to 1.5, "Swimming" to 1.7, "Basketball" to 1.6, "Football" to 1.7, "Tennis" to 1.5,
        "Badminton" to 1.4, "Boxing" to 1.8, "Hiking" to 1.5, "Dance" to 1.4, "Rowing" to 1.5,
        "Martial Arts" to 1.6, "Skiing" to 1.7, "Snowboarding" to 1.7, "Weightlifting" to 1.5,
        "Pilates" to 1.4, "Crossfit" to 1.8, "Jogging" to 1.4, "Power Walking" to 1.3,
        "Speed Walking" to 1.3, "Trail Running" to 1.7, "Nordic Walking" to 1.5,
        "Long-Distance Running" to 1.8, "Walking (4 km/h)" to 1.2, "Walking (5 km/h)" to 1.3,
        "Walking (6 km/h)" to 1.4, "Walking (7 km/h)" to 1.5, "Wrestling" to 2.0, "Karate" to 1.8,
        "Judo" to 1.9, "Kickboxing" to 2.2, "Muay Thai" to 2.3, "Taekwondo" to 1.9,
        "Mountaineering (Dağcılık)" to 2.0
    )

    val colorScheme = MaterialTheme.colorScheme
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Geri butonu
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = topPadding + 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding + 50.dp, start = 16.dp, end = 16.dp, bottom = 64.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter Your Body Informations",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                color = colorScheme.primary,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            // Height & Weight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { if (it.all { c -> c.isDigit() }) height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { if (it.all { c -> c.isDigit() }) weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // Age
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { c -> c.isDigit() }) age = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Gender
            Spacer(modifier = Modifier.height(8.dp))
            Text("Gender:", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isMale,
                    onCheckedChange = {
                        if (it) {
                            isMale = true
                            isFemale = false
                        }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                )
                Text("Male", modifier = Modifier.padding(end = 16.dp))
                Checkbox(
                    checked = isFemale,
                    onCheckedChange = {
                        if (it) {
                            isFemale = true
                            isMale = false
                        }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                )
                Text("Female")
            }

            // Exercise
            Spacer(modifier = Modifier.height(8.dp))
            Text("Exercise:", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = !isExercising,
                    onCheckedChange = { if (it) isExercising = false },
                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                )
                Text("I don't exercise", modifier = Modifier.padding(end = 16.dp))
                Checkbox(
                    checked = isExercising,
                    onCheckedChange = { if (it) isExercising = true },
                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                )
                Text("I exercise")
            }

            // Sport & Duration
            if (isExercising) {
                var expanded by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = selectedSport,
                    onValueChange = {},
                    label = { Text("Select Sport") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sportsList.forEach { sport ->
                        DropdownMenuItem(
                            onClick = {
                                selectedSport = sport
                                expanded = false
                            },
                            text = { Text(sport) }
                        )
                    }
                }

                OutlinedTextField(
                    value = exerciseDuration,
                    onValueChange = { if (it.all { c -> c.isDigit() }) exerciseDuration = it },
                    label = { Text("Duration in minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            // Calculate Button
            Button(
                onClick = {
                    val validInput = height.isNotEmpty() &&
                            weight.isNotEmpty() &&
                            age.isNotEmpty() &&
                            (isMale || isFemale) &&
                            (
                                    !isExercising ||
                                            (isExercising && exerciseDuration.isNotEmpty() && selectedSport.isNotEmpty())
                                    )

                    if (!validInput) {
                        Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val h = height.toDouble()
                    val w = weight.toDouble()
                    val a = age.toDouble()

                    val bmr = if (isMale) {
                        10 * w + 6.25 * h - 5 * a + 5
                    } else {
                        10 * w + 6.25 * h - 5 * a - 161
                    }

                    val baseFactor = sportActivityFactors[selectedSport] ?: 1.2

                    val factor = if (isExercising && exerciseDuration.isNotEmpty()) {
                        val minutes = exerciseDuration.trim().toDoubleOrNull() ?: 0.0
                        val durationFactor = (minutes / 60.0).coerceAtLeast(0.1)
                        1.2 + (baseFactor - 1.2) * durationFactor
                    } else {
                        1.2
                    }

                    dailyCalorieIntake = (bmr * factor).toInt()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text("Calculate Calories", color = colorScheme.onPrimary)
            }

            // Result
            if (dailyCalorieIntake > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Daily Calorie Intake: $dailyCalorieIntake kcal",
                    fontSize = 20.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun DailyCaloriesPreview() {
    //DailyCalories()
}