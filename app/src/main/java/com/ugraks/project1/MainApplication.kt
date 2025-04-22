package com.ugraks.project1 // Kendi paket adınız

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // Bu annotation Hilt kod üretimini başlatır
class MyApp : Application() {
    // Herhangi ek bir logic varsa buraya ekleyebilirsiniz
    override fun onCreate() {
        super.onCreate()
        // Uygulama başlatılırken yapılacak işlemler
    }
}
