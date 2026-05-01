package aktual.budget.schedules.ui.list

import aktual.budget.model.ScheduleId
import aktual.budget.schedules.vm.Schedule

internal object ListSchedulesPreview {
  val scheduleA = Schedule(id = ScheduleId("abc-123"))

  val scheduleB = Schedule(id = ScheduleId("def-456"))
}
