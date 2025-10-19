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

import aktual.about.ui.licenses.ArtifactItem
import aktual.about.ui.licenses.LicensesScaffold
import aktual.about.vm.LicensesState
import aktual.about.vm.SearchBarState
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf

@Preview
@Composable
private fun PreviewArtifactItem() = PreviewThemedColumn {
  ArtifactItem(
    artifact = AlakazamAndroidCore,
    onLaunchUrl = {},
  )
}

@TripleScreenPreview
@Composable
private fun PreviewLoading() = PreviewThemedScreen {
  LicensesScaffold(
    state = LicensesState.Loading,
    searchBarState = SearchBarState.Gone,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun PreviewNoneFound() = PreviewThemedScreen {
  LicensesScaffold(
    state = LicensesState.NoneFound,
    searchBarState = SearchBarState.Gone,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun PreviewLoaded() = PreviewThemedScreen {
  LicensesScaffold(
    state = LicensesState.Loaded(
      artifacts = persistentListOf(AlakazamAndroidCore, ComposeMaterialRipple, FragmentKtx, Slf4jApi),
    ),
    searchBarState = SearchBarState.Visible(text = "My wicked search query"),
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun PreviewError() = PreviewThemedScreen {
  LicensesScaffold(
    state = LicensesState.Error(errorMessage = "Something broke lol! Here's some more shite to show how it looks"),
    searchBarState = SearchBarState.Gone,
    onAction = {},
  )
}
