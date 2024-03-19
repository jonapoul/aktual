package dev.jonpoulton.actual.serverurl.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualColumn
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

@Preview
@Composable
private fun PreviewBothNull() = PreviewActualColumn {
  VersionsText(appVersion = null, serverVersion = null)
}

@Preview
@Composable
private fun PreviewAppNull() = PreviewActualColumn {
  VersionsText(appVersion = null, serverVersion = "1.2.3")
}

@Preview
@Composable
private fun PreviewServerNull() = PreviewActualColumn {
  VersionsText(appVersion = "1.2.3", serverVersion = null)
}

@Preview
@Composable
private fun PreviewBothVersions() = PreviewActualColumn {
  VersionsText(appVersion = "1.2.3", serverVersion = "2.3.4")
}
