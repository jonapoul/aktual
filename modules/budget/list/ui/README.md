# budget:list:ui

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
budgetlistui["budget:list:ui"]
budgetlistvm["budget:list:vm"]
budgetmodel["budget:model"]
codegenannotation["codegen:annotation"]
coredi["core:di"]
coremodel["core:model"]
coreui["core:ui"]
l10n["l10n"]
logging["logging"]
prefs["prefs"]
style accountmodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style apiactual fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetlistui fill:#FFFF55,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetlistvm fill:#F5A6A6,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetmodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
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
budgetlistui -.-> accountmodel
budgetlistui --> budgetlistvm
budgetlistui -.-> budgetmodel
budgetlistui --> coreui
budgetlistui -.-> l10n
budgetlistvm -.-> accountmodel
budgetlistvm -.-> apiactual
budgetlistvm --> budgetmodel
budgetlistvm --> coredi
budgetlistvm --> coremodel
budgetlistvm -.-> logging
budgetlistvm -.-> prefs
coreui --> budgetmodel
coreui -.-> coredi
coreui --> coremodel
coreui -.-> l10n
prefs --> accountmodel
prefs --> coremodel
```
