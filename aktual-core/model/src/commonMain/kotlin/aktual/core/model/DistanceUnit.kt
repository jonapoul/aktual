package aktual.core.model

enum class DistanceUnit {
  Metric,
  Imperial;

  companion object {
    val Default = Imperial
  }
}
