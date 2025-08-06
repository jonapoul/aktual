package actual.core.ui

import actual.budget.model.Interval
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.times
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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

@Preview
@Composable
private fun PreviewStrings() = PreviewColumn {
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
private fun PreviewEnum() = PreviewColumn {
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
