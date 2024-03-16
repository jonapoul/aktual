package dev.jonpoulton.actual.api.model.internal

import dev.jonpoulton.actual.api.model.account.NeedsBootstrapResponse
import dev.jonpoulton.actual.api.model.account.NeedsBootstrapResponse.Error
import dev.jonpoulton.actual.api.model.account.NeedsBootstrapResponse.Ok

internal object NeedsBootstrapResponseSerializer : ResponseSerializer<NeedsBootstrapResponse, Ok, Error>(
  responseClass = NeedsBootstrapResponse::class,
  okSerializer = Ok.serializer(),
  errorSerializer = Error.serializer(),
)
