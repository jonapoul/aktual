package aktual.gradle.dsl

import kotlin.reflect.KClass
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection

fun <T : Task, S : T> TaskCollection<T>.withType(type: KClass<S>): TaskCollection<S> =
  withType(type.java)
