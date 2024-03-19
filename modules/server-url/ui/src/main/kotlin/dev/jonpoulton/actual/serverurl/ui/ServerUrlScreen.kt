package dev.jonpoulton.actual.serverurl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jonpoulton.actual.core.model.Protocol
import dev.jonpoulton.actual.core.ui.ActualExposedDropDownMenu
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.BigActualTextField
import dev.jonpoulton.actual.core.ui.HorizontalSpacer
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualScreen
import dev.jonpoulton.actual.core.ui.PrimaryActualTextButtonWithLoading
import dev.jonpoulton.actual.core.ui.VerticalSpacer
import dev.jonpoulton.actual.serverurl.vm.ServerUrlViewModel
import dev.jonpoulton.actual.serverurl.vm.ShouldNavigate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import dev.jonpoulton.actual.core.res.R as ResR

@Composable
fun ServerUrlScreen(
  navigator: ServerUrlNavigator,
  viewModel: ServerUrlViewModel = hiltViewModel(),
) {
  val appVersion by viewModel.appVersion.collectAsStateWithLifecycle()
  val serverVersion by viewModel.serverVersion.collectAsStateWithLifecycle(initialValue = null)
  val enteredUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
  val protocol by viewModel.protocol.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val shouldNavigate by viewModel.shouldNavigate.collectAsStateWithLifecycle(initialValue = false)
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle(initialValue = null)

  when (shouldNavigate) {
    is ShouldNavigate.ToBootstrap -> navigator.navigateToBootstrap()
    is ShouldNavigate.ToLogin -> navigator.navigateToLogin()
  }

  val focusManager = LocalFocusManager.current

  ServerUrlScreenImpl(
    url = enteredUrl,
    protocol = protocol,
    protocols = viewModel.protocols,
    appVersion = appVersion,
    serverVersion = serverVersion,
    isLoading = isLoading,
    errorMessage = errorMessage,
    onClickConfirm = {
      viewModel.onClickConfirm()
      focusManager.clearFocus()
    },
    onUrlEntered = viewModel::onUrlEntered,
    onProtocolSelected = viewModel::onProtocolSelected,
  )
}

@Composable
private fun ServerUrlScreenImpl(
  url: String,
  protocol: Protocol,
  protocols: ImmutableList<String>,
  appVersion: String?,
  serverVersion: String?,
  isLoading: Boolean,
  errorMessage: String?,
  onClickConfirm: () -> Unit,
  onUrlEntered: (String) -> Unit,
  onProtocolSelected: (Protocol) -> Unit,
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
      protocol = protocol,
      protocols = protocols,
      appVersion = appVersion,
      serverVersion = serverVersion,
      isLoading = isLoading,
      errorMessage = errorMessage,
      onClickConfirm = onClickConfirm,
      onUrlEntered = onUrlEntered,
      onProtocolSelected = onProtocolSelected,
    )
  }
}

@Stable
@Composable
private fun Content(
  modifier: Modifier,
  url: String,
  protocol: Protocol,
  protocols: ImmutableList<String>,
  appVersion: String?,
  serverVersion: String?,
  isLoading: Boolean,
  errorMessage: String?,
  onClickConfirm: () -> Unit,
  onUrlEntered: (String) -> Unit,
  onProtocolSelected: (Protocol) -> Unit,
) {
  val colorScheme = LocalActualColorScheme.current
  Box(
    modifier = modifier
      .background(colorScheme.pageBackground)
      .fillMaxSize()
      .padding(16.dp),
    contentAlignment = Alignment.TopCenter,
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
        fontSize = 25.sp,
        color = colorScheme.pageTextPositive,
      )

      VerticalSpacer(height = 15.dp)

      Text(
        text = stringResource(id = ResR.string.server_url_message),
        fontFamily = ActualFontFamily,
        fontSize = 13.sp,
        color = colorScheme.pageText,
      )

      VerticalSpacer(height = 20.dp)

      Row(
        modifier = Modifier.fillMaxWidth(),
      ) {
        ActualExposedDropDownMenu(
          modifier = Modifier.width(110.dp),
          value = protocol.toString(),
          options = protocols,
          onValueChange = { onProtocolSelected(Protocol.fromString(it)) },
          showBorder = false,
        )

        HorizontalSpacer(width = 5.dp)

        BigActualTextField(
          modifier = Modifier.weight(1f),
          value = url,
          onValueChange = { onUrlEntered(it.lowercase()) },
          placeholderText = EXAMPLE_URL,
          keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Go,
          ),
          keyboardActions = KeyboardActions(
            onGo = { onClickConfirm() },
          ),
        )
      }
      VerticalSpacer(height = 20.dp)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        PrimaryActualTextButtonWithLoading(
          text = stringResource(id = ResR.string.server_url_confirm),
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

private const val EXAMPLE_URL = "example.com"

@PreviewActualScreen
@Composable
private fun Regular() = PreviewActualScreen {
  ServerUrlScreenImpl(
    url = "",
    protocol = Protocol.Https,
    protocols = persistentListOf("http", "https"),
    appVersion = "1.2.3",
    serverVersion = "24.3.0",
    isLoading = false,
    onClickConfirm = {},
    onUrlEntered = {},
    onProtocolSelected = {},
    errorMessage = null,
  )
}

@PreviewActualScreen
@Composable
private fun WithErrorMessage() = PreviewActualScreen {
  ServerUrlScreenImpl(
    url = "my.server.com:1234/path",
    protocol = Protocol.Http,
    protocols = persistentListOf("http", "https"),
    appVersion = "1.2.3",
    serverVersion = "24.3.0",
    isLoading = true,
    onClickConfirm = {},
    onUrlEntered = {},
    onProtocolSelected = {},
    errorMessage = "Hello this is an error message, split over multiple lines so you can see how it behaves",
  )
}
