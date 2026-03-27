package aktual.metrics.ui

import aktual.api.model.metrics.GetMetricsResponse
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.model.GB
import aktual.core.model.MB
import aktual.core.model.TB
import aktual.core.model.bytes
import aktual.core.model.kB
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.RowShape
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.metrics.vm.MetricsState
import aktual.metrics.vm.MetricsViewModel
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char

@Composable
fun MetricsScreen(
  nav: MetricsNavigator,
  viewModel: MetricsViewModel = metroViewModel<MetricsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  MetricsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        MetricsAction.NavBack -> nav.back()
        MetricsAction.Refresh -> viewModel.refresh()
      }
    },
  )
}

@Composable
internal fun MetricsScaffold(
  state: MetricsState,
  onAction: (MetricsAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  val blurState = rememberBlurredTopBarState()

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState),
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(MetricsAction.NavBack) } },
        title = { Text(Strings.metricsToolbar) },
        actions = {
          if (state is MetricsState.Success) {
            IconButton(onClick = { onAction(MetricsAction.Refresh) }) {
              Icon(imageVector = MaterialIcons.Refresh, contentDescription = Strings.metricsRefresh)
            }
          }
        },
      )
    }
  ) { innerPadding ->
    Box {
      WavyBackground()

      MetricsContent(
        modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
        contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
        state = state,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun MetricsContent(
  state: MetricsState,
  contentPadding: PaddingValues,
  onAction: (MetricsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Box(modifier = modifier.fillMaxSize().padding(horizontal = Dimens.Huge)) {
    when (state) {
      MetricsState.Loading -> LoadingContent()
      MetricsState.Disconnected -> FailureContent(Strings.metricsDisconnected, onAction, theme)
      is MetricsState.Failure -> FailureContent(state.cause, onAction, theme)
      is MetricsState.Success -> SuccessContent(state, contentPadding, theme)
    }
  }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) {
  val shimmer = rememberShimmer(ShimmerBounds.Custom)

  Column(
    modifier =
      modifier.shimmer(shimmer).onGloballyPositioned { coordinates ->
        shimmer.updateBounds(coordinates.unclippedBoundsInWindow())
      },
    verticalArrangement = Arrangement.spacedBy(VERTICAL_SPACING),
  ) {
    val shimmerModifier =
      Modifier.height(LocalMinimumInteractiveComponentSize.current)
        .fillMaxWidth()
        .background(theme.buttonNormalBackground, RowShape)
        .border(Dp.Hairline, theme.pillBorderDark, RowShape)
        .padding(Dimens.VeryLarge)

    repeat(times = 8) { Box(shimmerModifier) }
  }
}

@Composable
private fun FailureContent(
  message: String,
  onAction: (MetricsAction) -> Unit,
  theme: Theme,
  modifier: Modifier = Modifier,
) {
  FailureScreen(
    modifier = modifier.fillMaxSize(),
    title = Strings.metricsFailure,
    reason = message,
    retryText = Strings.metricsFailureRetry,
    onClickRetry = { onAction(MetricsAction.Refresh) },
    theme = theme,
  )
}

@Composable
private fun SuccessContent(
  state: MetricsState.Success,
  contentPadding: PaddingValues,
  theme: Theme,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()

  val dataModifier =
    Modifier.fillMaxWidth()
      .clip(CardShape)
      .background(theme.buttonNormalBackground, CardShape)
      .border(Dp.Hairline, theme.pillBorderDark, CardShape)
      .padding(Dimens.VeryLarge)

  LazyColumn(
    modifier = modifier.scrollbar(lazyListState),
    state = lazyListState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(VERTICAL_SPACING),
  ) {
    item {
      SuccessContentRow(
        modifier = dataModifier,
        title = Strings.metricsLastUpdate,
        value = formatted(state.lastUpdate),
      )
    }

    item {
      SuccessContentRow(
        modifier = dataModifier,
        title = Strings.metricsUptime,
        value = formatted(state.uptime),
      )
    }

    item {
      Column(modifier = dataModifier) {
        Text(
          text = Strings.metricsMemory,
          fontWeight = FontWeight.Bold,
          style = AktualTypography.titleLarge,
        )

        VerticalSpacer(10.dp)

        with(state.memory) {
          SuccessContentRow(title = Strings.metricsMemoryRss, value = rss.toString())
          SuccessContentRow(title = Strings.metricsMemoryHeapTotal, value = heapTotal.toString())
          SuccessContentRow(title = Strings.metricsMemoryHeapUsed, value = heapUsed.toString())
          SuccessContentRow(title = Strings.metricsMemoryExternal, value = external.toString())
          SuccessContentRow(
            title = Strings.metricsMemoryArrayBuffers,
            value = arrayBuffers.toString(),
          )
        }
      }
    }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@Composable
@Suppress("MagicNumber")
private fun formatted(duration: Duration): String =
  remember(duration) {
    val d = duration.inWholeDays
    val h = duration.inWholeHours % 24
    val m = duration.inWholeMinutes % 60
    val s = duration.inWholeSeconds % 60
    when {
      d >= 1 -> "${d}d ${h}h ${m}m ${s}s"
      h >= 1 -> "${h}h ${m}m ${s}s"
      m >= 1 -> "${m}m ${s}s"
      s >= 1 -> "${s}s"
      else -> "0s"
    }
  }

private val VERTICAL_SPACING = 8.dp

private val TIMESTAMP_FORMAT = DateTimeComponents.Format {
  date(LocalDate.Formats.ISO)
  char(' ')
  hour()
  char(':')
  minute()
  char(':')
  second()
}

@Composable
private fun formatted(instant: Instant): String {
  val timestamp = remember(instant) { instant.format(TIMESTAMP_FORMAT) }
  val clock = remember { Clock.System }

  val elapsed by
    produceState(Duration.ZERO, instant) {
      while (true) {
        value = clock.now() - instant
        delay(1.seconds)
      }
    }

  val dt = formatted(elapsed)
  return "$timestamp\n($dt ago)"
}

@Composable
private fun SuccessContentRow(title: String, value: String, modifier: Modifier = Modifier) {
  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Text(
      modifier = Modifier.weight(1f),
      text = title,
      style = AktualTypography.bodyLarge,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Start,
    )
    Text(
      modifier = Modifier.weight(1f),
      text = value,
      style = AktualTypography.bodyMedium,
      fontWeight = FontWeight.Normal,
      textAlign = TextAlign.End,
    )
  }
}

@PortraitPreview
@Composable
private fun PreviewMetricsScaffold(
  @PreviewParameter(MetricsStateProvider::class) params: ThemedParams<MetricsState>
) = PreviewWithColorScheme(params.theme) { MetricsScaffold(state = params.data, onAction = {}) }

@Suppress("MagicNumber")
private class MetricsStateProvider :
  ThemedParameterProvider<MetricsState>(
    MetricsState.Loading,
    MetricsState.Disconnected,
    MetricsState.Failure("Something broke"),
    MetricsState.Success(
      memory =
        GetMetricsResponse.Memory(
          rss = 123.bytes,
          heapTotal = 456.kB,
          heapUsed = 789.MB,
          external = 12.GB,
          arrayBuffers = 34.TB,
        ),
      uptime = 123.days + 4.hours + 5.seconds + 678.milliseconds,
      lastUpdate = Instant.fromEpochMilliseconds(1765211833825L), // Mon Dec 08 2025 16:37:13.825
    ),
  )
