package aktual.budget.tags.ui.search

import aktual.budget.tags.ui.list.ListTagsDS
import aktual.budget.tags.ui.list.TagChip
import aktual.budget.tags.ui.list.TagsPreview
import aktual.budget.tags.vm.list.TagItem
import aktual.budget.tags.vm.search.SearchTagsState
import aktual.budget.tags.vm.search.SearchTagsState.Results
import aktual.budget.tags.vm.search.SearchTagsViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.EditTagNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens.Large
import aktual.core.ui.Dimens.Medium
import aktual.core.ui.Dimens.VeryLarge
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.ScrollToTopOnNewQuery
import aktual.core.ui.SearchMessage
import aktual.core.ui.SearchScaffold
import aktual.core.ui.rememberHighlighted
import aktual.core.ui.scrollbar
import aktual.core.ui.searchResultCard
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SearchTagsScreen(
  back: BackNavigator,
  toEdit: EditTagNavigator,
  viewModel: SearchTagsViewModel = metroViewModel(),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  SearchTagsScaffold(
    query = query,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is SetQuery -> viewModel.setQuery(action.query)
        is OpenTag -> toEdit(action.id)
      }
    },
  )
}

@Composable
private fun SearchTagsScaffold(
  query: String,
  state: SearchTagsState,
  onAction: SearchTagsActionHandler,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  ScrollToTopOnNewQuery(listState, (state as? Results)?.query)

  SearchScaffold(
    modifier = modifier,
    initialQuery = query,
    placeholder = Strings.tagsSearchPlaceholder,
    listState = listState,
    onQueryChange = { onAction(SetQuery(it)) },
    onBack = { onAction(NavBack) },
  ) { innerPadding ->
    AnimatedContent(
      modifier = Modifier.padding(innerPadding),
      targetState = state,
      contentKey = { it::class },
      transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { animatedState ->
      when (animatedState) {
        NoQuery -> SearchMessage(text = Strings.tagsSearchPrompt)

        NoResults ->
          SearchMessage(
            icon = MaterialIcons.SearchOff,
            text = Strings.tagsSearchNoResults(query.trim()),
          )

        is Results ->
          ResultsList(results = animatedState, listState = listState, onAction = onAction)
      }
    }
  }
}

@Composable
private fun ResultsList(
  results: Results,
  listState: LazyListState,
  onAction: SearchTagsActionHandler,
  modifier: Modifier = Modifier,
) =
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = PaddingValues(horizontal = Large, vertical = Medium),
    verticalArrangement = Arrangement.spacedBy(Large),
  ) {
    items(results.tags, key = { it.id.value }) { tag ->
      ResultItem(
        modifier = Modifier.animateItem(),
        tag = tag,
        query = results.query,
        onClick = { onAction(OpenTag(tag.id)) },
      )
    }

    item(key = "bottom") { BottomSpacing() }
  }

@Composable
private fun ResultItem(
  tag: TagItem,
  query: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier =
      modifier.searchResultCard(
        colors = colors,
        onClick = onClick,
        padding = PaddingValues(horizontal = VeryLarge, vertical = Large),
      ),
    horizontalArrangement = Arrangement.spacedBy(VeryLarge),
    verticalAlignment = CenterVertically,
  ) {
    val alpha = if (tag.hidden) ListTagsDS.HIDDEN_ALPHA else 1f
    val noDescription = tag.description.isEmpty()

    TagChip(
      modifier = Modifier.widthIn(max = 160.dp).alpha(alpha),
      text = tag.tag,
      color = tag.color,
      query = query,
    )

    Text(
      modifier = Modifier.weight(1f).alpha(alpha),
      text =
        rememberHighlighted(
          text = tag.description.ifEmpty { Strings.tagsNoDescription },
          query = if (noDescription) "" else query,
        ),
      style = typography.bodySmall,
      color = if (noDescription) colors.tableTextLight else colors.tableText,
      fontStyle = if (noDescription) Italic else Normal,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }

@PortraitPreview
@Composable
private fun PreviewSearchTagsScaffold(
  @PreviewParameter(SearchTagsScaffoldProvider::class) params: ColoredParams<SearchTagsParams>,
) =
  PreviewWithColoredParams(params) {
    SearchTagsScaffold(query = query, state = state, onAction = {})
  }

private data class SearchTagsParams(val query: String, val state: SearchTagsState)

private class SearchTagsScaffoldProvider :
  ColoredParameterProvider<SearchTagsParams>(
    SearchTagsParams(query = "", state = NoQuery),
    SearchTagsParams(query = "xyz", state = NoResults),
    SearchTagsParams(
      query = "r",
      state =
        Results(
          query = "r",
          tags = persistentListOf(TagsPreview.groceries, TagsPreview.rent, TagsPreview.archived),
        ),
    ),
  )
