#/bin/bash

for f in ./*.json
do
    jq '[.[] | {
            activityId: .formLinkElementId,
            activityType: ("bpmn:" + (if .formAssociationType == "start-event" then "StartEvent" else "UserTask" end) + ":start"),
            processLinkType: (if has("formName") then "form" else "form-flow" end),
            formDefinitionName: .formName,
            formFlowDefinitionId: .formFlowName
        }] | del(.. | nulls)' $f > $(basename $f).processlink.json
done