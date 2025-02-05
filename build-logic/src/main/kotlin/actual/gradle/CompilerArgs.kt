package actual.gradle

internal val COMPILER_ARGS = listOf(
  "-Xjvm-default=all-compatibility",
  "-opt-in=kotlin.RequiresOptIn",
  "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
  "-opt-in=kotlinx.coroutines.FlowPreview",
)
