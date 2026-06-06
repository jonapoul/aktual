package aktual.account.ui.login

import aktual.account.domain.LoginResult
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColors
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
) {
  val errorMessage =
    when (result) {
      is LoginResult.InvalidPassword -> Strings.loginFailurePassword
      is LoginResult.TokenExpired -> Strings.loginFailureTokenExpired
      is LoginResult.HttpFailure -> Strings.loginFailureHttp(result.code, result.message)
      is LoginResult.NetworkFailure -> Strings.loginFailureNetwork(result.reason)
      is LoginResult.OtherFailure -> Strings.loginFailureOther(result.reason)
    }

  Text(
    modifier = modifier.testTag(Tags.LoginFailureText),
    text = errorMessage,
    color = colors.errorText,
    textAlign = TextAlign.Center,
  )
}

@Preview
@Composable
private fun PreviewLoginFailureText(
  @PreviewParameter(LoginFailureProvider::class) params: ColoredParams<LoginResult.Failure>
) = PreviewWithColors(params.colors) { LoginFailureText(params.data) }

private class LoginFailureProvider :
  ColoredParameterProvider<LoginResult.Failure>(
    LoginResult.InvalidPassword,
    LoginResult.TokenExpired,
    LoginResult.HttpFailure(code = 404, message = "Resource not found"),
    LoginResult.NetworkFailure(reason = "Network problem"),
    LoginResult.OtherFailure("Something broke"),
  )
