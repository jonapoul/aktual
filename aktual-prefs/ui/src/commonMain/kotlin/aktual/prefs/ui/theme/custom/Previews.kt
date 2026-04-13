package aktual.prefs.ui.theme.custom

import aktual.core.model.ThemeId
import aktual.core.theme.CustomThemeRepo
import aktual.core.theme.CustomThemeSummary
import aktual.core.theme.ThemeMode
import aktual.prefs.vm.theme.custom.CacheState
import aktual.prefs.vm.theme.custom.CatalogItem
import androidx.compose.ui.graphics.Color

internal val PREVIEW_SUMMARY =
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
    mode = ThemeMode.Light,
  )

internal val PREVIEW_CATALOG_ITEM =
  CatalogItem(
    id = ThemeId("username/repo_name"),
    summary = PREVIEW_SUMMARY,
    isSelected = false,
    state = CacheState.Cached(PREVIEW_SUMMARY),
  )
