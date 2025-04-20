package com.ugraks.project1.Boxing


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// Import the new BoxingItem, loadBoxingDataFromAssets, and getBoxingImageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// Make sure your navigation routes are defined correctly, e.g., in Screens object
import com.ugraks.project1.R // Assuming R is accessible

@Composable
fun BoxingDetailListScreen(
    navController: NavController,

    selectedCategories: List<String>,

    allBoxingItems: List<BoxingItem>
) {
    // Filter boxing items based on the selected categories
    val filteredBoxingItems = allBoxingItems.filter { it.category in selectedCategories }
    // State to track the expanded item
    val expandedItem = remember { mutableStateOf<BoxingItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔙 Back Button
        IconButton(
            onClick = { navController.navigateUp() },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 90.dp) // Space for back button and title
        ) {

            // 🏷 Title
            Text(
                text = "${selectedCategories.joinToString(", ")}", // Changed title
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp) // Space between title and cards
            )

            // 📋 Boxing Item Cards
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(filteredBoxingItems) { _, item -> // Iterate through filtered boxing items
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp) // Space between cards
                            .clickable {
                                // Toggle expanded state
                                expandedItem.value = if (expandedItem.value == item) null else item
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Card shadow
                        shape = RoundedCornerShape(16.dp) // Rounded corners
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Boxing Item Name
                            Text(
                                text = item.name, // Display item name
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            // Image or Icon
                            // Use the new getBoxingImageResource function
                            val currentImageResource = if (expandedItem.value == item) {
                                // If card is expanded, try to get specific image
                                getBoxingImageResource(item.name) // Use item.name
                            } else {
                                // If card is collapsed, show default icon
                                R.drawable.baseline_sports_martial_arts_24 // Still using placeholder icon
                            }

                            Image(
                                painter = painterResource(id = currentImageResource),
                                contentDescription = if (expandedItem.value == item) item.name else "Boxing Icon",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                // contentScale = ContentScale.Crop // Optional: Scale type
                            )

                            // Boxing Item Details (Animated Visibility)
                            AnimatedVisibility(visible = expandedItem.value == item) {
                                Column {
                                    // Display Category
                                    Text(
                                        text = "Category: ${item.category}", // Display category
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    // Display Description
                                    Text(
                                        text = "Description: ${item.description}", // Display description
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    // Display Details / How-to
                                    Text(
                                        text = "Details:", // Changed label
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // Display details, potentially split by a delimiter if needed
                                    // Assuming details might contain multiple steps/points
                                    item.details.split(" | ").forEach { step -> // Assuming | is still the delimiter
                                        Text(
                                            text = step.trim(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    // Or if details is just one block of text:
                                    // Text(
                                    //     text = item.details,
                                    //     style = MaterialTheme.typography.bodyMedium.copy(
                                    //         color = MaterialTheme.colorScheme.onBackground
                                    //     ),
                                    //     modifier = Modifier.padding(bottom = 4.dp)
                                    // )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}