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
import androidx.compose.foundation.lazy.LazyColumn // Dıştaki LazyColumn için import
import androidx.compose.foundation.lazy.itemsIndexed // Kayıt listesi için
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
import kotlin.math.roundToInt

// Data classes (unchanged)
data class FoodItemKeepNote(
    val name: String,
    val calories: Int, // Calories per 1000 units (kg or ml)
    val type: String, // "Food" or "Drink"
    val proteinPerKgL: Double, // Grams of protein per 1000 units (kg or L)
    val fatPerKgL: Double,     // Grams of fat per 1000 units (kg or L)
    val carbPerKgL: Double     // Grams of carbs per 1000 units (kg or L)
)

data class CalorieRecord(
    val foodItem: FoodItemKeepNote, // Stores the item details (name, original calories per 1000, type, P/F/C per 1000)
    val quantity: Double,
    val unit: String, // "g", "kg", "ml", "L"
    val time: String,
    val calories: Int, // Total calories for this specific record
    val protein: Double, // Total protein for this specific record
    val fat: Double,     // Total fat for this specific record
    val carb: Double     // Total carb for this specific record
)

// Function to read food items from assets (unchanged)
fun readFoodItemsFromAssets(context: Context): List<FoodItemKeepNote> {
    val items = mutableListOf<FoodItemKeepNote>()
    try {
        val inputStream = context.assets.open("items.txt")
        InputStreamReader(inputStream).forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 6) { // Expect 6 parts now (item,calories,type,protein_g,fat_g,carb_g)
                val itemName = parts[0].split(":")[1].trim()
                val calories = parts[1].split(":")[1].trim().toIntOrNull() ?: 0
                val itemType = parts[2].split(":")[1].trim()
                val protein = parts[3].split(":")[1].trim().toDoubleOrNull() ?: 0.0 // Read protein
                val fat = parts[4].split(":")[1].trim().toDoubleOrNull() ?: 0.0     // Read fat
                val carb = parts[5].split(":")[1].trim().toDoubleOrNull() ?: 0.0     // Read carbs

                // Assuming calories, protein_g, fat_g, carb_g in items.txt are per 1 kg or 1 Liter (1000 units)
                items.add(FoodItemKeepNote(itemName, calories, itemType, protein, fat, carb))
            } else {
                // Log an error or warning for malformed lines
                println("Skipping malformed line in items.txt: $line")
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

    // State management (unchanged)
    val allFoodItems = remember { mutableStateListOf<FoodItemKeepNote>() }
    val calorieRecords = remember { mutableStateListOf<CalorieRecord>() }

    var isManualEntryMode by remember { mutableStateOf(false) }

    // Search and Normal Input States (unchanged)
    var searchText by remember { mutableStateOf("") }
    var selectedFoodItem by remember { mutableStateOf<FoodItemKeepNote?>(null) }

    // Common Input States (Used for both normal and manual entry - unchanged)
    var quantity by remember { mutableStateOf("") }
    var isKilogram by remember { mutableStateOf(false) }
    var isLiter by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf("") }

    // Manual Entry States (unchanged)
    var manualFoodName by remember { mutableStateOf("") }
    var manualSelectedType by remember { mutableStateOf("Food") } // Default Food
    var selectedManualUnit by remember { mutableStateOf("g") } // Default to grams initially for manual mode
    var manualCaloriesInput by remember { mutableStateOf("") }
    var manualProteinInput by remember { mutableStateOf("") }
    var manualFatInput by remember { mutableStateOf("") }
    var manualCarbInput by remember { mutableStateOf("") }


    // State for Dialogs (unchanged)
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var selectedRecordToDelete by remember { mutableStateOf<CalorieRecord?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // State for Total Calories - Explicitly managed (unchanged)
    var totalCalories by remember { mutableStateOf(0) }
    // State for Total Macros (unchanged)
    var totalProtein by remember { mutableStateOf(0.0) }
    var totalFat by remember { mutableStateOf(0.0) }
    var totalCarb by remember { mutableStateOf(0.0) }


    // Color scheme (unchanged)
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val backgroundColor = colorScheme.background
    val surfaceColor = colorScheme.surface
    val surfaceVariantColor = colorScheme.surfaceVariant
    val contentColor = colorScheme.onSurface
    val onPrimaryColor = colorScheme.onPrimary


    // Read assets file on initial composition (unchanged)
    LaunchedEffect(Unit) {
        allFoodItems.addAll(readFoodItemsFromAssets(context))
    }

    // Filter food items based on search text (unchanged)
    val filteredFoodList = remember(allFoodItems, searchText) {
        if (searchText.isEmpty()) {
            emptyList()
        } else {
            allFoodItems.filter { it.name.toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault())) }
        }
    }

    // Effect to update totals whenever calorieRecords changes (unchanged)
    LaunchedEffect(calorieRecords.size) {
        totalCalories = calorieRecords.sumOf { it.calories }
        totalProtein = calorieRecords.sumOf { it.protein }
        totalFat = calorieRecords.sumOf { it.fat }
        totalCarb = calorieRecords.sumOf { it.carb }
    }

    // Effect to reset manual unit when manual type changes (unchanged)
    LaunchedEffect(manualSelectedType) {
        selectedManualUnit = if (manualSelectedType == "Food") "g" else "ml"
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Replace main Column with LazyColumn for overall scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                // Apply padding directly to LazyColumn
                .padding(horizontal = 20.dp, vertical = 24.dp),
            // Remove Arrangement.SpaceBetween from the outer column, it's not needed in LazyColumn
            // Remove verticalAlignment from outer column
        ) {
            // Item for Top Bar: Back and Clear
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp), // Keep bottom padding for separation
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
                            manualProteinInput = ""
                            manualFatInput = ""
                            manualCarbInput = ""
                            manualSelectedType = "Food"
                            selectedManualUnit = "g"
                            // Clear Common Fields
                            quantity = ""
                            time = ""
                            isKilogram = false
                            isLiter = false
                            // Clear Search/Selection Fields
                            searchText = ""
                            selectedFoodItem = null
                        } else {
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
            }


            // Items for Normal Entry UI (Hidden in Manual Entry Mode)
            if (!isManualEntryMode) {
                // --- Normal Entry UI ---
                // Create a local copy of selectedFoodItem for safe access within this UI block
                val currentSelectedItem = selectedFoodItem

                // Item for Search Bar
                item {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            selectedFoodItem = null // Clear selection state
                            quantity = ""
                            time = ""
                            isKilogram = false
                            isLiter = false
                        },
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
                                IconButton(onClick = {
                                    searchText = ""
                                    selectedFoodItem = null
                                    quantity = ""
                                    time = ""
                                    isKilogram = false
                                    isLiter = false
                                }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear Search", tint = primaryColor)
                                }
                            }
                        }
                    )
                }

                // Item(s) for Search Results or Offer Manual Entry
                if (searchText.isNotEmpty() && selectedFoodItem == null) { // selectedFoodItem != null check is correct here
                    if (filteredFoodList.isEmpty()) {
                        // No search results found, offer manual entry (as a single item block)
                        item {
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
                                Button(
                                    onClick = {
                                        isManualEntryMode = true
                                        searchText = ""
                                        selectedFoodItem = null
                                        quantity = ""
                                        time = ""
                                        isKilogram = false
                                        isLiter = false
                                        manualFoodName = ""
                                        manualCaloriesInput = ""
                                        manualProteinInput = ""
                                        manualFatInput = ""
                                        manualCarbInput = ""
                                        manualSelectedType = "Food"
                                        selectedManualUnit = "g"
                                    }) {
                                    Text("Add Manually", color = onPrimaryColor)
                                }
                            }
                        }
                    } else {
                        // Display Search Results (as separate items within the LazyColumn)
                        itemsIndexed(filteredFoodList) { index, item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(if (index == 0) RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp) else if (index == filteredFoodList.lastIndex) RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp) else RoundedCornerShape(0.dp))
                                    .background(colorScheme.surface)
                                    .border(
                                        1.dp,
                                        Color.LightGray,
                                        if (index == 0) RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp) else if (index == filteredFoodList.lastIndex) RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp) else RoundedCornerShape(0.dp)
                                    )
                                    .clickable {
                                        selectedFoodItem = item // Setting the state here
                                        searchText = item.name
                                        quantity = ""
                                        time = ""
                                        if (item.type == "Food") { isKilogram = false; isLiter = false } else { isKilogram = false; isLiter = true }
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor
                                )
                            }
                            if (index < filteredFoodList.lastIndex) {
                                Spacer(modifier = Modifier.height(0.5.dp))
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                } else {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }


                // Item for Quantity Input
                item {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            val newValue = it.filter { char -> char.isDigit() || char == '.' }
                            if (newValue.count { it == '.' } <= 1) { quantity = newValue }
                        },
                        label = { Text("Quantity", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface, unfocusedContainerColor = colorScheme.surface,
                            focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Gray, cursorColor = primaryColor
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = currentSelectedItem != null // Use local copy for enabled
                    )
                }

                // Item for Unit Selection (Normal Mode - Original Checkboxes)
                if (currentSelectedItem?.type == "Food" || currentSelectedItem?.type == "Drink") { // Use local copy for type check
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "Unit:", color = primaryColor, fontWeight = FontWeight.Medium)
                            if (currentSelectedItem.type == "Food") { // Use local copy
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox( checked = isKilogram, onCheckedChange = { isKilogram = it; if (it) isLiter = false }, enabled = currentSelectedItem != null ) // Use local copy
                                    Text(text = "Kilogram (kg)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox( checked = !isKilogram, onCheckedChange = { isKilogram = !it; if (!it) isLiter = false }, enabled = currentSelectedItem != null ) // Use local copy
                                    Text(text = "Gram (g)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                            } else if (currentSelectedItem.type == "Drink") { // Use local copy
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox( checked = isLiter, onCheckedChange = { isLiter = it; if (it) isKilogram = false }, enabled = currentSelectedItem != null ) // Use local copy
                                    Text(text = "Litre (L)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox( checked = !isLiter, onCheckedChange = { isLiter = !it; if (!it) isKilogram = false }, enabled = currentSelectedItem != null ) // Use local copy
                                    Text(text = "Milliliter (ml)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                            }
                        }
                    }
                }


                // Item for Time Input
                item {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (e.g., 12:30)", color = primaryColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface, unfocusedContainerColor = colorScheme.surface,
                            focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Gray, cursorColor = primaryColor
                        ),
                        enabled = currentSelectedItem != null // Use local copy for enabled
                    )
                }

                // Item for Spacer before Button
                item { Spacer(modifier = Modifier.height(20.dp)) }


            } else { // --- Manual Entry UI --- (Items within LazyColumn)

                item {
                    Text(
                        text = "Manual Entry",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontStyle = FontStyle.Italic, color = primaryColor, fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Item for Manual Food/Drink Name Input
                item {
                    OutlinedTextField(
                        value = manualFoodName,
                        onValueChange = { manualFoodName = it },
                        label = { Text("Food/Drink Name", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors( focusedContainerColor = colorScheme.surface, unfocusedContainerColor = colorScheme.surface, focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Gray, cursorColor = primaryColor )
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Item for Manual Type Selection (Food/Drink) - Radio Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(modifier = Modifier.selectable( selected = manualSelectedType == "Food", onClick = { manualSelectedType = "Food" }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton( selected = manualSelectedType == "Food", onClick = { manualSelectedType = "Food" } )
                            Text("Food", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                        }
                        Row(modifier = Modifier.selectable( selected = manualSelectedType == "Drink", onClick = { manualSelectedType = "Drink" }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton( selected = manualSelectedType == "Drink", onClick = { manualSelectedType = "Drink" } )
                            Text("Drink", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Item for Quantity Input (Manual Mode)
                item {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            val newValue = it.filter { char -> char.isDigit() || char == '.' }
                            if (newValue.count { it == '.' } <= 1) { quantity = newValue }
                        },
                        label = { Text("Quantity", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors( focusedContainerColor = colorScheme.surface, unfocusedContainerColor = colorScheme.surface, focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Gray, cursorColor = primaryColor ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Item for Unit Selection (Manual Mode) - Radio Buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (manualSelectedType == "Food") {
                            Row( modifier = Modifier.selectable( selected = selectedManualUnit == "g", onClick = { selectedManualUnit = "g" }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
                                RadioButton( selected = selectedManualUnit == "g", onClick = { selectedManualUnit = "g" } )
                                Text("g", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                            }
                            Row( modifier = Modifier.selectable( selected = selectedManualUnit == "kg", onClick = { selectedManualUnit = "kg" }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
                                RadioButton( selected = selectedManualUnit == "kg", onClick = { selectedManualUnit = "kg" } )
                                Text("kg", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                            }
                        } else { // Drink
                            Row( modifier = Modifier.selectable( selected = selectedManualUnit == "ml", onClick = { selectedManualUnit = "ml" }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
                                RadioButton( selected = selectedManualUnit == "ml", onClick = { selectedManualUnit = "ml" } )
                                Text("ml", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                            }
                            Row( modifier = Modifier.selectable( selected = selectedManualUnit == "L", onClick = { selectedManualUnit = "L" }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
                                RadioButton( selected = selectedManualUnit == "L", onClick = { selectedManualUnit = "L" } )
                                Text("L", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                            }
                        }
                    }
                }

                // Item for Time Input (Manual Mode)
                item {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (e.g., 12:30)", color = primaryColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors( focusedContainerColor = colorScheme.surface, unfocusedContainerColor = colorScheme.surface, focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Gray, cursorColor = primaryColor )
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Item for Manual Total Calories Input
                item {
                    OutlinedTextField(
                        value = manualCaloriesInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) { manualCaloriesInput = it } },
                        label = { Text("Total Calories (kcal)", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors( focusedContainerColor = colorScheme.surface, unfocusedContainerColor = colorScheme.surface, focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Gray, cursorColor = primaryColor ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) } // Space before Manual P/F/C

                // Items for MANUAL P/F/C INPUTS
                item {
                    OutlinedTextField(
                        value = manualProteinInput,
                        onValueChange = {
                            val newValue = it.filter { char -> char.isDigit() || char == '.' }
                            if (newValue.count { it == '.' } <= 1) { manualProteinInput = newValue }
                        },
                        label = { Text("Total Protein (g)", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors( focusedContainerColor = colorScheme.surface, unfocusedContainerColor = colorScheme.surface, focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Gray, cursorColor = primaryColor ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item {
                    OutlinedTextField(
                        value = manualFatInput,
                        onValueChange = {
                            val newValue = it.filter { char -> char.isDigit() || char == '.' }
                            if (newValue.count { it == '.' } <= 1) { manualFatInput = newValue }
                        },
                        label = { Text("Total Fat (g)", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors( focusedContainerColor = colorScheme.surface, unfocusedContainerColor = colorScheme.surface, focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Gray, cursorColor = primaryColor ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item {
                    OutlinedTextField(
                        value = manualCarbInput,
                        onValueChange = {
                            val newValue = it.filter { char -> char.isDigit() || char == '.' }
                            if (newValue.count { it == '.' } <= 1) { manualCarbInput = newValue }
                        },
                        label = { Text("Total Carb (g)", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors( focusedContainerColor = colorScheme.surface, unfocusedContainerColor = colorScheme.surface, focusedIndicatorColor = primaryColor, unfocusedIndicatorColor = Color.Gray, cursorColor = primaryColor ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }


                item { Spacer(modifier = Modifier.height(20.dp)) }
            }


            // Item for Add to List Button
            item {
                Button(
                    onClick = {
                        val quantityValue = quantity.toDoubleOrNull() ?: 0.0
                        val item = selectedFoodItem // Local copy for onClick

                        // Determine the scaling factor based on the selected unit (kg/L vs g/ml)
                        val unitScale = when {
                            isManualEntryMode -> when(selectedManualUnit) {
                                "kg", "L" -> quantityValue
                                "g", "ml" -> quantityValue / 1000.0
                                else -> 0.0
                            }
                            !isManualEntryMode && item != null -> when(
                                if (item.type == "Food") { if (isKilogram) "kg" else "g" } else { if (isLiter) "L" else "ml" }
                            ) {
                                "kg", "L" -> quantityValue
                                "g", "ml" -> quantityValue / 1000.0
                                else -> 0.0
                            }
                            else -> 0.0
                        }


                        if (isManualEntryMode) {
                            val manualCalories = manualCaloriesInput.toIntOrNull() ?: 0
                            val manualProtein = manualProteinInput.toDoubleOrNull() ?: 0.0
                            val manualFat = manualFatInput.toDoubleOrNull() ?: 0.0
                            val manualCarb = manualCarbInput.toDoubleOrNull() ?: 0.0

                            // Manual validation including macros
                            if (manualFoodName.isNotEmpty() && quantityValue > 0 && time.isNotEmpty() && manualCaloriesInput.isNotEmpty() && manualProteinInput.isNotEmpty() && manualFatInput.isNotEmpty() && manualCarbInput.isNotEmpty()) {
                                // Further check parsed values are non-negative
                                if (manualCalories >= 0 && manualProtein >= 0.0 && manualFat >= 0.0 && manualCarb >= 0.0) {
                                    val unit = selectedManualUnit
                                    calorieRecords.add(
                                        CalorieRecord(
                                            foodItem = FoodItemKeepNote(manualFoodName, 0, manualSelectedType, 0.0, 0.0, 0.0),
                                            quantity = quantityValue,
                                            unit = unit,
                                            time = time,
                                            calories = manualCalories,
                                            protein = manualProtein,
                                            fat = manualFat,
                                            carb = manualCarb
                                        )
                                    )
                                    // Clear fields after adding
                                    isManualEntryMode = false
                                    manualFoodName = ""
                                    manualCaloriesInput = ""
                                    manualProteinInput = ""
                                    manualFatInput = ""
                                    manualCarbInput = ""
                                    manualSelectedType = "Food"
                                    selectedManualUnit = "g"
                                    quantity = ""
                                    time = ""
                                    isKilogram = false
                                    isLiter = false
                                    searchText = ""
                                    selectedFoodItem = null
                                } else {
                                    // Handle validation error: negative numbers
                                }
                            } else {
                                // Handle validation error: empty required fields
                            }
                        } else {
                            // Normal Entry Mode
                            if (item != null && quantity.isNotEmpty() && quantityValue > 0 && time.isNotEmpty()) {
                                val caloriesPer1000Units = item.calories.toDouble()
                                val proteinPer1000Units = item.proteinPerKgL
                                val fatPer1000Units = item.fatPerKgL
                                val carbPer1000Units = item.carbPerKgL

                                val calculatedCalories = (caloriesPer1000Units * unitScale).roundToInt()
                                val calculatedProtein = proteinPer1000Units * unitScale
                                val calculatedFat = fatPer1000Units * unitScale
                                val calculatedCarb = carbPer1000Units * unitScale

                                val unit = if (item.type == "Food") { if (isKilogram) "kg" else "g" } else { if (isLiter) "L" else "ml" }

                                calorieRecords.add(
                                    CalorieRecord( foodItem = item, quantity = quantityValue, unit = unit, time = time, calories = calculatedCalories, protein = calculatedProtein, fat = calculatedFat, carb = calculatedCarb )
                                )
                                // Clear fields after adding
                                selectedFoodItem = null
                                searchText = ""
                                quantity = ""
                                time = ""
                                isKilogram = false
                                isLiter = false
                            } else {
                                // Handle validation error
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors( containerColor = primaryColor, contentColor = onPrimaryColor ),
                    enabled = if (isManualEntryMode) {
                        manualFoodName.isNotEmpty() && quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0 && time.isNotEmpty() &&
                                manualCaloriesInput.toIntOrNull() != null && manualCaloriesInput.toIntOrNull()!! >= 0 && // Allow 0 calories
                                manualProteinInput.toDoubleOrNull() != null && manualProteinInput.toDoubleOrNull()!! >= 0.0 && // Allow 0g macros
                                manualFatInput.toDoubleOrNull() != null && manualFatInput.toDoubleOrNull()!! >= 0.0 &&
                                manualCarbInput.toDoubleOrNull() != null && manualCarbInput.toDoubleOrNull()!! >= 0.0 &&
                                // Check if input strings are non-empty (required fields)
                                manualCaloriesInput.isNotEmpty() && manualProteinInput.isNotEmpty() && manualFatInput.isNotEmpty() && manualCarbInput.isNotEmpty()
                    } else {
                        selectedFoodItem != null && quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0 && time.isNotEmpty()
                    }
                ) {
                    Text("Add to List", fontSize = 18.sp)
                }
            }


            // Item for Calorie Records Header
            item {
                Text(
                    text = "My Records",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = primaryColor, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, top = 20.dp),
                    textAlign = TextAlign.Center
                )
            }


            // Items for Calorie Records List (Use itemsIndexed)
            itemsIndexed(calorieRecords) { index, record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors( containerColor = surfaceVariantColor, contentColor = contentColor )
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
                                text = record.foodItem.name,
                                style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic, color = primaryColor, fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${record.quantity} ${record.unit} - ${record.time}",
                                style = MaterialTheme.typography.bodyMedium, color = contentColor
                            )
                            // Display Protein, Fat, Carb below Quantity/Time if non-zero
                            if (record.protein > 0.0 || record.fat > 0.0 || record.carb > 0.0) {
                                Text(
                                    text = "P: ${record.protein.roundToInt()}g, F: ${record.fat.roundToInt()}g, C: ${record.carb.roundToInt()}g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${record.calories} kcal",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = primaryColor
                            )
                            IconButton( onClick = { selectedRecordToDelete = record; showDeleteConfirmationDialog = true } ) {
                                Icon( imageVector = Icons.Outlined.Delete, contentDescription = "Delete Record", tint = primaryColor )
                            }
                        }
                    }
                }
            }

            // Item for TOTAL CALORIES AND MACROS DISPLAY
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors( containerColor = surfaceVariantColor, contentColor = primaryColor )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Totals:",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Calories: $totalCalories kcal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        // Only display macro totals if they are non-zero
                        if (totalProtein > 0.0 || totalFat > 0.0 || totalCarb > 0.0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text( text = "Protein: ${totalProtein.roundToInt()}g", style = MaterialTheme.typography.bodyMedium )
                            Text( text = "Fat: ${totalFat.roundToInt()}g", style = MaterialTheme.typography.bodyMedium )
                            Text( text = "Carbs: ${totalCarb.roundToInt()}g", style = MaterialTheme.typography.bodyMedium )
                        }
                    }
                }
            }


        } // End of LazyColumn

        // Dialogs remain outside the main scrollable content
        if (showClearConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmationDialog = false },
                title = { Text("Clear All Records", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to clear all calorie records?") },
                confirmButton = {
                    TextButton(onClick = { calorieRecords.clear(); showClearConfirmationDialog = false }) {
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
                onDismissRequest = { showDeleteConfirmationDialog = false; selectedRecordToDelete = null },
                title = { Text("Delete Record", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to delete this record?") },
                confirmButton = {
                    TextButton(onClick = { calorieRecords.remove(selectedRecordToDelete!!); showDeleteConfirmationDialog = false; selectedRecordToDelete = null }) {
                        Text("Yes", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmationDialog = false; selectedRecordToDelete = null }) {
                        Text("Cancel", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                }
            )
        }
    } // End of Box
}