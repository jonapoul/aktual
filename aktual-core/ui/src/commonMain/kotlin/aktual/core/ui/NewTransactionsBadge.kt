package aktual.core.ui

import aktual.core.l10n.Plurals
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp

private val BadgeSize = 20.dp

// Draws nothing when there's nothing new
@Composable
fun NewTransactionsBadge(count: Int, modifier: Modifier = Modifier) {
  if (count <= 0) return
  val description = Plurals.bankSyncNewTransactions(count, count)

  Box(
    modifier =
      modifier
        .defaultMinSize(minWidth = BadgeSize, minHeight = BadgeSize)
        .background(colors.upcomingBackground, CircleShape)
        .padding(horizontal = 6.dp)
        .clearAndSetSemantics { contentDescription = description },
    contentAlignment = Center,
  ) {
    Text(
      text = if (count > MAX_COUNT) "$MAX_COUNT+" else count.toString(),
      style = typography.labelSmall.copy(fontFeatureSettings = "tnum"),
      fontWeight = SemiBold,
      color = colors.upcomingText,
      maxLines = 1,
    )
  }
}

private const val MAX_COUNT = 99
