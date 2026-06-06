package aktual.budget.navrail.ui.edit

import aktual.budget.navrail.ui.NavSheetItem
import aktual.budget.navrail.ui.icon
import aktual.budget.navrail.ui.label
import aktual.budget.navrail.vm.edit.EditNavGridState
import aktual.budget.navrail.vm.edit.EditNavGridViewModel
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Save
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetTab
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameters
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.toImmutableList
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

@Composable
internal fun EditNavGridScreen(
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditNavGridViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  var showDiscardDialog by remember { mutableStateOf(false) }

  EditNavGridScaffold(
    modifier = modifier,
    state = state,
    showDiscardDialog = showDiscardDialog,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        Save -> viewModel.save()
        ResetToSaved -> viewModel.resetToSaved()
        ResetToDefault -> viewModel.resetToDefault()
        is Move -> viewModel.move(action.from, action.to)
        ShowDiscardDialog -> showDiscardDialog = true
        DismissDiscardDialog -> showDiscardDialog = false
      }
    },
  )
}

@Composable
private fun EditNavGridScaffold(
  state: EditNavGridState,
  showDiscardDialog: Boolean,
  onAction: ActionHandler,
  modifier: Modifier = Modifier,
) {
  var showMenu by remember { mutableStateOf(false) }

  // Intercept the system back so unsaved changes prompt a confirmation first
  BackHandler(enabled = state.hasChanges) { onAction(ShowDiscardDialog) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = {
          if (state.hasChanges) {
            // Unsaved changes: a red cross that prompts the discard confirmation
            IconButton(onClick = { onAction(ShowDiscardDialog) }) {
              Icon(
                imageVector = MaterialIcons.Clear,
                contentDescription = Strings.budgetNavGridDiscardDiscard,
                tint = colors.errorText,
              )
            }
          } else {
            NavBackIconButton { onAction(NavBack) }
          }
        },
        title = {
          Text(
            text = Strings.budgetNavGridEditTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        actions = {
          BareIconButton(
            imageVector = MaterialIcons.Save,
            contentDescription = Strings.budgetNavGridSave,
            enabled = state.hasChanges,
            onClick = { onAction(Save) },
          )
          Box {
            BareIconButton(
              imageVector = MaterialIcons.MoreVert,
              contentDescription = Strings.budgetNavGridMoreActions,
              onClick = { showMenu = true },
            )
            AktualDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
              AktualDropdownMenuItem(
                text = Strings.budgetNavGridResetSaved,
                leadingIcon = MaterialIcons.Refresh,
                enabled = state.hasChanges,
                onClick = {
                  showMenu = false
                  onAction(ResetToSaved)
                },
              )
              AktualDropdownMenuItem(
                text = Strings.budgetNavGridResetDefault,
                leadingIcon = MaterialIcons.Refresh,
                enabled = !state.isDefault,
                onClick = {
                  showMenu = false
                  onAction(ResetToDefault)
                },
              )
            }
          }
        },
      )
    },
  ) { padding ->
    EditNavGridContent(
      state = state,
      onAction = onAction,
      modifier = Modifier.padding(padding),
    )
  }

  if (showDiscardDialog) {
    DiscardChangesDialog(
      onSave = {
        onAction(DismissDiscardDialog)
        onAction(Save)
        onAction(NavBack)
      },
      onDiscard = {
        onAction(DismissDiscardDialog)
        onAction(NavBack)
      },
      onCancel = { onAction(DismissDiscardDialog) },
    )
  }
}

@Composable
private fun EditNavGridContent(
  state: EditNavGridState,
  onAction: ActionHandler,
  modifier: Modifier = Modifier,
) {
  val lazyGridState = rememberLazyGridState()
  val reorderState =
    rememberReorderableLazyGridState(lazyGridState) { from, to ->
      onAction(Move(from.index, to.index))
    }
  val jiggle = rememberJiggleAngle()

  LazyVerticalGrid(
    state = lazyGridState,
    columns = GridCells.Fixed(BudgetTab.tabs.size),
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(8.dp),
  ) {
    itemsIndexed(state.items, key = { _, tab -> tab.name }) { index, tab ->
      ReorderableItem(reorderState, key = tab.name) { isDragging ->
        // Even/odd items jiggle in opposite directions for a livelier "editable" feel; the
        // actively-dragged item stops jiggling and lifts slightly instead. Use the visual grid
        // index, not tab.ordinal, so the alternation tracks position after reordering
        val rotation = if (isDragging) 0f else jiggle * if (index % 2 == 0) 1f else -1f
        val scale = if (isDragging) DRAG_SCALE else 1f

        // The item wraps its content, so center it within the wider grid cell
        Box(
          modifier =
            Modifier.padding(Dimens.Medium)
              .fillMaxWidth()
              .longPressDraggableHandle()
              .background(
                color = if (isDragging) colors.pillBackgroundSelected else colors.pillBackground,
                shape = CardShape,
              ),
          contentAlignment = Alignment.Center,
        ) {
          NavSheetItem(
            modifier =
              Modifier.graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
              },
            icon = tab.icon(),
            label = tab.label(),
            selected = false,
            onClick = null,
          )
        }
      }
    }
  }
}

@Composable
private fun DiscardChangesDialog(
  onSave: () -> Unit,
  onDiscard: () -> Unit,
  onCancel: () -> Unit,
) {
  AktualAlertDialog(
    title = Strings.budgetNavGridDiscardTitle,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.budgetNavGridDiscardCancel) }
      TextButton(onClick = onDiscard) { Text(Strings.budgetNavGridDiscardDiscard) }
      TextButton(onClick = onSave) { Text(Strings.budgetNavGridDiscardSave) }
    },
    content = { Text(Strings.budgetNavGridDiscardMessage) },
  )
}

@Composable
private fun rememberJiggleAngle(): Float {
  val transition = rememberInfiniteTransition(label = "jiggle")

  val angle by
    transition.animateFloat(
      initialValue = -JIGGLE_DEGREES,
      targetValue = JIGGLE_DEGREES,
      animationSpec = JIGGLE_ANIMATION_SPEC,
      label = "angle",
    )
  return angle
}

private const val JIGGLE_DEGREES = 2.5f
private const val JIGGLE_PERIOD_MS = 140
private const val DRAG_SCALE = 1.05f

private val JIGGLE_ANIMATION_SPEC =
  infiniteRepeatable<Float>(
    animation = tween(durationMillis = JIGGLE_PERIOD_MS, easing = LinearEasing),
    repeatMode = RepeatMode.Reverse,
  )

@PortraitPreview
@Composable
private fun PreviewEditNavGridScreen(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    EditNavGridScaffold(
      onAction = {},
      showDiscardDialog = false,
      state =
        EditNavGridState(
          items = BudgetTab.entries.toImmutableList(),
          hasChanges = true,
          isDefault = false,
        ),
    )
  }

@PortraitPreview
@Composable
private fun PreviewEditNavGridDiscardDialog(
  @PreviewParameter(ColoredParameters::class) colors: Colors
) =
  PreviewWithColors(colors) {
    EditNavGridScaffold(
      onAction = {},
      showDiscardDialog = true,
      state =
        EditNavGridState(
          items = BudgetTab.entries.toImmutableList(),
          hasChanges = true,
          isDefault = false,
        ),
    )
  }
