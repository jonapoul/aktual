package aktual.about.ui.logs

import aktual.about.vm.logs.LogEntriesViewModel
import aktual.about.vm.logs.LogFilesState
import aktual.about.vm.logs.LogFilesViewModel
import aktual.about.vm.logs.LogFilters
import aktual.core.nav.BackNavigator
import aktual.core.nav.LogDayNavigator
import aktual.core.ui.isCompactWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDate

@Composable
fun LogsScreen(
  back: BackNavigator,
  toDay: LogDayNavigator,
  filesViewModel: LogFilesViewModel = metroViewModel(),
) {
  val files by filesViewModel.state.collectAsStateWithLifecycle()
  val handler = rememberLogExportHandler()
  CollectLogExports(filesViewModel.exports, handler)

  val onFilesAction = LogsActionHandler { action ->
    when (action) {
      NavBack -> back()
      is OpenDay -> toDay(action.date)
      DeleteAll -> filesViewModel.deleteAll()
      is DeleteDay -> filesViewModel.delete(action.date)
      is Export -> filesViewModel.export(action.scope, action.target)
      is SetQuery,
      is TogglePriority,
      is AddTag,
      is RemoveTag,
      Pause,
      Resume -> Unit
    }
  }

  if (isCompactWidth()) {
    LogFilesScaffold(state = files, canShare = handler.canShare, onAction = onFilesAction)
    return
  }

  var selected by remember { mutableStateOf<LocalDate?>(null) }
  var lastFilters by remember { mutableStateOf(LogFilters.Default) }
  val date = selected ?: (files as? Loaded)?.files?.firstOrNull()?.date

  if (date == null) {
    LogsWideScaffold(
      files = files,
      entries = Loading,
      filters = lastFilters,
      canShare = handler.canShare,
      onAction = onFilesAction,
    )
    return
  }

  LogsWideEntries(
    files = files,
    handler = handler,
    onSelect = { selected = it },
    onFiltersChange = { lastFilters = it },
    back = back,
    viewModel = metroViewModel(date, lastFilters),
  )
}

@Composable
private fun LogsWideEntries(
  files: LogFilesState,
  handler: LogExportHandler,
  onSelect: (LocalDate) -> Unit,
  onFiltersChange: (LogFilters) -> Unit,
  back: BackNavigator,
  viewModel: LogEntriesViewModel,
) {
  val entries by viewModel.state.collectAsStateWithLifecycle()
  val filters by viewModel.filters.collectAsStateWithLifecycle()
  SideEffect(filters) { onFiltersChange(filters) }
  CollectLogExports(viewModel.exports, handler)

  LogsWideScaffold(
    files = files,
    entries = entries,
    filters = filters,
    canShare = handler.canShare,
    onAction = { action ->
      if (action is OpenDay) onSelect(action.date) else viewModel.handle(action, back)
    },
  )
}

@Composable
fun LogDayScreen(
  date: LocalDate,
  back: BackNavigator,
  viewModel: LogEntriesViewModel = metroViewModel(date, Default),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val filters by viewModel.filters.collectAsStateWithLifecycle()
  val handler = rememberLogExportHandler()
  CollectLogExports(viewModel.exports, handler)

  LogDayScaffold(
    state = state,
    filters = filters,
    canShare = handler.canShare,
    onAction = { action -> viewModel.handle(action, back) },
  )
}

@Composable
private fun metroViewModel(date: LocalDate, initialFilters: LogFilters) =
  assistedMetroViewModel<LogEntriesViewModel, LogEntriesViewModel.Factory>(
    key = date.toString(),
    createViewModel = { create(date, initialFilters) },
  )

private fun LogEntriesViewModel.handle(action: LogsAction, back: BackNavigator) {
  when (action) {
    NavBack -> back()
    is Export -> export(action.scope, action.target)
    is SetQuery -> setQuery(action.query)
    is TogglePriority -> togglePriority(action.priority)
    is AddTag -> addTag(action.tag)
    is RemoveTag -> removeTag(action.tag)
    Pause -> pause()
    Resume -> resume()
    is OpenDay,
    is DeleteDay,
    DeleteAll -> Unit
  }
}
