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
    object ScreenRatingPage : Screens("RatingPage")

    @Serializable
    object NewPassword     : Screens("NewPassword")

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

        val email: String

    ) : Screens("ProfileEditPage")




}