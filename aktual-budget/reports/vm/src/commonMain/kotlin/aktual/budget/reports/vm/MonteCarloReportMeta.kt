package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// From packages/loot-core/src/types/models/dashboard.ts. Amounts are in minor units, rates and
// percentages are decimal fractions (0.06 = 6%)
@Serializable
data class MonteCarloReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("pots") val pots: List<MonteCarloPot>? = null,
  @SerialName("withdrawalStrategy") val withdrawalStrategy: WithdrawalStrategy? = null,
  @SerialName("returnModel") val returnModel: ReturnModel? = null,
  @SerialName("withdrawalRule") val withdrawalRule: WithdrawalRule? = null,
  @SerialName("minimumSpending") val minimumSpending: Long? = null,
  @SerialName("spendingPhases") val spendingPhases: List<SpendingPhase>? = null,
  @SerialName("contributions") val contributions: List<Contribution>? = null,
  @SerialName("incomeStreams") val incomeStreams: List<IncomeStream>? = null,
  @SerialName("inflationMean") val inflationMean: Double? = null,
  @SerialName("inflationStdDev") val inflationStdDev: Double? = null,
  @SerialName("taxModel") val taxModel: TaxModel? = null,
  @SerialName("taxBands") val taxBands: List<TaxBand>? = null,
  @SerialName("currentAge") val currentAge: Int? = null,
  @SerialName("targetAge") val targetAge: Int? = null,
  @SerialName("simulationCount") val simulationCount: Int? = null,
) : ReportMeta

@Serializable
data class MonteCarloPot(
  @SerialName("id") val id: String,
  @SerialName("name") val name: String? = null,
  @SerialName("startingBalance") val startingBalance: Long? = null,
  @SerialName("allocationPreset") val allocationPreset: AllocationPreset? = null,
  @SerialName("allocationStocks") val allocationStocks: Double? = null,
  @SerialName("allocationBonds") val allocationBonds: Double? = null,
  @SerialName("allocationCash") val allocationCash: Double? = null,
  @SerialName("expectedReturnMean") val expectedReturnMean: Double? = null,
  @SerialName("returnStdDev") val returnStdDev: Double? = null,
  @SerialName("accessAge") val accessAge: Int? = null,
  @SerialName("accountId") val accountId: AccountId? = null,
  @SerialName("withdrawalTaxRate") val withdrawalTaxRate: Double? = null,
  @SerialName("taxableFraction") val taxableFraction: Double? = null,
  @SerialName("annualFeeFixed") val annualFeeFixed: Long? = null,
  @SerialName("feeAdjustsWithInflation") val feeAdjustsWithInflation: Boolean? = null,
  @SerialName("annualFeeRate") val annualFeeRate: Double? = null,
  @SerialName("isSurplus") val isSurplus: Boolean? = null,
)

@Serializable
data class WithdrawalRule(
  @SerialName("type") val type: WithdrawalRuleType,
  @SerialName("prosperityTriggerPct") val prosperityTriggerPct: Double? = null,
  @SerialName("prosperityIncreasePct") val prosperityIncreasePct: Double? = null,
  @SerialName("preservationTriggerPct") val preservationTriggerPct: Double? = null,
  @SerialName("preservationCutPct") val preservationCutPct: Double? = null,
  @SerialName("balanceThresholdMultiple") val balanceThresholdMultiple: Double? = null,
  @SerialName("consecutiveYears") val consecutiveYears: Int? = null,
  @SerialName("ratchetIncreasePct") val ratchetIncreasePct: Double? = null,
  @SerialName("floorPct") val floorPct: Double? = null,
  @SerialName("ceilingPct") val ceilingPct: Double? = null,
  @SerialName("upperRateThreshold") val upperRateThreshold: Double? = null,
  @SerialName("upperCutPct") val upperCutPct: Double? = null,
  @SerialName("lowerRateThreshold") val lowerRateThreshold: Double? = null,
  @SerialName("lowerIncreasePct") val lowerIncreasePct: Double? = null,
)

@Serializable
data class SpendingPhase(
  @SerialName("id") val id: String,
  @SerialName("name") val name: String? = null,
  @SerialName("fromAge") val fromAge: Int? = null,
  @SerialName("annualWithdrawal") val annualWithdrawal: Long? = null,
)

@Serializable
data class Contribution(
  @SerialName("id") val id: String,
  @SerialName("name") val name: String? = null,
  @SerialName("potId") val potId: String? = null,
  @SerialName("fromAge") val fromAge: Int? = null,
  @SerialName("toAge") val toAge: Int? = null,
  @SerialName("annualAmount") val annualAmount: Long? = null,
  @SerialName("adjustsWithInflation") val adjustsWithInflation: Boolean? = null,
  @SerialName("sourceIncomeStreamId") val sourceIncomeStreamId: String? = null,
  @SerialName("beforeTax") val beforeTax: Boolean? = null,
)

@Serializable
data class IncomeStream(
  @SerialName("id") val id: String,
  @SerialName("name") val name: String? = null,
  @SerialName("fromAge") val fromAge: Int? = null,
  @SerialName("toAge") val toAge: Int? = null,
  @SerialName("annualAmount") val annualAmount: Long? = null,
  @SerialName("adjustsWithInflation") val adjustsWithInflation: Boolean? = null,
  @SerialName("taxRate") val taxRate: Double? = null,
  @SerialName("taxableFraction") val taxableFraction: Double? = null,
)

@Serializable
data class TaxBand(
  @SerialName("id") val id: String,
  @SerialName("from") val from: Long? = null,
  @SerialName("rate") val rate: Double? = null,
)

@Serializable
enum class AllocationPreset {
  @SerialName("equity-100") Equity100,
  @SerialName("equity-80") Equity80,
  @SerialName("equity-60") Equity60,
  @SerialName("equity-40") Equity40,
  @SerialName("cash") Cash,
  @SerialName("custom-mix") CustomMix,
  @SerialName("custom") Custom,
  @Fallback Unknown,
}

@Serializable
enum class WithdrawalStrategy {
  @SerialName("proportional") Proportional,
  @SerialName("sequential") Sequential,
  @SerialName("best-performer") BestPerformer,
  @SerialName("target-mix") TargetMix,
  @Fallback Unknown,
}

@Serializable
enum class ReturnModel {
  @SerialName("normal") Normal,
  @SerialName("historical-bootstrap") HistoricalBootstrap,
  @SerialName("historical-sequence") HistoricalSequence,
  @Fallback Unknown,
}

@Serializable
enum class WithdrawalRuleType {
  @SerialName("none") None,
  @SerialName("guardrails") Guardrails,
  @SerialName("ratcheting") Ratcheting,
  @SerialName("floor-ceiling") FloorCeiling,
  @SerialName("boundaries") Boundaries,
  @Fallback Unknown,
}

@Serializable
enum class TaxModel {
  @SerialName("flat") Flat,
  @SerialName("bands") Bands,
  @Fallback Unknown,
}
