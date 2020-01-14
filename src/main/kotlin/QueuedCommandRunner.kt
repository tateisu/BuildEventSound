import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object QueuedCommandRunner {

    private val log = LogCategory("QueuedCommandRunner")

    private val runtime = Runtime.getRuntime()

    private val channel = Channel<BuildEvent>(capacity = Channel.UNLIMITED)

    private val lastEventTime = ConcurrentHashMap<BuildEvent, Long>()

    private var lastProcess: WeakReference<Process>? = null

    init {
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

    fun post(event: BuildEvent) = GlobalScope.launch(Dispatchers.IO) {
        try {
            channel.send(event)
        } catch (ex: Throwable) {
            log.w(ex, "channel.send failed.")
        }
    }

    fun handleEvent(event: BuildEvent,blockingTimeout:Long=0L) {

        // 同じイベントを処理するのは1秒に1回まで
        val lastTime = lastEventTime[event] ?: 0
        val now = System.currentTimeMillis()
        if (now - lastTime < 1000L) {
            log.i("ignore rapid event $event")
            return
        }
        lastEventTime[event] = now

        val config = Config.latest

        val file = config.getFileFromEvent(event.name)
        if (file == null) {
            log.w("missing file in section ${event.name}")
            return
        }

        val command = config.command(
            mapOf(
                "file" to "\"${file.canonicalPath}\"",
                "event" to event.name
            )
        )

        if (command.isBlank()) {
            log.w("missing command setting.")
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
            val process = runtime.exec(command)
            lastProcess = WeakReference(process)
            if(blockingTimeout > 0L){
                val exited = process.waitFor(blockingTimeout, TimeUnit.MILLISECONDS)
                if(!exited) log.w("process is not exited.")
            }
        } catch (ex: Throwable) {
            log.e(ex, "command execution failed. $command")
        }
    }
}
