package com.mystarnow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mystarnow.android.di.AndroidAppContainer
import com.mystarnow.shared.ui.navigation.MyStarNowApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = AndroidAppContainer(applicationContext)

        setContent {
            MyStarNowApp(
                services = container.appServices,
                onOpenExternalLink = container::openExternalLink,
            )
        }
    }
}
