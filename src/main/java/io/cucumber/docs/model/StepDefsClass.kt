package io.cucumber.docs.model

class StepDefsClass(val name: String) {

    val stepsCount
        get() = stepDefs.size

    var stepDefs: MutableList<StepDefEntry> = mutableListOf()

    fun addStepDefinition(stepDef: StepDefEntry) = stepDefs.add(stepDef)
}

