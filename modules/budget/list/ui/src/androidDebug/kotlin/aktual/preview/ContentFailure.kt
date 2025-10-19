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
package aktual.preview

import aktual.budget.list.ui.ContentFailure
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Failure() = PreviewThemedScreen {
  ContentFailure(
    reason = "Failed to do the thing, here's a bit more text to show how it behaves when wrapping",
    onClickRetry = {},
  )
}

@TripleScreenPreview
@Composable
private fun NoReason() = PreviewThemedScreen {
  ContentFailure(
    reason = null,
    onClickRetry = {},
  )
}
