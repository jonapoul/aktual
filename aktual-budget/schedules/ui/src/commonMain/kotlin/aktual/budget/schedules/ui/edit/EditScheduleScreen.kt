package aktual.budget.schedules.ui.edit

import aktual.budget.model.ScheduleId
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredBooleanParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun EditScheduleScreen(
  id: ScheduleId?,
  back: BackNavigator,
  modifier: Modifier = Modifier,
) {
  EditScheduleScaffold(modifier = modifier, isCreate = id == null, onBack = { back() })
}

@Composable
private fun EditScheduleScaffold(
  isCreate: Boolean,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = onBack) },
        title = {
          Text(
            text =
              if (isCreate) Strings.editScheduleToolbarCreate else Strings.editScheduleToolbarEdit
          )
        },
      )
    },
  ) {
    Box { PageBackground() }
  }
}

@PortraitPreview
@Composable
private fun PreviewEditScheduleScaffold(
  @PreviewParameter(ColoredBooleanParameters::class) params: ColoredParams<Boolean>
) {
  PreviewWithColoredParams(params) { EditScheduleScaffold(isCreate = this, onBack = {}) }
}
