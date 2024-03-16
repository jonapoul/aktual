package dev.jonpoulton.actual.api.model.internal

import dev.jonpoulton.actual.api.model.account.BootstrapResponse
import dev.jonpoulton.actual.api.model.account.BootstrapResponse.Error
import dev.jonpoulton.actual.api.model.account.BootstrapResponse.Ok

internal object BootstrapResponseSerializer : ResponseSerializer<BootstrapResponse, Ok, Error>(
  responseClass = BootstrapResponse::class,
  okSerializer = Ok.serializer(),
  errorSerializer = Error.serializer(),
)
