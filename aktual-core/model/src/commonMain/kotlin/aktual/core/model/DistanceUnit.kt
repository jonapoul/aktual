package aktual.core.model

enum class DistanceUnit {
  Automatic,
  Metric,
  Imperial;

  companion object {
    val Default = Automatic
  }
}
