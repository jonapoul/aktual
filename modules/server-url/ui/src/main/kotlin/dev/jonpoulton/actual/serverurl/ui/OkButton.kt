package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.PreviewActual

@Composable
internal fun OkButton(
  isLoading: Boolean = false,
  onClick: () -> Unit,
) {
  Button(
    modifier = Modifier.widthIn(min = 1.dp),
    onClick = onClick,
    shape = ButtonShape,
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    contentPadding = ActualButtonPadding,
  ) {
    if (isLoading) {
      OkButtonLoadingContent()
    } else {
      OkButtonTextContent()
    }
  }
}

@Composable
private fun OkButtonTextContent() {
  Text(
    text = stringResource(id = android.R.string.ok),
    color = MaterialTheme.colorScheme.onPrimaryContainer,
    fontFamily = ActualFontFamily,
  )
}

@Composable
private fun OkButtonLoadingContent() {
  CircularProgressIndicator(
    color = MaterialTheme.colorScheme.onPrimaryContainer,
  )
}

@PreviewThemes
@Composable
private fun PreviewNormal() = PreviewActual {
  OkButton(
    isLoading = false,
    onClick = {},
  )
}

@PreviewThemes
@Composable
private fun PreviewLoading() = PreviewActual {
  OkButton(
    isLoading = true,
    onClick = {},
  )
}
