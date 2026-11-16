package aktual.prefs.ui.bars

import aktual.budget.model.Amount
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronLeft
import aktual.core.icons.CloudCheck
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.HazeConfig
import aktual.core.ui.HazedTopBarState
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formattedString
import aktual.core.ui.hazedBottomBar
import aktual.core.ui.hazedTopBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
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
      PlaceholderLine(width = 120.dp, color = colors.pageText)
    },
    bottomBar = {
      Icon(
        modifier = Modifier.padding(horizontal = 12.dp).size(16.dp),
        imageVector = AktualIcons.CloudCheck,
        contentDescription = null,
        tint = colors.pageTextSubdued,
      )
      PlaceholderLine(width = 80.dp, color = colors.pageTextSubdued)
    },
  ) { topPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(top = topPadding, bottom = BarHeight),
    ) {
      val swatches = swatches()
      SampleAmounts.fastForEachIndexed { i, amount ->
        SampleRow(amount = amount, color = swatches[i % swatches.size])
      }
    }
  }

// Small sample of one bar over coloured stripes, for comparing options side by side
@Composable
internal fun BarThumbnail(config: HazeConfig, modifier: Modifier = Modifier) =
  HazedBars(
    config = config.copy(glassCornerRadius = ThumbnailCornerRadius),
    barHeight = ThumbnailBarHeight,
    modifier = modifier.size(width = 64.dp, height = 40.dp),
    topBar = {},
    bottomBar = null,
  ) { topPadding ->
    Row(modifier = Modifier.fillMaxSize().padding(top = topPadding)) {
      swatches().fastForEach { color ->
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(color))
      }
    }
  }

@Composable
private fun HazedBars(
  config: HazeConfig,
  barHeight: Dp,
  topBar: @Composable RowScope.() -> Unit,
  bottomBar: (@Composable RowScope.() -> Unit)?,
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
            scrollOffset = { Float.MAX_VALUE },
            config = config,
          ),
      verticalAlignment = CenterVertically,
      content = topBar,
    )

    if (bottomBar != null) {
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
}

@Composable
private fun SampleRow(amount: Amount, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(color))
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      PlaceholderLine(width = 96.dp, color = colors.pageText)
      PlaceholderLine(width = 64.dp, color = colors.pageTextSubdued)
    }
    Text(
      text = amount.formattedString(isPrivacyEnabled = false),
      style = typography.bodyMedium,
      color = if (amount.isPositive()) colors.numberPositive else colors.numberNegative,
    )
  }
}

@Composable
private fun PlaceholderLine(width: Dp, color: Color) =
  Box(
    modifier =
      Modifier.width(width)
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(color.copy(alpha = PlaceholderAlpha)),
  )

@Composable
@ReadOnlyComposable
private fun swatches(): List<Color> =
  listOf(colors.chartQual1, colors.chartQual2, colors.chartQual3, colors.chartQual4)

private val SampleAmounts =
  listOf(-12.5, 2_450.0, -64.99, -8.2, -120.0, 35.0, -4.75, -950.0, 18.3, -42.0).map { Amount(it) }

private val BarHeight = 44.dp
private val PreviewHeight = 220.dp
private val ThumbnailBarHeight = 16.dp
private val ThumbnailCornerRadius = 6.dp
private const val PlaceholderAlpha = 0.5f

@Preview
@Composable
private fun PreviewBarsPreview(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      BarsPreview(HazeConfig(appBarEffect = Blur))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BarThumbnail(HazeConfig(appBarEffect = None))
        BarThumbnail(HazeConfig(appBarEffect = Blur))
        BarThumbnail(HazeConfig(appBarEffect = Glass))
      }
    }
  }
