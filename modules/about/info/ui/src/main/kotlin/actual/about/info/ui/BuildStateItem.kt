package actual.about.info.ui

import actual.about.info.res.InfoStrings
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.NormalIconButton
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import android.R.attr.text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun BuildStateItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  onClick: (() -> Unit)? = null,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(ItemHeight)
      .background(theme.cardBackground, CardShape)
      .padding(horizontal = ItemPadding)
      .clickable(
        enabled = onClick != null,
        onClick = { onClick?.invoke() },
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier
        .padding(ItemPadding)
        .size(32.dp),
      imageVector = icon,
      contentDescription = title,
      tint = theme.pageText,
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(ItemPadding),
    ) {
      Text(
        modifier = Modifier.testTag(Tags.BuildStateItemTitle),
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = theme.pageText,
      )
      Text(
        modifier = Modifier.testTag(Tags.BuildStateItemValue),
        text = subtitle,
        style = MaterialTheme.typography.labelMedium,
        color = theme.pageTextSubdued,
      )
    }

    if (onClick != null) {
      NormalIconButton(
        modifier = Modifier.padding(ItemPadding),
        imageVector = Icons.AutoMirrored.Filled.Launch,
        contentDescription = InfoStrings.launch,
        onClick = onClick,
      )
    }
  }
}

private val ItemPadding = 8.dp
private val ItemHeight = 70.dp

@Preview
@Composable
private fun PreviewRegularItem() = PreviewColumn {
  BuildStateItem(
    icon = Icons.Filled.Info,
    title = "Info",
    subtitle = "More info",
    onClick = null,
  )
}

@Preview
@Composable
private fun PreviewClickableItem() = PreviewColumn {
  BuildStateItem(
    icon = Icons.Filled.Numbers,
    title = "Info",
    subtitle = "More info",
    onClick = {},
  )
}
