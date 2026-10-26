package aktual.about.ui.logs

import aktual.about.data.LogFile
import aktual.about.vm.logs.LogExportScope
import aktual.about.vm.logs.LogExportScope.Day
import aktual.about.vm.logs.LogFilesState
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronRight
import aktual.core.icons.Trash
import aktual.core.icons.material.Description
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Share
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.logging.LogFileFormat
import aktual.core.model.bytes
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.RowShape
import aktual.core.ui.SearchMessage
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.WavyBackground
import aktual.core.ui.contrastingTextColor
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

@Composable
internal fun LogFilesScaffold(
  state: LogFilesState,
  canShare: Boolean,
  onAction: LogsActionHandler,
  modifier: Modifier = Modifier,
) {
  var showExport by remember { mutableStateOf(false) }
  var showDelete by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(text = Strings.logsToolbar, maxLines = 1, overflow = Ellipsis) },
      )
    },
    bottomBar = {
      if (state is Loaded) {
        LogFilesBottomBar(onDelete = { showDelete = true }, onExport = { showExport = true })
      }
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      WavyBackground()
      when (state) {
        Loading -> ShimmerLogFiles(innerPadding)
        Empty -> SearchMessage(text = Strings.logsEmpty, icon = MaterialIcons.Description)
        is Loaded -> LogFilesList(state, innerPadding, onAction)
      }
    }
  }

  if (showExport && state is Loaded) {
    LogExportSheet(
      scopes = persistentListOf<LogExportScope>(Day(state.today), LastWeek, All),
      today = state.today,
      canShare = canShare,
      onExport = { scope, target -> onAction(Export(scope, target)) },
      onDismiss = { showExport = false },
    )
  }

  if (showDelete) {
    DeleteAllDialog(
      onConfirm = {
        showDelete = false
        onAction(DeleteAll)
      },
      onDismiss = { showDelete = false },
    )
  }
}

@Composable
private fun LogFilesList(
  state: LogFilesState.Loaded,
  contentPadding: PaddingValues,
  onAction: LogsActionHandler,
) {
  val listState = rememberLazyListState()

  // Only one row may be swiped open at a time
  var openDate by remember { mutableStateOf<LocalDate?>(null) }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.Large).scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = spacedBy(Dimens.Medium),
  ) {
    item { LogFilesSummary(state, Modifier.padding(vertical = Dimens.Huge)) }

    item { LogFilesDailyHeader() }

    items(state.files, key = { it.date.toString() }) { file ->
      SwipeableLogFileItem(
        modifier = Modifier.animateItem(),
        file = file,
        today = state.today,
        isOpen = openDate == file.date,
        onOpenChange = { open ->
          openDate =
            when {
              open -> file.date
              openDate == file.date -> null
              else -> openDate
            }
        },
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun LogFilesSummary(state: LogFilesState.Loaded, modifier: Modifier = Modifier) =
  Column(
    modifier = modifier.logFilesSummaryCard(colors.cardBackground, colors.cardShadow),
    verticalArrangement = spacedBy(Dimens.Medium),
  ) {
    Text(
      text = Strings.logsSummaryTitle,
      style = typography.labelMedium,
      color = colors.pageTextLight,
    )
    Row(verticalAlignment = Bottom, horizontalArrangement = spacedBy(8.dp)) {
      val count = state.files.size
      Text(
        text = Plurals.logsSummaryFiles(count, count),
        style = typography.headlineMedium,
        color = colors.pageText,
      )
      Text(
        text = state.totalSize.toString(),
        style = typography.bodyMedium,
        color = colors.pageTextLight,
      )
    }
    Text(
      text = Strings.logsSummaryRetention,
      style = typography.labelMedium,
      color = colors.pageTextSubdued,
    )
  }

@Composable
private fun SwipeableLogFileItem(
  file: LogFile,
  today: LocalDate,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: LogsActionHandler,
  modifier: Modifier = Modifier,
) {
  val deleteBackground = colors.errorText
  SwipeToReveal(
    actions =
      persistentListOf(
        SwipeAction(
          text = Strings.logsDelete,
          icon = AktualIcons.Trash,
          background = deleteBackground,
          foreground = deleteBackground.contrastingTextColor(),
          onClick = { onAction(DeleteDay(file.date)) },
        ),
      ),
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
    shape = RowShape,
  ) {
    LogFileItem(file, today, onClick = { onAction(OpenDay(file.date)) })
  }
}

@Composable
private fun LogFileItem(
  file: LogFile,
  today: LocalDate,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier.logFileRow(colors.tableBackground, onClick),
    verticalAlignment = CenterVertically,
    horizontalArrangement = spacedBy(Dimens.VeryLarge),
  ) {
    Icon(
      imageVector = MaterialIcons.Description,
      contentDescription = null,
      tint = colors.pageTextLight,
    )

    Column(modifier = Modifier.weight(1f), verticalArrangement = spacedBy(2.dp)) {
      Text(
        text = file.date.dayLabel(today),
        style = typography.bodyMedium,
        color = colors.tableText,
      )
      Text(
        text = "${LogFileFormat.fileName(file.date)} · ${file.size}",
        style = typography.labelSmall,
        fontFamily = Monospace,
        color = colors.pageTextLight,
      )
    }

    if (file.errorCount > 0) {
      Text(
        modifier =
          Modifier.background(colors.errorBackground, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        text = Plurals.logsErrors(file.errorCount, file.errorCount),
        style = typography.labelSmall,
        color = colors.errorText,
      )
    }

    Icon(
      imageVector = AktualIcons.CheveronRight,
      contentDescription = null,
      tint = colors.pageTextSubdued,
    )
  }

@Composable
private fun LogFilesDailyHeader() =
  Text(
    modifier = Modifier.padding(horizontal = Dimens.Medium, vertical = Dimens.Medium),
    text = Strings.logsDailyHeader,
    style = typography.labelLarge,
    color = colors.pageTextLight,
  )

@Composable
private fun ShimmerLogFiles(contentPadding: PaddingValues) =
  Column(
    modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = Dimens.Large),
    verticalArrangement = spacedBy(Dimens.Medium),
  ) {
    ShimmerLogFilesSummary(Modifier.padding(vertical = Dimens.Huge))
    LogFilesDailyHeader()
    repeat(times = 6) { ShimmerLogFileItem() }
  }

/** Keep in sync with [LogFilesSummary] */
@Composable
private fun ShimmerLogFilesSummary(modifier: Modifier = Modifier) =
  Column(
    modifier =
      modifier
        .logFilesSummaryCard(colors.cardBackground, colors.cardShadow)
        .shimmer(rememberShimmer(Window)),
    verticalArrangement = spacedBy(Dimens.Medium),
  ) {
    ShimmerLine(typography.labelMedium, ShimmerLabelHeight, Modifier.fillMaxWidth(fraction = 0.3f))
    Row(verticalAlignment = Bottom, horizontalArrangement = spacedBy(8.dp)) {
      ShimmerLine(
        typography.headlineMedium,
        ShimmerHeadlineHeight,
        Modifier.width(ShimmerCountWidth),
      )
      ShimmerLine(typography.bodyMedium, ShimmerBodyHeight, Modifier.width(ShimmerSizeWidth))
    }
    ShimmerLine(typography.labelMedium, ShimmerLabelHeight, Modifier.fillMaxWidth(fraction = 0.4f))
  }

/** Keep in sync with [LogFileItem] */
@Composable
private fun ShimmerLogFileItem() =
  Row(
    modifier = Modifier.logFileRow(colors.tableBackground).shimmer(rememberShimmer(Window)),
    verticalAlignment = CenterVertically,
    horizontalArrangement = spacedBy(Dimens.VeryLarge),
  ) {
    Box(Modifier.size(ShimmerIconSize).background(colors.pageText, CardShape))
    Column(modifier = Modifier.weight(1f), verticalArrangement = spacedBy(2.dp)) {
      ShimmerLine(typography.bodyMedium, ShimmerBodyHeight, Modifier.fillMaxWidth(fraction = 0.3f))
      ShimmerLine(
        typography.labelSmall.copy(fontFamily = Monospace),
        ShimmerLabelHeight,
        Modifier.fillMaxWidth(fraction = 0.6f),
      )
    }
  }

/** A bar vertically centred in the height of one line of [style], so it matches real text */
@Composable
private fun ShimmerLine(style: TextStyle, barHeight: Dp, modifier: Modifier = Modifier) =
  Box(modifier = modifier, contentAlignment = CenterStart) {
    Text(text = "", style = style, maxLines = 1)
    Box(Modifier.fillMaxWidth().height(barHeight).background(colors.pageText, CardShape))
  }

private fun Modifier.logFilesSummaryCard(background: Color, border: Color) =
  fillMaxWidth()
    .background(background, CardShape)
    .border(Hairline, border, CardShape)
    .padding(Dimens.VeryLarge)

private fun Modifier.logFileRow(background: Color, onClick: (() -> Unit)? = null) =
  fillMaxWidth()
    .heightIn(min = 56.dp)
    .clip(RowShape)
    .background(background)
    .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
    .padding(
      start = Dimens.VeryLarge,
      end = Dimens.Medium,
      top = Dimens.Large,
      bottom = Dimens.Large,
    )

@Composable
private fun LogFilesBottomBar(onDelete: () -> Unit, onExport: () -> Unit) =
  Column(modifier = Modifier.fillMaxWidth().background(colors.cardBackground)) {
    Row(
      modifier =
        Modifier.fillMaxWidth().padding(horizontal = Dimens.Large, vertical = Dimens.VeryLarge),
      horizontalArrangement = spacedBy(Dimens.Large),
    ) {
      NormalTextButton(
        modifier = Modifier.weight(1f),
        text = Strings.logsDeleteAll,
        onClick = onDelete,
      )
      PrimaryTextButton(
        modifier = Modifier.weight(2f),
        text = Strings.logsExport,
        onClick = onExport,
        prefix = { Icon(imageVector = MaterialIcons.Share, contentDescription = null) },
      )
    }
    BottomSpacing()
  }

@Composable
private fun DeleteAllDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialog(
    title = Strings.logsDeleteTitle,
    onDismissRequest = onDismiss,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.logsCancel) }
      TextButton(onClick = onConfirm) { Text(Strings.logsDelete) }
    },
  ) {
    Text(text = Strings.logsDeleteMessage)
  }

@PortraitPreview
@Composable
private fun PreviewLogFiles(
  @PreviewParameter(LogFilesProvider::class) params: ColoredParams<LogFilesState>,
) =
  PreviewWithColoredParams(params) {
    LogFilesScaffold(state = this, canShare = true, onAction = {})
  }

private val ShimmerLabelHeight = 9.dp
private val ShimmerBodyHeight = 11.dp
private val ShimmerHeadlineHeight = 22.dp
private val ShimmerCountWidth = 80.dp
private val ShimmerSizeWidth = 40.dp
private val ShimmerIconSize = 24.dp

private val PREVIEW_TODAY = LocalDate(2026, 10, 18)

private class LogFilesProvider :
  ColoredParameterProvider<LogFilesState>(
    LogFilesState.Loaded(
      files =
        persistentListOf(
          LogFile(PREVIEW_TODAY, 612_000.bytes, errorCount = 3),
          LogFile(PREVIEW_TODAY.minus(DatePeriod(days = 1)), 1_100_000.bytes, errorCount = 0),
          LogFile(PREVIEW_TODAY.minus(DatePeriod(days = 3)), 388_000.bytes, errorCount = 1),
        ),
      totalSize = 2_100_000.bytes,
      today = PREVIEW_TODAY,
    ),
    LogFilesState.Empty,
    LogFilesState.Loading,
  )
