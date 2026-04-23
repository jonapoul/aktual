package aktual.budget.rules.ui.edit

import aktual.app.nav.BackNavigator
import aktual.budget.model.RuleId
import aktual.budget.rules.vm.edit.EditRuleEvent
import aktual.budget.rules.vm.edit.EditRuleState
import aktual.budget.rules.vm.edit.EditRuleViewModel
import aktual.core.icons.material.DeleteForever
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.ui.BasicIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.ThemedDropdownMenu
import aktual.core.ui.ThemedDropdownMenuItem
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.normalIconButton
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

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

  EditRuleScaffold(
    modifier = modifier,
    mode = if (id == null) Mode.Create else Mode.Edit,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is Delete -> viewModel.delete(action.id)
      }
    },
  )
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
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        title = { EditRuleTitle(mode) },
        actions = { EditRuleActions(state, onAction) },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      EditRuleContent(
        modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
        contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
        state = state,
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
        Mode.Create -> Strings.editRulesToolbarCreate
        Mode.Edit -> Strings.editRulesToolbarEdit
      },
  )
}

@Composable
@Suppress("UnusedReceiverParameter")
private fun RowScope.EditRuleActions(state: EditRuleState, onAction: EditRuleActionHandler) {
  var showMenu by remember { mutableStateOf(false) }
  MoreButton(state, onClick = { showMenu = !showMenu })
  MoreMenu(state, showMenu, onAction, onDismissRequest = { showMenu = false })
}

@Composable
private fun MoreButton(state: EditRuleState, onClick: () -> Unit, modifier: Modifier = Modifier) {
  BasicIconButton(
    modifier = modifier,
    onClick = onClick,
    enabled = state is EditRuleState.Loaded,
    imageVector = MaterialIcons.MoreVert,
    contentDescription = Strings.editRulesMenu,
    colors = { theme, isPressed -> theme.normalIconButton(isPressed) },
  )
}

@Composable
private fun MoreMenu(
  state: EditRuleState,
  showMenu: Boolean,
  onAction: EditRuleActionHandler,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ThemedDropdownMenu(
    modifier = modifier,
    expanded = showMenu,
    onDismissRequest = onDismissRequest,
  ) {
    val string = Strings.editRulesActionDelete
    val red = LocalTheme.current.errorText
    ThemedDropdownMenuItem(
      enabled = state is EditRuleState.Loaded,
      text = { Text(string, color = red) },
      onClick = {
        val loaded = state as EditRuleState.Loaded
        onDismissRequest()
        onAction(Delete(loaded.rule.id))
      },
      leadingIcon = {
        Icon(imageVector = MaterialIcons.DeleteForever, contentDescription = string, tint = red)
      },
    )
  }
}

@Suppress("unused")
@Composable
private fun EditRuleContent(
  state: EditRuleState,
  contentPadding: PaddingValues,
  onAction: EditRuleActionHandler,
  modifier: Modifier = Modifier,
) {
  // TBC
}
