import settingsUi.MyPersistentState
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Config private constructor(
    private val configFile: File,
    private val lastModified: Long? = configFile.lastModifiedOrNull()
) {
    companion object {
        private val log = LogCategory("Config")
        private val reDriveRoot = """\A\w:\\""".toRegex()
        private val reComment = """\A#.+""".toRegex()
        private val reSection = """\A\[([^]]*)]""".toRegex()
        private val reMacro = """\$\{([^}]*)}""".toRegex()

        private const val SECTION_SETTINGS = "settings"

        private fun getConfigFile() = File(
                MyPersistentState.service.configPath.trim().notEmpty()
                ?: """C:\kotlin\MakinoVoice\config.txt"""
        )

        private fun File.lastModifiedOrNull() =
            try {
                lastModified().notZero()
            } catch (ex: Throwable) {
                log.e(ex, "lastModified() failed. $this")
                0L
            }

        private var config = Config(getConfigFile())

        val latest: Config
            get() {
                val newConfigFile = getConfigFile()
                val newLastModified = newConfigFile.lastModifiedOrNull()
                if (newConfigFile != config.configFile || newLastModified != config.lastModified) {
                    log.i("reload")
                    config = Config(newConfigFile, newLastModified)
                }
                return config
            }
    }

    // map of sections that contains file list.
    private val map = ConcurrentHashMap<String, ArrayList<File>>()

    fun getFileFromEvent(eventName: String) =
        map[eventName]?.random()

    // key=value pairs for settings.
    private val settings = ConcurrentHashMap<String, String>()

    private val validateFile: Boolean
        get() = settings["validateFile"]?.toBoolean() ?: true

    val destroyPreviousProcess: Boolean
        get() = settings["destroyPreviousProcess"]?.toBoolean() ?: true


    fun command(map: Map<String, String>): String =
        try {
            reMacro.replace(settings["command"] ?: "") {
                val keyword = it.groupValues[1]
                map[keyword] ?: "\${$keyword}"
            }
        } catch (ex: Throwable) {
            log.e(ex, "reMacro.replace failed. ${settings["command"]}")
            ""
        }


    init {
        val parent = configFile.parentFile

        try {

            BufferedReader(InputStreamReader(FileInputStream(configFile), "UTF-8")).use { reader ->
                var lineNum = 0
                var sectionName: String? = SECTION_SETTINGS
                var fileList: ArrayList<File>? = null

                while (true) {
                    ++lineNum

                    val line = reader.readLine()?.trim()
                        ?: break

                    if (line.isEmpty() || reComment.matches(line))
                        continue

                    // [section]
                    val sv = reSection.find(line)?.groupValues?.get(1)
                    if (sv != null) {
                        sectionName = sv
                        log.i("section $sectionName")

                        if (sectionName == SECTION_SETTINGS) {
                            fileList = null
                        } else {
                            fileList = map[sectionName]
                            if (fileList == null) {
                                fileList = ArrayList()
                                map[sectionName] = fileList
                            }
                        }
                        continue
                    }

                    when {
                        // settings セクションは key=value ペアを保持する
                        sectionName == SECTION_SETTINGS -> {
                            val cols = line.split("=", limit = 2).map { it.trim() }
                            when (cols.size) {
                                2 -> settings[cols[0]] = cols[1]
                                1 -> settings[cols[0]] = cols[0]
                                else -> log.w("settings: invalid line $line")
                            }
                        }

                        // 他のセクションが出現していない
                        fileList == null -> {
                            log.w("$configFile $lineNum : section is not yet specified in previous lines.")
                        }

                        else -> {
                            val head = line.firstOrNull()
                            val file = if (head == '/' || head == '\\' || reDriveRoot.matches(line)) {
                                File(line)
                            } else {
                                File(parent, line)
                            }
                            if (validateFile && !file.exists()) {
                                log.w("$configFile $lineNum : file not exists. ${file.canonicalPath}")
                            } else {
                                fileList.add(file)
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
