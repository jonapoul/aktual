package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.SearchAlternate
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun <T : Any> ListBottomSheet(
  value: T,
  options: ImmutableList<T>,
  onDismiss: () -> Unit,
  onSelect: (T) -> Unit,
  sheetState: SheetState,
  string: @Composable (T) -> String,
  modifier: Modifier = Modifier,
  leadingContent: (@Composable (T) -> Unit)? = null,
  trailingContent: (@Composable (T) -> Unit)? = null,
  key: ((T) -> Any)? = null,
  isEnabled: (T) -> Boolean = { true },
  searchPlaceholder: String? = null,
  searchTerms: (T) -> List<String> = { emptyList() },
) {
  AktualModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    val searchState = rememberTextFieldState()
    val query = searchState.text.toString().trim()
    val labels = if (query.isEmpty()) emptyList() else options.map { string(it) }
    val filtered =
      remember(options, labels, query, searchTerms) {
        filterOptions(options, labels, query, searchTerms)
      }

    if (searchPlaceholder != null) {
      AktualTextField(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        state = searchState,
        placeholderText = searchPlaceholder,
        singleLine = true,
        clearable = true,
        leadingIcon = { BottomSheetIcon(AktualIcons.SearchAlternate) },
        keyboardOptions = KeyboardOptions(imeAction = Search),
      )
    }

    if (filtered.isEmpty() && query.isNotEmpty()) BottomSheetMessage(Strings.searchNoResults)

    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      items(filtered, key) { item ->
        BottomSheetListItem(
          modifier = Modifier.animateItem(),
          label = string(item),
          isSelected = item == value,
          enabled = isEnabled(item),
          onClick = {
            onSelect(item)
            onDismiss()
          },
          leadingContent = leadingContent?.let { { it(item) } },
          trailingContent = trailingContent?.let { { it(item) } },
        )
      }
    }
  }
}

internal fun <T : Any> filterOptions(
  options: ImmutableList<T>,
  labels: List<String>,
  query: String,
  searchTerms: (T) -> List<String>,
): ImmutableList<T> =
  if (query.isEmpty()) {
    options
  } else {
    options
      .filterIndexed { i, item ->
        (searchTerms(item) + labels[i]).any { it.contains(query, ignoreCase = true) }
      }
      .toImmutableList()
  }

@Composable
fun BottomSheetListItem(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingContent: (@Composable () -> Unit)? = null,
  trailingContent: (@Composable () -> Unit)? = null,
  labelColor: Color = Unspecified,
) {
  val contentColor = if (enabled) colors.pageText else colors.pageText.disabled
  CompositionLocalProvider(LocalContentColor provides contentColor) {
    Row(
      modifier =
        modifier
          .fillMaxWidth()
          .clickable(enabled, onClick = onClick)
          .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = CenterVertically,
    ) {
      leadingContent?.invoke()

      Text(
        modifier = Modifier.weight(1f),
        text = label,
        color = if (enabled) labelColor else Unspecified,
        style = AktualTheme.typography.bodyLarge,
      )

      if (isSelected || trailingContent != null) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = CenterVertically,
        ) {
          if (isSelected) BottomSheetIcon(AktualIcons.Check)
          trailingContent?.invoke()
        }
      }
    }
  }
}

@Composable
fun BottomSheetMessage(text: String, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier.fillMaxWidth().padding(16.dp),
    text = text,
    color = colors.pageTextSubdued,
    textAlign = Center,
  )

@Composable
fun BottomSheetIcon(
  imageVector: ImageVector,
  modifier: Modifier = Modifier,
  contentDescription: String? = null,
) {
  Icon(
    modifier = modifier.size(24.dp),
    imageVector = imageVector,
    contentDescription = contentDescription,
  )
}
