package aktual.prefs.ui.inspect

import aktual.core.icons.AktualIcons
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.Pencil1
import aktual.core.icons.Refresh
import aktual.core.icons.SearchAlternate
import aktual.core.icons.Trash
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.OpenInNew
import aktual.core.icons.material.Sort
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.nav.BackNavigator
import aktual.core.nav.InspectThemeNavigator
import aktual.core.nav.SearchThemeNavigator
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.MidnightColors
import aktual.core.theme.isLight
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ColorParams
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.rememberHighlighted
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.vm.inspect.InspectThemeEvent.Deleted
import aktual.prefs.vm.inspect.InspectThemeEvent.Duplicated
import aktual.prefs.vm.inspect.InspectThemeState
import aktual.prefs.vm.inspect.InspectThemeState.Loaded
import aktual.prefs.vm.inspect.InspectThemeState.Loading
import aktual.prefs.vm.inspect.InspectThemeState.NotFound
import aktual.prefs.vm.inspect.InspectThemeViewModel
import aktual.prefs.vm.inspect.PropertySorting
import aktual.prefs.vm.inspect.ThemeProperty
import aktual.prefs.vm.inspect.ThemeSource
import aktual.prefs.vm.inspect.toHexString
import aktual.prefs.vm.theme.properties
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize.Min
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun InspectThemeScreen(
  back: BackNavigator,
  toSearch: SearchThemeNavigator,
  toInspectTheme: InspectThemeNavigator,
  themeId: ThemeId,
  viewModel: InspectThemeViewModel = metroViewModel(themeId),
) {
  val uriHandler = LocalUriHandler.current
  val state by viewModel.state.collectAsStateWithLifecycle()
  val sorting by viewModel.sorting.collectAsStateWithLifecycle()
  var showSortSheet by remember { mutableStateOf(false) }
  var dialog by remember { mutableStateOf<InspectDialog?>(null) }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is Duplicated -> toInspectTheme(event.id)
        Deleted -> back()
      }
    }
  }

  InspectThemeScaffold(
    state = state,
    sorting = sorting,
    showSortSheet = showSortSheet,
    dialog = dialog,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        OpenSearch -> toSearch(themeId)
        OpenRepo -> uriHandler.openUri(viewModel.repoUrl)
        Retry -> viewModel.retry()
        ShowSortSheet -> showSortSheet = true
        DismissSortSheet -> showSortSheet = false
        is SetSorting -> viewModel.setSorting(action.sorting)
        ShowDuplicateDialog -> dialog = Duplicate
        ShowDeleteDialog -> dialog = Delete
        is EditProperty -> dialog = InspectDialog.Edit(action.property)
        DismissDialog -> dialog = null
        is ConfirmDuplicate -> viewModel.duplicate(action.name)
        ConfirmDelete -> viewModel.delete()
        is SetColor -> viewModel.setColor(action.name, action.color)
      }
    },
  )
}

@Immutable
private sealed interface InspectDialog {
  data object Duplicate : InspectDialog

  data object Delete : InspectDialog

  @JvmInline value class Edit(val property: ThemeProperty) : InspectDialog
}

@Composable
private fun metroViewModel(themeId: ThemeId) =
  assistedMetroViewModel<InspectThemeViewModel, InspectThemeViewModel.Factory>(
    key = themeId.value,
    createViewModel = { create(themeId) },
  )

@Composable
private fun InspectThemeScaffold(
  state: InspectThemeState,
  sorting: PropertySorting,
  showSortSheet: Boolean,
  dialog: InspectDialog?,
  onAction: InspectThemeActionHandler,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val sheetState = rememberBottomSheetState(initialValue = Hidden)

  val title =
    when (state) {
      is Loading -> Strings.settingsThemeInspectLoading
      is NotFound -> Strings.settingsThemeInspectNotFound
      is Loaded -> state.displayName
    }

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(title) },
        actions = {
          if (state is Loaded) {
            BareIconButton(
              imageVector = AktualIcons.SearchAlternate,
              contentDescription = Strings.settingsThemeInspectSearch,
              onClick = { onAction(OpenSearch) },
            )
            MoreMenu(state.source, onAction)
          }
        },
      )
    },
  ) { innerPadding ->
    InspectThemeContent(
      modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
      contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
      state = state,
      listState = listState,
      onAction = onAction,
    )
  }

  if (state is Loaded && showSortSheet) {
    PropertySortingBottomSheet(sorting, onAction, sheetState)
  }

  if (state is Loaded) {
    InspectDialogs(state, dialog, onAction)
  }
}

private val Loaded.displayName: String
  get() = name ?: id.value

@Composable
private fun InspectDialogs(
  state: Loaded,
  dialog: InspectDialog?,
  onAction: InspectThemeActionHandler,
) {
  val onDismiss = { onAction(DismissDialog) }
  // Each dialog closes itself before passing on its result
  fun confirm(action: InspectThemeAction) {
    onAction(DismissDialog)
    onAction(action)
  }

  when (dialog) {
    Duplicate ->
      DuplicateThemeDialog(
        themeName = state.displayName,
        onConfirm = { confirm(ConfirmDuplicate(it)) },
        onDismiss = onDismiss,
      )

    Delete ->
      DeleteThemeDialog(
        themeName = state.displayName,
        onConfirm = { confirm(ConfirmDelete) },
        onDismiss = onDismiss,
      )

    is Edit ->
      EditPropertyDialog(
        property = dialog.property,
        onConfirm = { confirm(SetColor(dialog.property.name, it)) },
        onDismiss = onDismiss,
      )

    null -> Unit
  }
}

@Composable
private fun MoreMenu(source: ThemeSource, onAction: InspectThemeActionHandler) {
  var showMenu by remember { mutableStateOf(false) }

  BareIconButton(
    imageVector = AktualIcons.DotsHorizontalTriple,
    contentDescription = Strings.settingsThemeInspectMenu,
    onClick = { showMenu = true },
  )

  AktualDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
    val sortText = Strings.settingsThemeInspectSort
    AktualDropdownMenuItem(
      text = { Text(sortText) },
      onClick = {
        showMenu = false
        onAction(ShowSortSheet)
      },
      leadingIcon = { Icon(MaterialIcons.Sort, contentDescription = sortText) },
    )

    val duplicateText = Strings.settingsThemeInspectDuplicate
    AktualDropdownMenuItem(
      text = { Text(duplicateText) },
      onClick = {
        showMenu = false
        onAction(ShowDuplicateDialog)
      },
      leadingIcon = { Icon(AktualIcons.Pencil1, contentDescription = duplicateText) },
    )

    if (source == Downloaded) {
      val repoText = Strings.settingsThemeInspectOpenRepo
      AktualDropdownMenuItem(
        text = { Text(repoText) },
        onClick = {
          showMenu = false
          onAction(OpenRepo)
        },
        leadingIcon = { Icon(MaterialIcons.OpenInNew, contentDescription = repoText) },
      )
    }

    if (source == Local) {
      val deleteText = Strings.settingsThemeInspectDelete
      AktualDropdownMenuItem(
        text = { Text(deleteText) },
        onClick = {
          showMenu = false
          onAction(ShowDeleteDialog)
        },
        leadingIcon = { Icon(AktualIcons.Trash, contentDescription = deleteText) },
      )
    }
  }
}

@Composable
private fun InspectThemeContent(
  state: InspectThemeState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: InspectThemeActionHandler,
  modifier: Modifier = Modifier,
) {
  when (state) {
    is Loading -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Center) {
        CircularProgressIndicator()
      }
    }

    is NotFound -> {
      FailureScreen(
        modifier = modifier,
        title = Strings.settingsThemeInspectNotFound,
        reason = Strings.settingsThemeInspectNotFoundReason(state.id.value),
        background = colors.tableBackground,
        action =
          FailureAction(
            text = { Strings.settingsThemeInspectRetry },
            onClick = { onAction(Retry) },
            icon = AktualIcons.Refresh,
          ),
      )
    }

    is Loaded -> {
      LazyColumn(
        modifier = modifier.fillMaxSize().scrollbar(listState).padding(Dimens.Large),
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        val isEditable = state.source == Local
        if (isEditable) {
          item {
            Text(
              modifier = Modifier.padding(bottom = Dimens.Medium),
              text = Strings.settingsThemeInspectEditHint,
              style = typography.bodySmall,
              color = colors.pageTextSubdued,
            )
          }
        }

        items(state.properties, key = { it.name }) { property ->
          ThemePropertyRow(
            property = property,
            modifier = Modifier.animateItem(),
            onClick = { onAction(EditProperty(property)) }.takeIf { isEditable },
          )
        }

        item { BottomSpacing() }
      }
    }
  }
}

@Composable
internal fun ThemePropertyRow(
  property: ThemeProperty,
  modifier: Modifier = Modifier,
  query: String = "",
  onClick: (() -> Unit)? = null,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(Min)
        .background(property.color)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(4.dp),
    horizontalArrangement = SpaceBetween,
    verticalAlignment = CenterVertically,
  ) {
    val textColor = remember(property.color) { if (property.color.isLight()) Black else White }
    Text(
      modifier = Modifier.weight(1f),
      text = rememberHighlighted(property.name, query),
      color = textColor,
      style = typography.bodySmall,
      textAlign = Start,
      maxLines = 1,
    )

    Text(
      text = rememberHighlighted(property.color.toHexString(), query),
      color = textColor,
      style = typography.labelMedium,
      textAlign = End,
      maxLines = 1,
    )
  }
}

@Preview
@Composable
private fun PreviewInspectColors(
    @PreviewParameter(InspectThemePreviewProvider::class) params: ColorParams<InspectThemeState>,
) =
  PreviewWithColorParams(params) {
    InspectThemeScaffold(
      state = this,
      sorting = Default,
      showSortSheet = false,
      dialog = null,
      onAction = {},
    )
  }

private class InspectThemePreviewProvider :
  ColorParamsProvider<InspectThemeState>(
    NotFound(id = ThemeId("username/repo")),
    Loading,
    Loaded(LightColors.id, null, BuiltIn, LightColors.properties()),
    Loaded(DarkColors.id, "Dark (copy)", Local, DarkColors.properties()),
    Loaded(MidnightColors.id, "Midnight", Downloaded, MidnightColors.properties()),
  )
