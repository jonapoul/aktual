package actual.login.ui

import actual.core.model.ActualVersions
import actual.core.model.Password
import actual.core.model.ServerUrl
import actual.core.ui.ActualScreenPreview
import actual.core.ui.ActualTextField
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewActualScreen
import actual.core.ui.PrimaryActualTextButtonWithLoading
import actual.core.ui.UsingServerText
import actual.core.ui.VersionsText
import actual.core.ui.VerticalSpacer
import actual.login.vm.LoginResult
import actual.login.vm.LoginViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import actual.core.res.R as CoreR

@Composable
fun LoginScreen(
  navigator: LoginNavigator,
  viewModel: LoginViewModel = hiltViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val enteredPassword by viewModel.enteredPassword.collectAsStateWithLifecycle()
  val url by viewModel.serverUrl.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val loginFailure by viewModel.loginFailure.collectAsStateWithLifecycle()

  val navToBudgetList by viewModel.navToBudgetList.collectAsStateWithLifecycle()
  if (navToBudgetList) {
    SideEffect { navigator.listBudgets() }
  }

  var navToServerUrl by remember { mutableStateOf(false) }
  if (navToServerUrl) {
    SideEffect { navigator.changeServer() }
  }

  var clickedBack by remember { mutableStateOf(false) }
  if (clickedBack) {
    SideEffect { navigator.navBack() }
  }

  DisposableEffect(Unit) {
    onDispose {
      navToServerUrl = false
      viewModel.clearState()
    }
  }

  LoginScreenImpl(
    versions = versions,
    enteredPassword = enteredPassword,
    url = url,
    isLoading = isLoading,
    loginFailure = loginFailure,
    onClickBack = { clickedBack = true },
    onEnterPassword = viewModel::onEnterPassword,
    onClickSignIn = viewModel::onClickSignIn,
    onClickChangeServer = { navToServerUrl = true },
  )
}

@Composable
private fun LoginScreenImpl(
  versions: ActualVersions,
  enteredPassword: Password,
  url: ServerUrl,
  isLoading: Boolean,
  loginFailure: LoginResult.Failure?,
  onClickBack: () -> Unit,
  onEnterPassword: (String) -> Unit,
  onClickSignIn: () -> Unit,
  onClickChangeServer: () -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val theme = LocalTheme.current
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = theme.mobileHeaderBackground,
          titleContentColor = theme.mobileHeaderText,
        ),
        navigationIcon = {
          IconButton(onClick = onClickBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = CoreR.string.nav_back),
            )
          }
        },
        title = {
          Text(
            text = stringResource(id = CoreR.string.login_toolbar),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    Content(
      modifier = Modifier.padding(innerPadding),
      versions = versions,
      enteredPassword = enteredPassword,
      url = url,
      isLoading = isLoading,
      loginFailure = loginFailure,
      onEnterPassword = onEnterPassword,
      onClickSignIn = onClickSignIn,
      onClickChangeServer = onClickChangeServer,
    )
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
  onEnterPassword: (String) -> Unit,
  onClickSignIn: () -> Unit,
  onClickChangeServer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val theme = LocalTheme.current
  Column(
    modifier = modifier
      .background(theme.pageBackground)
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
        text = stringResource(id = CoreR.string.login_title),
        style = MaterialTheme.typography.headlineLarge,
      )

      VerticalSpacer(15.dp)

      Text(
        text = stringResource(id = CoreR.string.login_message),
        color = theme.tableRowHeaderText,
        style = MaterialTheme.typography.bodyLarge,
      )

      VerticalSpacer(20.dp)

      ActualTextField(
        modifier = Modifier.fillMaxWidth(1f),
        value = enteredPassword.toString(),
        onValueChange = onEnterPassword,
        placeholderText = stringResource(id = CoreR.string.login_password_hint),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
          autoCorrectEnabled = false,
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Go,
        ),
        keyboardActions = KeyboardActions(
          onGo = { },
        ),
      )

      VerticalSpacer(20.dp)

      PrimaryActualTextButtonWithLoading(
        modifier = Modifier.align(Alignment.End),
        text = stringResource(id = CoreR.string.login_sign_in),
        isLoading = isLoading,
        onClick = onClickSignIn,
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
      onClickChange = onClickChangeServer,
    )

    VerticalSpacer()

    VersionsText(
      modifier = Modifier.align(Alignment.End),
      versions = versions,
    )
  }
}

@ActualScreenPreview
@Composable
private fun Regular() = PreviewActualScreen {
  LoginScreenImpl(
    versions = ActualVersions(app = "1.2.3", server = "24.3.0"),
    enteredPassword = Password.Empty,
    url = ServerUrl.Demo,
    isLoading = false,
    loginFailure = null,
    onClickBack = {},
    onEnterPassword = {},
    onClickSignIn = {},
    onClickChangeServer = {},
  )
}

@ActualScreenPreview
@Composable
private fun WithErrorMessage() = PreviewActualScreen {
  LoginScreenImpl(
    versions = ActualVersions(app = "1.2.3", server = "24.3.0"),
    enteredPassword = Password(value = "abcd1234"),
    url = ServerUrl("https://this.is.a.long.url.discombobulated.com/actual/budget/whatever.json"),
    isLoading = true,
    loginFailure = LoginResult.InvalidPassword,
    onClickBack = {},
    onEnterPassword = {},
    onClickSignIn = {},
    onClickChangeServer = {},
  )
}
