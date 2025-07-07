package actual.core.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// TODO: Remove UnusedBoxWithConstraintsScope suppression when https://issuetracker.google.com/issues/429780473 is fixed
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ScaleToFitText(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
  maxLines: Int = 1,
  color: Color = Color.Unspecified,
  minTextSize: TextUnit = 0.sp,
  maxTextSize: TextUnit = 100.sp,
) {
  BoxWithConstraints(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    val density = LocalDensity.current
    val maxWidthPx = with(density) { maxWidth.toPx() }
    val maxHeightPx = with(density) { maxHeight.toPx() }

    var fontSize by remember { mutableStateOf(maxTextSize) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(text, maxWidthPx, maxHeightPx) {
      var minSize = minTextSize.value
      var maxSize = maxTextSize.value

      while (minSize <= maxSize) {
        val midSize = (minSize + maxSize) / 2f
        val testStyle = style.copy(fontSize = midSize.sp)

        val result = textMeasurer.measure(
          text = text,
          style = testStyle,
          constraints = Constraints(maxWidth = maxWidthPx.toInt(), maxHeight = maxHeightPx.toInt()),
          maxLines = maxLines,
        )

        val shouldReduceFont = with(result) {
          hasVisualOverflow || size.width > maxWidthPx || size.height > maxHeightPx
        }
        if (shouldReduceFont) {
          maxSize = midSize - SIZE_ITERATOR
        } else {
          minSize = midSize + SIZE_ITERATOR
          fontSize = midSize.sp
        }
      }
    }

    Text(
      modifier = Modifier.wrapContentSize(),
      text = text,
      style = style.copy(fontSize = fontSize),
      maxLines = maxLines,
      color = color,
    )
  }
}

private const val SIZE_ITERATOR = 0.5f

// NB: need to run interactive mode for this to render in preview
@Preview
@Composable
private fun ScaleToFitTextPreview() {
  Column {
    ScaleToFitText(
      modifier = Modifier
        .height(100.dp)
        .width(500.dp)
        .background(Color.Gray)
        .border(width = 1.dp, color = Color.Blue),
      text = "Hello world 100x500",
    )

    ScaleToFitText(
      modifier = Modifier
        .height(30.dp)
        .width(500.dp)
        .background(Color.Gray)
        .border(width = 1.dp, color = Color.Red),
      text = "Hello world 30x500",
    )

    ScaleToFitText(
      modifier = Modifier
        .height(300.dp)
        .width(100.dp)
        .background(Color.Gray)
        .border(width = 1.dp, color = Color.Green),
      text = "Hello world 300x100",
    )

    ScaleToFitText(
      modifier = Modifier
        .height(100.dp)
        .width(500.dp)
        .background(Color.Gray)
        .border(width = 1.dp, color = Color.Green),
      text = "Hello world 300x100 but loads more text here and here's some more",
    )
  }
}
