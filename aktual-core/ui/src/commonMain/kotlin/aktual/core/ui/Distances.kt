package aktual.core.ui

import aktual.core.l10n.Strings
import aktual.core.model.Distance
import aktual.core.model.DistanceUnit
import androidx.compose.runtime.Composable
import kotlin.math.roundToInt

@Composable
fun Distance.formatted(unit: DistanceUnit = LocalDistanceUnit.current): String {
  val feet = meters * FEET_PER_METER
  return when {
    unit == Imperial && feet < FEET_PER_MILE / TENTHS -> Strings.distanceFeet(feet.whole())
    unit == Imperial -> Strings.distanceMiles((feet / FEET_PER_MILE).oneDecimal())
    meters < METERS_PER_KILOMETER -> Strings.distanceMeters(meters.whole())
    else -> Strings.distanceKilometers((meters / METERS_PER_KILOMETER).oneDecimal())
  }
}

private fun Double.whole(): String = roundToInt().toString()

private fun Double.oneDecimal(): String {
  val tenths = (this * TENTHS).roundToInt()
  return "${tenths / TENTHS}.${tenths % TENTHS}"
}

private const val TENTHS = 10
private const val FEET_PER_METER = 3.28084
private const val FEET_PER_MILE = 5280.0
private const val METERS_PER_KILOMETER = 1000.0
