package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A bottom sheet editing one value, with cancel and confirm buttons along the bottom. It opens
 * fully expanded, so it stays above the keyboard as it grows to fit it. Inside a [SheetHost],
 * confirming closes the whole sheet while cancelling only closes this page.
 */
@Composable
fun EditorSheet(
  title: String,
  cancelText: String,
  confirmText: String,
  canConfirm: Boolean,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  content: @Composable EditorSheetScope.() -> Unit,
) {
  HostedSheet(onDismiss = onDismiss) {
    val sheet = this
    Column(
      modifier =
        modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = EditorSheetDS.padding)
          .padding(bottom = EditorSheetDS.padding),
      verticalArrangement = Arrangement.spacedBy(EditorSheetDS.spacing),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(EditorSheetDS.titleSpacing)) {
        Text(text = title, style = typography.headlineSmall)
        if (subtitle != null) {
          Text(
            text = subtitle.redacted(),
            style = typography.bodyMedium,
            color = colors.pageTextSubdued,
          )
        }
      }

      EditorSheetScope(column = this, hide = sheet::dismiss).content()

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EditorSheetDS.buttonSpacing),
      ) {
        AktualButton(
          modifier = Modifier.weight(1f),
          text = cancelText,
          onClick = { sheet.close {} },
        )
        AktualButton(
          kind = Primary,
          modifier = Modifier.weight(1f),
          text = confirmText,
          isEnabled = canConfirm,
          onClick = { sheet.dismiss(onConfirm) },
        )
      }
    }
  }
}

private object EditorSheetDS {
  val padding = 20.dp
  val spacing = 16.dp
  val titleSpacing = 4.dp
  val buttonSpacing = 12.dp
}
