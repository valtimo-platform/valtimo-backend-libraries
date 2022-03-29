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

package com.ritense.valtimo.service;

import com.ritense.valtimo.contract.exception.DocumentParserException;
import com.ritense.valtimo.contract.exception.ProcessNotFoundException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProcessShortTimerService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessShortTimerService.class);
    public static final String NAMESPACE_URL_BPMN = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    public static final String NAMESPACE_URL_BPMNDI = "http://www.omg.org/spec/BPMN/20100524/DI";
    private final RepositoryService repositoryService;

    public ProcessShortTimerService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void modifyAndDeployShortTimerVersion(String processDefinitionId) throws ProcessNotFoundException, DocumentParserException {
        Document doc = processModelChangeTimersToOneMinute(processDefinitionId);
        deployProcessShortTimerVersion(modifiedDocumentToInputStream(doc));
    }

    protected Document processModelChangeTimersToOneMinute(String processDefinitionId) throws ProcessNotFoundException, DocumentParserException {
        InputStream processModel = repositoryService.getProcessModel(processDefinitionId);

        if (processModel == null) {
            throw new ProcessNotFoundException("Process not found for: " + processDefinitionId);
        }
        Document doc = createDocumentFrom(processModel);
        doc = timerEventCycleSetOneMinuteOneCycle(doc);
        doc = timerEventDurationSetOneMinute(doc);
        doc = changeNameAndIdToTimerVersion(doc);
        doc = changeBpmnPlaneElementName(doc);
        return doc;
    }

    private Document createDocumentFrom(InputStream processModel) throws DocumentParserException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc;
        try {
            docBuilder = dbFactory.newDocumentBuilder();
            doc = docBuilder.parse(new InputSource(processModel));
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.error("Something went wrong while parsing the document");
            throw new DocumentParserException("Something went wrong while parsing the document");
        }
        return doc;
    }

    private Document timerEventDurationSetOneMinute(Document doc) {
        NodeList nodes = getNodeList(doc, NAMESPACE_URL_BPMN, "timeDuration");
        String oneMinuteDurationNotation = "PT1M";
        alterNodes(nodes, oneMinuteDurationNotation);
        return doc;
    }

    private Document timerEventCycleSetOneMinuteOneCycle(Document doc) {
        NodeList nodes = getNodeList(doc, NAMESPACE_URL_BPMN, "timeCycle");
        String oneMinuteCycleNotation = "R1/PT1M";
        alterNodes(nodes, oneMinuteCycleNotation);
        return doc;
    }

    private Document changeNameAndIdToTimerVersion(Document doc) {
        NodeList nodes = getNodeList(doc, NAMESPACE_URL_BPMN, "process");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String id = element.getAttribute("id");
            String name = element.getAttribute("name");
            element.setAttribute("id", id + "-short-timer-version");
            element.setAttribute("name", name + " Short Timer Version");
        }
        return doc;
    }

    private Document changeBpmnPlaneElementName(Document doc) {
        NodeList nodes = getNodeList(doc, NAMESPACE_URL_BPMNDI, "BPMNPlane");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String planeName = element.getAttribute("bpmnElement");
            element.setAttribute("bpmnElement", planeName + "-short-timer-version");
        }
        return doc;
    }

    private InputStream modifiedDocumentToInputStream(Document doc) throws DocumentParserException {
        InputStream processModelTimers;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(doc);
            Result outputTarget = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            processModelTimers = new ByteArrayInputStream(outputStream.toByteArray());
        } catch (TransformerException ex) {
            throw new DocumentParserException("Not able to transform xmlSource");
        }
        return processModelTimers;
    }

    private void deployProcessShortTimerVersion(InputStream inputStream) {
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addInputStream("valtimo-testproces-test.bpmn", inputStream).name("Timers set to one minute");
        deploymentBuilder.deploy();
    }

    private NodeList getNodeList(Document doc, String nameSpaceUrl, String localName) {
        return doc.getDocumentElement().getElementsByTagNameNS(nameSpaceUrl, localName);
    }

    private void alterNodes(NodeList nodes, String newValue) {
        Node node;
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            node.setTextContent(newValue);
        }
    }

}