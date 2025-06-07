package actual.about.info.ui

import actual.about.info.res.Strings
import actual.core.res.CoreStrings
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import alakazam.android.ui.compose.HorizontalSpacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import actual.core.res.R as CoreR

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
    val appName = CoreStrings.appName

    Box(
      modifier = Modifier.size(80.dp),
    ) {
      Image(
        modifier = Modifier.clip(CircleShape),
        painter = painterResource(id = CoreR.mipmap.ic_launcher_round_background),
        contentDescription = appName,
      )

      Image(
        painter = painterResource(id = CoreR.mipmap.ic_launcher_round_foreground),
        contentDescription = appName,
      )
    }

    HorizontalSpacer(10.dp)

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = appName,
        fontSize = 30.sp,
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
private fun PreviewHeader() = PreviewColumn {
  InfoHeader(year = 2024)
}
