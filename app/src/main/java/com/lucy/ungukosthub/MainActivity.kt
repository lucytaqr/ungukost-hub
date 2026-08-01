package com.lucy.ungukosthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lucy.ungukosthub.domain.repository.AuthRepository
import com.lucy.ungukosthub.presentation.navigation.AppNavigation
import com.lucy.ungukosthub.presentation.theme.UnguKostHubTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnguKostHubTheme {
                AppNavigation(authRepository = authRepository)
            }
        }
    }
}