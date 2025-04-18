@file:OptIn(ExperimentalMaterial3Api::class)

package com.ugraks.project1

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.io.InputStreamReader
import java.util.Locale

// Data classes
data class FoodItemKeepNote(
    val name: String,
    val calories: Int, // Calories per 1000 units (gram or ml) - based on items.txt assumption
    val type: String // "Food" or "Drink"
)

data class CalorieRecord(
    val foodItem: FoodItemKeepNote, // Stores the item details (name, original calories per 1000, type)
    val quantity: Double,
    val unit: String, // "g", "kg", "ml", "L"
    val time: String,
    val calories: Int // Total calories for this specific record (calculated or manually entered)
)

// Function to read food items from assets
fun readFoodItemsFromAssets(context: Context): List<FoodItemKeepNote> {
    val items = mutableListOf<FoodItemKeepNote>()
    try {
        val inputStream = context.assets.open("items.txt")
        InputStreamReader(inputStream).forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 3) {
                val itemName = parts[0].split(":")[1].trim()
                val calories = parts[1].split(":")[1].trim().toIntOrNull() ?: 0
                val itemType = parts[2].split(":")[1].trim()
                // Assuming calories in items.txt are per 1 kg or 1 Liter
                items.add(FoodItemKeepNote(itemName, calories, itemType))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return items
}


@Composable
fun KeepNotePage(navController: NavHostController) {
    val context = LocalContext.current

    // State management
    val allFoodItems = remember { mutableStateListOf<FoodItemKeepNote>() }
    val calorieRecords = remember { mutableStateListOf<CalorieRecord>() }

    var isManualEntryMode by remember { mutableStateOf(false) }

    // Search and Normal Input States
    var searchText by remember { mutableStateOf("") }
    var selectedFoodItem by remember { mutableStateOf<FoodItemKeepNote?>(null) }

    // Common Input States (Used for both normal and manual entry)
    var quantity by remember { mutableStateOf("") }
    var isKilogram by remember { mutableStateOf(false) }
    var isLiter by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf("") }

    // Manual Entry States
    var manualFoodName by remember { mutableStateOf("") }
    var manualSelectedType by remember { mutableStateOf("Food") } // Default Food
    var manualCaloriesInput by remember { mutableStateOf("") }

    // State for Dialogs
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var selectedRecordToDelete by remember { mutableStateOf<CalorieRecord?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // State for Total Calories - Explicitly managed
    var totalCalories by remember { mutableStateOf(0) }


    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val backgroundColor = colorScheme.background
    val surfaceColor = colorScheme.surface
    val surfaceVariantColor = colorScheme.surfaceVariant
    val contentColor = colorScheme.onSurface
    val onPrimaryColor = colorScheme.onPrimary


    // Read assets file on initial composition
    LaunchedEffect(Unit) {
        allFoodItems.addAll(readFoodItemsFromAssets(context))
    }

    // Filter food items based on search text
    val filteredFoodList = remember(allFoodItems, searchText) {
        if (searchText.isEmpty()) {
            emptyList()
        } else {
            allFoodItems.filter { it.name.toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault())) }
        }
    }

    // Effect to update totalCalories whenever calorieRecords changes
    LaunchedEffect(calorieRecords.size) { // Trigger when list size changes
        totalCalories = calorieRecords.sumOf { it.calories }
        // We could also trigger on the list content changing, but size change is a simple proxy
        // for add/remove. A more robust way might be observing the list itself,
        // but sumOf on mutableStateListOf within remember should generally work.
        // Explicitly setting the state here guarantees the update.
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top Bar: Back and Clear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (isManualEntryMode) {
                        // Cancel Manual Entry Mode
                        isManualEntryMode = false
                        // Clear Manual Fields
                        manualFoodName = ""
                        manualCaloriesInput = ""
                        manualSelectedType = "Food"
                        // Clear Common Fields
                        quantity = ""
                        time = ""
                        isKilogram = false
                        isLiter = false
                    } else {
                        // Navigate Back
                        navController.popBackStack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (isManualEntryMode) "Cancel Manual Entry" else "Go Back",
                        tint = primaryColor
                    )
                }
                Text(
                    text = "Calorie Tracker",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { showClearConfirmationDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear All Records",
                        tint = primaryColor
                    )
                }
            }

            // Normal Entry UI (Hidden in Manual Entry Mode)
            if (!isManualEntryMode) {
                // Search Bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search Food", color = primaryColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = primaryColor
                    ),
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear Search", tint = primaryColor)
                            }
                        }
                    }
                )

                // Search Results or Selected Item Display
                if (searchText.isNotEmpty() && selectedFoodItem == null) {
                    if (filteredFoodList.isEmpty()) {
                        // No search results found, offer manual entry
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "'${searchText}' not found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                isManualEntryMode = true
                                // Clear Search Fields
                                searchText = ""
                                selectedFoodItem = null
                                // Clear Common Fields
                                quantity = ""
                                time = ""
                                isKilogram = false
                                isLiter = false
                                // Clear Manual Fields
                                manualFoodName = ""
                                manualCaloriesInput = ""
                                manualSelectedType = "Food" // Default value
                            }) {
                                Text("Add Manually", color = onPrimaryColor)
                            }
                        }
                    } else {
                        // Display Search Results
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (filteredFoodList.isEmpty()) 0.dp else 150.dp) // Take space only if not empty
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        ) {
                            itemsIndexed(filteredFoodList) { index, item ->
                                Text(
                                    text = item.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedFoodItem = item
                                            searchText = item.name
                                            quantity = "" // Reset quantity on item selection
                                            time = "" // Reset time on item selection
                                            isKilogram = false // Reset units on item selection
                                            isLiter = false
                                        }
                                        .padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor
                                )
                                if (index < filteredFoodList.lastIndex) {
                                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                } else if (selectedFoodItem != null) {
                    // Display Selected Item Name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Selected: ${selectedFoodItem!!.name}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = primaryColor
                        )
                        IconButton(onClick = {
                            selectedFoodItem = null
                            searchText = ""
                            quantity = ""
                            time = ""
                            isKilogram = false
                            isLiter = false
                        }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear Selection", tint = primaryColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp)) // Space after Search/Selection

                // Quantity Input (Normal Mode)
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        // Allow only numbers and decimal point
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            quantity = it
                        }
                    },
                    label = { Text("Quantity", color = primaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = primaryColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = selectedFoodItem != null // Enabled only if an item is selected
                )

                // Unit Selection (Normal Mode - Shown based on selected item type)
                if (selectedFoodItem?.type == "Food") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Unit:", color = primaryColor, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isKilogram,
                                onCheckedChange = { isKilogram = it; if (it) isLiter = false },
                                enabled = selectedFoodItem != null
                            )
                            Text(text = "Kilogram (kg)", color = contentColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = !isKilogram,
                                onCheckedChange = { isKilogram = !it; if (!it) isLiter = false },
                                enabled = selectedFoodItem != null
                            )
                            Text(text = "Gram (g)", color = contentColor)
                        }
                    }
                } else if (selectedFoodItem?.type == "Drink") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Unit:", color = primaryColor, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isLiter,
                                onCheckedChange = { isLiter = it; if (it) isKilogram = false },
                                enabled = selectedFoodItem != null
                            )
                            Text(text = "Litre (L)", color = contentColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = !isLiter,
                                onCheckedChange = { isLiter = !it; if (!it) isKilogram = false },
                                enabled = selectedFoodItem != null
                            )
                            Text(text = "Milliliter (ml)", color = contentColor)
                        }
                    }
                }

                // Time Input (Normal Mode)
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g., 12:30)", color = primaryColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = primaryColor
                    ),
                    enabled = selectedFoodItem != null // Enabled only if an item is selected
                )

            } else { // Manual Entry UI
                Text(
                    text = "Manual Entry",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                )

                // Manual Food/Drink Name Input
                OutlinedTextField(
                    value = manualFoodName,
                    onValueChange = { manualFoodName = it },
                    label = { Text("Food/Drink Name", color = primaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Manual Type Selection (Food/Drink)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "Type:", color = primaryColor, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = manualSelectedType == "Food",
                            onClick = { manualSelectedType = "Food" }
                        )
                        Text("Food", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = manualSelectedType == "Drink",
                            onClick = { manualSelectedType = "Drink" }
                        )
                        Text("Drink", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity Input (Manual Mode - Common field)
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            quantity = it
                        }
                    },
                    label = { Text("Quantity", color = primaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = primaryColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Unit Selection (Manual Mode - Shown based on manual selected type)
                if (manualSelectedType == "Food") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Unit:", color = primaryColor, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isKilogram,
                                onCheckedChange = { isKilogram = it; if (it) isLiter = false }
                            )
                            Text(text = "Kilogram (kg)", color = contentColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = !isKilogram,
                                onCheckedChange = { isKilogram = !it; if (!it) isLiter = false }
                            )
                            Text(text = "Gram (g)", color = contentColor)
                        }
                    }
                } else { // Drink
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Unit:", color = primaryColor, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isLiter,
                                onCheckedChange = { isLiter = it; if (it) isKilogram = false }
                            )
                            Text(text = "Litre (L)", color = contentColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = !isLiter,
                                onCheckedChange = { isLiter = !it; if (!it) isKilogram = false }
                            )
                            Text(text = "Milliliter (ml)", color = contentColor)
                        }
                    }
                }

                // Time Input (Manual Mode - Common field)
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g., 12:30)", color = primaryColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Manual Total Calories Input
                OutlinedTextField(
                    value = manualCaloriesInput,
                    onValueChange = {
                        // Allow only digits
                        if (it.all { char -> char.isDigit() }) {
                            manualCaloriesInput = it
                        }
                    },
                    label = { Text("Total Calories (kcal)", color = primaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = primaryColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(20.dp)) // Space before Button
            }


            // Add to List Button
            Button(
                onClick = {
                    val quantityValue = quantity.toDoubleOrNull() ?: 0.0

                    if (isManualEntryMode) {
                        // Manual Entry Mode
                        val manualCalories = manualCaloriesInput.toIntOrNull() ?: 0
                        if (manualFoodName.isNotEmpty() && quantityValue > 0 && time.isNotEmpty() && manualCalories > 0) {
                            val unit = if (manualSelectedType == "Food") {
                                if (isKilogram) "kg" else "g"
                            } else {
                                if (isLiter) "L" else "ml"
                            }
                            calorieRecords.add(
                                CalorieRecord(
                                    // In manual entry, FoodItem just holds name and type, calories are manually entered
                                    foodItem = FoodItemKeepNote(manualFoodName, 0, manualSelectedType),
                                    quantity = quantityValue,
                                    unit = unit,
                                    time = time,
                                    calories = manualCalories // Manually entered total calories are directly saved
                                )
                            )
                            // Exit manual entry mode and clear fields
                            isManualEntryMode = false
                            manualFoodName = ""
                            manualCaloriesInput = ""
                            manualSelectedType = "Food" // Reset to default
                            quantity = ""
                            time = ""
                            isKilogram = false
                            isLiter = false

                        }
                    } else {
                        // Normal Entry Mode (from assets file)
                        if (selectedFoodItem != null && quantity.isNotEmpty() && time.isNotEmpty() && quantityValue > 0) {
                            // Assuming calories in items.txt are per 1 kg or 1 Liter
                            val caloriesPerUnit = selectedFoodItem!!.calories.toDouble()

                            val calculatedCalories = if (selectedFoodItem!!.type == "Food") {
                                if (isKilogram) { // Entered in Kilograms
                                    (caloriesPerUnit * quantityValue).toInt()
                                } else { // Entered in Grams
                                    // Since items.txt is per kg, divide by 1000 for grams
                                    ((caloriesPerUnit / 1000) * quantityValue).toInt()
                                }
                            } else { // Drink
                                if (isLiter) { // Entered in Liters
                                    (caloriesPerUnit * quantityValue).toInt()
                                } else { // Entered in Milliliters
                                    // Since items.txt is per Liter, divide by 1000 for milliliters
                                    ((caloriesPerUnit / 1000) * quantityValue).toInt()
                                }
                            }

                            val unit = if (selectedFoodItem!!.type == "Food") {
                                if (isKilogram) "kg" else "g"
                            } else {
                                if (isLiter) "L" else "ml"
                            }

                            calorieRecords.add(
                                CalorieRecord(
                                    foodItem = selectedFoodItem!!, // Original FoodItem is saved here
                                    quantity = quantityValue,
                                    unit = unit,
                                    time = time,
                                    calories = calculatedCalories // Calculated calories are saved
                                )
                            )
                            // Clear fields
                            selectedFoodItem = null
                            searchText = ""
                            quantity = ""
                            time = ""
                            isKilogram = false
                            isLiter = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = onPrimaryColor
                ),
                // Button is enabled if required fields are filled and quantity/calories are valid positive numbers
                enabled = if (isManualEntryMode) {
                    // Manual mode: name, quantity(>0), time, manual calories(>0) must be filled
                    manualFoodName.isNotEmpty() && quantity.isNotEmpty() && quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0 &&
                            time.isNotEmpty() && manualCaloriesInput.isNotEmpty() && manualCaloriesInput.toIntOrNull() != null && manualCaloriesInput.toIntOrNull()!! > 0
                } else {
                    // Normal mode: item selected, quantity(>0), and time must be filled
                    selectedFoodItem != null && quantity.isNotEmpty() && quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0 && time.isNotEmpty()
                }
            ) {
                Text("Add to List", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Calorie Records Header
            Text(
                text = "My Records",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
            )

            // Calorie Records List
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(calorieRecords) { index, record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceVariantColor,
                            contentColor = contentColor
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = record.foodItem.name, // Use name from FoodItem
                                    style = MaterialTheme.typography.titleMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${record.quantity} ${record.unit} - ${record.time}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor
                                )
                            }
                            Text(
                                text = "${record.calories} kcal", // Display total calories for this record
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = primaryColor
                            )
                            IconButton(
                                onClick = {
                                    selectedRecordToDelete = record
                                    showDeleteConfirmationDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete Record",
                                    tint = primaryColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space before Total Calories

            // *** TOTAL CALORIES DISPLAY ***
            Card( // Display Total Calories in a Card
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = primaryColor, // Primary color background
                    contentColor = onPrimaryColor // OnPrimary color text
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Calories:",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "$totalCalories kcal", // Display the calculated total calories
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            // ****************************


        } // End of main Column

        // Dialogs
        if (showClearConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmationDialog = false },
                title = { Text("Clear All Records", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to clear all calorie records?") },
                confirmButton = {
                    TextButton(onClick = {
                        calorieRecords.clear() // Clear the list
                        showClearConfirmationDialog = false
                    }) {
                        Text("Yes", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmationDialog = false }) {
                        Text("Cancel", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                }
            )
        }

        if (showDeleteConfirmationDialog && selectedRecordToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmationDialog = false
                    selectedRecordToDelete = null
                },
                title = { Text("Delete Record", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to delete this record?") },
                confirmButton = {
                    TextButton(onClick = {
                        calorieRecords.remove(selectedRecordToDelete!!) // Remove the record from the list
                        showDeleteConfirmationDialog = false
                        selectedRecordToDelete = null
                    }) {
                        Text("Yes", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirmationDialog = false
                        selectedRecordToDelete = null
                    }) {
                        Text("Cancel", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                }
            )
        }
    } // End of Box
}