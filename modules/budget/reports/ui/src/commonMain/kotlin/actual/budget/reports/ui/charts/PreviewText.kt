package actual.budget.reports.ui.charts

import actual.budget.reports.vm.TextData
import org.intellij.lang.annotations.Language

internal object PreviewText {
  @Language("Markdown")
  val MARKDOWN_1 = """
    # Title
    ## Subtitle
    ### Sub-subtitle
    Text goes here
    - Bullet 1
    - Bullet 2

    More text goes below - *lorem ipsum* blah blah who cares just trying to **split over multiple lines like this**.

    ![Image](https://dummyimage.com/600x400/000/fff)

    Here's a [link to Google](https://www.google.com)
  """.trimIndent()

  @Language("Markdown")
  val MARKDOWN_SHORT = """
    - Bullet 1
    - Bullet 2

    More text goes below - *lorem ipsum* blah blah who cares just trying to **split over multiple lines like this**. Here's a [link to Google](https://www.google.com)
  """.trimIndent()

  val DATA = TextData(
    content = MARKDOWN_1,
  )

  val SHORT_DATA = TextData(
    content = MARKDOWN_SHORT,
  )
}
