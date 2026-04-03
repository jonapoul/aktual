package aktual.app.android

import android.app.Activity
import dev.zacsweers.metro.MapKey
import kotlin.reflect.KClass

@MapKey(implicitClassKey = true)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ActivityKey(val value: KClass<out Activity> = Nothing::class)
