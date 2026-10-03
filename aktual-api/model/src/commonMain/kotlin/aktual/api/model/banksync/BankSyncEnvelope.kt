package aktual.api.model.banksync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The wrapper around every bank sync endpoint's response. Provider failures (e.g. an expired access
 * token) still come back with [status] "ok" and an error in [data]; only server-side rejections
 * like an unconfigured provider use another status, with [reason] set instead.
 *
 * See packages/loot-core/src/server/post.ts
 */
@Serializable
data class BankSyncEnvelope<T>(
  @SerialName("status") val status: String,
  @SerialName("data") val data: T? = null,
  @SerialName("reason") val reason: String? = null,
  @SerialName("details") val details: String? = null,
) {
  val isOk: Boolean
    get() = status == "ok"
}
