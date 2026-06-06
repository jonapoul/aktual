package aktual.account.ui.password

import aktual.account.vm.ChangePasswordState
import aktual.account.vm.ChangePasswordViewModel
import aktual.core.l10n.Strings
import aktual.core.model.AktualVersions
import aktual.core.model.Password
import aktual.core.nav.BackNavigator
import aktual.core.nav.ListBudgetsNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.LandscapePreview
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.VersionsText
import aktual.core.ui.WavyBackground
import aktual.core.ui.transparentTopAppBarColors
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun ChangePasswordScreen(
  navBack: BackNavigator,
  toListBudgets: ListBudgetsNavigator,
  viewModel: ChangePasswordViewModel = metroViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val inputPassword1 by viewModel.inputPassword1.collectAsStateWithLifecycle()
  val inputPassword2 by viewModel.inputPassword2.collectAsStateWithLifecycle()
  val showPasswords by viewModel.showPasswords.collectAsStateWithLifecycle()
  val passwordsMatch by viewModel.passwordsMatch.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val newToken by viewModel.token.collectAsStateWithLifecycle(initialValue = null)

  val token = newToken
  if (token != null) {
    LaunchedEffect(Unit) { toListBudgets() }
  }

  ChangePasswordScaffold(
    inputPassword1 = inputPassword1,
    inputPassword2 = inputPassword2,
    showPasswords = showPasswords,
    passwordsMatch = passwordsMatch,
    state = state,
    versions = versions,
    onAction = { action ->
      when (action) {
        NavBack -> navBack()
        Submit -> viewModel.submit()
        is SetPassword1 -> viewModel.setPassword1(action.value)
        is SetPassword2 -> viewModel.setPassword2(action.value)
        is SetPasswordsVisible -> viewModel.setPasswordsVisible(action.visible)
      }
    },
  )
}

@Composable
internal fun ChangePasswordScaffold(
  inputPassword1: Password,
  inputPassword2: Password,
  showPasswords: Boolean,
  passwordsMatch: Boolean,
  state: ChangePasswordState?,
  versions: AktualVersions,
  onAction: PasswordActionHandler,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { /* empty */ },
      )
    }
  ) { innerPadding ->
    Box {
      WavyBackground()

      ChangePasswordContent(
        modifier = Modifier.padding(innerPadding),
        inputPassword1 = inputPassword1,
        inputPassword2 = inputPassword2,
        showPasswords = showPasswords,
        passwordsMatch = passwordsMatch,
        state = state,
        versions = versions,
        onAction = onAction,
      )
    }
  }
}

@Stable
@Composable
private fun ChangePasswordContent(
  inputPassword1: Password,
  inputPassword2: Password,
  showPasswords: Boolean,
  passwordsMatch: Boolean,
  state: ChangePasswordState?,
  versions: AktualVersions,
  onAction: PasswordActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
    Column(
      modifier = Modifier.wrapContentWidth().weight(1f),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start,
    ) {
      Text(text = Strings.passwordTitle, style = typography.headlineLarge)

      VerticalSpacer(20.dp)

      Text(
        text = Strings.passwordMessage,
        color = colors.tableRowHeaderText,
        style = typography.bodyLarge,
      )

      VerticalSpacer(30.dp)

      ConfirmPasswordForm(
        modifier = Modifier.fillMaxWidth(),
        inputPassword1 = inputPassword1,
        inputPassword2 = inputPassword2,
        showPasswords = showPasswords,
        state = state,
        passwordsMatch = passwordsMatch,
        onAction = onAction,
      )

      VerticalSpacer(20.dp)

      when (state) {
        ChangePasswordState.Loading,
        null -> Unit
        is ChangePasswordState.Failure ->
          Text(text = state.errorMessage(), color = colors.errorText)
        ChangePasswordState.Success ->
          Text(text = Strings.passwordSuccess, color = colors.noticeText)
      }
    }

    VerticalSpacer(20.dp)

    VersionsText(modifier = Modifier.align(Alignment.End), versions = versions)

    BottomSpacing()
  }
}

@Composable
private fun ChangePasswordState.Failure.errorMessage(): String =
  when (this) {
    ChangePasswordState.InvalidPassword -> Strings.passwordFailureEmpty
    ChangePasswordState.NetworkFailure -> Strings.passwordFailureNetwork
    ChangePasswordState.OtherFailure -> Strings.passwordFailureOther
    ChangePasswordState.PasswordsDontMatch -> Strings.passwordFailureMatch
  }

@PortraitPreview
@LandscapePreview
@Composable
private fun PreviewChangePassword(
  @PreviewParameter(ChangePasswordProvider::class) params: ColoredParams<ChangePasswordParams>
) =
  PreviewWithColors(params.colors) {
    ChangePasswordScaffold(
      inputPassword1 = params.data.password1,
      inputPassword2 = params.data.password2,
      showPasswords = params.data.showPasswords,
      passwordsMatch = params.data.passwordsMatch,
      state = params.data.state,
      versions = AktualVersions.Dummy,
      onAction = {},
    )
  }

private data class ChangePasswordParams(
  val password1: Password = Password.Dummy,
  val password2: Password = Password.Dummy,
  val showPasswords: Boolean = false,
  val passwordsMatch: Boolean = false,
  val state: ChangePasswordState? = null,
)

private class ChangePasswordProvider :
  ColoredParameterProvider<ChangePasswordParams>(
    ChangePasswordParams(password1 = Empty, password2 = Empty),
    ChangePasswordParams(showPasswords = true, passwordsMatch = true),
    ChangePasswordParams(state = ChangePasswordState.Loading, passwordsMatch = true),
    ChangePasswordParams(state = ChangePasswordState.Success, passwordsMatch = true),
    ChangePasswordParams(state = ChangePasswordState.NetworkFailure, passwordsMatch = true),
  )
