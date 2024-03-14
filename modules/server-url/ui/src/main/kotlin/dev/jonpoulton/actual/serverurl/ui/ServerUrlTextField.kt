package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.jonpoulton.actual.core.ui.PreviewActual

@Composable
internal fun ServerUrlTextField(
  url: String,
  onUrlEntered: (String) -> Unit,
) {
  val textColor = MaterialTheme.colorScheme.onBackground
  val hintColor = textColor.copy(alpha = 0.5f)
  TextField(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(elevation = 4.dp),
    value = url,
    placeholder = { Text(text = EXAMPLE_URL) },
    shape = FieldShape,
    colors = TextFieldDefaults.colors(
      focusedTextColor = textColor,
      unfocusedTextColor = textColor,
      focusedPlaceholderColor = hintColor,
      unfocusedPlaceholderColor = hintColor,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
      disabledIndicatorColor = Color.Transparent,
    ),
    onValueChange = onUrlEntered,
  )
}

private const val EXAMPLE_URL = "https://example.com"

private val FieldShape = RoundedCornerShape(size = 8.dp)

@PreviewThemes
@Composable
private fun PreviewWithUrl() = PreviewActual {
  ServerUrlTextField(
    url = "https://my.website.com:1234/actual",
    onUrlEntered = {},
  )
}

@PreviewThemes
@Composable
private fun PreviewEmpty() = PreviewActual {
  ServerUrlTextField(
    url = "",
    onUrlEntered = {},
  )
}
