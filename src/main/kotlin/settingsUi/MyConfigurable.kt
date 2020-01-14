package settingsUi

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent
import javax.swing.JTextField

class MyConfigurable : Configurable {

    companion object{
        private val service
            get() = MyPersistentState.service
    }

    private var tfConfigPath: JTextField? = null

    private var uiConfigPath :String
        get() = (tfConfigPath?.text ?: "")
        set(value){
            tfConfigPath?.text = value
        }

    @Nls
    override fun getDisplayName(): String = "BuildEvent Sound"

    override fun getHelpTopic(): String = "preference.BuildEventSound"

    override fun createComponent(): JComponent {
        val form1 = Form1()
        this.tfConfigPath = form1.tfConfigPath
        reset()
        return form1.rootPanel
    }

    override fun disposeUIResources() {
        tfConfigPath = null
    }

    override fun apply() {
        service.configPath = uiConfigPath
    }

    override fun reset() {
        uiConfigPath = service.configPath
    }

    override fun isModified(): Boolean =
        service.configPath != uiConfigPath
}
