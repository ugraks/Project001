@file:OptIn(ExperimentalMaterial3Api::class)

package com.ugraks.project1

import android.text.Layout
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ugraks.project1.Authenticate.clearFoodListFromFile
import com.ugraks.project1.Authenticate.deleteFoodFromFile
import com.ugraks.project1.Authenticate.loadFoodListFromFile
import com.ugraks.project1.Authenticate.saveFoodToFile
import kotlinx.coroutines.launch


@Composable
fun KeepNotePage(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val foodList = remember { mutableStateListOf<FoodItemKeepNote>() }
    var showFoodEntryDialog by remember { mutableStateOf(false) }
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedItemToDelete by remember { mutableStateOf<FoodItemKeepNote?>(null) }

    var foodName by remember { mutableStateOf("") }
    var foodAmount by remember { mutableStateOf("") }
    var foodTime by remember { mutableStateOf("") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface

    // Listeyi dosyadan yükle
    LaunchedEffect(email) {
        foodList.clear()
        foodList.addAll(loadFoodListFromFile(context, email))
    }

    fun addFood(foodItem: FoodItemKeepNote) {
        saveFoodToFile(context, email, foodItem)
        foodList.add(foodItem)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Üst menü: Geri ve Temizle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryColor
                    )
                }
                Text(
                    text = "My Keep List",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontStyle = FontStyle.Italic,
                        color = primaryColor
                    ),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { showClearConfirmationDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Clear List",
                        tint = primaryColor
                    )
                }
            }

            // Liste
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(foodList) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceColor,
                            contentColor = contentColor
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.name} - ${item.amount} - ${item.time}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                fontStyle = FontStyle.Italic
                            )

                            IconButton(
                                onClick = {
                                    selectedItemToDelete = item
                                    showDeleteDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB (Add Food)
        FloatingActionButton(
            onClick = { showFoodEntryDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 48.dp, end = 24.dp), // FAB daha yukarıda
            containerColor = primaryColor
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Add Food", tint = Color.White)
        }

        // Add Food Dialog
        if (showFoodEntryDialog) {
            AlertDialog(
                onDismissRequest = { showFoodEntryDialog = false },
                title = { Text("Add New Food", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = {
                    Column {
                        TextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            label = { Text("Food Name") },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = primaryColor,
                                unfocusedIndicatorColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = foodAmount,
                            onValueChange = { foodAmount = it },
                            label = { Text("Food Amount") },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = primaryColor,
                                unfocusedIndicatorColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = foodTime,
                            onValueChange = { foodTime = it },
                            label = { Text("Time (e.g. 12:30 PM)") },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = primaryColor,
                                unfocusedIndicatorColor = Color.Gray
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (foodName.isNotEmpty() && foodAmount.isNotEmpty() && foodTime.isNotEmpty()) {
                            val newFood = FoodItemKeepNote(foodName, foodAmount, foodTime)
                            addFood(newFood)
                            foodName = ""
                            foodAmount = ""
                            foodTime = ""
                            showFoodEntryDialog = false
                        }
                    }) {
                        Text("Add", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFoodEntryDialog = false }) {
                        Text("Cancel", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                }
            )
        }

        // Clear List Dialog
        if (showClearConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmationDialog = false },
                title = { Text("Clear List", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to clear the entire list?") },
                confirmButton = {
                    TextButton(onClick = {
                        clearFoodListFromFile(context, email)
                        foodList.clear()
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

        // Delete Item Dialog
        if (showDeleteDialog && selectedItemToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    selectedItemToDelete = null
                },
                title = { Text("Remove Food", color = primaryColor, fontStyle = FontStyle.Italic) },
                text = { Text("Are you sure you want to delete this item?") },
                confirmButton = {
                    TextButton(onClick = {
                        deleteFoodFromFile(context, email, selectedItemToDelete!!)
                        foodList.remove(selectedItemToDelete)
                        showDeleteDialog = false
                        selectedItemToDelete = null
                    }) {
                        Text("Yes", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        selectedItemToDelete = null
                    }) {
                        Text("Cancel", color = primaryColor, fontStyle = FontStyle.Italic)
                    }
                }
            )
        }
    }
}

data class FoodItemKeepNote(val name: String, val amount: String, val time: String)

@Composable
fun FoodListItem(foodItem: FoodItemKeepNote, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onRemove() }
    ) {
        Text(
            text = "${foodItem.name} - ${foodItem.amount} - ${foodItem.time}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium
        )
    }
}