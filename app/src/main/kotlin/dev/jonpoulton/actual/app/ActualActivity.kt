package dev.jonpoulton.actual.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.jonpoulton.actual.core.ui.ActualTheme
import dev.jonpoulton.actual.nav.ActualNavHost

@AndroidEntryPoint
class ActualActivity : ComponentActivity() {
  private val viewModel by viewModels<ActualActivityViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.start()

    val colorSchemeType = viewModel.colorSchemeType

    enableEdgeToEdge()
    setContent {
      ActualTheme(colorSchemeType) {
        ActualNavHost()
      }
    }
  }
}
