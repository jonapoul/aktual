package aktual.about.ui.logs

import aktual.about.vm.logs.LiveState
import aktual.about.vm.logs.LogEntriesState
import aktual.about.vm.logs.LogExportScope
import aktual.about.vm.logs.LogExportScope.Day
import aktual.about.vm.logs.LogFilters
import aktual.core.icons.material.ArrowDownward
import aktual.core.icons.material.ArrowUpward
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.icons.material.Pause
import aktual.core.icons.material.PlayArrow
import aktual.core.icons.material.Search
import aktual.core.icons.material.Share
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.logging.LogLine
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens
import aktual.core.ui.LocalBottomSpacing
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.SearchMessage
import aktual.core.ui.bottomNavBarPadding
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import logcat.LogPriority

@Composable
internal fun LogDayScaffold(
  state: LogEntriesState,
  filters: LogFilters,
  canShare: Boolean,
  onAction: LogsActionHandler,
  modifier: Modifier = Modifier,
) {
  var isSearching by rememberSaveable { mutableStateOf(filters.query.isNotEmpty()) }
  var showExport by remember { mutableStateOf(false) }
  var showLevels by remember(state is Loaded) { mutableStateOf(false) }
  val listState = (state as? Loaded)?.let { rememberLogListState(it) }
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val hazeState = rememberHazedTopBarState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = {
      LogDaySnackbarHost(snackbarHostState, hasLiveBar = state is Loaded && state.live != None)
    },
    topBar = {
      // Reversed list, so lines are behind the bar whenever it can still scroll towards the top
      Column(
        modifier =
          Modifier.hazedTopBar(
            hazeState,
            scrollOffset = { if (listState?.canScrollForward == true) MAX_VALUE else 0f },
          ),
      ) {
        LogDayTopBar(
          state = state,
          isSearching = isSearching,
          initialQuery = filters.query,
          onSearch = { isSearching = true },
          onCloseSearch = {
            isSearching = false
            onAction(SetQuery(""))
          },
          onExport = { showExport = true },
          onScrollToTop = {
            if (state is Loaded && state.live == Following) onAction(Pause)
            listState?.let {
              scope.launch { it.animateScrollToItem(it.layoutInfo.totalItemsCount - 1) }
            }
          },
          onScrollToBottom = { listState?.let { scope.launch { it.animateScrollToItem(0) } } },
          onAction = onAction,
        )
        LogFilterChips(filters, onAction, onOpenLevels = { if (state is Loaded) showLevels = true })
        HorizontalDivider(color = colors.tableBorder)
        if (state is Loaded && filters.query.isNotBlank()) LogMatchCount(state.lines.size)
      }
    },
    bottomBar = {
      if (state is Loaded && state.live != None) {
        LiveBar(state.live, onAction)
      }
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      if (state is Loaded) {
        LogLines(
          modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
          contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
          state = state,
          listState = checkNotNull(listState),
          query = filters.query,
          onAction = onAction,
          onTagLongClick = { tag ->
            snackbarHostState.currentSnackbarData?.dismiss()
            scope.launch { snackbarHostState.showSnackbar(tag) }
          },
        )
      }
    }
  }

  if (showLevels && state is Loaded) {
    LogLevelSheet(
      filters = filters,
      counts = state.counts,
      onAction = onAction,
      onDismiss = { showLevels = false },
    )
  }

  if (showExport && state is Loaded) {
    LogExportSheet(
      scopes =
        persistentListOf<LogExportScope>(
          Shown,
          Day(state.date),
          LastWeek,
          All,
        ),
      today = state.today,
      canShare = canShare,
      onExport = { scope, target -> onAction(Export(scope, target)) },
      onDismiss = { showExport = false },
    )
  }
}

@Composable
private fun LogMatchCount(count: Int) =
  Text(
    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    text = Plurals.logsMatches(count, count),
    style = typography.labelMedium,
    color = colors.pageTextLight,
  )

@Composable
private fun LogDaySnackbarHost(hostState: SnackbarHostState, hasLiveBar: Boolean) =
  SnackbarHost(
    hostState = hostState,
    // The live bar already includes the bottom spacing
    modifier =
      if (hasLiveBar) {
        Modifier
      } else {
        Modifier.padding(bottom = LocalBottomSpacing.current + bottomNavBarPadding())
      },
  )

@Composable
private fun LogDayTopBar(
  state: LogEntriesState,
  isSearching: Boolean,
  initialQuery: String,
  onSearch: () -> Unit,
  onCloseSearch: () -> Unit,
  onExport: () -> Unit,
  onScrollToTop: () -> Unit,
  onScrollToBottom: () -> Unit,
  onAction: LogsActionHandler,
) {
  TopAppBar(
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = { NavBackIconButton { onAction(NavBack) } },
    title = {
      if (isSearching) {
        LogSearchField(initialQuery, onQueryChange = { onAction(SetQuery(it)) })
      } else if (state is Loaded) {
        Text(text = state.date.dayLabel(state.today), maxLines = 1, overflow = Ellipsis)
      } else {
        Text(text = Strings.logsToolbar, maxLines = 1, overflow = Ellipsis)
      }
    },
    actions = {
      if (isSearching) {
        BareIconButton(
          imageVector = MaterialIcons.Clear,
          contentDescription = Strings.logsSearchClose,
          onClick = onCloseSearch,
        )
      } else {
        BareIconButton(
          imageVector = MaterialIcons.Search,
          contentDescription = Strings.logsSearch,
          onClick = onSearch,
        )
      }
      BareIconButton(
        imageVector = MaterialIcons.Share,
        contentDescription = Strings.logsExport,
        onClick = onExport,
      )
      LogDayMenu(onScrollToTop, onScrollToBottom)
    },
  )
}

@Composable
private fun LogDayMenu(onScrollToTop: () -> Unit, onScrollToBottom: () -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    BareIconButton(
      imageVector = MaterialIcons.MoreVert,
      contentDescription = Strings.logsMenu,
      onClick = { expanded = true },
    )
    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      AktualDropdownMenuItem(
        text = Strings.logsGoToTop,
        leadingIcon = MaterialIcons.ArrowUpward,
        onClick = {
          expanded = false
          onScrollToTop()
        },
      )
      AktualDropdownMenuItem(
        text = Strings.logsGoToBottom,
        leadingIcon = MaterialIcons.ArrowDownward,
        onClick = {
          expanded = false
          onScrollToBottom()
        },
      )
    }
  }
}

@Composable
internal fun LogSearchField(
  initialQuery: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  showBorder: Boolean = false,
  clearable: Boolean = false,
) {
  val state = rememberTextFieldState(initialText = initialQuery)
  val currentOnQueryChange by rememberUpdatedState(onQueryChange)
  LaunchedEffect(state) {
    snapshotFlow { state.text.toString() }.collect { currentOnQueryChange(it) }
  }

  AktualTextField(
    modifier = modifier.fillMaxWidth(),
    state = state,
    singleLine = true,
    placeholderText = Strings.logsSearchPlaceholder,
    showBorder = showBorder,
    clearable = clearable,
    textStyle = typography.bodyLarge,
  )
}

@Composable
private fun LogLines(
  state: LogEntriesState.Loaded,
  listState: LazyListState,
  query: String,
  onAction: LogsActionHandler,
  onTagLongClick: (String) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  if (state.lines.isEmpty()) {
    SearchMessage(modifier = modifier.padding(contentPadding), text = Strings.logsNoMatches)
    return
  }

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    contentPadding = contentPadding,
    state = listState,
    reverseLayout = true,
    verticalArrangement = Arrangement.Top,
  ) {
    // The live bar already leaves room at the bottom
    if (state.live == None) item { BottomSpacing() }

    reversedLogLines(state.lines) { _, line ->
      Column(modifier = Modifier.animateItem()) {
        LogLineRow(
          line = line,
          query = query,
          onTagClick = { onAction(AddTag(it)) },
          onTagLongClick = onTagLongClick,
        )
        HorizontalDivider(color = colors.cardBackground)
      }
    }
  }
}

@Composable
private fun LiveBar(live: LiveState, onAction: LogsActionHandler) =
  Column(modifier = Modifier.fillMaxWidth().background(colors.cardBackground)) {
    Row(
      modifier =
        Modifier.fillMaxWidth().height(56.dp).padding(start = Dimens.Huge, end = Dimens.Large),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
    ) {
      val isFollowing = live == Following
      Box(
        modifier =
          Modifier.size(8.dp)
            .background(
              if (isFollowing) colors.numberPositive else colors.pageTextSubdued,
              CircleShape,
            ),
      )
      Text(
        modifier = Modifier.weight(1f),
        text =
          when (live) {
            is Paused -> Plurals.logsLivePaused(live.pending, live.pending)
            None,
            Following -> Strings.logsLiveFollowing
          },
        style = typography.labelLarge,
        color = colors.pageTextLight,
      )
      LiveButton(isFollowing, onAction)
    }
    BottomSpacing()
  }

@Composable
internal fun LiveButton(isFollowing: Boolean, onAction: LogsActionHandler) =
  NormalTextButton(
    text = if (isFollowing) Strings.logsPause else Strings.logsResume,
    onClick = { onAction(if (isFollowing) Pause else Resume) },
    prefix = {
      Icon(
        modifier = Modifier.size(18.dp),
        imageVector = if (isFollowing) MaterialIcons.Pause else MaterialIcons.PlayArrow,
        contentDescription = null,
      )
    },
  )

@PortraitPreview
@Composable
private fun PreviewLogDay(
  @PreviewParameter(LogDayProvider::class) params: ColoredParams<LogEntriesState>,
) =
  PreviewWithColoredParams(params) {
    LogDayScaffold(state = this, filters = PREVIEW_FILTERS, canShare = true, onAction = {})
  }

internal val PREVIEW_DATE = LocalDate(2026, 10, 18)

private const val PREVIEW_API_TAG = "ActualApi"

internal val PREVIEW_FILTERS =
  LogFilters(
    query = "",
    priorities = persistentSetOf(DEBUG, INFO, WARN, ERROR),
    tags = persistentSetOf("holiday", "shopping", "something-else"),
  )

internal val PREVIEW_LINES =
  persistentListOf(
    LogLine(LocalDateTime(2026, 10, 18, 14, 2, 11), INFO, "Main", "App started"),
    LogLine(
      LocalDateTime(2026, 10, 18, 14, 2, 11),
      DEBUG,
      PREVIEW_API_TAG,
      "GET /info -> 200 (132ms)",
    ),
    LogLine(
      LocalDateTime(2026, 10, 18, 14, 2, 16),
      WARN,
      PREVIEW_API_TAG,
      "POST /sync/sync took 2.4s",
    ),
    LogLine(
      LocalDateTime(2026, 10, 18, 14, 2, 31),
      ERROR,
      PREVIEW_API_TAG,
      "POST /sync/sync failed: SocketTimeoutException\n  at aktual.api.client.SyncApi.sync(SyncApi.kt:41)",
    ),
    LogLine(LocalDateTime(2026, 10, 18, 14, 2, 37), INFO, "SyncBudgetViewModel", "Sync complete"),
  )

private class LogDayProvider :
  ColoredParameterProvider<LogEntriesState>(
    LogEntriesState.Loaded(
      date = PREVIEW_DATE,
      today = PREVIEW_DATE,
      lines = PREVIEW_LINES,
      counts =
        persistentMapOf(
          LogPriority.INFO to 2,
          LogPriority.DEBUG to 1,
          LogPriority.WARN to 1,
          LogPriority.ERROR to 1,
        ),
      live = LiveState.Paused(pending = 12),
    ),
    LogEntriesState.Loaded(
      date = PREVIEW_DATE,
      today = PREVIEW_DATE,
      lines = persistentListOf(),
      counts = persistentMapOf(),
      live = None,
    ),
  )
