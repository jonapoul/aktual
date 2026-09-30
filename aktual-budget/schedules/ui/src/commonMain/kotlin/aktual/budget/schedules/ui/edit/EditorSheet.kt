package aktual.budget.schedules.ui.edit

import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PrimaryTextButton
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

internal object EditScheduleDS {
  val contentPadding = 16.dp
  val sectionSpacing = 20.dp
  val fieldSpacing = 12.dp
  val labelSpacing = 6.dp
  val rowPadding = 12.dp
  val sheetPadding = 20.dp
  val sheetSpacing = 16.dp
  val badgeSpacing = 10.dp
  val hairline: Dp = 1.dp
}

@Composable
internal fun EditorSheet(
  title: String,
  canConfirm: Boolean,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  val sheetState = rememberBottomSheetState(initialValue = Hidden)
  val scope = rememberCoroutineScope()
  fun hide(then: () -> Unit) {
    scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
  }

  ModalBottomSheet(
    modifier = modifier.border(EditScheduleDS.hairline, colors.modalBorder),
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = colors.modalBackground,
    contentColor = colors.pageText,
  ) {
    Column(
      modifier =
        Modifier.verticalScroll(rememberScrollState())
          .padding(horizontal = EditScheduleDS.sheetPadding)
          .padding(bottom = EditScheduleDS.sheetPadding),
      verticalArrangement = Arrangement.spacedBy(EditScheduleDS.sheetSpacing),
    ) {
      Text(text = title, style = typography.headlineSmall)

      content()

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.fieldSpacing),
      ) {
        NormalTextButton(
          modifier = Modifier.weight(1f),
          text = Strings.editScheduleDatePickerCancel,
          onClick = { hide(onDismiss) },
        )
        PrimaryTextButton(
          modifier = Modifier.weight(1f),
          text = Strings.editScheduleDone,
          isEnabled = canConfirm,
          onClick = {
            onConfirm()
            hide(onDismiss)
          },
        )
      }
    }
  }
}
