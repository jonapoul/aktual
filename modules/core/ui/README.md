# core:ui

<table>
<tr><th colspan='2'>Legend</th></tr>
<tr><td style='text-align:center;'>DI</td><td style='text-align:center; background-color:#FCB103; color:black'>module-name</td></tr>
<tr><td style='text-align:center;'>Compose</td><td style='text-align:center; background-color:#FFFF55; color:black'>module-name</td></tr>
<tr><td style='text-align:center;'>Multiplatform</td><td style='text-align:center; background-color:#9D8DF1; color:black'>module-name</td></tr>
</table>

```mermaid
graph TD
classDef titleStyle fill:none,stroke:none,font-size:24px,font-weight:bold
budgetmodel["budget:model"]
coredi["core:di"]
coremodel["core:model"]
coreui["core:ui"]
l10n["l10n"]
style budgetmodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coredi fill:#FCB103,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coremodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coreui fill:#FFFF55,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style l10n fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
coreui --> budgetmodel
coreui -.-> coredi
coreui --> coremodel
coreui -.-> l10n
```
