package aktual.budget.tags.ui.edit

import aktual.budget.model.TagId
import aktual.budget.tags.vm.edit.EditTagEvent
import aktual.budget.tags.vm.edit.EditTagState
import aktual.budget.tags.vm.edit.EditTagViewModel
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Save
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BlurredTopBarState
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LoadingScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun EditTagScreen(
  id: TagId?,
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditTagViewModel = editTagViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsStateWithLifecycle()
  val canSave by viewModel.canSave.collectAsStateWithLifecycle()

  // navigate away only once the tag has actually been persisted
  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        EditTagEvent.FinishedSaving -> back()
      }
    }
  }

  EditTagScaffold(
    modifier = modifier,
    state = state,
    hasChanges = hasUnsavedChanges,
    canSave = canSave,
    onAction = { action ->
      when (action) {
        SaveTag -> viewModel.save()
        is SetTag -> viewModel.setTag(action.tag)
        is SetDescription -> viewModel.setDescription(action.description)
        is SetColor -> viewModel.setColor(action.color)
        is SetColorError -> viewModel.setColorError(action.isError)
        NavigateBack -> back()
      }
    },
  )
}

@Composable
private fun editTagViewModel(id: TagId?) =
  assistedMetroViewModel<EditTagViewModel, EditTagViewModel.Factory>(key = id.toString()) {
    create(id)
  }

@Composable
private fun EditTagScaffold(
  state: EditTagState,
  hasChanges: Boolean,
  canSave: Boolean,
  onAction: EditTagActionHandler,
  modifier: Modifier = Modifier,
) {
  val editing = state as? EditTagState.Editing

  val tagState = rememberTextFieldState()
  val descriptionState = rememberTextFieldState()

  // seed the fields once, when the tag finishes loading
  LaunchedEffect(editing != null) {
    val loaded = editing ?: return@LaunchedEffect
    tagState.setTextAndPlaceCursorAtEnd(loaded.initialTag)
    descriptionState.setTextAndPlaceCursorAtEnd(loaded.initialDescription)
  }

  // mirror field edits into the view model, which owns the working state + change tracking
  LaunchedEffect(Unit) {
    snapshotFlow { tagState.text.toString() }.collect { onAction(SetTag(it)) }
  }
  LaunchedEffect(Unit) {
    snapshotFlow { descriptionState.text.toString() }.collect { onAction(SetDescription(it)) }
  }

  var showDiscardDialog by remember { mutableStateOf(false) }

  // intercept the system back so unsaved changes prompt a confirmation first
  BackHandler(enabled = hasChanges) { showDiscardDialog = true }

  val blurState = rememberBlurredTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      EditTagTopBar(
        editing = editing,
        hasChanges = hasChanges,
        saveEnabled = canSave,
        blurState = blurState,
        scrollState = scrollState,
        onAction = onAction,
        onRequestDiscard = { showDiscardDialog = true },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        EditTagState.Loading -> LoadingScreen(modifier = Modifier.padding(innerPadding))

        is EditTagState.Editing ->
          EditTagContent(
            modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
            tagState = tagState,
            descriptionState = descriptionState,
            color = state.color,
            onColorChange = { onAction(SetColor(it)) },
            onColorError = { onAction(SetColorError(it)) },
            scrollState = scrollState,
            contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
          )

        is EditTagState.Failure ->
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.tagsEditFailurePrefix,
            reason = state.cause ?: Strings.tagsEditNotFound,
            action =
              FailureAction(
                text = { Strings.navBack },
                icon = MaterialIcons.ArrowBack,
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
    }
  }
}

@Composable
private fun EditTagTopBar(
  editing: EditTagState.Editing?,
  hasChanges: Boolean,
  saveEnabled: Boolean,
  blurState: BlurredTopBarState,
  scrollState: ScrollState,
  onAction: EditTagActionHandler,
  onRequestDiscard: () -> Unit,
) {
  TopAppBar(
    modifier = Modifier.blurredTopBar(blurState, scrollOffset = { scrollState.value.toFloat() }),
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      if (hasChanges) {
        // unsaved changes: a red cross that prompts the discard confirmation
        IconButton(onClick = onRequestDiscard) {
          Icon(
            imageVector = MaterialIcons.Clear,
            contentDescription = Strings.tagsDiscardConfirm,
            tint = colors.errorText,
          )
        }
      } else {
        NavBackIconButton(onClick = { onAction(NavigateBack) })
      }
    },
    title = {
      editing?.let {
        Text(text = if (it.isNew) Strings.tagsCreateTitle else Strings.tagsEditTitle)
      }
    },
    actions = {
      if (editing != null) {
        SaveButton(onAction = onAction, enabled = saveEnabled)
      }
    },
  )
}

@Composable
private fun DiscardChangesDialog(onDiscard: () -> Unit, onCancel: () -> Unit) {
  AktualAlertDialog(
    title = Strings.tagsDiscardTitle,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.tagsDiscardCancel) }
      TextButton(onClick = onDiscard) { Text(Strings.tagsDiscardConfirm) }
    },
    content = { Text(Strings.tagsDiscardMessage) },
  )
}

@Composable
private fun EditTagContent(
  tagState: TextFieldState,
  descriptionState: TextFieldState,
  color: Color?,
  onColorChange: (Color) -> Unit,
  onColorError: (Boolean) -> Unit,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(EditTagDS.contentPadding),
    verticalArrangement = Arrangement.spacedBy(EditTagDS.fieldSpacing),
  ) {
    Field(label = Strings.tagsCreateTagLabel, required = true) {
      val isBlank = tagState.text.isBlank()
      AktualTextField(
        modifier = Modifier.fillMaxWidth(),
        state = tagState,
        singleLine = true,
        placeholderText = Strings.tagsCreateTagPlaceholder,
        supportingText =
          if (isBlank) {
            { Text(text = Strings.tagsCreateTagRequired, color = colors.errorText) }
          } else {
            null
          },
      )
    }

    Field(label = Strings.tagsCreateDescriptionLabel) {
      AktualTextField(
        modifier = Modifier.fillMaxWidth(),
        state = descriptionState,
        placeholderText = Strings.tagsCreateDescriptionPlaceholder,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
      )
    }

    Field(label = Strings.tagsCreateColorLabel) {
      TagColorPicker(
        modifier = Modifier.fillMaxWidth(),
        color = color,
        onColorChange = onColorChange,
        onErrorChange = onColorError,
      )
    }

    BottomSpacing()
  }
}

@Composable
private fun SaveButton(
  onAction: EditTagActionHandler,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  BareIconButton(
    modifier = modifier,
    imageVector = MaterialIcons.Save,
    contentDescription = Strings.tagsCreateSave,
    enabled = enabled,
    onClick = { onAction(SaveTag) },
  )
}

@Composable
private inline fun Field(
  label: String,
  required: Boolean = false,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(EditTagDS.labelSpacing)) {
    val labelText =
      if (required) {
        buildAnnotatedString {
          append(label)
          withStyle(SpanStyle(color = colors.errorText)) { append(" *") }
        }
      } else {
        AnnotatedString(label)
      }
    Text(text = labelText, style = typography.labelLarge, color = colors.pageTextSubdued)
    content()
  }
}

@PortraitPreview
@Composable
private fun PreviewEditTagScreen(
  @PreviewParameter(EditTagStateProvider::class) params: ColoredParams<EditTagState>
) =
  PreviewWithColoredParams(params) {
    EditTagScaffold(state = this, hasChanges = false, canSave = true, onAction = {})
  }

@Suppress("MagicNumber")
private class EditTagStateProvider :
  ColoredParameterProvider<EditTagState>(
    EditTagState.Loading,
    EditTagState.Editing(isNew = true),
    EditTagState.Editing(initialTag = "work", isNew = false),
    EditTagState.Editing(
      initialTag = "groceries",
      initialDescription = "Food and household shopping",
      color = Color(0xFF388E3C),
      isNew = false,
    ),
    EditTagState.Failure(cause = null),
  )
