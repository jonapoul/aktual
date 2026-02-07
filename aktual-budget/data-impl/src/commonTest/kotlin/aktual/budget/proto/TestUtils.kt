package aktual.budget.proto

import aktual.budget.model.Timestamp

internal operator fun Timestamp.invoke(value: Int): Timestamp {
  val content = toString().replace(oldValue = "0000", newValue = "%04d".format(value))
  return Timestamp.parse(content)
}
