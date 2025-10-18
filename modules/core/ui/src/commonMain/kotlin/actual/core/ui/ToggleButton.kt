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
package actual.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.times
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T> SlidingToggleButton(
  options: ImmutableList<T>,
  onSelectOption: (index: Int) -> Unit,
  modifier: Modifier = Modifier,
  selectedIndex: Int = 0,
  theme: Theme = LocalTheme.current,
  string: @Composable (T) -> String = { it.toString() },
  radius: Dp = 5.dp,
  fontSize: TextUnit = TextUnit.Unspecified,
  singleOptionWidth: Dp = Dp.Unspecified,
) {
  require(options.isNotEmpty()) { "Passed an empty options list into SlidingToggleButton" }

  val optionCount = options.size
  val realSingleOptionWidth = singleOptionWidth.takeIf { !it.isUnspecified } ?: 100.dp
  val buttonWidth = realSingleOptionWidth * optionCount
  val shape = RoundedCornerShape(radius)

  val slideOffset by animateFloatAsState(
    targetValue = selectedIndex.toFloat(),
    animationSpec = tween(durationMillis = 300),
    label = "slideOffset",
  )

  Box(
    modifier = modifier
      .width(buttonWidth)
      .height(50.dp)
      .clip(shape)
      .background(theme.buttonPrimaryDisabledBackground),
  ) {
    // Sliding indicator
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .width(realSingleOptionWidth)
        .offset(x = slideOffset * realSingleOptionWidth)
        .clip(RoundedCornerShape(radius))
        .background(theme.buttonPrimaryBackground),
    )

    // Option labels
    Row(
      modifier = Modifier.fillMaxSize(),
    ) {
      options.fastForEachIndexed { index, option ->
        Box(
          modifier = Modifier
            .width(realSingleOptionWidth)
            .fillMaxHeight()
            .clickable { onSelectOption(index) },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = string(option),
            color = if (selectedIndex == index) theme.buttonPrimaryText else theme.buttonPrimaryDisabledText,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
          )
        }
      }
    }
  }
}
