package aktual.core.model

fun interface UrlOpener {
  operator fun invoke(url: String)
}
