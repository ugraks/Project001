package com.ugraks.project1.AppNavigation

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

@Serializable
sealed class Screens(val route : String) {

    @Serializable
    object DailyCaloriesPage : Screens("DailyCaloriesPage")


    @Serializable
    object ScreenHomePage : Screens("ScreenHomePage")



    @Serializable
    object FoodSearchPage : Screens("FoodSearchPage")

    @Serializable
    data class FoodCaloriesPage(val foodItemName: String) : Screens("FoodCaloriesPage/{foodItemName}") {
        companion object {
            const val ROUTE = "FoodCaloriesPage/{foodItemName}"
        }
    }

    @Serializable
    object KeepNotePage : Screens("KeepNotePage")

    @Serializable
    object RecipeList : Screens("RecipeList")

    @Serializable
    data class RecipeDetail(val recipeName: String) : Screens("recipeDetail/$recipeName") {
        companion object {
            const val ROUTE = "recipeDetail/{recipeName}"
        }
    }

    @Serializable
    object MainScreen : Screens("mainScreen")
    @Serializable
    object ExerciseListScreen : Screens("exerciseListScreen/{muscleGroups}") {
        fun createRoute(muscleGroups: String) = "exerciseListScreen/$muscleGroups"
    }

    @Serializable
    object PedoMeterScreen : Screens("pedoMeterScreen")

    @Serializable
    object DailySummaryScreen : Screens("DailySummaryScreen")

    @Serializable
    object PedometerDailySummary : Screens("PedometerDailySummary")


    @Serializable
    object BoxingMainScreen : Screens("boxingMainScreen")
    @Serializable
    object BoxingDetailListScreen : Screens("boxingDetailListScreen/{selectedCategories}") {
        fun createRoute(selectedCategories: String) = "boxingDetailListScreen/$selectedCategories"}










}

