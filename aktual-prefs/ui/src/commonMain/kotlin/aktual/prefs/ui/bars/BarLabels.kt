package aktual.prefs.ui.bars

import aktual.budget.model.BarEffect
import aktual.budget.model.BarGlassStyle
import aktual.core.icons.material.BlurOn
import aktual.core.icons.material.CropSquare
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Opacity
import aktual.core.icons.material.Square
import aktual.core.icons.material.Window
import aktual.core.l10n.Strings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
internal fun BarEffect.label(): String =
  when (this) {
    None -> Strings.settingsUiBarEffectNone
    Blur -> Strings.settingsUiBarEffectBlur
    Glass -> Strings.settingsUiBarEffectGlass
  }

@Composable
internal fun BarGlassStyle.label(): String =
  when (this) {
    Regular -> Strings.settingsUiGlassStyleRegular
    Clear -> Strings.settingsUiGlassStyleClear
  }

internal val BarEffect.icon: ImageVector
  get() =
    when (this) {
      None -> MaterialIcons.Square
      Blur -> MaterialIcons.BlurOn
      Glass -> MaterialIcons.Window
    }

internal val BarGlassStyle.icon: ImageVector
  get() =
    when (this) {
      Regular -> MaterialIcons.Opacity
      Clear -> MaterialIcons.CropSquare
    }

@Composable
@NonRestartableComposable
internal fun OptionIcon(imageVector: ImageVector) =
  Icon(imageVector = imageVector, contentDescription = null)
