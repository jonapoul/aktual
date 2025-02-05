// https://publicobject.com/2024/01/30/internal-visibility/
// Only used to access the "outputFileNameDot" properties, which are declared internal
@file:Suppress(
  "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
  "INVISIBLE_MEMBER",
  "INVISIBLE_REFERENCE",
)

package actual.diagrams.internal

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorTask
import com.vanniktech.dependency.graph.generator.ProjectDependencyGraphGeneratorTask
import java.io.File

internal fun ProjectDependencyGraphGeneratorTask.getOutputFile(): File {
  return File(outputDirectory, projectGenerator.outputFileNameDot)
}

internal fun DependencyGraphGeneratorTask.getOutputFile(): File {
  return File(outputDirectory, generator.outputFileNameDot)
}
