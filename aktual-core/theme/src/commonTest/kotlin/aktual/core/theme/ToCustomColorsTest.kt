package aktual.core.theme

import androidx.compose.ui.graphics.Color
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class ToCustomColorsTest {
  private val repo = CustomThemeRepo.local("abc")

  @Test
  fun `Copying keeps every attribute`() {
    for (theme in Colors.Defaults) {
      val copy = theme.toCustomColors(name = "Copy", repo = repo)
      assertThat(copy.attributes()).isEqualTo(theme.attributes())
      assertThat(copy.isLight).isEqualTo(theme.isLight)
    }
  }

  @Test
  fun `Each override changes only its own attribute`() {
    val original = DarkColors.attributes()
    val override = Color(red = 1, green = 2, blue = 3)
    for (name in original.keys) {
      val copy = DarkColors.toCustomColors("Copy", repo, overrides = mapOf(name to override))
      assertThat(copy.attributes()).isEqualTo(original + (name to override))
    }
  }

  @Test
  fun `Copying keeps name and repo`() {
    val copy = LightColors.toCustomColors(name = "My theme", repo = repo)
    assertThat(copy.name).isEqualTo("My theme")
    assertThat(copy.repo).isEqualTo(repo)
    assertThat(copy.id.value).isEqualTo("@local/abc")
  }

  @Test
  fun `Unknown overrides are rejected`() {
    assertFailure { LightColors.toCustomColors("Copy", repo, mapOf("notAColour" to Color.Red)) }
      .isInstanceOf<IllegalArgumentException>()
  }
}
