# about:data

<table>
<tr><th colspan='2'>Legend</th></tr>
<tr><td style='text-align:center;'>Multiplatform</td><td style='text-align:center; background-color:#9D8DF1; color:black'>module-name</td></tr>
</table>

```mermaid
graph TD
classDef titleStyle fill:none,stroke:none,font-size:24px,font-weight:bold
aboutdata["about:data"]
apigithub["api:github"]
codegenannotation["codegen:annotation"]
coremodel["core:model"]
style aboutdata fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style apigithub fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style codegenannotation fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
style coremodel fill:#9D8DF1,stroke:#333,stroke-width:2px,color:black,font-weight:bold
aboutdata --> apigithub
apigithub -.-> codegenannotation
apigithub --> coremodel
```
