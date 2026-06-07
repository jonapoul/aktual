package aktual.budget.rules.ui.edit

import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.rules.ui.LocalEntityListFetcher
import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.ui.PreviewRule1
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.edit.EditRuleEvent
import aktual.budget.rules.vm.edit.EditRuleState
import aktual.budget.rules.vm.edit.EditRuleState.Failure
import aktual.budget.rules.vm.edit.EditRuleState.Loading
import aktual.budget.rules.vm.edit.EditRuleState.Success
import aktual.budget.rules.vm.edit.EditRuleViewModel
import aktual.core.icons.material.DeleteForever
import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.icons.material.Save
import aktual.core.icons.material.SaveAs
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.AktualTooltip
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun EditRuleScreen(
  id: RuleId?,
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditRuleViewModel = metroViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        EditRuleEvent.DeletedRule -> back()
      }
    }
  }

  CompositionLocalProvider(
    LocalNameFetcher provides viewModel.nameFetcher,
    LocalEntityListFetcher provides viewModel.entityListFetcher,
  ) {
    EditRuleScaffold(
      modifier = modifier,
      mode = if (id == null) Mode.Create else Mode.Edit,
      state = state,
      onAction = { action ->
        when (action) {
          NavBack -> back()
          Delete -> viewModel.delete()
          Save -> viewModel.save()
          AppendCondition -> viewModel.appendCondition()
          is SetStage -> viewModel.setStage(action.value)
          is SetConditionOp -> viewModel.setConditionOp(action.value)
          is SetConditionField -> viewModel.setConditionField(action.value, action.index)
          is SetConditionOperator -> viewModel.setConditionOperator(action.value, action.index)
          is SetConditionValue -> viewModel.setConditionValue(action.value, action.index)
          is DeleteCondition -> viewModel.deleteCondition(action.index)
        }
      },
    )
  }
}

@Composable
private fun metroViewModel(id: RuleId?) =
  assistedMetroViewModel<EditRuleViewModel, EditRuleViewModel.Factory>(key = id.toString()) {
    create(id)
  }

@Composable
private fun EditRuleScaffold(
  state: EditRuleState,
  mode: Mode,
  onAction: EditRuleActionHandler,
  modifier: Modifier = Modifier,
) {
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { EditRuleTitle(mode) },
        actions = { EditRuleActions(state, mode, onAction) },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      EditRuleContent(
        modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
        contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
        state = state,
        listState = listState,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun EditRuleTitle(mode: Mode, modifier: Modifier = Modifier) {
  Text(
    modifier = modifier,
    text =
      when (mode) {
        Mode.Create -> Strings.editRuleToolbarCreate
        Mode.Edit -> Strings.editRuleToolbarEdit
      },
  )
}

@Composable
private fun EditRuleActions(
  state: EditRuleState,
  mode: Mode,
  onAction: EditRuleActionHandler,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.End),
  ) {
    var showMenu by remember { mutableStateOf(false) }

    NormalIconButton(
      onClick = { onAction(Save) },
      isEnabled = state is Success,
      imageVector =
        when (mode) {
          Mode.Create -> MaterialIcons.SaveAs
          Mode.Edit -> MaterialIcons.Save
        },
      contentDescription =
        when (mode) {
          Mode.Create -> Strings.editRuleCreate
          Mode.Edit -> Strings.editRuleSave
        },
    )

    NormalIconButton(
      onClick = { showMenu = !showMenu },
      isEnabled = state is Success,
      imageVector = MaterialIcons.MoreVert,
      contentDescription = Strings.editRuleMenu,
    )

    MoreMenu(state, showMenu, onAction, onDismissRequest = { showMenu = false })
  }
}

@Composable
private fun MoreMenu(
  state: EditRuleState,
  showMenu: Boolean,
  onAction: EditRuleActionHandler,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualDropdownMenu(
    modifier = modifier,
    expanded = showMenu,
    onDismissRequest = onDismissRequest,
  ) {
    val string = Strings.editRuleActionDelete
    val red = colors.errorText
    AktualDropdownMenuItem(
      enabled = state is Success,
      text = { Text(string, color = red) },
      onClick = {
        onDismissRequest()
        onAction(Delete)
      },
      leadingIcon = {
        Icon(imageVector = MaterialIcons.DeleteForever, contentDescription = string, tint = red)
      },
    )
  }
}

@Composable
private fun EditRuleContent(
  state: EditRuleState,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: EditRuleActionHandler,
  modifier: Modifier = Modifier,
) {
  when (state) {
    is Failure -> {
      FailureScreen(
        title = Strings.editRuleFailedTitle,
        reason = state.reason,
        background = colors.tableBackground,
        action = null,
      )
    }

    Loading -> {
      LoadingContent(modifier = modifier, contentPadding = contentPadding, onAction = onAction)
    }

    is Success -> {
      LoadedContent(
        modifier = modifier,
        rule = state.rule,
        isEnabled = !state.isWorking,
        listState = listState,
        contentPadding = contentPadding,
        onAction = onAction,
      )
    }
  }
}

private val Failure.reason: String
  @Composable
  get() =
    when (this) {
      Failure.NoMatch -> Strings.editRuleFailedNoMatch
      is Failure.Saving -> Strings.editRuleFailedSaving(reason)
      is Failure.Other -> Strings.editRuleFailedOther(reason)
    }

/** Make sure this stays in sync with [LoadedContent] */
@Suppress("unused")
@Composable
private fun LoadingContent(
  onAction: EditRuleActionHandler,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  // TBC
}

/** Make sure this stays in sync with [LoadingContent] */
@Composable
private fun LoadedContent(
  rule: Rule,
  isEnabled: Boolean,
  onAction: EditRuleActionHandler,
  contentPadding: PaddingValues,
  listState: LazyListState,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.padding(8.dp).scrollbar(listState),
      state = listState,
      contentPadding = contentPadding,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item { RuleStage(rule, isEnabled, onAction) }

      item { Conditions(rule, isEnabled, onAction) }

      item { BottomSpacing() }
    }

    Column {
      VerticalSpacer(weight = 1f)

      BottomSpacing()
    }
  }
}

@Composable
private fun RuleStage(
  rule: Rule,
  isEnabled: Boolean,
  onAction: EditRuleActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier.fillMaxWidth().background(colors.cardBackground, CardShape).padding(CARD_PADDING),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row {
      Text(
        modifier = Modifier.weight(1f),
        text = Strings.editRuleStage,
        style = typography.titleSmall,
      )

      AktualTooltip(
        imageVector = MaterialIcons.Info,
        contentDescription = null,
        tooltipText = Strings.editRuleStageDesc,
      )
    }

    val stages = remember { RuleStage.entries.toImmutableList() }
    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth(),
      options = stages,
      isEnabled = isEnabled,
      selected = rule.stage,
      onSelect = { stage -> onAction(SetStage(stage)) },
      string = { it.string() },
    )
  }
}

@PortraitPreview
@Composable
private fun PreviewEditRuleScaffold(
  @PreviewParameter(EditRuleStateProvider::class) params: ColoredParams<EditRuleStateParams>
) {
  PreviewWithColoredParams(params) { EditRuleScaffold(state = state, mode = mode, onAction = {}) }
}

private data class EditRuleStateParams(val state: EditRuleState, val mode: Mode = Mode.Edit)

private class EditRuleStateProvider :
  ColoredParameterProvider<EditRuleStateParams>(
    EditRuleStateParams(Success(PreviewRule1, isWorking = false)),
    EditRuleStateParams(Success(PreviewRule1, isWorking = true)),
    EditRuleStateParams(Loading),
    EditRuleStateParams(Failure.Other("Something broke")),
  )
