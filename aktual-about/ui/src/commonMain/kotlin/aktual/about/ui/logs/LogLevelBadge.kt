package aktual.about.ui.logs

import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import logcat.LogPriority

@Composable
internal fun LogLetterBadge(
  priority: LogPriority,
  fontSize: TextUnit,
  modifier: Modifier = Modifier,
) {
  Text(
    modifier =
      modifier.width(14.dp).background(priority.background(colors), RoundedCornerShape(2.dp)),
    text = priority.letter.toString(),
    color = priority.foreground(colors),
    fontFamily = Monospace,
    fontWeight = Medium,
    fontSize = fontSize,
    textAlign = Center,
  )
}

@Composable
internal fun LogLabelBadge(priority: LogPriority, modifier: Modifier = Modifier) {
  Text(
    modifier =
      modifier
        .background(priority.background(colors), RoundedCornerShape(4.dp))
        .padding(horizontal = 6.dp, vertical = 2.dp),
    text = priority.label(),
    color = priority.foreground(colors),
    fontWeight = SemiBold,
    fontSize = 11.sp,
    maxLines = 1,
  )
}
