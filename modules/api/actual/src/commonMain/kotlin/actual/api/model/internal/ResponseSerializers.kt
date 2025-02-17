package actual.api.model.internal

import actual.api.model.Response
import actual.api.model.ResponseStatus
import actual.api.model.account.BootstrapResponse
import actual.api.model.account.LoginResponse
import actual.api.model.account.NeedsBootstrapResponse
import actual.api.model.sync.ListUserFilesResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass

internal object ListUserFileSerializer :
  ResponseSerializer<ListUserFilesResponse, ListUserFilesResponse.Ok, ListUserFilesResponse.Error>(
    responseClass = ListUserFilesResponse::class,
    okSerializer = ListUserFilesResponse.Ok.serializer(),
    errorSerializer = ListUserFilesResponse.Error.serializer(),
  )

internal object BootstrapResponseSerializer :
  ResponseSerializer<BootstrapResponse, BootstrapResponse.Ok, BootstrapResponse.Error>(
    responseClass = BootstrapResponse::class,
    okSerializer = BootstrapResponse.Ok.serializer(),
    errorSerializer = BootstrapResponse.Error.serializer(),
  )

internal object LoginResponseSerializer : ResponseSerializer<LoginResponse, LoginResponse.Ok, LoginResponse.Error>(
  responseClass = LoginResponse::class,
  okSerializer = LoginResponse.Ok.serializer(),
  errorSerializer = LoginResponse.Error.serializer(),
)

internal object NeedsBootstrapResponseSerializer :
  ResponseSerializer<NeedsBootstrapResponse, NeedsBootstrapResponse.Ok, NeedsBootstrapResponse.Error>(
    responseClass = NeedsBootstrapResponse::class,
    okSerializer = NeedsBootstrapResponse.Ok.serializer(),
    errorSerializer = NeedsBootstrapResponse.Error.serializer(),
  )

internal abstract class ResponseSerializer<R : Response, Ok : R, Error : R>(
  responseClass: KClass<R>,
  private val okSerializer: DeserializationStrategy<Ok>,
  private val errorSerializer: DeserializationStrategy<Error>,
) : JsonContentPolymorphicSerializer<R>(responseClass) {
  final override fun selectDeserializer(element: JsonElement): DeserializationStrategy<R> {
    val status = element.jsonObject["status"]
      ?.jsonPrimitive
      ?.content
      ?.let { status -> ResponseStatus.entries.firstOrNull { it.value.equals(status, ignoreCase = true) } }
      ?: error("Required status")
    return when (status) {
      ResponseStatus.Ok -> okSerializer
      ResponseStatus.Error -> errorSerializer
    }
  }
}
