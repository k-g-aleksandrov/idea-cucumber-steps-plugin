package io.cucumber.docs.model;

data class StepDefEntry(
        val regex: String,
        val methodName: String,
        val annotation: String,
        val params: List<String>,
        val file: String,
        val module: String
) {
    override fun toString() = regex
}