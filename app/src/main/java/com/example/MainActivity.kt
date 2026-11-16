package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.core.firebase.configureAppCheck
import com.example.ui.navigation.AetherApp
import com.example.ui.theme.AetherXTheme
import com.example.ui.theme.ObsidianVoid

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configure Firebase App Check with launch intent before any UI, ViewModels, or Firebase calls
    configureAppCheck(this, intent)

    enableEdgeToEdge()
    setContent {
      AetherXTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = ObsidianVoid
        ) {
          AetherApp()
        }
      }
    }
  }
}
