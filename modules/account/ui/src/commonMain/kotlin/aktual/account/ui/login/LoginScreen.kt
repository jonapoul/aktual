/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.account.ui.login

import aktual.account.domain.LoginResult
import aktual.account.vm.LoginViewModel
import aktual.core.model.AktualVersions
import aktual.core.model.Password
import aktual.core.model.ServerUrl
import aktual.core.ui.AktualTypography
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.core.ui.UsingServerText
import aktual.core.ui.VersionsText
import aktual.core.ui.WavyBackground
import aktual.core.ui.metroViewModel
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
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
  versions: AktualVersions,
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
  versions: AktualVersions,
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
        style = AktualTypography.headlineLarge,
      )

      VerticalSpacer(15.dp)

      Text(
        text = Strings.loginMessage,
        color = theme.tableRowHeaderText,
        style = AktualTypography.bodyLarge,
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
