package aktual.budget.home.ui

import aktual.budget.model.Amount
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Warning
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareTextButton
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.RounderCardShape
import aktual.core.ui.formattedString
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp.Companion.Hairline
import androidx.compose.ui.unit.dp

internal val CardPadding = 16.dp

// Unboxed cards draw no frame of their own, for when their container already provides one
@Composable
internal fun HomeCard(
  modifier: Modifier = Modifier,
  isBoxed: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  val frame =
    if (isBoxed) {
      Modifier.clip(RounderCardShape)
        .background(colors.tableBackground, RounderCardShape)
        .border(Hairline, colors.tableBorder, RounderCardShape)
    } else {
      Modifier
    }

  Column(
    modifier = modifier.fillMaxWidth().then(frame).padding(vertical = CardPadding),
    content = content,
  )
}

// Adds a hover highlight on top of the usual ripple, for desktop and other pointer devices
@Composable
internal fun Modifier.hoverClickable(role: Role? = null, onClick: () -> Unit): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  return background(if (isHovered) colors.tableRowBackgroundHover else Transparent)
    .clickable(
      interactionSource = interactionSource,
      indication = ripple(),
      role = role,
      onClick = onClick,
    )
}

internal fun TextStyle.tabularFigures() = copy(fontFeatureSettings = "tnum")

// Screen readers tend to skip a leading minus sign, so negative amounts spell it out
@Composable
internal fun AmountText(
  amount: Amount,
  style: TextStyle,
  color: Color,
  modifier: Modifier = Modifier,
  prefix: String = "",
) {
  val text = prefix + amount.formattedString()
  val spoken =
    if (amount < Zero && !LocalPrivacyEnabled.current) {
      prefix + Strings.homeAmountNegative((-amount).formattedString())
    } else {
      text
    }

  Text(
    modifier = modifier.semantics { contentDescription = spoken },
    text = text,
    style = style.tabularFigures(),
    fontWeight = SemiBold,
    color = color,
    maxLines = 1,
  )
}

@Composable
internal fun CardError(
  message: String,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(CardPadding),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(20.dp),
      imageVector = MaterialIcons.Warning,
      contentDescription = null,
      tint = colors.errorText,
    )

    Text(
      modifier = Modifier.weight(1f),
      text = message,
      style = typography.bodyMedium,
      color = colors.pageText,
    )

    BareTextButton(text = Strings.homeRetry, onClick = { onAction(Retry) })
  }
}
