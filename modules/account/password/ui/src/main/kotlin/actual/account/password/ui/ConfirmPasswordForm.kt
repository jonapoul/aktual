package actual.account.password.ui

import actual.account.model.Password
import actual.account.password.res.PasswordStrings
import actual.account.password.vm.ChangePasswordState
import actual.core.ui.PreviewColumn
import actual.core.ui.PrimaryTextButtonWithLoading
import actual.core.ui.VerticalSpacer
import actual.core.ui.keyboardFocusRequester
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ConfirmPasswordForm(
  inputPassword1: Password,
  inputPassword2: Password,
  showPasswords: Boolean,
  state: ChangePasswordState?,
  passwordsMatch: Boolean,
  onAction: (PasswordAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val keyboard = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current
  val isLoading = state is ChangePasswordState.Loading

  if (state is ChangePasswordState.Success) {
    keyboard?.hide()
  }

  Column(
    modifier = modifier,
  ) {
    PasswordEntryText(
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(keyboardFocusRequester(keyboard)),
      password = inputPassword1,
      placeholderText = PasswordStrings.input,
      showPassword = showPasswords,
      imeAction = ImeAction.Next,
      onValueChange = { pw -> onAction(PasswordAction.SetPassword1(pw)) },
      onGo = { focusManager.moveFocus(FocusDirection.Next) },
    )

    VerticalSpacer(10.dp)

    PasswordEntryText(
      modifier = Modifier.fillMaxWidth(),
      password = inputPassword2,
      placeholderText = PasswordStrings.inputConfirm,
      showPassword = showPasswords,
      imeAction = ImeAction.Go,
      onValueChange = { pw -> onAction(PasswordAction.SetPassword2(pw)) },
      onGo = {
        if (passwordsMatch) onAction(PasswordAction.Submit)
        keyboard?.hide()
      },
    )

    VerticalSpacer(10.dp)

    val interactionSource = remember { MutableInteractionSource() }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(
          interactionSource = interactionSource,
          indication = ripple(),
          enabled = true,
          onClick = { onAction(PasswordAction.SetPasswordsVisible(!showPasswords)) },
        ),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        checked = showPasswords,
        interactionSource = interactionSource,
        onCheckedChange = { onAction(PasswordAction.SetPasswordsVisible(!showPasswords)) },
      )

      Text(
        text = PasswordStrings.show,
        fontSize = 15.sp,
      )
    }

    VerticalSpacer(10.dp)

    PrimaryTextButtonWithLoading(
      modifier = Modifier.fillMaxWidth(),
      text = PasswordStrings.confirm,
      isLoading = isLoading,
      isEnabled = !isLoading && passwordsMatch,
      onClick = { onAction(PasswordAction.Submit) },
    )
  }
}

@Preview
@Composable
private fun EmptyForm() = PreviewColumn {
  ConfirmPasswordForm(
    inputPassword1 = Password.Empty,
    inputPassword2 = Password.Empty,
    showPasswords = false,
    state = null,
    passwordsMatch = false,
    onAction = {},
  )
}

@Preview
@Composable
private fun ShowPasswords() = PreviewColumn {
  ConfirmPasswordForm(
    inputPassword1 = Password.Dummy,
    inputPassword2 = Password.Dummy,
    showPasswords = true,
    state = null,
    passwordsMatch = true,
    onAction = {},
  )
}

@Preview
@Composable
private fun Loading() = PreviewColumn {
  ConfirmPasswordForm(
    inputPassword1 = Password.Dummy,
    inputPassword2 = Password.Dummy,
    showPasswords = false,
    state = ChangePasswordState.Loading,
    passwordsMatch = true,
    onAction = {},
  )
}
