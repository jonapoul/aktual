package aktual.budget.schedules.ui.list

import aktual.budget.schedules.domain.Schedule
import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.Refresh
import aktual.core.icons.Trash
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.PostAdd
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.RowShape
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.contrastingTextColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SwipeableListSchedulesItem(
  schedule: Schedule,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
) {
  SwipeToReveal(
    actions = swipeActions(schedule, onOpenChange, onAction),
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
    shape = RowShape,
  ) {
    ListSchedulesItem(schedule = schedule, onClick = { onAction(Open(schedule.id)) })
  }
}

@Composable
private fun swipeActions(
  schedule: Schedule,
  onOpenChange: (Boolean) -> Unit,
  onAction: ListSchedulesActionHandler,
): ImmutableList<SwipeAction> {
  fun action(action: ListSchedulesAction): () -> Unit = {
    onOpenChange(false)
    onAction(action)
  }

  val deleteBackground = colors.errorText
  val delete =
    SwipeAction(
      text = Strings.listSchedulesDelete,
      icon = AktualIcons.Trash,
      background = deleteBackground,
      foreground = deleteBackground.contrastingTextColor(),
      onClick = action(Delete(schedule)),
    )

  if (schedule.isCompleted) {
    val restartBackground = colors.buttonPrimaryBackground
    return persistentListOf(
      SwipeAction(
        text = Strings.listSchedulesRestart,
        icon = AktualIcons.Refresh,
        background = restartBackground,
        foreground = restartBackground.contrastingTextColor(),
        onClick = action(Restart(schedule)),
      ),
      delete,
    )
  }

  val completeBackground = colors.noticeBackground
  return persistentListOf(
    SwipeAction(
      text = Strings.listSchedulesPost,
      icon = MaterialIcons.PostAdd,
      background = lerp(colors.tableBackground, Black, fraction = 0.1f),
      foreground = colors.tableText,
      onClick = action(Post(schedule)),
    ),
    SwipeAction(
      text = Strings.listSchedulesComplete,
      icon = AktualIcons.Check,
      background = completeBackground,
      foreground = completeBackground.contrastingTextColor(),
      onClick = action(Complete(schedule)),
    ),
    delete,
  )
}

@Preview
@Composable
private fun PreviewSwipeableListSchedulesItem(
  @PreviewParameter(SwipeableProvider::class) params: ColorParams<Schedule>,
) =
  PreviewWithColorParams(params) {
    SwipeableListSchedulesItem(schedule = this, isOpen = true, onOpenChange = {}, onAction = {})
  }

private class SwipeableProvider :
  ColorParamsProvider<Schedule>(
    ListSchedulesPreview.scheduleA,
    ListSchedulesPreview.scheduleCompleted,
  )
