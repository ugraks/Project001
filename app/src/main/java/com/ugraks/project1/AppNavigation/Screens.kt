package com.ugraks.project1.AppNavigation

import android.net.Uri
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
    object ExerciseListScreen : Screens("exercise_list_screen/{muscleGroupsString}") { // <-- Placeholder eklendi
        fun createRoute(muscleGroupsString: String): String {
            // Navigasyonda özel karakterler sorun yaratmasın diye encode etmek iyi bir fikirdir
            val encodedString = Uri.encode(muscleGroupsString) // android.net.Uri importu gerekebilir
            return "exercise_list_screen/$encodedString"
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
    object PedoMeterScreen : Screens("pedoMeterScreen")

    @Serializable
    object DailySummaryScreen : Screens("DailySummaryScreen")

    @Serializable
    object PedometerDailySummary : Screens("PedometerDailySummary")


    @Serializable
    object BoxingMainScreen : Screens("boxing_main_screen")
    @Serializable
    object BoxingDetailListScreen : Screens("boxing_detail_list_screen/{selectedCategoriesString}") { // <-- Argüman placeholder'ı
        // Navigasyon için route'u argümanla birlikte oluşturan yardımcı fonksiyon
        fun createRoute(selectedCategoriesString: String): String {
            // Argüman string'ini URL encode etmek, özel karakterler sorun yaratmasın diye iyi bir fikirdir.
            val encodedString = Uri.encode(selectedCategoriesString)
            return "boxing_detail_list_screen/$encodedString"
        }
    }










}

