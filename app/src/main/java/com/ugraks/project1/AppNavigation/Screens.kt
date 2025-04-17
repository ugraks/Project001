package com.ugraks.project1.AppNavigation

import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

@Serializable
sealed class Screens(val route : String) {
    @Serializable
    object ScreenLoginPage : Screens("ScreenLoginPage")

    @Serializable
    object ForgotPassword  : Screens("ForgotPassword")

    @Serializable
    object ScreenRatingPage : Screens("rating/{email}")


    @Serializable
    object RegisterPage    : Screens("RegisterPage")

    @Serializable
    object DailyCaloriesPage : Screens("DailyCaloriesPage")


    @Serializable
    data class ScreenHomePage(

        val username : String,
        val email : String

        )

    @Serializable
    data class ScreenPersonPage(

        val email: String

    ) : Screens("PersonPage")

    @Serializable
    data class ScreenProfileEditPage(
        val email: String) : Screens("ProfileEditPage")

    @Serializable
    object FoodSearchPage : Screens("FoodSearchPage")

    @Serializable
    data class FoodCaloriesPage(val foodItemName: String) : Screens("FoodCaloriesPage/{foodItemName}") {
        companion object {
            const val ROUTE = "FoodCaloriesPage/{foodItemName}"
        }
    }

    @Serializable
    data class KeepNotePage(
        val email: String
    ) : Screens("KeepNotePage/{email}") {
        companion object {
            const val ROUTE = "KeepNotePage/{email}"
        }
    }

    @Serializable
    data class RecipeList(val userEmail: String) : Screens("recipeList/{userEmail}")

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












}

