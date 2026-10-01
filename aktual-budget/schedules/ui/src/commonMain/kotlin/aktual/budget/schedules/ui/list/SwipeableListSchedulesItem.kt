package aktual.budget.schedules.ui.list

import aktual.budget.schedules.vm.Schedule
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.PostAdd
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.RowShape
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.contrastingTextColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SwipeableListSchedulesItem(
  schedule: Schedule,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val deleteBackground = colors.errorText
  SwipeToReveal(
    actions =
      persistentListOf(
        SwipeAction(
          text = Strings.listSchedulesPost,
          icon = MaterialIcons.PostAdd,
          background = lerp(colors.tableBackground, Black, fraction = 0.1f),
          foreground = colors.tableText,
          onClick = { onAction(Post(schedule)) },
        ),
        SwipeAction(
          text = Strings.listSchedulesDelete,
          icon = MaterialIcons.Delete,
          background = deleteBackground,
          foreground = deleteBackground.contrastingTextColor(),
          onClick = { onAction(Delete(schedule)) },
        ),
      ),
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
    shape = RowShape,
  ) {
    ListSchedulesItem(schedule = schedule, onClick = { onAction(Open(schedule.id)) })
  }
}

@Preview
@Composable
private fun PreviewSwipeableListSchedulesItem(
  @PreviewParameter(SwipeableProvider::class) params: ColoredParams<Boolean>
) =
  PreviewWithColoredParams(params) {
    SwipeableListSchedulesItem(
      schedule = ListSchedulesPreview.scheduleA,
      isOpen = this,
      onOpenChange = {},
      onAction = {},
    )
  }

private class SwipeableProvider : ColoredParameterProvider<Boolean>(false, true)
