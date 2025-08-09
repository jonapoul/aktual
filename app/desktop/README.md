# app:desktop

<table>
<tr><th colspan='2'>Legend</th></tr>
<tr><td style='text-align:center;'>App</td><td style='text-align:center; background-color:#FF5555; color:black'>module-name</td></tr>
<tr><td style='text-align:center;'>DI</td><td style='text-align:center; background-color:#FCB103; color:black'>module-name</td></tr>
<tr><td style='text-align:center;'>Multiplatform</td><td style='text-align:center; background-color:#9D8DF1; color:black'>module-name</td></tr>
</table>

```mermaid
graph TD
classDef titleStyle fill:none,stroke:none,font-size:24px,font-weight:bold
accountmodel["account:model"]
apibuilder["api:builder"]
apigithub["api:github"]
appdesktop["app:desktop"]
appdi["app:di"]
budgetmodel["budget:model"]
codegenannotation["codegen:annotation"]
coredi["core:di"]
coremodel["core:model"]
logging["logging"]
prefs["prefs"]
style accountmodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style apibuilder fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style apigithub fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style appdesktop fill:#FF5555,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style appdi fill:#FCB103,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style budgetmodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style codegenannotation fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coredi fill:#FCB103,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coremodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style logging fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style prefs fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
appdesktop -.-> appdi
appdi -.-> apibuilder
appdi --> apigithub
appdi --> budgetmodel
appdi --> coredi
appdi --> coremodel
appdi --> prefs
apibuilder --> coremodel
apibuilder -.-> logging
apigithub -.-> codegenannotation
apigithub --> coremodel
prefs --> accountmodel
prefs --> coremodel
```
