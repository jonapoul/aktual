/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
