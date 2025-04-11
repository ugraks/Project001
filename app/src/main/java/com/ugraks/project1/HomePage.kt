package com.ugraks.project1

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ugraks.project1.AppNavigation.Screens.DailyCaloriesPage
import com.ugraks.project1.AppNavigation.Screens.ScreenLoginPage
import com.ugraks.project1.AppNavigation.Screens.ScreenPersonPage
import com.ugraks.project1.AppNavigation.Screens.ScreenRatingPage
import com.ugraks.project1.Authenticate.deleteUserByEmail
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavHostController, username: String, email: String) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Menu",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()

                    Text(
                        "Account",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    NavigationDrawerItem(
                        label = { Text("Your Profile") },
                        selected = false,
                        icon = {Icon(Icons.Filled.AccountCircle, contentDescription = null)},
                        onClick = {
                            navController.navigate(ScreenPersonPage(email))


                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Rate Our Application") },
                        selected = false,
                        icon = {Icon(Icons.Filled.Star, contentDescription = null)},
                        onClick = {
                            navController.navigate(ScreenRatingPage)
                        }
                    )
                    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
                    NavigationDrawerItem(
                        label = { Text("Delete Account") },
                        selected = false,
                        icon = {Icon(Icons.Filled.Close, contentDescription = null)},
                        onClick = {
                            showDeleteConfirmationDialog = true
                        }
                    )
                    if (showDeleteConfirmationDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showDeleteConfirmationDialog = false
                            },
                            title = { Text("Delete Account") },
                            text = { Text("Are you sure you want to delete your account?") },
                            confirmButton = {
                                TextButton(onClick = {

                                    deleteUserByEmail(context, email) // Fonksiyon az sonra
                                    Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                                    showDeleteConfirmationDialog = false
                                    navController.navigate(ScreenLoginPage)
                                }) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showDeleteConfirmationDialog = false
                                }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    NavigationDrawerItem(
                        label = { Text("Sign Out") },
                        selected = false,
                        icon = {Icon(Icons.Filled.ArrowBack, contentDescription = null)},
                        onClick = {
                            navController.navigate(ScreenLoginPage)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        "General Options",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        badge = { Text("20") }, // Placeholder
                        onClick = { /* Handle click */ }
                    )
                    NavigationDrawerItem(
                        label = { Text("Help and feedback") },
                        selected = false,
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        onClick = { /* Handle click */ },
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        drawerState = drawerState,
        gesturesEnabled = true

    ) {

        Scaffold(


            topBar = {


                TopAppBar(
                    title = { Text("Welcome ${username}", color = Color.Black) },
                    navigationIcon = {
                        IconButton(onClick = {

                            scope.launch {
                                drawerState.open() // Drawer'ı aç
                            }

                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                )

            },


            bottomBar = {

                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search")

                        },
                        label = { Text("Search") },
                        selected = false,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = "Profile") },
                        label = { Text("Daily Calories") },
                        selected = false,
                        onClick = {
                            navController.navigate(DailyCaloriesPage)


                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Info, contentDescription = "Profile") },
                        label = { Text("Food Calories") },
                        selected = false,
                        onClick = { }
                    )
                }

            },
            floatingActionButton = {

                FloatingActionButton(
                    onClick = {}
                    , containerColor = Color.Magenta
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }

            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Content
                Text(
                    text = "Main Content",
                    style = MaterialTheme.typography.headlineMedium
                )


            }


        }


    }


}
@Preview
@Composable

fun HomePagePreview() {

    //HomePage(navController, args.name, args.surname)


}