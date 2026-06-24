package aktual.about.ui.storage

import aktual.about.vm.BudgetStorageItem
import aktual.about.vm.ManageStorageState
import aktual.about.vm.ManageStorageViewModel
import aktual.about.vm.StorageDialog
import aktual.about.vm.StorageNavEvent
import aktual.budget.model.BudgetId
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.Bytes
import aktual.core.model.bytes
import aktual.core.model.percent
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BlurredTopBarSpacing
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
import aktual.core.ui.TabletPreview
import aktual.core.ui.WavyBackground
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.disabled
import aktual.core.ui.isCompactWidth
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ManageStorageScreen(
  navBack: BackNavigator,
  onStorageNavEvent: (StorageNavEvent) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ManageStorageViewModel = metroViewModel<ManageStorageViewModel>(),
) {
  LaunchedEffect(viewModel) { viewModel.navigationEvents.collect(onStorageNavEvent) }

  val state by viewModel.state.collectAsStateWithLifecycle()

  ManageStorageScaffold(
    state = state,
    modifier = modifier,
    onAction = { action ->
      when (action) {
        NavBack -> navBack()
        Reload -> viewModel.reload()
        RequestClearAllFiles -> viewModel.showDialog(StorageDialog.ConfirmClearAllFiles)
        is RequestClearBudget ->
          viewModel.showDialog(StorageDialog.ConfirmClearBudget(action.id, action.name))
        RequestClearCache -> viewModel.showDialog(StorageDialog.ConfirmClearCache)
        RequestClearPreferences -> viewModel.showDialog(StorageDialog.ConfirmClearPreferences)
        ConfirmClearAllFiles -> viewModel.clearAllFiles()
        is ConfirmClearBudget -> viewModel.clearBudget(action.id)
        ConfirmClearCache -> viewModel.clearCache()
        ConfirmClearPreferences -> viewModel.clearPreferences()
        DismissDialog -> viewModel.dismissDialog()
      }
    },
  )
}

@Composable
private fun ManageStorageScaffold(
  state: ManageStorageState,
  onAction: ManageStorageActionHandler,
  modifier: Modifier = Modifier,
) {
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(Strings.storageToolbar) },
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground()

      Column(modifier = Modifier.blurredTopBarContent(blurState, innerPadding)) {
        BlurredTopBarSpacing(blurState, innerPadding)
        PullToRefreshBox(
          modifier = Modifier.padding(8.dp),
          contentAlignment = Alignment.Center,
          onRefresh = { onAction(Reload) },
          isRefreshing = state is ManageStorageState.Loading,
          content = { ManageStorageContent(state, listState, onAction) },
        )
      }
    }
  }
}

@Composable
private fun ManageStorageContent(
  state: ManageStorageState,
  listState: LazyListState,
  onAction: ManageStorageActionHandler,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    when (state) {
      ManageStorageState.Loading -> {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
          content = { CircularProgressIndicator() },
        )
      }

      is ManageStorageState.Loaded -> {
        ManageStorageLoadedContent(state, listState, onAction)
        StorageDialogs(state.dialog, onAction)
      }
    }
  }
}

@Composable
private fun ManageStorageLoadedContent(
  state: ManageStorageState.Loaded,
  listState: LazyListState,
  onAction: ManageStorageActionHandler,
  modifier: Modifier = Modifier,
) {
  val sliceCount = state.budgets.size + 2 // +cache +other
  val colors = rememberDistinctColors(count = sliceCount.coerceAtLeast(1))

  if (isCompactWidth()) {
    LazyColumn(
      modifier = modifier.scrollbar(listState),
      state = listState,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        PieChart(
          state,
          colors,
          modifier = Modifier.fillParentMaxWidth(fraction = 0.5f).aspectRatio(1f),
        )
      }

      item { TotalStorageText(state) }

      item { BudgetsSummary(state, colors, onAction) }

      item { CacheSummary(state, colors) }

      item { ActionButtons(onAction) }

      item { BottomSpacing() }
    }
  } else {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        PieChart(state, colors, modifier = Modifier.padding(20.dp).weight(1f).aspectRatio(1f))

        TotalStorageText(state)

        BottomSpacing()
      }

      LazyColumn(
        modifier = Modifier.weight(1f).scrollbar(listState),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item { BudgetsSummary(state, colors, onAction) }

        item { CacheSummary(state, colors) }

        item { ActionButtons(onAction) }

        item { BottomSpacing() }
      }
    }
  }
}

@Composable
private fun ActionButtons(onAction: ManageStorageActionHandler) {
  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    NormalTextButton(
      text = Strings.storageClearCache,
      onClick = { onAction(RequestClearCache) },
      modifier = Modifier.fillMaxWidth(),
    )

    NormalTextButton(
      text = Strings.storageClearPreferences,
      onClick = { onAction(RequestClearPreferences) },
      modifier = Modifier.fillMaxWidth(),
    )

    PrimaryTextButton(
      text = Strings.storageClearAllFiles,
      onClick = { onAction(RequestClearAllFiles) },
      modifier = Modifier.fillMaxWidth(),
      colors = { pressed -> errorButtonColors(pressed) },
    )
  }
}

@Composable
private fun CacheSummary(
  state: ManageStorageState.Loaded,
  colors: ImmutableList<Color>,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(AktualTheme.colors.pillBackground.disabled, CardShape)
        .border(Dp.Hairline, AktualTheme.colors.pillBorderDark, CardShape)
        .padding(Dimens.Large),
    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    LegendRow(
      label = Strings.storageCache,
      size = state.cacheSize.toString(),
      color = colors[state.budgets.size],
    )

    LegendRow(
      label = Strings.storageOther,
      size = state.otherSize.toString(),
      color = colors[state.budgets.size + 1],
    )
  }
}

@Composable
private fun BudgetsSummary(
  state: ManageStorageState.Loaded,
  colors: ImmutableList<Color>,
  onAction: ManageStorageActionHandler,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(AktualTheme.colors.pillBackground.disabled, CardShape)
        .border(Dp.Hairline, AktualTheme.colors.pillBorderDark, CardShape)
        .padding(Dimens.Large),
    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    Text(
      text = Strings.storageBudgets,
      style = typography.bodyLarge,
      color = AktualTheme.colors.pageText,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = Dimens.Medium),
    )

    if (state.budgets.isEmpty()) {
      Text(
        text = "No budgets",
        style = typography.bodyMedium,
        color = AktualTheme.colors.pageTextSubdued,
      )
    } else {
      state.budgets.forEachIndexed { index, budget ->
        BudgetRow(
          budget = budget,
          color = colors[index],
          onDelete = { onAction(RequestClearBudget(budget.id, budget.name)) },
        )
      }
    }
  }
}

@Composable
private fun TotalStorageText(state: ManageStorageState.Loaded) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "${Strings.storageTotal}: ${state.totalSize}",
      style = typography.titleMedium,
      color = colors.pageText,
    )
    Text(
      text = percentTotalStorage(state),
      style = typography.labelMedium,
      color = colors.pageTextSubdued,
    )
  }
}

@Composable
private fun PieChart(
  state: ManageStorageState.Loaded,
  colors: ImmutableList<Color>,
  modifier: Modifier = Modifier,
) {
  if (state.totalSize > Bytes.Zero) {
    val slices = rememberSlices(state, colors)
    PieChart(slices = slices, modifier = modifier)
  }
}

@Composable
private fun rememberSlices(
  state: ManageStorageState.Loaded,
  colors: ImmutableList<Color>,
): ImmutableList<PieSlice> =
  remember(state, colors) {
    buildList {
      state.budgets.forEachIndexed { index, budget ->
        add(PieSlice(value = budget.size.numBytes.toFloat(), color = colors[index]))
      }
      add(
        PieSlice(
          value = state.cacheSize.numBytes.toFloat().coerceAtLeast(minimumValue = 0.001f),
          color = colors[state.budgets.size],
        )
      )
      add(
        PieSlice(
          value = state.otherSize.numBytes.toFloat().coerceAtLeast(minimumValue = 0.001f),
          color = colors[state.budgets.size + 1],
        )
      )
    }
      .toImmutableList()
  }

@Composable
private fun BudgetRow(budget: BudgetStorageItem, color: Color, onDelete: () -> Unit) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
    HorizontalSpacer(Dimens.Large)
    Text(
      text = budget.name,
      style = typography.bodyMedium,
      color = colors.pageText,
      modifier = Modifier.weight(1f),
    )
    Text(
      text = budget.size.toString(),
      style = typography.labelMedium,
      color = colors.pageTextSubdued,
    )
    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
      Icon(
        imageVector = MaterialIcons.Delete,
        contentDescription = null,
        tint = colors.errorText,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}

@Composable
private fun LegendRow(label: String, size: String, color: Color) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .padding(end = 32.dp), // align with budget rows that have delete button
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
    HorizontalSpacer(Dimens.Large)
    Text(
      text = label,
      style = typography.bodyMedium,
      color = colors.pageText,
      modifier = Modifier.weight(1f),
    )
    Text(text = size, style = typography.labelMedium, color = colors.pageTextSubdued)
  }
}

@Composable
private fun StorageDialogs(dialog: StorageDialog, onAction: ManageStorageActionHandler) {
  when (dialog) {
    StorageDialog.None -> {
      // N/A
    }

    StorageDialog.ConfirmClearAllFiles -> {
      AktualAlertDialog(
        title = Strings.storageClearAllFilesTitle,
        onDismissRequest = { onAction(DismissDialog) },
        buttons = {
          TextButton(onClick = { onAction(DismissDialog) }) { Text(Strings.storageCancel) }
          TextButton(onClick = { onAction(ConfirmClearAllFiles) }) {
            Text(Strings.storageConfirmDelete, color = colors.errorText)
          }
        },
      ) {
        Text(Strings.storageClearAllFilesMessage)
      }
    }

    is StorageDialog.ConfirmClearBudget -> {
      AktualAlertDialog(
        title = Strings.storageClearBudgetTitle(dialog.name),
        onDismissRequest = { onAction(DismissDialog) },
        buttons = {
          TextButton(onClick = { onAction(DismissDialog) }) { Text(Strings.storageCancel) }
          TextButton(onClick = { onAction(ConfirmClearBudget(dialog.id)) }) {
            Text(Strings.storageConfirmDelete, color = colors.errorText)
          }
        },
      ) {
        Text(Strings.storageClearBudgetMessage)
      }
    }

    StorageDialog.ConfirmClearCache -> {
      AktualAlertDialog(
        title = Strings.storageClearCacheTitle,
        onDismissRequest = { onAction(DismissDialog) },
        buttons = {
          TextButton(onClick = { onAction(DismissDialog) }) { Text(Strings.storageCancel) }
          TextButton(onClick = { onAction(ConfirmClearCache) }) {
            Text(Strings.storageConfirmClear, color = colors.errorText)
          }
        },
      ) {
        Text(Strings.storageClearCacheMessage)
      }
    }

    StorageDialog.ConfirmClearPreferences -> {
      AktualAlertDialog(
        title = Strings.storageClearPrefsTitle,
        onDismissRequest = { onAction(DismissDialog) },
        buttons = {
          TextButton(onClick = { onAction(DismissDialog) }) { Text(Strings.storageCancel) }
          TextButton(onClick = { onAction(ConfirmClearPreferences) }) {
            Text(Strings.storageConfirmClear, color = colors.errorText)
          }
        },
      ) {
        Text(Strings.storageClearPrefsMessage)
      }
    }
  }
}

@Composable
private fun percentTotalStorage(state: ManageStorageState.Loaded): String {
  val percent = state.percentTotalStorage
  val percentString =
    remember(percent) {
      if (percent < 0.1.percent) "<0.1%" else percent.toString(decimalPlaces = 1)
    }
  return Strings.storagePercent(percentString)
}

@Stable
@Composable
private fun errorButtonColors(isPressed: Boolean) =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) colors.buttonPrimaryBackground else colors.errorBackground,
    contentColor = if (isPressed) colors.buttonPrimaryText else colors.errorText,
  )

@Suppress("MagicNumber")
private val PreviewLoadedState =
  ManageStorageState.Loaded(
    totalSize = 52_428_800L.bytes,
    percentTotalStorage = 34.percent,
    budgets =
      persistentListOf(
        BudgetStorageItem(id = BudgetId("1"), name = "Personal Budget", size = 31_457_280L.bytes),
        BudgetStorageItem(id = BudgetId("2"), name = "Business", size = 10_485_760L.bytes),
        BudgetStorageItem(id = BudgetId("3"), name = "Savings Tracker", size = 5_242_880L.bytes),
      ),
    cacheSize = 3_145_728L.bytes,
    otherSize = 2_097_152L.bytes,
  )

private class StoragePreviewParams :
  ColoredParameterProvider<ManageStorageState>(
    PreviewLoadedState,
    ManageStorageState.Loading,
    PreviewLoadedState.copy(dialog = StorageDialog.ConfirmClearAllFiles),
  )

@PortraitPreview
@TabletPreview
@Composable
private fun PreviewManageStorage(
  @PreviewParameter(StoragePreviewParams::class) params: ColoredParams<ManageStorageState>
) {
  PreviewWithColoredParams(params) {
    ManageStorageScaffold(state = this, onAction = {}, modifier = Modifier.fillMaxSize())
  }
}
