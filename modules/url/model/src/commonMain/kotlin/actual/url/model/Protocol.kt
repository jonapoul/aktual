package actual.url.model

enum class Protocol(private val value: String) {
  Http("http"),
  Https("https"),
  ;

  override fun toString(): String = value
}
