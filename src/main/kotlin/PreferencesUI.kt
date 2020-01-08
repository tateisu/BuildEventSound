import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import org.jetbrains.annotations.Nls
import javax.swing.JComponent
import javax.swing.JTextField


class PreferencesUI : Configurable {

    companion object{
        private val log = LogCategory("PreferencesUI")

        private val state :MyPersistentState?
            get()= ServiceManager.getService(MyPersistentState::class.java)
    }

    private var form1 :Form1? = null
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
        val gui = Form1()
        val tfConfigPath = gui.textFieldConfigPath

        this.form1 = gui
        this.tfConfigPath = tfConfigPath

        loadSettings()

        return gui.rootPanel
    }

    override fun disposeUIResources() {
        log.w("disposeUIResources")
        tfConfigPath = null
        form1 = null
    }

    private fun loadSettings(){
        log.w("loadSettings")
        val tfConfigPath = this.tfConfigPath
        if(tfConfigPath==null){
            log.w("loadSettings(): missing tfConfigPath")
            return
        }

        val state = state
        if(state==null){
            log.w("loadSettings(): missing state")
            return
        }

        tfConfigPath.text = state.configPath
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        log.w("apply")
        val tfConfigPath = this.tfConfigPath
        if(tfConfigPath==null){
            log.w("apply(): missing tfConfigPath")
            return
        }

        val state = state
        if(state==null){
            log.w("apply(): missing state")
            return
        }

        state.configPath = tfConfigPath.text
        Dispatcher.reloadConfig()
    }

    override fun isModified(): Boolean {
        log.w("isModified")
        val tfConfigPath = this.tfConfigPath
        if(tfConfigPath==null){
            log.w("isModified(): missing tfConfigPath")
            return false
        }

        val state = state
        if(state==null){
            log.w("isModified(): missing state")
            return false
        }

        return state.configPath != tfConfigPath.text
    }

    override fun reset() {
        log.w("reset")
        loadSettings()
    }
}