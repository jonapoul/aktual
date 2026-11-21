package aktual.prefs.ui.bars

import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronLeft
import aktual.core.icons.CloudCheck
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.HazeConfig
import aktual.core.ui.HazedTopBarState
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.hazedBottomBar
import aktual.core.ui.hazedTopBar
import aktual.prefs.ui.SampleTransactionList
import aktual.theme.Colors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

// A mock screen with a top and bottom bar drawn by the app's real bar effects, over a scrollable
// list of made-up transactions so there's something behind the bars to blur
@Composable
internal fun BarsPreview(config: HazeConfig, modifier: Modifier = Modifier) =
  HazedBars(
    config = config,
    barHeight = BarHeight,
    modifier = modifier.fillMaxWidth().height(PreviewHeight),
    topBar = {
      Icon(
        modifier = Modifier.padding(horizontal = 12.dp).size(20.dp),
        imageVector = AktualIcons.CheveronLeft,
        contentDescription = null,
        tint = colors.pageText,
      )
      Text(
        text = Strings.transactionsTitleAll,
        style = typography.titleMedium,
        color = colors.pageText,
        maxLines = 1,
      )
    },
    bottomBar = {
      Icon(
        modifier = Modifier.padding(horizontal = 12.dp).size(16.dp),
        imageVector = AktualIcons.CloudCheck,
        contentDescription = null,
        tint = colors.pageTextSubdued,
      )
      Text(
        text = Strings.connectionConnected,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
        maxLines = 1,
      )
    },
  ) { topPadding ->
    SampleTransactionList(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(top = topPadding, bottom = BarHeight),
    )
  }

@Composable
private fun HazedBars(
  config: HazeConfig,
  barHeight: Dp,
  topBar: @Composable RowScope.() -> Unit,
  bottomBar: @Composable RowScope.() -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable (topPadding: Dp) -> Unit,
) {
  val hazeState = rememberHazeState()
  // With no effect the app lays content out below the top bar instead of behind it
  val topPadding = if (config.appBarEffect == None) barHeight else 0.dp
  Box(modifier = modifier.clip(CardShape)) {
    Box(
      modifier = Modifier.matchParentSize().hazeSource(hazeState).background(colors.pageBackground),
    ) {
      content(topPadding)
    }

    Row(
      modifier =
        Modifier.align(TopCenter)
          .fillMaxWidth()
          .height(barHeight)
          .hazedTopBar(
            state = HazedTopBarState(hazeState, config.appBarEffect),
            scrollOffset = { MAX_VALUE },
            config = config,
          ),
      verticalAlignment = CenterVertically,
      content = topBar,
    )

    Row(
      modifier =
        Modifier.align(BottomCenter)
          .fillMaxWidth()
          .height(barHeight)
          .hazedBottomBar(state = hazeState, config = config),
      verticalAlignment = CenterVertically,
      content = bottomBar,
    )
  }
}

private val BarHeight = 44.dp
private val PreviewHeight = 220.dp

@Preview
@Composable
private fun PreviewBarsPreview(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { BarsPreview(HazeConfig(appBarEffect = Blur)) }
