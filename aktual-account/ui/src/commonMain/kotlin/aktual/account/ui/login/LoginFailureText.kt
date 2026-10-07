package aktual.account.ui.login

import aktual.account.domain.LoginResult
import aktual.core.l10n.Strings
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.ErrorBanner
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
      is InvalidPassword -> Strings.loginFailurePassword
      is TokenExpired -> Strings.loginFailureTokenExpired
      is HttpFailure -> Strings.loginFailureHttp(result.code, result.message)
      is NetworkFailure -> Strings.loginFailureNetwork(result.reason)
      is OtherFailure -> Strings.loginFailureOther(result.reason)
    }

  ErrorBanner(modifier = modifier.testTag(Tags.LoginFailureText), text = errorMessage)
}

@Preview
@Composable
private fun PreviewLoginFailureText(
  @PreviewParameter(LoginFailureProvider::class) params: ColoredParams<LoginResult.Failure>,
) = PreviewWithColoredParams(params) { LoginFailureText(result = this) }

private class LoginFailureProvider :
  ColoredParameterProvider<LoginResult.Failure>(
    LoginResult.InvalidPassword,
    LoginResult.TokenExpired,
    LoginResult.HttpFailure(code = 404, message = "Resource not found"),
    LoginResult.NetworkFailure(reason = "Network problem"),
    LoginResult.OtherFailure("Something broke"),
  )
