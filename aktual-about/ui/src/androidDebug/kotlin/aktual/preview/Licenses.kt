/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
