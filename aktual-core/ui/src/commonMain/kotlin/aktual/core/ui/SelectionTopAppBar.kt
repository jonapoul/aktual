package aktual.core.ui

import aktual.core.icons.material.Clear
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SelectAll
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

// Replaces a list's top bar while some of its items are selected
@Composable
fun SelectionTopAppBar(
  numSelected: Int,
  onClear: () -> Unit,
  modifier: Modifier = Modifier,
  colors: TopAppBarColors = AktualTheme.colors.transparentTopAppBarColors(),
  actions: @Composable RowScope.() -> Unit = {},
) =
  TopAppBar(
    modifier = modifier,
    colors = colors,
    navigationIcon = {
      BareIconButton(
        imageVector = MaterialIcons.Clear,
        contentDescription = Strings.selectionClear,
        onClick = onClear,
      )
    },
    title = { Text(text = Plurals.selectionNumSelected(numSelected, numSelected)) },
    actions = actions,
  )

@Preview
@Composable
private fun PreviewSelectionTopAppBar(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    SelectionTopAppBar(numSelected = 3, onClear = {}) {
      BareIconButton(imageVector = MaterialIcons.SelectAll, contentDescription = null, onClick = {})
      BareIconButton(imageVector = MaterialIcons.Delete, contentDescription = null, onClick = {})
    }
  }
