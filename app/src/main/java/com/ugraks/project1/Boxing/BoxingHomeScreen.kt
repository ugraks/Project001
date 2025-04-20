package com.ugraks.project1.Boxing


import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// Import the new BoxingItem and loadBoxingDataFromAssets
// Make sure your navigation routes are defined correctly, e.g., in Screens object
import com.ugraks.project1.AppNavigation.Screens // Assuming you have a Screens object for navigation

@Composable
fun BoxingMainScreen(navController: NavController, context: Context) {
    // Load boxing items using the new function
    val boxingItems = loadBoxingDataFromAssets(context)
    // Extract distinct categories from boxing items
    val boxingCategories = boxingItems.map { it.category }.distinct()
    // State to hold selected categories
    val selectedBoxingCategories = remember { mutableStateListOf<String>() }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔙 Back Button (Consider if this is needed on the main screen)
        IconButton(
            onClick = { navController.navigateUp() }, // This might exit the app or go to a previous screen
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(120.dp)) // Space between title and back button

            // Title
            Text(
                text = "Select Boxing Categories", // Changed title
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 26.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // Checkbox List for Boxing Categories
            boxingCategories.forEach { category -> // Iterate through boxing categories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedBoxingCategories.contains(category),
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedBoxingCategories.add(category)
                            } else {
                                selectedBoxingCategories.remove(category)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurface,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category, // Display category name
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }

            // Show Button
            Button(
                onClick = {
                    if (selectedBoxingCategories.isNotEmpty()) {
                        // Navigate to the detail list screen with selected categories
                        // Make sure your route creation function is correctly defined in Screens
                        val route = Screens.BoxingDetailListScreen.createRoute( // Updated screen name
                            selectedBoxingCategories.joinToString(",")
                        )
                        navController.navigate(route)
                    } else {
                        // Changed Toast message
                        Toast.makeText(
                            context,
                            "Please select at least one boxing category",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Show Boxing Items", // Changed button text
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
        }
    }
}