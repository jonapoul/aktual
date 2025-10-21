/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.account.ui.password

import aktual.account.vm.ChangePasswordState
import aktual.account.vm.ChangePasswordViewModel
import aktual.core.model.AktualVersions
import aktual.core.model.Password
import aktual.core.ui.AktualTypography
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChangePasswordScreen(
  navigator: ChangePasswordNavigator,
  viewModel: ChangePasswordViewModel = metroViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val inputPassword1 by viewModel.inputPassword1.collectAsStateWithLifecycle()
  val inputPassword2 by viewModel.inputPassword2.collectAsStateWithLifecycle()
  val showPasswords by viewModel.showPasswords.collectAsStateWithLifecycle()
  val passwordsMatch by viewModel.passwordsMatch.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val newToken by viewModel.loginToken.collectAsStateWithLifecycle(initialValue = null)

  val token = newToken
  if (token != null) {
    LaunchedEffect(Unit) { navigator.toListBudgets(token) }
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
        PasswordAction.NavBack -> navigator.back()
        PasswordAction.Submit -> viewModel.submit()
        is PasswordAction.SetPassword1 -> viewModel.setPassword1(action.value)
        is PasswordAction.SetPassword2 -> viewModel.setPassword2(action.value)
        is PasswordAction.SetPasswordsVisible -> viewModel.setPasswordsVisible(action.visible)
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
  onAction: (PasswordAction) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val theme = LocalTheme.current
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = {
          IconButton(onClick = { onAction(PasswordAction.NavBack) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = Strings.navBack,
            )
          }
        },
        title = { /* empty */ },
        scrollBehavior = scrollBehavior,
      )
    },
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
        theme = theme,
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
  onAction: (PasswordAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
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
        text = Strings.passwordTitle,
        style = AktualTypography.headlineLarge,
      )

      VerticalSpacer(20.dp)

      Text(
        text = Strings.passwordMessage,
        color = theme.tableRowHeaderText,
        style = AktualTypography.bodyLarge,
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
        is ChangePasswordState.Failure -> Text(text = state.errorMessage(), color = theme.errorText)
        ChangePasswordState.Success -> Text(text = Strings.passwordSuccess, color = theme.successText)
        else -> Unit
      }
    }

    VerticalSpacer(20.dp)

    VersionsText(
      modifier = Modifier.align(Alignment.End),
      versions = versions,
    )
  }
}

@Composable
private fun ChangePasswordState.Failure.errorMessage(): String = when (this) {
  ChangePasswordState.InvalidPassword -> Strings.passwordFailureEmpty
  ChangePasswordState.NetworkFailure -> Strings.passwordFailureNetwork
  ChangePasswordState.OtherFailure -> Strings.passwordFailureOther
  ChangePasswordState.PasswordsDontMatch -> Strings.passwordFailureMatch
  ChangePasswordState.NotLoggedIn -> Strings.passwordFailureLoggedOut
}
