package actual.core.ui

import actual.core.res.CoreDimens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun DialogContent(
  title: String?,
  buttons: (@Composable RowScope.() -> Unit)?,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  theme: Theme = LocalTheme.current,
  titleColor: Color = theme.pageTextPositive,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier = modifier
      .defaultMinSize(minWidth = 300.dp)
      .background(theme.dialogBackground)
      .padding(CoreDimens.veryLarge),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
  ) {
    Row(
      modifier = Modifier,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      icon?.let {
        Icon(
          imageVector = it,
          contentDescription = null,
          tint = titleColor,
        )

        HorizontalSpacer(10.dp)
      }

      title?.let {
        Text(
          modifier = Modifier.padding(vertical = CoreDimens.large),
          text = title,
          color = titleColor,
        )
      }
    }

    VerticalSpacer(CoreDimens.medium)

    CompositionLocalProvider(LocalContentColor provides theme.pageText) {
      content()
    }

    VerticalSpacer(CoreDimens.medium)

    buttons?.let {
      CompositionLocalProvider(LocalContentColor provides theme.pageTextPositive) {
        Row(
          modifier = Modifier.align(Alignment.End),
          content = buttons,
        )
      }
    }
  }
}
