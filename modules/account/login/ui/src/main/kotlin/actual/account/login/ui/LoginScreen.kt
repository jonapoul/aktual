package actual.account.login.ui

import actual.account.login.domain.LoginResult
import actual.account.login.nav.LoginNavRoute
import actual.account.login.res.LoginStrings
import actual.account.login.vm.LoginViewModel
import actual.account.model.LoginToken
import actual.account.model.Password
import actual.budget.list.nav.ListBudgetsNavRoute
import actual.core.colorscheme.ColorSchemeType
import actual.core.res.CoreStrings
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.UsingServerText
import actual.core.ui.VersionsText
import actual.core.ui.VerticalSpacer
import actual.core.ui.WavyBackground
import actual.core.ui.debugNavigate
import actual.core.ui.transparentTopAppBarColors
import actual.core.versions.ActualVersions
import actual.url.model.ServerUrl
import actual.url.nav.ServerUrlNavRoute
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun LoginScreen(
  navController: NavHostController,
  viewModel: LoginViewModel = hiltViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val enteredPassword by viewModel.enteredPassword.collectAsStateWithLifecycle()
  val url by viewModel.serverUrl.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val themeType by viewModel.themeType.collectAsStateWithLifecycle()
  val loginFailure by viewModel.loginFailure.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.token.collect { token ->
      navController.listBudgets(token)
    }
  }

  DisposableEffect(Unit) {
    onDispose { viewModel.clearState() }
  }

  LoginScreenImpl(
    versions = versions,
    enteredPassword = enteredPassword,
    url = url,
    isLoading = isLoading,
    themeType = themeType,
    loginFailure = loginFailure,
    onAction = { action ->
      when (action) {
        LoginAction.ChangeServer -> navController.openUrlScreen()
        LoginAction.NavBack -> navController.popBackStack()
        LoginAction.SignIn -> viewModel.onClickSignIn()
        is LoginAction.EnterPassword -> viewModel.onEnterPassword(action.password)
      }
    },
  )
}

private fun NavHostController.openUrlScreen() =
  debugNavigate(ServerUrlNavRoute) { popUpTo(LoginNavRoute) { inclusive = true } }

private fun NavHostController.listBudgets(token: LoginToken) =
  debugNavigate(ListBudgetsNavRoute(token)) { popUpTo(LoginNavRoute) { inclusive = true } }

@Composable
private fun LoginScreenImpl(
  versions: ActualVersions,
  enteredPassword: Password,
  url: ServerUrl,
  isLoading: Boolean,
  themeType: ColorSchemeType,
  loginFailure: LoginResult.Failure?,
  onAction: (LoginAction) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val theme = LocalTheme.current
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = {
          IconButton(onClick = { onAction(LoginAction.NavBack) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = CoreStrings.navBack,
            )
          }
        },
        title = { },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground(themeType)

      Content(
        modifier = Modifier.padding(innerPadding),
        versions = versions,
        enteredPassword = enteredPassword,
        url = url,
        isLoading = isLoading,
        loginFailure = loginFailure,
        onAction = onAction,
      )
    }
  }
}

@Stable
@Composable
private fun Content(
  versions: ActualVersions,
  enteredPassword: Password,
  url: ServerUrl,
  isLoading: Boolean,
  loginFailure: LoginResult.Failure?,
  onAction: (LoginAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val theme = LocalTheme.current
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
  ) {
    Column(
      modifier = Modifier
        .wrapContentWidth()
        .weight(1f),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        text = LoginStrings.title,
        style = MaterialTheme.typography.headlineLarge,
      )

      VerticalSpacer(15.dp)

      Text(
        text = LoginStrings.message,
        color = theme.tableRowHeaderText,
        style = MaterialTheme.typography.bodyLarge,
      )

      VerticalSpacer(20.dp)

      PasswordLogin(
        modifier = Modifier.fillMaxWidth(),
        isLoading = isLoading,
        enteredPassword = enteredPassword,
        onAction = onAction,
      )

      if (loginFailure != null) {
        VerticalSpacer(20.dp)

        LoginFailureText(
          modifier = Modifier.fillMaxWidth(),
          result = loginFailure,
        )
      }
    }

    VerticalSpacer(20.dp)

    UsingServerText(
      modifier = Modifier.fillMaxWidth(),
      url = url,
      onClickChange = { onAction(LoginAction.ChangeServer) },
    )

    VerticalSpacer()

    VersionsText(
      modifier = Modifier.align(Alignment.End),
      versions = versions,
    )
  }
}

@ScreenPreview
@Composable
private fun Regular() = PreviewScreen { type ->
  LoginScreenImpl(
    versions = ActualVersions.Dummy,
    enteredPassword = Password.Empty,
    url = ServerUrl.Demo,
    isLoading = false,
    loginFailure = null,
    themeType = type,
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun WithErrorMessage() = PreviewScreen { type ->
  LoginScreenImpl(
    versions = ActualVersions.Dummy,
    enteredPassword = Password.Dummy,
    url = ServerUrl("https://this.is.a.long.url.discombobulated.com/actual/budget/whatever.json"),
    isLoading = true,
    loginFailure = LoginResult.InvalidPassword,
    themeType = type,
    onAction = {},
  )
}
