package aktual.metrics.ui

import aktual.api.model.metrics.GetMetricsResponse
import aktual.core.icons.ArrowBack
import aktual.core.icons.MaterialIcons
import aktual.core.icons.Refresh
import aktual.core.l10n.Strings
import aktual.core.model.GB
import aktual.core.model.MB
import aktual.core.model.TB
import aktual.core.model.bytes
import aktual.core.model.kB
import aktual.core.ui.AktualTypography
import aktual.core.ui.AnimatedLoading
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.Dimens
import aktual.core.ui.FailureScreen
import aktual.core.ui.HazedBox
import aktual.core.ui.LocalTheme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.WithHazeState
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import aktual.metrics.vm.MetricsState
import aktual.metrics.vm.MetricsViewModel
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

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
internal fun MetricsScaffold(state: MetricsState, onAction: (MetricsAction) -> Unit) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = { MetricsTopAppBar(state, scrollBehavior, onAction) },
  ) { innerPadding ->
    Box {
      val hazeState = remember { HazeState() }

      WavyBackground(modifier = Modifier.hazeSource(hazeState))

      WithHazeState(hazeState) {
        MetricsContent(
          modifier = Modifier.padding(innerPadding),
          state = state,
          onAction = onAction,
        )
      }
    }
  }
}

@Composable
private fun MetricsTopAppBar(
  state: MetricsState,
  scrollBehavior: TopAppBarScrollBehavior,
  onAction: (MetricsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  TopAppBar(
    modifier = modifier,
    colors = theme.transparentTopAppBarColors(),
    navigationIcon = {
      IconButton(onClick = { onAction(MetricsAction.NavBack) }) {
        Icon(imageVector = MaterialIcons.ArrowBack, contentDescription = Strings.navBack)
      }
    },
    title = { Text(Strings.metricsToolbar) },
    scrollBehavior = scrollBehavior,
    actions = {
      if (state is MetricsState.Success) {
        IconButton(onClick = { onAction(MetricsAction.Refresh) }) {
          Icon(imageVector = MaterialIcons.Refresh, contentDescription = Strings.metricsRefresh)
        }
      }
    },
  )

@Composable
private fun MetricsContent(
  state: MetricsState,
  onAction: (MetricsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  Box(modifier = modifier.fillMaxSize().padding(Dimens.Large)) {
    when (state) {
      MetricsState.Loading -> {
        LoadingContent()
      }

      MetricsState.Disconnected -> {
        FailureContent(
          message = Strings.metricsDisconnected,
          onAction = onAction,
          theme = theme,
          modifier = modifier,
        )
      }

      is MetricsState.Failure -> {
        FailureContent(
          message = state.cause,
          onAction = onAction,
          theme = theme,
          modifier = modifier,
        )
      }

      is MetricsState.Success -> {
        SuccessContent(state = state, modifier = modifier)
      }
    }
  }

@Composable
private fun BoxScope.LoadingContent() =
  HazedBox(modifier = Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
    AnimatedLoading(modifier = Modifier.align(Alignment.Center).size(50.dp))
  }

@Composable
private fun FailureContent(
  message: String,
  onAction: (MetricsAction) -> Unit,
  theme: Theme,
  modifier: Modifier = Modifier,
) =
  FailureScreen(
    modifier = modifier.fillMaxSize(),
    title = Strings.metricsFailure,
    reason = message,
    retryText = Strings.metricsFailureRetry,
    onClickRetry = { onAction(MetricsAction.Refresh) },
    theme = theme,
  )

@Composable
private fun SuccessContent(state: MetricsState.Success, modifier: Modifier = Modifier) =
  Column(
    modifier = modifier.padding(horizontal = 10.dp).verticalScrollWithBar(),
    verticalArrangement = Arrangement.spacedBy(Dimens.Large, Alignment.Top),
    horizontalAlignment = Alignment.Start,
  ) {
    HazedBox(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(Dimens.Huge)) {
      SuccessContentRow(title = Strings.metricsLastUpdate, value = state.lastUpdate.toString())
    }

    HazedBox(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(Dimens.Huge)) {
      SuccessContentRow(title = Strings.metricsUptime, value = state.uptime.formattedString())
    }

    HazedBox(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(Dimens.Huge)) {
      Column {
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

    BottomStatusBarSpacing()
    BottomNavBarSpacing()
  }

@Stable
@Suppress("MagicNumber")
private fun Duration.formattedString(): String {
  val d = inWholeDays
  val h = inWholeHours % 24
  val m = inWholeMinutes % 60
  val s = inWholeSeconds % 60
  return when {
    d >= 1 -> "${d}d ${h}h ${m}m ${s}s"
    h >= 1 -> "${h}h ${m}m ${s}s"
    m >= 1 -> "${m}m ${s}s"
    s >= 1 -> "${s}s"
    else -> "0s"
  }
}

@Composable
private fun SuccessContentRow(title: String, value: String, modifier: Modifier = Modifier) =
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

@PortraitPreview
@Composable
private fun PreviewMetricsScaffold(
  @PreviewParameter(MetricsStateProvider::class) params: ThemedParams<MetricsState>
) = PreviewWithColorScheme(params.type) { MetricsScaffold(state = params.data, onAction = {}) }

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
      lastUpdate =
        Instant.fromEpochMilliseconds(
          epochMilliseconds = 1765211833825L
        ), // Mon Dec 08 2025 16:37:13.825
    ),
  )
