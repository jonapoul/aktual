package actual.core.ui

import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
@Composable
fun Theme.topAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
  containerColor = mobileHeaderBackground,
  titleContentColor = mobileHeaderText,
  navigationIconContentColor = mobileHeaderText,
)
