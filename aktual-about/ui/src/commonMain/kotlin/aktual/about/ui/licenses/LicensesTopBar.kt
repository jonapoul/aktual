package aktual.about.ui.licenses

import aktual.about.vm.SearchBarState
import aktual.core.icons.ArrowBack
import aktual.core.icons.MaterialIcons
import aktual.core.icons.Search
import aktual.core.icons.SearchOff
import aktual.core.l10n.Strings
import aktual.core.ui.Theme
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun LicensesTopBar(
  state: SearchBarState,
  theme: Theme,
  onAction: (LicensesAction) -> Unit,
) =
  TopAppBar(
    colors = theme.transparentTopAppBarColors(),
    navigationIcon = {
      IconButton(onClick = { onAction(LicensesAction.NavBack) }) {
        Icon(
          imageVector = MaterialIcons.ArrowBack,
          contentDescription = Strings.licensesToolbarBack,
        )
      }
    },
    title = {
      Text(text = Strings.licensesToolbarTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
    },
    actions = {
      IconButton(onClick = { onAction(LicensesAction.ToggleSearchBar) }) {
        Icon(
          imageVector =
            when (state) {
              SearchBarState.Gone -> MaterialIcons.Search
              is SearchBarState.Visible -> MaterialIcons.SearchOff
            },
          contentDescription = Strings.licensesToolbarSearch,
        )
      }
    },
  )
