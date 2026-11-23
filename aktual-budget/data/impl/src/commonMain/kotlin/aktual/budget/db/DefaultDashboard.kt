package aktual.budget.db

// DEFAULT_DASHBOARD_STATE in packages/loot-core/src/shared/dashboard.ts, which upstream's
// 1722804019000_create_dashboard_table migration adds to every new budget
internal const val DEFAULT_DASHBOARD_WIDGETS =
  """
  [
    {
      "type": "summary-card",
      "width": 3,
      "height": 2,
      "x": 0,
      "y": 0,
      "meta": {
        "name": "Total Income (YTD)",
        "content": "{\"type\":\"sum\",\"fontSize\":20}",
        "timeFrame": {
          "start": "2024-01-01",
          "end": "2024-12-31",
          "mode": "yearToDate"
        },
        "conditions": [
          {
            "field": "amount",
            "op": "gt",
            "value": 0
          },
          {
            "field": "account",
            "op": "onBudget",
            "value": ""
          },
          {
            "field": "transfer",
            "op": "is",
            "value": false
          }
        ],
        "conditionsOp": "and"
      }
    },
    {
      "type": "summary-card",
      "width": 3,
      "height": 2,
      "x": 3,
      "y": 0,
      "meta": {
        "name": "Total Expenses (YTD)",
        "content": "{\"type\":\"sum\",\"fontSize\":20}",
        "timeFrame": {
          "start": "2024-01-01",
          "end": "2024-12-31",
          "mode": "yearToDate"
        },
        "conditions": [
          {
            "field": "amount",
            "op": "lt",
            "value": 0
          },
          {
            "field": "account",
            "op": "onBudget",
            "value": ""
          },
          {
            "field": "transfer",
            "op": "is",
            "value": false
          }
        ],
        "conditionsOp": "and"
      }
    },
    {
      "type": "summary-card",
      "width": 3,
      "height": 2,
      "x": 6,
      "y": 0,
      "meta": {
        "name": "Avg Per Month",
        "content": "{\"type\":\"avgPerMonth\",\"fontSize\":20}",
        "timeFrame": {
          "start": "2024-01-01",
          "end": "2024-12-31",
          "mode": "yearToDate"
        },
        "conditions": [
          {
            "field": "amount",
            "op": "lt",
            "value": 0
          },
          {
            "field": "account",
            "op": "onBudget",
            "value": ""
          },
          {
            "field": "transfer",
            "op": "is",
            "value": false
          }
        ],
        "conditionsOp": "and"
      }
    },
    {
      "type": "summary-card",
      "width": 3,
      "height": 2,
      "x": 9,
      "y": 0,
      "meta": {
        "name": "Avg Per Transaction",
        "content": "{\"type\":\"avgPerTransact\",\"fontSize\":20}",
        "timeFrame": {
          "start": "2024-01-01",
          "end": "2024-12-31",
          "mode": "yearToDate"
        },
        "conditions": [
          {
            "field": "amount",
            "op": "lt",
            "value": 0
          },
          {
            "field": "account",
            "op": "onBudget",
            "value": ""
          },
          {
            "field": "transfer",
            "op": "is",
            "value": false
          }
        ],
        "conditionsOp": "and"
      }
    },
    {
      "type": "net-worth-card",
      "width": 6,
      "height": 2,
      "x": 0,
      "y": 2,
      "meta": null
    },
    {
      "type": "cash-flow-card",
      "width": 6,
      "height": 2,
      "x": 6,
      "y": 2,
      "meta": null
    },
    {
      "type": "spending-card",
      "width": 4,
      "height": 2,
      "x": 0,
      "y": 5,
      "meta": {
        "name": "This Month",
        "mode": "single-month"
      }
    },
    {
      "type": "spending-card",
      "width": 4,
      "height": 2,
      "x": 4,
      "y": 5,
      "meta": {
        "name": "Budget Overview",
        "mode": "budget"
      }
    },
    {
      "type": "spending-card",
      "width": 4,
      "height": 2,
      "x": 8,
      "y": 5,
      "meta": {
        "name": "3-Month Average",
        "mode": "average"
      }
    },
    {
      "type": "calendar-card",
      "width": 8,
      "height": 4,
      "x": 0,
      "y": 8,
      "meta": {
        "name": "Transaction Calendar",
        "timeFrame": {
          "start": "2024-01-01",
          "end": "2024-03-31",
          "mode": "sliding-window"
        },
        "conditions": [
          {
            "field": "transfer",
            "op": "is",
            "value": false
          }
        ],
        "conditionsOp": "and"
      }
    },
    {
      "type": "summary-card",
      "width": 4,
      "height": 2,
      "x": 8,
      "y": 8,
      "meta": {
        "name": "Recent Net Worth Change",
        "content": "{\"type\":\"sum\",\"fontSize\":32}",
        "timeFrame": {
          "start": "2024-01-01",
          "end": "2024-03-31",
          "mode": "sliding-window"
        },
        "conditions": [],
        "conditionsOp": "and"
      }
    },
    {
      "type": "markdown-card",
      "width": 4,
      "height": 2,
      "x": 8,
      "y": 10,
      "meta": {
        "content": "## Dashboard Tips\n\nYou can add new widgets or edit existing widgets by using the buttons at the top of the page. Choose a widget type and customize it to fit your needs.\n\n**Moving cards:** Drag any card by its header to reposition it.\n\n**Deleting cards:** Click the three-dot menu on any card and select \"Remove\"."
      }
    }
  ]
  """
