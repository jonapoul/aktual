package aktual.core.ui

import aktual.budget.model.Interval
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun <T : Any> AktualSlidingToggleButton(
  selected: T,
  options: ImmutableList<T>,
  onSelect: (T) -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  string: @Composable (T) -> String = { it.toString() },
  fontSize: TextUnit = TextUnit.Unspecified,
  itemPadding: PaddingValues = PaddingValues(horizontal = 5.dp, vertical = 10.dp),
) {
  require(options.isNotEmpty()) { "Passed an empty options list into SlidingToggleButton" }

  val selectedIndex = options.indexOf(selected)

  val animatedIndex by
    animateFloatAsState(
      targetValue = selectedIndex.toFloat(),
      animationSpec = tween(durationMillis = 300),
      label = "animatedIndex",
    )

  Layout(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(ButtonShape)
        .background(colors.unselectedBackground(isEnabled), ButtonShape),
    content = {
      // Child 0: The Indicator
      Box(
        modifier =
          Modifier.fillMaxSize() // Will be constrained by the Layout logic
            .background(colors.selectedBackground(isEnabled), ButtonShape)
      )

      // Children 1 to N: The Labels
      options.fastForEachIndexed { index, option ->
        Box(
          modifier = Modifier.clickable(isEnabled) { onSelect(option) }.padding(itemPadding),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = string(option),
            color = colors.textColor(isEnabled, isSelected = selectedIndex == index),
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
          )
        }
      }
    },
  ) { measurables, constraints ->
    val count = options.size
    val itemWidth = constraints.maxWidth / count

    // 2. Measure labels first to determine the tallest one
    val labelMeasurables = measurables.subList(1, measurables.size)
    val labelPlaceables = labelMeasurables.map {
      it.measure(constraints.copy(minWidth = itemWidth, maxWidth = itemWidth))
    }

    // Determine the height based on the tallest label
    val maxHeight = labelPlaceables.maxOf { it.height }

    // 3. Measure the indicator (the first child) with the calculated dimensions
    val indicatorPlaceable =
      measurables[0].measure(Constraints.fixed(width = itemWidth, height = maxHeight))

    // 4. Set the layout size
    layout(constraints.maxWidth, maxHeight) {
      // Place Indicator
      indicatorPlaceable.placeRelative(x = (animatedIndex * itemWidth).roundToInt(), y = 0)

      labelPlaceables.forEachIndexed { index, placeable ->
        val xPosition = index * itemWidth
        val yPosition = (maxHeight - placeable.height) / 2
        placeable.placeRelative(xPosition, yPosition)
      }
    }
  }
}

@Stable
private fun Colors.textColor(isEnabled: Boolean, isSelected: Boolean): Color =
  if (isSelected) {
    if (isEnabled) checkboxText else buttonPrimaryDisabledText
  } else {
    if (isEnabled) buttonNormalText else buttonBareDisabledText
  }

@Stable
private fun Colors.selectedBackground(isEnabled: Boolean): Color =
  if (isEnabled) checkboxToggleBackgroundSelected else buttonNormalDisabledBackground

@Stable
private fun Colors.unselectedBackground(isEnabled: Boolean): Color =
  if (isEnabled) checkboxToggleBackground else buttonBareDisabledBackground

@Preview
@Composable
private fun PreviewStrings(
  @PreviewParameter(ColoredBooleanParameters::class) params: ColoredParams<Boolean>
) =
  PreviewWithColoredParams(params) {
    var selected by remember { mutableStateOf("Option A") }
    AktualSlidingToggleButton(
      modifier = Modifier.padding(4.dp),
      options = persistentListOf("Option A", "Option B"),
      selected = selected,
      onSelect = { value -> selected = value },
      isEnabled = this,
    )
  }

@Preview
@Composable
private fun PreviewEnum(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    var selected by remember { mutableStateOf(Interval.Weekly) }
    AktualSlidingToggleButton(
      modifier = Modifier.padding(4.dp),
      options = Interval.entries.toImmutableList(),
      selected = selected,
      onSelect = { value -> selected = value },
      string = { interval ->
        when (interval) {
          Interval.Daily -> "Daily"
          Interval.Weekly -> "Weekly"
          Interval.Monthly -> "Monthly"
          Interval.Yearly -> "Yearly with loads more text clipped off"
        }
      },
    )
  }
