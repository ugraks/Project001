package com.ugraks.project1 // Kendi paket adınız

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.hilt.navigation.compose.hiltViewModel // Hilt ViewModel için import
import androidx.navigation.NavHostController
import com.ugraks.project1.AppNavigation.Screens // Navigasyon ekranları
import com.ugraks.project1.KeepNoteComposable.FoodItemKeepNote // FoodItemKeepNote (asset'ten okunuyor olabilir)
import com.ugraks.project1.data.local.entity.CalorieRecordEntity // YENİ Room Entity
import com.ugraks.project1.ui.viewmodels.CalorieViewModel // Kendi ViewModel'ınız
import kotlinx.coroutines.launch // Coroutine başlatmak için
import java.time.LocalDate // LocalDate için import (API 26+)
import java.util.Locale
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O) // LocalDate kullanımı nedeniyle gerekebilir
@OptIn(ExperimentalMaterial3Api::class) // Material 3 opt-in gerektiriyorsa
@Composable
fun KeepNotePage(
    navController: NavHostController,
    viewModel: CalorieViewModel = hiltViewModel() // ViewModel'ı Hilt ile inject et
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() // suspend fonksiyonları çağırmak için scope

    // --- State Management ---
    // allFoodItems ViewModel'dan alınacak veya hala asset'ten okunacaksa burada tutulur
    val allFoodItems = remember { mutableStateListOf<FoodItemKeepNote>().apply { addAll(viewModel.allFoodItems) } }
    // calorieRecords artık ViewModel'dan Room'dan gelen Flow/StateFlow'u izleyecek
    val calorieRecords by viewModel.calorieRecords.collectAsState() // Room'dan gelen kayıt listesi

    var isManualEntryMode by remember { mutableStateOf(false) }

    // Search and Normal Input States
    var searchText by remember { mutableStateOf("") }
    var selectedFoodItem by remember { mutableStateOf<FoodItemKeepNote?>(null) }

    // --- Dialog States ---
    var showSaveSummaryDialog by remember { mutableStateOf(false) }
    var showUpdateSummaryDialog by remember { mutableStateOf(false) }
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var selectedRecordToDelete by remember { mutableStateOf<CalorieRecordEntity?>(null) } // Tipi CalorieRecordEntity oldu
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }


    // Common Input States
    var quantity by remember { mutableStateOf("") }
    var isKilogram by remember { mutableStateOf(false) }
    var isLiter by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf("") }

    // Manual Entry States
    var manualFoodName by remember { mutableStateOf("") }
    var manualSelectedType by remember { mutableStateOf("Food") }
    var selectedManualUnit by remember { mutableStateOf("g") }
    var manualCaloriesInput by remember { mutableStateOf("") }
    var manualProteinInput by remember { mutableStateOf("") }
    var manualFatInput by remember { mutableStateOf("") }
    var manualCarbInput by remember { mutableStateOf("") }


    // State for Total Calories/Macros (calorieRecords listesi değiştikçe otomatik hesaplanır)
    val totalCalories = remember(calorieRecords) { calorieRecords.sumOf { it.totalCalories } }
    val totalProtein = remember(calorieRecords) { calorieRecords.sumOf { it.totalProtein } }
    val totalFat = remember(calorieRecords) { calorieRecords.sumOf { it.totalFat } }
    val totalCarb = remember(calorieRecords) { calorieRecords.sumOf { it.totalCarb } }


    // Color scheme
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val backgroundColor = colorScheme.background
    val surfaceColor = colorScheme.surface
    val surfaceVariantColor = colorScheme.surfaceVariant
    val contentColor = colorScheme.onSurface
    val onPrimaryColor = colorScheme.onPrimary


    // LaunchedEffect: Uygulama ilk açıldığında veya bu Composable ilk oluşturulduğunda çalışır
    // TXT'den veri yükleme LaunchedEffect'i artık GEREKMEZ, ViewModel Flow sağlar.
    LaunchedEffect(Unit) {
        // allFoodItems ViewModel başlatıldığında asset'ten okunur
        // calorieRecords ViewModel'dan Flow ile izlenir
    }


    // Effekt: Kayıtlar değiştikçe bugün için özet var mı kontrol et (Save/Update butonu için)
    var todayHasSummary by remember { mutableStateOf(false) }
    LaunchedEffect(calorieRecords) { // calorieRecords listesi veya ilk açılış değiştiğinde
        // ViewModel'ın suspend fonksiyonunu CoroutineScope içinde çağır
        todayHasSummary = viewModel.checkTodaySummaryExists()
    }


    // filteredFoodList: Arama metnine göre filtrelenmiş yiyecek listesi (Asset'ten okunan listeden)
    val filteredFoodList = remember(allFoodItems, searchText) {
        if (searchText.isEmpty()) {
            emptyList()
        } else {
            allFoodItems.filter { it.name.lowercase(Locale.getDefault()).contains(searchText.lowercase(Locale.getDefault())) }
        }
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
            // Item for Top Bar: Back and Clear
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


            // Items for Normal Entry UI (Manuel Modda Gizli)
            if (!isManualEntryMode) {
                // --- Normal Entry UI ---
                val currentSelectedItem = selectedFoodItem

                // Item for Search Bar
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

                // Item(s) for Search Results or Offer Manual Entry
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
                        enabled = currentSelectedItem != null // Sadece yiyecek seçiliyse aktif
                    )
                }

                // Item for Unit Selection (Normal Mod)
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
                        enabled = currentSelectedItem != null // Sadece yiyecek seçiliyse aktif
                    )
                }

                // Item for Spacer before Button
                item { Spacer(modifier = Modifier.height(20.dp)) }


            } else { // --- Manual Entry UI ---

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

                // Item for Manual Type Selection
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

                // Item for Quantity Input (Manuel Mod)
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

                // Item for Unit Selection (Manuel Mod)
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

                item { Spacer(modifier = Modifier.height(12.dp)) } // Manuel Time öncesi boşluk

                // Item for Time Input (Manuel Mod)
                item {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (e.g., 12:30)", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
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

                item { Spacer(modifier = Modifier.height(12.dp)) } // Manuel P/F/C öncesi boşluk

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
                        val item = selectedFoodItem

                        // Manuel veya Normal Giriş moduna göre hesaplanacak veya manuel girilen değerler kullanılacak
                        val calculatedCalories: Int
                        val calculatedProtein: Double
                        val calculatedFat: Double
                        val calculatedCarb: Double
                        val recordFoodName: String
                        val recordOriginalCaloriesPer1000: Int
                        val recordFoodType: String
                        val recordOriginalProteinPerKgL: Double
                        val recordOriginalFatPerKgL: Double
                        val recordOriginalCarbPerKgL: Double
                        val recordUnit: String


                        if (isManualEntryMode) {
                            calculatedCalories = manualCaloriesInput.toIntOrNull() ?: 0
                            calculatedProtein = manualProteinInput.toDoubleOrNull() ?: 0.0
                            calculatedFat = manualFatInput.toDoubleOrNull() ?: 0.0
                            calculatedCarb = manualCarbInput.toDoubleOrNull() ?: 0.0
                            recordFoodName = manualFoodName
                            recordOriginalCaloriesPer1000 = 0 // Manuel girişte orijinal değer 0 olabilir
                            recordFoodType = manualSelectedType
                            recordOriginalProteinPerKgL = 0.0
                            recordOriginalFatPerKgL = 0.0
                            recordOriginalCarbPerKgL = 0.0
                            recordUnit = selectedManualUnit


                            // Manuel giriş alanlarının boş olmadığını, miktar > 0 olduğunu ve zamanın boş olmadığını kontrol et
                            if (manualFoodName.isNotEmpty() && quantity.isNotEmpty() && quantityValue > 0 && time.isNotEmpty() &&
                                manualCaloriesInput.isNotEmpty() && manualProteinInput.isNotEmpty() && manualFatInput.isNotEmpty() && manualCarbInput.isNotEmpty()) {

                                // Ayrıştırılan değerlerin negatif olmadığını kontrol et
                                if (calculatedCalories >= 0 && calculatedProtein >= 0.0 && calculatedFat >= 0.0 && calculatedCarb >= 0.0) {

                                    // YENİ: CalorieRecordEntity oluştur
                                    val newRecordEntity = CalorieRecordEntity(
                                        foodName = recordFoodName,
                                        originalCaloriesPer1000 = recordOriginalCaloriesPer1000,
                                        foodType = recordFoodType,
                                        originalProteinPerKgL = recordOriginalProteinPerKgL,
                                        originalFatPerKgL = recordOriginalFatPerKgL,
                                        originalCarbPerKgL = recordOriginalCarbPerKgL,
                                        quantity = quantityValue,
                                        unit = recordUnit,
                                        time = time,
                                        totalCalories = calculatedCalories,
                                        totalProtein = calculatedProtein,
                                        totalFat = calculatedFat,
                                        totalCarb = calculatedCarb // Alan adını kontrol edin
                                    )

                                    // YENİ: ViewModel üzerinden Room'a ekle
                                    viewModel.addCalorieRecord(newRecordEntity)
                                    // ESKİ: calorieRecords.add(...) ve saveCalorieRecords(...) çağrıları kaldırıldı

                                    // Alanları temizle ve modu sıfırla
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
                                    Toast.makeText(context, "Manual entry added!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Manual entry values must be non-negative.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Please fill all required fields for manual entry.", Toast.LENGTH_SHORT).show()
                            }

                        } else { // Normal Giriş Modu
                            // Yiyecek seçili olduğunu, miktar alanının boş olmadığını, miktarın > 0 olduğunu ve zamanın boş olmadığını kontrol et
                            if (item != null && quantity.isNotEmpty() && quantityValue > 0 && time.isNotEmpty()) {

                                // Hesaplama mantığı aynı kalır
                                val unitScale = when(
                                    if (item.type == "Food") { if (isKilogram) "kg" else "g" } else { if (isLiter) "L" else "ml" }
                                ) {
                                    "kg", "L" -> quantityValue
                                    "g", "ml" -> quantityValue / 1000.0
                                    else -> 0.0
                                }

                                calculatedCalories = (item.calories.toDouble() * unitScale).roundToInt()
                                calculatedProtein = item.proteinPerKgL * unitScale
                                calculatedFat = item.fatPerKgL * unitScale
                                calculatedCarb = item.carbPerKgL * unitScale

                                recordFoodName = item.name
                                recordOriginalCaloriesPer1000 = item.calories
                                recordFoodType = item.type
                                recordOriginalProteinPerKgL = item.proteinPerKgL
                                recordOriginalFatPerKgL = item.fatPerKgL
                                recordOriginalCarbPerKgL = item.carbPerKgL
                                recordUnit = if (item.type == "Food") { if (isKilogram) "kg" else "g" } else { if (isLiter) "L" else "ml" }


                                // YENİ: CalorieRecordEntity oluştur
                                val newRecordEntity = CalorieRecordEntity(
                                    foodName = recordFoodName,
                                    originalCaloriesPer1000 = recordOriginalCaloriesPer1000,
                                    foodType = recordFoodType,
                                    originalProteinPerKgL = recordOriginalProteinPerKgL,
                                    originalFatPerKgL = recordOriginalFatPerKgL,
                                    originalCarbPerKgL = recordOriginalCarbPerKgL,
                                    quantity = quantityValue,
                                    unit = recordUnit,
                                    time = time,
                                    totalCalories = calculatedCalories,
                                    totalProtein = calculatedProtein,
                                    totalFat = calculatedFat,
                                    totalCarb = calculatedCarb // Alan adını kontrol edin
                                )

                                // YENİ: ViewModel üzerinden Room'a ekle
                                viewModel.addCalorieRecord(newRecordEntity)
                                // ESKİ: calorieRecords.add(...) ve saveCalorieRecords(...) çağrıları kaldırıldı

                                // Alanları temizle
                                selectedFoodItem = null
                                searchText = ""
                                quantity = ""
                                time = ""
                                isKilogram = false
                                isLiter = false
                                Toast.makeText(context, "Record added!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please select a food, enter a valid quantity, and time.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    // Butonun aktif olup olmayacağını belirleyen koşullar (aynı kalır)
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors( containerColor = primaryColor, contentColor = onPrimaryColor ),
                    enabled = if (isManualEntryMode) {
                        manualFoodName.isNotEmpty() && quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0 && time.isNotEmpty() &&
                                manualCaloriesInput.isNotEmpty() && manualCaloriesInput.toIntOrNull() != null && manualCaloriesInput.toIntOrNull()!! >= 0 &&
                                manualProteinInput.isNotEmpty() && manualProteinInput.toDoubleOrNull() != null && manualProteinInput.toDoubleOrNull()!! >= 0.0 &&
                                manualFatInput.isNotEmpty() && manualFatInput.toDoubleOrNull() != null && manualFatInput.toDoubleOrNull()!! >= 0.0 &&
                                manualCarbInput.isNotEmpty() && manualCarbInput.toDoubleOrNull() != null && manualCarbInput.toDoubleOrNull()!! >= 0.0
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


            // Items for Calorie Records List (Room'dan gelen calorieRecords listesi kullanılır)
            // itemsIndexed(calorieRecords) { index, record -> // record artık CalorieRecordEntity
            itemsIndexed(calorieRecords) { index, recordEntity -> // YENİ: recordEntity kullanın
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
                            // record.foodItem.name yerine recordEntity.foodName
                            Text(
                                text = recordEntity.foodName,
                                style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic, color = primaryColor, fontWeight = FontWeight.Medium
                            )
                            // record.quantity, record.unit, record.time yerine recordEntity.quantity, recordEntity.unit, recordEntity.time
                            Text(
                                text = "${recordEntity.quantity} ${recordEntity.unit} - ${recordEntity.time}",
                                style = MaterialTheme.typography.bodyMedium, color = contentColor
                            )
                            // Makro toplamlarını göster (Sıfırdan büyükse) - Alan adlarını kontrol edin
                            if (recordEntity.totalProtein > 0.0 || recordEntity.totalFat > 0.0 || recordEntity.totalCarb > 0.0) {
                                Text(
                                    text = "P: ${recordEntity.totalProtein.roundToInt()}g, F: ${recordEntity.totalFat.roundToInt()}g, C: ${recordEntity.totalCarb.roundToInt()}g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            // record.calories yerine recordEntity.totalCalories
                            Text(
                                text = "${recordEntity.totalCalories} kcal",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = primaryColor
                            )
                            // Silme butonu
                            IconButton( onClick = { selectedRecordToDelete = recordEntity; showDeleteConfirmationDialog = true } ) { // YENİ: selectedRecordToDelete tipi CalorieRecordEntity
                                Icon( imageVector = Icons.Outlined.Delete, contentDescription = "Delete Record", tint = primaryColor )
                            }
                        }
                    }
                }
            }

            // Item for TOTAL CALORIES AND MACROS DISPLAY (calorieRecords listesi değiştikçe otomatik güncellenir)
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

                // --- Save/Update Daily Totals Button ---
                Button(
                    onClick = {
                        if (todayHasSummary) {
                            // Bugün zaten özet varsa, güncelleme diyalogunu göster
                            showUpdateSummaryDialog = true
                        } else {
                            // Bugün özet yoksa, normal kaydetme diyalogunu göster
                            showSaveSummaryDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = onPrimaryColor
                    ),
                    // Kaydedilmiş kayıtlar varsa ve toplam kalori > 0 ise butonu aktif et (aynı kalır)
                    enabled = calorieRecords.isNotEmpty() && totalCalories > 0
                ) {
                    Text(if (todayHasSummary) "Update Daily Totals" else "Save Daily Totals", fontSize = 16.sp)
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

        // --- Dialogs --- (Kaydırılabilir alanın dışında kalır)

        // 1. Clear All Records Dialog
        if (showClearConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmationDialog = false },
                title = { Text("Clear All Records", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to clear all calorie records?") },
                confirmButton = {
                    TextButton(onClick = {
                        // YENİ: ViewModel üzerinden tüm kayıtları sil
                        viewModel.clearAllCalorieRecords()
                        // ESKİ: calorieRecords.clear() ve saveCalorieRecords(...) çağrıları kaldırıldı
                        showClearConfirmationDialog = false
                        Toast.makeText(context, "All records cleared!", Toast.LENGTH_SHORT).show()
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

        // 2. Initial Save Daily Summary Dialog
        if (showSaveSummaryDialog) {
            AlertDialog(
                onDismissRequest = { showSaveSummaryDialog = false },
                title = { Text("Save Daily Summary", color = primaryColor) },
                text = { Text("Are you sure you want to save today's summary?") },
                confirmButton = {
                    TextButton(onClick = {
                        // YENİ: ViewModel üzerinden günlük özeti kaydet/güncelle
                        coroutineScope.launch { // ViewModel suspend fonksiyonu için scope
                            viewModel.saveOrUpdateDailySummary(totalCalories, totalProtein, totalFat, totalCarb)
                            showSaveSummaryDialog = false // Dialogu kapat
                            Toast.makeText(context, "Summary saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screens.DailySummaryScreen) // Başarılı kayıtta navigate
                        }
                        // ESKİ: saveTodaySummary(...) çağrısı kaldırıldı ve result kontrolü ViewModel içine taşındı
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

        // --- 3. Update Daily Summary Dialog ---
        if (showUpdateSummaryDialog) {
            AlertDialog(
                onDismissRequest = { showUpdateSummaryDialog = false },
                title = { Text("Update Daily Summary", color = primaryColor) },
                text = { Text("A summary for today already exists. Do you want to update it with the current totals?") },
                confirmButton = {
                    TextButton(onClick = {
                        // YENİ: ViewModel üzerinden günlük özeti kaydet/güncelle
                        coroutineScope.launch { // ViewModel suspend fonksiyonu için scope
                            viewModel.saveOrUpdateDailySummary(totalCalories, totalProtein, totalFat, totalCarb)
                            showUpdateSummaryDialog = false // Dialogu kapat
                            Toast.makeText(context, "Summary updated!", Toast.LENGTH_SHORT).show()
                            // Güncellemeden sonra da Daily Summary ekranına yönlendirebiliriz
                            navController.navigate(Screens.DailySummaryScreen)
                        }
                        // ESKİ: saveTodaySummary(...) çağrısı kaldırıldı ve result kontrolü ViewModel içine taşındı
                    }) {
                        Text("Update", color = primaryColor) // Buton metni "Update"
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUpdateSummaryDialog = false }) {
                        Text("Cancel", color = primaryColor)
                    }
                }
            )
        }


        // 4. Delete Single Record Dialog
        if (showDeleteConfirmationDialog && selectedRecordToDelete != null) { // selectedRecordToDelete CalorieRecordEntity tipinde
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false; selectedRecordToDelete = null },
                title = { Text("Delete Record", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to delete this record?") },
                confirmButton = {
                    TextButton(onClick = {
                        // YENİ: ViewModel üzerinden belirli kaydı sil
                        selectedRecordToDelete?.let { recordEntity ->
                            viewModel.deleteCalorieRecord(recordEntity) // ViewModel metodunu çağır
                            // ESKİ: calorieRecords.remove(...) ve saveCalorieRecords(...) çağrıları kaldırıldı
                        }
                        showDeleteConfirmationDialog = false
                        selectedRecordToDelete = null
                        Toast.makeText(context, "Record deleted!", Toast.LENGTH_SHORT).show()
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