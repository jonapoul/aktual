package aktual.about.ui.logs

import aktual.about.data.LogFile
import aktual.about.vm.logs.LiveState
import aktual.about.vm.logs.LogEntriesState
import aktual.about.vm.logs.LogExportScope
import aktual.about.vm.logs.LogExportScope.Day
import aktual.about.vm.logs.LogFilesState
import aktual.about.vm.logs.LogFilters
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Share
import aktual.core.l10n.Strings
import aktual.core.logging.LogFileFormat
import aktual.core.logging.LogLine
import aktual.core.model.bytes
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ButtonShape
import aktual.core.ui.ColoredParameters
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.SearchMessage
import aktual.core.ui.plainTextClipEntry
import aktual.core.ui.scrollbar
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import logcat.LogPriority

@Composable
internal fun LogsWideScaffold(
  files: LogFilesState,
  entries: LogEntriesState,
  filters: LogFilters,
  canShare: Boolean,
  onAction: LogsActionHandler,
  modifier: Modifier = Modifier,
) {
  var showExport by remember { mutableStateOf(false) }
  val loaded = entries as? Loaded

  Column(modifier = modifier.fillMaxSize().background(colors.pageBackground)) {
    WideToolbar(
      initialQuery = filters.query,
      live = loaded?.live ?: None,
      onExport = { showExport = true },
      onAction = onAction,
    )
    HorizontalDivider(color = colors.tableBorder)

    Row(modifier = Modifier.weight(1f)) {
      WideSidebar(files, loaded, filters, onAction)
      VerticalDivider(color = colors.tableBorder)
      Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        when (files) {
          Loading -> Unit
          Empty -> SearchMessage(text = Strings.logsEmpty)
          is Loaded -> if (loaded != null) WideTable(loaded, onAction)
        }
      }
    }
  }

  if (showExport && loaded != null) {
    LogExportSheet(
      scopes =
        persistentListOf<LogExportScope>(
          Shown,
          Day(loaded.date),
          LastWeek,
          All,
        ),
      today = loaded.today,
      canShare = canShare,
      onExport = { scope, target -> onAction(Export(scope, target)) },
      onDismiss = { showExport = false },
    )
  }
}

@Composable
private fun WideToolbar(
  initialQuery: String,
  live: LiveState,
  onExport: () -> Unit,
  onAction: LogsActionHandler,
) =
  Row(
    modifier = Modifier.fillMaxWidth().height(64.dp).padding(start = 4.dp, end = 12.dp),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    NavBackIconButton { onAction(NavBack) }
    Text(
      modifier = Modifier.width(SidebarWidth - 64.dp),
      text = Strings.logsToolbar,
      style = typography.titleLarge,
      color = colors.pageText,
    )
    LogSearchField(
      modifier = Modifier.weight(1f).widthIn(max = 520.dp),
      initialQuery = initialQuery,
      onQueryChange = { onAction(SetQuery(it)) },
      showBorder = true,
      clearable = true,
    )

    if (live != None) {
      val isFollowing = live == Following
      Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Box(
          modifier =
            Modifier.size(8.dp)
              .background(
                if (isFollowing) colors.numberPositive else colors.pageTextSubdued,
                CircleShape,
              )
        )
        Text(text = Strings.logsLive, style = typography.labelLarge, color = colors.pageTextLight)
      }
      LiveButton(isFollowing, onAction)
    }

    PrimaryTextButton(
      text = Strings.logsExport,
      onClick = onExport,
      prefix = {
        Icon(
          modifier = Modifier.size(18.dp),
          imageVector = MaterialIcons.Share,
          contentDescription = null,
        )
      },
    )
  }

@Composable
private fun WideSidebar(
  files: LogFilesState,
  entries: LogEntriesState.Loaded?,
  filters: LogFilters,
  onAction: LogsActionHandler,
) =
  Column(
    modifier =
      Modifier.width(SidebarWidth)
        .fillMaxHeight()
        .background(colors.cardBackground)
        .verticalScrollWithBar()
        .padding(horizontal = 12.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    if (files is Loaded) {
      SidebarSection(Strings.logsFiles) {
        for (file in files.files) {
          SidebarFile(file, isSelected = file.date == entries?.date, today = files.today, onAction)
        }
      }
    }

    SidebarSection(Strings.logsLevels) {
      for (priority in LogPriority.entries) {
        LogLevelRow(
          priority = priority,
          isChecked = priority in filters.priorities,
          count = entries?.counts?.get(priority) ?: 0,
          onAction = onAction,
        )
      }
    }

    SidebarSection(Strings.logsTags) {
      if (filters.tags.isEmpty()) {
        Text(
          modifier = Modifier.padding(horizontal = 8.dp),
          text = Strings.logsTagsHint,
          style = typography.labelMedium,
          color = colors.pageTextSubdued,
        )
      } else {
        FlowRow(
          modifier = Modifier.padding(horizontal = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          for (tag in filters.tags) {
            LogChip(
              text = "$tag ×",
              isSelected = true,
              onClick = { onAction(RemoveTag(tag)) },
              contentDescription = Strings.logsTagRemove(tag),
            )
          }
        }
      }
    }
  }

@Composable
private fun SidebarSection(title: String, content: @Composable () -> Unit) =
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Text(
      modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
      text = title,
      style = typography.labelMedium,
      fontWeight = SemiBold,
      color = colors.pageTextLight,
    )
    content()
  }

@Composable
private fun SidebarFile(
  file: LogFile,
  isSelected: Boolean,
  today: LocalDate,
  onAction: LogsActionHandler,
) =
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .height(36.dp)
        .background(
          if (isSelected) colors.tableRowBackgroundHighlight else Transparent,
          ButtonShape,
        )
        .clickable { onAction(OpenDay(file.date)) }
        .padding(horizontal = 8.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = file.date.dayLabel(today),
      style = typography.bodyMedium,
      color = if (isSelected) colors.tableRowBackgroundHighlightText else colors.pageText,
    )
    Text(
      text = file.size.toString(),
      style = typography.labelSmall,
      color = if (isSelected) colors.tableRowBackgroundHighlightText else colors.pageTextLight,
    )
  }

@Composable
private fun WideTable(state: LogEntriesState.Loaded, onAction: LogsActionHandler) {
  var selected by remember(state.date) { mutableStateOf<LogLine?>(null) }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .height(32.dp)
          .background(colors.tableHeaderBackground)
          .padding(horizontal = 16.dp),
      verticalAlignment = CenterVertically,
    ) {
      HeaderCell(Strings.logsColumnTime, TimeWidth)
      HeaderCell(Strings.logsColumnLevel, LevelWidth)
      HeaderCell(Strings.logsColumnTag, TagWidth)
      HeaderCell(Strings.logsColumnMessage, width = null)
    }

    Box(modifier = Modifier.weight(1f)) {
      if (state.lines.isEmpty()) {
        SearchMessage(text = Strings.logsNoMatches)
      } else {
        WideRows(state, selected, onSelect = { selected = it }, onAction)
      }
    }

    HorizontalDivider(color = colors.tableBorder)
    DetailPane(selected)
  }
}

@Composable
private fun RowScope.HeaderCell(text: String, width: Dp?) =
  Text(
    modifier = if (width == null) Modifier.weight(1f) else Modifier.width(width),
    text = text,
    style = typography.labelMedium,
    fontWeight = SemiBold,
    color = colors.tableHeaderText,
  )

@Composable
private fun WideRows(
  state: LogEntriesState.Loaded,
  selected: LogLine?,
  onSelect: (LogLine) -> Unit,
  onAction: LogsActionHandler,
) {
  val listState = rememberLogListState(state)

  LazyColumn(
    modifier = Modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    reverseLayout = true,
    verticalArrangement = Arrangement.Top,
  ) {
    reversedLogLines(state.lines) { index, line ->
      val background: Color =
        when {
          line == selected -> colors.tableRowBackgroundHighlight
          index % 2 == 1 -> colors.cardBackground
          else -> Transparent
        }
      Row(
        modifier =
          Modifier.animateItem()
            .fillMaxWidth()
            .height(28.dp)
            .background(background)
            .clickable { onSelect(line) }
            .padding(horizontal = 16.dp),
        verticalAlignment = CenterVertically,
      ) {
        MonoCell(line.timestamp.time.timeString(), TimeWidth, colors.pageTextLight)
        Box(modifier = Modifier.width(LevelWidth)) { LogLabelBadge(line.priority) }
        val tag = line.tag.orEmpty()
        MonoCell(
          text = tag,
          width = TagWidth,
          color = colors.pageTextPositive,
          modifier = Modifier.clickable(enabled = tag.isNotEmpty()) { onAction(AddTag(tag)) },
        )
        MonoCell(
          text = line.message.substringBefore('\n'),
          width = null,
          color = line.priority.messageColor(colors),
        )
      }
    }
  }
}

@Composable
private fun RowScope.MonoCell(
  text: String,
  width: Dp?,
  color: Color,
  modifier: Modifier = Modifier,
) =
  Text(
    modifier =
      (if (width == null) Modifier.weight(1f) else Modifier.width(width))
        .then(modifier)
        .padding(end = 12.dp),
    text = text,
    color = color,
    fontFamily = Monospace,
    fontSize = 12.5.sp,
    maxLines = 1,
    overflow = Ellipsis,
  )

@Composable
private fun DetailPane(line: LogLine?) {
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()

  Column(modifier = Modifier.fillMaxWidth().height(168.dp).background(colors.cardBackground)) {
    if (line == null) {
      Text(
        modifier = Modifier.padding(16.dp),
        text = Strings.logsSelectLine,
        style = typography.labelMedium,
        color = colors.pageTextSubdued,
      )
      return@Column
    }

    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = CenterVertically,
    ) {
      SelectionContainer(modifier = Modifier.weight(1f)) {
        Text(
          text =
            "${line.timestamp.time.timeString()} · ${line.priority.label()} · ${line.tag.orEmpty()}",
          style = typography.labelMedium,
          color = colors.pageTextLight,
        )
      }
      NormalTextButton(
        text = Strings.logsCopy,
        onClick = {
          scope.launch { clipboard.setClipEntry(plainTextClipEntry(LogFileFormat.format(line))) }
        },
      )
    }
    HorizontalDivider(color = colors.tableBorder)
    SelectionContainer(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
      Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        text = line.message,
        color = line.priority.messageColor(colors),
        fontFamily = Monospace,
        fontSize = 12.sp,
        lineHeight = 18.sp,
      )
    }
  }
}

private val SidebarWidth = 248.dp
private val TimeWidth = 112.dp
private val LevelWidth = 72.dp
private val TagWidth = 200.dp

@Preview(widthDp = 1280, heightDp = 760)
@Composable
private fun PreviewLogsWide(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LogsWideScaffold(
      files =
        LogFilesState.Loaded(
          files = persistentListOf(LogFile(PREVIEW_DATE, 612_000.bytes, errorCount = 1)),
          totalSize = 612_000.bytes,
          today = PREVIEW_DATE,
        ),
      entries =
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
          live = Following,
        ),
      filters = PREVIEW_FILTERS,
      canShare = false,
      onAction = {},
    )
  }
