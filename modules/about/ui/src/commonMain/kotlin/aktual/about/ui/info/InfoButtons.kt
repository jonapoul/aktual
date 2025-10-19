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
package aktual.about.ui.info

import aktual.core.ui.NormalTextButton
import aktual.l10n.Strings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun InfoButtons(
  onAction: (InfoAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val buttonModifier = Modifier.fillMaxWidth()

    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.SourceCodeButton),
      text = Strings.infoRepo,
      onClick = { onAction(InfoAction.OpenSourceCode) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.CheckUpdatesButton),
      text = Strings.infoCheckUpdates,
      onClick = { onAction(InfoAction.CheckUpdates) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.ReportButton),
      text = Strings.infoReportIssues,
      onClick = { onAction(InfoAction.ReportIssue) },
    )
    NormalTextButton(
      modifier = buttonModifier.testTag(Tags.LicensesButton),
      text = Strings.infoLicenses,
      onClick = { onAction(InfoAction.ViewLicenses) },
    )
  }
}
