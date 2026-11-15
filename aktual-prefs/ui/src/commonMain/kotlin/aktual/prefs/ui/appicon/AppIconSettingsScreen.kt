package aktual.prefs.ui.appicon

import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.AppIcon
import aktual.prefs.components.AppIconImage
import aktual.prefs.components.AppIconShape
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.appicon.AppIconSettingsViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Suppress("ViewModelForwarding", "ComposeViewModelForwarding")
@Composable
fun AppIconSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: AppIconSettingsViewModel = metroViewModel<AppIconSettingsViewModel>(),
) {
  val selected by viewModel.selected.collectAsStateWithLifecycle()
  AppIconSettingsScaffold(
    selected = selected,
    highlight = highlight,
    onBack = { back() },
    onSelect = viewModel::select,
  )
}

@Composable
private fun AppIconSettingsScaffold(
  selected: AppIcon,
  onBack: () -> Unit,
  onSelect: (AppIcon) -> Unit,
  highlight: SettingsHighlight? = null,
) {
  SettingsListScaffold(
    title = Strings.settingsAppIcon,
    description = Strings.settingsAppIconDesc,
    highlight = highlight,
    onBack = onBack,
  ) {
    item {
      PreferenceGroup(title = Strings.settingsAppIconPreview) {
        // The same icon under the masks different launchers apply
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = CenterVertically,
        ) {
          LauncherShapes.forEach { shape ->
            AppIconImage(
              icon = selected,
              modifier = Modifier.size(PreviewSize).border(HairlineWidth, colors.cardBorder, shape),
              shape = shape,
            )
          }
        }
      }
    }

    item {
      PreferenceGroup(title = Strings.settingsAppIconChoose) {
        FlowRow(
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalArrangement = Arrangement.spacedBy(12.dp),
          maxItemsInEachRow = OPTIONS_PER_ROW,
        ) {
          AppIcon.entries.forEach { icon ->
            IconOption(icon = icon, isSelected = icon == selected, onClick = { onSelect(icon) })
          }
        }
      }
    }
  }
}

@Composable
private fun IconOption(icon: AppIcon, isSelected: Boolean, onClick: () -> Unit) {
  Column(
    modifier =
      Modifier.width(OptionWidth)
        .clip(CardShape)
        .selectable(selected = isSelected, role = RadioButton, onClick = onClick)
        .padding(vertical = 6.dp),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    val ringColor = if (isSelected) colors.settingsNavItemAccentSelected else Color.Transparent
    AppIconImage(
      icon = icon,
      modifier =
        Modifier.border(width = 3.dp, color = ringColor, shape = AppIconShape)
          .padding(5.dp)
          .size(OptionIconSize)
          .border(HairlineWidth, colors.cardBorder, AppIconShape),
    )
    Text(
      text = icon.label(),
      style = typography.bodyMedium,
      textAlign = Center,
      color = if (isSelected) colors.pageText else colors.pageTextSubdued,
    )
  }
}

@Composable
private fun AppIcon.label(): String =
  when (this) {
    Default -> Strings.settingsAppIconDefault
    Light -> Strings.settingsAppIconLight
    Dark -> Strings.settingsAppIconDark
    Violet -> Strings.settingsAppIconViolet
    Sunset -> Strings.settingsAppIconSunset
    Aurora -> Strings.settingsAppIconAurora
    Ink -> Strings.settingsAppIconInk
  }

private val LauncherShapes: ImmutableList<Shape> =
  persistentListOf(
    CircleShape,
    AppIconShape,
    RoundedCornerShape(percent = 12),
    RoundedCornerShape(
      topStartPercent = 50,
      topEndPercent = 50,
      bottomEndPercent = 12,
      bottomStartPercent = 50,
    ),
  )

private val PreviewSize = 64.dp
private val OptionIconSize = 56.dp
private val OptionWidth = 84.dp
private val HairlineWidth = 1.dp

// Seven icons sit as four and three rather than leaving one alone on a phone-width row
private const val OPTIONS_PER_ROW = 4

@PortraitPreview
@Composable
private fun PreviewAppIconSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    AppIconSettingsScaffold(selected = Dark, onBack = {}, onSelect = {})
  }
