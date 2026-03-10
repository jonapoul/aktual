package aktual.settings.ui.theme

import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.theme.CustomThemeRepo
import aktual.core.theme.CustomThemeSummary
import aktual.core.theme.DarkTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.TabletPreview
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.disabledIf
import aktual.core.ui.isMobile
import aktual.core.ui.radioButton
import aktual.settings.ui.BasicPreferenceItem
import aktual.settings.vm.theme.CatalogItem
import aktual.settings.vm.theme.CatalogState
import aktual.settings.vm.theme.CustomThemeState
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun CustomThemesPreference(
  selectedTheme: Theme.Id?,
  enabled: Boolean,
  state: CatalogState,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsThemeCustomThemes,
    subtitle = null,
    icon = null,
    enabled = enabled,
    onClick = null,
    bottomContent = {
      when (state) {
        CatalogState.Loading -> CatalogLoading()
        is CatalogState.Failed -> CatalogFailure(state.reason, onAction)
        is CatalogState.Success -> CatalogLoaded(state.themes, selectedTheme, enabled, onAction)
      }
    },
  )
}

@Composable
private fun CatalogFailure(
  reason: String,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    Text(
      text = Strings.settingsThemeFailed(reason),
      color = theme.warningText,
      fontSize = 16.sp,
      textAlign = TextAlign.Center,
    )

    val retryText = Strings.settingsThemeRetry
    PrimaryTextButton(
      prefix = { Icon(imageVector = MaterialIcons.Refresh, contentDescription = retryText) },
      text = retryText,
      onClick = { onAction(ThemeSettingsAction.RetryFetchCatalog) },
    )
  }
}

@Composable
private fun CatalogLoading(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) {
  val shimmer = rememberShimmer(ShimmerBounds.Custom)

  Column(
    modifier =
      modifier
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .fillMaxWidth()
        .clip(CardShape)
        .shimmer(shimmer)
        .onGloballyPositioned { layoutCoordinates ->
          val position = layoutCoordinates.unclippedBoundsInWindow()
          shimmer.updateBounds(position)
        },
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    val shimmerModifier =
      Modifier.fillMaxWidth()
        .height(LocalMinimumInteractiveComponentSize.current)
        .background(theme.tableText, CardShape)

    Box(modifier = shimmerModifier)
    Box(modifier = shimmerModifier)
    Box(modifier = shimmerModifier)
  }
}

@Composable
private fun CatalogLoaded(
  themes: ImmutableList<CatalogItem>,
  selectedTheme: Theme.Id?,
  enabled: Boolean,
  onAction: (ThemeSettingsAction) -> Unit,
) {
  Column(
    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    themes.fastForEach { item ->
      CatalogLoadedItem(
        item = item,
        selectedId = selectedTheme,
        enabled = enabled,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun CatalogLoadedItem(
  item: CatalogItem,
  selectedId: Theme.Id?,
  enabled: Boolean,
  onAction: (ThemeSettingsAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = Modifier.height(IntrinsicSize.Min),
    horizontalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    val isSelected = selectedId == item.id
    val backgroundColor =
      remember(theme, isSelected) {
        theme.buttonNormalBackground.disabledIf(enabled || !isSelected)
      }

    if (!isMobile()) {
      CatalogLoadedItemTablet(
        item = item,
        enabled = enabled,
        isSelected = isSelected,
        backgroundColor = backgroundColor,
        onAction = onAction,
      )
    } else {
      CatalogLoadedItemMobile(
        item = item,
        enabled = enabled,
        isSelected = isSelected,
        backgroundColor = backgroundColor,
        onAction = onAction,
      )
    }

    NormalIconButton(
      modifier = Modifier.clip(CardShape).fillMaxHeight(),
      imageVector = MaterialIcons.ArrowRight,
      enabled = enabled,
      onClick = { onAction(ThemeSettingsAction.InspectTheme(item.id)) },
      contentDescription = Strings.settingsThemePreview(item.summary.name),
    )
  }
}

@Composable
private fun RowScope.CatalogLoadedItemTablet(
  item: CatalogItem,
  enabled: Boolean,
  isSelected: Boolean,
  backgroundColor: Color,
  onAction: (ThemeSettingsAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier =
      Modifier.fillMaxHeight()
        .weight(1f)
        .background(backgroundColor, CardShape)
        .clickable(enabled, onClick = { onAction(ThemeSettingsAction.SelectTheme(item.id)) }),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(
      modifier = Modifier.padding(8.dp),
      enabled = enabled,
      selected = isSelected,
      onClick = null,
      colors = theme.radioButton(),
    )

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = item.summary.name,
        style = AktualTypography.bodyLarge,
        color = theme.buttonNormalText.disabledIf(!enabled),
      )

      Text(
        text = item.summary.repo.toString(),
        style = AktualTypography.labelMedium,
        color = theme.pageTextSubdued.disabledIf(!enabled),
      )
    }

    Column(
      modifier = Modifier.wrapContentWidth().padding(4.dp),
      verticalArrangement = Arrangement.spacedBy(PREVIEW_SPACING),
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(PREVIEW_SPACING)) {
        BoxPreviewColor(summary = item.summary, index = 0)
        BoxPreviewColor(summary = item.summary, index = 1)
        BoxPreviewColor(summary = item.summary, index = 2)
      }
      Row(horizontalArrangement = Arrangement.spacedBy(PREVIEW_SPACING)) {
        BoxPreviewColor(summary = item.summary, index = 3)
        BoxPreviewColor(summary = item.summary, index = 4)
        BoxPreviewColor(summary = item.summary, index = 5)
      }
    }
  }
}

@Composable
private fun RowScope.CatalogLoadedItemMobile(
  item: CatalogItem,
  enabled: Boolean,
  isSelected: Boolean,
  backgroundColor: Color,
  onAction: (ThemeSettingsAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier =
      Modifier.fillMaxHeight()
        .weight(1f)
        .background(backgroundColor, CardShape)
        .clickable(enabled, onClick = { onAction(ThemeSettingsAction.SelectTheme(item.id)) }),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(
      modifier = Modifier.padding(8.dp),
      enabled = enabled,
      selected = isSelected,
      onClick = null,
      colors = theme.radioButton(),
    )

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = item.summary.name,
        style = AktualTypography.bodyLarge,
        color = theme.buttonNormalText.disabledIf(!enabled),
      )

      Text(
        text = item.summary.repo.toString(),
        style = AktualTypography.labelMedium,
        color = theme.pageTextSubdued.disabledIf(!enabled),
      )

      VerticalSpacer(4.dp)

      Row(horizontalArrangement = Arrangement.spacedBy(PREVIEW_SPACING)) {
        FlatPreviewColor(summary = item.summary, index = 0)
        FlatPreviewColor(summary = item.summary, index = 1)
        FlatPreviewColor(summary = item.summary, index = 2)
        FlatPreviewColor(summary = item.summary, index = 3)
        FlatPreviewColor(summary = item.summary, index = 4)
        FlatPreviewColor(summary = item.summary, index = 5)
      }
    }
  }
}

private val PREVIEW_SPACING = 2.dp

@Composable
private fun RowScope.FlatPreviewColor(
  summary: CustomThemeSummary,
  index: Int,
  modifier: Modifier = Modifier,
) = Box(modifier = modifier.height(5.dp).background(summary.colors[index]).weight(1f))

@Composable
private fun BoxPreviewColor(
  summary: CustomThemeSummary,
  index: Int,
  modifier: Modifier = Modifier,
) = Box(modifier = modifier.size(15.dp).background(summary.colors[index]))

@Preview
@Composable
private fun PreviewCustomThemesPreference(
  @PreviewParameter(CustomThemesScaffoldProvider::class) params: ThemedParams<CatalogState>
) =
  PreviewWithColorScheme(params.theme) {
    CustomThemesPreference(state = params.data, selectedTheme = null, enabled = true, onAction = {})
  }

private class CustomThemesScaffoldProvider :
  ThemedParameterProvider<CatalogState>(
    CatalogState.Loading,
    CatalogState.Failed("Failed for some reason"),
    CatalogState.Success(
      themes =
        persistentListOf(
          PREVIEW_CATALOG_ITEM.copy(id = Theme.Id("a")),
          PREVIEW_CATALOG_ITEM.copy(id = Theme.Id("b")),
          PREVIEW_CATALOG_ITEM.copy(id = Theme.Id("c")),
        )
    ),
  )

@Preview
@Composable
private fun PreviewCustomThemeListItem(
  @PreviewParameter(CustomThemeListItemProvider::class) params: ThemedParams<CustomThemeState>
) =
  PreviewWithColorScheme(params.theme) {
    CatalogLoadedItem(
      item = PREVIEW_CATALOG_ITEM.copy(state = params.data),
      selectedId = null,
      enabled = true,
      onAction = {},
    )
  }

private class CustomThemeListItemProvider :
  ThemedParameterProvider<CustomThemeState>(
    CustomThemeState.Fetching,
    CustomThemeState.Cached,
    CustomThemeState.Failed("Something broke"),
  )

internal val PREVIEW_CATALOG_ITEM =
  CatalogItem(
    id = Theme.Id("username/repo_name"),
    summary =
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
      ),
    isSelected = false,
    state = CustomThemeState.Cached,
  )

@TabletPreview
@Composable
private fun PreviewCustomThemeListItemTablet() =
  PreviewWithColorScheme(DarkTheme) {
    CatalogLoadedItem(item = PREVIEW_CATALOG_ITEM, selectedId = null, enabled = true, onAction = {})
  }
