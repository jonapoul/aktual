package aktual.budget.reports.vm

import org.intellij.lang.annotations.Language

// Plans shared by the Monte Carlo tests, whose expected values come from upstream

@Language("JSON") internal const val DEFAULT_PLAN = "{}"

@Language("JSON")
internal const val BOOTSTRAP_CUSTOM_MIX =
  """
  {
    "returnModel": "historical-bootstrap",
    "simulationCount": 2000,
    "pots": [
      {
        "id": "a",
        "startingBalance": 80000000,
        "allocationPreset": "equity-80"
      },
      {
        "id": "b",
        "startingBalance": 20000000,
        "allocationPreset": "custom-mix",
        "allocationStocks": 0.3,
        "allocationBonds": 0.5,
        "allocationCash": 0.2
      }
    ],
    "spendingPhases": [
      {
        "id": "p1",
        "annualWithdrawal": 4000000
      }
    ],
    "currentAge": 55,
    "targetAge": 95
  }

  """

@Language("JSON")
internal const val SEQUENCE_SEQUENTIAL =
  """
  {
    "returnModel": "historical-sequence",
    "pots": [
      {
        "id": "a",
        "startingBalance": 100000000,
        "allocationPreset": "equity-60"
      },
      {
        "id": "c",
        "startingBalance": 5000000,
        "allocationPreset": "custom",
        "expectedReturnMean": 0.04,
        "returnStdDev": 0.05
      }
    ],
    "spendingPhases": [
      {
        "id": "p1",
        "annualWithdrawal": 4500000
      }
    ],
    "withdrawalStrategy": "sequential"
  }

  """

@Language("JSON")
internal const val GUARDRAILS_LOCKED_POT =
  """
  {
    "withdrawalRule": {
      "type": "guardrails"
    },
    "minimumSpending": 2500000,
    "spendingPhases": [
      {
        "id": "p1",
        "annualWithdrawal": 3000000
      },
      {
        "id": "p2",
        "fromAge": 75,
        "annualWithdrawal": 2200000
      }
    ],
    "pots": [
      {
        "id": "a",
        "startingBalance": 70000000,
        "allocationPreset": "equity-80",
        "accessAge": 57
      },
      {
        "id": "b",
        "startingBalance": 20000000,
        "allocationPreset": "cash"
      }
    ],
    "currentAge": 52,
    "targetAge": 92,
    "withdrawalStrategy": "best-performer"
  }

  """

@Language("JSON")
internal const val RATCHETING_TARGET_MIX =
  """
  {
    "withdrawalRule": {
      "type": "ratcheting",
      "consecutiveYears": 2
    },
    "pots": [
      {
        "id": "a",
        "startingBalance": 60000000,
        "allocationPreset": "equity-100"
      },
      {
        "id": "b",
        "startingBalance": 40000000,
        "allocationPreset": "equity-40"
      }
    ],
    "withdrawalStrategy": "target-mix",
    "spendingPhases": [
      {
        "id": "p1",
        "annualWithdrawal": 3500000
      }
    ]
  }

  """

@Language("JSON")
internal const val FLOOR_CEILING_FEES =
  """
  {
    "withdrawalRule": {
      "type": "floor-ceiling",
      "floorPct": 0.1,
      "ceilingPct": 0.25
    },
    "inflationStdDev": 0,
    "inflationMean": 0.03,
    "pots": [
      {
        "id": "a",
        "startingBalance": 90000000,
        "annualFeeRate": 0.005,
        "annualFeeFixed": 50000,
        "feeAdjustsWithInflation": true
      }
    ],
    "spendingPhases": [
      {
        "id": "p1",
        "annualWithdrawal": 4000000
      }
    ]
  }

  """

@Language("JSON")
internal const val BOUNDARIES_TAX_BANDS =
  """
  {
    "withdrawalRule": {
      "type": "boundaries"
    },
    "minimumSpending": 3000000,
    "taxModel": "bands",
    "taxBands": [
      {
        "id": "b1",
        "from": 1257000,
        "rate": 0.2
      },
      {
        "id": "b2",
        "from": 5027000,
        "rate": 0.4
      }
    ],
    "pots": [
      {
        "id": "pen",
        "startingBalance": 60000000,
        "taxableFraction": 0.75,
        "accessAge": 57
      },
      {
        "id": "isa",
        "startingBalance": 30000000,
        "taxableFraction": 0
      }
    ],
    "incomeStreams": [
      {
        "id": "sp",
        "fromAge": 67,
        "annualAmount": 1150000,
        "taxableFraction": 1
      }
    ],
    "currentAge": 55,
    "targetAge": 95,
    "spendingPhases": [
      {
        "id": "p1",
        "annualWithdrawal": 4000000
      }
    ]
  }

  """

@Language("JSON")
internal const val INCOME_CONTRIBUTIONS_SURPLUS =
  """
  {
    "pots": [
      {
        "id": "surplus",
        "isSurplus": true
      },
      {
        "id": "a",
        "startingBalance": 40000000,
        "withdrawalTaxRate": 0.15
      }
    ],
    "incomeStreams": [
      {
        "id": "job",
        "toAge": 50,
        "annualAmount": 6000000,
        "taxRate": 0.3
      },
      {
        "id": "db",
        "fromAge": 60,
        "annualAmount": 1500000,
        "adjustsWithInflation": false,
        "taxRate": 0.1
      }
    ],
    "contributions": [
      {
        "id": "c1",
        "potId": "a",
        "toAge": 50,
        "annualAmount": 1000000,
        "sourceIncomeStreamId": "job",
        "beforeTax": true
      },
      {
        "id": "c2",
        "potId": "a",
        "toAge": 50,
        "annualAmount": 500000,
        "sourceIncomeStreamId": "job"
      },
      {
        "id": "c3",
        "potId": "a",
        "fromAge": 45,
        "toAge": 55,
        "annualAmount": 300000,
        "adjustsWithInflation": false
      }
    ],
    "spendingPhases": [
      {
        "id": "p1",
        "annualWithdrawal": 2800000
      }
    ],
    "currentAge": 40,
    "targetAge": 95,
    "simulationCount": 3000
  }

  """

@Language("JSON")
internal const val INFLATION_DISABLED =
  """
  {
    "inflationMean": null,
    "returnModel": "historical-bootstrap",
    "simulationCount": 1000,
    "pots": [
      {
        "id": "a",
        "startingBalance": 50000000,
        "allocationPreset": "equity-60"
      }
    ],
    "spendingPhases": [
      {
        "id": "p1",
        "annualWithdrawal": 2500000
      }
    ]
  }

  """
