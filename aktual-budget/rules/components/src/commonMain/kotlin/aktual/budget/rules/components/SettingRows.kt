package aktual.budget.rules.components

import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronRight
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
    text = text.uppercase(),
    style = typography.labelMedium.copy(fontWeight = SemiBold, letterSpacing = 0.08.em),
    color = colors.tableHeaderText,
  )

// The chevron slides in and out with [showChevron], so rows can change between viewing and editing
@Composable
fun SettingRow(
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  verticalAlignment: Alignment.Vertical = CenterVertically,
  showChevron: Boolean = onClick != null,
  content: @Composable RowScope.() -> Unit,
) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
        .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
    verticalAlignment = verticalAlignment,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = verticalAlignment,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      content = content,
    )
    AnimatedVisibility(
      visible = showChevron,
      enter = fadeIn() + expandHorizontally(),
      exit = fadeOut() + shrinkHorizontally(),
    ) {
      Icon(
        modifier = Modifier.padding(start = 12.dp).size(CHEVRON_SIZE),
        imageVector = AktualIcons.CheveronRight,
        contentDescription = null,
        tint = colors.tableHeaderText,
      )
    }
  }

@Composable
fun Divider(modifier: Modifier = Modifier) =
  Box(
    modifier =
      modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .height(1.dp)
        .background(colors.tableBorder),
  )

private val CHEVRON_SIZE = 20.dp
