package aktual.about.components

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.Dimens
import aktual.core.ui.RowShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ExportTargetRow(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(52.dp)
        .clip(RowShape)
        .background(colors.tableBackground)
        .clickable(onClick = onClick)
        .padding(horizontal = Dimens.VeryLarge),
    verticalAlignment = CenterVertically,
    horizontalArrangement = spacedBy(14.dp),
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = colors.pageTextLight)
    Text(text = text, style = typography.bodyLarge, color = colors.tableText)
  }
