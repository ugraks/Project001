package com.ugraks.project1.Foods // Kendi paket adınız


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect // ViewModel'dan veri geldiğinde quantity'yi sıfırlamak/güncellemek için gerekebilir
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.ugraks.project1.data.local.entity.FoodItemEntity
import com.ugraks.project1.ui.viewmodels.FoodViewModel.FoodCaloriesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCaloriesScreen(
    // foodItemName navigasyon argümanı Composable fonksiyon parametresinde kalır
    // Ancak değeri doğrudan ViewModel tarafından SavedStateHandle ile alınır.
    foodItemName: String, // Navigasyondan gelen isim
    navController: NavController,
    // YENİ: FoodCaloriesViewModel'ı Hilt ile inject et
    viewModel: FoodCaloriesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val density = androidx.compose.ui.platform.LocalDensity.current // Density'yi doğru paketinden alın
    val scrollState = rememberScrollState()


    // ESKİ: val foodItems = remember { readAndParseItemsFromAssets(context) } // KALDIRILDI
    // ESKİ: val selectedItem = foodItems.find { it.name == foodItemName } // KALDIRILDI

    // YENİ: ViewModel'dan seçilen yemek öğesini reaktif olarak izle
    // selectedFoodItem Flow'u FoodItemEntity? yayınlıyor
    val selectedItemEntity by viewModel.selectedFoodItem.collectAsState() // YENİ: ViewModel'dan gelen Entity (FoodItemEntity?)


    // Quantity ve birim seçim state'leri UI state'i olarak Composable içinde kalır
    var quantity by remember { mutableStateOf("0") }
    // Başlangıç birimi, seçilen öğenin tipine göre belirlenmeli
    // selectedItemEntity geldiğinde LaunchedEffect içinde ayarlanabilir.
    var isKg by remember { mutableStateOf(false) }
    var isLiters by remember { mutableStateOf(false) }

    // Besin değeri hesaplama state'leri UI state'i olarak Composable içinde kalır
    var totalCalories by remember { mutableStateOf(0) }
    var totalProtein by remember { mutableStateOf(0.0) }
    var totalFat by remember { mutableStateOf(0.0) }
    var totalCarb by remember { mutableStateOf(0.0) }

    // Hesaplama fonksiyonu ve tetiklenmesi UI katmanında kalır
    val updateNutritionalValues: (Int, Boolean, Boolean, FoodItemEntity?) -> Unit = { qty, kg, liters, item ->
        // calculateNutritionalValues fonksiyonu FoodItem data class'ı alıyor.
        // Buraya ya FoodItemEntity'yi FoodItem'a çevireceğiz ya da
        // calculateNutritionalValues fonksiyonunu FoodItemEntity alacak şekilde güncelleyeceğiz.
        // En basiti calculateNutritionalValues fonksiyonunu Entity alacak şekilde güncelleyelim.
        // calculateNutritionalValues fonksiyonunuzu açıp FoodItem parametresini FoodItemEntity olarak değiştirin.

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

    // selectedItemEntity (ViewModel'dan gelen veri) değiştiğinde veya quantity değiştiğinde
    // besin değerlerini yeniden hesapla ve birim seçimini ayarla.
    LaunchedEffect(selectedItemEntity, quantity) {
        selectedItemEntity?.let { item ->
            // Öğe tipi geldiğinde birim state'lerini ayarla
            if (item.type == "Food") {
                isKg = true // Varsayılan olarak kg seçili gelsin
                isLiters = false
            } else if (item.type == "Drink") {
                isLiters = true // Varsayılan olarak Lt seçili gelsin
                isKg = false
            } else {
                isKg = false
                isLiters = false
            }

            // Quantity'ye göre besin değerlerini hesapla
            val initialQuantityInt = quantity.toIntOrNull() ?: 0
            // YENİ: calculateNutritionalValues fonksiyonu artık FoodItemEntity almalı
            updateNutritionalValues(initialQuantityInt, isKg, isLiters, item)
        }
        // Eğer selectedItemEntity null olursa (örneğin öğe bulunamadıysa), değerler 0 kalır.
        if (selectedItemEntity == null) {
            quantity = "0" // Miktar alanını sıfırla
            isKg = false
            isLiters = false
            updateNutritionalValues(0, false, false, null) // Değerleri sıfırla
        }
    }

    // selectedItemEntity null değilse (yani öğe Room'dan başarıyla bulunduysa) içeriği göster
    if (selectedItemEntity != null) {
        val currentSelectedItem = selectedItemEntity!! // Null olmadığını biliyoruz

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Your Food Information:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                // YENİ: getImageResource fonksiyonu FoodItemEntity değil String isim alıyor, aynı kalır
                val imageResId = getImageResource(currentSelectedItem.name)
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "${currentSelectedItem.name} image", // YENİ: Entity'nin ismini kullan
                    modifier = Modifier
                        .size(180.dp)
                        .padding(bottom = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Text(
                    text = currentSelectedItem.name, // YENİ: Entity'nin ismini kullan
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        val newValue = it.filter { char -> char.isDigit() }
                        quantity = newValue
                        // Besin değerleri hesaplama LaunchEffect içinde tetikleniyor (quantity değişince)
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
                    // Öğe null değilse TextField etkin
                    enabled = currentSelectedItem != null
                )

                Spacer(modifier = Modifier.height(16.dp))
                // YENİ: currentSelectedItem.type kullanın
                if (currentSelectedItem.type == "Food" || currentSelectedItem.type == "Drink") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentSelectedItem.type == "Food") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isKg,
                                    onClick = {
                                        isKg = true
                                        isLiters = false
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        // YENİ: calculateNutritionalValues fonksiyonu FoodItemEntity almalı
                                        updateNutritionalValues(quantityInt, isKg, isLiters, currentSelectedItem)
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
                                    selected = !isKg, // gr seçiliyse Kg seçili değil
                                    onClick = {
                                        isKg = false
                                        isLiters = false
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        // YENİ: calculateNutritionalValues fonksiyonu FoodItemEntity almalı
                                        updateNutritionalValues(quantityInt, isKg, isLiters, currentSelectedItem)
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
                                        // YENİ: calculateNutritionalValues fonksiyonu FoodItemEntity almalı
                                        updateNutritionalValues(quantityInt, isKg, isLiters, currentSelectedItem)
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
                                    selected = !isLiters, // ml seçiliyse Lt seçili değil
                                    onClick = {
                                        isLiters = false
                                        isKg = false
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        // YENİ: calculateNutritionalValues fonksiyonu FoodItemEntity almalı
                                        updateNutritionalValues(quantityInt, isKg, isLiters, currentSelectedItem)
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

                // Besin bilgileri (calculateNutritionalValues hala burada çağrılır)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nutritional Info:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Calories: $totalCalories kcal",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Display Macros only if calculated (quantity > 0)
                    if (quantity.toIntOrNull() ?: 0 > 0) {
                        Text(
                            text = "Protein: ${totalProtein.roundToInt()} g",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Fat: ${totalFat.roundToInt()} g",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Carbs: ${totalCarb.roundToInt()} g",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // Optional: Add "Add to Log" button here if needed (Bu ViewModel'da yok, başka ViewModel veya Repository gerekir)
                // Spacer(modifier = Modifier.height(32.dp))
                // Button(...)
            }

            // Back button aynı kalır
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 24.dp, start = 8.dp)
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
        // selectedItemEntity null ise (veri henüz yüklenmediyse veya bulunamadıysa)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val message = if (foodItemName.isNullOrBlank()) {
                "No food item name provided." // Navigasyon argümanı yoksa
            } else {
                "Loading item '$foodItemName' or item not found." // Yükleniyor veya bulunamadıysa
            }
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            // Yükleniyor göstergesi eklenebilir
            /*
            if (!foodItemName.isNullOrBlank()) { // İsim geçerliyse yükleniyor olabilir
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            */
        }
    }
}