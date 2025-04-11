package com.ugraks.project1

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun DailyCalories(navController: NavController) {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    // Cinsiyet seçimi
    var isMale by remember { mutableStateOf(false) }
    var isFemale by remember { mutableStateOf(false) }

    // Spor yapıyor mu? Durumu
    var isExercising by remember { mutableStateOf(false) }
    var selectedSport by remember { mutableStateOf("Yoga") }
    var exerciseDuration by remember { mutableStateOf("") }

    // Günlük kalori ihtiyacı
    var dailyCalorieIntake by remember { mutableStateOf(0) }

    // Sporlar Listesi
    val sportsList = listOf(
        "Yoga",
        "Running (Normal)",
        "Cycling",
        "Swimming",
        "Basketball",
        "Football",
        "Tennis",
        "Badminton",
        "Boxing",
        "Hiking",
        "Dance",
        "Rowing",
        "Martial Arts",
        "Skiing",
        "Snowboarding",
        "Weightlifting",
        "Pilates",
        "Crossfit",
        "Jogging",
        "Power Walking",
        "Speed Walking",
        "Trail Running",
        "Nordic Walking",
        "Long-Distance Running",
        "Walking (4 km/h)",
        "Walking (5 km/h)",
        "Walking (6 km/h)",
        "Walking (7 km/h)",
        "Running (5 km/h)",
        "Running (10 km/h)",
        "Running (12 km/h)",
        "Running (14 km/h)",
        "Running (16 km/h)",
        "Wrestling",
        "Karate",
        "Judo",
        "Kickboxing",
        "Muay Thai",
        "Taekwondo",
        "Mountaineering (Dağcılık)"
    )

    // Aktivite faktörleri
    val sportActivityFactors = mapOf(
        "Yoga" to 1.3,
        "Running (Normal)" to 1.6,
        "Running (5 km/h)" to 1.6,
        "Running (10 km/h)" to 1.9,
        "Running (12 km/h)" to 2.0,
        "Running (14 km/h)" to 2.2,
        "Running (16 km/h)" to 2.3,
        "Cycling" to 1.5,
        "Swimming" to 1.7,
        "Basketball" to 1.6,
        "Football" to 1.7,
        "Tennis" to 1.5,
        "Badminton" to 1.4,
        "Boxing" to 1.8,
        "Hiking" to 1.5,
        "Dance" to 1.4,
        "Rowing" to 1.5,
        "Martial Arts" to 1.6,
        "Skiing" to 1.7,
        "Snowboarding" to 1.7,
        "Weightlifting" to 1.5,
        "Pilates" to 1.4,
        "Crossfit" to 1.8,
        "Jogging" to 1.4,
        "Power Walking" to 1.3,
        "Speed Walking" to 1.3,
        "Trail Running" to 1.7,
        "Nordic Walking" to 1.5,
        "Long-Distance Running" to 1.8,
        "Walking (4 km/h)" to 1.2,
        "Walking (5 km/h)" to 1.3,
        "Walking (6 km/h)" to 1.4,
        "Walking (7 km/h)" to 1.5,
        "Wrestling" to 2.0,
        "Karate" to 1.8,
        "Judo" to 1.9,
        "Kickboxing" to 2.2,
        "Muay Thai" to 2.3,
        "Taekwondo" to 1.9,
        "Mountaineering (Dağcılık)" to 2.0
    )


    Box(modifier = Modifier.fillMaxSize()) {

        // Geri Butonu (sol üstte sabit)
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = Color.Magenta,
                modifier = Modifier.size(30.dp)
            )
        }
    }
    val topPadding = WindowInsets.statusBars
        .asPaddingValues().calculateTopPadding()

        // Column düzeni
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding + 48.dp, // status bar + sabit boşluk
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Başlık
            Text(
                text = "Enter Your Body Informations:",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                modifier = Modifier.padding(bottom = 40.dp, top = 20.dp)
                    .align(Alignment.CenterHorizontally),
                color = Color.Magenta
            )


            // Row içinde iki TextField'ı yan yana yerleştiriyoruz
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Height input alanı
                OutlinedTextField(
                    value = height,
                    onValueChange = {
                        // Sadece sayılar geçerli
                        if (it.all { it.isDigit() }) {
                            height = it
                        }
                    },
                    label = { Text("Height (cm)") },
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                // Weight input alanı
                OutlinedTextField(
                    value = weight,
                    onValueChange = { newValue ->
                        // Sadece sayılar geçerli
                        if (newValue.all { it.isDigit() }) {
                            weight = newValue
                        }
                    },
                    label = { Text("Weight (kg)") },
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Yaş input alanı
            OutlinedTextField(
                value = age,
                onValueChange = {
                    if (it.all { it.isDigit() }) {
                        age = it
                    }
                },
                label = { Text("Age") },
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(6.dp)
            )

            // Cinsiyet seçimi
            Spacer(modifier = Modifier.height(16.dp))
            Text("Gender:", fontWeight = FontWeight.Bold)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(6.dp)
            ) {
                Checkbox(
                    checked = isMale,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            isMale = true
                            isFemale = false // Only one checkbox can be selected
                        }
                    }, colors = CheckboxDefaults.colors(
                        checkedColor = Color.Magenta,
                        checkmarkColor = Color.White
                    )
                )
                Text("Male")

                Spacer(modifier = Modifier.width(16.dp))

                Checkbox(
                    checked = isFemale,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            isFemale = true
                            isMale = false // Only one checkbox can be selected
                        }
                    }, colors = CheckboxDefaults.colors(
                        checkedColor = Color.Magenta,
                        checkmarkColor = Color.White
                    )
                )
                Text("Female")
            }

            // Spor yapıp yapmadığını belirleyen Checkbox'lar
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = !isExercising, // I don't exercise seçeneği
                    onCheckedChange = { isChecked ->
                        if (isChecked) isExercising = false
                    },
                    modifier = Modifier.padding(6.dp), colors = CheckboxDefaults.colors(
                        checkedColor = Color.Magenta,
                        checkmarkColor = Color.White
                    )
                )
                Text("I don't exercise")

                Spacer(modifier = Modifier.width(16.dp))

                Checkbox(
                    checked = isExercising, // I exercise seçeneği
                    onCheckedChange = { isChecked ->
                        if (isChecked) isExercising = true
                    },
                    modifier = Modifier.padding(6.dp), colors = CheckboxDefaults.colors(
                        checkedColor = Color.Magenta,
                        checkmarkColor = Color.White
                    )
                )
                Text("I exercise")
            }

            // Eğer spor yapıyorsa, dropdown ve süre girişi
            if (isExercising) {
                Spacer(modifier = Modifier.height(16.dp))

                // DropdownMenu
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedSport,
                        onValueChange = {},
                        label = { Text("Select Sport") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {

                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Icon"
                                )
                            }
                        }
                    )

                    // Dropdown
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
                }

                // Süre girişi
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = exerciseDuration,
                    onValueChange = {
                        if (it.all { it.isDigit() }) {
                            exerciseDuration = it
                        }
                    },
                    label = { Text("Duration in minutes") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Submit Butonu
            Button(
                onClick = {
                    if (height.isNotEmpty() && weight.isNotEmpty() && age.isNotEmpty() && (isMale || isFemale)) {
                        // Boy, kilo, yaş ve cinsiyet verisi alındıysa kalori hesaplaması yapılacak
                        val heightInCm = height.toInt()
                        val weightInKg = weight.toInt()
                        val ageInYears = age.toInt()

                        // Cinsiyet seçimi için BMR hesaplama
                        val bmr = if (isMale) {
                            // Erkek için BMR hesaplaması
                            88.362 + (13.397 * weightInKg) + (4.799 * heightInCm) - (5.677 * ageInYears)
                        } else {
                            // Kadın için BMR hesaplaması
                            447.593 + (9.247 * weightInKg) + (3.098 * heightInCm) - (4.330 * ageInYears)
                        }

                        // Aktivite faktörünü hesaplayalım
                        val activityFactor = sportActivityFactors[selectedSport] ?: 1.2
                        dailyCalorieIntake = (bmr * activityFactor).toInt()

                        // Sonuçları ekranda gösterebiliriz (Örnek)
                        println("BMR: $bmr, Kalori ihtiyacı: $dailyCalorieIntake")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)
            ) {
                Text(text = "Calculate Calories")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (dailyCalorieIntake > 0) {
                Text(
                    text = "Daily Calorie Intake: $dailyCalorieIntake kcal",
                    fontSize = 20.sp,
                    color = Color.Green,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }


@Preview(showBackground = true)
@Composable
fun DailyCaloriesPreview() {
    //DailyCalories()
}