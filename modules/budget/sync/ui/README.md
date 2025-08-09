# budget:sync:ui

<table>
<tr><th colspan='2'>Legend</th></tr>
<tr><td style='text-align:center;'>ViewModel</td><td style='text-align:center; background-color:#F5A6A6; color:black'>module-name</td></tr>
<tr><td style='text-align:center;'>DI</td><td style='text-align:center; background-color:#FCB103; color:black'>module-name</td></tr>
<tr><td style='text-align:center;'>Compose</td><td style='text-align:center; background-color:#FFFF55; color:black'>module-name</td></tr>
<tr><td style='text-align:center;'>Multiplatform</td><td style='text-align:center; background-color:#9D8DF1; color:black'>module-name</td></tr>
</table>

```mermaid
graph TD
classDef titleStyle fill:none,stroke:none,font-size:24px,font-weight:bold
accountmodel["account:model"]
apiactual["api:actual"]
budgetdata["budget:data"]
budgetdi["budget:di"]
budgetencryption["budget:encryption"]
budgetmodel["budget:model"]
budgetsyncui["budget:sync:ui"]
budgetsyncvm["budget:sync:vm"]
codegenannotation["codegen:annotation"]
coredi["core:di"]
coremodel["core:model"]
coreui["core:ui"]
l10n["l10n"]
logging["logging"]
prefs["prefs"]
style accountmodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style apiactual fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetdata fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetdi fill:#FCB103,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetencryption fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetmodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetsyncui fill:#FFFF55,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetsyncvm fill:#F5A6A6,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style codegenannotation fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coredi fill:#FCB103,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coremodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coreui fill:#FFFF55,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style l10n fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style logging fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style prefs fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
apiactual --> accountmodel
apiactual --> budgetmodel
apiactual -.-> codegenannotation
apiactual --> coremodel
budgetdata --> budgetmodel
budgetdata --> coremodel
budgetdata -.-> logging
budgetdi --> budgetdata
budgetencryption --> accountmodel
budgetencryption --> budgetmodel
budgetencryption --> coremodel
budgetsyncui -.-> accountmodel
budgetsyncui --> budgetsyncvm
budgetsyncui --> coreui
budgetsyncui -.-> l10n
budgetsyncvm -.-> accountmodel
budgetsyncvm -.-> apiactual
budgetsyncvm --> budgetdi
budgetsyncvm --> budgetencryption
budgetsyncvm --> budgetmodel
budgetsyncvm --> coredi
budgetsyncvm -.-> coremodel
budgetsyncvm -.-> logging
budgetsyncvm -.-> prefs
coreui --> budgetmodel
coreui -.-> coredi
coreui --> coremodel
coreui -.-> l10n
prefs --> accountmodel
prefs --> coremodel
```
