# about:ui

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
aboutdata["about:data"]
aboutui["about:ui"]
aboutvm["about:vm"]
apigithub["api:github"]
budgetmodel["budget:model"]
codegenannotation["codegen:annotation"]
coredi["core:di"]
coremodel["core:model"]
coreui["core:ui"]
l10n["l10n"]
logging["logging"]
style aboutdata fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style aboutui fill:#FFFF55,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style aboutvm fill:#F5A6A6,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style apigithub fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetmodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style codegenannotation fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coredi fill:#FCB103,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coremodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coreui fill:#FFFF55,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style l10n fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style logging fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
aboutdata --> apigithub
aboutui --> aboutvm
aboutui --> coreui
aboutui -.-> l10n
aboutvm --> aboutdata
aboutvm --> coredi
aboutvm -.-> logging
apigithub -.-> codegenannotation
apigithub --> coremodel
coreui --> budgetmodel
coreui -.-> coredi
coreui --> coremodel
coreui -.-> l10n
```
