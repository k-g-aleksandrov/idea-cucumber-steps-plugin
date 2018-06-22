package io.cucumber.docs

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.ui.content.ContentFactory

class StepDefsDocsToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolContent = ContentFactory.SERVICE.getInstance()
                .createContent(StepDefsDocsToolWindowPanel(project), "", true)

        toolWindow.contentManager.addContent(toolContent)

        toolWindow.setType(ToolWindowType.DOCKED, null)
    }
}