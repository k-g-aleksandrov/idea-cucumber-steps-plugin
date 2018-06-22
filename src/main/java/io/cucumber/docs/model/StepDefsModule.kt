package io.cucumber.docs.model

import java.util.*

class StepDefsModule(val name: String) {

    var files: SortedMap<String, StepDefsClass> = sortedMapOf()

    fun addStepDefinition(stepDef: StepDefEntry) {
        if (files[stepDef.file] == null) {
            files[stepDef.file] = StepDefsClass(stepDef.file)
        }
        files[stepDef.file]!!.addStepDefinition(stepDef)
    }
}
