package settingsUi

import LogCategory
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean
import org.jetbrains.annotations.Nullable

@State(
    name="BuildEventSoundPersistentState",
    storages = [Storage("BuildEventSoundPersistentState.xml")]
)
class MyPersistentState : PersistentStateComponent<MyPersistentState> {

    companion object{
        private val log = LogCategory("MyPersistentState")

        var service: MyPersistentState = try {
            ServiceManager.getService(MyPersistentState::class.java)!!
        }catch(ex:Throwable){
            log.w(ex,"ServiceManager.getService failed.")
            // ユニットテスト時に発生する。ダミーのオブジェクトを用意する
            MyPersistentState()
        }
    }

    var configPath: String = ""

    @Nullable
    override fun getState(): MyPersistentState {
        return this
    }

    override fun loadState(state: MyPersistentState) {
        copyBean(state, this)
    }
}