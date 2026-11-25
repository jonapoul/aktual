package aktual.prefs.ui.theme

import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronRight
import aktual.core.icons.Cloud
import aktual.core.icons.CloudWarning
import aktual.core.icons.Refresh
import aktual.core.icons.Tuning
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.OfflinePin
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualProgressIndicator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ButtonShape
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.RowShape
import aktual.core.ui.ShimmerBlock
import aktual.core.ui.ShimmerLine
import aktual.core.ui.disabled
import aktual.core.ui.disabledIf
import aktual.core.ui.radioButton
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.vm.theme.CatalogItem
import aktual.prefs.vm.theme.CatalogState
import aktual.prefs.vm.theme.CatalogStatus
import aktual.prefs.vm.theme.DownloadState
import aktual.theme.CustomThemeRepo
import aktual.theme.CustomThemeSummary
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// While matching the system, tapping a theme only downloads it so the pickers can use it
@Composable
internal fun CatalogPreference(
  catalog: CatalogState,
  selectedTheme: ThemeId?,
  isSelectable: Boolean,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) {
  PreferenceGroup(
    modifier = modifier,
    title = Strings.settingsThemeCatalog,
    subtitle = Strings.settingsThemeCatalogSubtitle,
    actions = { CatalogActions(catalog.status, onAction) },
  ) {
    if (catalog.status == Failed) {
      FailureRow(onAction)
      if (catalog.items.isNotEmpty()) CatalogDivider()
    }

    if (catalog.status == Loading && catalog.items.isEmpty()) {
      repeat(times = 4) { index ->
        if (index > 0) CatalogDivider()
        LoadingItem()
      }
    }

    catalog.items.fastForEachIndexed { index, item ->
      if (index > 0) CatalogDivider()
      CatalogThemeItem(
        item = item,
        isSelected = isSelectable && selectedTheme == item.id,
        isSelectable = isSelectable,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun CatalogActions(
  status: CatalogStatus,
  onAction: OnAction<ThemeSettingsAction>,
) = Row {
  AktualIconButton(
    imageVector = AktualIcons.Tuning,
    contentDescription = Strings.settingsThemeSortFilter,
    onClick = { onAction(ShowCatalogOptions) },
  )

  AktualIconButton(
    isLoading = status == Loading,
    imageVector = AktualIcons.Refresh,
    contentDescription = Strings.settingsThemeRefresh,
    onClick = { onAction(RefreshCatalog) },
  )
}

@Composable
private fun CatalogDivider() =
  HorizontalDivider(
    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
    color = colors.settingsCardBorder,
  )

@Composable
private fun FailureRow(onAction: OnAction<ThemeSettingsAction>) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(ItemPadding),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = CenterVertically,
  ) {
    Icon(AktualIcons.CloudWarning, null, StateIconModifier, colors.errorText)
    Text(
      modifier = Modifier.weight(1f),
      text = Strings.settingsThemeRefreshFailure,
      style = typography.bodyMedium,
      color = colors.errorText,
    )
    AktualButton(
      text = Strings.settingsThemeRefreshRetry,
      variant = Secondary,
      icon = AktualIcons.Refresh,
      onClick = { onAction(RefreshCatalog) },
    )
  }
}

// Keep in sync with CatalogThemeItem
@Composable
private fun LoadingItem() {
  val shimmer = rememberShimmer(Window)

  Row(
    modifier = Modifier.fillMaxWidth().padding(ItemPadding).shimmer(shimmer),
    verticalAlignment = CenterVertically,
  ) {
    ShimmerBlock(Modifier.padding(8.dp).size(20.dp), CircleShape)

    Column(
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      ShimmerLine(typography.bodyMedium, Modifier.fillMaxWidth(fraction = 0.55f))
      ShimmerLine(typography.bodySmall, Modifier.fillMaxWidth(fraction = 0.35f))
      Row(horizontalArrangement = Arrangement.spacedBy(PaletteSpacing)) {
        repeat(times = 6) { ShimmerBlock(Modifier.weight(1f).height(PaletteHeight)) }
      }
    }
  }
}

@Composable
private fun CatalogThemeItem(
  item: CatalogItem,
  isSelected: Boolean,
  isSelectable: Boolean,
  onAction: OnAction<ThemeSettingsAction>,
) {
  val enabled = item.download != Downloading
  val isChosen = isSelected && enabled
  val background by
    animateColorAsState(
      if (isChosen) colors.accentSubtle else Color.Transparent,
    )

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RowShape)
        .background(background, RowShape)
        .then(
          if (isSelectable) {
            Modifier.selectable(
              selected = isSelected,
              enabled = enabled,
              role = Role.RadioButton,
              onClick = { onAction(SelectCatalogTheme(item.summary)) },
            )
          } else {
            Modifier.clickable(
              enabled = enabled,
              role = Button,
              onClick = { onAction(SelectCatalogTheme(item.summary)) },
            )
          },
        )
        .padding(ItemPadding),
    verticalAlignment = CenterVertically,
  ) {
    if (isSelectable) {
      RadioButton(
        modifier = Modifier.padding(8.dp),
        enabled = enabled,
        selected = isSelected,
        onClick = null,
        colors = colors.radioButton(),
      )
    }

    Column(
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(verticalAlignment = CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.summary.name,
            style = typography.bodyLarge,
            fontWeight = if (isChosen) Medium else Normal,
            color =
              when {
                !enabled -> colors.settingsCardText.disabled
                isChosen -> colors.accentSubtleText
                else -> colors.settingsCardText
              },
            maxLines = 1,
            overflow = Ellipsis,
          )

          Text(
            text = item.summary.repo.toString(),
            style = typography.bodySmall,
            color = colors.pageTextSubdued.disabledIf(!enabled),
            maxLines = 1,
            overflow = Ellipsis,
          )
        }

        DownloadStateIcon(item, onAction)
      }

      val palette = remember(item.summary) { item.summary.colors.toImmutableList() }
      Palette(palette, enabled)
    }

    AktualIconButton(
      imageVector = AktualIcons.CheveronRight,
      isEnabled = enabled,
      onClick = { onAction(InspectCatalogTheme(item.summary)) },
      contentDescription = Strings.settingsThemePreview(item.summary.name),
    )
  }
}

@Composable
internal fun DownloadStateIcon(item: CatalogItem, onAction: OnAction<ThemeSettingsAction>) =
  DownloadStateIcon(item.summary.name, item.download, onAction)

@Composable
internal fun DownloadStateIcon(
  name: String,
  state: DownloadState,
  onAction: OnAction<ThemeSettingsAction>,
) {
  when (state) {
    Downloading -> AktualProgressIndicator(Modifier.padding(4.dp), size = StateIconSize)
    Downloaded -> Icon(MaterialIcons.OfflinePin, null, StateIconModifier, colors.pageText)
    Remote -> Icon(AktualIcons.Cloud, null, StateIconModifier, colors.pageText)
    is Failed -> {
      val failure = DownloadFailure(name, state.reason)
      Icon(
        modifier =
          Modifier.clip(ButtonShape)
            .clickable(role = Button) { onAction(ShowDownloadFailure(failure)) }
            .then(StateIconModifier),
        imageVector = AktualIcons.CloudWarning,
        contentDescription = Strings.settingsThemeDownloadFailedShow(name),
        tint = colors.errorText,
      )
    }
  }
}

@Composable
internal fun Palette(
  palette: ImmutableList<Color>,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(PaletteSpacing),
  ) {
    palette.fastForEach { color ->
      Box(
        Modifier.weight(1f).height(PaletteHeight).background(color.disabledIf(!enabled), CardShape),
      )
    }
  }
}

private val ItemPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
private val StateIconSize = 25.dp
private val StateIconModifier = Modifier.padding(4.dp).size(StateIconSize)
private val PaletteSpacing = 2.dp
private val PaletteHeight = 16.dp

internal val PreviewSummary =
  CustomThemeSummary(
    name = "My theme",
    repo = CustomThemeRepo(userName = "username", repoName = "repo_name"),
    colors =
      listOf(
        Color(0xFFff3456),
        Color(0xFF4561ff),
        Color(0xFFff2356),
        Color(0xFF45ff36),
        Color(0xFFf214f6),
        Color(0xFF2156ff),
      ),
    mode = Light,
  )

private val PreviewItems: ImmutableList<CatalogItem> =
  persistentListOf(
    CatalogItem(PreviewSummary, Downloaded),
    CatalogItem(PreviewSummary.copy(repo = CustomThemeRepo("a", "remote")), Remote),
    CatalogItem(PreviewSummary.copy(repo = CustomThemeRepo("b", "fetching")), Downloading),
    CatalogItem(
      PreviewSummary.copy(repo = CustomThemeRepo("c", "failed")),
      DownloadState.Failed("Something broke"),
    ),
  )

@Preview
@Composable
private fun PreviewCatalogPreference(
  @PreviewParameter(CatalogPreferenceProvider::class) params: ColorParams<CatalogPreferenceParams>,
) =
  PreviewWithColorParams(params) {
    CatalogPreference(
      catalog = catalog,
      selectedTheme = PreviewItems.first().id,
      isSelectable = isSelectable,
      onAction = {},
    )
  }

private data class CatalogPreferenceParams(val catalog: CatalogState, val isSelectable: Boolean)

private class CatalogPreferenceProvider :
  ColorParamsProvider<CatalogPreferenceParams>(
    CatalogPreferenceParams(CatalogState(PreviewItems, Idle), isSelectable = true),
    CatalogPreferenceParams(CatalogState(PreviewItems, Idle), isSelectable = false),
    CatalogPreferenceParams(Initial, isSelectable = true),
    CatalogPreferenceParams(
      CatalogState(PreviewItems, Failed),
      isSelectable = true,
    ),
  )
