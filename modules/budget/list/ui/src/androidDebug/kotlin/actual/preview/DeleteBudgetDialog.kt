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
package actual.preview

import actual.budget.list.ui.Content
import actual.budget.list.vm.DeletingState
import actual.core.ui.PreviewParameters
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun PreviewContent(
  @PreviewParameter(ContentStateParameters::class) params: Params,
) = PreviewThemedColumn {
  Content(
    deletingState = params.state,
    localFileExists = params.localFileExists,
    onDeleteLocal = {},
    onDeleteRemote = {},
  )
}

private class Params(val state: DeletingState, val localFileExists: Boolean)

private class ContentStateParameters : PreviewParameters<Params>(
  Params(state = DeletingState.Inactive, localFileExists = true),
  Params(state = DeletingState.Active(deletingLocal = true), localFileExists = true),
  Params(state = DeletingState.Active(deletingRemote = true), localFileExists = true),
  Params(state = DeletingState.Active(deletingRemote = true), localFileExists = false),
)
