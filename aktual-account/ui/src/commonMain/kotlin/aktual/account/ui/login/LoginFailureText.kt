package aktual.account.ui.login

import aktual.account.domain.LoginResult
import aktual.core.l10n.Strings
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Stable
@Composable
internal fun LoginFailureText(
  result: LoginResult.Failure,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val errorMessage =
    when (result) {
      is LoginResult.InvalidPassword -> Strings.loginFailurePassword
      is LoginResult.HttpFailure -> Strings.loginFailureHttp(result.code, result.message)
      is LoginResult.NetworkFailure -> Strings.loginFailureNetwork(result.reason)
      is LoginResult.OtherFailure -> Strings.loginFailureOther(result.reason)
    }

  Text(
    modifier = modifier.testTag(Tags.LoginFailureText),
    text = errorMessage,
    color = theme.errorText,
    textAlign = TextAlign.Center,
  )
}

@Preview
@Composable
private fun PreviewLoginFailureText(
  @PreviewParameter(LoginFailureProvider::class) params: ThemedParams<LoginResult.Failure>
) = PreviewWithColorScheme(params.type) { LoginFailureText(params.data) }

private class LoginFailureProvider :
  ThemedParameterProvider<LoginResult.Failure>(
    LoginResult.InvalidPassword,
    LoginResult.HttpFailure(code = 404, message = "Resource not found"),
    LoginResult.NetworkFailure(reason = "Network problem"),
    LoginResult.OtherFailure("Something broke"),
  )
