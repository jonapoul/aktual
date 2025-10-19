/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.core.ui

import aktual.core.model.immutableList
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
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
            maxLines = 1,
            overflow = if (ellipsize) TextOverflow.Ellipsis else TextOverflow.Clip,
          )
        }
      }
    }
  }
}
