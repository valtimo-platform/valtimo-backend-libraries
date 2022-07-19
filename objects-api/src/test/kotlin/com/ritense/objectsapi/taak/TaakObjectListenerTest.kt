package com.ritense.objectsapi.taak

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.connector.domain.Connector
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.domain.GenericObject
import com.ritense.objectsapi.domain.ObjectRecord
import com.ritense.objectsapi.domain.request.HandleNotificationRequest
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.objectsapi.opennotificaties.OpenNotificationEvent
import com.ritense.objectsapi.service.ObjectTypeConfig
import com.ritense.objectsapi.service.ObjectsApiConnector
import com.ritense.objectsapi.service.ObjectsApiProperties
import com.ritense.openzaak.service.ZaakService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.OpenZaakService
import com.ritense.valtimo.service.BpmnModelService
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class TaakObjectListenerTest {

    lateinit var listener: TaakObjectListener
    lateinit var openNotificatieService: OpenNotificatieService
    lateinit var camundaTaskService: CamundaTaskService
    lateinit var valueResolverService: ValueResolverService
    lateinit var bpmnModelService: BpmnModelService
    lateinit var runtimeService: RuntimeService
    lateinit var documentService: DocumentService
    lateinit var processDocumentService: ProcessDocumentService
    lateinit var zaakService: ZaakService
    lateinit var openZaakService: OpenZaakService

    @BeforeEach
    fun setup() {
        openNotificatieService = mock()
        camundaTaskService = mock()
        valueResolverService = mock()
        bpmnModelService = mock()
        runtimeService = mock()
        documentService = mock()
        processDocumentService = mock()
        zaakService = mock()
        openZaakService = mock()
        listener = TaakObjectListener(
            openNotificatieService,
            camundaTaskService,
            valueResolverService,
            bpmnModelService,
            runtimeService,
            documentService,
            processDocumentService,
            zaakService,
            openZaakService,
        )
    }

    @Test
    fun `notificationReceived should handle taakobject notification`() {
        val connector = mock<TaakObjectConnector>()
        whenever(openNotificatieService.findConnector("123", "key")).thenReturn(connector)

        val objectsApiConnector = mock<ObjectsApiConnector>()
        whenever(connector.getObjectsApiConnector()).thenReturn(objectsApiConnector)
        whenever(objectsApiConnector.getProperties()).thenReturn(ObjectsApiProperties(
            objectType = ObjectTypeConfig(
                url = "http://example.com/some-url"
            )
        ))

        whenever(connector.getTaakObjectRecord(UUID.fromString("321f370a-b8cc-4286-91d8-2fd293796b4c"))).thenReturn(
            GenericObject(
                UUID.randomUUID(), "http://example.com", "taak", ObjectRecord(
                    1, "2022-01-01", TaakObjectDto(
                        bsn = "bsn",
                        kvk = "kvk",
                        verwerkerTaakId = UUID.fromString("0155b054-ceb1-42ab-888b-c522b203685e"),
                        formulierId = "some-form",
                        status = TaakObjectStatus.ingediend
                    )
                )
            )
        )

        val event = OpenNotificationEvent(
            HandleNotificationRequest(
                "objecten",
                "http://some-url/api/321f370a-b8cc-4286-91d8-2fd293796b4c",
                "update",
                mapOf(
                    "objectType" to "http://example.com/some-url"
                )
            ),
            "123",
            "key"
        )

        listener.notificationReceived(event)

        verify(camundaTaskService).completeTaskWithoutFormData("0155b054-ceb1-42ab-888b-c522b203685e")
        verify(connector).modifyTaakObjectStatusVerwerkt(any())
    }

    @Test
    fun `notificationReceived not handle notification for other kanaal`() {
        val event = OpenNotificationEvent(
            HandleNotificationRequest(
                "some-invalid-kanaal",
                "http://some-url/api/321f370a-b8cc-4286-91d8-2fd293796b4c",
                "update",
                mapOf(
                    "objectType" to "http://some-url/some-type"
                )
            ),
            "123",
            "key"
        )

        listener.notificationReceived(event)

        verify(camundaTaskService, never()).completeTaskWithoutFormData("0155b054-ceb1-42ab-888b-c522b203685e")
    }

    @Test
    fun `notificationReceived not handle notification that is not edit action`() {
        val event = OpenNotificationEvent(
            HandleNotificationRequest(
                "objecten",
                "http://some-url/api/321f370a-b8cc-4286-91d8-2fd293796b4c",
                "create",
                mapOf(
                    "objectType" to "http://some-url/some-type"
                )
            ),
            "123",
            "key"
        )

        listener.notificationReceived(event)

        verify(camundaTaskService, never()).completeTaskWithoutFormData("0155b054-ceb1-42ab-888b-c522b203685e")
    }

    @Test
    fun `notificationReceived should not handle notification for other connector`() {
        val connector = mock<Connector>()
        whenever(openNotificatieService.findConnector("123", "key")).thenReturn(connector)

        val event = OpenNotificationEvent(
            HandleNotificationRequest(
                "objecten",
                "http://some-url/api/321f370a-b8cc-4286-91d8-2fd293796b4c",
                "update",
                mapOf(
                    "objectType" to "http://some-url/some-type"
                )
            ),
            "123",
            "key"
        )

        listener.notificationReceived(event)

        verify(camundaTaskService, never()).completeTaskWithoutFormData("0155b054-ceb1-42ab-888b-c522b203685e")
    }

    @Test
    fun `notificationReceived should not handle notification for other object type`() {
        val connector = mock<TaakObjectConnector>()
        whenever(openNotificatieService.findConnector("123", "key")).thenReturn(connector)

        val objectsApiConnector = mock<ObjectsApiConnector>()
        whenever(connector.getObjectsApiConnector()).thenReturn(objectsApiConnector)
        whenever(objectsApiConnector.getProperties()).thenReturn(ObjectsApiProperties(
            objectType = ObjectTypeConfig(
                title = "some-other-type"
            )
        ))

        val event = OpenNotificationEvent(
            HandleNotificationRequest(
                "objecten",
                "http://some-url/api/321f370a-b8cc-4286-91d8-2fd293796b4c",
                "update",
                mapOf(
                    "objectType" to "http://some-url/some-type"
                )
            ),
            "123",
            "key"
        )

        listener.notificationReceived(event)

        verify(camundaTaskService, never()).completeTaskWithoutFormData("0155b054-ceb1-42ab-888b-c522b203685e")
        verify(connector, never()).deleteTaakObject(UUID.fromString("321f370a-b8cc-4286-91d8-2fd293796b4c"))
    }

    @Test
    fun `notificationReceived should not handle object when status is not ingediend`() {
        val connector = mock<TaakObjectConnector>()
        whenever(openNotificatieService.findConnector("123", "key")).thenReturn(connector)

        val objectsApiConnector = mock<ObjectsApiConnector>()
        whenever(connector.getObjectsApiConnector()).thenReturn(objectsApiConnector)
        whenever(objectsApiConnector.getProperties()).thenReturn(ObjectsApiProperties(
            objectType = ObjectTypeConfig(
                title = "some-type"
            )
        ))

        whenever(connector.getTaakObjectRecord(UUID.fromString("321f370a-b8cc-4286-91d8-2fd293796b4c"))).thenReturn(
            GenericObject(
                UUID.randomUUID(), "http://example.com", "taak", ObjectRecord(
                    1, "2022-01-01", TaakObjectDto(
                        bsn = "bsn",
                        kvk = "kvk",
                        verwerkerTaakId = UUID.fromString("0155b054-ceb1-42ab-888b-c522b203685e"),
                        formulierId = "some-form",
                        status = TaakObjectStatus.ingediend
                    )
                )
            )
        )

        val event = OpenNotificationEvent(
            HandleNotificationRequest(
                "objecten",
                "http://some-url/api/321f370a-b8cc-4286-91d8-2fd293796b4c",
                "update",
                mapOf(
                    "objectType" to "http://some-url/some-type"
                )
            ),
            "123",
            "key"
        )

        listener.notificationReceived(event)

        verify(camundaTaskService, never()).completeTaskWithoutFormData("0155b054-ceb1-42ab-888b-c522b203685e")
        verify(connector, never()).deleteTaakObject(UUID.fromString("321f370a-b8cc-4286-91d8-2fd293796b4c"))
    }
}