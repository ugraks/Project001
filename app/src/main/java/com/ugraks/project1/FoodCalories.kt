package com.ugraks.project1

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCaloriesScreen(foodItemName: String, navController: NavController) {
    val context = LocalContext.current

    val foodItems = readAndParseItemsFromAssets(context)


    val selectedItem = foodItems.find { it.name == foodItemName }

    if (selectedItem != null) {
        var quantity by remember { mutableStateOf("0") }
        var isKg by remember { mutableStateOf(selectedItem.type == "Food") }
        var isLiters by remember { mutableStateOf(selectedItem.type == "Drink") }

        var totalCalories by remember { mutableStateOf(0) }
        var totalProtein by remember { mutableStateOf(0.0) }
        var totalFat by remember { mutableStateOf(0.0) }
        var totalCarb by remember { mutableStateOf(0.0) }

        val updateNutritionalValues: (Int, Boolean, Boolean, FoodItem?) -> Unit = { qty, kg, liters, item ->
            if (item != null && qty > 0) {
                val calculated = calculateNutritionalValues(item, qty, kg, liters)
                totalCalories = calculated.calories
                totalProtein = calculated.protein
                totalFat = calculated.fat
                totalCarb = calculated.carb
            } else {
                totalCalories = 0
                totalProtein = 0.0
                totalFat = 0.0
                totalCarb = 0.0
            }
        }

        LaunchedEffect(selectedItem) {
            val initialQuantityInt = quantity.toIntOrNull() ?: 0
            updateNutritionalValues(initialQuantityInt, isKg, isLiters, selectedItem)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 16.dp) // Adjusted horizontal padding
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Your Food Information:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier.padding(bottom = 24.dp), // Increased bottom padding
                    color = MaterialTheme.colorScheme.primary
                )

                val imageResId = getImageResource(selectedItem.name)
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "${selectedItem.name} image",
                    modifier = Modifier
                        .size(180.dp) // Increased image size
                        .padding(bottom = 24.dp) // More space after image
                        .clip(RoundedCornerShape(16.dp))
                )

                Text(
                    text = selectedItem.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 24.dp), // More space after name
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        val newValue = it.filter { char -> char.isDigit() }
                        quantity = newValue
                        val quantityInt = newValue.toIntOrNull() ?: 0
                        updateNutritionalValues(quantityInt, isKg, isLiters, selectedItem)
                    },
                    label = { Text("Quantity", color = MaterialTheme.colorScheme.primary) },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = selectedItem != null
                )

                Spacer(modifier = Modifier.height(16.dp))
                if (selectedItem.type == "Food" || selectedItem.type == "Drink") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedItem.type == "Food") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isKg,
                                    onClick = {
                                        isKg = true
                                        isLiters = false
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        updateNutritionalValues(quantityInt, isKg, isLiters, selectedItem)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                                Text("kg", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.width(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = !isKg,
                                    onClick = {
                                        isKg = false
                                        isLiters = false
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        updateNutritionalValues(quantityInt, isKg, isLiters, selectedItem)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                                Text("gr", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        } else { // Drink
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isLiters,
                                    onClick = {
                                        isLiters = true
                                        isKg = false
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        updateNutritionalValues(quantityInt, isKg, isLiters, selectedItem)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                                Text("Lt", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.width(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = !isLiters,
                                    onClick = {
                                        isLiters = false
                                        isKg = false
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        updateNutritionalValues(quantityInt, isKg, isLiters, selectedItem)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                                Text("ml", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Display nutritional information without a background box
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally // Center the text lines
                ) {
                    Text(
                        text = "Nutritional Info:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp) // Space below header
                    )

                    Text(
                        text = "Calories: $totalCalories kcal",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp) // Space below Calories
                    )

                    // Display Macros only if calculated (quantity > 0)
                    if (quantity.toIntOrNull() ?: 0 > 0) {
                        // No Spacer needed between lines, padding on Text handles it
                        Text(
                            text = "Protein: ${totalProtein.roundToInt()} g",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp) // Space below Protein
                        )
                        Text(
                            text = "Fat: ${totalFat.roundToInt()} g",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp) // Space below Fat
                        )
                        Text(
                            text = "Carbs: ${totalCarb.roundToInt()} g",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // Optional: Add "Add to Log" button here if needed
                // Spacer(modifier = Modifier.height(32.dp))
                // Button(...)
            }

            // Back button is positioned in the Box outside the scrollable column
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 24.dp, start = 8.dp) // Positioning
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Item '$foodItemName' not found", color = MaterialTheme.colorScheme.error, fontSize = 20.sp)
        }
    }
}



data class FoodItem(
    val name: String,
    val calories: Int, // per 1000 units
    val type: String,
    val proteinPerKgL: Double,
    val fatPerKgL: Double,
    val carbPerKgL: Double
)

data class NutritionalValues(
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carb: Double
)

fun readAndParseItemsFromAssets(context: Context): List<FoodItem> {
    val foodItems = mutableListOf<FoodItem>()
    try {
        val inputStream = context.assets.open("items.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 6) {
                val name = parts[0].split(":")[1].trim()
                val calories = parts[1].split(":")[1].trim().toIntOrNull() ?: 0
                val type = parts[2].split(":")[1].trim()
                val protein = parts[3].split(":")[1].trim().toDoubleOrNull() ?: 0.0
                val fat = parts[4].split(":")[1].trim().toDoubleOrNull() ?: 0.0
                val carb = parts[5].split(":")[1].trim().toDoubleOrNull() ?: 0.0
                foodItems.add(FoodItem(name, calories, type, protein, fat, carb))
            }
        }
        reader.close()
    } catch (e: Exception) { e.printStackTrace() }
    return foodItems
}

fun calculateNutritionalValues(item: FoodItem, quantity: Int, isKg: Boolean, isLiters: Boolean): NutritionalValues {
     val quantityInBaseUnits = when {
         item.type == "Food" -> if (isKg) quantity * 1000.0 else quantity.toDouble()
         item.type == "Drink" -> if (isLiters) quantity * 1000.0 else quantity.toDouble()
         else -> quantity.toDouble()
     }
     val scaleFactor = quantityInBaseUnits / 1000.0
     val totalCalories = (item.calories * scaleFactor).roundToInt()
     val totalProtein = item.proteinPerKgL * scaleFactor
     val totalFat = item.fatPerKgL * scaleFactor
     val totalCarb = item.carbPerKgL * scaleFactor
     return NutritionalValues(totalCalories, totalProtein, totalFat, totalCarb)
}

@Composable
fun getImageResource(foodName: String): Int {
    val imageName = foodName.lowercase(Locale.getDefault()).replace(" ", "_")
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
    return if (resId != 0) resId else R.drawable.ic_launcher_background
}
