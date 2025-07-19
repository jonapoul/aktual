package actual.budget.reports.ui.charts

import actual.budget.reports.ui.Action
import actual.budget.reports.ui.ActionListener
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.TextData
import actual.core.ui.BareTextButton
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.NormalTextButton
import actual.core.ui.PreviewColumn
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.colintheshots.twain.MarkdownEditor
import com.colintheshots.twain.MarkdownText

@Composable
internal fun TextChart(
  data: TextData,
  compact: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
) {
  var isEditing by remember { mutableStateOf(false) }
  var editingContent by remember(data) { mutableStateOf(data.content) }

  Box(
    modifier = Modifier.weight(1f),
  ) {
    if (isEditing) {
      MarkdownEditor(
        modifier = Modifier.fillMaxSize(),
        value = editingContent,
        onValueChange = { editingContent = it },
      )
    } else {
      MarkdownText(
        modifier = Modifier.fillMaxSize(),
        markdown = data.content,
        color = theme.pageText,
        textAlign = TextAlign.Center,
      )
    }
  }

  // No editing in compact mode
  if (compact) return@Column

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (isEditing) {
      BareTextButton(
        modifier = Modifier.weight(1f),
        text = Strings.reportsTextDiscard,
        onClick = {
          editingContent = data.content
          isEditing = false
        },
      )

      NormalTextButton(
        modifier = Modifier.weight(1f),
        text = Strings.reportsTextSave,
        onClick = {
          onAction(Action.SaveTextContent(data, editingContent))
          isEditing = false
        },
      )
    } else {
      NormalTextButton(
        modifier = Modifier.fillMaxWidth(),
        text = Strings.reportsTextEdit,
        onClick = {
          editingContent = data.content
          isEditing = true
        },
      )
    }
  }
}

@ScreenPreview
@Composable
private fun PreviewRegular() = PreviewScreen(isPrivacyEnabled = false) {
  Surface(Modifier.background(LocalTheme.current.tableBackground)) {
    TextChart(
      modifier = Modifier
        .background(LocalTheme.current.tableBackground, CardShape)
        .padding(horizontal = 4.dp),
      data = PreviewText.DATA,
      compact = false,
      onAction = {},
    )
  }
}

@ScreenPreview
@Composable
private fun PreviewRegularPrivate() = PreviewScreen(isPrivacyEnabled = true) {
  Surface(Modifier.background(LocalTheme.current.tableBackground)) {
    TextChart(
      modifier = Modifier
        .background(LocalTheme.current.tableBackground, CardShape)
        .padding(horizontal = 4.dp),
      data = PreviewText.DATA,
      compact = false,
      onAction = {},
    )
  }
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewColumn(isPrivacyEnabled = false) {
  TextChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .height(300.dp)
      .padding(4.dp),
    data = PreviewText.DATA,
    compact = true,
    onAction = {},
  )
}
