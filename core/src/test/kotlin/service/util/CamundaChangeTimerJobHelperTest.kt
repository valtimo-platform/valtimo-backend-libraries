package service.util

import org.camunda.bpm.engine.ManagementService
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.Job
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import java.time.ZonedDateTime
import java.util.Date

const val DOCUMENT_ID = "6218600e-5012-4320-9ca6-40fd58e0a297"
private const val PROCESS_INSTANCE_ID = "df08455f-71ee-4637-b736-81cbd4bd551c"
private const val CURRENT_DUE_DATE = "2022-07-15T00:00:00+02:00"
private const val NEW_DUE_DATE = "2022-07-22T00:00:00+02:00"
private const val TIMER_EVENT_ID = "TestTimer1Event"

@MockitoSettings(strictness = Strictness.LENIENT)
internal class CamundaChangeTimerJobHelperTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var managementService: ManagementService

    @Mock
    lateinit var job: Job

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var processEngine: ProcessEngine

    private lateinit var execution: DelegateExecution

    @BeforeEach
    fun setup() {
        execution = delegateExecution()
    }

    @Test
    fun `should retrieve timer and call setJobDuedate method` () {
        //given

        whenever(managementService.createJobQuery()
            .timers()
            .processInstanceId(any())
            .activityId(any())
            .singleResult())
            .thenReturn(job)

        //when
        CamundaChangeTimerJobHelper.changeTimerJob(
            execution = execution,
            timerEventId = TIMER_EVENT_ID,
            newDueDate = Date.from(ZonedDateTime.parse(NEW_DUE_DATE).toInstant())
        )

        //then
        verify(processEngine.managementService, times(1)).setJobDuedate(eq(null), any())
    }

    private fun delegateExecution(): DelegateExecution {
        return DelegateExecutionFake()
            .withBusinessKey(DOCUMENT_ID)
            .withProcessInstanceId(PROCESS_INSTANCE_ID)
            .withProcessEngine(processEngine)
            .withVariables(
                mapOf(
                    "newUitersteInschrijfdatum" to CURRENT_DUE_DATE,
                    "uitersteInschrijfdatum" to CURRENT_DUE_DATE
                )
            )
    }
}