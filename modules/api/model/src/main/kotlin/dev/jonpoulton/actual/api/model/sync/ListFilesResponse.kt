package dev.jonpoulton.actual.api.model.sync

import dev.jonpoulton.actual.api.model.Response
import dev.jonpoulton.actual.api.model.ResponseStatus
import dev.jonpoulton.actual.api.model.internal.IntToBooleanSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListFilesResponse(
  @SerialName("data")
  val data: List<Data>,

  @SerialName("status")
  override val status: ResponseStatus = ResponseStatus.Ok,
) : Response {
  @Serializable
  data class Data(
    @SerialName("name")
    val name: String,

    @SerialName("fileId")
    val fileId: String,

    @SerialName("groupId")
    val groupId: String,

    @SerialName("encryptKeyId")
    val encryptKeyId: String?,

    @SerialName("deleted")
    @Serializable(IntToBooleanSerializer::class)
    val deleted: Boolean,
  )
}
