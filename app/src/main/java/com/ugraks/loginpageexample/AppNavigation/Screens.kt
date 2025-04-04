package com.ugraks.loginpageexample.AppNavigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screens(val route : String) {
    @Serializable
    object ScreenLoginPage : Screens("ScreenLoginPage")

    @Serializable
    object ForgotPassword  : Screens("ForgotPassword")

    @Serializable
    object NewPassword     : Screens("NewPassword")

    @Serializable
    object RegisterPage    : Screens("RegisterPage")

    @Serializable
    data class ScreenSignUpSuccess(
        val name: String,



    )


    @Serializable
    data class ScreenSignInSuccess(
        val name: String,



    )

    @Serializable
    data class ScreenHomePage(

        val name : String,


    )

    @Serializable
    data class ScreenPersonPage(
        val name : String,


    )




}







