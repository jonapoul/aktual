package actual.core.di

import dev.zacsweers.metro.Provider
import kotlin.reflect.KClass

typealias ProviderMap<T> = Map<KClass<out T>, Provider<T>>
