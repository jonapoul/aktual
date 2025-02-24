package actual.budget.sync.ui

import actual.budget.sync.res.BudgetSyncStrings
import actual.budget.sync.vm.Bytes
import actual.budget.sync.vm.Percent
import actual.budget.sync.vm.bytes
import actual.budget.sync.vm.percent
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
internal fun ContentDownloading(
  fileSize: Bytes,
  percent: Percent,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier.padding(CirclePadding),
      contentAlignment = Alignment.Center,
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(CircleSize),
        color = theme.sliderActiveTrack,
        trackColor = theme.sliderInactiveTrack,
        strokeWidth = StrokeWidth,
        progress = { percent.toFraction() },
      )

      Text(
        text = percent.string(),
        fontSize = 30.sp,
        color = theme.pageText,
      )
    }

    Text(
      text = bytesString(fileSize, percent),
      fontSize = 30.sp,
      color = theme.pageText,
    )
  }
}

@Stable
@Suppress("MagicNumber")
private fun Percent.toFraction(): Float = value / 100f

@Stable
@Composable
private fun bytesString(bytes: Bytes, percent: Percent): String {
  val bytesDownloaded = (bytes.numBytes * percent.toFraction()).bytes
  return BudgetSyncStrings.syncDownloadingProgress(bytesDownloaded.toString(), bytes.toString())
}

@Stable
private fun Percent.string() = toString()

@ScreenPreview
@Composable
private fun ZeroPercent() = PreviewScreen {
  ContentDownloading(
    fileSize = 1234.bytes,
    percent = 0.percent,
  )
}

@ScreenPreview
@Composable
private fun FiftyPercent() = PreviewScreen {
  ContentDownloading(
    fileSize = 1_000_000.bytes,
    percent = 50.percent,
  )
}

@ScreenPreview
@Composable
private fun HundredPercent() = PreviewScreen {
  ContentDownloading(
    fileSize = 1_000_000_000.bytes,
    percent = 100.percent,
  )
}
