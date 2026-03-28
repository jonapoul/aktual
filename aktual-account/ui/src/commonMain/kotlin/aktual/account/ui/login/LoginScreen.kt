package aktual.account.ui.login

import aktual.account.domain.LoginResult
import aktual.account.vm.LoginViewModel
import aktual.app.nav.BackNavigator
import aktual.app.nav.ListBudgetsNavigator
import aktual.app.nav.ServerUrlNavigator
import aktual.core.l10n.Strings
import aktual.core.model.AktualVersions
import aktual.core.model.LoginMethod
import aktual.core.model.Password
import aktual.core.model.Password.Companion.Dummy
import aktual.core.model.Password.Companion.Empty
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.LandscapePreview
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun LoginScreen(
  back: BackNavigator,
  toServerUrl: ServerUrlNavigator,
  toListBudgets: ListBudgetsNavigator,
  viewModel: LoginViewModel = metroViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val enteredPassword by viewModel.enteredPassword.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val loginFailure by viewModel.loginFailure.collectAsStateWithLifecycle()
  val redirectUrl by viewModel.redirectUrl.collectAsStateWithLifecycle()
  val loginMethods by viewModel.loginMethods.collectAsStateWithLifecycle()
  val selectedLoginMethod by viewModel.selectedLoginMethod.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) { viewModel.token.collect { token -> toListBudgets(token) } }

  val uriHandler = LocalUriHandler.current
  LaunchedEffect(redirectUrl) {
    val url = redirectUrl ?: return@LaunchedEffect
    uriHandler.openUri(url)
  }

  DisposableEffect(Unit) { onDispose { viewModel.clearState() } }

  LoginScaffold(
    versions = versions,
    enteredPassword = enteredPassword,
    isLoading = isLoading,
    loginFailure = loginFailure,
    loginMethods = loginMethods,
    selectedLoginMethod = selectedLoginMethod,
    onAction = { action ->
      when (action) {
        LoginAction.ChangeServer -> toServerUrl()
        LoginAction.NavBack -> back()
        LoginAction.SignIn -> viewModel.onClickSignIn()
        is LoginAction.EnterPassword -> viewModel.onEnterPassword(action.password)
        is LoginAction.SelectLoginMethod -> viewModel.onSelectLoginMethod(action.method)
      }
    },
  )
}

@Composable
internal fun LoginScaffold(
  versions: AktualVersions,
  enteredPassword: Password,
  isLoading: Boolean,
  loginFailure: LoginResult.Failure?,
  loginMethods: ImmutableList<LoginMethod>,
  selectedLoginMethod: LoginMethod,
  onAction: (LoginAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(LoginAction.NavBack) } },
        title = {},
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground()

      Content(
        modifier = Modifier.padding(innerPadding),
        versions = versions,
        enteredPassword = enteredPassword,
        isLoading = isLoading,
        loginFailure = loginFailure,
        loginMethods = loginMethods,
        selectedLoginMethod = selectedLoginMethod,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Composable
private fun Content(
  versions: AktualVersions,
  enteredPassword: Password,
  isLoading: Boolean,
  loginFailure: LoginResult.Failure?,
  loginMethods: ImmutableList<LoginMethod>,
  selectedLoginMethod: LoginMethod,
  onAction: (LoginAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxSize().padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Column(
      modifier = Modifier.wrapContentWidth().weight(1f),
      verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Top),
      horizontalAlignment = Alignment.Start,
    ) {
      Text(text = Strings.loginTitle, style = AktualTypography.headlineLarge)

      Text(
        text = Strings.loginMessage,
        color = theme.tableRowHeaderText,
        style = AktualTypography.bodyLarge,
      )

      if (loginMethods.size > 1) {
        LoginMethodPicker(
          modifier = Modifier.fillMaxWidth(),
          methods = loginMethods,
          selectedMethod = selectedLoginMethod,
          onAction = onAction,
        )
      }

      when (selectedLoginMethod) {
        LoginMethod.Password -> {
          PasswordLogin(
            modifier = Modifier.fillMaxWidth(),
            isLoading = isLoading,
            enteredPassword = enteredPassword,
            onAction = onAction,
          )
        }

        LoginMethod.Header -> {
          HeaderLogin(
            modifier = Modifier.fillMaxWidth(),
            isLoading = isLoading,
            hasFailure = loginFailure != null,
            onAction = onAction,
          )
        }

        LoginMethod.OpenId -> {
          OpenIdLogin(modifier = Modifier.fillMaxWidth())
        }
      }

      if (loginFailure != null) {
        LoginFailureText(modifier = Modifier.fillMaxWidth(), result = loginFailure)
      }
    }

    VerticalSpacer(4.dp)

    VersionsText(modifier = Modifier.align(Alignment.End), versions = versions)

    BottomStatusBarSpacing()
    BottomNavBarSpacing()
  }
}

@Composable
@PortraitPreview
@LandscapePreview
private fun PreviewLoginScaffold(
  @PreviewParameter(LoginScaffoldProvider::class) params: ThemedParams<LoginScaffoldParams>
) =
  PreviewWithTheme(params.theme) {
    val data = params.data
    LoginScaffold(
      versions = data.versions,
      enteredPassword = data.password,
      isLoading = data.isLoading,
      loginFailure = data.loginFailure,
      loginMethods = data.loginMethods,
      selectedLoginMethod = data.selectedLoginMethod,
      onAction = {},
    )
  }

private data class LoginScaffoldParams(
  val versions: AktualVersions = AktualVersions.Dummy,
  val password: Password = Dummy,
  val isLoading: Boolean = false,
  val loginFailure: LoginResult.Failure? = null,
  val loginMethods: ImmutableList<LoginMethod> = persistentListOf(),
  val selectedLoginMethod: LoginMethod = LoginMethod.Password,
)

private val ALL_METHODS = LoginMethod.entries.toImmutableList()

private class LoginScaffoldProvider :
  ThemedParameterProvider<LoginScaffoldParams>(
    LoginScaffoldParams(password = Empty, isLoading = false),
    LoginScaffoldParams(
      password = Dummy,
      isLoading = true,
      loginFailure = LoginResult.InvalidPassword,
    ),
    LoginScaffoldParams(loginMethods = ALL_METHODS, selectedLoginMethod = LoginMethod.Password),
    LoginScaffoldParams(
      loginMethods = ALL_METHODS,
      selectedLoginMethod = LoginMethod.Header,
      isLoading = true,
    ),
    LoginScaffoldParams(loginMethods = ALL_METHODS, selectedLoginMethod = LoginMethod.OpenId),
  )
