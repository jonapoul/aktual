package actual.preview

import actual.account.model.Password
import actual.account.ui.password.ChangePasswordScaffold
import actual.account.vm.ChangePasswordState
import actual.core.model.ActualVersions
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Regular() = PreviewThemedScreen {
  ChangePasswordScaffold(
    inputPassword1 = Password.Empty,
    inputPassword2 = Password.Empty,
    showPasswords = false,
    passwordsMatch = false,
    state = null,
    versions = ActualVersions.Dummy,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun Loading() = PreviewThemedScreen {
  ChangePasswordScaffold(
    inputPassword1 = Password.Empty,
    inputPassword2 = Password.Empty,
    showPasswords = false,
    passwordsMatch = false,
    state = ChangePasswordState.Loading,
    versions = ActualVersions.Dummy,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun Success() = PreviewThemedScreen {
  ChangePasswordScaffold(
    inputPassword1 = Password.Empty,
    inputPassword2 = Password.Empty,
    showPasswords = false,
    passwordsMatch = false,
    state = ChangePasswordState.Success,
    versions = ActualVersions.Dummy,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun Failure() = PreviewThemedScreen {
  ChangePasswordScaffold(
    inputPassword1 = Password.Dummy,
    inputPassword2 = Password.Dummy,
    showPasswords = false,
    passwordsMatch = true,
    state = ChangePasswordState.NetworkFailure,
    versions = ActualVersions.Dummy,
    onAction = {},
  )
}
