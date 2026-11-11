package aktual.account.ui.login

import aktual.account.vm.OpenIdFirstLogin
import aktual.core.l10n.Strings
import aktual.core.model.Password
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButtonWithLoading
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun OpenIdLogin(
  isLoading: Boolean,
  firstLogin: OpenIdFirstLogin,
  enteredPassword: Password,
  onAction: OnAction<LoginAction>,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Text(
      text = Strings.loginOpenidMessage,
      color = colors.tableRowHeaderText,
      style = typography.bodyMedium,
    )

    if (firstLogin != NotFirst) {
      VerticalSpacer(12.dp)

      Text(
        text = Strings.loginOpenidFirstOwner,
        color = colors.warningText,
        style = typography.bodyMedium,
      )
    }

    if (firstLogin == FirstWithPassword) {
      VerticalSpacer(12.dp)

      PasswordField(
        modifier = Modifier.testTag(Tags.OpenIdLoginPasswordField).fillMaxWidth(1f),
        isLoading = isLoading,
        enteredPassword = enteredPassword,
        onAction = onAction,
      )
    }

    VerticalSpacer(20.dp)

    PrimaryTextButtonWithLoading(
      modifier = Modifier.testTag(Tags.OpenIdLoginButton).padding(5.dp).fillMaxWidth(),
      text = Strings.loginSignIn,
      isLoading = isLoading,
      isEnabled = firstLogin != FirstWithPassword || enteredPassword != Password.Empty,
      onClick = { onAction(SignIn) },
    )
  }
}

@Preview
@Composable
private fun PreviewOpenIdLogin(
  @PreviewParameter(OpenIdLoginProvider::class) params: ColorParams<OpenIdLoginParams>,
) =
  PreviewWithColorParams(params) {
    OpenIdLogin(
      isLoading = isLoading,
      firstLogin = firstLogin,
      enteredPassword = Password.Empty,
      onAction = {},
    )
  }

private data class OpenIdLoginParams(val isLoading: Boolean, val firstLogin: OpenIdFirstLogin)

private class OpenIdLoginProvider :
  ColorParamsProvider<OpenIdLoginParams>(
    OpenIdLoginParams(isLoading = false, firstLogin = NotFirst),
    OpenIdLoginParams(isLoading = true, firstLogin = NotFirst),
    OpenIdLoginParams(isLoading = false, firstLogin = First),
    OpenIdLoginParams(isLoading = false, firstLogin = FirstWithPassword),
  )
