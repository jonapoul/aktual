package aktual.prefs.ui.inspect

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.disabled
import aktual.core.ui.keyboardFocusRequester
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun DuplicateThemeDialog(
  themeName: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val initialName = Strings.settingsThemeInspectDuplicateName(themeName)
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    DuplicateThemeDialogContent(rememberTextFieldState(initialName), onConfirm, onDismiss)
  }
}

@Composable
private fun DuplicateThemeDialogContent(
  state: TextFieldState,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val name = state.text.trim().toString()
  val enabled = name.isNotEmpty()
  AktualAlertDialogContent(
    title = Strings.settingsThemeInspectDuplicate,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.settingsThemeInspectCancel) }
      TextButton(enabled = enabled, onClick = { onConfirm(name) }) {
        Text(
          text = Strings.settingsThemeInspectDuplicateConfirm,
          fontWeight = if (enabled) Bold else null,
          color = if (enabled) colors.numberPositive else colors.buttonNormalDisabledText.disabled,
        )
      }
    },
    content = {
      AktualTextField(
        modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
        state = state,
        placeholderText = Strings.settingsThemeInspectDuplicatePlaceholder,
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = Sentences, imeAction = Done),
        onKeyboardAction = { if (enabled) onConfirm(name) },
      )
    },
  )
}

@Composable
internal fun DeleteThemeDialog(themeName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialog(onDismissRequest = onDismiss) {
    DeleteThemeDialogContent(themeName, onConfirm, onDismiss)
  }

@Composable
private fun DeleteThemeDialogContent(
  themeName: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialogContent(
    title = Strings.settingsThemeInspectDeleteTitle(themeName),
    highlight = colors.errorText,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.settingsThemeInspectCancel) }
      TextButton(onClick = onConfirm) {
        Text(
          text = Strings.settingsThemeInspectDeleteConfirm,
          fontWeight = Bold,
          color = colors.errorText,
        )
      }
    },
    content = { Text(Strings.settingsThemeInspectDeleteMessage) },
  )

@Preview
@Composable
private fun PreviewDuplicateThemeDialog(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    DuplicateThemeDialogContent(rememberTextFieldState("Midnight (copy)"), {}, {})
  }

@Preview
@Composable
private fun PreviewDeleteThemeDialog(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { DeleteThemeDialogContent("My theme", {}, {}) }
