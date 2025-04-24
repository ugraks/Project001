package com.ugraks.project1.AppNavigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ugraks.project1.AppNavigation.Screens.DailyCaloriesPage
import com.ugraks.project1.AppNavigation.Screens.KeepNotePage
import com.ugraks.project1.AppNavigation.Screens.PedoMeterScreen
import com.ugraks.project1.AppNavigation.Screens.RecipeList
import com.ugraks.project1.AppNavigation.Screens.ScreenHomePage
import com.ugraks.project1.Boxing.BoxingDetailListScreen
import com.ugraks.project1.Boxing.BoxingMainScreen
import com.ugraks.project1.Boxing.loadBoxingDataFromAssets
import com.ugraks.project1.DailyCalorie.DailyCalories
import com.ugraks.project1.Fitness.ExerciseListScreen
import com.ugraks.project1.Foods.FoodCaloriesScreen
import com.ugraks.project1.Foods.FoodSearchScreen
import com.ugraks.project1.Home.HomePage
import com.ugraks.project1.KeepNotePage
import com.ugraks.project1.Fitness.MainScreen
import com.ugraks.project1.Recipes.RecipeDetailScreen
import com.ugraks.project1.Recipes.RecipeListScreen
import com.ugraks.project1.Fitness.loadExercisesFromAssets
import com.ugraks.project1.KeepNoteComposable.DailySummariesPage
import com.ugraks.project1.Pedometerr.DailySummaryPage
import androidx.navigation.NavHostController
import com.ugraks.project1.StepCounterPage


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SayfaGecisleri(navController: NavHostController) {

    val context = LocalContext.current

    NavHost(navController = navController, startDestination = ScreenHomePage) {



        composable<ScreenHomePage> {

            HomePage(navController)



        }
        composable<DailyCaloriesPage> {

            DailyCalories(navController)

        }





        composable(Screens.FoodSearchPage.route) {
            FoodSearchScreen(navController) // Arama ekranı
        }

        composable(Screens.FoodCaloriesPage.ROUTE) { backStackEntry ->
            val foodItemName = backStackEntry.arguments?.getString("foodItemName") ?: ""
            FoodCaloriesScreen(foodItemName = foodItemName, navController = navController) // Detay ekranı
        }

        composable<KeepNotePage> {
            KeepNotePage(navController)
        }

        composable<RecipeList> {

            RecipeListScreen(navController)
        }


        composable(
            route = "recipeDetail/{recipeName}",
            arguments = listOf(navArgument("recipeName") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("recipeName") ?: ""
            RecipeDetailScreen(recipeName = name, navController = navController)
        }


        composable(Screens.MainScreen.route) {
            MainScreen(navController = navController)
        }



        composable(

            route = Screens.ExerciseListScreen.route,
            arguments = listOf(
                navArgument("muscleGroupsString") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val muscleGroupsString = backStackEntry.arguments?.getString("muscleGroupsString")

            // Koma ile ayrılmış String'i List<String>'e dönüştürüyoruz
            // Eğer string null veya boşsa boş bir liste döndürürüz
            val muscleGroups = muscleGroupsString
                ?.split(",") // Koma ile ayır
                ?.filter { it.isNotEmpty() } // Boş stringleri temizle (örneğin sonunda fazladan virgül varsa)
                ?: emptyList() // Eğer muscleGroupsString null ise boş liste ver

            // ExerciseListScreen Composable'ını çağırırken parametreleri iletiyoruz:
            ExerciseListScreen(
                navController = navController, // NavController'ı ilet
                muscleGroups = muscleGroups // Parsed (dönüştürülmüş) List<String>'i ilet

            )
        }

        composable<PedoMeterScreen> {
            StepCounterPage(navController)

        }

        composable<Screens.DailySummaryScreen> {
            DailySummariesPage(navController)

        }

        composable<Screens.PedometerDailySummary> {
            DailySummaryPage(navController)

        }

        composable(Screens.BoxingMainScreen.route) {
            BoxingMainScreen(navController = navController, context = context) // Boks ana ekranı
        }

        composable(
            route = Screens.BoxingDetailListScreen.route, // Rota tanımı
            arguments = listOf(navArgument("selectedCategories") { type = NavType.StringType }) // Argüman tanımı
        ) { backStackEntry ->
            // Argümanı al ve listeye dönüştür
            val selectedCategoriesString = backStackEntry.arguments?.getString("selectedCategories") ?: ""
            val selectedCategoriesList = selectedCategoriesString.split(",").filter { it.isNotBlank() }

            // Boks verisini burada yükle (Mevcut ExerciseListScreen örneği gibi)
            val allBoxingItems = loadBoxingDataFromAssets(context)

            // Boks detay listesi ekranını çağır
            BoxingDetailListScreen(
                navController = navController,
                selectedCategories = selectedCategoriesList,
                allBoxingItems = allBoxingItems // Yüklenen veriyi ekrana ilet
            )
        }



















    }





}