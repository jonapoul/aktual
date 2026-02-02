package aktual.about.ui.info

import aktual.core.l10n.Drawables
import aktual.core.l10n.Strings
import aktual.core.model.ColorSchemeType
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import alakazam.kotlin.compose.HorizontalSpacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun InfoHeader(
  year: Int,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier
      .wrapContentWidth()
      .padding(10.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val appName = Strings.appName

    Image(
      modifier = Modifier.size(50.dp),
      painter = Drawables.appIcon192,
      contentDescription = appName,
    )

    HorizontalSpacer(10.dp)

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = appName,
        fontSize = 25.sp,
        fontWeight = FontWeight.W700,
        color = theme.pageText,
      )
      Text(
        text = Strings.infoSubtitle1(year),
        color = theme.pageTextSubdued,
      )
      Text(
        text = Strings.infoSubtitle2,
        color = theme.pageTextSubdued,
      )
    }
  }
}

@Preview
@Composable
private fun PreviewHeader(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  InfoHeader(year = 2025)
}
