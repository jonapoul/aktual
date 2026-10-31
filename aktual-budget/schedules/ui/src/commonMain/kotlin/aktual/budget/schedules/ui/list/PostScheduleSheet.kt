package aktual.budget.schedules.ui.list

import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.ui.list.ListSchedulesPreview.scheduleA
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.formatted
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

@Composable
internal fun PostScheduleSheet(
  schedule: Schedule,
  today: LocalDate,
  onPost: (LocalDate) -> Unit,
  onDismiss: () -> Unit,
) {
  AktualModalBottomSheet(onDismissRequest = onDismiss) {
    PostScheduleSheetContent(
      schedule = schedule,
      today = today,
      onPost = { date ->
        onDismiss()
        onPost(date)
      },
    )
  }
}

@Composable
private fun PostScheduleSheetContent(
  schedule: Schedule,
  today: LocalDate,
  onPost: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth().padding(bottom = 24.dp)) {
    Text(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).semantics { heading() },
      text = Strings.listSchedulesPostTitle,
      style = typography.headlineSmall,
    )
    PostOption(title = Strings.listSchedulesPostToday, date = today, onPost = onPost)
    if (schedule.nextDate != today) {
      PostOption(
        title = Strings.listSchedulesPostScheduled,
        date = schedule.nextDate,
        onPost = onPost,
      )
    }
  }
}

@Composable
private fun PostOption(title: String, date: LocalDate, onPost: (LocalDate) -> Unit) {
  ListItem(
    modifier = Modifier.clickable { onPost(date) },
    headlineContent = { Text(title) },
    supportingContent = { Text(date.formatted()) },
    colors = colors.option(),
  )
}

@Composable
private fun Colors.option(): ListItemColors =
  ListItemDefaults.colors(
    containerColor = Transparent,
    headlineColor = pageText,
    supportingColor = pageTextSubdued,
  )

@Preview
@Composable
private fun PreviewPostScheduleSheet(
  @PreviewParameter(PostScheduleSheetProvider::class) params: ColorParams<LocalDate>,
) =
  PreviewWithColorParams(params) {
    PostScheduleSheetContent(schedule = scheduleA, today = this, onPost = {})
  }

private class PostScheduleSheetProvider :
  ColorParamsProvider<LocalDate>(scheduleA.nextDate.minus(1, DateTimeUnit.DAY), scheduleA.nextDate)
