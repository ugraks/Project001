package com.ugraks.project1.AppNavigation

import StepCounterPage
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



@Composable
fun SayfaGecisleri() {
    val navController = rememberNavController()
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
            MainScreen(navController = navController, context = context)
        }

        composable(Screens.ExerciseListScreen.route) { backStackEntry ->
            val muscleGroupsParam = backStackEntry.arguments?.getString("muscleGroups") ?: ""
            val muscleGroups = muscleGroupsParam.split(",")
            ExerciseListScreen(
                navController = navController,
                muscleGroups = muscleGroups,
                exercises = loadExercisesFromAssets(context)
            )
        }

        composable<PedoMeterScreen> {
            StepCounterPage(navController)

        }

        composable<Screens.DailySummaryScreen> {
            DailySummariesPage(context = LocalContext.current,navController)

        }

        composable<Screens.PedometerDailySummary> {
            DailySummaryPage(navController)

        }



















    }





}