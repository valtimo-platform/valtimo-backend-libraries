/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import static com.ritense.valtimo.service.ProcessShortTimerService.NAMESPACE_URL_BPMN;
import static com.ritense.valtimo.service.ProcessShortTimerService.NAMESPACE_URL_BPMNDI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcessShortTimerServiceTest {

    private ProcessShortTimerService processShortTimerService;
    private RepositoryService repositoryService;

    @BeforeEach
    void setUp() {
        repositoryService = mock(RepositoryService.class);
        processShortTimerService = new ProcessShortTimerService(repositoryService);
    }

    @Test
    void processModelChangeTimersToOneMinuteTestWithCorrectId() throws ProcessNotFoundException, DocumentParserException {

        //Given
        FileInputStream fileInputStream = getFileInputStream("camundaProcessModelTest.xml");

        String processDefinitionId = "notUsedSinceMocked";

        //When
        when(repositoryService.getProcessModel(processDefinitionId)).thenReturn(fileInputStream);
        Document document = processShortTimerService.processModelChangeTimersToOneMinute(processDefinitionId);

        //Then
        assertDurationTimersAreSetTo1Minute(document);
        assertCycleTimersAreSetTo1Minute(document);
        assertChangeNameAndIdToTimerVersion(document);
        assertChangeBpmnPlaneElementNameToTimerVersion(document);
    }

    @Test
    void processModelChangeTimersToOneMinuteInvalidId() {
        assertThrows(ProcessNotFoundException.class, () -> processShortTimerService.processModelChangeTimersToOneMinute("0"));
    }

    @Test
    void processModelChangeTimersToOneMinuteNullId() {
        assertThrows(ProcessNotFoundException.class, () -> processShortTimerService.processModelChangeTimersToOneMinute(null));
    }

    @Test
    void whenParsingInvalidXmlShouldThrowException() throws ProcessNotFoundException, DocumentParserException {

        //Given
        FileInputStream fileInputStream = getFileInputStream("invalid.xml");
        String processDefinitionId = "notUsedSinceMocked";

        //When
        when(repositoryService.getProcessModel(processDefinitionId)).thenReturn(fileInputStream);
        assertThrows(DocumentParserException.class, () -> processShortTimerService.processModelChangeTimersToOneMinute(processDefinitionId));
    }

    private void assertDurationTimersAreSetTo1Minute(Document doc) {
        NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(NAMESPACE_URL_BPMN, "timeDuration");

        assertEquals(1, nodes.getLength());

        Node node;
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            assertEquals("PT1M", node.getTextContent());
        }
    }

    private void assertCycleTimersAreSetTo1Minute(Document doc) {
        NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(NAMESPACE_URL_BPMN, "timeCycle");

        assertEquals(1, nodes.getLength());

        Node node;
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            assertEquals("R1/PT1M", node.getTextContent());
        }
    }

    private void assertChangeNameAndIdToTimerVersion(Document doc) {
        NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(NAMESPACE_URL_BPMN, "process");
        Element element;

        assertEquals(1, nodes.getLength());

        for (int i = 0; i < nodes.getLength(); i++) {
            element = (Element) nodes.item(i);
            element.setAttribute("id", "short-timer");
            element.setAttribute("name", "process");
            assertEquals("process", element.getAttribute("name"));
            assertEquals("short-timer", element.getAttribute("id"));
        }
    }

    private void assertChangeBpmnPlaneElementNameToTimerVersion(Document doc) {
        NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(NAMESPACE_URL_BPMNDI, "BPMNPlane");

        assertEquals(1, nodes.getLength());

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String processName = element.getAttribute("bpmnElement");
            element.setAttribute("bpmnElement", processName + "-short-timer-version");
            assertEquals(element.getAttribute("bpmnElement"), processName + "-short-timer-version");
        }
    }

    private FileInputStream getFileInputStream(String fileName) {
        ClassLoader loader = ProcessShortTimerServiceTest.class.getClassLoader();
        File file = new File(loader.getResource(fileName).getFile().replace("%20", " "));
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileInputStream;
    }
}



