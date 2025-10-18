/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.budget.reports.ui.charts

import actual.budget.reports.ui.Action
import actual.budget.reports.ui.ActionListener
import actual.budget.reports.vm.TextData
import actual.core.ui.BareTextButton
import actual.core.ui.LocalTheme
import actual.core.ui.NormalTextButton
import actual.core.ui.Theme
import actual.core.ui.verticalScrollWithBar
import actual.l10n.Strings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor

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
  val keyboard = LocalSoftwareKeyboardController.current

  val onSave = {
    keyboard?.hide()
    onAction(Action.SaveTextContent(data, editingContent))
    isEditing = false
  }

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier.weight(1f),
  ) {
    if (isEditing) {
      BasicTextField(
        modifier = Modifier
          .fillMaxSize()
          .padding(4.dp)
          .verticalScrollWithBar(scrollState),
        value = editingContent,
        onValueChange = { editingContent = it },
        keyboardOptions = KeyboardOptions(
          autoCorrectEnabled = true,
          capitalization = KeyboardCapitalization.Sentences,
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.None,
        ),
      )
    } else {
      Markdown(
        modifier = Modifier
          .fillMaxSize()
          .verticalScrollWithBar(scrollState),
        content = data.content,
        colors = markdownColor(theme.pageText),
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
        onClick = { onSave() },
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
