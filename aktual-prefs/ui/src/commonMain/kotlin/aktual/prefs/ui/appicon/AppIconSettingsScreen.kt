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
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.appicon.AppIconSettingsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

@Suppress("ViewModelForwarding", "ComposeViewModelForwarding")
@Composable
fun AppIconSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: AppIconSettingsViewModel = metroViewModel<AppIconSettingsViewModel>(),
) {
  val selected by viewModel.selected.collectAsStateWithLifecycle()
  var mask by rememberSaveable { mutableStateOf(LauncherMask.Circle) }
  AppIconSettingsScaffold(
    selected = selected,
    mask = mask,
    highlight = highlight,
    onBack = { back() },
    onSelect = viewModel::select,
    onSelectMask = { mask = it },
  )
}

@Composable
private fun AppIconSettingsScaffold(
  selected: AppIcon,
  mask: LauncherMask,
  onBack: () -> Unit,
  onSelect: (AppIcon) -> Unit,
  onSelectMask: (LauncherMask) -> Unit,
  highlight: SettingsHighlight? = null,
) {
  SettingsListScaffold(
    title = Strings.settingsAppIcon,
    description = Strings.settingsAppIconDesc,
    highlight = highlight,
    onBack = onBack,
  ) {
    item {
      PreferenceGroup(
        title = Strings.settingsAppIconShape,
        subtitle = Strings.settingsAppIconShapeDesc,
      ) {
        MaskSelector(
          selected = mask,
          onSelect = onSelectMask,
          modifier = Modifier.padding(vertical = 8.dp),
        )
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
            IconOption(
              icon = icon,
              shape = mask.shape,
              isSelected = icon == selected,
              onClick = { onSelect(icon) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MaskSelector(
  selected: LauncherMask,
  onSelect: (LauncherMask) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(CardShape)
        .background(colors.buttonNormalBackground)
        .padding(4.dp)
        .selectableGroup(),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    LauncherMask.entries.forEach { mask ->
      val isSelected = mask == selected
      val contentColor = if (isSelected) colors.buttonPrimaryText else colors.pageTextLight
      Column(
        modifier =
          Modifier.weight(1f)
            .heightIn(min = MinTouchHeight)
            .clip(CardShape)
            .background(if (isSelected) colors.buttonPrimaryBackgroundHover else Color.Transparent)
            .border(
              width = HairlineWidth,
              color = if (isSelected) colors.buttonPrimaryBorder else Color.Transparent,
              shape = CardShape,
            )
            .selectable(selected = isSelected, role = RadioButton, onClick = { onSelect(mask) })
            .padding(4.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, CenterVertically),
      ) {
        Box(Modifier.size(SwatchSize).background(contentColor, mask.shape))
        Text(text = mask.label(), style = typography.bodySmall, color = contentColor)
      }
    }
  }
}

@Composable
private fun IconOption(icon: AppIcon, shape: Shape, isSelected: Boolean, onClick: () -> Unit) {
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
      shape = shape,
      modifier =
        Modifier.border(width = 3.dp, color = ringColor, shape = shape)
          .padding(5.dp)
          .size(OptionIconSize)
          .border(HairlineWidth, colors.tableBorder, shape),
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
private fun LauncherMask.label(): String =
  when (this) {
    Circle -> Strings.settingsAppIconShapeCircle
    Squircle -> Strings.settingsAppIconShapeSquircle
    Square -> Strings.settingsAppIconShapeSquare
    Teardrop -> Strings.settingsAppIconShapeTeardrop
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

private val OptionIconSize = 56.dp
private val OptionWidth = 84.dp
private val HairlineWidth = 1.dp
private val SwatchSize = 16.dp
private val MinTouchHeight = 52.dp

// Seven icons sit as four and three rather than leaving one alone on a phone-width row
private const val OPTIONS_PER_ROW = 4

@PortraitPreview
@Composable
private fun PreviewAppIconSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    AppIconSettingsScaffold(
      selected = Dark,
      mask = Teardrop,
      onBack = {},
      onSelect = {},
      onSelectMask = {},
    )
  }
