package actual.budget.sync.nav

import actual.account.model.LoginToken
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaIoSerializable

@Immutable
@Serializable
data class SyncBudgetsNavRoute(
  val token: LoginToken,
) : JavaIoSerializable
