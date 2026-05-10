package aktual.di

import dev.zacsweers.metro.MapKey
import kotlin.reflect.KClass

/**
 * A way of wiring up manual DI in exceptional circumstances. Usually when I can't be bothered
 * untangling the module graph to make it work better
 */
@MapKey(implicitClassKey = false)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class AccessorKey(val value: KClass<out Any>)

fun interface Accessor {
  operator fun invoke(): Any
}
