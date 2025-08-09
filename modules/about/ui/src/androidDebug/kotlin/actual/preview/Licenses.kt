package actual.preview

import actual.about.ui.licenses.ArtifactItem
import actual.about.ui.licenses.LicensesScaffold
import actual.about.vm.LicensesState
import actual.about.vm.SearchBarState
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
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
