package actual.budget.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// See DataEntity in packages/loot-core/src/types/models/reports.ts
// TODO: Add
@Serializable
data class ReportMetadata(
  @SerialName("startDate") val startDate: ReportDate?,
  @SerialName("endDate") val endDate: ReportDate?,
  @SerialName("data") val data: List<ReportMetadataItem>?,
  @SerialName("groupedData") val groupedData: List<ReportMetadataItem>?,
  @SerialName("totalAssets") val totalAssets: Double,
  @SerialName("totalDebts") val totalDebts: Double,
  @SerialName("totalTotals") val totalTotals: Double,
  @SerialName("netAssets") val netAssets: Double,
  @SerialName("netDebts") val netDebts: Double,
  @SerialName("legend") val legend: List<ReportLegendItem>?,
  @SerialName("intervalData") val intervalData: List<ReportIntervalItem>?,
)

// See GroupedEntity in packages/loot-core/src/types/models/reports.ts
@Serializable
data class ReportMetadataItem(
  @SerialName("id") val id: String,
  @SerialName("name") val name: String,
  @SerialName("date") val date: String?,
)

// See LegendEntity in packages/loot-core/src/types/models/reports.ts
@Serializable
data class ReportLegendItem(
  @SerialName("id") val id: String?,
  @SerialName("name") val name: String,
  @SerialName("color") val color: ReportLegendColor,
)

// See IntervalEntity in packages/loot-core/src/types/models/reports.ts
@Serializable
data class ReportIntervalItem(
  @SerialName("id") val id: String?,
  @SerialName("name") val name: String,
  @SerialName("color") val color: ReportLegendColor,
)

@Serializable(ReportDataColorSerializer::class)
sealed interface ReportLegendColor {
  // Possible values in getColorScale(), packages/desktop-client/src/components/reports/chart-theme.ts
  @JvmInline
  value class Hex(val hex: String) : ReportLegendColor

  // Possible values in getColor(), packages/desktop-client/src/components/reports/spreadsheets/calculateLegend.ts
  sealed class Named(val name: String) : ReportLegendColor {
    companion object {
      operator fun get(name: String) = when (name) {
        "reportsBlue" -> Blue
        "reportsRed" -> Red
        "reportsGreen" -> Green
        "reportsGray" -> Gray
        else -> error("Unknown colour $name")
      }
    }
  }

  data object Blue : Named("reportsBlue")
  data object Red : Named("reportsRed")
  data object Green : Named("reportsGreen")
  data object Gray : Named("reportsGray")
}

// e.g "var(--color-reportsBlue)"
private val VarReportColorRegex = "^var\\(--color-(.*?)\\)$".toRegex()

private object ReportDataColorSerializer : KSerializer<ReportLegendColor> {
  override val descriptor = PrimitiveSerialDescriptor("ReportDataColor", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ReportLegendColor) {
    when (value) {
      is ReportLegendColor.Hex -> encoder.encodeString(value.hex)
      is ReportLegendColor.Named -> encoder.encodeString("var(--color-${value.name})")
    }
  }

  override fun deserialize(decoder: Decoder): ReportLegendColor {
    val value = decoder.decodeString()
    val varName = VarReportColorRegex.find(value)?.groupValues?.get(1)
    return if (varName != null) ReportLegendColor.Named[varName] else ReportLegendColor.Hex(value)
  }
}
