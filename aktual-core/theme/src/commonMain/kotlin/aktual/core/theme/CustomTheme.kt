package aktual.core.theme

import androidx.compose.runtime.Immutable

@Immutable
interface CustomTheme : Theme {
  val name: String
  val repo: CustomThemeRepo

  override val id: Theme.Id
    get() = repo.toId()
}
