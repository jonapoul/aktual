package aktual.prefs.ui.appicon

import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.AppIcon
import aktual.prefs.components.AppIconImage
import aktual.prefs.components.AppIconShape
import aktual.prefs.ui.PreviewOptionGroup
import aktual.prefs.ui.PreviewSettingsScaffold
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.appicon.AppIconSettingsViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.toImmutableList

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
    showShapePreview = ShouldShowShapePreview,
    highlight = highlight,
    onBack = back::invoke,
    onSelect = viewModel::select,
  )
}

@Composable
private fun AppIconSettingsScaffold(
  selected: AppIcon,
  showShapePreview: Boolean,
  onBack: () -> Unit,
  onSelect: (AppIcon) -> Unit,
  highlight: SettingsHighlight? = null,
) {
  val options: LazyListScope.() -> Unit = {
    item {
      PreviewOptionGroup(
        title = Strings.settingsAppIconChoose,
        options = AppIcons,
        selected = selected,
        onSelect = onSelect,
        label = { it.label() },
        preview = { icon ->
          AppIconImage(
            icon = icon,
            modifier =
              Modifier.size(ListIconSize).border(HairlineWidth, colors.tableBorder, AppIconShape),
          )
        },
      )
    }
  }

  if (showShapePreview) {
    PreviewSettingsScaffold(
      title = Strings.settingsAppIcon,
      description = Strings.settingsAppIconDesc,
      onBack = onBack,
      preview = { ShapePreviews(icon = selected) },
      highlight = highlight,
      content = options,
    )
  } else {
    SettingsListScaffold(
      title = Strings.settingsAppIcon,
      description = Strings.settingsAppIconDesc,
      highlight = highlight,
      onBack = onBack,
      content = options,
    )
  }
}

@Composable
private fun ShapePreviews(icon: AppIcon, modifier: Modifier = Modifier) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    LauncherMask.entries.fastForEach { mask ->
      Column(
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.VeryLarge),
      ) {
        AppIconImage(
          icon = icon,
          shape = mask.shape,
          modifier =
            Modifier.size(PreviewIconSize).border(HairlineWidth, colors.tableBorder, mask.shape),
        )
        Text(text = mask.label(), style = typography.bodySmall, color = colors.pageTextSubdued)
      }
    }
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

private val PreviewIconSize = 56.dp
private val ListIconSize = 40.dp
private val HairlineWidth = 1.dp
private val AppIcons = AppIcon.entries.toImmutableList()

@PortraitPreview
@Composable
private fun PreviewAppIconSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    AppIconSettingsScaffold(selected = Dark, showShapePreview = true, onBack = {}, onSelect = {})
  }
