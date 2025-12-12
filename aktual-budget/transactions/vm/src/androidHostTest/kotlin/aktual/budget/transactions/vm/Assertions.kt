package aktual.budget.transactions.vm

import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingSource.LoadResult.Page
import assertk.Assert
import assertk.assertions.support.expected

internal inline fun <reified K : Any, reified V : Any> Assert<LoadResult<K, V>>.isPage() =
  transform("is a LoadResult.Page") { result ->
    when (result) {
      is Page<K, V> -> result
      is LoadResult.Error -> expected("Page, got ${result.throwable}")
      is LoadResult.Invalid -> expected("Page, got Invalid")
    }
  }

internal fun <K : Any, V : Any> Assert<Page<K, V>>.withData(expected: List<V>) = transform { page ->
  if (page.data != expected) {
    expected("page with data $expected, got ${page.data}")
  }
  return@transform page
}

internal fun <K : Any, V : Any> Assert<Page<K, V>>.withData(vararg expected: V) = withData(expected.toList())

internal fun <K : Any, V : Any> Assert<Page<K, V>>.withPrevKey(expected: K?) = transform { page ->
  if (page.prevKey != expected) {
    expected("page with prevKey $expected, got ${page.prevKey}")
  }
  return@transform page
}

internal fun <K : Any, V : Any> Assert<Page<K, V>>.withNextKey(expected: K?) = transform { page ->
  if (page.nextKey != expected) {
    expected("page with nextKey $expected, got ${page.nextKey}")
  }
  return@transform page
}
