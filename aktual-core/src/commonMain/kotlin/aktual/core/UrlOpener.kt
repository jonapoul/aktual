package aktual.core

fun interface UrlOpener {
  operator fun invoke(url: String)
}
