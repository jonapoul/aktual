/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.reports.ui.charts

import aktual.budget.reports.vm.TextData
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

    ```
    import androidx.compose.foundation.background
    import androidx.compose.foundation.gestures.scrollable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.text.BasicTextField
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.MaterialTheme
    import androidx.compose.material.Surface
    import androidx.compose.material.Text
    import androidx.compose.runtime.*
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.SolidColor
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.tooling.preview.Preview

    @Composable
    fun ScrollableBasicTextField() {
        var text by remember { mutableStateOf("") }
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .height(150.dp) // fixed height container
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
                .verticalScroll(scrollState) // enables vertical scrolling
                .padding(8.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = TextStyle.Default.copy(color = MaterialTheme.colors.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colors.primary),
                maxLines = Int.MAX_VALUE, // allow unlimited lines
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewScrollableBasicTextField() {
        MaterialTheme {
            Surface {
                ScrollableBasicTextField()
            }
        }
    }
    ```

    ## Explanation:

    - Box sets fixed height & fills width.
    - verticalScroll(scrollState) makes the whole Box scrollable vertically.
    - BasicTextField inside takes all available width and unlimited lines (maxLines = Int.MAX_VALUE).
    - When the text is long enough to overflow the height, vertical scroll kicks in.
    - You can add a placeholder, decorationBox, or other modifiers if you want.

    ## Bonus: Scroll to cursor as you type (advanced)

    If you want the text field to auto-scroll as the user types beyond the visible area, that requires tracking cursor position and syncing scroll. It’s a bit more complex but doable. Want me to prepare that snippet?

    Does this solve your scrollable BasicTextField need? Want me to add placeholder support or styling next?
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
