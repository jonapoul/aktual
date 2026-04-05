package aktual.core.theme

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable

@Immutable
interface CustomTheme : Theme {
  val name: String
  val repo: CustomThemeRepo

  override val id: ThemeId
    get() = repo.toId()
}
