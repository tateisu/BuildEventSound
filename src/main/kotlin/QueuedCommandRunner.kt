import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class QueuedCommandRunner(private val config: Config) {
    companion object {
        private val log = LogCategory("QueuedCommandRunner")

        private val reMacro = """\$\{(\w+)}""".toRegex()

        private val runtime = Runtime.getRuntime()
    }

    private val channel = Channel<BuildEvent>(capacity = Channel.UNLIMITED)

    private var lastProcess: WeakReference<Process>? = null
    private val lastEventTime = ConcurrentHashMap<BuildEvent, Long>()

    init {
        start()
    }

    fun enqueue(event: BuildEvent) = GlobalScope.launch(Dispatchers.IO) {
        try {
            channel.send(event)
        } catch (ex: Throwable) {
            log.w(ex, "channel.send failed.")
        }
    }

    fun close() {
        try {
            channel.close()
        } catch (ex: Throwable) {
            log.w(ex, "channel.close failed.")
        }
    }

    private fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                val event = try {
                    channel.receive()
                } catch (ex: Throwable) {
                    log.e(ex, "channel.receive failed.")
                    break
                }

                try {
                    handleEvent(event)
                } catch (ex: Throwable) {
                    log.e(ex, "handleEvent failed.")
                }
            }
        }
    }

    private fun handleEvent(event: BuildEvent) {

        // 同じイベントを処理するのは1秒に1回まで
        val lastTime = lastEventTime[event] ?: 0
        val now = System.currentTimeMillis()
        if (now - lastTime < 1000L) {
            log.i("ignore rapid event $event")
            return
        }
        lastEventTime[event] = now

        config.reloadIfModified()

        val file = config.getFileFromEvent(event.name)
        if (file == null) {
            log.w("missing file for section ${event.name}")
            return
        }

        val commandBase = config.command
        if (commandBase.isBlank()) {
            log.w("missing command=… in [settings] section.")
            return
        }

        val command = try {
            reMacro.replace(commandBase) {
                when (val word = it.groupValues[1]) {
                    "file" -> "\"${file.canonicalPath}\""
                    "event" -> event.name
                    else -> "\${$word}"
                }
            }
        } catch (ex: Throwable) {
            log.e(ex, "reMacro.replace failed. ${config.command}")
            return
        }

        if (config.destroyPreviousProcess) {
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
