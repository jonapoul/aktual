package actual.core.ui

import actual.core.model.immutableList
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun WeightedTable(
  data: ImmutableList<ImmutableList<String>>,
  modifier: Modifier = Modifier,
  textStyle: TextStyle = LocalTextStyle.current,
  padding: PaddingValues = PaddingValues(all = 0.dp),
  ellipsize: Boolean = true,
) = WeightedTable(
  modifier = modifier,
  data = data,
  textStyles = remember(textStyle) { immutableList(data.numColumns) { textStyle } },
  paddings = remember(padding) { immutableList(data.numColumns) { padding } },
  ellipsize = ellipsize,
)

@Composable
fun WeightedTable(
  data: ImmutableList<ImmutableList<String>>,
  textStyles: ImmutableList<TextStyle>,
  paddings: ImmutableList<PaddingValues>,
  modifier: Modifier = Modifier,
  ellipsize: Boolean = true,
) {
  val textMeasurer = rememberTextMeasurer()

  val columnWidths = remember(data, textStyles) {
    (0 until data.numColumns)
      .map { columnIndex -> columnWidth(textMeasurer, data, textStyles, columnIndex) }
      .toImmutableList()
  }

  val totalWidth = columnWidths.sum()

  LazyColumn(
    modifier = modifier,
  ) {
    items(data) { cells ->
      Row(verticalAlignment = Alignment.CenterVertically) {
        cells.fastForEachIndexed { index, cell ->
          val columnWidth = columnWidths.getOrNull(index) ?: 0
          val weight = if (totalWidth > 0) columnWidth.toFloat() / totalWidth else 1f
          Text(
            modifier = Modifier
              .padding(paddings[index])
              .weight(weight),
            text = cell,
            style = textStyles[index],
            maxLines = 1,
            overflow = if (ellipsize) TextOverflow.Ellipsis else TextOverflow.Clip,
          )
        }
      }
    }
  }
}

@Stable
private fun columnWidth(
  textMeasurer: TextMeasurer,
  data: ImmutableList<ImmutableList<String>>,
  textStyles: ImmutableList<TextStyle>,
  columnIndex: Int,
): Int {
  var maxWidth = 0
  data.fastForEach { row ->
    val width = textMeasurer
      .measure(row[columnIndex], textStyles[columnIndex])
      .size
      .width
    maxWidth = maxOf(maxWidth, width)
  }
  return maxWidth
}

private val ImmutableList<ImmutableList<*>>.numColumns: Int get() = maxOfOrNull { it.size } ?: 0

@Composable
fun WrapWidthTable(
  data: ImmutableList<ImmutableList<String>>,
  modifier: Modifier = Modifier,
  textStyle: TextStyle = LocalTextStyle.current,
  padding: PaddingValues = PaddingValues(all = 0.dp),
  ellipsize: Boolean = true,
) = WrapWidthTable(
  modifier = modifier,
  data = data,
  textStyles = remember(textStyle) { immutableList(data.numColumns) { textStyle } },
  paddings = remember(padding) { immutableList(data.numColumns) { padding } },
  ellipsize = ellipsize,
)

@Composable
fun WrapWidthTable(
  data: ImmutableList<ImmutableList<String>>,
  textStyles: ImmutableList<TextStyle>,
  paddings: ImmutableList<PaddingValues>,
  modifier: Modifier = Modifier,
  ellipsize: Boolean = true,
) {
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current

  val columnWidths = remember(data, textStyles, density) {
    (0 until data.numColumns).map { columnIndex ->
      val maxWidthPx = data.maxOfOrNull { row ->
        textMeasurer
          .measure(
            text = row[columnIndex],
            style = textStyles[columnIndex],
          ).size.width
      } ?: 0

      with(density) { maxWidthPx.toDp() }
    }
  }

  LazyColumn(
    modifier = modifier,
  ) {
    items(data) { row ->
      Row(verticalAlignment = Alignment.CenterVertically) {
        row.fastForEachIndexed { index, cell ->
          Text(
            modifier = Modifier
              .padding(paddings[index])
              .width(columnWidths[index]),
            text = cell,
            style = textStyles[index],
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = if (ellipsize) TextOverflow.Ellipsis else TextOverflow.Clip,
          )
        }
      }
    }
  }
}

@Preview(widthDp = 500)
@Composable
private fun PreviewWeightedTable() = PreviewColumn(maxHeight = 500.dp) {
  WeightedTable(
    modifier = Modifier.wrapContentSize(),
    data = PREVIEW_CELLS,
  )
}

@Preview(widthDp = 700)
@Composable
private fun PreviewWeightedTableWithStylesAndPadding() = PreviewColumn(maxHeight = 500.dp) {
  WeightedTable(
    modifier = Modifier.wrapContentSize(),
    data = PREVIEW_CELLS,
    paddings = persistentListOf(
      PaddingValues(all = 8.dp),
      PaddingValues(top = 16.dp),
      PaddingValues(all = 0.dp),
      PaddingValues(all = 0.dp),
    ),
    textStyles = persistentListOf(
      LocalTextStyle.current,
      MaterialTheme.typography.bodyMedium,
      MaterialTheme.typography.headlineSmall,
      MaterialTheme.typography.labelSmall,
    ),
  )
}

@Preview
@Composable
private fun PreviewWrapWidth() = PreviewColumn(maxHeight = 500.dp) {
  WrapWidthTable(
    modifier = Modifier.wrapContentSize(),
    data = PREVIEW_CELLS,
  )
}

@Preview(widthDp = 900)
@Composable
private fun PreviewWrapWidthWithStylesAndPadding() = PreviewColumn(maxHeight = 500.dp) {
  WrapWidthTable(
    modifier = Modifier.wrapContentSize(),
    data = PREVIEW_CELLS,
    padding = PaddingValues(4.dp),
  )
}

private val PREVIEW_CELLS = persistentListOf(
  persistentListOf("John Doe", "28", "Engineering", "john.doe@company.com"),
  persistentListOf("Jane Smith", "32", "Marketing", "jane.smith@company.com"),
  persistentListOf("Bob Johnsonovic", "45", "Human Resources", "bob.johnson@company.com"),
  persistentListOf("Alice Brown", "29", "Design", "alice.brown@company.com"),
)
