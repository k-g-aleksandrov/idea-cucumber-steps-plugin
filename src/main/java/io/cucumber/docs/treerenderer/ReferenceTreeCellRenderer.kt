package io.cucumber.docs.treerenderer

import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.UIUtil
import io.cucumber.docs.model.StepDefEntry
import io.cucumber.docs.model.StepDefsClass
import io.cucumber.docs.model.StepDefsModule
import java.awt.Color
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class ReferenceTreeCellRenderer : ColoredTreeCellRenderer() {

    private val annotationTextStyle: SimpleTextAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, Color.decode(if (UIUtil.isUnderDarcula()) "#CC7832" else "#000080"))

    override fun customizeCellRenderer(tree: JTree, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean,
                                       row: Int, hasFocus: Boolean) {

        when (value) {
            is ModuleTreeNode -> {
                this.icon = value.icon
                val module = value.userObject as StepDefsModule
                append(module.name.removeSuffix("_main").removeSuffix("_test"))
            }
            is ClassTreeNode -> {
                this.icon = value.icon
                val classObject = value.userObject as StepDefsClass
                append(classObject.name.removeSuffix(".java").removeSuffix(".kt"))
                append(" - ")
                append(classObject.stepsCount.toString(), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES)
            }
            is StepDefTreeNode -> {
                this.icon = value.icon
                val stepDefObject = value.userObject as StepDefEntry
                append(stepDefObject.annotation, annotationTextStyle)
                append(" ")
                append(stepDefObject.regex.removeSuffix("$").removePrefix("^"))
            }
            is DefaultMutableTreeNode -> append(value.userObject.toString())
        }
    }
}