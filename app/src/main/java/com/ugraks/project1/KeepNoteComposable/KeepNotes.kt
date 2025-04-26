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
import androidx.compose.foundation.lazy.items // items yerine itemsIndexed de kullanabilirsiniz
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.* // remember, mutableStateOf, LaunchedEffect, getValue, setValue, rememberCoroutineScope, collectAsState
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
import com.ugraks.project1.KeepNoteComposable.FoodItemKeepNote // FoodItemKeepNote (asset'ten okunuyor)
import com.ugraks.project1.data.local.entity.CalorieRecordEntity // Room Entity
import com.ugraks.project1.ui.viewmodels.CalorieViewModel // Kendi ViewModel'ınız
import kotlinx.coroutines.launch // Coroutine başlatmak için
import java.time.LocalDate // LocalDate için import (API 26+)
import java.util.Locale
import kotlin.math.roundToInt

// Yardımcı fonksiyonların importları (calculateNutritionalValues, getImageResource)
// calculateNutritionalValues fonksiyonunuzun FoodItemKeepNote ve Double quantityScale alacak şekilde ayarlandığından emin olun!
// import com.ugraks.project1.Foods.calculateNutritionalValues // Eğer kullanılıyorsa


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
    // allFoodItems ViewModel'dan List olarak alınacak (Repository asset'ten okuyor)
    val allFoodItems by viewModel.allFoodItems.collectAsState() // ViewModel'dan doğrudan List<FoodItemKeepNote> al

    // calorieRecords ViewModel'dan Room'dan gelen Flow/StateFlow'u izleyecek
    val calorieRecords by viewModel.calorieRecords.collectAsState() // Room'dan gelen kayıt listesi

    // *** YENİ STATE: DailyCalorieViewModel tarafından kaydedilen kalori ihtiyacı ***
    val dailyCalculatedCalorieNeed by viewModel.dailyCalculatedCalorieNeed // ViewModel'dan gelen değeri gözlemle
    // ****************************************************************************

    var isManualEntryMode by remember { mutableStateOf(false) }

    // Search and Normal Input States
    var searchText by remember { mutableStateOf("") }
    // selectedFoodItem, Composable'ın kendi state'i olarak FoodItemKeepNote? tutacak
    var selectedFoodItem by remember { mutableStateOf<FoodItemKeepNote?>(null) }


    // --- Dialog States ---
    var showSaveSummaryDialog by remember { mutableStateOf(false) }
    var showUpdateSummaryDialog by remember { mutableStateOf(false) }
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var selectedRecordToDelete by remember { mutableStateOf<CalorieRecordEntity?>(null) } // Tipi CalorieRecordEntity oldu
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    // *** YENİ DIALOG STATE: Kalori aşımı uyarısı için ***
    var showCalorieWarningDialog by remember { mutableStateOf(false) }
    // Ek: Uyarı gösterildikten sonra tekrar hemen göstermemek için bir state
    var hasShownCalorieWarningForCurrentState by remember { mutableStateOf(false) }
    // *****************************************************


    // Common Input States
    var quantity by remember { mutableStateOf("") }
    // quantity'nin Double değerini burada hesaplayın
    val quantityDouble = remember(quantity) { quantity.toDoubleOrNull() ?: 0.0 } // quantity değiştiğinde otomatik güncellenir
    var isKilogram by remember { mutableStateOf(false) }
    var isLiter by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf("") }

    // Manual Entry States
    var manualFoodName by remember { mutableStateOf("") }
    var manualSelectedType by remember { mutableStateOf("Food") }
    var selectedManualUnit by remember { mutableStateOf("g") }
    var manualCaloriesInput by remember { mutableStateOf("") }
    var manualProteinInput by remember { mutableStateOf("") } // String olarak tutulacak
    var manualFatInput by remember { mutableStateOf("") } // String olarak tutulacak
    var manualCarbInput by remember { mutableStateOf("") } // String olarak tutulacak


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
    LaunchedEffect(Unit) {
        // allFoodItems ViewModel/Repository tarafından asset'ten okunur (lazy ile ViewModel'da)
        // calorieRecords ViewModel'dan Flow ile izlenir
    }

    // Effekt: Kayıtlar değiştikçe bugün için özet var mı kontrol et (Save/Update butonu için)
    var todayHasSummary by remember { mutableStateOf(false) }
    LaunchedEffect(calorieRecords) { // calorieRecords listesi veya ilk açılış değiştiğinde
        todayHasSummary = viewModel.checkTodaySummaryExists() // ViewModel metodunu çağırıyoruz
    }

    // *** YENİ LaunchedEffect: Toplam kalori ve günlük ihtiyacı karşılaştır ve uyarıyı tetikle ***
    LaunchedEffect(totalCalories, dailyCalculatedCalorieNeed) {
        // totalCalories veya dailyCalculatedCalorieNeed değiştiğinde çalışır

        // Eğer günlük kalori ihtiyacı hesaplanmışsa (> 0) VE
        // Tüketilen toplam kalori günlük ihtiyacı aştıysa VE
        // Bu durum için uyarı henüz gösterilmediyse
        if (dailyCalculatedCalorieNeed > 0 && totalCalories > dailyCalculatedCalorieNeed && !hasShownCalorieWarningForCurrentState) {
            showCalorieWarningDialog = true
            hasShownCalorieWarningForCurrentState = true // Uyarıyı bu durum için gösterildi olarak işaretle
        }
        // Eğer toplam kalori tekrar ihtiyacın altına düşerse, bir sonraki aşım için uyarıyı tekrar göstermeye izin ver
        else if (dailyCalculatedCalorieNeed > 0 && totalCalories <= dailyCalculatedCalorieNeed) {
            hasShownCalorieWarningForCurrentState = false // İhtiyaç karşılanınca uyarı state'ini sıfırla
        }
        // Eğer günlük kalori ihtiyacı 0'a dönerse (hesaplanmamış/sıfırlanmışsa) uyarı state'ini sıfırla
        else if (dailyCalculatedCalorieNeed <= 0) {
            hasShownCalorieWarningForCurrentState = false
            showCalorieWarningDialog = false // Diyalog açıksa kapat
        }
    }
    // ***************************************************************************************


    // filteredFoodList: Arama metnine göre filtrelenmiş yiyecek listesi
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
            .padding(top = 25.dp, bottom = 25.dp) // Box padding'i
    ) {
        // LazyColumn: Tüm içeriğin kaydırılabilir olmasını sağlar
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp), // LazyColumn padding'i
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
                            // Alanları temizle
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
                            searchText = "" // Arama metnini sıfırla
                            selectedFoodItem = null // Seçimi sıfırla
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            // Manuel modda iptal, normal modda geri ikonu
                            imageVector = if (isManualEntryMode) Icons.Filled.Clear else Icons.AutoMirrored.Filled.ArrowBack,
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
                            imageVector = Icons.Outlined.Delete, // Çöp kutusu ikonu daha uygun
                            contentDescription = "Clear All Records",
                            tint = primaryColor
                        )
                    }
                }
            }


            // Items for Normal Entry UI (Manuel Modda Gizli)
            if (!isManualEntryMode) {
                // --- Normal Entry UI ---
                // selectedFoodItem state'i kullanılır

                // Item for Search Bar
                item {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it // Arama metni state'ini güncelle
                            selectedFoodItem = null // Yeni arama başlarken veya metin değişince seçimi temizle
                            // Miktar ve zaman UI state'leri burada sıfırlanır
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
                                    searchText = "" // Arama metnini sıfırla
                                    selectedFoodItem = null // Seçimi sıfırla
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
                // Arama metni boş değilse VE öğe seçili değilse arama sonuçlarını veya manuel giriş teklifini göster
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
                                        // Alanları temizle ve moda geç
                                        searchText = "" // Arama metnini sıfırla
                                        selectedFoodItem = null // Seçimi sıfırla
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
                        // Display Search Results (filteredFoodList kullanılır - FoodItemKeepNote listesi)
                        itemsIndexed(filteredFoodList, key = { _, item -> item.name }) { index, item -> // item FoodItemKeepNote tipinde
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
                                        // Seçimi ayarla
                                        selectedFoodItem = item // Tıklanan FoodItemKeepNote öğesini seçili state'e ata

                                        // Arama çubuğunu güncelle (isteğe bağlı, ama genelde yapılır)
                                        searchText = item.name

                                        // Miktar ve zaman inputlarını temizle
                                        quantity = ""
                                        time = ""

                                        // Birim checkbox state'lerini doğrudan güncelle
                                        if (item.type == "Food") {
                                            isKilogram = true // Genellikle Food için varsayılan birim kg/g olur
                                            isLiter = false
                                        } else if (item.type == "Drink") {
                                            isLiter = true // Genellikle Drink için varsayılan birim L/ml olur
                                            isKilogram = false
                                        } else {
                                            // Diğer tipler için varsayılan veya hata durumu
                                            isKilogram = false
                                            isLiter = false
                                        }

                                        // Manuel giriş alanlarını temizlemek isteyebilirsiniz
                                        manualFoodName = ""
                                        manualCaloriesInput = ""
                                        manualProteinInput = ""
                                        manualFatInput = ""
                                        manualCarbInput = ""
                                        manualSelectedType = "Food"
                                        selectedManualUnit = "g"
                                        isManualEntryMode = false // Normal moda dön (eğer manuel moddaysak)
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = item.name, // item FoodItemKeepNote'un adı
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
                } else { // Arama metni boşsa VEYA öğe seçiliyse (arama sonuçları görünmez)
                    // Eğer öğe seçiliyse, boşluk bırakılır
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
                        enabled = selectedFoodItem != null // Sadece yiyecek seçiliyse aktif
                    )
                }

                // Item for Unit Selection (Normal Mod)
                if (selectedFoodItem != null && (selectedFoodItem?.type == "Food" || selectedFoodItem?.type == "Drink")) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "Unit:", color = primaryColor, fontWeight = FontWeight.Medium)
                            if (selectedFoodItem?.type == "Food") {
                                Row(modifier = Modifier.selectable( selected = isKilogram, onClick = {
                                    // Düzeltme: selectable onClick'te it kullanılmaz, doğrudan state ayarlanır
                                    isKilogram = true
                                    isLiter = false
                                }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Checkbox'ın onCheckedChange'i hala it (yeni checked durumu) kullanabilir
                                    Checkbox( checked = isKilogram, onCheckedChange = { isChecked -> isKilogram = isChecked; if (isChecked) isLiter = false }, enabled = selectedFoodItem != null )
                                    Text(text = "Kilogram (kg)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                                Row(modifier = Modifier.selectable( selected = !isKilogram, onClick = {
                                    // Düzeltme: selectable onClick'te it kullanılmaz, doğrudan state ayarlanır
                                    isKilogram = false // Kilogram değilse Gram'dır
                                    isLiter = false
                                }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Checkbox'ın onCheckedChange'i hala it (yeni checked durumu) kullanabilir
                                    // Bu Checkbox !isKilogram durumunu temsil ettiği için, checked olduğunda isKilogram false olmalı
                                    Checkbox( checked = !isKilogram, onCheckedChange = { isChecked -> // it yerine isChecked kullanmak daha net olabilir
                                        if (isChecked) { // Eğer Gram Checkbox'ı işaretlendiyse
                                            isKilogram = false
                                            isLiter = false
                                        }
                                        // Eğer Gram Checkbox'ının işareti kaldırıldıysa, başka bir şey otomatik seçilmez
                                    }, enabled = selectedFoodItem != null )
                                    Text(text = "Gram (g)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                            } else if (selectedFoodItem?.type == "Drink") {
                                Row(modifier = Modifier.selectable( selected = isLiter, onClick = {
                                    // Düzeltme: selectable onClick'te it kullanılmaz, doğrudan state ayarlanır
                                    isLiter = true
                                    isKilogram = false
                                }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Checkbox'ın onCheckedChange'i hala it (yeni checked durumu) kullanabilir
                                    Checkbox( checked = isLiter, onCheckedChange = { isChecked -> isLiter = isChecked; if (isChecked) isKilogram = false }, enabled = selectedFoodItem != null )
                                    Text(text = "Litre (L)", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                                }
                                Row(modifier = Modifier.selectable( selected = !isLiter, onClick = {
                                    // Düzeltme: selectable onClick'te it kullanılmaz, doğrudan state ayarlanır
                                    isLiter = false // Litre değilse Milliliter'dır
                                    isKilogram = false
                                }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Checkbox'ın onCheckedChange'i hala it (yeni checked durumu) kullanabilir
                                    // Bu Checkbox !isLiter durumunu temsil ettiği için, checked olduğunda isLiter false olmalı
                                    Checkbox( checked = !isLiter, onCheckedChange = { isChecked -> // it yerine isChecked kullanmak daha net olabilir
                                        if (isChecked) { // Eğer Milliliter Checkbox'ı işaretlendiyse
                                            isLiter = false
                                            isKilogram = false
                                        }
                                        // Eğer Milliliter Checkbox'ının işareti kaldırıldıysa, başka bir şey otomatik seçilmez
                                    }, enabled = selectedFoodItem != null )
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = selectedFoodItem != null // Sadece yiyecek seçiliyse aktif
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
                            RadioButton( selected = manualSelectedType == "Food", onClick = { manualSelectedType = "Food" } ) // RadioButton onClick kullanır
                            Text("Food", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                        }
                        Row(modifier = Modifier.selectable( selected = manualSelectedType == "Drink", onClick = { manualSelectedType = "Drink" }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton( selected = manualSelectedType == "Drink", onClick = { manualSelectedType = "Drink" } ) // RadioButton onClick kullanır
                            Text("Drink", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // Item for Quantity Input (Manuel Mod)
                item {
                    OutlinedTextField(
                        value = quantity, // quantityDouble değişkeni zaten quantity'den hesaplanıyor
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
                            Row( modifier = Modifier.selectable( selected = selectedManualUnit == "g", onClick = {
                                // Düzeltme: selectable onClick'te it kullanılmaz, doğrudan state ayarlanır
                                selectedManualUnit = "g"
                            }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
                                RadioButton( selected = selectedManualUnit == "g", onClick = { selectedManualUnit = "g" } )
                                Text("g", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                            }
                            Row( modifier = Modifier.selectable( selected = selectedManualUnit == "kg", onClick = {
                                // Düzeltme: selectable onClick'te it kullanılmaz, doğrudan state ayarlanır
                                selectedManualUnit = "kg"
                            }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
                                RadioButton( selected = selectedManualUnit == "kg", onClick = { selectedManualUnit = "kg" } )
                                Text("kg", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                            }
                        } else { // Drink
                            Row( modifier = Modifier.selectable( selected = selectedManualUnit == "ml", onClick = {
                                // Düzeltme: selectable onClick'te it kullanılmaz, doğrudan state ayarlanır
                                selectedManualUnit = "ml"
                            }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
                                RadioButton( selected = selectedManualUnit == "ml", onClick = { selectedManualUnit = "ml" } )
                                Text("ml", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                            }
                            Row( modifier = Modifier.selectable( selected = selectedManualUnit == "L", onClick = {
                                // Düzeltme: selectable onClick'te it kullanılmaz, doğrudan state ayarlanır
                                selectedManualUnit = "L"
                            }).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
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

                item { Spacer(modifier = Modifier.height(12.dp)) } // Manuel Makro öncesi boşluk

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

            } // Manuel Giriş UI Sonu


            // Item for Add to List Button (Bu item artık if/else bloğunun dışında)
            item {
                Button(
                    onClick = {
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
                            // Manuel giriş alanlarının dolu ve geçerli olduğunu kontrol et
                            if (manualFoodName.isNotEmpty() && quantityDouble > 0 && time.isNotEmpty() &&
                                manualCaloriesInput.toIntOrNull() != null && manualCaloriesInput.toIntOrNull()!! >= 0 &&
                                manualProteinInput.toDoubleOrNull() != null && manualProteinInput.toDoubleOrNull()!! >= 0.0 &&
                                manualFatInput.toDoubleOrNull() != null && manualFatInput.toDoubleOrNull()!! >= 0.0 &&
                                manualCarbInput.toDoubleOrNull() != null && manualCarbInput.toDoubleOrNull()!! >= 0.0) {

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

                                // CalorieRecordEntity oluştur
                                val newRecordEntity = CalorieRecordEntity(
                                    foodName = recordFoodName,
                                    originalCaloriesPer1000 = recordOriginalCaloriesPer1000,
                                    foodType = recordFoodType,
                                    originalProteinPerKgL = recordOriginalProteinPerKgL,
                                    originalFatPerKgL = recordOriginalFatPerKgL,
                                    originalCarbPerKgL = recordOriginalCarbPerKgL,
                                    quantity = quantityDouble,
                                    unit = recordUnit,
                                    time = time,
                                    totalCalories = calculatedCalories,
                                    totalProtein = calculatedProtein,
                                    totalFat = calculatedFat,
                                    totalCarb = calculatedCarb
                                )

                                // ViewModel üzerinden Room'a ekle
                                viewModel.addCalorieRecord(newRecordEntity)

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
                                searchText = "" // Arama metnini sıfırla
                                selectedFoodItem = null // Seçimi sıfırla
                                // Uyarı durumu için state'i sıfırla (Yeni kayıtla toplam değiştiği için LaunchedEffect tekrar kontrol edecektir)
                                hasShownCalorieWarningForCurrentState = false
                                // showCalorieWarningDialog = false // Gerek Yok, LaunchedEffect yönetecek

                                Toast.makeText(context, "Manual entry added!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Eksik veya geçersiz input olduğunda gösterilecek mesaj
                                Toast.makeText(context, "Please fill all required fields with valid non-negative numbers.", Toast.LENGTH_SHORT).show()
                            }

                        } else { // Normal Giriş Modu
                            // Yiyecek seçili olduğunu, miktar > 0 olduğunu ve zamanın boş olmadığını kontrol et
                            if (selectedFoodItem != null && quantityDouble > 0 && time.isNotEmpty()) {

                                val unitScale: Double
                                val recordUnit: String // Kaydedilecek kaydın birimi

                                val item = selectedFoodItem!! // Artık item'ın null olmadığını biliyoruz

                                if (item.type == "Food") {
                                    if (isKilogram) {
                                        unitScale = quantityDouble
                                        recordUnit = "kg"
                                    } else { // isGram ise (isKilogram false olduğunda)
                                        unitScale = quantityDouble / 1000.0
                                        recordUnit = "g"
                                    }
                                } else if (item.type == "Drink") {
                                    if (isLiter) {
                                        unitScale = quantityDouble
                                        recordUnit = "L"
                                    } else { // isMilliliter ise (isLiter false olduğunda)
                                        unitScale = quantityDouble / 1000.0
                                        recordUnit = "ml"
                                    }
                                } else {
                                    unitScale = 0.0
                                    recordUnit = "unknown"
                                    Toast.makeText(context, "Unknown item type for unit calculation.", Toast.LENGTH_SHORT).show()
                                }

                                // Hesaplamayı burada manuel yapalım, calculateNutritionalValues fonksiyonunuz benzerini yapmalıydı
                                calculatedCalories = (item.calories.toDouble() * unitScale).roundToInt()
                                calculatedProtein = item.proteinPerKgL * unitScale
                                calculatedFat = item.fatPerKgL * unitScale
                                calculatedCarb = item.carbPerKgL * unitScale


                                // RecordEntity oluşturulurken unit ve quantity alanlarını bu yeni değerlerle güncelleyin
                                val newRecordEntity = CalorieRecordEntity(
                                    foodName = item.name,
                                    originalCaloriesPer1000 = item.calories,
                                    foodType = item.type,
                                    originalProteinPerKgL = item.proteinPerKgL,
                                    originalFatPerKgL = item.fatPerKgL,
                                    originalCarbPerKgL = item.carbPerKgL,
                                    quantity = quantityDouble,
                                    unit = recordUnit,
                                    time = time,
                                    totalCalories = calculatedCalories,
                                    totalProtein = calculatedProtein,
                                    totalFat = calculatedFat,
                                    totalCarb = calculatedCarb
                                )

                                // ViewModel üzerinden Room'a ekle
                                viewModel.addCalorieRecord(newRecordEntity)

                                // Alanları temizle
                                selectedFoodItem = null
                                searchText = ""
                                quantity = ""
                                time = ""
                                isKilogram = false // Birimleri varsayılan duruma getir
                                isLiter = false // Birimleri varsayılan duruma getir
                                // Uyarı durumu için state'i sıfırla (Yeni kayıtla toplam değiştiği için LaunchedEffect tekrar kontrol edecektir)
                                hasShownCalorieWarningForCurrentState = false
                                // showCalorieWarningDialog = false // Gerek Yok, LaunchedEffect yönetecek

                                Toast.makeText(context, "Record added!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please select a food, enter a valid quantity (> 0), and time.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    // Butonun aktif olup olmayacağını belirleyen koşullar (quantityDouble kullanıldı)
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors( containerColor = primaryColor, contentColor = onPrimaryColor ),
                    enabled = if (isManualEntryMode) {
                        // Manuel modda geçerlilik kontrolü
                        manualFoodName.isNotEmpty() && quantityDouble > 0 && time.isNotEmpty() &&
                                manualCaloriesInput.toIntOrNull() != null && manualCaloriesInput.toIntOrNull()!! >= 0 &&
                                manualProteinInput.toDoubleOrNull() != null && manualProteinInput.toDoubleOrNull()!! >= 0.0 &&
                                manualFatInput.toDoubleOrNull() != null && manualFatInput.toDoubleOrNull()!! >= 0.0 &&
                                manualCarbInput.toDoubleOrNull() != null && manualCarbInput.toDoubleOrNull()!! >= 0.0
                    } else {
                        // Normal modda geçerlilik kontrolü
                        selectedFoodItem != null && quantityDouble > 0 && time.isNotEmpty()
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
            itemsIndexed(calorieRecords, key = { _, recordEntity -> recordEntity.id }) { index, recordEntity -> // Key eklendi (performans için iyi)
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
                                text = recordEntity.foodName,
                                style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic, color = primaryColor, fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${recordEntity.quantity} ${recordEntity.unit} - ${recordEntity.time}",
                                style = MaterialTheme.typography.bodyMedium, color = contentColor
                            )
                            // Makro toplamlarını göster (Sıfırdan büyükse)
                            if (recordEntity.totalProtein > 0.0 || recordEntity.totalFat > 0.0 || recordEntity.totalCarb > 0.0) {
                                Text(
                                    text = "P: ${recordEntity.totalProtein.roundToInt()}g, F: ${recordEntity.totalFat.roundToInt()}g, C: ${recordEntity.totalCarb.roundToInt()}g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${recordEntity.totalCalories} kcal",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = primaryColor
                            )
                            // Silme butonu
                            IconButton( onClick = { selectedRecordToDelete = recordEntity; showDeleteConfirmationDialog = true } ) { // selectedRecordToDelete tipi CalorieRecordEntity
                                Icon( imageVector = Icons.Outlined.Delete, contentDescription = "Delete Record", tint = primaryColor )
                            }
                        }
                    }
                }
            }

            // Liste boşsa bilgi mesajı göster
            if (calorieRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No calorie records saved yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
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
                        // *** YENİ: Günlük Kalori İhtiyacını Göster (Hesaplanmışsa) ***
                        if (dailyCalculatedCalorieNeed > 0) {
                            Text(
                                text = "Daily Need: $dailyCalculatedCalorieNeed kcal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal, fontSize = 16.sp), // Biraz daha küçük font
                                color = contentColor // Primary yerine contentColor kullanmak daha iyi olabilir
                            )
                            Spacer(modifier = Modifier.height(4.dp)) // İhtiyaç ile tüketilen arasına boşluk
                        }
                        // *********************************************************

                        Text(
                            text = "Calories Consumed: $totalCalories kcal", // Metin güncellendi
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

                // --- Save/Update Daily Totals Button --- (onClick mantığı ViewModel'a gidiyor)
                Button(
                    onClick = {
                        if (todayHasSummary) {
                            showUpdateSummaryDialog = true
                        } else {
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
                    // Kaydedilmiş kayıtlar varsa ve toplam kalori > 0 ise butonu aktif et
                    enabled = calorieRecords.isNotEmpty() && totalCalories > 0
                ) {
                    Text(if (todayHasSummary) "Update Daily Totals" else "Save Daily Totals", fontSize = 16.sp)
                }

                // Go to Daily Summaries Button
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
                        // ViewModel üzerinden tüm kayıtları sil
                        viewModel.clearAllCalorieRecords()
                        showClearConfirmationDialog = false
                        Toast.makeText(context, "All records cleared!", Toast.LENGTH_SHORT).show()
                        // Kayıtlar silinince uyarı state'ini sıfırla
                        hasShownCalorieWarningForCurrentState = false
                        showCalorieWarningDialog = false
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
                        coroutineScope.launch {
                            viewModel.saveOrUpdateDailySummary(totalCalories, totalProtein, totalFat, totalCarb)
                            showSaveSummaryDialog = false // Dialogu kapat
                            Toast.makeText(context, "Summary saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screens.DailySummaryScreen) // Başarılı kayıtta navigate
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

        // 3. Update Daily Summary Dialog
        if (showUpdateSummaryDialog) {
            AlertDialog(
                onDismissRequest = { showUpdateSummaryDialog = false },
                title = { Text("Update Daily Summary", color = primaryColor) },
                text = { Text("A summary for today already exists. Do you want to update it with the current totals?") },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            viewModel.saveOrUpdateDailySummary(totalCalories, totalProtein, totalFat, totalCarb)
                            showUpdateSummaryDialog = false // Dialogu kapat
                            Toast.makeText(context, "Summary updated!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screens.DailySummaryScreen)
                        }
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
        if (showDeleteConfirmationDialog && selectedRecordToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false; selectedRecordToDelete = null },
                title = { Text("Delete Record", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to delete this record?") },
                confirmButton = {
                    TextButton(onClick = {
                        selectedRecordToDelete?.let { recordEntity ->
                            viewModel.deleteCalorieRecord(recordEntity) // ViewModel metodunu çağır
                        }
                        showDeleteConfirmationDialog = false
                        selectedRecordToDelete = null
                        // Kayıt silinince uyarı state'ini sıfırla (Toplam kalori değişecektir, bu LaunchedEffect'i tekrar tetikler)
                        hasShownCalorieWarningForCurrentState = false
                        showCalorieWarningDialog = false
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

        // *** YENİ DİYALOG: Kalori Aşımı Uyarısı ***
        if (showCalorieWarningDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCalorieWarningDialog = false
                    // hasShownCalorieWarningForCurrentState true kalır, aynı durum için tekrar göstermez
                },
                title = { Text("Calorie Warning", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }, // Hata rengi ve kalın font
                text = {
                    Column {
                        Text("You have exceeded your estimated daily calorie need!")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Calculated Need: $dailyCalculatedCalorieNeed kcal")
                        Text("Calories Consumed: $totalCalories kcal")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showCalorieWarningDialog = false
                        // hasShownCalorieWarningForCurrentState true kalır
                    }) {
                        Text("OK", color = MaterialTheme.colorScheme.error) // Buton rengi hata rengiyle uyumlu
                    }
                }
            )
        }
        // *******************************************

    } // Box Sonu
} // KeepNotePage Sonu