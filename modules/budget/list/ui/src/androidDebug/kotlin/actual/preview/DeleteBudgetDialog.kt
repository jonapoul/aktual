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
