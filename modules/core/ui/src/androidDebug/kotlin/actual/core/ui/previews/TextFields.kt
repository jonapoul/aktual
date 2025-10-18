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

import actual.budget.model.DateRangeType
import actual.core.ui.ExposedDropDownMenu
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.TextField
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Preview
@Composable
private fun PreviewEmptyTextField() = PreviewThemedColumn {
  TextField(
    value = "",
    onValueChange = {},
    placeholderText = "I'm empty",
  )
}

@Preview
@Composable
private fun PreviewFilledTextField() = PreviewThemedColumn {
  TextField(
    value = "I'm full",
    onValueChange = {},
    placeholderText = "Hello world",
  )
}

@Preview
@Composable
private fun PreviewFilledClearable() = PreviewThemedColumn {
  TextField(
    value = "I'm full",
    onValueChange = {},
    placeholderText = "Hello world",
    clearable = true,
  )
}

@Preview
@Composable
private fun PreviewDropDownMenu() = PreviewThemedColumn {
  var value by remember { mutableStateOf("B") }
  val options = persistentListOf("A", "B", "C", "D")
  ExposedDropDownMenu(
    value = value,
    onValueChange = { newValue -> value = newValue },
    options = options,
  )
}

@Preview
@Composable
private fun PreviewDropDownMenuForcedWidth() = PreviewThemedColumn {
  var value by remember { mutableStateOf("B") }
  val options = persistentListOf("A", "B", "C", "D")
  ExposedDropDownMenu(
    modifier = Modifier.width(100.dp),
    value = value,
    onValueChange = { newValue -> value = newValue },
    options = options,
  )
}

@Preview
@Composable
private fun PreviewDropDownMenuEnum() = PreviewThemedColumn {
  var value by remember { mutableStateOf(DateRangeType.YearToDate) }
  val options = DateRangeType.entries.toImmutableList()
  ExposedDropDownMenu(
    value = value,
    onValueChange = { newValue -> value = newValue },
    options = options,
    string = { type ->
      when (type) {
        DateRangeType.YearToDate -> "YTD"
        DateRangeType.LastYear -> "Last Year"
        DateRangeType.AllTime -> "All Time with some more text"
        else -> type.name
      }
    },
  )
}
