package aktual.budget.db.model

import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import androidx.room3.ColumnInfo
import androidx.room3.DatabaseView
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray

@DatabaseView(
  viewName = "v_schedules",
  value =
    """
    SELECT
      s.id,
      s.name,
      s.rule,
      CASE
        WHEN snd.local_next_date_ts = snd.base_next_date_ts THEN snd.local_next_date
        ELSE snd.base_next_date
      END AS next_date,
      s.completed,
      s.posts_transaction,
      s.tombstone,
      pm.targetId AS _payee,
      json_extract(r.conditions, sjp.account || '.value') AS _account,
      json_extract(r.conditions, sjp.amount || '.value') AS _amount,
      json_extract(r.conditions, sjp.amount || '.op') AS _amountOp,
      json_extract(r.conditions, sjp.date || '.value') AS _date,
      r.conditions AS _conditions,
      r.actions AS _actions
    FROM schedules s
    LEFT JOIN schedules_next_date snd ON snd.schedule_id = s.id
    LEFT JOIN schedules_json_paths sjp ON sjp.schedule_id = s.id
    LEFT JOIN rules r ON r.id = s.rule
    LEFT JOIN payee_mapping pm ON pm.id = json_extract(r.conditions, sjp.payee || '.value')
  """,
)
data class VSchedules(
  @ColumnInfo(name = "id") val id: ScheduleId,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "rule") val rule: RuleId?,
  @ColumnInfo(name = "next_date") val nextDate: LocalDate?,
  @ColumnInfo(name = "completed") val completed: Boolean?,
  @ColumnInfo(name = "posts_transaction") val postsTransaction: Boolean?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean?,
  @ColumnInfo(name = "_payee") val payee: PayeeId?,
  @ColumnInfo(name = "_account") val account: String?,
  @ColumnInfo(name = "_amount") val amount: String?,
  @ColumnInfo(name = "_amountOp") val amountOp: String?,
  @ColumnInfo(name = "_date") val date: String?,
  @ColumnInfo(name = "_conditions") val conditions: JsonArray?,
  @ColumnInfo(name = "_actions") val actions: JsonArray?,
)
