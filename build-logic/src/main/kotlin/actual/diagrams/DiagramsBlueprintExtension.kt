@file:Suppress("MagicNumber")

package actual.diagrams

import guru.nidi.graphviz.attribute.Rank
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

open class DiagramsBlueprintExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory,
) {
  val generateModules: Property<Boolean> = objects
    .property(Boolean::class.java)
    .convention(true)

  val generateDependencies: Property<Boolean> = objects
    .property(Boolean::class.java)
    .convention(true)

  val showLegend: Property<Boolean> = objects
    .property(Boolean::class.java)
    .convention(true)

  val legendBackground: Property<String> = objects
    .property(String::class.java)
    .convention("#bbbbbb")

  val legendTitleFontSize: Property<Int> = objects
    .property(Int::class.java)
    .convention(20)

  val legendFontSize: Property<Int> = objects
    .property(Int::class.java)
    .convention(15)

  val nodeFontSize: Property<Int> = objects
    .property(Int::class.java)
    .convention(30)

  val removeModulePrefix: Property<String?> = objects
    .property(String::class.java)
    .convention(":modules:")

  val replacementModulePrefix: Property<String?> = objects
    .property(String::class.java)
    .convention(":")

  val rankDir: Property<Rank.RankDir> = objects
    .property(Rank.RankDir::class.java)
    .convention(Rank.RankDir.TOP_TO_BOTTOM)

  val rankSeparation: Property<Double> = objects
    .property(Double::class.java)
    .convention(1.5)

  val topLevelProject: Property<String> = objects
    .property(String::class.java)
    .convention(target.path)

  val checkModulesDotfile: Property<Boolean> = objects
    .property(Boolean::class.java)
    .convention(true)

  val checkDependenciesDotfile: Property<Boolean> = objects
    .property(Boolean::class.java)
    .convention(false)

  val moduleTypes: SetProperty<ModuleType> = objects
    .setProperty(ModuleType::class.java)
    .convention(ActualModuleType.values().toSet())

  val moduleTypeFinder: Property<ModuleType.Finder> = objects
    .property(ModuleType.Finder::class.java)
    .convention(ActualModuleType.Finder)

  val checkReadmeContents: Property<Boolean> = objects
    .property(Boolean::class.java)
    .convention(true)
}
