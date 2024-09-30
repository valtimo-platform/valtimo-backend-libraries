package com.ritense.gzac.fvm

import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.springframework.stereotype.Component

@Component
class TestViewModelLoader : ViewModelLoader<TestViewModel> {
    override fun load(task: CamundaTask?): TestViewModel = TestViewModel("test")

    override fun getFormName(): String = "fvm-test"
}