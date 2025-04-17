package com.ugraks.project1

import android.content.Context
import android.graphics.drawable.shapes.OvalShape
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import java.io.BufferedReader
import java.io.InputStreamReader

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class FoodItem(
    val name: String,       // Yiyeceğin adı
    val calories: Int,      // Yiyeceğin kalorisi
    val type: String        // Yiyeceğin türü (Food veya Drink)
)


fun readItemsFromAssets(context: Context): List<String> {
    val itemsList = mutableListOf<String>()

    // Assets klasöründen dosyayı açalım
    val inputStream = context.assets.open("items.txt")
    val reader = BufferedReader(InputStreamReader(inputStream))

    // Dosyadaki her satırı listeye ekleyelim
    reader.forEachLine { line -> itemsList.add(line) }
    reader.close()

    return itemsList
}


fun parseItemData(itemsList: List<String>): List<FoodItem> {
    val foodItems = mutableListOf<FoodItem>()

    for (item in itemsList) {
        val parts = item.split(",") // Satırı ',' ile ayırıyoruz
        if (parts.size == 3) {
            val name = parts[0].split(":")[1].trim() // Adı alıyoruz
            val calories = parts[1].split(":")[1].trim().toInt() // Kalori kısmını alıyoruz
            val type = parts[2].split(":")[1].trim() // Türü alıyoruz

            foodItems.add(FoodItem(name, calories, type)) // FoodItem listesine ekliyoruz
        }
    }

    return foodItems
}

fun calculateCalories(item: FoodItem, quantity: Int, isKg: Boolean, isLiters: Boolean): Int {
    return when {
        item.type == "Food" -> {
            // Eğer yiyecekse ve kg seçilmişse
            if (isKg) {
                // 1 kg başına kalori verilmişse, doğrudan kalori hesaplanır
                item.calories * quantity
            } else {
                // Eğer gram seçildiyse, kaloriyi 1000'e bölüp gram cinsinden hesaplıyoruz
                // Burada gram başına doğru kalori hesaplanması yapılacak
                val caloriesPerGram = item.calories / 1000.0 // 1 gram için kalori hesaplanır
                (caloriesPerGram * quantity).toInt()  // Sonuç bir tam sayıya dönüştürülür
            }
        }
        item.type == "Drink" -> {
            // Eğer içecekse ve litre seçilmişse
            if (isLiters) {
                // 1 litre başına kalori verilmişse, doğrudan kalori hesaplanır
                item.calories * quantity
            } else {
                // Eğer mililitre seçildiyse, kaloriyi 1000'e bölüp mililitre cinsinden hesaplıyoruz
                val caloriesPerMl = item.calories / 1000.0 // 1 mililitre için kalori hesaplanır
                (caloriesPerMl * quantity).toInt()  // Sonuç bir tam sayıya dönüştürülür
            }
        }
        else -> item.calories
    }
}

@Composable
fun getImageResource(foodName: String): Int {
    // Görsellerin ismini küçük harfe çeviriyoruz ve boşlukları kaldırıyoruz
    val imageName = foodName.lowercase().replace(" ", "_")

    // Kaynağa ait resim dosyasını buluyoruz
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)

    // Eğer resim dosyası bulunursa, o resmi döndürüyoruz, bulamazsak placeholder görselini döndürüyoruz
    return if (resId != 0) resId else R.drawable.ic_launcher_background

}








@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCaloriesScreen(foodItemName: String, navController: NavController) {
    val context = LocalContext.current
    val itemsList = readItemsFromAssets(context)
    val foodItems = parseItemData(itemsList)

    val selectedItem = foodItems.find { it.name == foodItemName }

    if (selectedItem != null) {
        // State variables to hold input values
        var quantity by remember { mutableStateOf("0") } // Default value is "0"
        var isLiters by remember { mutableStateOf(false) }
        var isKg by remember { mutableStateOf(false) }
        var totalCalories by remember { mutableStateOf(0) } // Default total calories is 0

        // Box for the back button at the top left
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go Back",
                    tint = MaterialTheme.colorScheme.primary, // Tema uyumlu renk
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Adjust for status bar padding
        val topPadding = WindowInsets.statusBars
            .asPaddingValues().calculateTopPadding()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding + 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add Spacer for more space between top and title
            Spacer(modifier = Modifier.height(60.dp))  // Increase this value for more space

            // Header - "Enter Your Food Information"
            Text(
                text = "Enter Your Food Information:",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Cursive,
                modifier = Modifier.padding(bottom = 20.dp),
                color = MaterialTheme.colorScheme.primary // Tema uyumlu renk
            )

            // Image based on food name
            val imageResId = getImageResource(selectedItem.name)  // get image resource ID
            Image(
                painter = painterResource(id = imageResId),  // Load the image
                contentDescription = "${selectedItem.name} image",
                modifier = Modifier
                    .size(150.dp)  // Adjust size of image to fit well
                    .padding(bottom = 20.dp)  // Add some space between image and title
                     // Yuvarlak köşeler
                    .align(Alignment.CenterHorizontally)
            )

            // Show the selected food item name
            Text(
                text = "${selectedItem.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(bottom = 20.dp),
                color = MaterialTheme.colorScheme.primary // Tema uyumlu renk
            )

            // Quantity input field
            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    quantity = it
                    // Recalculate calories dynamically whenever the quantity changes
                    if (it.isNotEmpty()) {
                        val quantityInt = it.toIntOrNull() ?: 0
                        totalCalories = calculateCalories(selectedItem, quantityInt, isKg, isLiters)
                    } else {
                        // If quantity is empty, set calories to 0
                        totalCalories = 0
                    }
                },
                label = { Text("Quantity") },
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground) // Tema uyumlu yazı rengi
            )

            // Unit selection: kg/gr or ml/Lt based on food or drink type
            Spacer(modifier = Modifier.height(16.dp))
            if (selectedItem.type == "Food") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // "kg" seçeneği
                    RadioButton(
                        selected = isKg,
                        onClick = {
                            isKg = true
                            val quantityInt = quantity.toIntOrNull() ?: 0
                            totalCalories = calculateCalories(selectedItem, quantityInt, isKg, isLiters)
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary, // Seçili olduğunda renk
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Seçili olmadığında renk
                        )
                    )
                    Text("kg")

                    // "gr" seçeneği
                    RadioButton(
                        selected = !isKg,
                        onClick = {
                            isKg = false
                            val quantityInt = quantity.toIntOrNull() ?: 0
                            totalCalories = calculateCalories(selectedItem, quantityInt, isKg, isLiters)
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary, // Seçili olduğunda renk
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Seçili olmadığında renk
                        )
                    )
                    Text("gr")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // "Lt" seçeneği
                    RadioButton(
                        selected = isLiters,
                        onClick = {
                            isLiters = true
                            val quantityInt = quantity.toIntOrNull() ?: 0
                            totalCalories = calculateCalories(selectedItem, quantityInt, isKg, isLiters)
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary, // Seçili olduğunda renk
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Seçili olmadığında renk
                        )
                    )
                    Text("Lt")

                    // "ml" seçeneği
                    RadioButton(
                        selected = !isLiters,
                        onClick = {
                            isLiters = false
                            val quantityInt = quantity.toIntOrNull() ?: 0
                            totalCalories = calculateCalories(selectedItem, quantityInt, isKg, isLiters)
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary, // Seçili olduğunda renk
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Seçili olmadığında renk
                        )
                    )
                    Text("ml")
                }
            }

            // Display the result dynamically based on the input
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Calories: $totalCalories kcal",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Text("Item not found")
    }
}


