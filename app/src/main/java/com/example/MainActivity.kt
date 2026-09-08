package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.AppNavigation
import com.example.ui.Screen
import com.example.ui.TryOnManager
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    var startDest = Screen.Splash.route
    if (intent?.action == Intent.ACTION_SEND && intent.type != null) {
      if (intent.type?.startsWith("image/") == true) {
        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (imageUri != null) {
          TryOnManager.selectedUserPhotoUri = imageUri.toString()
        }
        startDest = Screen.TryOn.route
      } else {
        startDest = Screen.TryOn.route
      }
    }

    setContent {
      MyApplicationTheme {
        AppNavigation(startDestination = startDest)
      }
    }
  }
}

