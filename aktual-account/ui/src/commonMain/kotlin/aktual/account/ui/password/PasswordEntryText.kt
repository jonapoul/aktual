package aktual.account.ui.password

import aktual.core.model.Password
import aktual.core.ui.AktualTextField
import aktual.core.ui.PasswordTransformation
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun PasswordEntryText(
  password: Password,
  placeholderText: String,
  showPassword: Boolean,
  onValueChange: (Password) -> Unit,
  modifier: Modifier = Modifier,
  imeAction: ImeAction = ImeAction.Go,
  onGo: (() -> Unit)? = null,
) {
  val textState = rememberTextFieldState(initialText = password.value)
  val rememberedOnValueChange by rememberUpdatedState(onValueChange)

  LaunchedEffect(textState) {
    snapshotFlow { textState.text.toString() }
      .collect { value -> rememberedOnValueChange(Password(value)) }
  }

  AktualTextField(
    modifier = modifier,
    state = textState,
    placeholderText = placeholderText,
    outputTransformation = if (showPassword) null else PasswordTransformation,
    keyboardOptions =
      KeyboardOptions(
        autoCorrectEnabled = false,
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
      ),
    onKeyboardAction = onGo?.let { go -> { _ -> go() } },
  )
}

@Preview
@Composable
private fun PreviewPasswordEntryText(
  @PreviewParameter(PasswordEntryProvider::class) params: ThemedParams<PasswordEntryParams>
) =
  PreviewWithTheme(params.theme) {
    PasswordEntryText(
      password = params.data.password,
      placeholderText = "Password",
      showPassword = params.data.showPassword,
      onValueChange = {},
      onGo = {},
    )
  }

private data class PasswordEntryParams(val password: Password, val showPassword: Boolean)

private class PasswordEntryProvider :
  ThemedParameterProvider<PasswordEntryParams>(
    PasswordEntryParams(password = Password.Empty, showPassword = false),
    PasswordEntryParams(password = Password.Dummy, showPassword = false),
    PasswordEntryParams(password = Password.Dummy, showPassword = true),
  )
