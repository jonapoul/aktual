package actual.diagrams.tasks

import actual.diagrams.ModuleType
import okio.buffer
import okio.sink
import java.io.File

internal object TypedModules {
  fun read(file: File): Set<TypedModule> = file
    .readLines()
    .map(::TypedModule)
    .toSet()

  fun write(data: Set<TypedModule>, file: File) {
    file.sink().buffer().use { sink ->
      data.forEach {
        sink.writeUtf8(it.toString())
        sink.writeUtf8("\n")
      }
    }
  }
}

internal data class TypedModule(
  val projectPath: String,
  val type: ModuleType,
) : Comparable<TypedModule> {
  override fun toString(): String = "$projectPath ${type.name}"
  override fun compareTo(other: TypedModule): Int = projectPath.compareTo(other.projectPath)
}

internal fun TypedModule(string: String): TypedModule {
  val (path, type) = string.split(" ")
  return TypedModule(path, ModuleType.valueOf(type))
}
