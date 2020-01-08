import com.intellij.openapi.components.ServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val log = LogCategory("Dispatcher")

private fun String?.notEmpty():String? =
    if(this?.isNotEmpty()==true) this else null

class Config(configFile : File) {

    companion object {
        private val reDriveRoot = """\A\w:\\""".toRegex()
        private val reComment = """\A#.+""".toRegex()
        @Suppress("RegExpRedundantEscape")
        private val reSection = """\A\[([^\]]*)]""".toRegex()

        private const val SECTION_SETTINGS = "settings"
    }

    // map of section to list of file name.
    val map = ConcurrentHashMap<String, ArrayList<File>>()

    val settings = ConcurrentHashMap<String, String>()

    init {
        val parent = configFile.parentFile
        try {
            BufferedReader(InputStreamReader(FileInputStream(configFile), "UTF-8")).use { reader ->
                var lineNum = 0
                var section: ArrayList<File>? = null
                var sectionName: String? = null

                loop@ while (true) {
                    ++lineNum
                    val line = reader.readLine()?.trim() ?: break
                    val tmpSectionName = reSection.find(line)?.groupValues?.get(1)
                    when {
                        reComment.matches(line) || line.isEmpty() -> {

                        }

                        tmpSectionName != null -> {
                            sectionName = tmpSectionName
                            log.i("section $sectionName")
                            if (sectionName != SECTION_SETTINGS) {
                                section = map[sectionName]
                                if (section == null) {
                                    section = ArrayList()
                                    map[sectionName] = section
                                }
                            }
                        }
                        sectionName == SECTION_SETTINGS -> {
                            val cols = line.split("=", limit = 2).map { it.trim() }
                            when (cols.size) {
                                2 -> settings[cols[0]] = cols[1]
                                1 -> settings[cols[0]] = cols[0]
                                else -> log.w("settings: invalid line $line")
                            }
                        }
                        section == null -> {
                            log.w("$configFile $lineNum : section is not yet specified in previous lines.")
                        }
                        else -> {
                            val head = line.firstOrNull()
                            val file = if (head == '/' || head == '\\' || reDriveRoot.matches(line)) {
                                File(line)
                            } else {
                                File(parent, line)
                            }
                            if (!file.isFile) {
                                log.w("$configFile $lineNum : not a file. ${file.canonicalPath}")
                            } else {
                                section.add(file)
                            }
                        }
                    }
                }
            }
        }catch(ex:Throwable){
            log.e(ex,"can't read config file. $configFile")
        }
    }
}

class SuspendPlayer(private val player : String?) {
    private val channel = Channel<File>(capacity = Channel.UNLIMITED )

    init {
        if (player == null) {
            log.w("missing player=... in [settings] section.")
            try {
                channel.close()
            }catch(_:Throwable){
            }
        }else{
            GlobalScope.launch(Dispatchers.IO){
                while (true) {
                    val file = try {
                        channel.receive()
                    } catch (ex : Throwable) {
                        log.e(ex, "channel.receive failed.")
                        break
                    }
                    GlobalScope.launch(Dispatchers.IO){
                        try {
                            val command = player.replace("\${file}", "\"${file.canonicalPath}\"")
                            Runtime.getRuntime().exec(command)
                        } catch (ex : IOException) {
                            log.e(ex, "command execution failed. $file $player")
                        }
                    }
                }
            }
        }
    }

    fun play(file : File) {
        if (player == null) {
            log.w("missing player=... in [settings] section.")
            try {
                channel.close()
            }catch(_:Throwable){
            }
        }else {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    channel.send(file)
                } catch (ex: Throwable) {
                    log.e(ex, "channel.send failed.")
                }
            }
        }
    }

    fun close() {
        try {
            channel.close()
        } catch (ex : Throwable) {
            log.e(ex, "channel.close failed.")
        }
    }
}

enum class Events {
    BeforeCompile,
    BuildSuccess,
    BuildError,
    // BuildWarning,
}

object Dispatcher {

    private var config = loadConfig()

    private var player = SuspendPlayer(config?.settings?.get("player"))

    private fun loadConfig() = try {
        val state = ServiceManager.getService(MyPersistentState::class.java)
        val path = state?.configPath?.trim()?.notEmpty()
            ?: """C:\kotlin\MakinoVoice\config.txt"""
        Config(File(path))
    } catch (ex : Throwable) {
        log.e(ex,"fileList load failed.")
        null
    }

    fun dispatch(event : Events) {
        log.i("dispatch $event")
        try {
            val config = this.config
            if (config == null) {
                log.w("fileList is null")
                return
            }
            val file = config.map[event.name]?.random()
            if (file == null) {
                log.w("missing file for section ${event.name}")
                return
            }
            player.play(file)
        } catch (ex : Throwable) {
            log.e(ex, "dispatch failed.")
        }
    }

    fun reloadConfig() {
        config = loadConfig()
        player.close()
        player = SuspendPlayer(config?.settings?.get("player"))
    }
}