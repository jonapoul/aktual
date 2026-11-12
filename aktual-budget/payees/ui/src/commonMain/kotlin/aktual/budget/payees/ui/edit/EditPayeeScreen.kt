package aktual.budget.payees.ui.edit

import aktual.budget.model.Condition
import aktual.budget.model.ConditionSet
import aktual.budget.model.ConditionsFilter
import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.ui.list.PayeeChip
import aktual.budget.payees.ui.list.PayeesPreview
import aktual.budget.payees.ui.list.RuleCountChip
import aktual.budget.payees.ui.openRules
import aktual.budget.payees.vm.edit.EditPayeeEvent.LocationDeleted
import aktual.budget.payees.vm.edit.EditPayeeState
import aktual.budget.payees.vm.edit.EditPayeeViewModel
import aktual.budget.payees.vm.edit.Editing
import aktual.budget.payees.vm.edit.Failure
import aktual.budget.payees.vm.edit.Loading
import aktual.budget.payees.vm.edit.SavedLocation
import aktual.budget.payees.vm.list.PayeeItem
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronLeft
import aktual.core.icons.Delete
import aktual.core.icons.Location
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Save
import aktual.core.l10n.Plurals
import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.payees_location_delete_failed
import aktual.core.l10n.payees_location_restore_failed
import aktual.core.model.Coordinates
import aktual.core.nav.BackNavigator
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.SearchRulesNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardColumn
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.HazedTopBarState
import aktual.core.ui.LocalDateFormatter
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.TextFieldShape
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.showErrorSnackbar
import aktual.core.ui.switch
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.getString

@Composable
internal fun EditPayeeScreen(
  id: PayeeId,
  back: BackNavigator,
  editRule: EditRuleNavigator,
  searchRules: SearchRulesNavigator,
  toTransactions: TransactionsNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditPayeeViewModel = editPayeeViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
  val learnsCategories by viewModel.learnsCategories.collectAsStateWithLifecycle()
  val isLearningEnabled by viewModel.isLearningEnabled.collectAsStateWithLifecycle()
  val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsStateWithLifecycle()
  val canSave by viewModel.canSave.collectAsStateWithLifecycle()
  val isDuplicateName by viewModel.isDuplicateName.collectAsStateWithLifecycle()
  val saveError by viewModel.saveError.collectAsStateWithLifecycle()
  val locations by viewModel.locations.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current
  val uriHandler = LocalUriHandler.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        FinishedSaving -> back()
        is LocationDeleted ->
          snackbars.show { showLocationDeleted(event, onUndo = viewModel::restoreLocation) }
        LocationDeleteFailed ->
          snackbars.show {
            showErrorSnackbar(getString(Res.string.payees_location_delete_failed))
          }
        LocationRestoreFailed ->
          snackbars.show {
            showErrorSnackbar(getString(Res.string.payees_location_restore_failed))
          }
      }
    }
  }

  EditPayeeScaffold(
    modifier = modifier,
    state = state,
    isFavorite = isFavorite,
    learnsCategories = learnsCategories,
    isLearningEnabled = isLearningEnabled,
    hasChanges = hasUnsavedChanges,
    canSave = canSave,
    isDuplicateName = isDuplicateName,
    saveError = saveError,
    locations = locations,
    onAction = { action ->
      when (action) {
        NavigateBack -> back()
        SavePayee -> viewModel.save()
        DismissSaveError -> viewModel.dismissSaveError()
        is SetName -> viewModel.setName(action.name)
        is SetFavorite -> viewModel.setFavorite(action.isFavorite)
        is SetLearnCategories -> viewModel.setLearnCategories(action.learn)
        is OpenRules -> openRules(action.payee, editRule, searchRules)
        is OpenTransactions -> toTransactions(action.title, payeeFilter(id))
        is OpenLocation -> uriHandler.openUri(action.coordinates.mapUrl())
        is DeleteLocation -> viewModel.deleteLocation(action.id)
      }
    },
  )
}

@Composable
private fun editPayeeViewModel(id: PayeeId) =
  assistedMetroViewModel<EditPayeeViewModel, EditPayeeViewModel.Factory>(key = id.value) {
    create(id)
  }

private fun payeeFilter(id: PayeeId) =
  ConditionsFilter(
    listOf(
      ConditionSet(
        listOf(Condition(field = Payee, operator = Is, type = Id, value = JsonPrimitive(id.value))),
      ),
    ),
  )

@Composable
private fun EditPayeeScaffold(
  state: EditPayeeState,
  isFavorite: Boolean,
  hasChanges: Boolean,
  canSave: Boolean,
  onAction: OnAction<EditPayeeAction>,
  modifier: Modifier = Modifier,
  learnsCategories: Boolean = false,
  isLearningEnabled: Boolean = false,
  isDuplicateName: Boolean = false,
  saveError: String? = null,
  locations: ImmutableList<SavedLocation> = persistentListOf(),
) {
  val editing = state as? Editing
  val nameState = rememberTextFieldState()

  // seed the field once, when the payee finishes loading
  SideEffect(editing != null) {
    val loaded = editing ?: return@SideEffect
    nameState.setTextAndPlaceCursorAtEnd(loaded.payee.name)
  }

  LaunchedEffect(Unit) {
    snapshotFlow { nameState.text.toString() }.collect { onAction(SetName(it)) }
  }

  var showDiscardDialog by remember { mutableStateOf(false) }
  BackHandler(enabled = hasChanges) { showDiscardDialog = true }

  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      EditPayeeTopBar(
        editing = editing,
        hasChanges = hasChanges,
        saveEnabled = canSave,
        hazeState = hazeState,
        scrollState = scrollState,
        onAction = onAction,
        onRequestDiscard = { showDiscardDialog = true },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        Loading ->
          ShimmerEditPayee(
            numLocations = locations.size,
            modifier = Modifier.padding(innerPadding),
          )

        is Editing ->
          EditPayeeContent(
            modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
            payee = state.payee,
            nameState = nameState,
            isFavorite = isFavorite,
            learnsCategories = learnsCategories,
            isLearningEnabled = isLearningEnabled,
            isDuplicateName = isDuplicateName,
            locations = locations,
            scrollState = scrollState,
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
            onAction = onAction,
          )

        is Failure ->
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.payeesEditFailurePrefix,
            reason = state.cause ?: Strings.payeesEditNotFound,
            action =
              FailureAction(
                text = { Strings.navBack },
                icon = AktualIcons.CheveronLeft,
                onClick = { onAction(NavigateBack) },
              ),
          )
      }

      if (showDiscardDialog) {
        DiscardChangesDialog(
          onDiscard = {
            showDiscardDialog = false
            onAction(NavigateBack)
          },
          onCancel = { showDiscardDialog = false },
        )
      }

      if (saveError != null) {
        SaveErrorDialog(message = saveError, onDismiss = { onAction(DismissSaveError) })
      }
    }
  }
}

@Composable
private fun EditPayeeTopBar(
  editing: Editing?,
  hasChanges: Boolean,
  saveEnabled: Boolean,
  hazeState: HazedTopBarState,
  scrollState: ScrollState,
  onAction: OnAction<EditPayeeAction>,
  onRequestDiscard: () -> Unit,
) {
  TopAppBar(
    modifier = Modifier.hazedTopBar(hazeState, scrollState),
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      if (hasChanges) {
        IconButton(onClick = onRequestDiscard) {
          Icon(
            imageVector = AktualIcons.Delete,
            contentDescription = Strings.payeesDiscardConfirm,
            tint = colors.errorText,
          )
        }
      } else {
        NavBackIconButton(onClick = { onAction(NavigateBack) })
      }
    },
    title = { if (editing != null) Text(text = Strings.payeesEditTitle) },
    actions = {
      if (editing != null && !editing.payee.isTransfer) {
        BareIconButton(
          imageVector = MaterialIcons.Save,
          contentDescription = Strings.payeesEditSave,
          enabled = saveEnabled,
          onClick = { onAction(SavePayee) },
        )
      }
    },
  )
}

@Composable
private fun DiscardChangesDialog(onDiscard: () -> Unit, onCancel: () -> Unit) {
  AktualAlertDialog(
    title = Strings.payeesDiscardTitle,
    highlight = colors.warningText,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.payeesDiscardCancel) }
      TextButton(onClick = onDiscard) {
        Text(Strings.payeesDiscardConfirm, color = colors.warningText)
      }
    },
    content = { Text(Strings.payeesDiscardMessage) },
  )
}

@Composable
private fun SaveErrorDialog(message: String, onDismiss: () -> Unit) {
  AktualAlertDialog(
    title = Strings.payeesSaveFailureTitle,
    highlight = colors.errorText,
    onDismissRequest = onDismiss,
    buttons = { TextButton(onClick = onDismiss) { Text(Strings.payeesSaveFailureDismiss) } },
    content = { Text(message) },
  )
}

/** Keep in sync with [ShimmerEditPayee] */
@Composable
private fun EditPayeeContent(
  payee: PayeeItem,
  nameState: TextFieldState,
  isFavorite: Boolean,
  learnsCategories: Boolean,
  isLearningEnabled: Boolean,
  isDuplicateName: Boolean,
  locations: ImmutableList<SavedLocation>,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  onAction: OnAction<EditPayeeAction>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(Dimens.ScreenPadding),
    verticalArrangement = Arrangement.spacedBy(EditPayeeDS.fieldSpacing),
  ) {
    EditPayeeCard {
      Field(label = Strings.payeesEditNameLabel, required = !payee.isTransfer) {
        val supporting =
          when {
            payee.isTransfer -> Strings.payeesEditTransferHelp to colors.pageTextSubdued
            nameState.text.isBlank() -> Strings.payeesEditNameRequired to colors.errorText
            isDuplicateName -> Strings.payeesEditNameDuplicate to colors.warningText
            else -> null
          }
        AktualTextField(
          modifier = Modifier.fillMaxWidth(),
          state = nameState,
          singleLine = true,
          readOnly = payee.isTransfer,
          placeholderText = Strings.payeesEditNamePlaceholder,
          keyboardOptions = KeyboardOptions(capitalization = Sentences),
          isError = !payee.isTransfer && nameState.text.isBlank(),
          supportingText =
            supporting?.let { (text, color) -> { Text(text = text, color = color) } },
        )
      }
    }

    EditPayeeCard {
      SwitchRow(
        label = Strings.payeesFavorite,
        help = Strings.payeesEditFavoriteHelp,
        checked = isFavorite,
        onCheckedChange = { onAction(SetFavorite(it)) },
        enabled = !payee.isTransfer,
      )

      // Upstream only offers the per-payee switch while the global pref is on
      if (isLearningEnabled) {
        SwitchRow(
          label = Strings.payeesEditLearnLabel,
          help = Strings.payeesEditLearnHelp,
          checked = learnsCategories,
          onCheckedChange = { onAction(SetLearnCategories(it)) },
          enabled = !payee.isTransfer,
        )
      }
    }

    EditPayeeCard {
      Field(label = Strings.payeesEditRulesLabel) {
        RuleCountChip(numRules = payee.numRules, onClick = { onAction(OpenRules(payee)) })
      }

      Field(label = Strings.payeesEditTransactionsLabel) {
        val title = if (payee.isTransfer) Strings.payeesTransfer(payee.name) else payee.name
        PayeeChip(
          text = Plurals.payeesNumTransactions(payee.numTransactions, payee.numTransactions),
          onClick = { onAction(OpenTransactions(title)) },
        )
      }
    }

    if (locations.isNotEmpty()) {
      EditPayeeCard { LocationsField(locations, onAction) }
    }

    BottomSpacing()
  }
}

@Composable
private fun LocationsField(
  locations: ImmutableList<SavedLocation>,
  onAction: OnAction<EditPayeeAction>,
) =
  Column(verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing)) {
    Text(
      text = Strings.payeesEditLocationsLabel,
      style = typography.labelLarge,
      color = colors.pageText,
    )
    Text(
      text = Strings.payeesEditLocationsHelp,
      style = typography.bodySmall,
      color = colors.pageTextSubdued,
    )
    locations.fastForEach { location -> LocationRow(location, onAction) }
  }

@Composable
private fun LocationRow(location: SavedLocation, onAction: OnAction<EditPayeeAction>) {
  val coordinates = location.coordinates
  val coordinatesText =
    Strings.payeesEditLocationCoordinates(
      coordinates.latitude.fiveDecimals(),
      coordinates.longitude.fiveDecimals(),
    )
  Row(
    modifier =
      Modifier.fillMaxWidth().clickable(onClickLabel = Strings.payeesEditLocationOpen) {
        onAction(OpenLocation(coordinates))
      },
    horizontalArrangement = Arrangement.spacedBy(EditPayeeDS.locationSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      imageVector = AktualIcons.Location,
      contentDescription = null,
      tint = colors.pageTextSubdued,
    )
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
      val date = location.date
      if (date != null) {
        Text(text = LocalDateFormatter.current.format(date), style = typography.bodyMedium)
      }
      Text(text = coordinatesText, style = typography.bodySmall, color = colors.pageTextSubdued)
    }
    BareIconButton(
      imageVector = AktualIcons.Delete,
      contentDescription = Strings.payeesEditLocationDelete,
      onClick = { onAction(DeleteLocation(location.id)) },
    )
  }
}

@Composable
private fun SwitchRow(
  label: String,
  help: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  enabled: Boolean,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(EditPayeeDS.switchSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing),
    ) {
      Text(text = label, style = typography.labelLarge, color = colors.pageText)
      Text(text = help, style = typography.bodySmall, color = colors.pageTextSubdued)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      colors = colors.switch(),
    )
  }
}

/** Keep in sync with [EditPayeeContent] */
@Composable
private fun ShimmerEditPayee(numLocations: Int, modifier: Modifier = Modifier) {
  val bar = Modifier.background(colors.pageText, CardShape)
  val label =
    bar.size(width = EditPayeeDS.shimmerLabelWidth, height = EditPayeeDS.shimmerLabelHeight)
  val chip = bar.size(width = EditPayeeDS.shimmerChipWidth, height = EditPayeeDS.shimmerChipHeight)

  Column(
    modifier =
      modifier.fillMaxSize().padding(Dimens.ScreenPadding).shimmer(rememberShimmer(Window)),
    verticalArrangement = Arrangement.spacedBy(EditPayeeDS.fieldSpacing),
  ) {
    Column(
      modifier = Modifier.padding(EditPayeeDS.cardPadding),
      verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing),
    ) {
      Box(label)
      Box(
        Modifier.background(colors.pageText, TextFieldShape)
          .fillMaxWidth()
          .height(EditPayeeDS.shimmerFieldHeight),
      )
    }

    repeat(times = 2) {
      Column(
        modifier = Modifier.padding(EditPayeeDS.cardPadding),
        verticalArrangement = Arrangement.spacedBy(EditPayeeDS.fieldSpacing),
      ) {
        repeat(times = 2) {
          Column(verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing)) {
            Box(label)
            Box(chip)
          }
        }
      }
    }

    if (numLocations > 0) {
      Column(
        modifier = Modifier.padding(EditPayeeDS.cardPadding),
        verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing),
      ) {
        Box(label)
        Box(bar.size(width = EditPayeeDS.shimmerHelpWidth, height = EditPayeeDS.shimmerTextHeight))
        repeat(numLocations) { ShimmerLocationRow() }
      }
    }
  }
}

@Composable
private fun ShimmerLocationRow() {
  val bar = Modifier.background(colors.pageText, CardShape)
  Row(
    modifier = Modifier.fillMaxWidth().height(EditPayeeDS.shimmerLocationHeight),
    horizontalArrangement = Arrangement.spacedBy(EditPayeeDS.locationSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Box(bar.size(EditPayeeDS.shimmerIconSize))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing),
    ) {
      Box(bar.size(width = EditPayeeDS.shimmerDateWidth, height = EditPayeeDS.shimmerTextHeight))
      Box(
        bar.size(
          width = EditPayeeDS.shimmerCoordinatesWidth,
          height = EditPayeeDS.shimmerTextHeight,
        ),
      )
    }
    Box(Modifier.size(EditPayeeDS.shimmerLocationHeight), contentAlignment = Center) {
      Box(bar.size(EditPayeeDS.shimmerIconSize))
    }
  }
}

@Composable
private fun EditPayeeCard(content: @Composable ColumnScope.() -> Unit) {
  CardColumn(
    modifier = Modifier.fillMaxWidth(),
    contentPadding = EditPayeeDS.cardPadding,
    verticalArrangement = Arrangement.spacedBy(EditPayeeDS.fieldSpacing),
    content = content,
  )
}

@Composable
private inline fun Field(
  label: String,
  required: Boolean = false,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing)) {
    val labelText =
      if (required) {
        buildAnnotatedString {
          append(label)
          withStyle(SpanStyle(color = colors.errorText)) { append(" *") }
        }
      } else {
        AnnotatedString(label)
      }
    Text(text = labelText, style = typography.labelLarge, color = colors.pageText)
    content()
  }
}

@PortraitPreview
@Composable
private fun PreviewEditPayeeScreen(
  @PreviewParameter(EditPayeeStateProvider::class) params: ColorParams<EditPayeeState>,
) =
  PreviewWithColorParams(params) {
    EditPayeeScaffold(
      state = this,
      isFavorite = (this as? Editing)?.payee?.isFavorite == true,
      learnsCategories = (this as? Editing)?.payee?.learnsCategories == true,
      isLearningEnabled = true,
      hasChanges = false,
      canSave = true,
      locations =
        PreviewLocations.takeIf { (this as? Editing)?.payee?.isTransfer == false }
          ?: persistentListOf(),
      onAction = {},
    )
  }

private val PreviewLocations =
  persistentListOf(
    SavedLocation(
      id = PayeeLocationId("a"),
      coordinates = Coordinates(latitude = 51.50735, longitude = -0.12776),
      date = LocalDate(2026, 11, 7),
    ),
    SavedLocation(
      id = PayeeLocationId("b"),
      coordinates = Coordinates(latitude = 51.51322, longitude = -0.08806),
      date = null,
    ),
  )

private class EditPayeeStateProvider :
  ColorParamsProvider<EditPayeeState>(
    Loading,
    Editing(PayeesPreview.tesco),
    Editing(PayeesPreview.transfer),
    Failure(cause = null),
  )

private fun Coordinates.mapUrl(): String =
  "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=$MAP_ZOOM/$latitude/$longitude"

private fun Double.fiveDecimals(): String {
  val scaled = (this * FIVE_DECIMALS).roundToLong()
  val whole = abs(scaled) / FIVE_DECIMALS.toLong()
  val fraction = (abs(scaled) % FIVE_DECIMALS.toLong()).toString().padStart(length = 5, '0')
  return "${if (scaled < 0) "-" else ""}$whole.$fraction"
}

private const val MAP_ZOOM = 18
private const val FIVE_DECIMALS = 100_000.0
