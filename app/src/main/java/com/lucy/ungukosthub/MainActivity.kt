package com.lucy.ungukosthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lucy.ungukosthub.presentastion.theme.UnguKostHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnguKostHubTheme {
                // NavHost akan ditempatkan di sini pada sprint berikutnya
            }
        }
    }
}