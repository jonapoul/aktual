package aktual.budget.tags.ui.edit

import aktual.budget.tags.ui.contrastingTextColor
import aktual.budget.tags.vm.toColorOrNull
import aktual.budget.tags.vm.toHex
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Tune
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.ButtonShape
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredBooleanParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.textField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.collections.immutable.persistentListOf

@Suppress("LongMethod")
@Composable
internal fun TagColorPicker(
  color: Color?,
  onColorChange: (Color) -> Unit,
  onErrorChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  showAdvanced: Boolean = false,
) {
  val controller = rememberColorPickerController()
  val hexState = rememberTextFieldState()
  val hexInteraction = remember { MutableInteractionSource() }
  val hexFocused by hexInteraction.collectIsFocusedAsState()

  val currentOnColorChange by rememberUpdatedState(onColorChange)
  val currentOnErrorChange by rememberUpdatedState(onErrorChange)

  var hexError by remember { mutableStateOf(false) }
  LaunchedEffect(hexError) { currentOnErrorChange(hexError) }

  // keep the advanced wheel + brightness slider tracking the current colour, wherever it came from
  // (a preset, the hex field, or the colour the editor seeded). HsvColorPicker's initialColor only
  // applies on first composition, so later changes have to be pushed onto the controller here. The
  // equality guard avoids fighting the wheel mid-drag, where the colour already matches
  LaunchedEffect(color) {
    if (color != null && controller.selectedColor.value != color) {
      controller.selectByColor(color, fromUser = false)
    }
  }

  // when the colour changes from elsewhere (wheel, slider, preset), reflect it in the field.
  // Skip only when the field already represents this colour — i.e. the user just typed it — so we
  // don't reformat their input mid-edit. The canonical value is always valid, so clear any error
  LaunchedEffect(color, hexFocused) {
    val fieldAlreadyMatches = hexState.text.toString().trim().toColorOrNull() == color
    if (hexFocused && fieldAlreadyMatches) return@LaunchedEffect

    val hex = color?.toHex().orEmpty()
    if (!hexState.text.contentEquals(hex, ignoreCase = true)) {
      hexState.setTextAndPlaceCursorAtEnd(hex)
    }
    hexError = false
  }

  // auto-apply what the user types: a valid hex updates the colour, anything else flags an error
  LaunchedEffect(Unit) {
    snapshotFlow { hexState.text.toString() }
      .collect { text ->
        if (!hexFocused) return@collect
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
          hexError = false
        } else {
          val parsed = trimmed.toColorOrNull()
          if (parsed == null) {
            hexError = true
          } else {
            hexError = false
            currentOnColorChange(parsed)
          }
        }
      }
  }

  var advancedVisible by remember { mutableStateOf(showAdvanced) }

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(EditTagDS.colorPickerSectionSpacing),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(EditTagDS.colorPickerSectionSpacing),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // always visible: the hex field
      ColorPickerTextField(
        modifier = Modifier.weight(1f),
        hexState = hexState,
        hexInteraction = hexInteraction,
        color = color,
        isError = hexError,
      )

      BareIconButton(
        imageVector = MaterialIcons.Tune,
        contentDescription = Strings.tagsCreateColorAdvanced,
        onClick = { advancedVisible = !advancedVisible },
        colors =
          if (advancedVisible) IconButtonColorProvider.Primary else IconButtonColorProvider.Bare,
      )
    }

    // the preset swatches — hidden while the advanced wheel/slider is showing
    AnimatedVisibility(
      visible = !advancedVisible,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut(),
    ) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EditTagDS.colorPresetSpacing),
        verticalArrangement = Arrangement.spacedBy(EditTagDS.colorPresetSpacing),
      ) {
        TAG_COLOR_PRESETS.fastForEach { preset ->
          PresetSwatch(
            color = preset,
            selected = preset == color,
            onClick = { onColorChange(preset) },
          )
        }
      }
    }

    AnimatedVisibility(
      visible = advancedVisible,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(fraction = EditTagDS.COLOR_PICKER_HORIZ_WEIGHT),
        horizontalArrangement = Arrangement.spacedBy(EditTagDS.colorPickerSectionSpacing),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HsvColorPicker(
          modifier = Modifier.weight(1f).height(EditTagDS.colorWheelHeight),
          controller = controller,
          initialColor = color ?: TAG_COLOR_PRESETS.first(),
          onColorChanged = { envelope -> if (envelope.fromUser) onColorChange(envelope.color) },
        )

        BrightnessSlider(
          modifier =
            Modifier.rotateVertically()
              .width(EditTagDS.colorWheelHeight)
              .height(EditTagDS.brightnessSliderHeight)
              .clip(CardShape)
              .border(1.dp, colors.pageText, CardShape),
          controller = controller,
        )
      }
    }
  }
}

@Composable
private fun ColorPickerTextField(
  hexState: TextFieldState,
  hexInteraction: MutableInteractionSource,
  color: Color?,
  isError: Boolean,
  modifier: Modifier = Modifier,
) {
  val fieldColors =
    if (color != null) {
      val contrasting = color.contrastingTextColor()
      colors.textField(
        focusedContainer = color,
        text = contrasting,
        textPlaceholder = contrasting.copy(alpha = EditTagDS.COLOR_FIELD_PLACEHOLDER_ALPHA),
        icon = contrasting,
      )
    } else {
      colors.textField()
    }

  AktualTextField(
    modifier = modifier,
    state = hexState,
    interactionSource = hexInteraction,
    singleLine = true,
    placeholderText = Strings.tagsCreateColorPlaceholder,
    colors = fieldColors,
    textStyle = LocalTextStyle.current.merge(textAlign = TextAlign.Center),
    supportingText =
      if (isError) {
        {
          Text(
            text = Strings.tagsCreateColorInvalid,
            color = colors.errorText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      } else {
        null
      },
  )
}

@Composable
private fun PresetSwatch(
  color: Color,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val borderColor = if (selected) colors.pageText else colors.pillBorderDark
  val borderWidth =
    if (selected) EditTagDS.colorPresetBorderSelected else EditTagDS.colorPresetBorder
  Box(
    modifier =
      modifier
        .size(EditTagDS.colorPresetSize)
        .clip(ButtonShape)
        .background(color)
        .border(borderWidth, borderColor, ButtonShape)
        .clickable(onClick = onClick)
  )
}

// the default tag colour set, copied verbatim from upstream Actual's ColorPicker component
internal val TAG_COLOR_PRESETS =
  persistentListOf(
    Color(0xFF690CB0),
    Color(0xFFD32F2F),
    Color(0xFFC2185B),
    Color(0xFF7B1FA2),
    Color(0xFF512DA8),
    Color(0xFF303F9F),
    Color(0xFF1976D2),
    Color(0xFF0288D1),
    Color(0xFF0097A7),
    Color(0xFF00796B),
    Color(0xFF388E3C),
    Color(0xFF689F38),
    Color(0xFFAFB42B),
    Color(0xFFFBC02D),
    Color(0xFFFFA000),
    Color(0xFFF57C00),
    Color(0xFFE64A19),
    Color(0xFF5D4037),
    Color(0xFF616161),
    Color(0xFF455A64),
    Color(0xFFFF6666),
    Color(0xFFFF99FF),
    Color(0xFFC39DDF),
    Color(0xFF6666FF),
    Color(0xFFB2FFFF),
    Color(0xFF99CB99),
    Color(0xFFFFFF7F),
    Color(0xFFFFAB66),
    Color(0xFFD4B89C),
    Color(0xFFBFBFBF),
    Color(0xFFFFAEAE),
    Color(0xFFFFCCFF),
    Color(0xFFE4D4FF),
    Color(0xFFB0B0FF),
    Color(0xFFD8FFFF),
    Color(0xFFCFE5CF),
    Color(0xFFFFFFB2),
    Color(0xFFFFD5B3),
    Color(0xFFE4D3C3),
    Color(0xFFDADADA),
  )

// Rotates a composable 90° and swaps its measured bounds so it occupies the rotated footprint.
// Modifiers chained after this describe the composable in its original (horizontal) orientation.
private fun Modifier.rotateVertically(): Modifier =
  rotate(degrees = -90f).layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.height, placeable.width) {
      placeable.place(
        x = -(placeable.width / 2 - placeable.height / 2),
        y = -(placeable.height / 2 - placeable.width / 2),
      )
    }
  }

@Preview
@Composable
private fun PreviewTagColorPicker(
  @PreviewParameter(ColoredBooleanParameters::class) params: ColoredParams<Boolean>
) =
  PreviewWithColoredParams(params) {
    var color by remember { mutableStateOf<Color?>(TAG_COLOR_PRESETS.random()) }
    TagColorPicker(
      modifier = Modifier.padding(16.dp),
      color = color,
      onColorChange = { color = it },
      onErrorChange = {},
      showAdvanced = this,
    )
  }
