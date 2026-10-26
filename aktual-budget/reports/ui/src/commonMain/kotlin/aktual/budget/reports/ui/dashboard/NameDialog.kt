package aktual.budget.reports.ui.dashboard

import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ColorParams
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.disabled
import aktual.core.ui.keyboardFocusRequester
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun NameDialog(
  title: String,
  placeholder: String,
  confirmText: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
  initialName: String = "",
  capitalization: KeyboardCapitalization = Sentences,
  isValid: NameValidator = AlwaysValid,
  error: @Composable (String) -> String? = { null },
) =
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    NameDialogContent(
      title = title,
      placeholder = placeholder,
      confirmText = confirmText,
      state = rememberTextFieldState(initialName),
      onConfirm = onConfirm,
      onDismiss = onDismiss,
      capitalization = capitalization,
      isValid = isValid,
      error = error,
    )
  }

@Immutable
internal fun interface NameValidator {
  operator fun invoke(value: String): Boolean

  companion object {
    val AlwaysValid = NameValidator { true }
  }
}

@Composable
private fun NameDialogContent(
  title: String,
  placeholder: String,
  confirmText: String,
  state: TextFieldState,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
  capitalization: KeyboardCapitalization = Sentences,
  isValid: NameValidator = AlwaysValid,
  error: @Composable (String) -> String? = { null },
) {
  val name = state.text.trim().toString()
  val errorText = error(name)
  val enabled = name.isNotEmpty() && isValid(name) && errorText == null
  AktualAlertDialogContent(
    title = title,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.reportsDashboardNameCancel) }
      TextButton(enabled = enabled, onClick = { onConfirm(name) }) {
        Text(
          text = confirmText,
          fontWeight = if (enabled) Bold else null,
          color = if (enabled) colors.numberPositive else colors.buttonNormalDisabledText.disabled,
        )
      }
    },
    content = {
      Column {
        AktualTextField(
          modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
          state = state,
          placeholderText = placeholder,
          singleLine = true,
          keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = Done),
          onKeyboardAction = { if (enabled) onConfirm(name) },
        )
        if (errorText != null) {
          Text(
            modifier = Modifier.padding(top = 8.dp),
            text = errorText,
            style = typography.bodySmall,
            color = colors.errorText,
          )
        }
      }
    },
  )
}

@Preview
@Composable
private fun PreviewNameDialog(
    @PreviewParameter(NameDialogProvider::class) params: ColorParams<String>,
) =
  PreviewWithColorParams(params) {
    NameDialogContent(
      title = Strings.reportsDashboardNewPage,
      placeholder = Strings.reportsDashboardPageName,
      confirmText = Strings.reportsDashboardNameCreate,
      state = rememberTextFieldState(params.data),
      onConfirm = {},
      onDismiss = {},
    )
  }

private class NameDialogProvider : ColorParamsProvider<String>("", "Holidays")
