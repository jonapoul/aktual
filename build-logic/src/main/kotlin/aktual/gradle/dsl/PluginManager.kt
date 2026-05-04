package aktual.gradle.dsl

import kotlin.reflect.KClass
import org.gradle.api.plugins.PluginManager

internal fun PluginManager.apply(klass: KClass<*>) = apply(klass.java)
