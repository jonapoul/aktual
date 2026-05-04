package aktual.gradle.dsl

import kotlin.reflect.KClass
import org.gradle.api.plugins.ExtensionContainer

internal fun <T : Any> ExtensionContainer.configure(klass: KClass<T>, action: T.() -> Unit) =
  configure(klass.java, action)
