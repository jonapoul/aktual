package aktual.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimens {
  val Small = 2.dp
  val Medium = 4.dp
  val Large = 8.dp
  val VeryLarge = 12.dp
  val Huge = 16.dp

  val ScreenPadding = Large
  val ListItemSpacing = Large
}

@get:Stable
val Dp.padding: PaddingValues
  get() = PaddingValues(this)
