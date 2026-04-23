package aktual.core.ui

import aktual.core.icons.material.Check
import aktual.core.icons.material.MaterialIcons
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T : Any> ListBottomSheet(
  value: T,
  options: ImmutableList<T>,
  onDismiss: () -> Unit,
  onSelect: (T) -> Unit,
  sheetState: SheetState,
  string: @Composable (T) -> String,
  modifier: Modifier = Modifier,
  leadingIcon: (@Composable (T) -> ImageVector)? = null,
  trailingIcon: (@Composable (T) -> ImageVector)? = null,
  key: ((T) -> Any)? = null,
  isEnabled: (T) -> Boolean = { true },
  theme: Theme = LocalTheme.current,
) {
  ModalBottomSheet(
    modifier = modifier.border(Dp.Hairline, theme.modalBorder),
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = theme.modalBackground,
    contentColor = theme.pageText,
  ) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      items(options, key) { item ->
        val label = string(value)
        ListItem(
          modifier =
            Modifier.clickable(isEnabled(item)) {
              onSelect(item)
              onDismiss()
            },
          leadingContent = leadingIcon?.let { { BottomSheetIcon(imageVector = it(item), label) } },
          headlineContent = {
            Text(modifier = Modifier.weight(1f), text = label, textAlign = TextAlign.Center)
          },
          trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              if (item == value) BottomSheetIcon(MaterialIcons.Check)
              trailingIcon?.let { BottomSheetIcon(imageVector = it(item), label) }
            }
          },
          colors = theme.listItem(),
        )
      }
    }
  }
}

@Composable
private fun Theme.listItem(): ListItemColors =
  ListItemDefaults.colors(
    containerColor = Color.Transparent,
    headlineColor = pageText,
    leadingIconColor = pageText,
    trailingIconColor = pageText,
  )

@Composable
private fun BottomSheetIcon(imageVector: ImageVector, contentDescription: String? = null) {
  Icon(
    modifier = Modifier.size(24.dp),
    imageVector = imageVector,
    contentDescription = contentDescription,
  )
}
