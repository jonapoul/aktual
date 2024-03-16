package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActual
import dev.jonpoulton.actual.core.res.R as ResR

@Composable
internal fun VersionsText(
  modifier: Modifier = Modifier,
  appVersion: String?,
  serverVersion: String?,
) {
  val appVersionString = if (appVersion == null) {
    stringResource(id = ResR.string.server_url_version_app_null)
  } else {
    stringResource(id = ResR.string.server_url_version_app, appVersion)
  }
  val serverVersionString = if (serverVersion == null) {
    stringResource(id = ResR.string.server_url_version_server_null)
  } else {
    stringResource(id = ResR.string.server_url_version_server, serverVersion)
  }

  val colorScheme = LocalActualColorScheme.current

  Text(
    modifier = modifier.padding(15.dp),
    text = "$appVersionString | $serverVersionString",
    fontSize = 13.sp,
    color = colorScheme.pageTextSubdued,
  )
}

@PreviewThemes
@Composable
private fun PreviewBothNull() = PreviewActual {
  VersionsText(appVersion = null, serverVersion = null)
}

@PreviewThemes
@Composable
private fun PreviewAppNull() = PreviewActual {
  VersionsText(appVersion = null, serverVersion = "1.2.3")
}

@PreviewThemes
@Composable
private fun PreviewServerNull() = PreviewActual {
  VersionsText(appVersion = "1.2.3", serverVersion = null)
}

@PreviewThemes
@Composable
private fun PreviewBothVersions() = PreviewActual {
  VersionsText(appVersion = "1.2.3", serverVersion = "2.3.4")
}
