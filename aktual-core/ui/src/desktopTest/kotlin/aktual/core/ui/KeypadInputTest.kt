package aktual.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeypadInputTest {
  @Test
  fun `Digits and separators are typed as they are`() {
    assertEquals(KeypadInput.Append("7"), keypadInput(Seven, '7'))
    assertEquals(KeypadInput.Append("3"), keypadInput(NumPad3, '3'))
    assertEquals(KeypadInput.Append("."), keypadInput(Period, '.'))
    assertEquals(KeypadInput.Append(","), keypadInput(Comma, ','))
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
    assertEquals(KeypadInput.Backspace, keypadInput(Backspace, '\b'))
    assertEquals(KeypadInput.Done, keypadInput(Enter, '\n'))
    assertEquals(KeypadInput.Done, keypadInput(NumPadEnter, '\n'))
  }

  @Test
  fun `Other keys are ignored`() {
    assertNull(keypadInput(A, 'a'))
    assertNull(keypadInput(Spacebar, ' '))
    assertNull(keypadInput(DirectionLeft, MIN_VALUE))
  }
}
