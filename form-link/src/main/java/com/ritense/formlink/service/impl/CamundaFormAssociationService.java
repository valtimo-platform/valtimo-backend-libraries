/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.formlink.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.service.DocumentService;
import com.ritense.form.domain.FormDefinition;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.service.FormDefinitionService;
import com.ritense.formlink.domain.FormAssociation;
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociationId;
import com.ritense.formlink.domain.impl.formassociation.FormAssociationFactory;
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;
import com.ritense.formlink.domain.impl.formassociation.FormAssociations;
import com.ritense.formlink.domain.impl.formassociation.Mapper;
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink;
import com.ritense.formlink.domain.request.CreateFormAssociationRequest;
import com.ritense.formlink.domain.request.FormLinkRequest;
import com.ritense.formlink.domain.request.ModifyFormAssociationRequest;
import com.ritense.formlink.repository.ProcessFormAssociationRepository;
import com.ritense.formlink.service.FormAssociationService;
import com.ritense.formlink.service.SubmissionTransformerService;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.valtimo.contract.form.FormFieldDataResolver;
import com.ritense.valtimo.service.CamundaProcessService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.ritense.form.domain.FormIoFormDefinition.PROCESS_VAR_PREFIX;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@RequiredArgsConstructor
public class CamundaFormAssociationService implements FormAssociationService {

    private final FormDefinitionService formDefinitionService;
    private final ProcessFormAssociationRepository processFormAssociationRepository;
    private final DocumentService documentService;
    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final CamundaProcessService camundaProcessService;
    private final TaskService taskService;
    private final SubmissionTransformerService submissionTransformerService;
    private final List<FormFieldDataResolver> formFieldDataResolvers;

    @Override
    public Set<CamundaFormAssociation> getAllFormAssociations(String processDefinitionKey) {
        return processFormAssociationRepository.findByProcessDefinitionKey(processDefinitionKey)
            .map(CamundaProcessFormAssociation::getFormAssociations).orElseThrow();
    }

    @Override
    public Optional<CamundaFormAssociation> getFormAssociationById(String processDefinitionKey, UUID id) {
        return processFormAssociationRepository.findByProcessDefinitionKey(processDefinitionKey)
            .flatMap(camundaProcessFormAssociation ->
                camundaProcessFormAssociation
                    .getFormAssociations()
                    .stream()
                    .filter(camundaFormAssociation -> camundaFormAssociation.getId().equals(id))
                    .findFirst()
            );
    }

    @Override
    public Optional<CamundaFormAssociation> getFormAssociationByFormLinkId(String processDefinitionKey, String formLinkId) {
        return processFormAssociationRepository.findByProcessDefinitionKey(processDefinitionKey)
            .flatMap(camundaProcessFormAssociation ->
                camundaProcessFormAssociation
                    .getFormAssociations()
                    .stream()
                    .filter(camundaFormAssociation -> camundaFormAssociation.getFormLink().getId().equals(formLinkId))
                    .findFirst()
            );
    }

    @Override
    public Optional<JsonNode> getFormDefinitionByFormLinkId(String processDefinitionKey, String formLinkId) {
        return getFormAssociationByFormLinkId(processDefinitionKey, formLinkId)
            .flatMap(camundaFormAssociation -> Optional.of(buildFormDefinition(camundaFormAssociation)));
    }

    public Optional<CamundaFormAssociation> getStartEventFormDefinitionByProcessDefinitionKey(String processDefinitionKey) {
        Predicate<? super CamundaFormAssociation> filter = camundaFormAssociation ->
            camundaFormAssociation instanceof StartEventFormAssociation;
        return getStartEventFormDefinitionByProcessDefinitionKeyAndFilter(processDefinitionKey, filter);
    }

    public Optional<JsonNode> getStartEventFormDefinition(String processDefinitionKey) {
        return getStartEventFormDefinitionByProcessDefinitionKey(processDefinitionKey).map(this::buildFormDefinition);
    }

    private Optional<CamundaFormAssociation> getStartEventFormDefinitionByProcessDefinitionKeyAndFilter(
        String processDefinitionKey,
        Predicate<? super CamundaFormAssociation> filter
    ) {
        return processFormAssociationRepository.findByProcessDefinitionKey(processDefinitionKey)
            .flatMap(camundaProcessFormAssociation ->
                camundaProcessFormAssociation
                    .getFormAssociations()
                    .stream()
                    .filter(filter)
                    .findFirst()
            );
    }

    @Override
    public Optional<JsonNode> getPreFilledFormDefinitionByFormLinkId(
        Document.Id documentId,
        String processDefinitionKey,
        String formLinkId
    ) {
        assertArgumentNotNull(documentId, "documentId is required");
        assertArgumentNotNull(processDefinitionKey, "processDefinitionKey is required");
        assertArgumentNotNull(formLinkId, "formLinkId is required");
        return getFormAssociationByFormLinkId(
            processDefinitionKey,
            formLinkId
        ).flatMap(getPrefilledFormDefinitionFromFormAssociation(Optional.of(documentId), Optional.empty()));
    }

    @Override
    public Optional<JsonNode> getPreFilledFormDefinitionByFormLinkId(
        Document.Id documentId,
        String processDefinitionKey,
        String formLinkId,
        String taskInstanceId
    ) {
        return getPreFilledFormDefinitionByFormLinkId(
            processDefinitionKey,
            formLinkId,
            Optional.ofNullable(documentId),
            Optional.ofNullable(taskInstanceId));
    }

    @Override
    public Optional<JsonNode> getPreFilledFormDefinitionByFormLinkId(
        String processDefinitionKey,
        String formLinkId,
        Optional<Document.Id> documentId,
        Optional<String> taskInstanceId
    ) {
        assertArgumentNotNull(processDefinitionKey, "processDefinitionKey is required");
        assertArgumentNotNull(formLinkId, "formLinkId is required");

        return getFormAssociationByFormLinkId(
            processDefinitionKey,
            formLinkId
        ).flatMap(getPrefilledFormDefinitionFromFormAssociation(documentId, taskInstanceId));
    }

    //Note: candidate for refactor. Using submission object approach
    private Function<CamundaFormAssociation, Optional<? extends JsonNode>> getPrefilledFormDefinitionFromFormAssociation(
        Optional<Document.Id> documentId,
        Optional<String> taskInstanceId
    ) {
        return camundaFormAssociation -> {
            final var formLink = camundaFormAssociation.getFormLink();

            if (formLink.includeFormDefinition()) {
                return getFormDefinitionByAssociation(camundaFormAssociation, documentId, taskInstanceId);
            } else {
                return getSimpleForm(camundaFormAssociation);
            }
        };
    }

    @Override
    @Transactional
    public Optional<JsonNode> getPreFilledFormDefinitionByFormKey(String formKey, Optional<Document.Id> documentId) {
        return formDefinitionService.getFormDefinitionByName(formKey).map(formDefinition -> getFormDefinition(
            (FormIoFormDefinition) formDefinition,
            Optional.empty(),
            documentId,
            Optional.empty()
        ));
    }

    @Override
    @Transactional
    public FormAssociation createFormAssociation(
        String processDefinitionKey,
        String formName,
        String formLinkElementId,
        FormAssociationType type
    ) {
        final FormDefinition formDefinition = formDefinitionService.getFormDefinitionByName(formName).orElseThrow();
        final FormLinkRequest formLinkRequest = new FormLinkRequest(
            formLinkElementId,
            type,
            formDefinition.getId(),
            null,
            null
        );
        return createFormAssociation(new CreateFormAssociationRequest(processDefinitionKey, formLinkRequest));
    }

    @Override
    @Transactional
    public FormAssociation createFormAssociation(
        String processDefinitionKey,
        String formName,
        String formLinkElementId,
        FormAssociationType type,
        boolean isPublic
    ) {
        return createFormAssociation(processDefinitionKey, formName, formLinkElementId, type);
    }

    @Override
    @Transactional
    public CamundaFormAssociation createFormAssociation(CreateFormAssociationRequest request) {
        final UUID formAssociationId = UUID.randomUUID();
        final CamundaFormAssociation formAssociation = FormAssociationFactory.getFormAssociation(
            formAssociationId,
            request.getFormLinkRequest().getType(),
            request.getFormLinkRequest().getId(),
            request.getFormLinkRequest().getFormId(),
            request.getFormLinkRequest().getCustomUrl(),
            request.getFormLinkRequest().getAngularStateUrl()
        );
        if (formAssociation.getFormLink() instanceof BpmnElementFormIdLink) {
            if (!formDefinitionService.formDefinitionExistsById(formAssociation.getFormLink().getFormId())) {
                throw new RuntimeException("Form definition not found with id " + request.getFormLinkRequest().getFormId());
            }
        }
        processFormAssociationRepository.findByProcessDefinitionKey(request.getProcessDefinitionKey())
            .ifPresentOrElse(processFormAssociation -> {
                    processFormAssociation.addFormAssociation(formAssociation);
                    processFormAssociationRepository.saveAndFlush(processFormAssociation);
                },
                () -> {
                    final var camundaProcessFormAssociationId = CamundaProcessFormAssociationId.newId(UUID.randomUUID());
                    final var formAssociations = new FormAssociations();
                    formAssociations.add(formAssociation);

                    final var camundaProcessFormAssociation = new CamundaProcessFormAssociation(
                        camundaProcessFormAssociationId,
                        request.getProcessDefinitionKey(),
                        formAssociations
                    );
                    processFormAssociationRepository.save(camundaProcessFormAssociation);
                }
            );
        return formAssociation;
    }

    @Override
    @Transactional
    public CamundaFormAssociation modifyFormAssociation(ModifyFormAssociationRequest request) {
        final CamundaFormAssociation formAssociation = FormAssociationFactory.getFormAssociation(
            request.getFormAssociationId(),
            request.getFormLinkRequest().getType(),
            request.getFormLinkRequest().getId(),
            request.getFormLinkRequest().getFormId(),
            request.getFormLinkRequest().getCustomUrl(),
            request.getFormLinkRequest().getAngularStateUrl()
        );
        if (formAssociation.getFormLink() instanceof BpmnElementFormIdLink) {
            if (!formDefinitionService.formDefinitionExistsById(formAssociation.getFormLink().getFormId())) {
                throw new RuntimeException("Form definition not found with id " + request.getFormLinkRequest().getFormId());
            }
        }
        final var camundaProcessFormAssociation = processFormAssociationRepository
            .findByProcessDefinitionKey(request.getProcessDefinitionKey()).orElseThrow();
        camundaProcessFormAssociation.updateFormAssociation(formAssociation);
        processFormAssociationRepository.save(camundaProcessFormAssociation);
        return formAssociation;
    }

    @Override
    @Transactional
    public CamundaFormAssociation upsertFormAssociation(String processDefinitionKey, FormLinkRequest formLinkRequest) {
        final var formAssociation = getFormAssociationByFormLinkId(
            processDefinitionKey, formLinkRequest.getId()
        );
        return formAssociation.map(
            camundaFormAssociation -> modifyFormAssociation(new ModifyFormAssociationRequest(
                    processDefinitionKey,
                    formAssociation.get().getId(),
                    formLinkRequest
                )
            )
        ).orElseGet(() -> createFormAssociation(new CreateFormAssociationRequest(
            processDefinitionKey,
            formLinkRequest
        )));
    }

    @Override
    @Transactional
    public void deleteFormAssociation(String processDefinitionKey, UUID formAssociationId) {
        processFormAssociationRepository.findByProcessDefinitionKey(processDefinitionKey)
            .ifPresent(processFormAssociation -> processFormAssociation.removeFormAssociation(formAssociationId));
    }

    private Optional<ObjectNode> getFormDefinitionByAssociation(
        CamundaFormAssociation camundaFormAssociation,
        Optional<Document.Id> documentId,
        Optional<String> taskInstanceId
    ) {
        Optional<FormIoFormDefinition> optionalFormDefinition = (Optional<FormIoFormDefinition>) formDefinitionService
            .getFormDefinitionById(camundaFormAssociation.getFormLink().getFormId());

        return optionalFormDefinition.map(formDefinition -> getFormDefinition(
            formDefinition,
            Optional.of(camundaFormAssociation),
            documentId,
            taskInstanceId
        ));
    }

    private ObjectNode getFormDefinition(
        FormIoFormDefinition formDefinition,
        Optional<CamundaFormAssociation> camundaFormAssociation,
        Optional<Document.Id> documentId,
        Optional<String> taskInstanceId
    ) {
        documentId.ifPresent(id -> prefillForm(camundaFormAssociation, formDefinition, id, taskInstanceId));

        ObjectNode formDefinitionJson = (ObjectNode) formDefinition.getFormDefinition();
        camundaFormAssociation.ifPresent(formAssociation -> appendFormAssociation(formDefinitionJson, formAssociation));
        return formDefinitionJson;
    }

    private void prefillForm(
        Optional<CamundaFormAssociation> camundaFormAssociation,
        FormIoFormDefinition formDefinition,
        Document.Id documentId,
        Optional<String> taskInstanceId
    ) {
        //Metadata
        final JsonSchemaDocument document = (JsonSchemaDocument) documentService.findBy(documentId).orElseThrow();
        final ObjectNode extendedDocumentContent = (ObjectNode) document.content().asJson();
        extendedDocumentContent.set("metadata", buildMetaDataObject(document));

        prefillProcessVariables(formDefinition, document);
        prefillDataResolverFields(formDefinition, document, extendedDocumentContent);
        if (camundaFormAssociation.isPresent() && camundaFormAssociation.get() instanceof UserTaskFormAssociation) {
            taskInstanceId
                .ifPresent(instanceId -> prefillTaskVariables(formDefinition, instanceId, extendedDocumentContent));
        }
    }

    private void prefillProcessVariables(FormIoFormDefinition formDefinition, Document document) {
        final List<String> processVarsNames = formDefinition.extractProcessVarNames();
        final Map<String, Object> processInstanceVariables = new HashMap<>();
        processDocumentAssociationService.findProcessDocumentInstances(document.id())
            .forEach(processDocumentInstance -> processInstanceVariables.putAll(
                camundaProcessService.getProcessInstanceVariables(
                    processDocumentInstance.processDocumentInstanceId().processInstanceId().toString(),
                    processVarsNames
                ))
            );
        if (!processInstanceVariables.isEmpty()) {
            formDefinition.preFillWith(PROCESS_VAR_PREFIX, processInstanceVariables);
        }
    }

    private void prefillDataResolverFields(FormIoFormDefinition formDefinition, JsonSchemaDocument document, JsonNode extendedDocumentContent) {
        //FormFieldDataResolver pre-filling
        formDefinition.buildExternalFormFieldsMap()
            .forEach((externalFormFieldType, externalContentItems) -> formFieldDataResolvers
                .stream()
                .filter(formFieldDataResolver -> formFieldDataResolver.supports(externalFormFieldType))
                .findFirst()
                .ifPresent(
                    formFieldDataResolver -> {
                        String[] varNames = externalContentItems.stream()
                            .map(FormIoFormDefinition.ExternalContentItem::getName).toArray(String[]::new);
                        Map<String, Object> externalDataMap = formFieldDataResolver.get(
                            document.definitionId().name(),
                            document.id().getId(),
                            varNames
                        );
                        formDefinition.preFillWith(externalFormFieldType.name().toLowerCase(), externalDataMap);
                    }
                )
            );
        formDefinition.preFill(extendedDocumentContent);
    }

    private void prefillTaskVariables(FormIoFormDefinition formDefinition, String taskInstanceId, JsonNode extendedDocumentContent) {
        final Map<String, Object> taskVariables = taskService.getVariables(taskInstanceId);
        final ObjectNode placeholders = Mapper.INSTANCE.objectMapper().valueToTree(taskVariables);
        submissionTransformerService.prePreFillTransform(formDefinition, placeholders, extendedDocumentContent);
    }

    private Optional<ObjectNode> getSimpleForm(CamundaFormAssociation camundaFormAssociation) {
        ObjectNode formDefinitionJson = emptyFormDefinition();
        appendFormAssociation(formDefinitionJson, camundaFormAssociation);
        return Optional.of(formDefinitionJson);
    }

    private ObjectNode buildFormDefinition(CamundaFormAssociation camundaFormAssociation) {
        return (ObjectNode) getPrefilledFormDefinitionFromFormAssociation(Optional.empty(), Optional.empty())
            .apply(camundaFormAssociation).orElseThrow();
    }

    private ObjectNode emptyFormDefinition() {
        return JsonNodeFactory.instance.objectNode();
    }

    private void appendFormAssociation(ObjectNode formDefinition, CamundaFormAssociation camundaFormAssociation) {
        formDefinition.set("formAssociation", camundaFormAssociation.toJson());
    }

    private ObjectNode buildMetaDataObject(Document document) {
        final ObjectNode metaDataNode = JsonNodeFactory.instance.objectNode();
        metaDataNode.put("id", document.id().toString());
        metaDataNode.put("createdOn", document.createdOn().toString());
        metaDataNode.put("createdBy", document.createdBy());
        metaDataNode.put("modifiedOn", document.modifiedOn().toString());
        metaDataNode.put("sequence", document.sequence());
        return metaDataNode;
    }
}