package aktual.about.ui.info

import aktual.core.l10n.Strings
import aktual.core.theme.Theme
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun InfoTopBar(theme: Theme, onAction: (InfoAction) -> Unit) {
  TopAppBar(
    colors = theme.transparentTopAppBarColors(),
    navigationIcon = { NavBackIconButton { onAction(InfoAction.NavBack) } },
    title = {
      Text(text = Strings.infoToolbarTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
    },
  )
}
