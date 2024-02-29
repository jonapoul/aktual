import org.gradle.api.Project

fun Project.stringPropertyOrNull(key: String): String? {
  return properties[key]?.toString()
}

fun Project.stringPropertyOrElse(key: String, default: String): String {
  return stringPropertyOrNull(key) ?: default
}

fun Project.stringPropertyOrThrow(key: String): String {
  return stringPropertyOrNull(key) ?: error("Required property $key")
}

fun Project.intPropertyOrNull(key: String): Int? {
  return stringPropertyOrNull(key)?.toIntOrNull()
}

fun Project.intPropertyOrElse(key: String, default: Int): Int {
  return intPropertyOrNull(key) ?: default
}

fun Project.intPropertyOrThrow(key: String): Int {
  return intPropertyOrNull(key) ?: error("Required property $key")
}

fun Project.floatPropertyOrNull(key: String): Float? {
  return stringPropertyOrNull(key)?.toFloatOrNull()
}

fun Project.floatPropertyOrElse(key: String, default: Float): Float {
  return floatPropertyOrNull(key) ?: default
}

fun Project.boolPropertyOrNull(key: String): Boolean? {
  return stringPropertyOrNull(key)?.toBooleanStrictOrNull()
}

fun Project.boolPropertyOrElse(key: String, default: Boolean): Boolean {
  return boolPropertyOrNull(key) ?: default
}

fun Project.doublePropertyOrNull(key: String): Double? {
  return stringPropertyOrNull(key)?.toDoubleOrNull()
}

fun Project.doublePropertyOrElse(key: String, default: Double): Double {
  return doublePropertyOrNull(key) ?: default
}
