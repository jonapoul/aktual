package aktual.prefs.ui.inspect

import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronRight
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.disabled
import aktual.prefs.vm.inspect.ThemeProperty
import aktual.prefs.vm.inspect.parseHexColorOrNull
import aktual.prefs.vm.inspect.toHexString
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
internal fun EditPropertyDialog(
  property: ThemeProperty,
  onConfirm: (Color) -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    EditPropertyDialogContent(property, onConfirm, onDismiss)
  }

@Composable
private fun EditPropertyDialogContent(
  property: ThemeProperty,
  onConfirm: (Color) -> Unit,
  onDismiss: () -> Unit,
) {
  val controller = rememberColorPickerController()
  val hexState = rememberTextFieldState(property.color.toHexString())
  var color by remember { mutableStateOf(property.color) }
  val isValid = hexState.text.toString().parseHexColorOrNull() != null

  // Typing a valid code moves the picker to match. Picker changes are pushed back into the field
  // below, so they come through here too and snap the colour to what the field shows
  LaunchedEffect(Unit) {
    snapshotFlow { hexState.text.toString().parseHexColorOrNull() }
      .collect { parsed ->
        if (parsed != null && parsed != color) {
          color = parsed
          controller.selectByColor(parsed, fromUser = false)
        }
      }
  }

  AktualAlertDialogContent(
    title = property.name,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.settingsThemeInspectCancel) }
      TextButton(enabled = isValid, onClick = { onConfirm(color) }) {
        Text(
          text = Strings.settingsThemeInspectEditSave,
          fontWeight = if (isValid) Bold else null,
          color = if (isValid) colors.numberPositive else colors.buttonNormalDisabledText.disabled,
        )
      }
    },
    content = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = CenterVertically,
      ) {
        Swatch(property.color)
        Icon(AktualIcons.CheveronRight, contentDescription = null)
        Swatch(color)
      }

      HsvColorPicker(
        modifier = Modifier.size(PICKER_SIZE),
        controller = controller,
        initialColor = property.color,
        onColorChanged = { envelope ->
          if (envelope.fromUser) {
            hexState.setTextAndPlaceCursorAtEnd(envelope.color.toHexString())
          }
        },
      )

      BrightnessSlider(
        modifier = Modifier.fillMaxWidth().height(SLIDER_HEIGHT),
        controller = controller,
      )

      AlphaSlider(modifier = Modifier.fillMaxWidth().height(SLIDER_HEIGHT), controller = controller)

      AktualTextField(
        modifier = Modifier.fillMaxWidth(),
        state = hexState,
        placeholderText = Strings.settingsThemeInspectEditPlaceholder,
        singleLine = true,
        supportingText =
          if (isValid) {
            null
          } else {
            { Text(text = Strings.settingsThemeInspectEditInvalid, color = colors.errorText) }
          },
      )
    },
  )
}

@Composable
private fun Swatch(color: Color) =
  Box(
    modifier =
      Modifier.size(SWATCH_SIZE)
        .background(color, CardShape)
        .border(1.dp, colors.pageText, CardShape),
  )

private val PICKER_SIZE = 200.dp
private val SLIDER_HEIGHT = 24.dp
private val SWATCH_SIZE = 32.dp

@Preview
@Composable
private fun PreviewEditPropertyDialog(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    EditPropertyDialogContent(
      property = ThemeProperty(name = "tableRowBackgroundAlternate", color = Color(0xFF2B8FED)),
      onConfirm = {},
      onDismiss = {},
    )
  }
