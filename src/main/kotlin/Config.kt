import settingsUi.MyConfigurable
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Config {

    companion object {
        private val log = LogCategory("Config")
        private val reDriveRoot = """\A\w:\\""".toRegex()
        private val reComment = """\A#.+""".toRegex()
        private val reSection = """\A\[([^]]*)]""".toRegex()

        const val SECTION_SETTINGS = "settings"

        private fun getConfigFile() = File(
            MyConfigurable.state.configPath.trim().notEmpty()
                ?: """C:\kotlin\MakinoVoice\config.txt"""
        )
    }

    // map of sections that contains file list.
    val map = ConcurrentHashMap<String, ArrayList<File>>()

    private val settings = ConcurrentHashMap<String, String>()

    val command: String
        get() = settings["command"] ?: ""

    val destroyPreviousProcess: Boolean
        get() = settings["destroyPreviousProcess"]?.toBoolean() ?: true

    private val validateFile: Boolean
        get() = settings["validateFile"]?.toBoolean() ?: true

    init {
        val configFile = getConfigFile()
        val parent = configFile.parentFile
        try {
            BufferedReader(InputStreamReader(FileInputStream(configFile), "UTF-8")).use { reader ->
                var lineNum = 0
                var sectionName: String? = SECTION_SETTINGS
                var fileList: ArrayList<File>? = null

                loop@ while (true) {
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
