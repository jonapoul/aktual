package aktual.core.ui

import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Search
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.Dimens.Large
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter

/**
 * Full screen search layout, with a back button and search field in the top bar. Scrolling
 * [listState] hides the keyboard.
 */
@Composable
fun SearchScaffold(
  initialQuery: String,
  placeholder: String,
  listState: LazyListState,
  onQueryChange: (String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable (PaddingValues) -> Unit,
) {
  val keyboard = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current

  LaunchedEffect(listState) {
    snapshotFlow { listState.isScrollInProgress }
      .filter { it }
      .collect {
        keyboard?.hide()
        focusManager.clearFocus()
      }
  }

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = onBack) },
        title = { SearchField(initialQuery, placeholder, onQueryChange) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      content(innerPadding)
    }
  }
}

@Composable
private fun SearchField(
  initialQuery: String,
  placeholder: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val state = rememberTextFieldState(initialText = initialQuery)
  val focusRequester = remember { FocusRequester() }
  val keyboard = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current
  val currentOnQueryChange by rememberUpdatedState(onQueryChange)
  var hasFocused by rememberSaveable { mutableStateOf(false) }

  // Only on first open, not when coming back from another screen
  LaunchedEffect(Unit) {
    if (!hasFocused) {
      focusRequester.requestFocus()
      hasFocused = true
    }
  }

  LaunchedEffect(state) {
    snapshotFlow { state.text.toString() }.collect { query -> currentOnQueryChange(query) }
  }

  ProvideTextStyle(value = typography.bodyLarge) {
    AktualTextField(
      modifier = modifier.focusRequester(focusRequester).fillMaxWidth(),
      state = state,
      singleLine = true,
      placeholderText = placeholder,
      showBorder = false,
      clearable = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      onKeyboardAction = {
        keyboard?.hide()
        focusManager.clearFocus()
      },
    )
  }
}

/**
 * Scrolls [listState] back to the top when [query] changes to a new non-null value, but not when
 * coming back to the screen with the same query.
 */
@Composable
fun ScrollToTopOnNewQuery(listState: LazyListState, query: String?) {
  var lastQuery by rememberSaveable { mutableStateOf(query) }
  SideEffect(query) {
    if (query != null && query != lastQuery) {
      lastQuery = query
      listState.requestScrollToItem(0)
    }
  }
}

@Composable
fun SearchMessage(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = MaterialIcons.Search,
) =
  Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Center) {
    Column(
      horizontalAlignment = CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Large),
    ) {
      Icon(
        modifier = Modifier.size(48.dp),
        imageVector = icon,
        contentDescription = null,
        tint = colors.pageTextSubdued,
      )
      Text(
        text = text,
        color = colors.pageTextSubdued,
        style = typography.bodyLarge,
        textAlign = Center,
      )
    }
  }

fun Modifier.searchResultCard(
  colors: Colors,
  onClick: () -> Unit,
  padding: PaddingValues = PaddingValues(12.dp),
): Modifier =
  fillMaxWidth()
    .clip(CardShape)
    .background(colors.tableBackground)
    .clickable(onClick = onClick)
    .padding(padding)

// Bolds each case-insensitive match of query in text
@Composable
fun rememberHighlighted(text: String, query: String): AnnotatedString =
  remember(text, query) {
    buildAnnotatedString {
      append(text)
      if (query.isEmpty()) return@buildAnnotatedString
      var index = text.indexOf(query, ignoreCase = true)
      while (index >= 0) {
        addStyle(SpanStyle(fontWeight = Bold), index, index + query.length)
        index = text.indexOf(query, startIndex = index + query.length, ignoreCase = true)
      }
    }
  }

@Preview
@Composable
private fun PreviewSearchMessage(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { SearchMessage(text = "Enter some text to search") }
