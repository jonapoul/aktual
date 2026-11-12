package aktual.budget.rules.components

import aktual.core.ui.AktualTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

// Commits when the field loses focus rather than on every keystroke, since each change to the plan
// re-runs the simulation. Changes from outside show up while the field isn't being edited
@Composable
fun CommitTextField(
  text: String,
  onCommit: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  isEnabled: Boolean = true,
  keyboardType: KeyboardType = Text,
  capitalization: KeyboardCapitalization = None,
) {
  val state = rememberTextFieldState(text)
  var isFocused by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val currentOnCommit by rememberUpdatedState(onCommit)

  // Also puts back the last value when the typed text was rejected
  SideEffect(text, isFocused) { if (!isFocused) state.setTextAndPlaceCursorAtEnd(text) }

  AktualTextField(
    modifier =
      modifier.onFocusChanged { focus ->
        if (isFocused && !focus.isFocused) currentOnCommit(state.text.toString())
        isFocused = focus.isFocused
      },
    state = state,
    placeholderText = placeholder,
    isEnabled = isEnabled,
    singleLine = true,
    keyboardOptions =
      KeyboardOptions(
        capitalization = capitalization,
        keyboardType = keyboardType,
        imeAction = Done,
      ),
    onKeyboardAction = { focusManager.clearFocus() },
  )
}
