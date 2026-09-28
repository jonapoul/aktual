package aktual.budget.reports.ui.charts

import aktual.budget.reports.vm.TextAlign
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.compose.components.CurrentComponentsBridge
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents

// Text alignment alone only moves text within its own block, and headings and list items are only
// as wide as their content. So each block is shrunk to fit and then aligned as a whole, which also
// moves list bullets
@Composable
internal fun alignedMarkdownComponents(align: TextAlign): MarkdownComponents =
  remember(align) {
    val alignment =
      when (align) {
        Center -> Alignment.CenterHorizontally
        Right -> Alignment.End
        Left,
        Unknown -> null
      }

    if (alignment == null) {
      markdownComponents()
    } else {
      fun MarkdownComponent.aligned(): MarkdownComponent = { model ->
        AlignedBlock(alignment) { this(model) }
      }

      with(CurrentComponentsBridge) {
        markdownComponents(
          heading1 = heading1.aligned(),
          heading2 = heading2.aligned(),
          heading3 = heading3.aligned(),
          heading4 = heading4.aligned(),
          heading5 = heading5.aligned(),
          heading6 = heading6.aligned(),
          setextHeading1 = setextHeading1.aligned(),
          setextHeading2 = setextHeading2.aligned(),
          blockQuote = blockQuote.aligned(),
          paragraph = paragraph.aligned(),
          orderedList = orderedList.aligned(),
          unorderedList = unorderedList.aligned(),
        )
      }
    }
  }

@Composable
private fun AlignedBlock(alignment: Alignment.Horizontal, content: @Composable () -> Unit) =
  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
    Box(modifier = Modifier.width(intrinsicSize = Max)) { content() }
  }
