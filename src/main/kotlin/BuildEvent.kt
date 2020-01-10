enum class BuildEvent {
    BeforeCompile,
    BuildSuccess,
    BuildError,
    BuildWarning,
    TestPassed,
    TestDefect,

    ;

    companion object {
        private val log = LogCategory("BuildEvent")

        private var runner: QueuedCommandRunner? = null

        init {
            reload()
        }

        fun reload() {
            val config = try {
                Config()
            } catch (ex: Throwable) {
                log.e(ex, "config load failed.")
                return
            }
            runner?.close()
            runner = QueuedCommandRunner(config)
        }
    }

    fun dispatch() {
        log.i("dispatch $this")
        runner?.enqueue(this)
    }
}
