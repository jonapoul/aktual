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
package aktual.core.ui.previews

import aktual.budget.model.Interval
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.SlidingToggleButton
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Preview
@Composable
private fun PreviewStrings() = PreviewThemedColumn {
  // Basic toggle
  var selectedA by remember { mutableIntStateOf(0) }
  SlidingToggleButton(
    modifier = Modifier.padding(16.dp),
    options = persistentListOf("Option A", "Option B"),
    selectedIndex = selectedA,
    onSelectOption = { newOption -> selectedA = newOption },
  )
}

@Preview
@Composable
private fun PreviewEnum() = PreviewThemedColumn {
  // On/Off toggle
  var selectedB by remember { mutableIntStateOf(3) }
  SlidingToggleButton(
    modifier = Modifier.padding(16.dp),
    options = Interval.entries.toImmutableList(),
    selectedIndex = selectedB,
    string = { interval ->
      when (interval) {
        Interval.Daily -> "Daily"
        Interval.Weekly -> "Weekly"
        Interval.Monthly -> "Monthly"
        Interval.Yearly -> "Yearly with loads more text clipped off"
      }
    },
    onSelectOption = { newOption -> selectedB = newOption },
    singleOptionWidth = 75.dp,
  )
}
