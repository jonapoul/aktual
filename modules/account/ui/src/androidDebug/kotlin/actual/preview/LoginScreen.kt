package actual.preview

import actual.account.domain.LoginResult
import actual.account.ui.login.LoginScaffold
import actual.core.model.ActualVersions
import actual.core.model.Password
import actual.core.model.ServerUrl
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Regular() = PreviewThemedScreen {
  LoginScaffold(
    versions = ActualVersions.Dummy,
    enteredPassword = Password.Empty,
    url = ServerUrl.Demo,
    isLoading = false,
    loginFailure = null,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun WithErrorMessage() = PreviewThemedScreen {
  LoginScaffold(
    versions = ActualVersions.Dummy,
    enteredPassword = Password.Dummy,
    url = ServerUrl("https://this.is.a.long.url.discombobulated.com/actual/budget/whatever.json"),
    isLoading = true,
    loginFailure = LoginResult.InvalidPassword,
    onAction = {},
  )
}
