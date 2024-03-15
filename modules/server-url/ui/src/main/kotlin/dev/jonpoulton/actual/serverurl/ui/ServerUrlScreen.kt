package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.BigActualTextField
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActual
import dev.jonpoulton.actual.core.ui.PrimaryActualTextButtonWithLoading
import dev.jonpoulton.actual.core.ui.VerticalSpacer
import dev.jonpoulton.actual.serverurl.vm.ServerUrlViewModel
import dev.jonpoulton.actual.core.res.R as ResR

@Composable
fun ServerUrlScreen(
  navigator: ServerUrlNavigator,
  viewModel: ServerUrlViewModel = hiltViewModel(),
) {
  val appVersion by viewModel.appVersion.collectAsStateWithLifecycle()
  val serverVersion by viewModel.serverVersion.collectAsStateWithLifecycle(initialValue = null)
  val enteredUrl by viewModel.enteredUrl.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val shouldNavigate by viewModel.shouldNavigate.collectAsStateWithLifecycle(initialValue = false)
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle(initialValue = null)

  if (shouldNavigate) {
    navigator.navigateToLogin()
  }

  ServerUrlScreenImpl(
    url = enteredUrl,
    appVersion = appVersion,
    serverVersion = serverVersion,
    isLoading = isLoading,
    errorMessage = errorMessage,
    onClickConfirm = viewModel::onClickConfirm,
    onUrlEntered = viewModel::onUrlEntered,
  )
}

@Composable
private fun ServerUrlScreenImpl(
  url: String,
  appVersion: String?,
  serverVersion: String?,
  isLoading: Boolean,
  errorMessage: String?,
  onClickConfirm: () -> Unit,
  onUrlEntered: (String) -> Unit,
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
            text = stringResource(id = ResR.string.server_url_toolbar),
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
      url = url,
      appVersion = appVersion,
      serverVersion = serverVersion,
      isLoading = isLoading,
      errorMessage = errorMessage,
      onClickConfirm = onClickConfirm,
      onUrlEntered = onUrlEntered,
    )
  }
}

@Stable
@Composable
private fun Content(
  modifier: Modifier,
  url: String,
  appVersion: String?,
  serverVersion: String?,
  isLoading: Boolean,
  errorMessage: String?,
  onClickConfirm: () -> Unit,
  onUrlEntered: (String) -> Unit,
) {
  val colorScheme = LocalActualColorScheme.current
  Box(
    modifier = modifier
      .background(colorScheme.pageBackground)
      .fillMaxSize()
      .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .wrapContentWidth()
        .wrapContentHeight(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        text = stringResource(id = ResR.string.server_url_title),
        style = MaterialTheme.typography.displayLarge,
        color = colorScheme.pageTextPositive,
      )

      VerticalSpacer(height = 15.dp)

      Text(
        text = stringResource(id = ResR.string.server_url_message),
        fontFamily = ActualFontFamily,
        color = colorScheme.pageText,
      )

      VerticalSpacer(height = 20.dp)

      BigActualTextField(
        modifier = Modifier.fillMaxWidth(),
        value = url,
        onValueChange = onUrlEntered,
        placeholderText = EXAMPLE_URL,
      )

      VerticalSpacer(height = 20.dp)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        PrimaryActualTextButtonWithLoading(
          text = "Confirm",
          isLoading = isLoading,
          onClick = onClickConfirm,
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter),
    ) {
      if (errorMessage != null) {
        Text(
          text = errorMessage,
          fontFamily = ActualFontFamily,
          color = colorScheme.errorText,
          textAlign = TextAlign.Center,
        )
      }

      VersionsText(
        modifier = Modifier.align(Alignment.End),
        appVersion = appVersion,
        serverVersion = serverVersion,
      )
    }
  }
}

private const val EXAMPLE_URL = "https://example.com"

@PreviewThemes
@Composable
private fun Preview() = PreviewActual {
  ServerUrlScreenImpl(
    url = "",
    appVersion = "v1.2.3",
    serverVersion = "v24.3.0",
    isLoading = false,
    onClickConfirm = {},
    onUrlEntered = {},
    errorMessage = null,
  )
}

@PreviewThemes
@Composable
private fun PreviewWithErrorMessage() = PreviewActual {
  ServerUrlScreenImpl(
    url = "",
    appVersion = "v1.2.3",
    serverVersion = "v24.3.0",
    isLoading = false,
    onClickConfirm = {},
    onUrlEntered = {},
    errorMessage = "Hello this is an error message, split over multiple lines so you can see how it behaves",
  )
}
