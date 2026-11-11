package aktual.core.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.setTextAndSelectAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeypadInputTest {
  @Test
  fun `Digits are typed as they are, and either separator as a point`() {
    assertEquals(KeypadInput.Append("7"), keypadInput(Seven, '7'))
    assertEquals(KeypadInput.Append("3"), keypadInput(NumPad3, '3'))
    assertEquals(KeypadInput.Append("."), keypadInput(Period, '.'))
    assertEquals(KeypadInput.Append("."), keypadInput(Comma, ','))
  }

  @Test
  fun `Operators are typed as the keypad's symbols`() {
    assertEquals(KeypadInput.Append("+"), keypadInput(Equals, '+'))
    assertEquals(KeypadInput.Append("−"), keypadInput(Minus, '-'))
    assertEquals(KeypadInput.Append("×"), keypadInput(NumPadMultiply, '*'))
    assertEquals(KeypadInput.Append("÷"), keypadInput(Slash, '/'))
  }

  @Test
  fun `Backspace and Enter`() {
    assertEquals(Backspace, keypadInput(Backspace, '\b'))
    assertEquals(Done, keypadInput(Enter, '\n'))
    assertEquals(Done, keypadInput(NumPadEnter, '\n'))
  }

  @Test
  fun `Other keys are ignored`() {
    assertNull(keypadInput(A, 'a'))
    assertNull(keypadInput(Spacebar, ' '))
    assertNull(keypadInput(DirectionLeft, MIN_VALUE))
  }

  @Test
  fun `Ctrl or Cmd shortcuts`() {
    assertEquals(SelectAll, shortcutInput(A))
    assertEquals(Copy, shortcutInput(C))
    assertEquals(Paste, shortcutInput(V))
    assertNull(shortcutInput(Seven))
  }

  @Test
  fun `A digit replaces the amount while it's all selected`() {
    val state = TextFieldState()
    state.setTextAndSelectAll("12.34")
    state.typeKey("5")
    assertEquals("5", state.text.toString())
    state.typeKey("6")
    assertEquals("56", state.text.toString())
  }

  @Test
  fun `An operator or backspace carries on from the end of a selected amount`() {
    val state = TextFieldState()
    state.setTextAndSelectAll("12.34")
    state.typeKey("+")
    assertEquals("12.34+", state.text.toString())

    state.setTextAndSelectAll("12.34")
    state.backspace()
    assertEquals("12.3", state.text.toString())
  }

  @Test
  fun `Keys append when nothing is selected`() {
    val state = TextFieldState()
    state.setTextAndPlaceCursorAtEnd("12")
    state.typeKey("3")
    assertEquals("123", state.text.toString())
  }
}
