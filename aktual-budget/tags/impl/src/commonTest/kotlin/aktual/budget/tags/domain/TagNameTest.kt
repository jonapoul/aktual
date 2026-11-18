package aktual.budget.tags.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class TagNameTest {
  @Test
  fun `Strips hashes`() {
    assertThat(sanitiseTagName("#food#")).isEqualTo("food")
  }

  @Test
  fun `Trims surrounding whitespace`() {
    assertThat(sanitiseTagName("  food  ")).isEqualTo("food")
  }

  @Test
  fun `Turns whitespace runs into a single dash`() {
    assertThat(sanitiseTagName("eating   out")).isEqualTo("eating-out")
    assertThat(sanitiseTagName("eating\tout")).isEqualTo("eating-out")
  }

  @Test
  fun `Merges dashes around whitespace`() {
    assertThat(sanitiseTagName("eating - out")).isEqualTo("eating-out")
    assertThat(sanitiseTagName("eating-- -out")).isEqualTo("eating-out")
  }

  @Test
  fun `Leaves dashes without whitespace alone`() {
    assertThat(sanitiseTagName("eating--out")).isEqualTo("eating--out")
  }

  @Test
  fun `Input keeps one trailing space`() {
    assertThat(sanitiseTagNameInput("eating ")).isEqualTo("eating ")
    assertThat(sanitiseTagNameInput("eating   ")).isEqualTo("eating ")
  }

  @Test
  fun `Input turns a space into a dash once more is typed`() {
    assertThat(sanitiseTagNameInput("eating o")).isEqualTo("eating-o")
  }

  @Test
  fun `Input drops leading whitespace and hashes`() {
    assertThat(sanitiseTagNameInput("  #food")).isEqualTo("food")
    assertThat(sanitiseTagNameInput(" ")).isEqualTo("")
    assertThat(sanitiseTagNameInput("#")).isEqualTo("")
  }
}
