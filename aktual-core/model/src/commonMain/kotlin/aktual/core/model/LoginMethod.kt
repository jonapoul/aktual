package aktual.core.model

import alakazam.kotlin.serialization.SerializableByString
import alakazam.kotlin.serialization.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

/**
 * https://github.com/actualbudget/actual-server/blob/master/src/config-types.ts#L3
 */
@Serializable(LoginMethod.Serializer::class)
enum class LoginMethod(override val value: String) : SerializableByString {
  Password("password"),
  Header("header"),
  OpenId("openid"),
  ;

  internal object Serializer : KSerializer<LoginMethod> by enumStringSerializer()
}
