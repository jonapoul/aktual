package aktual.theme

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import assertk.assertions.prop
import kotlin.test.Test

class SidebarColorsTest {
  @Test
  fun `Light theme uses the redesign sidebar colours`() {
    assertThat(LightColors.sidebarColors()).all {
      prop(Colors::sidebarBackground).isEqualTo(LightColors.sidebarRedesignBackground)
      prop(Colors::sidebarItemText).isEqualTo(LightColors.sidebarRedesignItemText)
      prop(Colors::sidebarItemTextSelected).isEqualTo(LightColors.sidebarRedesignItemTextSelected)
      prop(Colors::sidebarBorder).isEqualTo(LightColors.sidebarRedesignItemBackgroundHover)
      prop(Colors::pageBackground).isEqualTo(LightColors.pageBackground)
      prop(Colors::isLight).isEqualTo(true)
    }
  }

  @Test
  fun `Other themes keep their sidebar colours`() {
    assertThat(DarkColors.sidebarColors()).isSameInstanceAs(DarkColors)
    assertThat(MidnightColors.sidebarColors()).isSameInstanceAs(MidnightColors)
  }
}
