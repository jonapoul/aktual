package aktual.budget.proto

import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import okio.Buffer
import kotlin.time.Instant

fun interface SyncRequestEncoder {
  operator fun invoke(groupId: String, fileId: BudgetId, since: Instant): Buffer
}

@Inject
@ContributesBinding(BudgetScope::class, binding<SyncRequestEncoder>())
class SyncRequestEncoderImpl : SyncRequestEncoder {
  override operator fun invoke(groupId: String, fileId: BudgetId, since: Instant): Buffer {
    TODO()
  }
}
