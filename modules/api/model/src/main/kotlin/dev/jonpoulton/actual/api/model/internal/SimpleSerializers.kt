package dev.jonpoulton.actual.api.model.internal

import alakazam.kotlin.serialization.SimpleSerializer
import dev.jonpoulton.actual.core.model.Password
import dev.jonpoulton.actual.core.model.LoginToken

internal object PasswordSerializer : SimpleSerializer<Password>(serialName = "Password", ::Password)
internal object TokenSerializer : SimpleSerializer<LoginToken>(serialName = "Token", ::LoginToken)
