package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import dev.jonpoulton.actual.core.ui.ActualColors
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.PreviewActual
import dev.jonpoulton.actual.serverurl.vm.ServerUrlViewModel
import dev.jonpoulton.actual.core.res.R as ResR

@Composable
fun ServerUrlScreen(
  navController: NavHostController,
  viewModel: ServerUrlViewModel = hiltViewModel(),
) {
  val appVersion by viewModel.appVersion.collectAsStateWithLifecycle(initialValue = null)
  val serverVersion by viewModel.serverVersion.collectAsStateWithLifecycle(initialValue = null)

  ServerUrlScreenImpl(
    appVersion = appVersion,
    serverVersion = serverVersion,
  )
}

@Composable
private fun ServerUrlScreenImpl(
  appVersion: String?,
  serverVersion: String?,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.primary,
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
      appVersion = appVersion,
      serverVersion = serverVersion,
    )
  }
}

@Composable
private fun Content(
  modifier: Modifier,
  appVersion: String?,
  serverVersion: String?,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .wrapContentWidth()
        .wrapContentHeight()
        .padding(32.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        modifier = Modifier.padding(bottom = 15.dp),
        text = stringResource(id = ResR.string.server_url_title),
        fontFamily = ActualFontFamily,
        fontWeight = FontWeight.W700,
        color = ActualColors.purple200,
        fontSize = 25.sp,
      )

      Text(
        text = stringResource(id = ResR.string.server_url_message),
        fontFamily = ActualFontFamily,
        color = ActualColors.navy150,
      )

      TextField(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .padding(vertical = 20.dp),
        value = "",
        placeholder = { Text(text = "https://example.com") },
        onValueChange = { /* TODO: Handle input */ },
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OkButton(
          onClick = {},
        )

        CancelButton(
          onClick = {},
        )
      }
    }

    VersionsText(appVersion, serverVersion)
  }
}

@Composable
private fun OkButton(onClick: () -> Unit) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val backgroundColor = if (isPressed) ActualColors.purple600 else ActualColors.purple400

  Button(
    modifier = Modifier.widthIn(min = 1.dp),
    onClick = onClick,
    shape = ButtonShape,
    colors = ButtonDefaults.buttonColors(
      containerColor = backgroundColor,
      contentColor = ActualColors.white,
    ),
    contentPadding = ActualButtonPadding,
    content = { Text(text = "OK", fontFamily = ActualFontFamily) },
    interactionSource = interactionSource,
  )
}

@Composable
private fun CancelButton(onClick: () -> Unit) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val backgroundColor = if (isPressed) Color(color = 0x4DC8C8C8) else ActualColors.transparent

  TextButton(
    modifier = Modifier
      .widthIn(min = 1.dp)
      .padding(start = 10.dp),
    onClick = onClick,
    shape = RoundedCornerShape(size = 4.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = backgroundColor,
      contentColor = ActualColors.navy150,
    ),
    contentPadding = ActualButtonPadding,
    content = { Text(text = "Cancel", fontFamily = ActualFontFamily) },
    interactionSource = interactionSource,
  )
}

@Composable
fun BoxScope.VersionsText(
  appVersion: String?,
  serverVersion: String?,
) {
  val appVersionString = if (appVersion == null) "App: Unknown" else "App: $appVersion"
  val serverVersionString = if (serverVersion == null) "Server: Unknown" else "Server: $serverVersion"
  Text(
    modifier = Modifier.align(Alignment.BottomEnd),
    text = "$appVersionString | $serverVersionString",
    fontSize = 13.sp,
    color = ActualColors.navy500,
  )
}

private val ActualButtonPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
private val ButtonShape = RoundedCornerShape(size = 4.dp)

@PreviewThemes
@Composable
private fun Preview() = PreviewActual {
  ServerUrlScreenImpl(
    serverVersion = "v24.3.0",
    appVersion = "v1.2.3",
  )
}
