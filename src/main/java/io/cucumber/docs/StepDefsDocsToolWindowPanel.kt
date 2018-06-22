package io.cucumber.docs

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.containers.isNullOrEmpty
import io.cucumber.docs.model.StepDefEntry
import io.cucumber.docs.model.StepDefsModule
import io.cucumber.docs.treerenderer.ClassTreeNode
import io.cucumber.docs.treerenderer.ModuleTreeNode
import io.cucumber.docs.treerenderer.ReferenceTreeCellRenderer
import io.cucumber.docs.treerenderer.StepDefTreeNode
import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex
import java.awt.BorderLayout
import java.util.*
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.tree.DefaultMutableTreeNode


class StepDefsTreeContent {

    var stepsCount = 0

    var modules: SortedMap<String, StepDefsModule> = sortedMapOf()

    fun addStepDefinition(stepDef: StepDefEntry) {
        if (modules[stepDef.module] == null) {
            modules[stepDef.module] = StepDefsModule(stepDef.module)
        }
        modules[stepDef.module]!!.addStepDefinition(stepDef)
    }
}

class StepDefsDocsToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private lateinit var scrollPane: JScrollPane

    private val fileEditorManager = FileEditorManager.getInstance(project)

    init {
        dummyTree()
        trackFiles()
    }

    private fun dummyTree() {
        val treeRoot = DefaultMutableTreeNode("Available Steps")
        val tree = Tree(treeRoot)
        this.scrollPane = ScrollPaneFactory.createScrollPane(tree)
        this.add(scrollPane)
    }

    fun recreateTree() {
        this.remove(scrollPane)
        val treeRoot = DefaultMutableTreeNode("Available Steps")
        fillTree(treeRoot)
        val tree = Tree(treeRoot)
        tree.cellRenderer = ReferenceTreeCellRenderer()
        scrollPane = ScrollPaneFactory.createScrollPane(tree)
        this.add(scrollPane)
    }

    private fun fillTree(treeRoot: DefaultMutableTreeNode) {
        val files = fileEditorManager.selectedFiles
        this.remove(scrollPane)

        if (files.isEmpty()) {
            treeRoot.add(DefaultMutableTreeNode("No selected files"))
        } else {
            processFile(files[0], treeRoot)
        }
    }

    private fun processFile(file: VirtualFile, treeRoot: DefaultMutableTreeNode) {
        val psiFile = PsiManager.getInstance(project).findFile(file)
        if (psiFile is GherkinFile) {
            fillStepsData(psiFile, treeRoot)
        } else {
            treeRoot.add(DefaultMutableTreeNode("${file.name} is not a Feature file"))
        }
    }

    private fun fillStepsData(psiFile: PsiFile, treeRoot: DefaultMutableTreeNode) {
        val treeContent = calculateTreeContent(psiFile)

        treeContent.modules.values.forEach { module ->
            val moduleNode = ModuleTreeNode(module)

            module.files.values.forEach { file ->
                val fileNode = ClassTreeNode(file)

                file.stepDefs.forEach { stepDef ->
                    val stepDefNode = StepDefTreeNode(stepDef)
                    if (!stepDef.params.isNullOrEmpty()) {
                        val stepParamsNode = DefaultMutableTreeNode("params -> ${stepDef.params.joinToString(" | ")}")
                        stepDefNode.add(stepParamsNode)
                    }
                    fileNode.add(stepDefNode)
                }
                moduleNode.add(fileNode)
            }
            treeRoot.add(moduleNode)
        }
    }

    private fun calculateTreeContent(psiFile: PsiFile): StepDefsTreeContent {
        val stepDefsTreeContent = StepDefsTreeContent()

        val definitionsList = CucumberStepsIndex.getInstance(project).getAllStepDefinitions(psiFile)
        definitionsList.map {
            val psiMethod = it.element as PsiMethod
            val stepDefPsiFile = psiMethod.containingFile
            val regex = it.cucumberRegex ?: ""
            val methodName = psiMethod.name
            val annotation = findCucumberAnnotation(psiMethod)
            val params = it.variableNames
            val file = stepDefPsiFile.name
            val module = ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(stepDefPsiFile.virtualFile)?.name
                    ?: "<no_module>"
            StepDefEntry(regex, methodName, annotation, params, file, module)
        }.forEach(stepDefsTreeContent::addStepDefinition)

        stepDefsTreeContent.stepsCount = definitionsList.size

        return stepDefsTreeContent
    }

    private fun findCucumberAnnotation(psiMethod: PsiMethod): String {
        psiMethod.annotations.forEach {
            it.qualifiedName?.let {
                if (it.startsWith("cucumber.api.java")) {
                    return it.substringAfterLast(".")
                }
            }
        }
        return ""
    }

    private fun trackFiles() {
        val messageBus = project.messageBus

        val connection = messageBus.connect()

        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                DumbService.getInstance(project).runWhenSmart { recreateTree() }
            }

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                DumbService.getInstance(project).runWhenSmart { recreateTree() }
            }

            override fun selectionChanged(event: FileEditorManagerEvent) {
                DumbService.getInstance(project).runWhenSmart { recreateTree() }
            }
        })
    }
}