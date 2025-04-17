package com.ugraks.project1

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.ugraks.project1.AppNavigation.Screens
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

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                navController.navigate(Screens.PedoMeterScreen)
            } else {
                Toast.makeText(context, "Fiziksel aktivite izni reddedildi.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmationDialog by remember { mutableStateOf(false) }

    // 🔙 Geri tuşuna basınca Sign Out'u tetikle
    BackHandler {
        showSignOutConfirmationDialog = true
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
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
                        icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                        onClick = {
                            navController.navigate(ScreenPersonPage(email))
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Rate Our Application") },
                        selected = false,
                        icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                        onClick = {
                            navController.navigate("rating/$email")
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Delete Account") },
                        selected = false,
                        icon = { Icon(Icons.Filled.Close, contentDescription = null) },
                        onClick = {
                            showDeleteConfirmationDialog = true
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Sign Out") },
                        selected = false,
                        icon = { Icon(Icons.Filled.ArrowBack, contentDescription = null) },
                        onClick = {
                            showSignOutConfirmationDialog = true
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
                        badge = { Text("20") },
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
                    title = {
                        Text(
                            "Welcome $username",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge

                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) { // bu kısma rehber eklenecek, uygulama nasıl kullanılır diye.
                            Icon(Icons.Outlined.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)

                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ), modifier = Modifier.clip(RoundedCornerShape(bottomEnd = 30.dp, bottomStart = 30.dp))
                )
            },
            bottomBar = {
                NavigationBar(modifier = Modifier.clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))) {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Food Recipes",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("Food Recipes", fontSize = 11.sp) },
                        selected = false,
                        onClick = {
                            navController.navigate("recipeList/$email")
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.List,
                                contentDescription = "My Keep List",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("My Keep List", fontSize = 11.sp) },
                        selected = false,
                        onClick = {
                            navController.navigate(Screens.KeepNotePage.ROUTE.replace("{email}", email))
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Daily Calories",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("Daily Calories", fontSize = 11.sp) },
                        selected = false,
                        onClick = {
                            navController.navigate(DailyCaloriesPage)
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = "Food Calories",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("Food Calories", fontSize = 11.sp) },
                        selected = false,
                        onClick = {
                            navController.navigate(Screens.FoodSearchPage.route)
                        }
                    )
                }
            },

            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp) // Genel padding
                ) {
                    // 1. FAB
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screens.MainScreen.route)
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd) // Sağ altta hizalama
                            .padding(bottom = 80.dp) // Üst üste gelmesi için padding
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_fitness_center_24),
                            contentDescription = "Fitness"
                        )
                    }

                    // 2. FAB
                    FloatingActionButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.ACTIVITY_RECOGNITION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {

                                    navController.navigate(Screens.PedoMeterScreen)
                                } else {

                                    requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
                                }
                            } else {

                                navController.navigate(Screens.PedoMeterScreen)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd) // Sağ altta hizalama
                            .padding(bottom = 16.dp) // 2. FAB'yi biraz daha altta konumlandırıyoruz
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_directions_walk_24),
                            contentDescription = "Pedometer"
                        )
                    }
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
                Text(
                    text = "Main Content",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }

    // ✅ AlertDialogs
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account?") },
            confirmButton = {
                TextButton(onClick = {
                    deleteUserByEmail(context, email)
                    Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                    showDeleteConfirmationDialog = false
                    navController.navigate(ScreenLoginPage)
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmationDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showSignOutConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmationDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                    showSignOutConfirmationDialog = false
                    navController.navigate(ScreenLoginPage)
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSignOutConfirmationDialog = false
                }) { Text("Cancel") }
            }
        )
    }
}

@Preview
@Composable
fun HomePagePreview() {
    // HomePage(navController, args.name, args.surname)
}