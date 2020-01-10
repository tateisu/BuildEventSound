package settingsUi

import BuildEvent
import LogCategory
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent
import javax.swing.JTextField


class MyConfigurable : Configurable {

    companion object {
        private val log = LogCategory("settingsUi.MyConfigurable")

        val state: MyPersistentState
            get() = ServiceManager.getService(MyPersistentState::class.java)!!
    }

    private var tfConfigPath : JTextField? = null

    @Nls
    override fun getDisplayName(): String {
        return "BuildEvent Sound"
    }

    override fun getHelpTopic(): String {
        return "preference.BuildEventSound"
    }

    override fun createComponent(): JComponent {
        log.w("createComponent")
        val form1 = Form1()
        this.tfConfigPath = form1.textFieldConfigPath
        reset()
        return form1.rootPanel
    }

    override fun disposeUIResources() {
        log.w("disposeUIResources")
        tfConfigPath = null
    }

    override fun apply() {
        log.w("apply")
        state.configPath = (tfConfigPath?.text ?: "")
        BuildEvent.reload()
    }

    override fun isModified(): Boolean {
        log.w("isModified")
        return state.configPath != (tfConfigPath?.text ?: "")
    }

    override fun reset() {
        log.w("reset")
        tfConfigPath?.text = state.configPath
    }
}
