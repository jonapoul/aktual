package actual.core.di

import dev.zacsweers.metro.MapKey
import kotlin.reflect.KClass

@MapKey
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AssistedFactoryKey(val value: KClass<out ViewModelAssistedFactory>)
