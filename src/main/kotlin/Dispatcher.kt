import com.intellij.openapi.components.ServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val log = LogCategory("Dispatcher")

private fun String?.notEmpty(): String? =
    if (this?.isNotEmpty() == true) this else null

class Config(configFile: File) {

    companion object {
        private val reDriveRoot = """\A\w:\\""".toRegex()
        private val reComment = """\A#.+""".toRegex()
        @Suppress("RegExpRedundantEscape")
        private val reSection = """\A\[([^\]]*)]""".toRegex()

        const val SECTION_SETTINGS = "settings"
    }

    // map of section to list of file name.
    val map = ConcurrentHashMap<String, ArrayList<File>>()

    private val settings = ConcurrentHashMap<String, String>()

    fun command() = settings["command"]
    private fun validateFile() = settings["validateFile"]?.toBoolean() ?: true
    fun destroyPreviousProcess() = settings["destroyPreviousProcess"]?.toBoolean() ?: true

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
                            if (validateFile() && !file.exists()) {
                                log.w("$configFile $lineNum : file not exists. ${file.canonicalPath}")
                            } else {
                                section.add(file)
                            }
                        }
                    }
                }
            }
        } catch (ex: Throwable) {
            log.e(ex, "can't read config file. $configFile")
        }
    }
}

class PlayItem(val file: File, val event: String)

class SuspendPlayer(config: Config) {
    companion object {
        private val reMacro = "\\\$\\{(\\w+)}".toRegex()
        private val runtime = Runtime.getRuntime()
    }

    private val commandBase = config.command()
    private val destroyPreviousProcess = config.destroyPreviousProcess()
    private val channel = Channel<PlayItem>(capacity = Channel.UNLIMITED)
    private var lastProcess: WeakReference<Process>? = null

    init {
        if (commandBase == null) {
            log.w("missing command=… in [settings] section.")
            try {
                channel.close()
            } catch (_: Throwable) {
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                while (true) {
                    val item = try {
                        channel.receive()
                    } catch (ex: Throwable) {
                        log.e(ex, "channel.receive failed.")
                        break
                    }

                    val command = try {
                        reMacro.replace(commandBase) {
                            when (val word = it.groupValues[1]) {
                                "file" -> "\"${item.file.canonicalPath}\""
                                "event" -> item.event
                                else -> "\${$word}"
                            }
                        }
                    } catch (ex: Throwable) {
                        log.e(ex, "reMacro.replace failed. $commandBase")
                        continue
                    }

                    if (destroyPreviousProcess) {
                        try {
                            lastProcess?.get()?.destroy()
                        } catch (ex: Throwable) {
                            log.e(ex, "Process.destroy failed.")
                        }
                    }

                    try {
                        lastProcess = WeakReference(runtime.exec(command))
                    } catch (ex: IOException) {
                        log.e(ex, "command execution failed. $command")
                    }
                }
            }
        }
    }

    fun play(item: PlayItem) {
        if (commandBase == null) {
            log.w("missing command=… in [settings] section.")
            try {
                channel.close()
            } catch (_: Throwable) {
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    channel.send(item)
                } catch (ex: Throwable) {
                    log.e(ex, "channel.send failed.")
                }
            }
        }
    }

    fun close() {
        try {
            channel.close()
        } catch (ex: Throwable) {
            log.e(ex, "channel.close failed.")
        }
    }
}

enum class Events {
    BeforeCompile,
    BuildSuccess,
    BuildError,
    BuildWarning,
}

object Dispatcher {

    private var config: Config? = null
    private var player: SuspendPlayer? = null

    private val lastEventTime = ConcurrentHashMap<Events, Long>()

    init {
        reloadConfig()
    }

    fun dispatch(event: Events) {
        log.i("dispatch $event")

        val lastTime = lastEventTime[event] ?: 0
        val now = System.currentTimeMillis()
        if (now - lastTime < 1000L) {
            log.i("ignore rapid event $event")
            return
        }
        lastEventTime[event] = now

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
            player?.play(PlayItem(file, event.name))
        } catch (ex: Throwable) {
            log.e(ex, "dispatch failed.")
        }
    }

    fun reloadConfig() {
        val config = try {
            val state = ServiceManager.getService(MyPersistentState::class.java)
            val path = state?.configPath?.trim()?.notEmpty()
                ?: """C:\kotlin\MakinoVoice\config.txt"""
            Config(File(path))
        } catch (ex: Throwable) {
            log.e(ex, "fileList load failed.")
            null
        }
        this.config = config
        if (config != null) {
            player?.close()
            player = SuspendPlayer(config)
        }
    }
}