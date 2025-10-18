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
package actual.core.ui.previews

import actual.core.ui.ActualTypography
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.WeightedTable
import actual.core.ui.WrapWidthTable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

@Preview(widthDp = 500)
@Composable
private fun PreviewWeightedTable() = PreviewThemedColumn(maxHeight = 500.dp) {
  WeightedTable(
    modifier = Modifier.wrapContentSize(),
    data = PREVIEW_CELLS,
  )
}

@Preview(widthDp = 700)
@Composable
private fun PreviewWeightedTableWithStylesAndPadding() = PreviewThemedColumn(maxHeight = 500.dp) {
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
      ActualTypography.bodyMedium,
      ActualTypography.headlineSmall,
      ActualTypography.labelSmall,
    ),
  )
}

@Preview
@Composable
private fun PreviewWrapWidth() = PreviewThemedColumn(maxHeight = 500.dp) {
  WrapWidthTable(
    modifier = Modifier.wrapContentSize(),
    data = PREVIEW_CELLS,
  )
}

@Preview(widthDp = 900)
@Composable
private fun PreviewWrapWidthWithStylesAndPadding() = PreviewThemedColumn(maxHeight = 500.dp) {
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
