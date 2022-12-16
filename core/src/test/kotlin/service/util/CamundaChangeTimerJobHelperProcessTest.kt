package service.util

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.camunda.bpm.scenario.ProcessScenario
import org.camunda.bpm.scenario.Scenario
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject

private const val PROCESS_DEFINITION_KEY = "camunda-timer-job-test-process"
private const val BUSINESS_KEY = "test"
private const val DUE_DATE_KEY = "dueDate"
private const val NEW_DUE_DATE_KEY = "newDueDate"
private const val FIRST_TIMER_ID = "TestTimer1Event"
private const val SECOND_TIMER_ID = "TestTimer2Event"

@Deployment(resources = ["bpmn/camunda-timer-job-test-process.bpmn"])
@ExtendWith(MockitoExtension::class, ProcessEngineExtension::class)
class CamundaChangeTimerJobHelperProcessTest {

    @Inject
    lateinit var processEngine: ProcessEngine

    @Mock
    lateinit var changeTimerJobProcessScenario: ProcessScenario

    @Test
    fun `should `() {
        val scenario = runScenario()

        // Process will wait at first timer in process
        BpmnAwareTests.assertThat(scenario.instance(changeTimerJobProcessScenario))
            .hasPassedInOrder(
                "StartEvent",
                FIRST_TIMER_ID,
                "EndEvent"
            )
            .isEnded

        // First timer date will be changed to now and process should continue to second timer
        // Which should still be with original date and process will wait
//        CamundaChangeTimerJobHelper.changeTimerJob(
//            execution = execution(scenario),
//            timerEventId = FIRST_TIMER_ID,
//            newDueDate = Date.from(Timestamp.valueOf(LocalDateTime.now()).toInstant())
//        )
//
//        BpmnAwareTests.assertThat(scenario.instance(changeTimerJobProcessScenario))
//            .isNotWaitingAt(
//                FIRST_TIMER_ID
//            )
//            .isWaitingAt(
//                SECOND_TIMER_ID
//            )
//            .isNotEnded
//
//        // Second timer date will be changed to now and process should continue and end
//        CamundaChangeTimerJobHelper.changeTimerJob(
//            execution = execution(scenario),
//            timerEventId = SECOND_TIMER_ID,
//            newDueDate = Date.from(Timestamp.valueOf(LocalDateTime.now()).toInstant())
//        )
//
//        BpmnAwareTests.assertThat(scenario.instance(changeTimerJobProcessScenario))
//            .isNotWaitingAt(
//                SECOND_TIMER_ID
//            )
//            .hasPassed(
//                "EndEvent"
//            )
//            .isEnded
    }

    private fun runScenario(): Scenario =
        Scenario.run(changeTimerJobProcessScenario)
            .startByKey(
                PROCESS_DEFINITION_KEY,
                BUSINESS_KEY,
                mapOf(
                    DUE_DATE_KEY to LocalDateTime.now().plusHours(1).toString(),
                    NEW_DUE_DATE_KEY to LocalDateTime.now().plusSeconds(5).toString()
                )
            )
            .execute()

    private fun execution(scenario: Scenario): DelegateExecution = DelegateExecutionFake()
        .withProcessEngine(processEngine)
        .withProcessInstanceId(scenario.instance(changeTimerJobProcessScenario).processInstanceId.toString()
        )
}