package aktual.core.theme

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ColorAttributesTest {
  @Test
  fun `Attributes cover every colour in the interface`() {
    // Color is a value class over Long, so its getters compile to methods returning long
    val colorGetters =
      Colors::class.java.methods.count { it.returnType == Long::class.javaPrimitiveType }
    assertThat(LightColors.attributes().size).isEqualTo(colorGetters)
  }
}
