package aktual.budget.apikeys.ui

import aktual.api.model.apikeys.ApiKeyId
import aktual.budget.apikeys.vm.ApiKeyItem
import aktual.budget.apikeys.vm.ApiKeysState
import aktual.budget.apikeys.vm.ApiKeysViewModel
import aktual.budget.apikeys.vm.Empty
import aktual.budget.apikeys.vm.Failure
import aktual.budget.apikeys.vm.Loaded
import aktual.budget.apikeys.vm.Loading
import aktual.budget.apikeys.vm.Unsupported
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Refresh
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardRow
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.formatted
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.ShimmerBounds.Window
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

@Composable
internal fun ApiKeysScreen(
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: ApiKeysViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialog by viewModel.dialog.collectAsStateWithLifecycle()

  ApiKeysScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        Retry -> viewModel.reload()
        NewKey -> viewModel.add()
        is Revoke -> viewModel.revoke(action.key)
      }
    },
  )

  ApiKeysDialogs(
    dialog = dialog,
    onCreate = viewModel::create,
    onConfirmRevoke = viewModel::confirmRevoke,
    onDismiss = viewModel::dismissDialog,
  )
}

@Composable
private fun ApiKeysScaffold(
  state: ApiKeysState,
  onAction: OnAction<ApiKeysAction>,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(NavBack) }) },
        title = { Text(text = Strings.apiKeysTitle) },
        actions = {
          if (state is Loaded || state is Empty) {
            BareIconButton(
              imageVector = AktualIcons.Add,
              contentDescription = Strings.apiKeysAdd,
              onClick = { onAction(NewKey) },
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      ApiKeysContent(
        modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
        state = state,
        scrollState = scrollState,
        contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun ApiKeysContent(
  state: ApiKeysState,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  onAction: OnAction<ApiKeysAction>,
  modifier: Modifier = Modifier,
) {
  when (state) {
    Unsupported -> {
      FailureScreen(
        modifier = modifier.padding(contentPadding),
        title = Strings.apiKeysUnsupported,
        reason = Strings.apiKeysUnsupportedMessage,
        icon = null,
        action = null,
      )
    }

    is Failure -> {
      FailureScreen(
        modifier = modifier.padding(contentPadding),
        title = Strings.apiKeysFailure,
        reason = state.cause ?: Strings.apiKeysFailureDefault,
        action =
          FailureAction(
            text = { Strings.syncRetry },
            icon = AktualIcons.Refresh,
            onClick = { onAction(Retry) },
          ),
      )
    }

    Empty -> {
      FailureScreen(
        modifier = modifier.padding(contentPadding),
        title = Strings.apiKeysEmpty,
        reason = Strings.apiKeysMessage,
        icon = null,
        action =
          FailureAction(
            text = { Strings.apiKeysAdd },
            icon = AktualIcons.Add,
            onClick = { onAction(NewKey) },
          ),
      )
    }

    Loading,
    is Loaded -> {
      Column(
        modifier =
          modifier
            .fillMaxSize()
            .verticalScrollWithBar(scrollState)
            .padding(contentPadding)
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
      ) {
        Text(
          text = Strings.apiKeysMessage,
          style = typography.bodyMedium,
          color = colors.pageTextSubdued,
        )

        VerticalSpacer(4.dp)

        if (state is Loaded) {
          KeysList(keys = state.keys, onAction = onAction)
        } else {
          repeat(times = 3) { ShimmerKeyItem() }
        }

        BottomSpacing()
      }
    }
  }
}

@Composable
private fun KeysList(keys: ImmutableList<ApiKeyItem>, onAction: OnAction<ApiKeysAction>) {
  for (key in keys) {
    KeyItem(key = key, onAction = onAction)
  }
}

@Composable
private fun KeyItem(
  key: ApiKeyItem,
  onAction: OnAction<ApiKeysAction>,
  modifier: Modifier = Modifier,
) {
  CardRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = key.name,
        style = typography.bodyLarge,
        color = colors.tableText,
        maxLines = 2,
        overflow = Ellipsis,
      )
      Text(
        text = Strings.apiKeysCreated(key.created.formatted()),
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }

    NormalTextButton(text = Strings.apiKeysRevoke, onClick = { onAction(Revoke(key)) })
  }
}

@Composable
private fun ShimmerKeyItem(modifier: Modifier = Modifier) {
  CardRow(modifier = modifier.fillMaxWidth().shimmer(rememberShimmer(Window))) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
      Box(
        Modifier.fillMaxWidth(fraction = 0.5f)
          .height(18.dp)
          .background(colors.pageTextSubdued, CardShape),
      )
      Box(
        Modifier.fillMaxWidth(fraction = 0.3f)
          .height(12.dp)
          .background(colors.pageTextSubdued, CardShape),
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewApiKeysScaffold(
  @PreviewParameter(ApiKeysStateProvider::class) params: ColorParams<ApiKeysState>,
) = PreviewWithColorParams(params) { ApiKeysScaffold(state = this, onAction = {}) }

private val PREVIEW_IMPORTER =
  ApiKeyItem(ApiKeyId("1"), "Transaction importer", LocalDate(2026, 11, 2))

private val PREVIEW_HOME_ASSISTANT =
  ApiKeyItem(ApiKeyId("2"), "Home Assistant", LocalDate(2026, 10, 14))

private class ApiKeysStateProvider :
  ColorParamsProvider<ApiKeysState>(
    Loaded(persistentListOf(PREVIEW_IMPORTER, PREVIEW_HOME_ASSISTANT)),
    Loading,
    Empty,
    Unsupported,
    Failure("unauthorized"),
  )
