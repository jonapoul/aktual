package dev.jonpoulton.actual.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import dev.jonpoulton.actual.core.theme.ActualTheme

@AndroidEntryPoint
class ActualActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      ActualTheme {
        ActualNavHost()
      }
    }
  }
}
