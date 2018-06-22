package io.cucumber.docs.treerenderer;

import icons.CucumberDocsPluginIcons
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode

class StepDefTreeNode(any: Any) : DefaultMutableTreeNode(any) {

    val icon: Icon = CucumberDocsPluginIcons.STEP_DEF
}
