package actual.url.ui

import actual.core.colorscheme.ColorSchemeType
import actual.core.res.CoreStrings
import actual.core.ui.ActualExposedDropDownMenu
import actual.core.ui.ActualFontFamily
import actual.core.ui.ActualScreenPreview
import actual.core.ui.ActualTextField
import actual.core.ui.AppThemeChooser
import actual.core.ui.HorizontalSpacer
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewActualScreen
import actual.core.ui.PrimaryActualTextButtonWithLoading
import actual.core.ui.VersionsText
import actual.core.ui.VerticalSpacer
import actual.core.ui.debugNavigate
import actual.core.versions.ActualVersions
import actual.login.nav.LoginNavRoute
import actual.url.model.Protocol
import actual.url.res.ServerUrlStrings
import actual.url.vm.ServerUrlViewModel
import actual.url.vm.ShouldNavigate
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ServerUrlScreen(
  navController: NavHostController,
  viewModel: ServerUrlViewModel = hiltViewModel(),
) {
  val versions by viewModel.versions.collectAsStateWithLifecycle()
  val enteredUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
  val protocol by viewModel.protocol.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val isEnabled by viewModel.isEnabled.collectAsStateWithLifecycle()
  val shouldNavigate by viewModel.shouldNavigate.collectAsStateWithLifecycle(initialValue = false)
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle(initialValue = null)
  val themeType by viewModel.themeType.collectAsStateWithLifecycle()
  var clickedBack by remember { mutableStateOf(false) }

  DisposableEffect(Unit) {
    onDispose {
      viewModel.clearState()
      clickedBack = false
    }
  }

  val context = LocalContext.current
  val activity = remember { context as? Activity ?: error("$context isn't an activity?") }

  when {
    clickedBack -> SideEffect { activity.finish() }
    shouldNavigate is ShouldNavigate.ToBootstrap -> SideEffect { /* TODO */ }
    shouldNavigate is ShouldNavigate.ToLogin -> SideEffect { navController.debugNavigate(LoginNavRoute) }
  }

  ServerUrlScaffold(
    url = enteredUrl,
    protocol = protocol,
    protocols = viewModel.protocols,
    versions = versions,
    isEnabled = isEnabled,
    isLoading = isLoading,
    errorMessage = errorMessage,
    themeType = themeType,
    onAction = { action ->
      when (action) {
        ServerUrlAction.ConfirmUrl -> viewModel.onClickConfirm()
        ServerUrlAction.NavBack -> clickedBack = true
        is ServerUrlAction.EnterUrl -> viewModel.onEnterUrl(action.url)
        is ServerUrlAction.SelectProtocol -> viewModel.onSelectProtocol(action.protocol)
        is ServerUrlAction.SetTheme -> viewModel.onSetTheme(action.type)
      }
    },
  )
}

@Composable
private fun ServerUrlScaffold(
  url: String,
  protocol: Protocol,
  protocols: ImmutableList<String>,
  versions: ActualVersions,
  isEnabled: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
  themeType: ColorSchemeType,
  onAction: (ServerUrlAction) -> Unit,
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
          IconButton(onClick = { onAction(ServerUrlAction.NavBack) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = CoreStrings.navBack,
            )
          }
        },
        title = {
          Text(
            text = ServerUrlStrings.serverUrlToolbar,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    ServerUrlContent(
      modifier = Modifier.padding(innerPadding),
      url = url,
      protocol = protocol,
      protocols = protocols,
      versions = versions,
      isEnabled = isEnabled,
      isLoading = isLoading,
      errorMessage = errorMessage,
      themeType = themeType,
      onAction = onAction,
    )
  }
}

@Stable
@Composable
private fun ServerUrlContent(
  url: String,
  protocol: Protocol,
  protocols: ImmutableList<String>,
  versions: ActualVersions,
  isEnabled: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
  themeType: ColorSchemeType,
  onAction: (ServerUrlAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val theme = LocalTheme.current

  Column(
    modifier = modifier
      .background(theme.pageBackground)
      .padding(16.dp)
      .wrapContentWidth()
      .wrapContentHeight(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.Start,
  ) {
    Text(
      text = ServerUrlStrings.serverUrlTitle,
      style = MaterialTheme.typography.headlineLarge,
    )

    VerticalSpacer(height = 15.dp)

    Text(
      text = ServerUrlStrings.serverUrlMessage,
      color = theme.tableRowHeaderText,
      style = MaterialTheme.typography.bodyLarge,
    )

    VerticalSpacer(height = 20.dp)

    Row(
      modifier = Modifier.fillMaxWidth(),
    ) {
      ActualExposedDropDownMenu(
        modifier = Modifier.width(110.dp),
        value = protocol.toString(),
        options = protocols,
        onValueChange = { onAction(ServerUrlAction.SelectProtocol(Protocol.fromString(it))) },
      )

      HorizontalSpacer(width = 5.dp)

      val focusManager = LocalFocusManager.current

      ActualTextField(
        modifier = Modifier.weight(1f),
        value = url,
        onValueChange = { onAction(ServerUrlAction.EnterUrl(it.lowercase())) },
        placeholderText = EXAMPLE_URL,
        keyboardOptions = KeyboardOptions(
          autoCorrectEnabled = false,
          capitalization = KeyboardCapitalization.None,
          keyboardType = KeyboardType.Uri,
          imeAction = ImeAction.Go,
        ),
        keyboardActions = KeyboardActions(
          onGo = {
            focusManager.clearFocus()
            onAction(ServerUrlAction.ConfirmUrl)
          },
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
        text = ServerUrlStrings.serverUrlConfirm,
        isLoading = isLoading,
        isEnabled = isEnabled,
        onClick = { onAction(ServerUrlAction.ConfirmUrl) },
      )
    }

    if (errorMessage != null) {
      VerticalSpacer(20.dp)

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = errorMessage,
        fontFamily = ActualFontFamily,
        color = theme.errorText,
        textAlign = TextAlign.Center,
      )
    }

    Spacer(
      modifier = Modifier.weight(1f),
    )

    AppThemeChooser(
      selected = themeType,
      onSelect = { type -> onAction(ServerUrlAction.SetTheme(type)) },
    )

    Box(
      modifier = Modifier.fillMaxWidth(),
    ) {
      VersionsText(
        modifier = Modifier.align(Alignment.BottomEnd),
        versions = versions,
      )
    }
  }
}

private const val EXAMPLE_URL = "example.com"

@ActualScreenPreview
@Composable
private fun Regular() = PreviewActualScreen { type ->
  ServerUrlScaffold(
    url = "",
    protocol = Protocol.Https,
    protocols = persistentListOf("http", "https"),
    versions = ActualVersions(app = "1.2.3", server = "24.3.0"),
    isEnabled = true,
    isLoading = false,
    themeType = type,
    onAction = {},
    errorMessage = null,
  )
}

@ActualScreenPreview
@Composable
private fun WithErrorMessage() = PreviewActualScreen { type ->
  ServerUrlScaffold(
    url = "my.server.com:1234/path",
    protocol = Protocol.Http,
    protocols = persistentListOf("http", "https"),
    versions = ActualVersions(app = "1.2.3", server = "24.3.0"),
    isEnabled = true,
    isLoading = true,
    themeType = type,
    onAction = {},
    errorMessage = "Hello this is an error message, split over multiple lines so you can see how it behaves",
  )
}
