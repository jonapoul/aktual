package aktual.budget.transactions.ui.edit

import aktual.core.l10n.Strings
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.plainText
import aktual.core.ui.plainTextClipEntry
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.launch

// An amount has no text field to select, since the keypad types it, so a long press or right
// click opens a menu to copy it or paste over it
@Stable
internal class AmountMenuState {
  var isExpanded by mutableStateOf(false)
}

internal fun Modifier.amountClickable(
  menu: AmountMenuState,
  label: String,
  onClick: () -> Unit,
): Modifier =
  pointerInput(menu) {
      awaitPointerEventScope {
        while (true) {
          val event = awaitPointerEvent()
          if (event.type == Press && event.buttons.isSecondaryPressed) {
            menu.isExpanded = true
            event.changes.fastForEach { it.consume() }
          }
        }
      }
    }
    .combinedClickable(
      onClickLabel = label,
      onClick = onClick,
      onLongClick = { menu.isExpanded = true },
    )

// While the whole amount is selected, so the next digit typed replaces it
internal fun Modifier.selectionHighlight(show: Boolean, color: Color): Modifier =
  if (show) background(color, SelectionShape) else this

// Anchored to the amount it's placed in
@Composable
internal fun AmountMenu(menu: AmountMenuState, copyText: String, onPaste: (String) -> Unit) {
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  AktualDropdownMenu(expanded = menu.isExpanded, onDismissRequest = { menu.isExpanded = false }) {
    AktualDropdownMenuItem(
      text = Strings.inputCopy,
      enabled = copyText.isNotEmpty(),
      onClick = {
        menu.isExpanded = false
        scope.launch { clipboard.setClipEntry(plainTextClipEntry(copyText)) }
      },
    )
    AktualDropdownMenuItem(
      text = Strings.inputPaste,
      onClick = {
        menu.isExpanded = false
        scope.launch { clipboard.getClipEntry()?.plainText()?.let(onPaste) }
      },
    )
  }
}

private val SelectionShape = RoundedCornerShape(4.dp)
