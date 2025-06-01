package actual.budget.encryption

import okio.IOException
import okio.Sink
import okio.Source

@Throws(UnknownAlgorithmException::class)
expect fun Source.decryptToSink(
  key: ByteArray,
  iv: ByteArray,
  authTag: ByteArray,
  algorithm: String,
  sink: Sink,
)

class UnknownAlgorithmException(val algorithm: String) : IOException()
