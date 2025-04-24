package com.ugraks.project1.DailyCalorie // Paket adınızı projenize göre ayarlayın

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // Bu import hala expanded state'i için gerekli
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue // Bu import hala expanded state'i için gerekli
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ugraks.project1.presentation.dailycalorie.DailyCalorieViewModel // ViewModel'inizi import edin

@Composable
fun DailyCalories(
    navController: NavController,
    viewModel: DailyCalorieViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // ViewModel'deki State'lerin değerlerine .value ile erişiyoruz
    // Bu, "by" delegasyon hatasını atlatmamıza yardımcı olmalı
    val height = viewModel.height.value
    val weight = viewModel.weight.value
    val age = viewModel.age.value
    val isMale = viewModel.isMale.value
    val isFemale = viewModel.isFemale.value
    val isExercising = viewModel.isExercising.value
    val exerciseDuration = viewModel.exerciseDuration.value
    val dailyCalorieIntake = viewModel.dailyCalorieIntake.value
    val showErrorToast = viewModel.showErrorToast.value

    // Aktiviteler listesi Flow'dan State'e collectAsState ile dönüştürülür ve sonra .value ile değeri alınır
    val activities = viewModel.activities.collectAsState().value
    val selectedSport = viewModel.selectedSport // selectedSport ViewModel'de "by" ile tanımlı olduğu için doğrudan kullanabiliriz veya .value yapabiliriz. ViewModel içinde "by" kullandık, burada da Composable'da "by" kullanalım daha tutarlı olur.
    val selectedSportBy = viewModel.selectedSport // selectedSport ViewModel'de "by" ile tanımlıysa Composable'da da "by" kullanmak daha iyidir. Önceki ViewModel kodunda selectedSport "by" ile tanımlıydı, bu versiyonda da öyle kalsın.

    // Dropdown menü açık/kapalı durumu Composable'ın kendi state'i olabilir
    var expanded by remember { mutableStateOf(false) } // Bu state Composable'ın kendi state'i

    // ViewModel'den gelen hata mesajı state'ini dinle ve Toast göster
    LaunchedEffect(showErrorToast) {
        if (showErrorToast) {
            Toast.makeText(context, "Please fill in all required fields correctly.", Toast.LENGTH_SHORT).show()
            viewModel.toastShown()
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { viewModel.onHeightChange(it) },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { viewModel.onWeightChange(it) },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = age,
                onValueChange = { viewModel.onAgeChange(it) },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Gender:", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isMale,
                    onCheckedChange = { viewModel.onGenderChange(isMale = it, isFemale = !it) },
                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                )
                Text("Male", modifier = Modifier.padding(end = 16.dp))
                Checkbox(
                    checked = isFemale,
                    onCheckedChange = { viewModel.onGenderChange(isMale = !it, isFemale = it) },
                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                )
                Text("Female")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Exercise:", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = !isExercising,
                    onCheckedChange = { if (it) viewModel.onExerciseStatusChange(isExercising = false) },
                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                )
                Text("I don't exercise", modifier = Modifier.padding(end = 16.dp))
                Checkbox(
                    checked = isExercising,
                    onCheckedChange = { if (it) viewModel.onExerciseStatusChange(isExercising = true) },
                    colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
                )
                Text("I exercise")
            }

            // Sport & Duration (Sadece egzersiz yapılıyorsa ve aktiviteler yüklendiyse göster)
            if (isExercising && activities.isNotEmpty()) {
                OutlinedTextField(
                    value = selectedSportBy, // ViewModel'den "by" ile alınan değeri kullan
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
                    activities.forEach { sport ->
                        DropdownMenuItem(
                            onClick = {
                                viewModel.onSportSelect(sport)
                                expanded = false
                            },
                            text = { Text(sport) }
                        )
                    }
                }

                OutlinedTextField(
                    value = exerciseDuration,
                    onValueChange = { viewModel.onExerciseDurationChange(it) },
                    label = { Text("Duration in minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            } else if (isExercising && activities.isEmpty()) {
                Text("Loading activities...", modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = { viewModel.calculateCalories() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text("Calculate Calories", color = colorScheme.onPrimary)
            }

            if (dailyCalorieIntake > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Estimated Daily Calorie Need: $dailyCalorieIntake kcal",
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
    Text("Preview not available for Hilt ViewModel")
}