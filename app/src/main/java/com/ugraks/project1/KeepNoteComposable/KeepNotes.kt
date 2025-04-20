@file:OptIn(ExperimentalMaterial3Api::class)

package com.ugraks.project1 // Paket adınızı kontrol edin

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.ugraks.project1.AppNavigation.Screens
import com.ugraks.project1.KeepNoteComposable.CalorieRecord
import com.ugraks.project1.KeepNoteComposable.FoodItemKeepNote
import com.ugraks.project1.KeepNoteComposable.loadCalorieRecords
import com.ugraks.project1.KeepNoteComposable.readFoodItemsFromAssets
import com.ugraks.project1.KeepNoteComposable.saveCalorieRecords
import com.ugraks.project1.KeepNoteComposable.saveTodaySummary
import java.io.File
import java.io.InputStreamReader
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun KeepNotePage(navController: NavHostController) {
    val context = LocalContext.current

    // State management (Durum yönetimi)
    val allFoodItems = remember { mutableStateListOf<FoodItemKeepNote>() }
    // calorieRecords başlangıçta boş değil, LaunchedEffect ile dosyadan yüklenecek
    val calorieRecords = remember { mutableStateListOf<CalorieRecord>() }

    var isManualEntryMode by remember { mutableStateOf(false) }

    // Search and Normal Input States (Arama ve Normal Giriş Durumları)
    var searchText by remember { mutableStateOf("") }
    var selectedFoodItem by remember { mutableStateOf<FoodItemKeepNote?>(null) }
    var showSaveSummaryDialog by remember { mutableStateOf(false) }

    // Common Input States (Ortak Giriş Durumları - Hem normal hem manuel için)
    var quantity by remember { mutableStateOf("") }
    var isKilogram by remember { mutableStateOf(false) } // Normal Food
    var isLiter by remember { mutableStateOf(false) } // Normal Drink
    var time by remember { mutableStateOf("") }

    // Manual Entry States (Manuel Giriş Durumları)
    var manualFoodName by remember { mutableStateOf("") }
    var manualSelectedType by remember { mutableStateOf("Food") } // Default Food
    var selectedManualUnit by remember { mutableStateOf("g") } // Default to grams initially for manual mode
    var manualCaloriesInput by remember { mutableStateOf("") } // Total calories for the portion
    var manualProteinInput by remember { mutableStateOf("") }   // Total protein for the portion
    var manualFatInput by remember { mutableStateOf("") }       // Total fat for the portion
    var manualCarbInput by remember { mutableStateOf("") }     // Total carbs for the portion


    // State for Dialogs (Dialog Durumları)
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var selectedRecordToDelete by remember { mutableStateOf<CalorieRecord?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // State for Total Calories/Macros (Toplam Kalori/Makro Durumları) - Explicitly managed
    var totalCalories by remember { mutableStateOf(0) }
    var totalProtein by remember { mutableStateOf(0.0) }
    var totalFat by remember { mutableStateOf(0.0) }
    var totalCarb by remember { mutableStateOf(0.0) }


    // Color scheme (Renk Şeması)
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val backgroundColor = colorScheme.background
    val surfaceColor = colorScheme.surface
    val surfaceVariantColor = colorScheme.surfaceVariant
    val contentColor = colorScheme.onSurface
    val onPrimaryColor = colorScheme.onPrimary


    // LaunchedEffect: Uygulama ilk açıldığında veya bu Composable ilk oluşturulduğunda çalışır
    LaunchedEffect(Unit) {
        // 1. Assets dosyasından tüm yiyecekleri oku
        allFoodItems.addAll(readFoodItemsFromAssets(context))
        // 2. Kayıtları dosyadan yükle ve listeye ekle
        val loaded = loadCalorieRecords(context) // Dosyadan yükleme fonksiyonu çağrısı
        calorieRecords.addAll(loaded)
    }

    // filteredFoodList: Arama metnine göre filtrelenmiş yiyecek listesi (BU TANIM BU COMPSABLE İÇİNDE OLMALIDIR)
    val filteredFoodList = remember(allFoodItems, searchText) {
        if (searchText.isEmpty()) {
            emptyList()
        } else {
            allFoodItems.filter { it.name.toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault())) }
        }
    }


    // Effect to update totals whenever calorieRecords changes (Kayıtlar değişince toplamları güncelle)
    LaunchedEffect(calorieRecords.size) {
        totalCalories = calorieRecords.sumOf { it.calories }
        totalProtein = calorieRecords.sumOf { it.protein }
        totalFat = calorieRecords.sumOf { it.fat }
        totalCarb = calorieRecords.sumOf { it.carb }
    }

    // Effect to reset manual unit when manual type changes (Manuel tip değişince birimi sıfırla)
    LaunchedEffect(manualSelectedType) {
        selectedManualUnit = if (manualSelectedType == "Food") "g" else "ml"
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // LazyColumn: Tüm içeriğin kaydırılabilir olmasını sağlar
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            // Item for Top Bar: Back and Clear (Üst Bar)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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


            // Items for Normal Entry UI (Normal Giriş Alanları - Manuel Modda Gizli)
            if (!isManualEntryMode) {
                // --- Normal Entry UI ---
                val currentSelectedItem = selectedFoodItem

                // Item for Search Bar (Arama Çubuğu)
                item {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            selectedFoodItem = null // Seçimi temizle
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

                // Item(s) for Search Results or Offer Manual Entry (Arama Sonuçları veya Manuel Giriş Teklifi)
                if (searchText.isNotEmpty() && selectedFoodItem == null) {
                    if (filteredFoodList.isEmpty()) {
                        // No search results found, offer manual entry
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
                                        // Alanları temizle
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
                        // Display Search Results
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
                                        selectedFoodItem = item // Seçimi ayarla
                                        searchText = item.name // Arama çubuğunu güncelle
                                        quantity = ""
                                        time = ""
                                        if (item.type == "Food") { isKilogram = false; isLiter = false } else { isKilogram = false; isLiter = true } // Varsayılan birimi ayarla
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


                // Item for Quantity Input (Miktar Girişi)
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
                        enabled = currentSelectedItem != null // Sadece yiyecek seçiliyse aktif
                    )
                }

                // Item for Unit Selection (Birim Seçimi - Normal Mod)
                if (currentSelectedItem?.type == "Food" || currentSelectedItem?.type == "Drink") {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "Unit:", color = primaryColor, fontWeight = FontWeight.Medium)
                            if (currentSelectedItem.type == "Food") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox( checked = isKilogram, onCheckedChange = { isKilogram = it; if (it) isLiter = false }, enabled = currentSelectedItem != null )
                                    Text(text = "Kilogram (kg)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox( checked = !isKilogram, onCheckedChange = { isKilogram = !it; if (!it) isLiter = false }, enabled = currentSelectedItem != null )
                                    Text(text = "Gram (g)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                            } else if (currentSelectedItem.type == "Drink") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox( checked = isLiter, onCheckedChange = { isLiter = it; if (it) isKilogram = false }, enabled = currentSelectedItem != null )
                                    Text(text = "Litre (L)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox( checked = !isLiter, onCheckedChange = { isLiter = !it; if (!it) isKilogram = false }, enabled = currentSelectedItem != null )
                                    Text(text = "Milliliter (ml)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                            }
                        }
                    }
                }


                // Item for Time Input (Zaman Girişi)
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
                        enabled = currentSelectedItem != null // Sadece yiyecek seçiliyse aktif
                    )
                }

                // Item for Spacer before Button (Butondan önce boşluk)
                item { Spacer(modifier = Modifier.height(20.dp)) }


            } else { // --- Manual Entry UI --- (Manuel Giriş Alanları)

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

                // Item for Manual Food/Drink Name Input (Manuel İsim Girişi)
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

                // Item for Manual Type Selection (Manuel Tip Seçimi - Food/Drink)
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

                // Item for Quantity Input (Manuel Mod Miktar Girişi)
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

                // Item for Unit Selection (Manuel Mod Birim Seçimi)
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

                // Item for Time Input (Manuel Mod Zaman Girişi)
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

                // Item for Manual Total Calories Input (Manuel Toplam Kalori Girişi)
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

                item { Spacer(modifier = Modifier.height(12.dp)) } // Manuel P/F/C öncesi boşluk

                // Items for MANUAL P/F/C INPUTS (Manuel Makro Girişleri)
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


            // Item for Add to List Button (Listeye Ekle Butonu)
            item {
                Button(
                    onClick = {
                        val quantityValue = quantity.toDoubleOrNull() ?: 0.0
                        val item = selectedFoodItem // onClick içinde kullanılacak yerel kopya

                        // Birim seçimine göre ölçeklendirme faktörünü belirle
                        val unitScale = when {
                            isManualEntryMode -> when(selectedManualUnit) {
                                "kg", "L" -> quantityValue
                                "g", "ml" -> quantityValue / 1000.0
                                else -> 0.0 // Olmaması gereken durum
                            }
                            !isManualEntryMode && item != null -> when(
                                if (item.type == "Food") { if (isKilogram) "kg" else "g" } else { if (isLiter) "L" else "ml" }
                            ) {
                                "kg", "L" -> quantityValue
                                "g", "ml" -> quantityValue / 1000.0
                                else -> 0.0 // Olmaması gereken durum
                            }
                            else -> 0.0 // Normal modda item null ise
                        }


                        if (isManualEntryMode) {
                            val manualCalories = manualCaloriesInput.toIntOrNull() ?: 0
                            val manualProtein = manualProteinInput.toDoubleOrNull() ?: 0.0
                            val manualFat = manualFatInput.toDoubleOrNull() ?: 0.0
                            val manualCarb = manualCarbInput.toDoubleOrNull() ?: 0.0

                            // Manuel giriş alanlarının boş olmadığını, miktar > 0 olduğunu ve zamanın boş olmadığını kontrol et
                            if (manualFoodName.isNotEmpty() && quantityValue > 0 && time.isNotEmpty() && manualCaloriesInput.isNotEmpty() && manualProteinInput.isNotEmpty() && manualFatInput.isNotEmpty() && manualCarbInput.isNotEmpty()) {
                                // Ayrıştırılan değerlerin negatif olmadığını kontrol et
                                if (manualCalories >= 0 && manualProtein >= 0.0 && manualFat >= 0.0 && manualCarb >= 0.0) {
                                    val unit = selectedManualUnit
                                    calorieRecords.add(
                                        CalorieRecord(
                                            // Manuel giriş olduğu için orijinal besin değerleri 0 olarak kaydediliyor, toplamlar Record içinde tutuluyor
                                            foodItem = FoodItemKeepNote(manualFoodName, 0, manualSelectedType, 0.0, 0.0, 0.0),
                                            quantity = quantityValue,
                                            unit = unit,
                                            time = time,
                                            calories = manualCalories, // Kullanıcının girdiği toplam kalori
                                            protein = manualProtein,   // Kullanıcının girdiği toplam protein
                                            fat = manualFat,         // Kullanıcının girdiği toplam yağ
                                            carb = manualCarb        // Kullanıcının girdiği toplam karbonhidrat
                                        )
                                    )
                                    // Kayıt listesi güncellendikten sonra DOSYAYA KAYDET
                                    saveCalorieRecords(context, calorieRecords.toList())

                                    // Alanları temizle
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
                                    // Hata yönetimi: negatif sayılar
                                    println("Validation Error: Manual entry values must be non-negative.")
                                }
                            } else {
                                // Hata yönetimi: boş bırakılan alanlar
                                println("Validation Error: Please fill all required fields for manual entry.")
                            }
                        } else {
                            // Normal Giriş Modu
                            // Yiyecek seçili olduğunu, miktar alanının boş olmadığını, miktarın > 0 olduğunu ve zamanın boş olmadığını kontrol et
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
                                    CalorieRecord(
                                        foodItem = item, // items.txt'den gelen orijinal item
                                        quantity = quantityValue,
                                        unit = unit,
                                        time = time,
                                        calories = calculatedCalories, // Hesaplanan toplam kalori
                                        protein = calculatedProtein,   // Hesaplanan toplam protein
                                        fat = calculatedFat,         // Hesaplanan toplam yağ
                                        carb = calculatedCarb        // Hesaplanan toplam karbonhidrat
                                    )
                                )
                                // Kayıt listesi güncellendikten sonra DOSYAYA KAYDET
                                saveCalorieRecords(context, calorieRecords.toList())

                                // Alanları temizle
                                selectedFoodItem = null
                                searchText = ""
                                quantity = ""
                                time = ""
                                isKilogram = false
                                isLiter = false
                            } else {
                                // Hata yönetimi
                                println("Validation Error: Please select a food, enter a quantity, and time.")
                            }
                        }
                    },
                    // Butonun aktif olup olmayacağını belirleyen koşullar
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors( containerColor = primaryColor, contentColor = onPrimaryColor ),
                    enabled = if (isManualEntryMode) {
                        // Manuel modda tüm gerekli alanların dolu ve geçerli olduğunu kontrol et
                        manualFoodName.isNotEmpty() && quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0 && time.isNotEmpty() &&
                                manualCaloriesInput.isNotEmpty() && manualCaloriesInput.toIntOrNull() != null && manualCaloriesInput.toIntOrNull()!! >= 0 && // 0 kaloriye izin ver
                                manualProteinInput.isNotEmpty() && manualProteinInput.toDoubleOrNull() != null && manualProteinInput.toDoubleOrNull()!! >= 0.0 && // 0g makroya izin ver
                                manualFatInput.isNotEmpty() && manualFatInput.toDoubleOrNull() != null && manualFatInput.toDoubleOrNull()!! >= 0.0 &&
                                manualCarbInput.isNotEmpty() && manualCarbInput.toDoubleOrNull() != null && manualCarbInput.toDoubleOrNull()!! >= 0.0
                    } else {
                        // Normal modda yiyecek seçili olduğunu ve miktar ile zamanın geçerli olduğunu kontrol et
                        selectedFoodItem != null && quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0 && time.isNotEmpty()
                    }
                ) {
                    Text("Add to List", fontSize = 18.sp)
                }
            }


            // Item for Calorie Records Header (Kayıtlar Başlığı)
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


            // Items for Calorie Records List (Kayıtlar Listesi)
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
                            // Makro toplamlarını göster (Sıfırdan büyükse)
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
                            // Silme butonu
                            IconButton( onClick = { selectedRecordToDelete = record; showDeleteConfirmationDialog = true } ) {
                                Icon( imageVector = Icons.Outlined.Delete, contentDescription = "Delete Record", tint = primaryColor )
                            }
                        }
                    }
                }
            }

            // Item for TOTAL CALORIES AND MACROS DISPLAY (Toplamları Gösterme Alanı)
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
                        // Makro toplamlarını sadece sıfırdan büyükse göster
                        if (totalProtein > 0.0 || totalFat > 0.0 || totalCarb > 0.0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text( text = "Protein: ${totalProtein.roundToInt()}g", style = MaterialTheme.typography.bodyMedium )
                            Text( text = "Fat: ${totalFat.roundToInt()}g", style = MaterialTheme.typography.bodyMedium )
                            Text( text = "Carbs: ${totalCarb.roundToInt()}g", style = MaterialTheme.typography.bodyMedium )
                        }
                    }
                }

                Button(
                    onClick = { showSaveSummaryDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = onPrimaryColor
                    )
                ) {
                    Text("Save Daily Totals", fontSize = 16.sp)
                }

                Button(
                    onClick = { navController.navigate(Screens.DailySummaryScreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = onPrimaryColor
                    )
                ) {
                    Text("Go to Daily Summaries", fontSize = 16.sp)
                }




            }
        } // LazyColumn Sonu

        // Dialogs (Dialoglar - Kaydırılabilir alanın dışında kalır)
        if (showClearConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmationDialog = false },
                title = { Text("Clear All Records", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to clear all calorie records?") },
                confirmButton = {
                    TextButton(onClick = {
                        calorieRecords.clear()
                        saveCalorieRecords(context, calorieRecords.toList()) // TEMİZLEDİKTEN SONRA KAYDET
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

        if (showSaveSummaryDialog) {
            AlertDialog(
                onDismissRequest = { showSaveSummaryDialog = false },
                title = { Text("Save Daily Summary", color = primaryColor) },
                text = { Text("Are you sure you want to save today's summary?") },
                confirmButton = {
                    TextButton(onClick = {
                        val saved = saveTodaySummary(context, totalCalories, totalProtein, totalFat, totalCarb)
                        showSaveSummaryDialog = false
                        if (saved) {
                            Toast.makeText(context, "Summary saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screens.DailySummaryScreen)
                        } else {
                            Toast.makeText(context, "You've already saved a summary for today.", Toast.LENGTH_SHORT).show()

                        }
                    }) {
                        Text("Yes", color = primaryColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveSummaryDialog = false }) {
                        Text("Cancel", color = primaryColor)
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
                    TextButton(onClick = {
                        calorieRecords.remove(selectedRecordToDelete!!)
                        saveCalorieRecords(context, calorieRecords.toList()) // SİLDİKTEN SONRA KAYDET
                        showDeleteConfirmationDialog = false
                        selectedRecordToDelete = null
                    }) {
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
    } // Box Sonu
}