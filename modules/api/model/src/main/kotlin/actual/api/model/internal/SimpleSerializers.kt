package actual.api.model.internal

import actual.core.model.LoginToken
import actual.core.model.Password
import alakazam.kotlin.serialization.SimpleSerializer

internal object PasswordSerializer : SimpleSerializer<Password>(serialName = "Password", ::Password)
internal object TokenSerializer : SimpleSerializer<LoginToken>(serialName = "Token", ::LoginToken)
