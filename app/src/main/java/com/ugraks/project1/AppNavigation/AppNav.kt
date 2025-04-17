package com.ugraks.project1.AppNavigation

import StepCounterPage
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.ugraks.project1.AppNavigation.Screens.DailyCaloriesPage
import com.ugraks.project1.AppNavigation.Screens.ForgotPassword
import com.ugraks.project1.AppNavigation.Screens.PedoMeterScreen
import com.ugraks.project1.AppNavigation.Screens.RegisterPage
import com.ugraks.project1.AppNavigation.Screens.ScreenHomePage
import com.ugraks.project1.AppNavigation.Screens.ScreenLoginPage
import com.ugraks.project1.AppNavigation.Screens.ScreenPersonPage
import com.ugraks.project1.AppNavigation.Screens.ScreenProfileEditPage
import com.ugraks.project1.DailyCalories
import com.ugraks.project1.ExerciseListScreen
import com.ugraks.project1.FirstLoginPage
import com.ugraks.project1.FoodCaloriesScreen
import com.ugraks.project1.FoodSearchScreen
import com.ugraks.project1.ForgotPassword
import com.ugraks.project1.HomePage
import com.ugraks.project1.KeepNotePage
import com.ugraks.project1.MainScreen
import com.ugraks.project1.PersonPage
import com.ugraks.project1.ProfileEditPage
import com.ugraks.project1.RatingPage
import com.ugraks.project1.RecipeDetailScreen
import com.ugraks.project1.RecipeListScreen
import com.ugraks.project1.RegisterPage
import com.ugraks.project1.loadExercisesFromAssets


@Composable
fun SayfaGecisleri() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = ScreenLoginPage) {
        composable<ScreenLoginPage> {

            FirstLoginPage(navController)
        }

        composable<RegisterPage> {

            RegisterPage(navController)
        }


        composable<ForgotPassword> {
            ForgotPassword(navController)

        }


        composable<ScreenHomePage> {
            val args = it.toRoute<ScreenHomePage>()
            HomePage(navController,args.username,args.email)



        }
        composable<ScreenPersonPage>{
            val args = it.toRoute<ScreenPersonPage>()
            PersonPage(navController,args.email)


        }
        composable<DailyCaloriesPage> {

            DailyCalories(navController)

        }

        composable("rating/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            RatingPage(navController = navController, email = email)
        }

        composable<ScreenProfileEditPage>{
            val args = it.toRoute<ScreenProfileEditPage>()
            ProfileEditPage(navController,args.email)


        }

        composable(Screens.FoodSearchPage.route) {
            FoodSearchScreen(navController) // Arama ekranı
        }

        composable(Screens.FoodCaloriesPage.ROUTE) { backStackEntry ->
            val foodItemName = backStackEntry.arguments?.getString("foodItemName") ?: ""
            FoodCaloriesScreen(foodItemName = foodItemName, navController = navController) // Detay ekranı
        }

        composable(Screens.KeepNotePage.ROUTE) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            KeepNotePage(navController, email) // KeepNotePage yönlendirme
        }

        composable("recipeList/{userEmail}") { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("userEmail") ?: ""
            RecipeListScreen(navController, userEmail)
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















    }





}