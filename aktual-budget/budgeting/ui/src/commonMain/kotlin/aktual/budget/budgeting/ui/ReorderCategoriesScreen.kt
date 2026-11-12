package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.ReorderCategoriesState
import aktual.budget.budgeting.vm.ReorderCategoriesViewModel
import aktual.budget.budgeting.vm.ReorderCategory
import aktual.budget.budgeting.vm.ReorderGroup
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.icons.material.DragIndicator
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.UnfoldLess
import aktual.core.icons.material.UnfoldMore
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BasicTextButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.FailureScreen
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.buttonTextStyle
import aktual.core.ui.disabled
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.normalButton
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.showErrorSnackbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ReorderCategoriesScreen(
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: ReorderCategoriesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current
  val saveFailed = Strings.budgetingReorderSaveFailed

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        Saved -> back()
        SaveFailed -> snackbars.show { showErrorSnackbar(saveFailed) }
      }
    }
  }

  ReorderCategoriesScaffold(
    modifier = modifier,
    state = state,
    onBack = { back() },
    onMoveCategory = viewModel::moveCategory,
    onMoveGroup = viewModel::moveGroup,
    onDiscard = viewModel::discard,
    onSave = viewModel::save,
  )
}

@Composable
internal fun ReorderCategoriesScaffold(
  state: ReorderCategoriesState,
  onBack: () -> Unit,
  onMoveCategory: (CategoryId, CategoryGroupId, Int) -> Unit,
  onMoveGroup: (CategoryGroupId, Int) -> Unit,
  onDiscard: () -> Unit,
  onSave: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val loaded = state as? Loaded
  val hasChanges = loaded?.hasChanges == true
  var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
  val requestBack = { if (hasChanges) showDiscardDialog = true else onBack() }
  BackHandler(enabled = hasChanges) { showDiscardDialog = true }

  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val moveCategory by rememberUpdatedState(onMoveCategory)
  val moveGroup by rememberUpdatedState(onMoveGroup)
  val reorder =
    remember(listState, scope) {
      ReorderListState(listState, scope) { row, groups ->
        commitMove(row, groups, moveCategory, moveGroup)
      }
    }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = requestBack) },
        title = { Text(text = Strings.budgetingReorder, maxLines = 1, overflow = Ellipsis) },
        actions = {
          if (loaded != null) {
            val allCollapsed = loaded.groups.all { it.id in reorder.collapsed }
            AktualIconButton(
              imageVector =
                if (allCollapsed) MaterialIcons.UnfoldMore else MaterialIcons.UnfoldLess,
              contentDescription =
                if (allCollapsed) {
                  Strings.budgetingReorderExpandAll
                } else {
                  Strings.budgetingReorderCollapseAll
                },
              onClick = { reorder.setAllCollapsed(!allCollapsed) },
            )
          }
        },
      )
    },
    bottomBar = {
      if (loaded != null) {
        ReorderBottomBar(
          hasChanges = loaded.hasChanges,
          isSaving = loaded.isSaving,
          onDiscard = onDiscard,
          onSave = onSave,
        )
      }
    },
  ) { innerPadding ->
    when (state) {
      Loading ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Center) {
          CircularProgressIndicator(color = colors.pageTextSubdued)
        }

      Failed ->
        FailureScreen(
          modifier = Modifier.padding(innerPadding),
          title = Strings.budgetingReorderFailed,
          reason = null,
          action = null,
        )

      is Loaded ->
        Box(modifier = Modifier.hazedTopBarContent(hazeState, innerPadding)) {
          ReorderList(
            groups = state.groups,
            reorder = reorder,
            listState = listState,
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
          )
        }
    }
  }

  if (showDiscardDialog) {
    DiscardDialog(
      onCancel = { showDiscardDialog = false },
      onDiscard = {
        showDiscardDialog = false
        onDiscard()
        onBack()
      },
    )
  }
}

private fun commitMove(
  row: ReorderRow,
  groups: List<ReorderGroup>,
  onMoveCategory: (CategoryId, CategoryGroupId, Int) -> Unit,
  onMoveGroup: (CategoryGroupId, Int) -> Unit,
) {
  when (row) {
    is HeaderRow -> {
      val index = groups.filter { !it.isIncome }.indexOfFirst { it.id == row.group.id }
      onMoveGroup(row.group.id, index)
    }

    is ItemRow -> {
      val id = row.category.id
      val group = groups.first { group -> group.categories.any { it.id == id } }
      onMoveCategory(id, group.id, group.categories.indexOfFirst { it.id == id })
    }
  }
}

@Composable
private fun ReorderList(
  groups: ImmutableList<ReorderGroup>,
  reorder: ReorderListState,
  listState: LazyListState,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  SideEffect { reorder.groups = groups }

  val rows = reorder.rows(groups)
  val haptics = LocalHapticFeedback.current
  val density = LocalDensity.current
  val edge = with(density) { AutoScrollEdge.toPx() }
  val speed = with(density) { AutoScrollSpeed.toPx() }
  val moveUp = Strings.budgetingReorderMoveUp
  val moveDown = Strings.budgetingReorderMoveDown

  val held = reorder.held
  LaunchedEffect(held) { if (held != null) reorder.autoScroll(held, edge, speed) }

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
  ) {
    item(key = "hint") {
      Text(
        modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
        text = Strings.budgetingReorderHint,
        fontSize = textSizes.smallPlus,
        color = colors.pageTextSubdued,
      )
    }

    var previousIncome = false
    rows.fastForEach { row ->
      val isIncomeHeader = row is HeaderRow && row.group.isIncome
      if (isIncomeHeader && !previousIncome) {
        item(key = "income") {
          Text(
            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 12.dp),
            text = Strings.budgetingReorderIncomeLocked,
            fontSize = textSizes.smallPlus,
            color = colors.pageTextSubdued,
          )
        }
      }
      previousIncome = previousIncome || isIncomeHeader

      item(key = row.key, contentType = row::class) {
        val isDragged = reorder.dragged == row.key
        val elevation by animateDpAsState(if (reorder.held == row.key) 8.dp else 0.dp)
        val rowModifier =
          Modifier.zIndex(if (isDragged) 1f else 0f)
            .then(if (isDragged) Modifier else Modifier.animateItem())
            .graphicsLayer { translationY = reorder.offsetOf(row.key) }
            .shadow(elevation)
            .semantics {
              customActions = buildList {
                if (reorder.canMoveBy(row, -1)) {
                  add(CustomAccessibilityAction(moveUp) { true.also { reorder.moveBy(row, -1) } })
                }
                if (reorder.canMoveBy(row, 1)) {
                  add(CustomAccessibilityAction(moveDown) { true.also { reorder.moveBy(row, 1) } })
                }
              }
            }
        val handle = Modifier.reorderHandle(reorder, row, haptics)

        when (row) {
          is HeaderRow ->
            ReorderHeader(
              modifier = rowModifier,
              row = row,
              handleModifier = handle,
              onToggle = { reorder.toggle(row.group.id) },
            )

          is ItemRow -> ReorderItem(modifier = rowModifier, row = row, handleModifier = handle)
        }
      }
    }

    item(key = "bottom") { Spacer(Modifier.size(Dimens.ScreenPadding)) }
  }
}

@Composable
private fun ReorderHeader(
  row: HeaderRow,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
  handleModifier: Modifier = Modifier,
) {
  val group = row.group
  val label =
    if (row.isCollapsed) {
      Strings.budgetingGroupExpand(group.name)
    } else {
      Strings.budgetingGroupCollapse(group.name)
    }
  val rotation by animateFloatAsState(if (row.isCollapsed) COLLAPSED_ROTATION else 0f)

  Column(modifier = modifier.fillMaxWidth().background(colors.tableRowHeaderBackground)) {
    HorizontalDivider()
    TableRow(
      modifier = Modifier.clickable(onClickLabel = label, role = Button, onClick = onToggle),
      height = BudgetDS.headerHeight,
    ) {
      Row(
        modifier = Modifier.weight(1f).alpha(if (group.isHidden) HIDDEN_ALPHA else 1f),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = CenterVertically,
      ) {
        Icon(
          modifier = Modifier.size(16.dp).rotate(rotation),
          imageVector = AktualIcons.CheveronDown,
          contentDescription = null,
          tint = colors.tableRowHeaderText,
        )
        Text(
          modifier = Modifier.weight(1f, fill = false),
          text = group.name,
          fontSize = textSizes.medium,
          fontWeight = SemiBold,
          color = colors.tableRowHeaderText,
          maxLines = 1,
          overflow = Ellipsis,
        )
        if (row.isCollapsed) {
          val count = group.categories.size
          Text(
            text = Plurals.budgetingReorderCategoryCount(count, count),
            fontSize = textSizes.small,
            color = colors.tableTextSubdued,
            maxLines = 1,
          )
        }
        if (group.isHidden) HiddenLabel()
      }

      if (group.isIncome) {
        Spacer(Modifier.width(HandleWidth))
      } else {
        DragHandle(modifier = handleModifier, name = group.name)
      }
    }
  }
}

@Composable
private fun ReorderItem(
  row: ItemRow,
  modifier: Modifier = Modifier,
  handleModifier: Modifier = Modifier,
) {
  val category = row.category
  Column(modifier = modifier.fillMaxWidth().background(colors.tableBackground)) {
    HorizontalDivider(color = colors.tableBorder)
    TableRow(height = BudgetDS.rowHeight) {
      Row(
        modifier =
          Modifier.weight(1f)
            .padding(start = BudgetDS.categoryIndent - BudgetDS.rowPadding)
            .alpha(if (category.isHidden) HIDDEN_ALPHA else 1f),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = CenterVertically,
      ) {
        Text(
          modifier = Modifier.weight(1f, fill = false),
          text = category.name,
          fontSize = textSizes.medium,
          color = colors.tableText,
          maxLines = 1,
          overflow = Ellipsis,
        )
        if (category.isHidden) HiddenLabel()
      }
      DragHandle(modifier = handleModifier, name = category.name)
    }
  }
}

@Composable
private fun DragHandle(name: String, modifier: Modifier = Modifier) {
  Box(modifier = modifier.width(HandleWidth).fillMaxHeight(), contentAlignment = Center) {
    Icon(
      modifier = Modifier.size(20.dp),
      imageVector = MaterialIcons.DragIndicator,
      contentDescription = Strings.budgetingReorderDrag(name),
      tint = colors.tableTextSubdued,
    )
  }
}

@Composable
private fun HiddenLabel(modifier: Modifier = Modifier) =
  Text(
    modifier = modifier,
    text = Strings.budgetingReorderHidden,
    fontSize = textSizes.small,
    color = colors.tableTextSubdued,
    maxLines = 1,
  )

@Composable
private fun ReorderBottomBar(
  hasChanges: Boolean,
  isSaving: Boolean,
  onDiscard: () -> Unit,
  onSave: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth().background(colors.pageBackground)) {
    HorizontalDivider()
    Row(
      modifier = Modifier.fillMaxWidth().padding(Dimens.ScreenPadding),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = CenterVertically,
    ) {
      Text(
        modifier = Modifier.weight(1f),
        text =
          if (hasChanges) Strings.budgetingReorderUnsaved else Strings.budgetingReorderNoChanges,
        fontSize = textSizes.medium,
        color = if (hasChanges) colors.pageText else colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )

      val canDiscard = hasChanges && !isSaving
      BasicTextButton(
        text = Strings.budgetingReorderDiscard,
        onClick = onDiscard,
        isEnabled = canDiscard,
        style = typography.buttonTextStyle,
        border =
          BorderStroke(
            Hairline,
            if (canDiscard) colors.errorText else colors.buttonNormalBorder.disabled,
          ),
        colors = { pressed -> colors.normalButton(pressed, text = colors.errorText) },
      )

      AktualButton(
        kind = Primary,
        text = Strings.budgetingReorderSave,
        isLoading = isSaving,
        isEnabled = hasChanges,
        onClick = onSave,
      )
    }
    BottomSpacing()
  }
}

@Composable
private fun DiscardDialog(onCancel: () -> Unit, onDiscard: () -> Unit) {
  AktualAlertDialog(
    title = Strings.budgetingReorderDiscardTitle,
    highlight = colors.warningText,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.budgetingReorderKeepEditing) }
      TextButton(onClick = onDiscard) {
        Text(Strings.budgetingReorderDiscard, color = colors.warningText)
      }
    },
    content = { Text(Strings.budgetingReorderDiscardMessage) },
  )
}

private val HandleWidth = 40.dp
private val AutoScrollEdge = 64.dp
private val AutoScrollSpeed = 800.dp

private val PREVIEW_REORDER =
  ReorderCategoriesState.Loaded(
    groups =
      persistentListOf(
        previewGroup(
          "usual",
          "Usual Expenses",
          previewCategory("food", "Food"),
          previewCategory("fun", "Fun"),
          previewCategory("old", "Old subscription", isHidden = true),
        ),
        previewGroup("bills", "Bills", previewCategory("rent", "Rent")),
        previewGroup("income", "Income", previewCategory("salary", "Salary"), isIncome = true),
      ),
    hasChanges = false,
    isSaving = false,
  )

private fun previewCategory(id: String, name: String, isHidden: Boolean = false) =
  ReorderCategory(CategoryId(id), name, isHidden)

private fun previewGroup(
  id: String,
  name: String,
  vararg categories: ReorderCategory,
  isIncome: Boolean = false,
) =
  ReorderGroup(
    id = CategoryGroupId(id),
    name = name,
    isIncome = isIncome,
    isHidden = false,
    categories = persistentListOf(*categories),
  )

private class ReorderCategoriesStateProvider :
  ColorParamsProvider<ReorderCategoriesState>(
    PREVIEW_REORDER,
    PREVIEW_REORDER.copy(hasChanges = true),
    PREVIEW_REORDER.copy(hasChanges = true, isSaving = true),
    Failed,
  )

@PortraitPreview
@Composable
private fun PreviewReorderCategories(
  @PreviewParameter(ReorderCategoriesStateProvider::class)
  params: ColorParams<ReorderCategoriesState>,
) {
  PreviewWithColorParams(params) {
    ReorderCategoriesScaffold(
      state = this,
      onBack = {},
      onMoveCategory = { _, _, _ -> },
      onMoveGroup = { _, _ -> },
      onDiscard = {},
      onSave = {},
    )
  }
}
