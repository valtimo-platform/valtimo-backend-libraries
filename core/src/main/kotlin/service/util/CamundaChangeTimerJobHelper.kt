package service.util

import mu.KotlinLogging
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.Job
import java.util.Date

class CamundaChangeTimerJobHelper {

    companion object {

        fun changeTimerJob(
            execution: DelegateExecution,
            timerEventId: String,
            newDueDate: Date
        ) {
            val engine = execution.processEngine
            val processInstanceId = execution.processInstanceId

            val timerJob = getTimerJobByProcessInstanceId(
                engine = engine,
                processInstanceId = processInstanceId,
                timerEventId = timerEventId
            )

            updateJobDueDate(
                timerJob = timerJob,
                engine = engine,
                newDueDate = newDueDate
            ).also {
                logger.debug {
                    "Changed the date of timer ${timerJob.id} to $newDueDate " +
                        "for process instance ${timerJob.processInstanceId}"
                }
            }
        }

        private fun getTimerJobByProcessInstanceId(
            engine: ProcessEngine,
            processInstanceId: String,
            timerEventId: String
        ): Job = engine.managementService
            .createJobQuery()
            .timers()
            .processInstanceId(processInstanceId)
            .activityId(timerEventId)
            .singleResult()
            ?: throw ProcessEngineException(
                "No job found for process with Id $processInstanceId"
            )

        private fun updateJobDueDate(
            timerJob: Job,
            engine: ProcessEngine,
            newDueDate: Date
        ) {
            engine.managementService.setJobDuedate(timerJob.id, newDueDate)
        }

        private val logger = KotlinLogging.logger { }
    }
}