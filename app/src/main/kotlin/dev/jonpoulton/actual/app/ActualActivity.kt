package dev.jonpoulton.actual.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import dagger.hilt.android.AndroidEntryPoint
import dev.jonpoulton.actual.core.ui.ActualTheme
import dev.jonpoulton.actual.nav.ActualNavHost
import timber.log.Timber

@AndroidEntryPoint
class ActualActivity : ComponentActivity() {
  private val permissionRequest = registerForActivityResult(RequestMultiplePermissions()) { results ->
    val resultString = results.toList().joinToString { (key, value) -> "$key=$value" }
    Timber.i("Permission results = [ $resultString ]")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    permissionRequest.launch(
      arrayOf(
        "android.permission.INTERNET",
      ),
    )

    setContent {
      ActualTheme {
        ActualNavHost()
      }
    }
  }
}
