package actual.about.ui.info

import actual.about.res.Strings
import actual.core.ui.Theme
import actual.core.ui.topAppBarColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun InfoTopBar(
  theme: Theme,
  onAction: (InfoAction) -> Unit,
) {
  TopAppBar(
    colors = theme.topAppBarColors(),
    navigationIcon = {
      IconButton(onClick = { onAction(InfoAction.NavBack) }) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = Strings.infoToolbarBack,
        )
      }
    },
    title = {
      Text(
        text = Strings.infoToolbarTitle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    },
  )
}
