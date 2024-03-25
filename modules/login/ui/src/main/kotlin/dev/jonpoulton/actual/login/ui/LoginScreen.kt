package dev.jonpoulton.actual.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jonpoulton.actual.core.model.ActualVersions
import dev.jonpoulton.actual.core.model.Password
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.ActualTextField
import dev.jonpoulton.actual.core.ui.BareActualTextButton
import dev.jonpoulton.actual.core.ui.HorizontalSpacer
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualScreen
import dev.jonpoulton.actual.core.ui.PrimaryActualTextButtonWithLoading
import dev.jonpoulton.actual.core.ui.VersionsText
import dev.jonpoulton.actual.core.ui.VerticalSpacer
import dev.jonpoulton.actual.login.vm.LoginViewModel
import dev.jonpoulton.actual.core.res.R as ResR

@Composable
fun LoginScreen(
  navigator: LoginNavigator,
  viewModel: LoginViewModel = hiltViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val enteredPassword by viewModel.enteredPassword.collectAsStateWithLifecycle()
  val url by viewModel.serverUrl.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val shouldStartSyncing by viewModel.shouldStartSyncing.collectAsStateWithLifecycle(initialValue = false)

  if (shouldStartSyncing) {
    navigator.syncBudget()
  }

  LoginScreenImpl(
    versions = versions,
    enteredPassword = enteredPassword,
    url = url,
    errorMessage = errorMessage,
    onClickSignIn = viewModel::onClickSignIn,
    onClickChangeServer = { navigator.changeServer() },
  )
}

@Composable
private fun LoginScreenImpl(
  versions: ActualVersions,
  enteredPassword: Password,
  url: ServerUrl,
  errorMessage: String?,
  onClickSignIn: () -> Unit,
  onClickChangeServer: () -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val colorScheme = LocalActualColorScheme.current
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = colorScheme.mobileHeaderBackground,
          titleContentColor = colorScheme.mobileHeaderText,
        ),
        title = {
          Text(
            text = stringResource(id = ResR.string.login_toolbar),
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
      errorMessage = errorMessage,
      onClickSignIn = onClickSignIn,
      onClickChangeServer = onClickChangeServer,
    )
  }
}

@Stable
@Composable
private fun Content(
  modifier: Modifier,
  versions: ActualVersions,
  enteredPassword: Password,
  url: ServerUrl,
  errorMessage: String?,
  onClickSignIn: () -> Unit,
  onClickChangeServer: () -> Unit,
) {
  val colorScheme = LocalActualColorScheme.current
  Column(
    modifier = modifier
      .background(colorScheme.pageBackground)
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
        text = stringResource(id = ResR.string.login_title),
        style = MaterialTheme.typography.displayLarge,
        fontFamily = ActualFontFamily,
        fontSize = 25.sp,
        color = colorScheme.pageTextPositive,
      )

      VerticalSpacer(15.dp)

      Text(
        text = stringResource(id = ResR.string.login_message),
        fontFamily = ActualFontFamily,
        fontSize = 16.sp,
        color = colorScheme.pageText,
      )

      VerticalSpacer(20.dp)

      ActualTextField(
        modifier = Modifier.fillMaxWidth(1f),
        value = enteredPassword.toString(),
        onValueChange = { },
        placeholderText = stringResource(id = ResR.string.login_password_hint),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
          autoCorrect = false,
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
        text = stringResource(id = ResR.string.login_sign_in),
        isLoading = false,
        onClick = onClickSignIn,
      )

      if (errorMessage != null) {
        VerticalSpacer(20.dp)

        Text(
          modifier = Modifier.fillMaxWidth(),
          text = errorMessage,
          fontFamily = ActualFontFamily,
          color = colorScheme.errorText,
          textAlign = TextAlign.Center,
        )
      }
    }

    VerticalSpacer(20.dp)

    UsingServer(
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

@Stable
@Composable
private fun UsingServer(
  url: ServerUrl,
  onClickChange: () -> Unit,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 16.sp,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      val colorScheme = LocalActualColorScheme.current

      Text(
        text = stringResource(id = ResR.string.login_using_server),
        fontSize = fontSize,
        color = colorScheme.pageText,
      )

      HorizontalSpacer(width = 5.dp)

      Text(
        text = url.toString(),
        fontSize = fontSize,
        color = colorScheme.pageText,
        fontWeight = FontWeight.Bold,
      )
    }

    BareActualTextButton(
      text = stringResource(id = ResR.string.login_server_change),
      onClick = onClickChange,
    )
  }
}

@PreviewActualScreen
@Composable
private fun Regular() = PreviewActualScreen {
  LoginScreenImpl(
    versions = ActualVersions(app = "1.2.3", server = "24.3.0"),
    enteredPassword = Password.Empty,
    url = ServerUrl.Demo,
    errorMessage = null,
    onClickSignIn = {},
    onClickChangeServer = {},
  )
}

@PreviewActualScreen
@Composable
private fun WithErrorMessage() = PreviewActualScreen {
  LoginScreenImpl(
    versions = ActualVersions(app = "1.2.3", server = "24.3.0"),
    enteredPassword = Password("abcd1234"),
    url = ServerUrl.Demo,
    errorMessage = "Something broke, idiot",
    onClickSignIn = {},
    onClickChangeServer = {},
  )
}
