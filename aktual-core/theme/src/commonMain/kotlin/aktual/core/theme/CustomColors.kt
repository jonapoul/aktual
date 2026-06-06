package aktual.core.theme

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable

@Immutable
interface CustomColors : Colors {
  val name: String
  val repo: CustomThemeRepo

  override val id: ThemeId
    get() = repo.toId()
}
