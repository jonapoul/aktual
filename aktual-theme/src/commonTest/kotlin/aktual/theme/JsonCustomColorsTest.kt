package aktual.theme

import androidx.compose.ui.graphics.Color
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class JsonCustomColorsTest {
  @Test
  fun `Themes saved without accent colours take them from older colours`() {
    val theme =
      DarkColors.toCustomColors(
        name = "Old",
        repo = CustomThemeRepo.local("old"),
        overrides = mapOf("buttonPrimaryBackground" to Blue),
      )
    val encoded = ThemeJson.encodeToJsonElement(JsonCustomColors.serializer(), theme).jsonObject
    val button = encoded.getValue("button").jsonObject
    val saved =
      JsonObject(
        encoded - "accent" + ("button" to JsonObject(button - "calendarSelectedText")),
      )

    val decoded = ThemeJson.decodeFromJsonElement(JsonCustomColors.serializer(), saved)

    assertThat(decoded).all {
      prop(JsonCustomColors::accent).isEqualTo(Blue)
      prop(JsonCustomColors::accentText).isEqualTo(DarkColors.pageTextLink)
      prop(JsonCustomColors::calendarSelectedText).isEqualTo(DarkColors.buttonPrimaryText)
    }
  }

  private companion object {
    val Blue = Color(0xFF2B8FED)
  }
}
