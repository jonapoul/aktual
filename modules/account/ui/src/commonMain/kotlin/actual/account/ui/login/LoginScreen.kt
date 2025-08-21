package actual.account.ui.login

import actual.account.domain.LoginResult
import actual.account.vm.LoginViewModel
import actual.core.model.ActualVersions
import actual.core.model.Password
import actual.core.model.ServerUrl
import actual.core.ui.ActualTypography
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.core.ui.UsingServerText
import actual.core.ui.VersionsText
import actual.core.ui.WavyBackground
import actual.core.ui.metroViewModel
import actual.core.ui.transparentTopAppBarColors
import actual.l10n.Strings
import alakazam.kotlin.compose.VerticalSpacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun LoginScreen(
  nav: LoginNavigator,
  viewModel: LoginViewModel = metroViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val enteredPassword by viewModel.enteredPassword.collectAsStateWithLifecycle()
  val url by viewModel.serverUrl.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val loginFailure by viewModel.loginFailure.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.token.collect { token ->
      nav.toListBudgets(token)
    }
  }

  DisposableEffect(Unit) {
    onDispose { viewModel.clearState() }
  }

  LoginScaffold(
    versions = versions,
    enteredPassword = enteredPassword,
    url = url,
    isLoading = isLoading,
    loginFailure = loginFailure,
    onAction = { action ->
      when (action) {
        LoginAction.ChangeServer -> nav.toUrl()
        LoginAction.NavBack -> nav.back()
        LoginAction.SignIn -> viewModel.onClickSignIn()
        is LoginAction.EnterPassword -> viewModel.onEnterPassword(action.password)
      }
    },
  )
}

@Composable
internal fun LoginScaffold(
  versions: ActualVersions,
  enteredPassword: Password,
  url: ServerUrl?,
  isLoading: Boolean,
  loginFailure: LoginResult.Failure?,
  onAction: (LoginAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = {
          IconButton(onClick = { onAction(LoginAction.NavBack) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = Strings.navBack,
            )
          }
        },
        title = { },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    Box {
      val hazeState = remember { HazeState() }

      WavyBackground(
        modifier = Modifier.hazeSource(hazeState),
      )

      Content(
        modifier = Modifier.padding(innerPadding),
        versions = versions,
        enteredPassword = enteredPassword,
        url = url,
        isLoading = isLoading,
        loginFailure = loginFailure,
        hazeState = hazeState,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Stable
@Composable
private fun Content(
  versions: ActualVersions,
  enteredPassword: Password,
  url: ServerUrl?,
  isLoading: Boolean,
  loginFailure: LoginResult.Failure?,
  hazeState: HazeState,
  onAction: (LoginAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Column(
      modifier = Modifier
        .wrapContentWidth()
        .weight(1f),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        text = Strings.loginTitle,
        style = ActualTypography.headlineLarge,
      )

      VerticalSpacer(15.dp)

      Text(
        text = Strings.loginMessage,
        color = theme.tableRowHeaderText,
        style = ActualTypography.bodyLarge,
      )

      VerticalSpacer(20.dp)

      PasswordLogin(
        modifier = Modifier.fillMaxWidth(),
        isLoading = isLoading,
        enteredPassword = enteredPassword,
        theme = theme,
        onAction = onAction,
      )

      if (loginFailure != null) {
        VerticalSpacer(20.dp)

        LoginFailureText(
          modifier = Modifier.fillMaxWidth(),
          result = loginFailure,
          theme = theme,
        )
      }
    }

    VerticalSpacer(20.dp)

    UsingServerText(
      url = url,
      hazeState = hazeState,
      theme = theme,
      onClickChange = { onAction(LoginAction.ChangeServer) },
    )

    VerticalSpacer(4.dp)

    VersionsText(
      modifier = Modifier.align(Alignment.End),
      versions = versions,
    )
  }
}
