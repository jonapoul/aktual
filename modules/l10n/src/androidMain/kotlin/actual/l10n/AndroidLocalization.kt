package actual.l10n

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class AndroidLocalization(private val context: Context) : Localization {
  override fun dimen(id: Int): Dp = context.resources.getDimension(id).dp
  override fun string(id: Int): String = context.getString(id)
  override fun string(id: Int, vararg args: Any): String = context.getString(id, *args)
}
