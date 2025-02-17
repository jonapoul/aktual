package actual.budget.list.nav

import actual.login.model.LoginToken
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaIoSerializable

@Immutable
@Serializable
data class ListBudgetsNavRoute(
  val token: LoginToken,
) : JavaIoSerializable
